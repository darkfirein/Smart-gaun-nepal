package com.example.model

import com.example.data.AiMessage
import com.example.data.FarmingAiMessage

enum class BottomNavTab {
    HOME,
    SERVICES,
    NOTICES,
    FARMING,
    HEALTH,
    AI
}

enum class LocationPickerStep {
    SELECT_PROVINCE,
    SELECT_DISTRICT,
    SELECT_MUNICIPALITY,
    SELECT_WARD
}

enum class ServicesSubTab(val id: String, val titleNe: String, val titleEn: String) {
    CIVIC_SERVICES("civic", "नागरिक वडापत्र", "Citizen Charter"),
    NOTICES("notices", "सूचना तथा जानकारी", "Notices"),
    HEALTH("health", "स्वास्थ्य संस्था", "Health Facilities"),
    EMERGENCY("emergency", "आपतकालीन सम्पर्क", "Emergency Contacts")
}

data class HomeUiState(
    val language: GaunLanguage = GaunLanguage.NEPALI,
    val isOnline: Boolean = true,
    val selectedLocation: GaunLocation,
    val weatherState: WeatherUiState = WeatherUiState.Idle,
    val currentTab: BottomNavTab = BottomNavTab.HOME,
    val isLocationPickerOpen: Boolean = false,
    val isAiAssistantOpen: Boolean = false,
    val aiPromptInput: String = "",
    val aiMessages: List<AiMessage> = emptyList(),
    val isAiThinking: Boolean = false,
    val isVoiceModeEnabled: Boolean = false,
    val isVoiceListening: Boolean = false,
    val voicePartialText: String = "",
    val voiceErrorMessage: String = "",
    val currentlySpeakingMessageId: String? = null,
    val noticesList: List<LocalNotice> = emptyList(),
    val isNoticesLoading: Boolean = false,
    val selectedNotice: LocalNotice? = null,
    val isNoticeScreenOpen: Boolean = false,
    val isNoticeInfoOpen: Boolean = false,
    val noticeCategoryFilter: NoticeCategory = NoticeCategory.ALL,
    val noticeSearchQuery: String = "",
    val communityServices: List<CommunityService> = emptyList(),
    val selectedService: CommunityService? = null,
    val servicesSubTab: ServicesSubTab = ServicesSubTab.CIVIC_SERVICES,
    val healthFacilitiesState: HealthFacilitiesUiState = HealthFacilitiesUiState.Loading,
    val ambulanceServices: List<AmbulanceServiceInfo> = emptyList(),
    val healthEmergencyNumbers: List<EmergencyServiceNumber> = emptyList(),
    val healthGuidelines: List<BasicHealthGuideline> = emptyList(),
    val isHealthEmergencyScreenOpen: Boolean = false,
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val selectedEmergencyContact: EmergencyContact? = null,
    val farmingAiMessages: List<FarmingAiMessage> = emptyList(),
    val farmingAiInput: String = "",
    val isFarmingAiThinking: Boolean = false,
    val isFarmingAiScreenOpen: Boolean = false,
    val pickerStep: LocationPickerStep = LocationPickerStep.SELECT_PROVINCE,
    val pickerSelectedProvince: ProvinceData? = null,
    val pickerSelectedDistrict: DistrictData? = null,
    val pickerSelectedMunicipality: MunicipalityData? = null,
    val pickerSelectedWard: Int? = null,
    val pickerSearchQuery: String = "",
    val isDetectingGpsLocation: Boolean = false,
    val gpsDetectionMessage: String = "",
    val lowDataModeEnabled: Boolean = false,
    val currentUser: GaunUser = GaunUser(uid = "guest_citizen", isAnonymous = true),
    val isAdminAuthDialogOpen: Boolean = false,
    val isAuthLoading: Boolean = false,
    val authErrorMessage: String = "",
    val isAdminNoticeDialogOpen: Boolean = false,
    val adminEditingNotice: LocalNotice? = null,
    val isAdminFacilityDialogOpen: Boolean = false,
    val adminEditingFacility: HealthFacility? = null,
    val isAdminEmergencyDialogOpen: Boolean = false,
    val adminEditingEmergency: EmergencyContact? = null,
    val isShowingDemoFormat: Boolean = false,
    val showRecordAudioRationale: Boolean = false,
    val isCheckingWeather: Boolean = false,
    val weatherCheckMessage: String = ""
) {
    val isAdmin: Boolean
        get() = currentUser.isAdmin
}
