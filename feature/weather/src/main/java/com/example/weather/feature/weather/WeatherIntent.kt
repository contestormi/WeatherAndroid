package com.example.weather.feature.weather

import com.example.weather.core.model.City

sealed interface WeatherIntent {
    data object LoadInitial : WeatherIntent
    data object LoadWeather : WeatherIntent
    data object Refresh : WeatherIntent
    data class SelectDay(val dayIndex: Int) : WeatherIntent
    data object OpenCitySearch : WeatherIntent
    data class CitySelected(val city: City) : WeatherIntent
    data object RequestLocation : WeatherIntent
    data object OpenHourlyForecast : WeatherIntent
}
