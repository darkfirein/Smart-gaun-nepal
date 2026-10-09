package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.NepalAdministrativeData
import com.example.model.LocationPickerStep
import com.example.viewmodel.HomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Phase 21: Location State Reset Test.
 *
 * Verifies that when parents change (Province, District), child states (District, Municipality, Ward)
 * are cleanly reset to prevent invalid mixed combinations. Also verifies hierarchy validation.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocationStateResetTest {

    private lateinit var app: Application
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = HomeViewModel(app)
    }

    @Test
    fun testProvinceSelectionResetsChildren() {
        viewModel.onOpenLocationPicker()
        assertEquals(LocationPickerStep.SELECT_PROVINCE, viewModel.uiState.value.pickerStep)

        val bagmati = NepalAdministrativeData.provinces.first { it.id == "province_3_bagmati" }
        viewModel.onPickerSelectProvince(bagmati)
        assertEquals(LocationPickerStep.SELECT_DISTRICT, viewModel.uiState.value.pickerStep)
        assertEquals(bagmati, viewModel.uiState.value.pickerSelectedProvince)
        assertNull(viewModel.uiState.value.pickerSelectedDistrict)
        assertNull(viewModel.uiState.value.pickerSelectedMunicipality)

        // Select a district
        val ktmDistrict = bagmati.districts.first { it.id == "district_27_kathmandu" }
        viewModel.onPickerSelectDistrict(ktmDistrict)
        assertEquals(LocationPickerStep.SELECT_MUNICIPALITY, viewModel.uiState.value.pickerStep)
        assertEquals(ktmDistrict, viewModel.uiState.value.pickerSelectedDistrict)

        // Select a municipality
        val ktmMuni = ktmDistrict.municipalities.first { it.nameEn == "Kathmandu" }
        viewModel.onPickerSelectMunicipality(ktmMuni)
        assertEquals(LocationPickerStep.SELECT_WARD, viewModel.uiState.value.pickerStep)
        assertEquals(ktmMuni, viewModel.uiState.value.pickerSelectedMunicipality)

        // NOW change Province to Koshi: district and municipality MUST be reset to null!
        val koshi = NepalAdministrativeData.provinces.first { it.id == "province_1_koshi" }
        viewModel.onPickerSelectProvince(koshi)
        assertEquals(LocationPickerStep.SELECT_DISTRICT, viewModel.uiState.value.pickerStep)
        assertEquals(koshi, viewModel.uiState.value.pickerSelectedProvince)
        assertNull("District must be reset when province changes", viewModel.uiState.value.pickerSelectedDistrict)
        assertNull("Municipality must be reset when province changes", viewModel.uiState.value.pickerSelectedMunicipality)
    }

    @Test
    fun testDistrictSelectionResetsMunicipality() {
        val bagmati = NepalAdministrativeData.provinces.first { it.id == "province_3_bagmati" }
        viewModel.onPickerSelectProvince(bagmati)

        val dist1 = bagmati.districts.first { it.id == "district_27_kathmandu" }
        viewModel.onPickerSelectDistrict(dist1)
        val muni1 = dist1.municipalities.first()
        viewModel.onPickerSelectMunicipality(muni1)
        assertEquals(LocationPickerStep.SELECT_WARD, viewModel.uiState.value.pickerStep)
        assertNotNull(viewModel.uiState.value.pickerSelectedMunicipality)

        // Switch to different district: municipality must be reset
        val dist2 = bagmati.districts.first { it.id == "district_35_sindhupalchok" }
        viewModel.onPickerSelectDistrict(dist2)
        assertEquals(LocationPickerStep.SELECT_MUNICIPALITY, viewModel.uiState.value.pickerStep)
        assertNull("Municipality must be reset when district changes", viewModel.uiState.value.pickerSelectedMunicipality)
    }

    @Test
    fun testStepBackTransitions() {
        val bagmati = NepalAdministrativeData.provinces.first { it.id == "province_3_bagmati" }
        val dist = bagmati.districts.first()
        val muni = dist.municipalities.first()

        viewModel.onOpenLocationPicker()
        viewModel.onPickerSelectProvince(bagmati)
        viewModel.onPickerSelectDistrict(dist)
        viewModel.onPickerSelectMunicipality(muni)
        assertEquals(LocationPickerStep.SELECT_WARD, viewModel.uiState.value.pickerStep)

        viewModel.onPickerStepBack()
        assertEquals(LocationPickerStep.SELECT_MUNICIPALITY, viewModel.uiState.value.pickerStep)

        viewModel.onPickerStepBack()
        assertEquals(LocationPickerStep.SELECT_DISTRICT, viewModel.uiState.value.pickerStep)

        viewModel.onPickerStepBack()
        assertEquals(LocationPickerStep.SELECT_PROVINCE, viewModel.uiState.value.pickerStep)

        viewModel.onPickerStepBack()
        assertFalse("Should close picker when stepping back from province", viewModel.uiState.value.isLocationPickerOpen)
    }

    @Test
    fun testHierarchyValidationPreventsInvalidCombinations() {
        // Valid combination: Bagmati -> Sindhupalchok -> Helambu -> Ward 1
        val valid = NepalAdministrativeData.validateLocationHierarchy(
            provinceId = "province_3_bagmati",
            districtId = "district_35_sindhupalchok",
            localLevelId = "local_level_391_helambu",
            wardNumber = 1
        )
        assertTrue("Valid hierarchy must pass", valid)

        // Invalid: Koshi province + Kathmandu district
        val invalidProv = NepalAdministrativeData.validateLocationHierarchy(
            provinceId = "province_1_koshi",
            districtId = "district_27_kathmandu",
            localLevelId = "local_level_307_kathmandu",
            wardNumber = 1
        )
        assertFalse("Mismatched province and district must fail", invalidProv)

        // Invalid: Kathmandu district + Helambu municipality
        val invalidMuni = NepalAdministrativeData.validateLocationHierarchy(
            provinceId = "province_3_bagmati",
            districtId = "district_27_kathmandu",
            localLevelId = "local_level_391_helambu",
            wardNumber = 1
        )
        assertFalse("Mismatched district and local level must fail", invalidMuni)

        // Invalid ward: Helambu has 7 wards, ward 10 must fail
        val invalidWard = NepalAdministrativeData.validateLocationHierarchy(
            provinceId = "province_3_bagmati",
            districtId = "district_35_sindhupalchok",
            localLevelId = "local_level_391_helambu",
            wardNumber = 10
        )
        assertFalse("Ward exceeding maximum count must fail", invalidWard)
    }
}
