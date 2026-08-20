package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.GeoPoint
import com.example.shoptourr.domain.model.NativeMapCamera
import com.example.shoptourr.domain.model.NativeMapsConfig
import com.example.shoptourr.domain.model.RouteStop
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NativeMapCameraTest {

    @Test
    fun `empty stops yield no native camera`() {
        assertNull(NativeMapCamera.fromStops(emptyList()))
    }

    @Test
    fun `stops without geo fall back to canvas`() {
        val stops = listOf(
            RouteStop(id = "1", title = "A", orderIndex = 0),
            RouteStop(id = "2", title = "B", orderIndex = 1),
        )
        assertNull(NativeMapCamera.fromStops(stops))
    }

    @Test
    fun `invalid coordinates are ignored`() {
        val stops = listOf(
            RouteStop(
                id = "1",
                title = "Bad",
                orderIndex = 0,
                point = GeoPoint(lat = "not-a-number", lng = "9.1"),
            ),
            RouteStop(
                id = "2",
                title = "Out of range",
                orderIndex = 1,
                point = GeoPoint(lat = "120", lng = "9.1"),
            ),
        )
        assertNull(NativeMapCamera.fromStops(stops))
    }

    @Test
    fun `single geo stop pads a usable span`() {
        val camera = NativeMapCamera.fromStops(
            listOf(
                RouteStop(
                    id = "belem",
                    title = "Belem",
                    orderIndex = 0,
                    point = GeoPoint(lat = "38.70", lng = "-9.20"),
                ),
            ),
        )
        assertNotNull(camera)
        assertEquals(38.70, camera.centerLat, absoluteTolerance = 1e-6)
        assertEquals(-9.20, camera.centerLng, absoluteTolerance = 1e-6)
        assertTrue(camera.latDelta >= 0.01)
        assertTrue(camera.lngDelta >= 0.01)
        assertEquals(1, camera.pins.size)
        assertEquals("belem", camera.pins.single().id)
        assertEquals("Belem", camera.pins.single().title)
    }

    @Test
    fun `two geo stops center on the midpoint and keep both pins`() {
        val camera = NativeMapCamera.fromStops(
            listOf(
                RouteStop(
                    id = "south",
                    title = "South",
                    orderIndex = 1,
                    point = GeoPoint(lat = "38.70", lng = "9.10"),
                ),
                RouteStop(
                    id = "north",
                    title = "North",
                    orderIndex = 0,
                    point = GeoPoint(lat = "38.80", lng = "9.20"),
                ),
                RouteStop(id = "no-geo", title = "Skip", orderIndex = 2),
            ),
        )
        assertNotNull(camera)
        assertEquals(38.75, camera.centerLat, absoluteTolerance = 1e-6)
        assertEquals(9.15, camera.centerLng, absoluteTolerance = 1e-6)
        assertEquals(2, camera.pins.size)
        assertEquals(listOf("north", "south"), camera.pins.map { it.id })
        assertTrue(camera.latDelta > 0.10)
        assertTrue(camera.lngDelta > 0.10)
    }

    @Test
    fun `maps api key is ignored until a real value is injected`() {
        assertFalse(NativeMapsConfig.isConfiguredApiKey(null))
        assertFalse(NativeMapsConfig.isConfiguredApiKey(""))
        assertFalse(NativeMapsConfig.isConfiguredApiKey("   "))
        assertFalse(NativeMapsConfig.isConfiguredApiKey("\${MAPS_API_KEY}"))
        assertTrue(NativeMapsConfig.isConfiguredApiKey("AIzaSyDummyKeyForTestsOnly"))
    }
}
