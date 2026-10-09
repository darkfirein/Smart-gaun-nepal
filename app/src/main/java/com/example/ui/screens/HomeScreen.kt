package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.NepalAdministrativeData
import com.example.model.BottomNavTab
import com.example.model.CommunityService
import com.example.model.DistrictData
import com.example.model.GaunLanguage
import com.example.model.HomeUiState
import com.example.model.LocalNotice
import com.example.model.LocationPickerStep
import com.example.model.MunicipalityData
import com.example.model.ProvinceData
import com.example.ui.components.AdminAuthDialog
import com.example.ui.components.AdminEmergencyDialog
import com.example.ui.components.AdminHealthFacilityDialog
import com.example.ui.components.AdminNoticeDialog
import com.example.ui.components.AiAssistantBottomSheet
import com.example.ui.components.AiAssistantHomeCard
import com.example.ui.components.CitizenNoticeGuidelineDialog
import com.example.ui.components.CommunityServiceCard
import com.example.ui.components.EmergencyDialerDialog
import com.example.ui.components.EmergencyQuickSection
import com.example.ui.components.FarmingCard
import com.example.ui.components.GaunHeader
import com.example.ui.components.LocationSelectorBar
import com.example.ui.components.NoticeCard
import com.example.ui.components.ServiceDetailDialog
import com.example.ui.components.WeatherCard
import com.example.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var dialNumber by remember { mutableStateOf<String?>(null) }
    var dialTitle by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onUseCurrentLocation(granted)
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.startVoiceListening(granted)
    }

    BackHandler(enabled = uiState.currentTab != BottomNavTab.HOME) {
        viewModel.selectTab(BottomNavTab.HOME)
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("main_scaffold"),
        topBar = {
            Column {
                GaunHeader(
                    language = uiState.language,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    isVoiceMode = uiState.isVoiceModeEnabled,
                    onToggleVoiceMode = { viewModel.toggleVoiceMode() }
                )
                // Admin Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isAdmin) {
                        Text(
                            text = "वडा अधिकृत (Admin Mode)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(
                            onClick = { viewModel.signOut() },
                            modifier = Modifier.testTag("admin_signout_button")
                        ) {
                            Text(text = "बाहिरिनुहोस् (Sign Out)", fontSize = 11.sp)
                        }
                    } else {
                        TextButton(
                            onClick = { viewModel.onOpenAdminAuthDialog() },
                            modifier = Modifier.testTag("admin_login_entry_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.language == GaunLanguage.NEPALI) "प्रशासक लगइन" else "Admin Login",
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
                NavigationBarItem(
                    selected = uiState.currentTab == BottomNavTab.HOME,
                    onClick = { viewModel.selectTab(BottomNavTab.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text(if (uiState.language == GaunLanguage.NEPALI) "गृहपृष्ठ" else "Home") },
                    modifier = Modifier.testTag("nav_item_home")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == BottomNavTab.SERVICES,
                    onClick = { viewModel.selectTab(BottomNavTab.SERVICES) },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Services") },
                    label = { Text(if (uiState.language == GaunLanguage.NEPALI) "सेवा" else "Services") },
                    modifier = Modifier.testTag("nav_item_services")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == BottomNavTab.NOTICES,
                    onClick = { viewModel.selectTab(BottomNavTab.NOTICES) },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = "Notices") },
                    label = { Text(if (uiState.language == GaunLanguage.NEPALI) "सूचना" else "Notices") },
                    modifier = Modifier.testTag("nav_item_notices")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == BottomNavTab.FARMING,
                    onClick = { viewModel.selectTab(BottomNavTab.FARMING) },
                    icon = { Icon(Icons.Default.Agriculture, contentDescription = "Farming") },
                    label = { Text(if (uiState.language == GaunLanguage.NEPALI) "कृषि" else "Agro") },
                    modifier = Modifier.testTag("nav_item_farming")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == BottomNavTab.HEALTH,
                    onClick = { viewModel.selectTab(BottomNavTab.HEALTH) },
                    icon = { Icon(Icons.Default.LocalHospital, contentDescription = "Health") },
                    label = { Text(if (uiState.language == GaunLanguage.NEPALI) "स्वास्थ्य" else "Health") },
                    modifier = Modifier.testTag("nav_item_health")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == BottomNavTab.AI,
                    onClick = { viewModel.selectTab(BottomNavTab.AI) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") },
                    label = { Text("AI") },
                    modifier = Modifier.testTag("nav_item_ai")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                BottomNavTab.HOME -> {
                    HomeDashboardContent(
                        uiState = uiState,
                        onOpenLocationPicker = { viewModel.onOpenLocationPicker() },
                        onDetectGps = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                viewModel.onUseCurrentLocation(true)
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        onRefreshWeather = { viewModel.refreshWeather() },
                        onCallEmergency = { num, title ->
                            dialNumber = num
                            dialTitle = title
                        },
                        onViewAllEmergency = { viewModel.selectTab(BottomNavTab.HEALTH) },
                        onOpenAi = { viewModel.onOpenAiAssistant() },
                        onStartVoice = {
                            val hasMic = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasMic) {
                                viewModel.startVoiceListening(true)
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onOpenFarming = { viewModel.selectTab(BottomNavTab.FARMING) },
                        onSelectNotice = { viewModel.onSelectNotice(it) },
                        onViewAllNotices = { viewModel.selectTab(BottomNavTab.NOTICES) },
                        onSelectService = { viewModel.onServiceSelected(it) },
                        onViewAllServices = { viewModel.selectTab(BottomNavTab.SERVICES) }
                    )
                }

                BottomNavTab.SERVICES -> {
                    ServicesScreen(
                        location = uiState.selectedLocation,
                        language = uiState.language,
                        isAdmin = uiState.isAdmin,
                        currentSubTab = uiState.servicesSubTab,
                        notices = uiState.noticesList,
                        isNoticesLoading = uiState.isNoticesLoading,
                        healthFacilitiesState = uiState.healthFacilitiesState,
                        emergencyContacts = uiState.emergencyContacts,
                        communityServices = uiState.communityServices,
                        onSelectSubTab = { viewModel.selectServicesSubTab(it) },
                        onSelectService = { viewModel.onServiceSelected(it) },
                        onSelectNotice = { viewModel.onSelectNotice(it) },
                        onCallEmergency = { num, title ->
                            dialNumber = num
                            dialTitle = title
                        },
                        onOpenCreateNotice = { viewModel.onOpenCreateNoticeDialog() },
                        onOpenEditNotice = { viewModel.onOpenEditNoticeDialog(it) },
                        onOpenCreateFacility = { viewModel.onOpenCreateFacilityDialog() },
                        onOpenEditFacility = { viewModel.onOpenEditFacilityDialog(it) },
                        onOpenCreateEmergency = { viewModel.onOpenCreateEmergencyDialog() },
                        onOpenEditEmergency = { viewModel.onOpenEditEmergencyDialog(it) }
                    )
                }

                BottomNavTab.NOTICES -> {
                    NoticeScreen(
                        location = uiState.selectedLocation,
                        notices = uiState.noticesList,
                        selectedCategory = uiState.noticeCategoryFilter,
                        searchQuery = uiState.noticeSearchQuery,
                        language = uiState.language,
                        isAdmin = uiState.isAdmin,
                        onBack = { viewModel.selectTab(BottomNavTab.HOME) },
                        onSelectCategory = { viewModel.onSelectNoticeCategory(it) },
                        onSearchQueryChange = { viewModel.onNoticeSearchQueryChanged(it) },
                        onSelectNotice = { viewModel.onSelectNotice(it) },
                        onOpenCreateNotice = { viewModel.onOpenCreateNoticeDialog() },
                        onOpenEditNotice = { viewModel.onOpenEditNoticeDialog(it) }
                    )
                }

                BottomNavTab.FARMING -> {
                    FarmingAiScreen(
                        location = uiState.selectedLocation,
                        messages = uiState.farmingAiMessages,
                        isThinking = uiState.isFarmingAiThinking,
                        inputPrompt = uiState.farmingAiInput,
                        language = uiState.language,
                        onBack = { viewModel.selectTab(BottomNavTab.HOME) },
                        onPromptChange = { viewModel.onFarmingAiPromptChanged(it) },
                        onSend = { viewModel.sendFarmingAiPrompt(it) }
                    )
                }

                BottomNavTab.HEALTH -> {
                    HealthEmergencyScreen(
                        location = uiState.selectedLocation,
                        emergencyNumbers = uiState.healthEmergencyNumbers,
                        ambulanceServices = uiState.ambulanceServices,
                        healthFacilitiesState = uiState.healthFacilitiesState,
                        healthGuidelines = uiState.healthGuidelines,
                        language = uiState.language,
                        isAdmin = uiState.isAdmin,
                        onBack = { viewModel.selectTab(BottomNavTab.HOME) },
                        onCall = { num, title ->
                            dialNumber = num
                            dialTitle = title
                        },
                        onOpenCreateFacility = { viewModel.onOpenCreateFacilityDialog() },
                        onOpenEditFacility = { viewModel.onOpenEditFacilityDialog(it) }
                    )
                }

                BottomNavTab.AI -> {
                    AiTabScreen(
                        location = uiState.selectedLocation,
                        language = uiState.language,
                        messages = uiState.aiMessages,
                        inputText = uiState.aiPromptInput,
                        onInputTextChange = { viewModel.onAiPromptChanged(it) },
                        onSendMessage = { viewModel.sendAiPrompt(it) },
                        isThinking = uiState.isAiThinking,
                        isListening = uiState.isVoiceListening,
                        onStartVoice = {
                            val hasMic = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasMic) {
                                viewModel.startVoiceListening(true)
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onSpeakMessage = { viewModel.speakMessage(it) }
                    )
                }
            }
        }
    }

    // Bottom Sheet Assistant
    if (uiState.isAiAssistantOpen) {
        AiAssistantBottomSheet(
            messages = uiState.aiMessages,
            inputText = uiState.aiPromptInput,
            onInputTextChange = { viewModel.onAiPromptChanged(it) },
            onSendMessage = { viewModel.sendAiPrompt(it) },
            isThinking = uiState.isAiThinking,
            isListening = uiState.isVoiceListening,
            onStartVoice = {
                val hasMic = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasMic) {
                    viewModel.startVoiceListening(true)
                } else {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            language = uiState.language,
            sheetState = sheetState,
            onDismiss = { viewModel.onDismissAiAssistant() }
        )
    }

    // Location Picker Dialog
    if (uiState.isLocationPickerOpen) {
        LocationPickerDialog(
            uiState = uiState,
            onSelectProvince = { viewModel.onPickerSelectProvince(it) },
            onSelectDistrict = { viewModel.onPickerSelectDistrict(it) },
            onSelectMunicipality = { viewModel.onPickerSelectMunicipality(it) },
            onSelectWard = { viewModel.onPickerSelectWard(it) },
            onStepBack = { viewModel.onPickerStepBack() },
            onDismiss = { viewModel.onDismissLocationPicker() }
        )
    }

    // Emergency Calling Dialog
    if (dialNumber != null) {
        EmergencyDialerDialog(
            phoneNumber = dialNumber ?: "",
            title = dialTitle ?: "Helpline",
            language = uiState.language,
            onDismiss = {
                dialNumber = null
                dialTitle = null
            }
        )
    }

    // Service Detail Dialog
    uiState.selectedService?.let { service ->
        ServiceDetailDialog(
            service = service,
            language = uiState.language,
            onDismiss = { viewModel.onDismissServiceDetails() }
        )
    }

    // Notice Detail Dialog
    uiState.selectedNotice?.let { notice ->
        CitizenNoticeGuidelineDialog(
            notice = notice,
            language = uiState.language,
            onDismiss = { viewModel.onDismissNoticeDetail() }
        )
    }

    // Admin Auth Dialog
    if (uiState.isAdminAuthDialogOpen) {
        AdminAuthDialog(
            language = uiState.language,
            onLogin = { email, pass -> viewModel.signInWithEmail(email, pass) },
            onDismiss = { viewModel.onDismissAdminAuthDialog() },
            errorMessage = uiState.authErrorMessage.ifBlank { null }
        )
    }

    // Admin Notice Dialog
    if (uiState.isAdminNoticeDialogOpen) {
        AdminNoticeDialog(
            initialNotice = uiState.adminEditingNotice,
            language = uiState.language,
            onSave = { viewModel.saveNotice(it) },
            onDelete = { viewModel.deleteNotice(it) },
            onDismiss = { viewModel.onDismissNoticeDialog() }
        )
    }

    // Admin Health Facility Dialog
    if (uiState.isAdminFacilityDialogOpen) {
        AdminHealthFacilityDialog(
            initialFacility = uiState.adminEditingFacility,
            language = uiState.language,
            onSave = { viewModel.saveHealthFacility(it) },
            onDelete = { viewModel.deleteHealthFacility(it) },
            onDismiss = { viewModel.onDismissFacilityDialog() }
        )
    }

    // Admin Emergency Contact Dialog
    if (uiState.isAdminEmergencyDialogOpen) {
        AdminEmergencyDialog(
            initialContact = uiState.adminEditingEmergency,
            language = uiState.language,
            onSave = { viewModel.saveEmergencyContact(it) },
            onDelete = { viewModel.deleteEmergencyContact(it) },
            onDismiss = { viewModel.onDismissEmergencyAdminDialog() }
        )
    }
}

@Composable
private fun HomeDashboardContent(
    uiState: HomeUiState,
    onOpenLocationPicker: () -> Unit,
    onDetectGps: () -> Unit,
    onRefreshWeather: () -> Unit,
    onCallEmergency: (String, String) -> Unit,
    onViewAllEmergency: () -> Unit,
    onOpenAi: () -> Unit,
    onStartVoice: () -> Unit,
    onOpenFarming: () -> Unit,
    onSelectNotice: (LocalNotice) -> Unit,
    onViewAllNotices: () -> Unit,
    onSelectService: (CommunityService) -> Unit,
    onViewAllServices: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(2.dp)) }

        // 1. Location Bar
        item {
            LocationSelectorBar(
                location = uiState.selectedLocation,
                language = uiState.language,
                onOpenPicker = onOpenLocationPicker,
                onDetectGps = onDetectGps,
                isDetectingGps = uiState.isDetectingGpsLocation
            )
        }

        // 2. Weather Card
        item {
            WeatherCard(
                weatherState = uiState.weatherState,
                language = uiState.language,
                onRefresh = onRefreshWeather
            )
        }

        // 3. Emergency SOS Section
        item {
            EmergencyQuickSection(
                emergencyNumbers = uiState.healthEmergencyNumbers,
                language = uiState.language,
                onCallNumber = onCallEmergency,
                onViewAll = onViewAllEmergency
            )
        }

        // 4. AI Civic Assistant Card
        item {
            AiAssistantHomeCard(
                language = uiState.language,
                onOpenAi = onOpenAi,
                onStartVoice = onStartVoice
            )
        }

        // 5. Agro Advisory Card
        item {
            FarmingCard(
                language = uiState.language,
                onOpenFarmingAi = onOpenFarming
            )
        }

        // 6. Latest Ward Notices
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.language == GaunLanguage.NEPALI) "ताजा वडा सूचनाहरू" else "Latest Ward Notices",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (uiState.language == GaunLanguage.NEPALI) "सबै सूचना →" else "View All →",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onViewAllNotices() }
                )
            }
        }

        val topNotices = uiState.noticesList.take(2)
        items(items = topNotices, key = { it.id }) { notice ->
            NoticeCard(
                notice = notice,
                language = uiState.language,
                onClick = { onSelectNotice(notice) }
            )
        }

        // 7. Popular Citizen Charter Services
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.language == GaunLanguage.NEPALI) "नागरिक वडापत्र (सेवाहरू)" else "Citizen Charter Services",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (uiState.language == GaunLanguage.NEPALI) "सबै सेवा →" else "View All →",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onViewAllServices() }
                )
            }
        }

        val topServices = uiState.communityServices.take(2)
        items(items = topServices, key = { it.id }) { service ->
            CommunityServiceCard(
                service = service,
                language = uiState.language,
                onClick = { onSelectService(service) }
            )
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun LocationPickerDialog(
    uiState: HomeUiState,
    onSelectProvince: (ProvinceData) -> Unit,
    onSelectDistrict: (DistrictData) -> Unit,
    onSelectMunicipality: (MunicipalityData) -> Unit,
    onSelectWard: (Int) -> Unit,
    onStepBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val lang = uiState.language

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = when (uiState.pickerStep) {
                        LocationPickerStep.SELECT_PROVINCE -> if (lang == GaunLanguage.NEPALI) "प्रदेश छान्नुहोस्" else "Select Province"
                        LocationPickerStep.SELECT_DISTRICT -> if (lang == GaunLanguage.NEPALI) "जिल्ला छान्नुहोस्" else "Select District"
                        LocationPickerStep.SELECT_MUNICIPALITY -> if (lang == GaunLanguage.NEPALI) "स्थानीय तह छान्नुहोस्" else "Select Municipality"
                        LocationPickerStep.SELECT_WARD -> if (lang == GaunLanguage.NEPALI) "वडा नम्बर छान्नुहोस्" else "Select Ward Number"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                when (uiState.pickerStep) {
                    LocationPickerStep.SELECT_PROVINCE -> {
                        for (prov in NepalAdministrativeData.provinces) {
                            LocationItemRow(
                                title = if (lang == GaunLanguage.NEPALI) "${prov.nameNe} प्रदेश" else "${prov.nameEn} Province",
                                onClick = { onSelectProvince(prov) }
                            )
                        }
                    }

                    LocationPickerStep.SELECT_DISTRICT -> {
                        val districts = uiState.pickerSelectedProvince?.districts.orEmpty()
                        for (dist in districts) {
                            LocationItemRow(
                                title = if (lang == GaunLanguage.NEPALI) dist.nameNe else dist.nameEn,
                                onClick = { onSelectDistrict(dist) }
                            )
                        }
                    }

                    LocationPickerStep.SELECT_MUNICIPALITY -> {
                        val municipalities = uiState.pickerSelectedDistrict?.municipalities.orEmpty()
                        for (muni in municipalities) {
                            LocationItemRow(
                                title = if (lang == GaunLanguage.NEPALI) muni.nameNe else muni.nameEn,
                                onClick = { onSelectMunicipality(muni) }
                            )
                        }
                    }

                    LocationPickerStep.SELECT_WARD -> {
                        val maxWards = uiState.pickerSelectedMunicipality?.maxWards ?: 9
                        for (w in 1..maxWards) {
                            LocationItemRow(
                                title = if (lang == GaunLanguage.NEPALI) "वडा नं. $w" else "Ward No. $w",
                                onClick = { onSelectWard(w) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (uiState.pickerStep != LocationPickerStep.SELECT_PROVINCE) {
                TextButton(onClick = onStepBack) {
                    Text(text = if (lang == GaunLanguage.NEPALI) "पछाडि (Back)" else "Back")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (lang == GaunLanguage.NEPALI) "रद्द (Cancel)" else "Cancel")
            }
        },
        modifier = Modifier.testTag("location_picker_dialog")
    )
}

@Composable
private fun LocationItemRow(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(14.dp)
        )
    }
}
