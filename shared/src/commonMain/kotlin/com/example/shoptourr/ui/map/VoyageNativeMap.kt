package com.example.shoptourr.ui.map

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.shoptourr.domain.model.RouteStop
import com.example.shoptourr.ui.testing.VoyageTestTags

/**
 * Native map slot. iOS uses MapKit. Android uses Google Maps when a
 * `MAPS_API_KEY` is present and Play services are available; otherwise both
 * platforms keep the stylized canvas (no MapLibre — stays off the 40 MiB budget).
 * [VoyageMapHost] still honors the remote `nativeMaps` flag.
 */
@Composable
expect fun VoyageNativeMap(
    stops: List<RouteStop>,
    caption: String?,
    modifier: Modifier = Modifier,
)

internal fun Modifier.voyageNativeMapChrome(caption: String?): Modifier =
    fillMaxWidth()
        .height(220.dp)
        .clip(RoundedCornerShape(18.dp))
        .testTag(VoyageTestTags.ROUTE_MAP)
        .semantics { contentDescription = caption ?: "Trip route map" }
