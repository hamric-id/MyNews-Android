package com.hamric.feature.articles.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CompactSortDropdown(
    sortBy: String,
    onSortByChange: (String) -> Unit,
    hasActiveFilter: Boolean,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getSortDisplayName(sortBy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Sort",
                modifier = Modifier.size(16.dp)
            )
        }

        if (hasActiveFilter) {
            IconButton(
                onClick = onClearFilter,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear sort filter",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Newest First") },
                onClick = {
                    onSortByChange("publishedAt")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Most Relevant") },
                onClick = {
                    onSortByChange("relevancy")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Most Popular") },
                onClick = {
                    onSortByChange("popularity")
                    expanded = false
                }
            )
        }
    }
}

fun getSortDisplayName(sortBy: String): String {
    return when (sortBy) {
        "publishedAt" -> "Newest"
        "relevancy" -> "Relevant"
        "popularity" -> "Popular"
        else -> "Sort"
    }
}