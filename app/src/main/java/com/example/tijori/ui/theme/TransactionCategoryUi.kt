package com.example.tijori.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.toColorInt
import com.example.tijori.data.entities.ExpenseCategory
import com.example.tijori.data.entities.IncomeCategory
import com.example.tijori.data.entities.Transaction
import com.example.tijori.data.entities.TransactionType


val ExpenseCategory.color: Color
    get() = Color(colorHex.toColorInt())

val IncomeCategory.color: Color
    get() = Color(colorHex.toColorInt())

val Transaction.displayIcon: ImageVector
    get() = if (type == TransactionType.DEBIT) {
        expenseCategory?.icon ?: Icons.Filled.AttachMoney
    } else {
        incomeCategory?.icon ?: Icons.Filled.AttachMoney
    }

val Transaction.displayColor: Color
    get() = if (type == TransactionType.DEBIT) {
        expenseCategory?.color ?: Color.Gray
    } else {
        incomeCategory?.color ?: Color.Gray
    }

val Transaction.displayAmount: String
    get() = "%,.2f".format(amount)
val Transaction.displayCategoryName: String
    get() = if (type == TransactionType.DEBIT) {
        expenseCategory?.name?.replace("_", " ") ?: "Other"
    } else {
        incomeCategory?.name?.replace("_", " ") ?: "Other"
    }


fun String.toTitleCase(): String =
    split("_").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }