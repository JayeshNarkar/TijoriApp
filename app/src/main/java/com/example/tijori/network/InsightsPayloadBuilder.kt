package com.example.tijori.network

import com.example.tijori.data.dao.TransactionDao
import com.example.tijori.ui.theme.displayCategoryName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class InsightsPayloadBuilder @Inject constructor(
    private val transactionDao: TransactionDao
) {
    suspend fun build(userId: String, currencySymbol: String): InsightsPayload {
        val calendar = Calendar.getInstance()
        val today = calendar.time

        // Start of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val currentMonthStart = calendar.time

        val currentMonth = buildMonthSummary(
            userId, currentMonthStart, today,
            label = "${monthName(calendar)} 1–${dayOfMonth(today)} (month to date)"
        )

        val priorMonths = (1..3).map { monthsAgo ->
            val monthCal = Calendar.getInstance()
            monthCal.set(Calendar.DAY_OF_MONTH, 1)
            monthCal.add(Calendar.MONTH, -monthsAgo)
            monthCal.set(Calendar.HOUR_OF_DAY, 0)
            monthCal.set(Calendar.MINUTE, 0)
            monthCal.set(Calendar.SECOND, 0)
            val monthStart = monthCal.time

            val endCal = monthCal.clone() as Calendar
            endCal.add(Calendar.MONTH, 1)
            endCal.add(Calendar.MILLISECOND, -1)
            val monthEnd = endCal.time

            buildMonthSummary(userId, monthStart, monthEnd, label = monthName(monthCal) + " " + monthCal.get(Calendar.YEAR))
        }

        val topTransactions = transactionDao.getTopTransactionsBetween(userId, currentMonthStart, today, limit = 5)
        val notable = topTransactions.map {
            NotableTransaction(
                label = it.note?.takeIf { n -> n.isNotBlank() } ?: it.displayCategoryName,
                amount = it.amount,
                date = SimpleDateFormat("MMM d", Locale.getDefault()).format(it.date)
            )
        }

        return InsightsPayload(
            currencySymbol = currencySymbol,
            currentMonth = currentMonth,
            priorMonths = priorMonths,
            notableTransactions = notable
        )
    }

    private suspend fun buildMonthSummary(userId: String, start: Date, end: Date, label: String): MonthSummary {
        val income = transactionDao.getIncomeTotalBetween(userId, start, end)
        val expenses = transactionDao.getExpenseTotalBetween(userId, start, end)
        val expenseCategories = transactionDao.getExpenseBreakdownBetween(userId, start, end)
            .mapNotNull { it.expenseCategory?.let { cat -> CategoryAmount(cat.name, it.total, it.count) } }
        val incomeCategories = transactionDao.getIncomeBreakdownBetween(userId, start, end)
            .mapNotNull { it.incomeCategory?.let { cat -> CategoryAmount(cat.name, it.total, it.count) } }

        return MonthSummary(
            label = label,
            income = income,
            expenses = expenses,
            categoryBreakdown = expenseCategories + incomeCategories
        )
    }

    private fun monthName(cal: Calendar) = SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
    private fun dayOfMonth(date: Date) = SimpleDateFormat("d", Locale.getDefault()).format(date)
}