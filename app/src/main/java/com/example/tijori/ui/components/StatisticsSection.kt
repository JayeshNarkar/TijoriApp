package com.example.tijori.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tijori.data.entities.ExpenseCategory
import com.example.tijori.data.entities.TimeFrame
import com.example.tijori.ui.theme.color

@Composable
fun TimeFrameChip(
    timeFrame: TimeFrame,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = timeFrame.label,
        color = contentColor,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun BalanceSummaryCard(
    estimatedBalance: String,
    income: String,
    expenses: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = "Estimated Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = estimatedBalance,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            ) {
                BalanceStat(
                    label = "Income",
                    value = income,
                    dotColor = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )

                VerticalDivider(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .height(48.dp)
                )

                BalanceStat(
                    label = "Expenses",
                    value = expenses,
                    dotColor = Color(0xFFE57373),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BalanceStat(label: String, value: String, dotColor: Color,modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = dotColor
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

data class CategorySlice(
    val category: ExpenseCategory,
    val total: Double,
    val percent: Float // 0f..1f
)

@Composable
fun CategoryBreakdownCard(
    currSymbol: String,
    slices: List<CategorySlice>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Where it went",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "${slices.size} categories",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (slices.isEmpty()) {
                Text(
                    text = "No expenses",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
                return@Column
            }

            // --- Segmented bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 20.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                slices.forEach { slice ->
                    Box(
                        modifier = Modifier
                            .weight(slice.percent.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(slice.category.color)
                    )
                }
            }

            // --- Rows ---
            slices.forEachIndexed { index, slice ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(slice.category.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = slice.category.name.split("_")
                            .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$currSymbol${"%,.0f".format(slice.total)}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "${(slice.percent * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (index != slices.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}