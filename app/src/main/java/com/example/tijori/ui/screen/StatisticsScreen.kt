package com.example.tijori.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.tijori.data.entities.TimeFrame
import com.example.tijori.data.entities.startDate
import com.example.tijori.ui.components.BalanceSummaryCard
import com.example.tijori.ui.components.CategoryBreakdownCard
import com.example.tijori.ui.components.CategorySlice
import com.example.tijori.ui.components.TijoriTopBar
import com.example.tijori.ui.components.TimeFrameChip
import com.example.tijori.ui.viewmodel.AppConfigDBViewModel
import com.example.tijori.ui.viewmodel.AppConfigLoadState
import com.example.tijori.ui.viewmodel.TransactionDBViewModel
import com.example.tijori.ui.viewmodel.UserDBViewModel
import com.example.tijori.ui.viewmodel.UserLoadState
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    userViewModel: UserDBViewModel = hiltViewModel(),
    configViewModel: AppConfigDBViewModel = hiltViewModel(),
    transactionViewModel: TransactionDBViewModel = hiltViewModel()
) {
    val userState by userViewModel.currentUserState.collectAsState()
    val user = (userState as? UserLoadState.Loaded)?.user

    val configState by configViewModel.configState.collectAsState()
    val config = (configState as? AppConfigLoadState.Loaded)?.config

    var selectedTimeFrame by remember { mutableStateOf(TimeFrame.THIS_WEEK) }

    val currSymbol = config?.currencySymbol ?: "₹"

    // --- Estimated balance: independent of the selected timeframe, always
    // measured from startingBalanceDate, same calculation as Home's Summary. ---
    val balanceIncomeFlow = remember(user, config?.startingBalanceDate) {
        if (user != null && config != null) {
            transactionViewModel.getIncomeTotalSince(user.id, config.startingBalanceDate)
        } else emptyFlow()
    }
    val balanceIncome by balanceIncomeFlow.collectAsState(initial = 0.0)

    val balanceExpenseFlow = remember(user, config?.startingBalanceDate) {
        if (user != null && config != null) {
            transactionViewModel.getExpenseTotalSince(user.id, config.startingBalanceDate)
        } else emptyFlow()
    }
    val balanceExpense by balanceExpenseFlow.collectAsState(initial = 0.0)

    val estimatedBalance = (config?.startingBalance ?: 0.0) + balanceIncome - balanceExpense

    // --- Income/Expenses for the selected timeframe only ---
    val timeFrameIncomeFlow = remember(user, selectedTimeFrame) {
        user?.let { transactionViewModel.getIncomeTotalSince(it.id, selectedTimeFrame.startDate()) }
            ?: emptyFlow()
    }
    val timeFrameIncome by timeFrameIncomeFlow.collectAsState(initial = 0.0)

    val timeFrameExpenseFlow = remember(user, selectedTimeFrame) {
        user?.let { transactionViewModel.getExpenseTotalSince(it.id, selectedTimeFrame.startDate()) }
            ?: emptyFlow()
    }
    val timeFrameExpense by timeFrameExpenseFlow.collectAsState(initial = 0.0)

    val categoryTotalsFlow = remember(user, selectedTimeFrame) {
        user?.let { transactionViewModel.getExpenseTotalsByCategory(it.id, selectedTimeFrame.startDate()) }
            ?: emptyFlow()
    }
    val categoryTotals by categoryTotalsFlow.collectAsState(initial = emptyList())

    val totalForPercentages = categoryTotals.sumOf { it.total }
    val categorySlices = categoryTotals.mapNotNull { catTotal ->
        val category = catTotal.expenseCategory ?: return@mapNotNull null
        CategorySlice(
            category = category,
            total = catTotal.total,
            percent = if (totalForPercentages > 0) (catTotal.total / totalForPercentages).toFloat() else 0f
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = { TijoriTopBar(title = "Statistics") }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(TimeFrame.entries) { timeFrame ->
                    TimeFrameChip(
                        timeFrame = timeFrame,
                        selected = timeFrame == selectedTimeFrame,
                        onClick = { selectedTimeFrame = timeFrame }
                    )
                }
            }

            BalanceSummaryCard(
                estimatedBalance = "$currSymbol${"%,.0f".format(estimatedBalance)}",
                income = "$currSymbol${"%,.0f".format(timeFrameIncome)}",
                expenses = "$currSymbol${"%,.0f".format(timeFrameExpense)}",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            CategoryBreakdownCard(
                currSymbol = currSymbol,
                slices = categorySlices,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }
    }
}