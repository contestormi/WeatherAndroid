package com.example.weather.feature.city

sealed class CityIntent {
    data class SearchQueryChanged(val query: String) : CityIntent()
    data class CitySelected(val cityId: String) : CityIntent()
    data object Dismiss : CityIntent()
}
