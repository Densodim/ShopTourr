package com.example.shoptourr.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.shoptourr.domain.model.RouteStop

/**
 * Native map slot. Android/iOS actuals use the canvas renderer until MapLibre
 * fits the 40 MiB budget; [VoyageMapHost] still honors the remote `nativeMaps` flag.
 */
@Composable
expect fun VoyageNativeMap(
    stops: List<RouteStop>,
    caption: String?,
    modifier: Modifier = Modifier,
)
