package com.example.tijori.network

data class InsightsRequest(
    val summary_text: String
)

data class InsightsResponse(
    val status: String,
    val result: InsightsApiResult? = null,
    val message: String? = null
)

data class InsightsApiResult(
    val summary: String,
    val flags: List<String>
)

data class MonthSummary(
    val label: String,
    val income: Double,
    val expenses: Double,
    val categoryBreakdown: List<CategoryAmount>
)

data class CategoryAmount(
    val category: String,
    val total: Double,
    val count: Int
)

data class NotableTransaction(
    val label: String,
    val amount: Double,
    val date: String
)

data class InsightsPayload(
    val currencySymbol: String,
    val currentMonth: MonthSummary,
    val priorMonths: List<MonthSummary>,
    val notableTransactions: List<NotableTransaction>
) {
    fun toPromptText(): String = buildString {
        appendLine("Currency: $currencySymbol")
        appendLine()
        appendLine("=== ${currentMonth.label} ===")
        appendLine("Income: $currencySymbol${"%.0f".format(currentMonth.income)}")
        appendLine("Expenses: $currencySymbol${"%.0f".format(currentMonth.expenses)}")
        currentMonth.categoryBreakdown.forEach {
            appendLine("  ${it.category}: $currencySymbol${"%.0f".format(it.total)} (${it.count} transactions)")
        }
        appendLine()
        priorMonths.forEach { month ->
            appendLine("=== ${month.label} ===")
            appendLine("Income: $currencySymbol${"%.0f".format(month.income)}, Expenses: $currencySymbol${"%.0f".format(month.expenses)}")
            month.categoryBreakdown.forEach {
                appendLine("  ${it.category}: $currencySymbol${"%.0f".format(it.total)} (${it.count} transactions)")
            }
            appendLine()
        }
        if (notableTransactions.isNotEmpty()) {
            appendLine("=== Notable transactions this month ===")
            notableTransactions.forEach {
                appendLine("  $currencySymbol${"%.0f".format(it.amount)} — ${it.label} (${it.date})")
            }
        }
    }
}