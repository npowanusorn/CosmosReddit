package com.hamburghini.cosmos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hamburghini.cosmos.ui.theme.RedditOrange

@Composable
fun TabBarItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDot: Boolean = false
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) RedditOrange else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        animationSpec = tween(300),
        label = "iconColor"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(300),
        label = "iconScale"
    )

    val dotScale by animateFloatAsState(
        targetValue = if (isSelected && showDot) 1f else 0f,
        animationSpec = tween(300),
        label = "dotScale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .padding(4.dp)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .scale(iconScale)
        )

        // Indicator dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(dotScale)
                .background(
                    color = if (isSelected) RedditOrange else Color.Transparent,
                    shape = CircleShape
                )
        )
    }
}