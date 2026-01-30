package com.hamburghini.cosmos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamburghini.cosmos.data.repository.SortType
import com.hamburghini.cosmos.ui.theme.RedditOrange

@Composable
fun SortFilterRow(
    currentSort: SortType,
    isPersonalized: Boolean,
    onSortChanged: (SortType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Show appropriate sorts based on whether it's personalized feed
        val availableSorts = if (isPersonalized) {
            listOf(SortType.BEST, SortType.HOT, SortType.NEW, SortType.TOP)
        } else {
            SortType.entries
        }

        items(availableSorts) { sortType ->
            FilterChip(
                selected = currentSort == sortType,
                onClick = { onSortChanged(sortType) },
                label = {
                    Text(
                        text = when (sortType) {
                            SortType.BEST -> "Best"
                            SortType.HOT -> "Hot"
                            SortType.NEW -> "New"
                            SortType.TOP -> "Top"
                            SortType.RISING -> "Rising"
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RedditOrange,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Preview(
    showBackground = true
)
@Composable
private fun PreviewSortFilterRow() {
    SortFilterRow(
        currentSort = SortType.TOP,
        isPersonalized = true,
        onSortChanged = {}
    )
}