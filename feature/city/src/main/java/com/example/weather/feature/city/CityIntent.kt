package com.example.weather.feature.city

import com.example.weather.core.model.City

sealed class CityIntent {
    data class SearchQueryChanged(val query: String) : CityIntent()
    data class CitySelected(val city: City) : CityIntent()
    data object Dismiss : CityIntent()
}
