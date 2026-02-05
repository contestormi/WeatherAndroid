package com.example.weather.feature.city

import com.example.weather.core.model.City

data class CityState(
    val searchQuery: String = "",
    val searchResults: List<City> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
