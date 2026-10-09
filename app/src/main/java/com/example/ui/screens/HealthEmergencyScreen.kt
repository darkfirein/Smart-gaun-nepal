package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AmbulanceServiceInfo
import com.example.model.BasicHealthGuideline
import com.example.model.EmergencyServiceNumber
import com.example.model.GaunLanguage
import com.example.model.GaunLocation
import com.example.model.HealthFacilitiesUiState
import com.example.model.HealthFacility
import com.example.ui.components.HealthEmergencyCard

@Composable
fun HealthEmergencyScreen(
    location: GaunLocation,
    emergencyNumbers: List<EmergencyServiceNumber>,
    ambulanceServices: List<AmbulanceServiceInfo>,
    healthFacilitiesState: HealthFacilitiesUiState,
    healthGuidelines: List<BasicHealthGuideline>,
    language: GaunLanguage,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onCall: (String, String) -> Unit,
    onOpenCreateFacility: () -> Unit,
    onOpenEditFacility: (HealthFacility) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("health_emergency_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("health_screen_back_button")) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = if (language == GaunLanguage.NEPALI) "स्वास्थ्य तथा आपतकालीन सेवा" else "Health & Emergency Services",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SOS Hotline Numbers
            item {
                Text(
                    text = if (language == GaunLanguage.NEPALI) "🚨 द्रुत आपतकालीन हटलाइन (२४ घण्टा निशुल्क)" else "🚨 Emergency SOS Hotlines (24/7 Toll-Free)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            items(emergencyNumbers) { item ->
                EmergencyActionCallCard(
                    title = if (language == GaunLanguage.NEPALI) item.titleNe else item.titleEn,
                    number = item.number,
                    description = if (language == GaunLanguage.NEPALI) item.descriptionNe else item.descriptionEn,
                    isUrgent = item.isUrgentSos,
                    onCall = { onCall(item.number, item.titleNe) }
                )
            }

            // Ambulance Services
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == GaunLanguage.NEPALI) "🚑 एम्बुलेन्स सेवा सम्पर्क" else "🚑 Local Ambulance Contacts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(ambulanceServices) { amb ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ambulance_card_${amb.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == GaunLanguage.NEPALI) amb.providerNameNe else amb.providerNameEn,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "केन्द्र: ${if (language == GaunLanguage.NEPALI) amb.baseStationNe else amb.baseStationEn}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = amb.contactNumber,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { onCall(amb.contactNumber, amb.providerNameNe) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "कल", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Health Facilities
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == GaunLanguage.NEPALI) "🏥 नजिकका स्वास्थ्य संस्था तथा अस्पताल" else "🏥 Nearby Health Facilities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (healthFacilitiesState is HealthFacilitiesUiState.Success) {
                items(healthFacilitiesState.facilities) { fac ->
                    HealthEmergencyCard(
                        facility = fac,
                        language = language,
                        onCall = { onCall(fac.contactNumber, fac.nameNe) }
                    )
                }
            }

            // First Aid Guidelines
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == GaunLanguage.NEPALI) "🩺 प्राथमिक उपचार तथा स्वास्थ्य सल्लाह" else "🩺 First Aid & Health Guidelines",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(healthGuidelines) { guide ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("guideline_card_${guide.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == GaunLanguage.NEPALI) guide.titleNe else guide.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == GaunLanguage.NEPALI) guide.summaryNe else guide.summaryEn,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val steps = if (language == GaunLanguage.NEPALI) guide.stepsNe else guide.stepsEn
                        steps.forEach { step ->
                            Text(text = step, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun EmergencyActionCallCard(
    title: String,
    number: String,
    description: String,
    isUrgent: Boolean,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onCall() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUrgent) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (description.isNotBlank()) {
                    Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = "डायल: $number",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Button(
                onClick = onCall,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = "Dial", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "कल", fontWeight = FontWeight.Bold)
            }
        }
    }
}
