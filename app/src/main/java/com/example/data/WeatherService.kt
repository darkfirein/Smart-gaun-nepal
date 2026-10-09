package com.example.data

import com.example.model.GaunLocation
import com.example.model.WeatherData

interface WeatherService {
    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        location: GaunLocation
    ): Result<WeatherData>
}
