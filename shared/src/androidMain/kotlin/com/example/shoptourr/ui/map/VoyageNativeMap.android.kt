package com.example.shoptourr.ui.map

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.shoptourr.domain.model.NativeMapCamera
import com.example.shoptourr.domain.model.NativeMapsConfig
import com.example.shoptourr.domain.model.RouteStop
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

private const val GOOGLE_MAPS_API_KEY_META = "com.google.android.geo.API_KEY"

@Composable
actual fun VoyageNativeMap(
    stops: List<RouteStop>,
    caption: String?,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val camera = NativeMapCamera.fromStops(stops)
    if (camera == null || !canShowGoogleMap(context)) {
        RouteMapCanvas(stops = stops, caption = caption, modifier = modifier)
        return
    }
    val mapView = remember {
        MapView(context).apply { onCreate(Bundle()) }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        var destroyed = false
        fun destroyOnce() {
            if (!destroyed) {
                destroyed = true
                mapView.onDestroy()
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> destroyOnce()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            destroyOnce()
        }
    }
    AndroidView(
        factory = { mapView },
        modifier = modifier.voyageNativeMapChrome(caption),
        update = { view ->
            view.getMapAsync { googleMap ->
                googleMap.uiSettings.isMapToolbarEnabled = false
                googleMap.uiSettings.isMyLocationButtonEnabled = false
                googleMap.clear()
                camera.pins.forEach { pin ->
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(pin.lat, pin.lng))
                            .title(pin.title),
                    )
                }
                val move = if (camera.pins.size == 1) {
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(camera.centerLat, camera.centerLng),
                        14f,
                    )
                } else {
                    val bounds = LatLngBounds.builder().apply {
                        camera.pins.forEach { include(LatLng(it.lat, it.lng)) }
                    }.build()
                    CameraUpdateFactory.newLatLngBounds(bounds, 72)
                }
                googleMap.setOnMapLoadedCallback {
                    googleMap.moveCamera(move)
                }
            }
        },
    )
}

internal fun canShowGoogleMap(context: Context): Boolean {
    if (!hasMapsApiKey(context)) return false
    val status = GoogleApiAvailability.getInstance()
        .isGooglePlayServicesAvailable(context)
    return status == ConnectionResult.SUCCESS
}

internal fun hasMapsApiKey(context: Context): Boolean {
    val info = runCatching {
        context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA,
        )
    }.getOrNull() ?: return false
    val key = info.metaData?.getString(GOOGLE_MAPS_API_KEY_META)
    return NativeMapsConfig.isConfiguredApiKey(key)
}
