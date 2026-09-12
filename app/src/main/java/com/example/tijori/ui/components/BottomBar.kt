package com.example.tijori.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import com.example.tijori.ui.navigation.BottomNavItem
import com.example.tijori.ui.navigation.bottomNavItems
import com.example.tijori.ui.navigation.matchesRoute

@Composable
fun BottomBar(
    currentDestination: NavDestination?,
    onTabClick: (Any) -> Unit,
    onAddExpenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    BottomBarTab(
                        item = item,
                        selected = currentDestination?.matchesRoute(item.route) == true,
                        onClick = { onTabClick(item.route) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAddExpenseClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add expense",modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun BottomBarTab(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(item.icon, contentDescription = item.label, tint = tint, modifier = Modifier.size(30.dp))
        Text(item.label, style = MaterialTheme.typography.labelMedium, color = tint)
    }
}