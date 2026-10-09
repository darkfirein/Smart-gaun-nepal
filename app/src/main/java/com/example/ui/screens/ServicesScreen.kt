package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CommunityService
import com.example.model.EmergencyContact
import com.example.model.GaunLanguage
import com.example.model.GaunLocation
import com.example.model.HealthFacilitiesUiState
import com.example.model.HealthFacility
import com.example.model.LocalNotice
import com.example.model.ServicesSubTab
import com.example.ui.components.CommunityServiceCard
import com.example.ui.components.HealthEmergencyCard
import com.example.ui.components.NoticeCard

@Composable
fun ServicesScreen(
    location: GaunLocation,
    language: GaunLanguage,
    isAdmin: Boolean,
    currentSubTab: ServicesSubTab,
    notices: List<LocalNotice>,
    isNoticesLoading: Boolean,
    healthFacilitiesState: HealthFacilitiesUiState,
    emergencyContacts: List<EmergencyContact>,
    communityServices: List<CommunityService>,
    onSelectSubTab: (ServicesSubTab) -> Unit,
    onSelectService: (CommunityService) -> Unit,
    onSelectNotice: (LocalNotice) -> Unit,
    onCallEmergency: (String, String) -> Unit,
    onOpenCreateNotice: () -> Unit,
    onOpenEditNotice: (LocalNotice) -> Unit,
    onOpenCreateFacility: () -> Unit,
    onOpenEditFacility: (HealthFacility) -> Unit,
    onOpenCreateEmergency: () -> Unit,
    onOpenEditEmergency: (EmergencyContact) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().testTag("services_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = currentSubTab.ordinal,
                modifier = Modifier.fillMaxWidth().testTag("services_subtabs_row"),
                edgePadding = 16.dp
            ) {
                ServicesSubTab.entries.forEach { tab ->
                    Tab(
                        selected = currentSubTab == tab,
                        onClick = { onSelectSubTab(tab) },
                        text = {
                            Text(
                                text = if (language == GaunLanguage.NEPALI) tab.titleNe else tab.titleEn,
                                fontWeight = if (currentSubTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.id}")
                    )
                }
            }

            when (currentSubTab) {
                ServicesSubTab.CIVIC_SERVICES -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = if (language == GaunLanguage.NEPALI)
                                    "नागरिक वडापत्र — सरकारी सेवा, आवश्यक कागजात तथा दस्तुर"
                                else
                                    "Citizen Charter — Services, Required Documents & Fees",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        items(communityServices) { srv ->
                            CommunityServiceCard(
                                service = srv,
                                language = language,
                                onClick = { onSelectService(srv) }
                            )
                        }
                    }
                }

                ServicesSubTab.NOTICES -> {
                    if (isNoticesLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(notices) { notice ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NoticeCard(
                                        notice = notice,
                                        language = language,
                                        onClick = { onSelectNotice(notice) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isAdmin) {
                                        IconButton(onClick = { onOpenEditNotice(notice) }) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Notice")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ServicesSubTab.HEALTH -> {
                    when (healthFacilitiesState) {
                        is HealthFacilitiesUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is HealthFacilitiesUiState.Success -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(healthFacilitiesState.facilities) { facility ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        HealthEmergencyCard(
                                            facility = facility,
                                            language = language,
                                            onCall = { onCallEmergency(facility.contactNumber, facility.nameNe) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isAdmin) {
                                            IconButton(onClick = { onOpenEditFacility(facility) }) {
                                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Facility")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is HealthFacilitiesUiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = healthFacilitiesState.messageNe, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                ServicesSubTab.EMERGENCY -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(emergencyContacts) { contact ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("contact_card_${contact.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (language == GaunLanguage.NEPALI) contact.nameNe else contact.nameEn,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (language == GaunLanguage.NEPALI) contact.designationNe else contact.designationEn,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = contact.contactNumber,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isAdmin) {
                                            IconButton(onClick = { onOpenEditEmergency(contact) }) {
                                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Contact")
                                            }
                                        }
                                        Button(
                                            onClick = { onCallEmergency(contact.contactNumber, contact.nameNe) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isAdmin) {
            FloatingActionButton(
                onClick = {
                    when (currentSubTab) {
                        ServicesSubTab.NOTICES -> onOpenCreateNotice()
                        ServicesSubTab.HEALTH -> onOpenCreateFacility()
                        ServicesSubTab.EMERGENCY -> onOpenCreateEmergency()
                        else -> onOpenCreateNotice()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("admin_fab_add")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    }
}
