package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.GeoPoint
import com.example.shoptourr.domain.model.RouteMapProjector
import com.example.shoptourr.domain.model.RouteStop
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteMapProjectorTest {

    @Test
    fun `indexes scatter pins like mila map when no geo`() {
        val stops = listOf(
            RouteStop(id = "1", title = "A", orderIndex = 0),
            RouteStop(id = "2", title = "B", orderIndex = 1),
            RouteStop(id = "3", title = "C", orderIndex = 2),
        )
        val pins = RouteMapProjector.project(stops, width = 400f, height = 360f)
        assertEquals(3, pins.size)
        assertEquals(400f * 20 / 100f, pins[0].x)
        assertEquals(360f * 20 / 100f, pins[0].y)
        assertEquals(400f * (20 + 37) / 100f, pins[1].x)
        assertEquals(360f * (20 + 53) / 100f, pins[1].y)
    }

    @Test
    fun `projects geo points into padded bounds`() {
        val stops = listOf(
            RouteStop(
                id = "1",
                title = "South",
                orderIndex = 0,
                point = GeoPoint(lat = "38.70", lng = "9.10"),
            ),
            RouteStop(
                id = "2",
                title = "North",
                orderIndex = 1,
                point = GeoPoint(lat = "38.80", lng = "9.20"),
            ),
        )
        val pins = RouteMapProjector.project(stops, width = 100f, height = 100f, paddingFraction = 0.1f)
        assertEquals(2, pins.size)
        // south is lower on screen (higher y)
        assertTrue(pins[0].y > pins[1].y)
        // west has smaller x
        assertTrue(pins[0].x < pins[1].x)
        assertTrue(pins.all { it.x in 10f..90f && it.y in 10f..90f })
    }

    @Test
    fun `empty input returns empty pins`() {
        assertTrue(RouteMapProjector.project(emptyList(), 100f, 100f).isEmpty())
    }
}
