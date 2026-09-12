package com.example.tijori.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.tijori.data.entities.ExpenseCategory
import com.example.tijori.data.entities.IncomeCategory
import com.example.tijori.data.entities.TransactionType
import com.example.tijori.ui.components.CategoryChip
import com.example.tijori.ui.components.TijoriTopBar
import com.example.tijori.ui.theme.color
import com.example.tijori.ui.viewmodel.TransactionDBViewModel
import com.example.tijori.ui.viewmodel.UserDBViewModel
import com.example.tijori.ui.viewmodel.UserLoadState
import androidx.compose.runtime.collectAsState
import com.example.tijori.data.entities.CategoryFilter
import com.example.tijori.ui.components.DateTimeField
import com.example.tijori.ui.components.TimePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    userViewModel: UserDBViewModel = hiltViewModel(),
    transactionViewModel: TransactionDBViewModel = hiltViewModel()
) {
    val userState by userViewModel.currentUserState.collectAsState()
    val user = (userState as? UserLoadState.Loaded)?.user

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.DEBIT) }
    var selectedCategory by remember { mutableStateOf<CategoryFilter?>(null) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var showTimePicker by remember { mutableStateOf(false) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    val dateFormatter = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }
    val isDebit = selectedType == TransactionType.DEBIT

    val canSave = amount.toDoubleOrNull() != null && when (val filter = selectedCategory) {
        is CategoryFilter.Expense -> isDebit
        is CategoryFilter.Income -> !isDebit
        null -> false
    }

    fun onTypeChange(newType: TransactionType) {
        if (newType != selectedType) {
            selectedType = newType
            selectedCategory = null // category choice no longer valid for the new type
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TijoriTopBar(title = "Add Transaction", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = isDebit,
                    onClick = { onTypeChange(TransactionType.DEBIT) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Expense") }
                SegmentedButton(
                    selected = !isDebit,
                    onClick = { onTypeChange(TransactionType.CREDIT) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("Income") }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { value ->
                    if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amount = value
                    }
                },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Category", style = MaterialTheme.typography.labelLarge)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (isDebit) {
                    items(ExpenseCategory.entries) { category ->
                        CategoryChip(
                            label = category.name,
                            icon = category.icon,
                            color = category.color,
                            selected = selectedCategory == CategoryFilter.Expense(category),
                            onClick = { selectedCategory = CategoryFilter.Expense(category) }
                        )
                    }
                } else {
                    items(IncomeCategory.entries) { category ->
                        CategoryChip(
                            label = category.name,
                            icon = category.icon,
                            color = category.color,
                            selected = selectedCategory == CategoryFilter.Income(category),
                            onClick = { selectedCategory = CategoryFilter.Income(category) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DateTimeField(
                    icon = Icons.Filled.CalendarToday,
                    label = dateFormatter.format(selectedDate),
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
                DateTimeField(
                    icon = Icons.Filled.AccessTime,
                    label = timeFormatter.format(selectedDate),
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull() ?: return@Button
                    val currentUser = user ?: return@Button
                    val filter = selectedCategory

                    transactionViewModel.addTransaction(
                        userId = currentUser.id,
                        amount = parsedAmount,
                        type = selectedType,
                        expenseCategory = (filter as? CategoryFilter.Expense)?.category,
                        incomeCategory = (filter as? CategoryFilter.Income)?.category,
                        note = note.ifBlank { null },
                        date = selectedDate
                    )
                    onBack()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save ${if (isDebit) "Expense" else "Income"}")
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { newDateMillis ->
                        // The picker returns midnight UTC for the chosen day — carry
                        // the existing hour/minute over instead of losing them.
                        val existingCal = Calendar.getInstance().apply { time = selectedDate }
                        val pickedCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = newDateMillis
                        }
                        val combined = Calendar.getInstance().apply {
                            set(Calendar.YEAR, pickedCal.get(Calendar.YEAR))
                            set(Calendar.MONTH, pickedCal.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, pickedCal.get(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, existingCal.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, existingCal.get(Calendar.MINUTE))
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        selectedDate = combined.time
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    if (showTimePicker) {
        val existingCal = remember { Calendar.getInstance().apply { time = selectedDate } }
        val timePickerState = rememberTimePickerState(
            initialHour = existingCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = existingCal.get(Calendar.MINUTE),
            is24Hour = false
        )

        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val combined = Calendar.getInstance().apply {
                    time = selectedDate
                    set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    set(Calendar.MINUTE, timePickerState.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDate = combined.time
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}