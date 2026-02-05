package com.example.weather.feature.weather

import com.example.weather.core.model.City
import com.example.weather.core.model.DailyForecast
import com.example.weather.core.model.WeatherData

data class WeatherState(
    val city: City? = null,
    val weatherData: WeatherData? = null,
    val selectedDayIndex: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
) {
    val selectedDay: DailyForecast?
        get() = weatherData?.daily?.getOrNull(selectedDayIndex)

    val hourlyForSelectedDay: List<com.example.weather.core.model.HourlyForecast>
        get() {
            val day = selectedDay ?: return emptyList()
            val dayStart = (day.date / 86400) * 86400
            val dayEnd = dayStart + 86400
            return weatherData?.hourly?.filter { it.time in dayStart until dayEnd } ?: emptyList()
        }
}
