package com.example.tijori.ui.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.tijori.data.entities.Transaction
import com.example.tijori.ui.components.ReviewTransactionDialog
import com.example.tijori.ui.components.TijoriTopBar
import com.example.tijori.ui.components.TransactionRow
import com.example.tijori.ui.viewmodel.AppConfigDBViewModel
import com.example.tijori.ui.viewmodel.AppConfigLoadState
import com.example.tijori.ui.viewmodel.TransactionDBViewModel
import com.example.tijori.ui.viewmodel.UserDBViewModel
import com.example.tijori.ui.viewmodel.UserLoadState

@Composable
fun ViewAllTransactionsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    userViewModel: UserDBViewModel = hiltViewModel(),
    configViewModel: AppConfigDBViewModel = hiltViewModel(),
    transactionViewModel: TransactionDBViewModel = hiltViewModel()
) {
    val userState by userViewModel.currentUserState.collectAsState()
    val user = (userState as? UserLoadState.Loaded)?.user

    val configState by configViewModel.configState.collectAsState()
    val config = (configState as? AppConfigLoadState.Loaded)?.config

    val transactions by transactionViewModel.pagedTransactions.collectAsState()
    val isLoadingMore by transactionViewModel.isLoadingMore.collectAsState()

    var reviewingTransaction by remember { mutableStateOf<Transaction?>(null) }

    // Kick off the first page load once the user is known.
    LaunchedEffect(user) {
        user?.let { transactionViewModel.resetAndLoadFirstPage(it.id) }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= layoutInfo.totalItemsCount - 5
        }.collect { nearBottom ->
            if (nearBottom) transactionViewModel.loadNextPage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TijoriTopBar(title = "All Transactions", onBack = onBack) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionRow(
                        currSymbol = config?.currencySymbol,
                        transaction = transaction,
                        onClick = { reviewingTransaction = transaction }
                    )
                }

                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            if (transactions.isEmpty() && !isLoadingMore) {
                Text(
                    text = "No transactions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    reviewingTransaction?.let { transaction ->
        ReviewTransactionDialog(
            transaction = transaction,
            onConfirm = { type, expenseCategory, incomeCategory, note, date ->
                transactionViewModel.reviewExpense(transaction.id, expenseCategory, incomeCategory, note, date, type)
            },
            onDelete = {
                transactionViewModel.deleteTransaction(transaction.id)
                reviewingTransaction = null
            },
            onDismiss = { reviewingTransaction = null }
        )
    }
}