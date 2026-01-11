package com.hamburghini.cosmos.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hamburghini.cosmos.ui.theme.RedditOrange
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TabBarItem(
    @DrawableRes filledIconRes: Int,
    @DrawableRes outlineIconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDot: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hapticFeedback = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    // Track press interactions
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isPressed = true
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel -> isPressed = false
            }
        }
    }

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

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(150),
        label = "pressScale"
    )

    val dotScale by animateFloatAsState(
        targetValue = if (isSelected && showDot) 1f else 0f,
        animationSpec = tween(300),
        label = "dotScale"
    )

    Column(
        modifier = modifier
            .padding(4.dp)
            .scale(pressScale)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val iconRes = if (isSelected) filledIconRes else outlineIconRes
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .scale(iconScale)
        )

        // Indicator dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .scale(dotScale)
                .background(
                    color = if (isSelected) RedditOrange else Color.Transparent,
                    shape = CircleShape
                )
        )
    }
}