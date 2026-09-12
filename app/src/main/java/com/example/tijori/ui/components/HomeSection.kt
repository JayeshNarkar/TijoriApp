package com.example.tijori.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tijori.data.entities.CategoryFilter
import com.example.tijori.data.entities.ExpenseCategory
import com.example.tijori.data.entities.IncomeCategory
import com.example.tijori.data.entities.ThemeMode
import com.example.tijori.data.entities.Transaction
import com.example.tijori.data.entities.TransactionType
import com.example.tijori.ui.theme.color
import com.example.tijori.ui.theme.displayAmount
import com.example.tijori.ui.theme.displayCategoryName
import com.example.tijori.ui.theme.displayColor
import com.example.tijori.ui.theme.displayIcon
import com.example.tijori.ui.theme.toTitleCase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@Composable
fun HomeHeader(
    firstName: String,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentlyDark = themeMode == ThemeMode.DARK
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello,",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = firstName,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        IconButton(
            onClick = {
                onThemeModeChange(if (isCurrentlyDark) ThemeMode.LIGHT else ThemeMode.DARK)
            }
        ) {
            Icon(
                imageVector = if (isCurrentlyDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = if (isCurrentlyDark) "Switch to light mode" else "Switch to dark mode"
            )
        }


        IconButton(onClick = onNavigateToSettings) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Settings",
                modifier = Modifier
                    .clip(CircleShape)
            )
        }
    }
}


@Composable
fun CategoryChip(
    label: String,
    icon: ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = if (selected) 0.35f else 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color
            )
        }

        Text(
            text = label.toTitleCase(),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AllCategoryChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = if (selected) 0.35f else 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Apps,
                contentDescription = "All categories",
                tint = tint
            )
        }

        Text(
            text = "All",
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun ReviewTransactionDialog(
    transaction: Transaction,
    onConfirm: (type: TransactionType, expenseCategory: ExpenseCategory?, incomeCategory: IncomeCategory?, note: String?, date: Date) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(transaction.type) }
    var selectedCategory by remember {
        mutableStateOf(
            transaction.expenseCategory?.let { CategoryFilter.Expense(it) }
                ?: transaction.incomeCategory?.let { CategoryFilter.Income(it) }
        )
    }

    var note by remember { mutableStateOf(transaction.note ?: "") }
    var selectedDate by remember { mutableStateOf(transaction.date) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("d MMM, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val isDebit = selectedType == TransactionType.DEBIT

    val canSave = when (val filter = selectedCategory) {
        is CategoryFilter.Expense -> isDebit
        is CategoryFilter.Income -> !isDebit
        null -> false
    }

    fun onTypeChange(newType: TransactionType) {
        if (newType != selectedType) {
            selectedType = newType
            selectedCategory = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isDebit) "Categorize Expense" else "Categorize Income"} — ₹${transaction.displayAmount}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete transaction",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

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

                TextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    DateTimeField(
                        icon = Icons.Filled.CalendarToday,
                        label = dateFormatter.format(selectedDate),
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    DateTimeField(
                        icon = Icons.Filled.AccessTime,
                        label = timeFormatter.format(selectedDate),
                        onClick = { showTimePicker = true },
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val filter = selectedCategory
                    onConfirm(
                        selectedType,
                        (filter as? CategoryFilter.Expense)?.category,
                        (filter as? CategoryFilter.Income)?.category,
                        note.ifBlank { null },
                        selectedDate
                    )
                    onDismiss()
                },
                enabled = canSave
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Later") }
        }
    )

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

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text("Cancel") } },
        text = { content() }
    )
}

@Composable
fun DateTimeField(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun TransactionRow(
    currSymbol: String?,
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormatter = remember { SimpleDateFormat("d MMM, yyyy", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(transaction.displayColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.displayIcon,
                    contentDescription = transaction.displayCategoryName,
                    tint = transaction.displayColor
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = transaction.note?.takeIf { it.isNotBlank() } ?: transaction.displayCategoryName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (transaction.needsReview) "Tap to review" else transaction.displayCategoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (transaction.needsReview) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val sign = if (transaction.type == TransactionType.CREDIT) "+" else "-"
                Text(
                    text = "$sign${currSymbol ?: "₹"}${transaction.displayAmount}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (transaction.type == TransactionType.CREDIT) {
                        Color(0xFF4CAF50)
                    } else {
                        Color(0xFFE57373)
                    }
                )
                Text(
                    text = dateFormatter.format(transaction.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor
                )
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
                modifier = Modifier.padding(top = 2.dp),
                color = when {
                    label.contains("income", ignoreCase = true) -> Color(0xFF4CAF50)
                    label.contains("expense", ignoreCase = true) -> Color(0xFFE57373)
                    else -> LocalContentColor.current
                }
            )
        }
    }
}