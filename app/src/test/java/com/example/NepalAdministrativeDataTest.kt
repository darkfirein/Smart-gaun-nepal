package com.example

import com.example.data.NepalAdministrativeData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NepalAdministrativeDataTest {

    @Test
    fun testDefaultLocationIsHelambu() {
        val loc = NepalAdministrativeData.defaultLocation
        assertEquals("helambu_sindhupalchok", loc.id)
        assertEquals("हेलम्बु गाउँपालिका", loc.nameNe)
        assertEquals("Helambu Rural Municipality", loc.nameEn)
        assertEquals(7, NepalAdministrativeData.provinces.size)
    }

    @Test
    fun testFindLocation() {
        val kathmandu = NepalAdministrativeData.findLocation("bagmati", "kathmandu", "kathmandu")
        assertNotNull(kathmandu)
        assertEquals("काठमाण्डौं महानगरपालिका", kathmandu?.nameNe)
        assertEquals("Kathmandu Metropolitan City", kathmandu?.nameEn)
    }
}
