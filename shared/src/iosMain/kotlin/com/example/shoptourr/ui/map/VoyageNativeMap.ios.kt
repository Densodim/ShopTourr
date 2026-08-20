package com.example.shoptourr.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.example.shoptourr.domain.model.NativeMapCamera
import com.example.shoptourr.domain.model.RouteStop
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMake
import platform.MapKit.MKCoordinateSpanMake
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VoyageNativeMap(
    stops: List<RouteStop>,
    caption: String?,
    modifier: Modifier,
) {
    val camera = NativeMapCamera.fromStops(stops)
    if (camera == null) {
        RouteMapCanvas(stops = stops, caption = caption, modifier = modifier)
        return
    }
    UIKitView(
        modifier = modifier.voyageNativeMapChrome(caption),
        factory = {
            MKMapView().apply {
                zoomEnabled = true
                scrollEnabled = true
                rotateEnabled = false
                pitchEnabled = false
            }
        },
        update = { mapView -> applyCamera(mapView, camera) },
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun applyCamera(mapView: MKMapView, camera: NativeMapCamera) {
    val region = MKCoordinateRegionMake(
        CLLocationCoordinate2DMake(camera.centerLat, camera.centerLng),
        MKCoordinateSpanMake(camera.latDelta, camera.lngDelta),
    )
    mapView.setRegion(region, animated = false)
    mapView.removeAnnotations(mapView.annotations)
    camera.pins.forEach { pin ->
        val annotation = MKPointAnnotation()
        annotation.setCoordinate(CLLocationCoordinate2DMake(pin.lat, pin.lng))
        annotation.setTitle(pin.title)
        mapView.addAnnotation(annotation)
    }
}
