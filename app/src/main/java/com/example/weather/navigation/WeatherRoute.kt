package com.example.weather.navigation

import kotlinx.serialization.Serializable

@Serializable
object WeatherRoute

@Serializable
object CitySearchRoute

@Serializable
data class HourlyRoute(val dayIndex: Int)
