package com.example

import com.example.data.NepalAdministrativeData
import com.example.model.LocalLevelType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Phase 21: Location Integration Test.
 *
 * Verifies that official Nepal administrative locations across all 7 provinces
 * and all 4 local-level types flow correctly with stable canonical IDs.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocationIntegrationTest {

    @Test
    fun testAllSevenProvincesDataFlow() {
        // Province 1: Koshi
        val koshi = NepalAdministrativeData.findLocation("province_1_koshi", "morang", "biratnagar", 1)
        assertNotNull(koshi)
        assertEquals("province_1_koshi", koshi?.provinceId)
        assertEquals("Koshi", koshi?.provinceEn)
        assertEquals("काेशी", koshi?.provinceNe)
        assertEquals("Morang", koshi?.districtEn)
        assertEquals(LocalLevelType.METROPOLITAN, koshi?.localLevelType)
        assertEquals(1, koshi?.wardNumber)

        // Province 2: Madhesh
        val madhesh = NepalAdministrativeData.findLocation("province_2_madhesh", "dhanusha", "janakpurdham", 3)
        assertNotNull(madhesh)
        assertEquals("province_2_madhesh", madhesh?.provinceId)
        assertEquals("Madhesh", madhesh?.provinceEn)
        assertEquals("Dhanusha", madhesh?.districtEn)
        assertEquals(LocalLevelType.SUB_METROPOLITAN, madhesh?.localLevelType)
        assertEquals(3, madhesh?.wardNumber)

        // Province 3: Bagmati
        val bagmati = NepalAdministrativeData.findLocation("province_3_bagmati", "kathmandu", "kathmandu", 5)
        assertNotNull(bagmati)
        assertEquals("province_3_bagmati", bagmati?.provinceId)
        assertEquals("Bagmati", bagmati?.provinceEn)
        assertEquals("Kathmandu", bagmati?.districtEn)
        assertEquals(LocalLevelType.METROPOLITAN, bagmati?.localLevelType)
        assertEquals(5, bagmati?.wardNumber)

        // Province 4: Gandaki
        val gandaki = NepalAdministrativeData.findLocation("province_4_gandaki", "kaski", "pokhara", 2)
        assertNotNull(gandaki)
        assertEquals("province_4_gandaki", gandaki?.provinceId)
        assertEquals("Gandaki", gandaki?.provinceEn)
        assertEquals("Kaski", gandaki?.districtEn)
        assertEquals(LocalLevelType.METROPOLITAN, gandaki?.localLevelType)
        assertEquals(2, gandaki?.wardNumber)

        // Province 5: Lumbini
        val lumbini = NepalAdministrativeData.findLocation("province_5_lumbini", "rupandehi", "butwal", 4)
        assertNotNull(lumbini)
        assertEquals("province_5_lumbini", lumbini?.provinceId)
        assertEquals("Lumbini", lumbini?.provinceEn)
        assertEquals("Rupandehi", lumbini?.districtEn)
        assertEquals(LocalLevelType.SUB_METROPOLITAN, lumbini?.localLevelType)
        assertEquals(4, lumbini?.wardNumber)

        // Province 6: Karnali
        val karnali = NepalAdministrativeData.findLocation("province_6_karnali", "surkhet", "birendranagar", 6)
        assertNotNull(karnali)
        assertEquals("province_6_karnali", karnali?.provinceId)
        assertEquals("Karnali", karnali?.provinceEn)
        assertEquals("Surkhet", karnali?.districtEn)
        assertEquals(LocalLevelType.MUNICIPALITY, karnali?.localLevelType)
        assertEquals(6, karnali?.wardNumber)

        // Province 7: Sudurpashchim
        val sudur = NepalAdministrativeData.findLocation("province_7_sudurpaschim", "kailali", "dhangadhi", 7)
        assertNotNull(sudur)
        assertEquals("province_7_sudurpaschim", sudur?.provinceId)
        assertEquals("Sudurpaschim", sudur?.provinceEn)
        assertEquals("Kailali", sudur?.districtEn)
        assertEquals(LocalLevelType.SUB_METROPOLITAN, sudur?.localLevelType)
        assertEquals(7, sudur?.wardNumber)
    }

    @Test
    fun testAllFourLocalLevelTypesFlow() {
        // 1. Metropolitan City
        val metro = NepalAdministrativeData.findLocation("province_3_bagmati", "lalitpur", "lalitpur", 1)
        assertNotNull(metro)
        assertEquals(LocalLevelType.METROPOLITAN, metro?.localLevelType)
        assertTrue(metro?.nameNe?.contains("महानगरपालिका") == true)
        assertTrue(metro?.nameEn?.contains("Metropolitan City") == true)

        // 2. Sub-Metropolitan City
        val subMetro = NepalAdministrativeData.findLocation("province_1_koshi", "sunsari", "dharan", 1)
        assertNotNull(subMetro)
        assertEquals(LocalLevelType.SUB_METROPOLITAN, subMetro?.localLevelType)
        assertTrue(subMetro?.nameNe?.contains("उपमहानगरपालिका") == true)
        assertTrue(subMetro?.nameEn?.contains("Sub-Metropolitan City") == true)

        // 3. Municipality
        val muni = NepalAdministrativeData.findLocation("province_3_bagmati", "kavrepalanchok", "dhulikhel", 1)
        assertNotNull(muni)
        assertEquals(LocalLevelType.MUNICIPALITY, muni?.localLevelType)
        assertTrue(muni?.nameNe?.contains("नगरपालिका") == true)
        assertTrue(muni?.nameEn?.contains("Municipality") == true)

        // 4. Rural Municipality
        val rural = NepalAdministrativeData.findLocation("province_3_bagmati", "sindhupalchok", "helambu", 1)
        assertNotNull(rural)
        assertEquals(LocalLevelType.RURAL_MUNICIPALITY, rural?.localLevelType)
        assertTrue(rural?.nameNe?.contains("गाउँपालिका") == true)
        assertTrue(rural?.nameEn?.contains("Rural Municipality") == true)
    }

    @Test
    fun testOfflineLocationDataMasterResolution() {
        // Master administrative data is bundled and operates completely offline
        val totalProvinces = NepalAdministrativeData.provinces.size
        val totalDistricts = NepalAdministrativeData.districts.size
        val totalLocalLevels = NepalAdministrativeData.localLevels.size
        val allWards = NepalAdministrativeData.getAllWards()

        assertEquals(7, totalProvinces)
        assertEquals(77, totalDistricts)
        assertEquals(753, totalLocalLevels)
        assertEquals(6743, allWards.size)
    }
}
