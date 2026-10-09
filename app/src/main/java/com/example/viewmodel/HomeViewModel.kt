package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiAssistantService
import com.example.data.AiMessage
import com.example.data.AppAiContext
import com.example.data.ConfigurableHealthEmergencyRepository
import com.example.data.FarmingAiMessage
import com.example.data.FirebaseRepository
import com.example.data.GaunRepository
import com.example.data.GeminiFarmingAiService
import com.example.data.GpsLocationProvider
import com.example.data.LocalNoticeRepository
import com.example.data.LocationPreferencesRepository
import com.example.data.NepalAdministrativeData
import com.example.data.NetworkMonitor
import com.example.data.OpenMeteoWeatherService
import com.example.data.WeatherCacheRepository
import com.example.data.voice.VoiceInputManager
import com.example.data.voice.VoiceOutputManager
import com.example.model.BottomNavTab
import com.example.model.CommunityService
import com.example.model.DistrictData
import com.example.model.EmergencyContact
import com.example.model.GaunLanguage
import com.example.model.GaunLocation
import com.example.model.HealthFacilitiesUiState
import com.example.model.HealthFacility
import com.example.model.HomeUiState
import com.example.model.LocalNotice
import com.example.model.LocationPickerStep
import com.example.model.MunicipalityData
import com.example.model.NoticeCategory
import com.example.model.ProvinceData
import com.example.model.ServicesSubTab
import com.example.model.WeatherUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val networkMonitor = NetworkMonitor(context)
    private val locationPrefs = LocationPreferencesRepository(context)
    private val weatherCache = WeatherCacheRepository(context)
    private val weatherService = OpenMeteoWeatherService()
    private val noticeRepo = LocalNoticeRepository()
    private val healthRepo = ConfigurableHealthEmergencyRepository()
    private val gaunRepo = GaunRepository()
    private val farmingAiService = GeminiFarmingAiService()
    private val aiAssistantService = AiAssistantService()
    private val gpsProvider = GpsLocationProvider(context)
    private val firebaseRepo = FirebaseRepository(context)
    private val voiceInputManager = VoiceInputManager(context)
    private val voiceOutputManager = VoiceOutputManager(context)

    private val _uiState = MutableStateFlow(
        HomeUiState(
            selectedLocation = NepalAdministrativeData.defaultLocation,
            communityServices = gaunRepo.getCommunityServices(),
            emergencyContacts = gaunRepo.getEmergencyContacts(),
            healthEmergencyNumbers = healthRepo.getEmergencyNumbers(),
            healthGuidelines = healthRepo.getBasicHealthGuidelines()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Collect network state
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
            }
        }

        // Collect saved location
        viewModelScope.launch {
            locationPrefs.locationFlow.collect { savedLoc ->
                val loc = savedLoc ?: NepalAdministrativeData.defaultLocation
                _uiState.update { it.copy(selectedLocation = loc) }
                fetchWeatherForLocation(loc)
                fetchNoticesForLocation(loc)
                fetchHealthEmergencyData(loc)
            }
        }

        // Collect auth state
        viewModelScope.launch {
            firebaseRepo.getCurrentUserFlow().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    fun toggleLanguage() {
        _uiState.update {
            val next = if (it.language == GaunLanguage.NEPALI) GaunLanguage.ENGLISH else GaunLanguage.NEPALI
            it.copy(language = next)
        }
    }

    fun selectTab(tab: BottomNavTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectServicesSubTab(subTab: ServicesSubTab) {
        _uiState.update { it.copy(servicesSubTab = subTab) }
    }

    fun refreshWeather() {
        fetchWeatherForLocation(_uiState.value.selectedLocation)
    }

    fun fetchWeatherForLocation(location: GaunLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(weatherState = WeatherUiState.Loading) }

            val lat = location.latitude ?: 27.7172
            val lon = location.longitude ?: 85.3240

            if (!_uiState.value.isOnline) {
                val cached = weatherCache.getCachedWeather(location.id)
                if (cached != null) {
                    _uiState.update {
                        it.copy(weatherState = WeatherUiState.Offline("अफलाइन डाटा", "Offline data", cached))
                    }
                } else {
                    _uiState.update {
                        it.copy(weatherState = WeatherUiState.Offline("इन्टरनेट छैन", "No internet connection"))
                    }
                }
                return@launch
            }

            val result = weatherService.fetchWeather(lat, lon, location)
            result.onSuccess { data ->
                weatherCache.saveWeather(location.id, data)
                _uiState.update { it.copy(weatherState = WeatherUiState.Success(data)) }
            }.onFailure { err ->
                val cached = weatherCache.getCachedWeather(location.id)
                _uiState.update {
                    it.copy(weatherState = WeatherUiState.Error("मौसम अद्यावधिक भएन: ${err.message}", "Could not refresh weather: ${err.message}", cached))
                }
            }
        }
    }

    fun fetchNoticesForLocation(location: GaunLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isNoticesLoading = true) }
            val res = noticeRepo.fetchNotices(location)
            res.onSuccess { notices ->
                _uiState.update { it.copy(noticesList = notices, isNoticesLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(isNoticesLoading = false) }
            }
        }
    }

    fun fetchHealthEmergencyData(location: GaunLocation) {
        viewModelScope.launch {
            val amb = healthRepo.getAmbulanceServices(location)
            _uiState.update { it.copy(ambulanceServices = amb) }
            healthRepo.getNearbyHealthFacilities(location).collect { state ->
                _uiState.update { it.copy(healthFacilitiesState = state) }
            }
        }
    }

    // Location Picker
    fun onOpenLocationPicker() {
        _uiState.update {
            it.copy(
                isLocationPickerOpen = true,
                pickerStep = LocationPickerStep.SELECT_PROVINCE,
                pickerSelectedProvince = null,
                pickerSelectedDistrict = null,
                pickerSelectedMunicipality = null,
                pickerSearchQuery = ""
            )
        }
    }

    fun onDismissLocationPicker() {
        _uiState.update { it.copy(isLocationPickerOpen = false) }
    }

    fun onPickerSelectProvince(province: ProvinceData) {
        _uiState.update {
            it.copy(
                pickerSelectedProvince = province,
                pickerSelectedDistrict = null,
                pickerSelectedMunicipality = null,
                pickerStep = LocationPickerStep.SELECT_DISTRICT
            )
        }
    }

    fun onPickerSelectDistrict(district: DistrictData) {
        _uiState.update {
            it.copy(
                pickerSelectedDistrict = district,
                pickerSelectedMunicipality = null,
                pickerStep = LocationPickerStep.SELECT_MUNICIPALITY
            )
        }
    }

    fun onPickerSelectMunicipality(municipality: MunicipalityData) {
        _uiState.update {
            it.copy(
                pickerSelectedMunicipality = municipality,
                pickerStep = LocationPickerStep.SELECT_WARD
            )
        }
    }

    fun onPickerSelectWard(ward: Int?) {
        val s = _uiState.value
        val prov = s.pickerSelectedProvince ?: return
        val dist = s.pickerSelectedDistrict ?: return
        val muni = s.pickerSelectedMunicipality ?: return

        val newLoc = NepalAdministrativeData.findLocation(prov.id, dist.id, muni.id, ward)
            ?: return

        applyAndSaveLocation(newLoc)
        onDismissLocationPicker()
    }

    fun onPickerStepBack() {
        _uiState.update {
            when (it.pickerStep) {
                LocationPickerStep.SELECT_WARD -> it.copy(pickerStep = LocationPickerStep.SELECT_MUNICIPALITY)
                LocationPickerStep.SELECT_MUNICIPALITY -> it.copy(pickerStep = LocationPickerStep.SELECT_DISTRICT)
                LocationPickerStep.SELECT_DISTRICT -> it.copy(pickerStep = LocationPickerStep.SELECT_PROVINCE)
                LocationPickerStep.SELECT_PROVINCE -> it.copy(isLocationPickerOpen = false)
            }
        }
    }

    fun onUseCurrentLocation(hasPermission: Boolean) {
        if (!hasPermission) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDetectingGpsLocation = true) }
            val detected = gpsProvider.getCurrentGaunLocation()
            if (detected != null) {
                applyAndSaveLocation(detected)
                onDismissLocationPicker()
            }
            _uiState.update { it.copy(isDetectingGpsLocation = false) }
        }
    }

    fun applyAndSaveLocation(location: GaunLocation) {
        viewModelScope.launch {
            locationPrefs.saveLocation(location)
            _uiState.update { it.copy(selectedLocation = location) }
            fetchWeatherForLocation(location)
            fetchNoticesForLocation(location)
            fetchHealthEmergencyData(location)
        }
    }

    // AI Chat & Speech
    fun onOpenAiAssistant() {
        _uiState.update { it.copy(isAiAssistantOpen = true) }
    }

    fun onDismissAiAssistant() {
        _uiState.update { it.copy(isAiAssistantOpen = false) }
        stopVoiceListening()
    }

    fun onAiPromptChanged(prompt: String) {
        _uiState.update { it.copy(aiPromptInput = prompt) }
    }

    fun sendAiPrompt(customPrompt: String? = null) {
        val prompt = customPrompt ?: _uiState.value.aiPromptInput
        if (prompt.isBlank()) return

        val userMsg = AiMessage(sender = AiMessage.MessageSender.USER, text = prompt)
        _uiState.update {
            it.copy(
                aiMessages = it.aiMessages + userMsg,
                aiPromptInput = "",
                isAiThinking = true
            )
        }

        viewModelScope.launch {
            val currState = _uiState.value
            val appContext = AppAiContext(
                location = currState.selectedLocation,
                language = currState.language,
                weather = (currState.weatherState as? WeatherUiState.Success)?.data,
                notices = currState.noticesList,
                healthFacilities = (currState.healthFacilitiesState as? HealthFacilitiesUiState.Success)?.facilities ?: emptyList(),
                emergencyContacts = currState.emergencyContacts
            )

            val reply = aiAssistantService.askAssistant(
                prompt = prompt,
                sessionHistory = currState.aiMessages,
                language = currState.language,
                appContext = appContext
            )

            _uiState.update {
                it.copy(
                    aiMessages = it.aiMessages + reply,
                    isAiThinking = false
                )
            }

            if (_uiState.value.isVoiceModeEnabled) {
                speakMessage(reply)
            }
        }
    }

    // Farming AI
    fun onOpenFarmingAiScreen() {
        _uiState.update { it.copy(isFarmingAiScreenOpen = true, currentTab = BottomNavTab.FARMING) }
    }

    fun onDismissFarmingAiScreen() {
        _uiState.update { it.copy(isFarmingAiScreenOpen = false, currentTab = BottomNavTab.HOME) }
    }

    fun onFarmingAiPromptChanged(prompt: String) {
        _uiState.update { it.copy(farmingAiInput = prompt) }
    }

    fun sendFarmingAiPrompt(customPrompt: String? = null) {
        val prompt = customPrompt ?: _uiState.value.farmingAiInput
        if (prompt.isBlank()) return

        val userMsg = FarmingAiMessage(sender = FarmingAiMessage.MessageSender.USER, text = prompt)
        _uiState.update {
            it.copy(
                farmingAiMessages = it.farmingAiMessages + userMsg,
                farmingAiInput = "",
                isFarmingAiThinking = true
            )
        }

        viewModelScope.launch {
            val loc = _uiState.value.selectedLocation
            val locationContext = "${loc.provinceNe} प्रदेश, ${loc.districtNe} जिल्ला, ${loc.nameNe}, वडा नं ${loc.wardNumber ?: 1}"
            val reply = farmingAiService.getAdvice(
                prompt = prompt,
                language = _uiState.value.language,
                locationName = locationContext,
                isOnline = _uiState.value.isOnline
            )
            _uiState.update {
                it.copy(
                    farmingAiMessages = it.farmingAiMessages + reply,
                    isFarmingAiThinking = false
                )
            }
        }
    }

    // Voice Handling
    fun toggleVoiceMode() {
        _uiState.update { it.copy(isVoiceModeEnabled = !it.isVoiceModeEnabled) }
    }

    fun startVoiceListening(hasAudioPermission: Boolean) {
        if (!hasAudioPermission) {
            _uiState.update { it.copy(showRecordAudioRationale = true) }
            return
        }

        _uiState.update { it.copy(isVoiceListening = true, voiceErrorMessage = "") }

        voiceInputManager.startListening(
            language = _uiState.value.language,
            onPartial = { partial ->
                _uiState.update { it.copy(voicePartialText = partial) }
            },
            onResult = { text ->
                _uiState.update { it.copy(isVoiceListening = false, voicePartialText = "", aiPromptInput = text) }
                sendAiPrompt(text)
            },
            onError = { err ->
                _uiState.update { it.copy(isVoiceListening = false, voiceErrorMessage = err) }
            }
        )
    }

    fun stopVoiceListening() {
        voiceInputManager.stopListening()
        _uiState.update { it.copy(isVoiceListening = false) }
    }

    fun speakMessage(message: AiMessage) {
        _uiState.update { it.copy(currentlySpeakingMessageId = message.id) }
        voiceOutputManager.speak(
            text = message.text,
            language = _uiState.value.language,
            utteranceId = message.id,
            onStart = {
                _uiState.update { it.copy(currentlySpeakingMessageId = message.id) }
            },
            onDone = {
                _uiState.update { it.copy(currentlySpeakingMessageId = null) }
            }
        )
    }

    fun stopSpeaking() {
        voiceOutputManager.stop()
        _uiState.update { it.copy(currentlySpeakingMessageId = null) }
    }

    // Notice & Service selection
    fun onSelectNotice(notice: LocalNotice) {
        _uiState.update { it.copy(selectedNotice = notice, isNoticeInfoOpen = true) }
    }

    fun onDismissNoticeDetail() {
        _uiState.update { it.copy(selectedNotice = null, isNoticeInfoOpen = false) }
    }

    fun onSelectNoticeCategory(cat: NoticeCategory) {
        _uiState.update { it.copy(noticeCategoryFilter = cat) }
    }

    fun onNoticeSearchQueryChanged(query: String) {
        _uiState.update { it.copy(noticeSearchQuery = query) }
    }

    fun onServiceSelected(service: CommunityService) {
        _uiState.update { it.copy(selectedService = service) }
    }

    fun onDismissServiceDetails() {
        _uiState.update { it.copy(selectedService = null) }
    }

    fun onEmergencyContactSelected(contact: EmergencyContact) {
        _uiState.update { it.copy(selectedEmergencyContact = contact) }
    }

    fun onDismissEmergencyDialog() {
        _uiState.update { it.copy(selectedEmergencyContact = null) }
    }

    // Admin Auth & Management
    fun onOpenAdminAuthDialog() {
        _uiState.update { it.copy(isAdminAuthDialogOpen = true, authErrorMessage = "") }
    }

    fun onDismissAdminAuthDialog() {
        _uiState.update { it.copy(isAdminAuthDialogOpen = false) }
    }

    fun signInWithEmail(email: String, pass: String) {
        firebaseRepo.signInWithEmail(email, pass) { success, err ->
            if (success) {
                _uiState.update { it.copy(isAdminAuthDialogOpen = false) }
            } else {
                _uiState.update { it.copy(authErrorMessage = err ?: "लगइन असफल भयो") }
            }
        }
    }

    fun signOut() {
        firebaseRepo.signOut()
    }

    fun onOpenCreateNoticeDialog() {
        _uiState.update { it.copy(isAdminNoticeDialogOpen = true, adminEditingNotice = null) }
    }

    fun onOpenEditNoticeDialog(notice: LocalNotice) {
        _uiState.update { it.copy(isAdminNoticeDialogOpen = true, adminEditingNotice = notice) }
    }

    fun onDismissNoticeDialog() {
        _uiState.update { it.copy(isAdminNoticeDialogOpen = false, adminEditingNotice = null) }
    }

    fun saveNotice(notice: LocalNotice) {
        firebaseRepo.addNotice(notice)
        _uiState.update {
            val list = it.noticesList.toMutableList()
            val idx = list.indexOfFirst { n -> n.id == notice.id }
            if (idx >= 0) list[idx] = notice else list.add(0, notice)
            it.copy(noticesList = list)
        }
    }

    fun deleteNotice(noticeId: String) {
        firebaseRepo.deleteNotice(noticeId)
        _uiState.update {
            it.copy(noticesList = it.noticesList.filterNot { n -> n.id == noticeId })
        }
    }

    fun onOpenCreateFacilityDialog() {
        _uiState.update { it.copy(isAdminFacilityDialogOpen = true, adminEditingFacility = null) }
    }

    fun onOpenEditFacilityDialog(facility: HealthFacility) {
        _uiState.update { it.copy(isAdminFacilityDialogOpen = true, adminEditingFacility = facility) }
    }

    fun onDismissFacilityDialog() {
        _uiState.update { it.copy(isAdminFacilityDialogOpen = false, adminEditingFacility = null) }
    }

    fun saveHealthFacility(facility: HealthFacility) {
        firebaseRepo.addHealthFacility(facility)
        fetchHealthEmergencyData(_uiState.value.selectedLocation)
    }

    fun deleteHealthFacility(facilityId: String) {
        firebaseRepo.deleteHealthFacility(facilityId)
        fetchHealthEmergencyData(_uiState.value.selectedLocation)
    }

    fun onOpenCreateEmergencyDialog() {
        _uiState.update { it.copy(isAdminEmergencyDialogOpen = true, adminEditingEmergency = null) }
    }

    fun onOpenEditEmergencyDialog(contact: EmergencyContact) {
        _uiState.update { it.copy(isAdminEmergencyDialogOpen = true, adminEditingEmergency = contact) }
    }

    fun onDismissEmergencyAdminDialog() {
        _uiState.update { it.copy(isAdminEmergencyDialogOpen = false, adminEditingEmergency = null) }
    }

    fun saveEmergencyContact(contact: EmergencyContact) {
        firebaseRepo.addEmergencyContact(contact)
        _uiState.update {
            val list = it.emergencyContacts.toMutableList()
            val idx = list.indexOfFirst { c -> c.id == contact.id }
            if (idx >= 0) list[idx] = contact else list.add(contact)
            it.copy(emergencyContacts = list)
        }
    }

    fun deleteEmergencyContact(contactId: String) {
        firebaseRepo.deleteEmergencyContact(contactId)
        _uiState.update {
            it.copy(emergencyContacts = it.emergencyContacts.filterNot { c -> c.id == contactId })
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceOutputManager.shutdown()
        voiceInputManager.stopListening()
    }
}
