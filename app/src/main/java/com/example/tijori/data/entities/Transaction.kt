package com.example.tijori.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "Transactions",
    indices = [Index(value = ["userId"]), Index(value = ["type"]), Index(value = ["expenseCategory"]), Index(
        value = ["incomeCategory"]
    )]
)
data class Transaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val amount: Double,
    val type: TransactionType,
    val expenseCategory: ExpenseCategory? = null,
    val incomeCategory: IncomeCategory? = null,
    val note: String? = null,
    val date: Date = Date(),
    val createdAt: Date = Date(),
    val source: TransactionSource = TransactionSource.MANUAL,
    val needsReview: Boolean = false,
    val upiTransactionId: String? = null
)

enum class TransactionSource {
    MANUAL, SMS_AUTO
}

enum class TransactionType {
    DEBIT, CREDIT
}

enum class TimeFrame(val label: String) {
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time")
}

fun TimeFrame.startDate(): Date {
    val calendar = Calendar.getInstance()
    return when (this) {
        TimeFrame.THIS_WEEK -> {
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            calendar.time
        }
        TimeFrame.THIS_MONTH -> {
            calendar.add(Calendar.DAY_OF_YEAR, -30)
            calendar.time
        }
        TimeFrame.THIS_YEAR -> {
            calendar.add(Calendar.DAY_OF_YEAR, -365)
            calendar.time
        }
        TimeFrame.ALL_TIME -> Date(0) // epoch — every transaction is after this
    }
}