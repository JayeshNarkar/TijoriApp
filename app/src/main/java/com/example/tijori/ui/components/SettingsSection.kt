package com.example.tijori.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tijori.data.entities.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SettingsEntry(
    val label: String,
    val icon: ImageVector? = null,
    val trailingText: String? = null,
    val onClick: () -> Unit = {}
)

@Composable
fun SettingsSection(
    title: String, entries: List<SettingsEntry>, modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title, style = TextStyle(
                fontWeight = FontWeight.Medium, fontSize = 18.sp
            ), modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors()
        ) {
            entries.forEachIndexed { index, entry ->
                SettingsEntryRow(entry)
                if (index != entries.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SettingsEntryRow(entry: SettingsEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = entry.onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        entry.icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp)
            )
        }
        Text(
            text = entry.label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        entry.trailingText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EditProfileDialog(
    email: String,
    firstName: String?,
    lastName: String?,
    onConfirm: (firstName: String, lastName: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var newFirstName by remember { mutableStateOf(firstName ?: "") }
    var newLastName by remember { mutableStateOf(lastName ?: "") }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Edit Profile") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = email,
                enabled = false,
                onValueChange = { },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = newFirstName,
                onValueChange = { newFirstName = it },
                label = { Text("First name") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = newLastName,
                onValueChange = { newLastName = it },
                label = { Text("Last name") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }, confirmButton = {
        Button(
            onClick = {
                val trimmedFirst = newFirstName.trim()
                if (trimmedFirst.isBlank()) return@Button // don't allow empty first name
                val trimmedLast = newLastName.trim().ifBlank { null }
                onConfirm(trimmedFirst, trimmedLast)
                onDismiss()
            }) {
            Text("Save")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    })
}

@Composable
fun LogoutConfirmDialog(
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log out?") },
        text = { Text("You'll need to sign in again to access your account.") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                }) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        })
}

data class CurrencyOption(val code: String, val symbol: String, val label: String)

private val supportedCurrencies = listOf(
    CurrencyOption("INR", "₹", "Indian Rupee"),
    CurrencyOption("USD", "$", "US Dollar"),
    CurrencyOption("EUR", "€", "Euro"),
    CurrencyOption("GBP", "£", "British Pound")
)

@Composable
fun CurrencyPickerDialog(
    currentCode: String, onConfirm: (code: String, symbol: String) -> Unit, onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(currentCode) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Currency") }, text = {
        Column {
            supportedCurrencies.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = option.code == selected,
                            onClick = { selected = option.code }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = option.code == selected, onClick = { selected = option.code })
                    Text("${option.label} (${option.symbol})")
                }
            }
        }
    }, confirmButton = {
        Button(onClick = {
            val chosen = supportedCurrencies.first { it.code == selected }
            onConfirm(chosen.code, chosen.symbol)
            onDismiss()
        }) {
            Text("Save")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) { Text("Cancel") }
    })
}


@Composable
fun ThemeModePickerDialog(
    currentMode: ThemeMode, onConfirm: (ThemeMode) -> Unit, onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(currentMode) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Theme") }, text = {
        Column {
            ThemeMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = mode == selected, onClick = { selected = mode }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = mode == selected, onClick = { selected = mode })
                    Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
    }, confirmButton = {
        Button(onClick = {
            onConfirm(selected)
            onDismiss()
        }) {
            Text("Save")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) { Text("Cancel") }
    })
}


@Composable
fun MinimumBalanceDialog(
    currentMinimum: Double?,
    onConfirm: (Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var enabled by remember { mutableStateOf(currentMinimum != null) }
    var input by remember {
        mutableStateOf(currentMinimum?.toString() ?: "0")
    }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Minimum Balance") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Require a minimum balance")
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            if (enabled) {
                TextField(
                    value = input,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                            input = value
                        }
                    },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }, confirmButton = {
        Button(onClick = {
            val result = if (enabled) (input.toDoubleOrNull() ?: 0.0) else null
            onConfirm(result)
            onDismiss()
        }) {
            Text("Save")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) { Text("Cancel") }
    })
}

@Composable
fun StartingBalanceDialog(
    currentBalance: Double,
    currentDate: Date,
    onConfirm: (balance: Double, date: Date) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember {
        mutableStateOf(if (currentBalance != 0.0) currentBalance.toString() else "")
    }
    var selectedDate by remember { mutableStateOf(currentDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Starting Balance") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(
                value = input,
                onValueChange = { value ->
                    if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                        input = value
                    }
                },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null)
                Text("  As of ${dateFormatter.format(selectedDate)}")
            }
        }
    }, confirmButton = {
        Button(onClick = {
            val amount = input.toDoubleOrNull() ?: 0.0
            onConfirm(amount, selectedDate)
            onDismiss()
        }) {
            Text("Save")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) { Text("Cancel") }
    })

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Date(millis)
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
}