package com.example.tijori.data.entities

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface CategoryFilter {
    data class Expense(val category: ExpenseCategory) : CategoryFilter
    data class Income(val category: IncomeCategory) : CategoryFilter
}

enum class ExpenseCategory(val colorHex: String, val icon: ImageVector) {
    GROCERIES("#FBC02D", Icons.Filled.ShoppingCart),
    TRANSPORTATION("#1976D2",Icons.Filled.DirectionsCar),
    DINING("#E64A19",Icons.Filled.Restaurant),
    ENTERTAINMENT("#8E24AA",Icons.Filled.Movie),
    UTILITIES("#00897B", Icons.Filled.Receipt),
    RENT("#5D4037", Icons.Filled.Home),
    HEALTHCARE("#D81B60",Icons.Filled.LocalHospital),
    SHOPPING("#3949AB",Icons.Filled.ShoppingBag),
    EDUCATION("#546E7A",Icons.Filled.School),
    OTHER_EXPENSE("#757575",Icons.Filled.Receipt)
}

enum class IncomeCategory(val colorHex: String, val icon: ImageVector) {
    SALARY("#2E7D32", Icons.Filled.AccountBalanceWallet),
    REFUND("#0288D1", Icons.AutoMirrored.Filled.Undo),
    INTEREST("#F9A825", Icons.Filled.Percent),
    GIFT("#C2185B", Icons.Filled.CardGiftcard),
    OTHER_INCOME("#607D8B", Icons.Filled.AttachMoney)
}