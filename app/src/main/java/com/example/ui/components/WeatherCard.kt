package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GaunLanguage
import com.example.model.WeatherData
import com.example.model.WeatherUiState

@Composable
fun WeatherCard(
    weatherState: WeatherUiState,
    language: GaunLanguage,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == GaunLanguage.NEPALI) "स्थानीय मौसम तथा पूर्वानुमान" else "Local Weather & Forecast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("weather_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Weather",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            when (weatherState) {
                is WeatherUiState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (language == GaunLanguage.NEPALI) "मौसम जानकारी लोड हुँदै..." else "Fetching weather...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is WeatherUiState.Success -> {
                    WeatherContent(data = weatherState.data, language = language)
                }
                is WeatherUiState.Error -> {
                    if (weatherState.cachedData != null) {
                        WeatherContent(data = weatherState.cachedData, language = language)
                        Text(
                            text = if (language == GaunLanguage.NEPALI) "⚠️ अफलाइन डाटा (अद्यावधिक हुन सकेन)" else "⚠️ Offline data (could not refresh)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = if (language == GaunLanguage.NEPALI) weatherState.messageNe else weatherState.messageEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is WeatherUiState.Offline -> {
                    if (weatherState.cachedData != null) {
                        WeatherContent(data = weatherState.cachedData, language = language)
                    } else {
                        Text(
                            text = if (language == GaunLanguage.NEPALI) "इन्टरनेट छैन (अफलाइन मोड)" else "No internet connection",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is WeatherUiState.Idle -> {
                    Text(
                        text = if (language == GaunLanguage.NEPALI) "मौसम हेर्न रिफ्रेस गर्नुहोस्" else "Tap refresh for weather",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherContent(data: WeatherData, language: GaunLanguage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "${data.temperatureC.toInt()}°C",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            val desc = if (language == GaunLanguage.NEPALI) data.condition.descriptionNe else data.condition.descriptionEn
            Text(
                text = desc,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            data.feelsLikeC?.let {
                Text(
                    text = if (language == GaunLanguage.NEPALI) "अनुभूत: ${it.toInt()}°C" else "Feels like: ${it.toInt()}°C",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        val icon = getWeatherIcon(data.condition.iconKey)
        Icon(
            imageVector = icon,
            contentDescription = "Weather Icon",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        data.humidityPercent?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.WaterDrop, contentDescription = "Humidity", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$it%", style = MaterialTheme.typography.bodySmall)
            }
        }
        data.windSpeedKmh?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Air, contentDescription = "Wind", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${it.toInt()} km/h", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun getWeatherIcon(key: String): ImageVector {
    return when {
        key.contains("clear") || key.contains("sun") -> Icons.Default.WbSunny
        key.contains("rain") || key.contains("drizzle") -> Icons.Default.WaterDrop
        key.contains("cloud") -> Icons.Default.Cloud
        else -> Icons.Default.CloudQueue
    }
}
