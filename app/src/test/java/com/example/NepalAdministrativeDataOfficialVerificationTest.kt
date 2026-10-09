package com.example

import com.example.data.NepalAdministrativeData
import com.example.model.LocalLevelType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Phase 20: Official Nepal Administrative Data Verification Test.
 *
 * Verifies official Nepal administrative hierarchy against authoritative
 * Government of Nepal sources (MoFAGA & NSO Census):
 * Nepal → 7 Provinces → 77 Districts → 753 Local Levels → 6,743 Wards
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NepalAdministrativeDataOfficialVerificationTest {

    @Test
    fun testOfficialRecordCounts() {
        val meta = NepalAdministrativeData.metadata
        assertEquals(7, NepalAdministrativeData.provinces.size)
        assertEquals(77, NepalAdministrativeData.districts.size)
        assertEquals(753, NepalAdministrativeData.localLevels.size)

        assertEquals(7, meta.provinceCount)
        assertEquals(77, meta.districtCount)
        assertEquals(753, meta.localLevelCount)
        assertEquals(6743, meta.wardCount)
        assertEquals(6, meta.metropolitanCount)
        assertEquals(11, meta.subMetropolitanCount)
        assertEquals(276, meta.municipalityCount)
        assertEquals(460, meta.ruralMunicipalityCount)

        val metros = NepalAdministrativeData.localLevels.filter { it.type == LocalLevelType.METROPOLITAN }
        val subMetros = NepalAdministrativeData.localLevels.filter { it.type == LocalLevelType.SUB_METROPOLITAN }
        val munis = NepalAdministrativeData.localLevels.filter { it.type == LocalLevelType.MUNICIPALITY }
        val ruralMunis = NepalAdministrativeData.localLevels.filter { it.type == LocalLevelType.RURAL_MUNICIPALITY }

        assertEquals(6, metros.size)
        assertEquals(11, subMetros.size)
        assertEquals(276, munis.size)
        assertEquals(460, ruralMunis.size)
        assertEquals(753, metros.size + subMetros.size + munis.size + ruralMunis.size)
    }

    @Test
    fun testOfficialSourceMetadata() {
        val meta = NepalAdministrativeData.metadata
        assertNotNull(meta.datasetVersion)
        assertTrue(meta.datasetVersion.isNotEmpty())
        assertTrue("Source name must cite MoFAGA and NSO", meta.sourceName.contains("MoFAGA") && meta.sourceName.contains("NSO"))
        assertTrue("Must include MoFAGA official URL", meta.sourceUrls.any { it.contains("mofaga.gov.np") })
        assertTrue("Must include NSO/CBS official URL", meta.sourceUrls.any { it.contains("censusnepal.cbs.gov.np") })
        assertNotNull(meta.verificationDate)
        assertEquals("SmartGaunNepalAdministrativeVerifier-Phase20", meta.verifiedBySystem)
    }

    @Test
    fun testProvinceDistrictIntegrity() {
        val provinces = NepalAdministrativeData.provinces
        val provinceIds = provinces.map { it.id }.toSet()
        assertEquals("No duplicate province IDs", 7, provinceIds.size)

        val districts = NepalAdministrativeData.districts
        val districtIds = districts.map { it.id }.toSet()
        assertEquals("No duplicate district IDs", 77, districtIds.size)

        for (d in districts) {
            assertTrue("District ${d.id} must have non-empty nameNe", d.nameNe.isNotBlank())
            assertTrue("District ${d.id} must have non-empty nameEn", d.nameEn.isNotBlank())
            assertTrue("District ${d.id} must belong to a valid province", provinceIds.contains(d.provinceId))
            assertTrue("District ${d.id} must have at least one municipality", d.municipalities.isNotEmpty())
        }

        // Verify official district counts per province
        val distsByProv = districts.groupBy { it.provinceId }
        assertEquals(14, distsByProv["province_1_koshi"]?.size)
        assertEquals(8, distsByProv["province_2_madhesh"]?.size)
        assertEquals(13, distsByProv["province_3_bagmati"]?.size)
        assertEquals(11, distsByProv["province_4_gandaki"]?.size)
        assertEquals(12, distsByProv["province_5_lumbini"]?.size)
        assertEquals(10, distsByProv["province_6_karnali"]?.size)
        assertEquals(9, distsByProv["province_7_sudurpaschim"]?.size)
    }

    @Test
    fun testLocalLevelAndWardIntegrity() {
        val districtIds = NepalAdministrativeData.districts.map { it.id }.toSet()
        val provinceIds = NepalAdministrativeData.provinces.map { it.id }.toSet()
        val localLevels = NepalAdministrativeData.localLevels
        val localLevelIds = localLevels.map { it.id }.toSet()

        assertEquals("No duplicate local level IDs", 753, localLevelIds.size)

        var totalWardsCalculated = 0
        for (ll in localLevels) {
            assertTrue("LocalLevel ${ll.id} must have non-empty nameNe", ll.nameNe.isNotBlank())
            assertTrue("LocalLevel ${ll.id} must have non-empty nameEn", ll.nameEn.isNotBlank())
            assertTrue("LocalLevel ${ll.id} must belong to a valid district", districtIds.contains(ll.districtId))
            assertTrue("LocalLevel ${ll.id} must belong to a valid province", provinceIds.contains(ll.provinceId))
            assertTrue("LocalLevel ${ll.id} must have ward count >= 1", ll.wardCount >= 1)
            totalWardsCalculated += ll.wardCount
        }

        assertEquals(6743, totalWardsCalculated)

        val allWards = NepalAdministrativeData.generateAllWards()
        assertEquals(6743, allWards.size)
        val wardIds = allWards.map { it.id }.toSet()
        assertEquals("No duplicate ward IDs", 6743, wardIds.size)

        for (w in allWards) {
            assertTrue(localLevelIds.contains(w.localLevelId))
            assertTrue(districtIds.contains(w.districtId))
            assertTrue(provinceIds.contains(w.provinceId))
            assertTrue(w.wardNumber >= 1)
        }
    }

    @Test
    fun testMetropolitanAndSubMetropolitanEntities() {
        val metros = NepalAdministrativeData.localLevels.filter { it.type == LocalLevelType.METROPOLITAN }
        val metroNames = metros.map { it.nameEn }.toSet()
        val expectedMetros = setOf("Kathmandu", "Lalitpur", "Bharatpur", "Pokhara", "Biratnagar", "Birgunj")
        assertEquals(expectedMetros, metroNames)

        val subMetros = NepalAdministrativeData.localLevels.filter { it.type == LocalLevelType.SUB_METROPOLITAN }
        val subMetroNames = subMetros.map { it.nameEn }.toSet()
        val expectedSubMetros = setOf(
            "Dharan", "Itahari", "Janakpurdham", "Jitpur Simara", "Kalaiya",
            "Hetauda", "Butwal", "Tulsipur", "Ghorahi", "Nepalgunj", "Dhangadhi"
        )
        assertEquals(expectedSubMetros, subMetroNames)
    }

    @Test
    fun testSpecialNamingAndAliases() {
        // 1. Nawalpur (Nawalparasi East) in Gandaki vs Parasi (Nawalparasi West) in Lumbini
        val nawalpur = NepalAdministrativeData.districts.firstOrNull { it.id == "district_43_nawalpur" }
        assertNotNull(nawalpur)
        assertEquals("province_4_gandaki", nawalpur?.provinceId)
        assertTrue(nawalpur?.aliases?.contains("Nawalparasi East") == true)

        val parasi = NepalAdministrativeData.districts.firstOrNull { it.id == "district_48_parasi" }
        assertNotNull(parasi)
        assertEquals("province_5_lumbini", parasi?.provinceId)
        assertTrue(parasi?.aliases?.contains("Nawalparasi West") == true)

        // 2. Eastern Rukum in Lumbini vs Western Rukum in Karnali
        val easternRukum = NepalAdministrativeData.districts.firstOrNull { it.id == "district_56_eastern_rukum" }
        assertNotNull(easternRukum)
        assertEquals("province_5_lumbini", easternRukum?.provinceId)

        val westernRukum = NepalAdministrativeData.districts.firstOrNull { it.id == "district_59_western_rukum" }
        assertNotNull(westernRukum)
        assertEquals("province_6_karnali", westernRukum?.provinceId)

        // 3. Kathmandu Metropolitan City name variants
        val ktm = NepalAdministrativeData.findLocation("bagmati", "kathmandu", "kathmandu_met")
        assertNotNull(ktm)
        assertEquals(LocalLevelType.METROPOLITAN, ktm?.localLevelType)
        assertEquals("local_level_307_kathmandu_1", ktm?.id)
        assertTrue(ktm?.nameNe?.contains("महानगरपालिका") == true)
    }

    @Test
    fun testLocationPickerSevenProvincesRegression() {
        // Test selection across all 7 provinces
        val p1 = NepalAdministrativeData.findLocation("province_1_koshi", "district_6_morang", "Biratnagar", 1)
        assertNotNull(p1)
        assertEquals("province_1_koshi", p1?.provinceId)
        assertEquals("district_6_morang", p1?.districtId)
        assertEquals(LocalLevelType.METROPOLITAN, p1?.localLevelType)

        val p2 = NepalAdministrativeData.findLocation("province_2_madhesh", "district_20_dhanusha", "Janakpurdham", 1)
        assertNotNull(p2)
        assertEquals("province_2_madhesh", p2?.provinceId)
        assertEquals(LocalLevelType.SUB_METROPOLITAN, p2?.localLevelType)

        val p3 = NepalAdministrativeData.findLocation("province_3_bagmati", "district_27_kathmandu", "Kathmandu", 1)
        assertNotNull(p3)
        assertEquals("province_3_bagmati", p3?.provinceId)
        assertEquals(LocalLevelType.METROPOLITAN, p3?.localLevelType)

        val p4 = NepalAdministrativeData.findLocation("province_4_gandaki", "district_38_kaski", "Pokhara", 1)
        assertNotNull(p4)
        assertEquals("province_4_gandaki", p4?.provinceId)
        assertEquals(LocalLevelType.METROPOLITAN, p4?.localLevelType)

        val p5 = NepalAdministrativeData.findLocation("province_5_lumbini", "district_49_rupandehi", "Butwal", 1)
        assertNotNull(p5)
        assertEquals("province_5_lumbini", p5?.provinceId)
        assertEquals(LocalLevelType.SUB_METROPOLITAN, p5?.localLevelType)

        val p6 = NepalAdministrativeData.findLocation("province_6_karnali", "district_63_surkhet", "Birendranagar", 1)
        assertNotNull(p6)
        assertEquals("province_6_karnali", p6?.provinceId)
        assertEquals(LocalLevelType.MUNICIPALITY, p6?.localLevelType)

        val p7 = NepalAdministrativeData.findLocation("province_7_sudurpaschim", "district_77_kailali", "Dhangadhi", 1)
        assertNotNull(p7)
        assertEquals("province_7_sudurpaschim", p7?.provinceId)
        assertEquals(LocalLevelType.SUB_METROPOLITAN, p7?.localLevelType)

        // Test Municipality location
        val muni = NepalAdministrativeData.findLocation("province_3_bagmati", "district_26_kavrepalanchok", "Dhulikhel", 3)
        assertNotNull(muni)
        assertEquals(LocalLevelType.MUNICIPALITY, muni?.localLevelType)
        assertEquals(3, muni?.wardNumber)

        // Test Rural Municipality location
        val rural = NepalAdministrativeData.findLocation("province_3_bagmati", "sindhupalchok", "Helambu", 1)
        assertNotNull(rural)
        assertEquals(LocalLevelType.RURAL_MUNICIPALITY, rural?.localLevelType)
        assertEquals(1, rural?.wardNumber)

        // Test Default Helambu location
        val def = NepalAdministrativeData.defaultLocation
        assertEquals("helambu_sindhupalchok", def.id)
        assertEquals("हेलम्बु गाउँपालिका", def.nameNe)
        assertEquals(LocalLevelType.RURAL_MUNICIPALITY, def.localLevelType)
        assertNotNull(def.latitude)
        assertNotNull(def.longitude)
    }
}
