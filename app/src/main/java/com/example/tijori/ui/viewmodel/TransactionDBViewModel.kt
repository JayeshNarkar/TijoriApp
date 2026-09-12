package com.example.tijori.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tijori.data.dao.TransactionDao
import com.example.tijori.data.entities.ExpenseCategory
import com.example.tijori.data.entities.IncomeCategory
import com.example.tijori.data.entities.Transaction
import com.example.tijori.data.entities.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TransactionDBViewModel @Inject constructor(
    private val transactionDao: TransactionDao
) : ViewModel() {
    fun getTransactionsNeedingReview(userId: String) = transactionDao.getNeedingReview(userId)

    fun reviewExpense(transactionId: String, expenseCategory: ExpenseCategory?, incomeCategory: IncomeCategory?, note: String?, date: Date, type: TransactionType) {
        viewModelScope.launch {
            val transaction = transactionDao.getById(transactionId) ?: return@launch
            transactionDao.update(
                transaction.copy(
                    expenseCategory = expenseCategory,
                    incomeCategory = incomeCategory,
                    note = note,
                    date = date,
                    needsReview = false,
                    type = type
                )
            )
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            val transaction = transactionDao.getById(transactionId) ?: return@launch
            transactionDao.delete(transaction)
        }
    }

    fun getTransactions(userId: String, expenseCategory: ExpenseCategory?, incomeCategory: IncomeCategory?): Flow<List<Transaction>> {
        return if (expenseCategory == null && incomeCategory == null) {
            transactionDao.getRecentTransactions(userId)
        } else {
            transactionDao.getRecentTransactionsByCategory(userId, expenseCategory, incomeCategory)
        }
    }

    fun getTransactionCount(userId: String,expenseCategory:ExpenseCategory?, incomeCategory: IncomeCategory?): Flow<Int> {
        return if (expenseCategory == null && incomeCategory == null) {
            transactionDao.getTransactionCount(userId)
        } else {
            transactionDao.getTransactionCountByCategory(userId, expenseCategory, incomeCategory)
        }
    }

    fun getExpenseTotalSince(userId: String, since: Date): Flow<Double> =
        transactionDao.getExpenseTotalSince(userId, since)

    fun getIncomeTotalSince(userId: String, since: Date): Flow<Double> =
        transactionDao.getIncomeTotalSince(userId, since)

    fun addTransaction(
        userId: String,
        amount: Double,
        type: TransactionType,
        expenseCategory: ExpenseCategory?,
        incomeCategory: IncomeCategory?,
        note: String?,
        date: Date
    ) {
        viewModelScope.launch {
            transactionDao.insert(
                Transaction(
                    userId = userId,
                    amount = amount,
                    type = type,
                    expenseCategory = expenseCategory,
                    incomeCategory = incomeCategory,
                    note = note,
                    date = date,
                    needsReview = false
                )
            )
        }
    }

    fun getExpenseTotalsByCategory(userId: String, since: Date): Flow<List<TransactionDao.CategoryTotal>> =
        transactionDao.getExpenseTotalsByCategory(userId, since)

    private val pageSize = 30

    private val _pagedTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val pagedTransactions: StateFlow<List<Transaction>> = _pagedTransactions.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentOffset = 0
    private var reachedEnd = false
    private var currentUserId: String? = null

    fun resetAndLoadFirstPage(userId: String) {
        currentUserId = userId
        currentOffset = 0
        reachedEnd = false
        _pagedTransactions.value = emptyList()
        loadNextPage()
    }

    fun loadNextPage() {
        val userId = currentUserId ?: return
        if (_isLoadingMore.value || reachedEnd) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            val page = transactionDao.getTransactionsPage(userId, pageSize, currentOffset)
            _pagedTransactions.value += page
            currentOffset += page.size
            reachedEnd = page.size < pageSize
            _isLoadingMore.value = false
        }
    }
}