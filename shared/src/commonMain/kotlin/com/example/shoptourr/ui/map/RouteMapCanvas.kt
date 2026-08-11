package com.example.shoptourr.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.RouteMapProjector
import com.example.shoptourr.domain.model.RouteStop
import com.example.shoptourr.ui.theme.VoyageTokens

@Composable
fun RouteMapCanvas(
    stops: List<RouteStop>,
    caption: String? = null,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(shape)
            .background(VoyageTokens.surface2)
            .border(1.dp, VoyageTokens.border, shape),
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val pins = RouteMapProjector.project(stops, size.width, size.height)
            if (pins.size >= 2) {
                val path = Path().apply {
                    moveTo(pins.first().x, pins.first().y)
                    pins.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(
                    path = path,
                    color = VoyageTokens.accent.copy(alpha = 0.55f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
                    ),
                )
            }
            pins.forEach { pin ->
                drawCircle(
                    color = VoyageTokens.accent,
                    radius = 7.dp.toPx(),
                    center = Offset(pin.x, pin.y),
                )
                drawCircle(
                    color = VoyageTokens.bg,
                    radius = 3.dp.toPx(),
                    center = Offset(pin.x, pin.y),
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
        ) {
            if (caption != null) {
                Text(
                    text = caption,
                    color = VoyageTokens.muted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "500 m",
                color = VoyageTokens.ink2,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
