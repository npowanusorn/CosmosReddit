package com.hamburghini.cosmos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hamburghini.cosmos.navigation.BottomNavDestination

@Composable
fun CustomBottomTabBar(
    destinations: List<BottomNavDestination>,
    currentDestination: String?,
    onNavigate: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            destinations.forEach { destination ->
                TabBarItem(
                    filledIconRes = destination.filledIconRes,
                    outlineIconRes = destination.outlineIconRes,
                    isSelected = currentDestination == destination.route,
                    onClick = { onNavigate(destination) },
                    modifier = Modifier.weight(1f),
                    showDot = true // Enable the dot indicator
                )
            }
        }
    }
}