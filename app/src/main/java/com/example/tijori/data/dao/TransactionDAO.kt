package com.example.tijori.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tijori.data.entities.Transaction
import com.example.tijori.data.entities.ExpenseCategory
import com.example.tijori.data.entities.IncomeCategory
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM Transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Transaction?

    @Query("SELECT * FROM Transactions WHERE upiTransactionId = :upiId LIMIT 1")
    suspend fun findByUpiTransactionId(upiId: String): Transaction?

    @Query("SELECT * FROM Transactions WHERE userId = :userId AND needsReview = 1 ORDER BY date DESC")
    fun getNeedingReview(userId: String): Flow<List<Transaction>>

    @Query(
        """
        SELECT * FROM Transactions 
        WHERE userId = :userId AND needsReview = 0 
        ORDER BY date DESC LIMIT 10
    """
    )
    fun getRecentTransactions(userId: String): Flow<List<Transaction>>

    @Query(
        """
    SELECT * FROM Transactions 
    WHERE userId = :userId 
    AND (:expenseCategory IS NULL OR expenseCategory = :expenseCategory)
    AND (:incomeCategory IS NULL OR incomeCategory = :incomeCategory)
    AND needsReview = 0
    ORDER BY date DESC LIMIT 10
"""
    )
    fun getRecentTransactionsByCategory(
        userId: String, expenseCategory: ExpenseCategory?, incomeCategory: IncomeCategory?
    ): Flow<List<Transaction>>

    @Query("SELECT COUNT(*) FROM Transactions WHERE userId = :userId AND needsReview = 0")
    fun getTransactionCount(userId: String): Flow<Int>

    @Query(
        """
    SELECT COUNT(*) FROM Transactions 
    WHERE userId = :userId 
    AND (:expenseCategory IS NULL OR expenseCategory = :expenseCategory)
    AND (:incomeCategory IS NULL OR incomeCategory = :incomeCategory)
    AND needsReview = 0
    ORDER BY date DESC
"""
    )
    fun getTransactionCountByCategory(
        userId: String, expenseCategory: ExpenseCategory?, incomeCategory: IncomeCategory?
    ): Flow<Int>

    // --- Balance estimation ---
    @Query(
        """
        SELECT COALESCE(SUM(
            CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END
        ), 0.0)
        FROM Transactions
        WHERE userId = :userId AND needsReview = 0
    """
    )
    fun getNetTransactionTotal(userId: String): Flow<Double>

    @Query(
        """
    SELECT COALESCE(SUM(amount), 0.0) FROM Transactions 
    WHERE userId = :userId AND type = 'DEBIT' AND date >= :since AND needsReview = 0
    """
    )
    fun getExpenseTotalSince(userId: String, since: Date): Flow<Double>

    @Query(
        """
    SELECT COALESCE(SUM(amount), 0.0) FROM Transactions 
    WHERE userId = :userId AND type = 'CREDIT' AND date >= :since AND needsReview = 0
    """
    )
    fun getIncomeTotalSince(userId: String, since: Date): Flow<Double>

    @Query(
        """
        SELECT * FROM Transactions 
        WHERE userId = :userId AND needsReview = 0
        ORDER BY date DESC 
        LIMIT :pageSize OFFSET :offset
    """
    )
    suspend fun getTransactionsPage(userId: String, pageSize: Int, offset: Int): List<Transaction>

    data class CategoryTotal(
        val expenseCategory: ExpenseCategory?, val total: Double
    )

    @Query(
        """
        SELECT expenseCategory, SUM(amount) as total FROM Transactions
        WHERE userId = :userId AND type = 'DEBIT' AND date >= :since AND needsReview = 0
        GROUP BY expenseCategory
        ORDER BY total DESC
    """
    )
    fun getExpenseTotalsByCategory(userId: String, since: Date): Flow<List<CategoryTotal>>

    data class TransactionEligibilityStats(
        val count: Int,
        val oldestDate: Date?,
        val newestDate: Date?
    )

    @Query("""
        SELECT COUNT(*) as count, MIN(date) as oldestDate, MAX(date) as newestDate
        FROM Transactions
        WHERE userId = :userId AND needsReview = 0
    """)
    suspend fun getEligibilityStats(userId: String): TransactionEligibilityStats
}