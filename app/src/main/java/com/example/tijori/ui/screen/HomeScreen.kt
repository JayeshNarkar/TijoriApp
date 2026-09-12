package com.example.tijori.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.tijori.data.entities.CategoryFilter
import com.example.tijori.data.entities.ExpenseCategory
import com.example.tijori.data.entities.IncomeCategory
import com.example.tijori.data.entities.ThemeMode
import com.example.tijori.data.entities.Transaction
import com.example.tijori.ui.components.AllCategoryChip
import com.example.tijori.ui.components.CategoryChip
import com.example.tijori.ui.components.TransactionRow
import com.example.tijori.ui.components.HomeHeader
import com.example.tijori.ui.components.ReviewTransactionDialog
import com.example.tijori.ui.components.SummaryCard
import com.example.tijori.ui.theme.color
import com.example.tijori.ui.viewmodel.AppConfigDBViewModel
import com.example.tijori.ui.viewmodel.AppConfigLoadState
import com.example.tijori.ui.viewmodel.TransactionDBViewModel
import com.example.tijori.ui.viewmodel.UserDBViewModel
import com.example.tijori.ui.viewmodel.UserLoadState
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userViewModel: UserDBViewModel = hiltViewModel(),
    configViewModel: AppConfigDBViewModel = hiltViewModel(),
    transactionViewModel: TransactionDBViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToViewAllTransactions: () -> Unit = {}
) {
    val userState by userViewModel.currentUserState.collectAsState()
    val user = (userState as? UserLoadState.Loaded)?.user

    val configState by configViewModel.configState.collectAsState()
    val config = (configState as? AppConfigLoadState.Loaded)?.config

    val needsReview by (user?.let { transactionViewModel.getTransactionsNeedingReview(it.id) }
        ?: emptyFlow()).collectAsState(initial = emptyList())

    var reviewingTransaction by remember { mutableStateOf<Transaction?>(null) }

    var selectedCategory by remember { mutableStateOf<CategoryFilter?>(null) }

    val transactionFlow = remember(user, selectedCategory) {
        user?.let {
            when (val filter = selectedCategory) {
                null -> transactionViewModel.getTransactions(it.id, null, null)
                is CategoryFilter.Expense -> transactionViewModel.getTransactions(it.id, filter.category, null)
                is CategoryFilter.Income -> transactionViewModel.getTransactions(it.id, null, filter.category)
            }
        } ?: emptyFlow()
    }
    val transactions by transactionFlow.collectAsState(initial = emptyList())

    val transactionCountFlow = remember(user, selectedCategory) {
        user?.let {
            when (val filter = selectedCategory) {
                null -> transactionViewModel.getTransactionCount(it.id, null, null)
                is CategoryFilter.Expense -> transactionViewModel.getTransactionCount(it.id, filter.category, null)
                is CategoryFilter.Income -> transactionViewModel.getTransactionCount(it.id, null, filter.category)
            }
        } ?: emptyFlow()
    }

    val transactionCount by transactionCountFlow.collectAsState(initial = 0)

    val expenseTotalFlow = remember(user, config?.startingBalanceDate) {
        if (user != null && config != null) {
            transactionViewModel.getExpenseTotalSince(user.id, config.startingBalanceDate)
        } else emptyFlow()
    }
    val totalExpenses by expenseTotalFlow.collectAsState(initial = 0.0)

    val incomeTotalFlow = remember(user, config?.startingBalanceDate) {
        if (user != null && config != null) {
            transactionViewModel.getIncomeTotalSince(user.id, config.startingBalanceDate)
        } else emptyFlow()
    }
    val totalIncome by incomeTotalFlow.collectAsState(initial = 0.0)

    val estimatedBalance = (config?.startingBalance ?: 0.0) + totalIncome - totalExpenses
    val currSymbol = config?.currencySymbol ?: "₹"

    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                HomeHeader(
                    firstName = user?.firstName ?: "User",
                    themeMode = config?.themeMode ?: ThemeMode.SYSTEM,
                    onThemeModeChange = { newMode -> configViewModel.updateThemeMode(newMode) },
                    onNavigateToSettings = onNavigateToSettings
                )

                Text(
                    modifier = Modifier.padding(PaddingValues(start = 16.dp, top = 15.dp)),
                    text = "Summary",
                    style = MaterialTheme.typography.headlineSmall
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val minimumBalance = config?.minimumBalance

                    if (minimumBalance != null) {
                        val usableBalance = estimatedBalance - minimumBalance

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SummaryCard(
                                label = "Estimated Balance",
                                value = "$currSymbol${"%,.2f".format(estimatedBalance)}",
                                icon = Icons.Filled.AccountBalanceWallet,
                                iconColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                label = "Usable Balance",
                                value = "$currSymbol${"%,.2f".format(usableBalance)}",
                                icon = Icons.Filled.Savings,
                                iconColor = if (usableBalance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        SummaryCard(
                            label = "Estimated Balance",
                            value = "$currSymbol${"%,.2f".format(estimatedBalance)}",
                            icon = Icons.Filled.AccountBalanceWallet,
                            iconColor = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryCard(
                            label = "Total Income",
                            value = "$currSymbol${"%,.2f".format(totalIncome)}",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            iconColor = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            label = "Total Expenses",
                            value = "$currSymbol${"%,.2f".format(totalExpenses)}",
                            icon = Icons.Filled.CreditCard,
                            iconColor = Color(0xFFE57373),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (needsReview.isNotEmpty()) {
                    Text(
                        text = "Needs Review",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        needsReview.forEach { transaction ->
                            TransactionRow(
                                currSymbol = config?.currencySymbol,
                                transaction = transaction,
                                onClick = { reviewingTransaction = transaction }
                            )
                        }
                    }
                }

                Text(
                    modifier = Modifier.padding(PaddingValues(start = 16.dp, top = 15.dp)),
                    text = "Categories",
                    style = MaterialTheme.typography.headlineSmall
                )
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    item {
                        AllCategoryChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null }
                        )
                    }
                    items(IncomeCategory.entries) { category ->
                        CategoryChip(
                            label = category.name,
                            icon = category.icon,
                            color = category.color,
                            selected = selectedCategory == CategoryFilter.Income(category),
                            onClick = { selectedCategory = CategoryFilter.Income(category) }
                        )
                    }
                    items(ExpenseCategory.entries) { category ->
                        CategoryChip(
                            label = category.name,
                            icon = category.icon,
                            color = category.color,
                            selected = selectedCategory == CategoryFilter.Expense(category),
                            onClick = { selectedCategory = CategoryFilter.Expense(category) }
                        )
                    }
                }

                Text(
                    modifier = Modifier.padding(PaddingValues(start = 16.dp, top = 5.dp)),
                    text = "Transactions",
                    style = MaterialTheme.typography.headlineSmall
                )

                if (transactions.isEmpty()) {
                    Text(
                        text = "No transactions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        transactions.forEach { transaction ->
                            TransactionRow(
                                currSymbol = config?.currencySymbol,
                                transaction = transaction,
                                onClick = { reviewingTransaction = transaction }
                            )
                        }

                        if (transactionCount > 10) {
                            TextButton(
                                onClick = onNavigateToViewAllTransactions,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("View All")
                            }
                        }
                    }
                }
            }

            reviewingTransaction?.let { transaction ->
                ReviewTransactionDialog(
                    transaction = transaction,
                    onConfirm = { type, expenseCategory, incomeCategory, note, date ->
                        transactionViewModel.reviewExpense(
                            transaction.id,
                            expenseCategory,
                            incomeCategory,
                            note,
                            date,
                            type
                        )
                    },
                    onDelete = {
                        transactionViewModel.deleteTransaction(transaction.id)
                        reviewingTransaction = null
                    },
                    onDismiss = { reviewingTransaction = null }
                )
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}