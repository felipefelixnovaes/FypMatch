package com.ideiassertiva.FypMatch.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Skeleton loading com efeito shimmer — substitui CircularProgressIndicator.
 * Usa tokens do MaterialTheme para consistência com light/dark theme.
 *
 * @param count Número de skeletons a renderizar (ex: 3 cards, 5 list items)
 * @param itemHeight Altura de cada skeleton item
 * @param roundedRadius Border radius dos itens
 */
@Composable
fun SkeletonLoading(
    count: Int = 3,
    itemHeight: Int = 200,
    roundedRadius: Int = 16,
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(count) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight.dp)
                    .background(brush = brush, shape = RoundedCornerShape(roundedRadius.dp))
            )
        }
    }
}

/**
 * Skeleton para listas (conversas, matches, etc.) — altura reduzida.
 */
@Composable
fun SkeletonListLoading(
    count: Int = 5,
    modifier: Modifier = Modifier
) {
    SkeletonLoading(
        count = count,
        itemHeight = 72,
        roundedRadius = 12,
        modifier = modifier
    )
}
