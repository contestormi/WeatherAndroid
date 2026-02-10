package com.example.weather.feature.weather

import androidx.compose.runtime.Immutable
import com.example.weather.core.model.City
import com.example.weather.core.model.WeatherData

@Immutable
data class WeatherScreenUiState(
    val cityName: String?,
    val isLoading: Boolean,
    val isRefreshing: Boolean = false,
    val hasWeatherData: Boolean,
    val error: String?,
    val selectedDayIndex: Int,
)

internal data class WeatherScreenBackingState(
    val city: City? = null,
    val weatherData: WeatherData? = null,
    val selectedDayIndex: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
) {
    val hourlyForSelectedDay: List<com.example.weather.core.model.HourlyForecast>
        get() = hourlyForDayIndex(selectedDayIndex)

    fun hourlyForDayIndex(dayIndex: Int): List<com.example.weather.core.model.HourlyForecast> {
        val day = weatherData?.daily?.getOrNull(dayIndex) ?: return emptyList()
        val dayStart =
            (day.date / WeatherConstants.SECONDS_PER_DAY) * WeatherConstants.SECONDS_PER_DAY
        val dayEnd = dayStart + WeatherConstants.SECONDS_PER_DAY
        return weatherData.hourly.filter { it.time in dayStart until dayEnd }
    }
}
