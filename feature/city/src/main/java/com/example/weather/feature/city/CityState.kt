package com.example.weather.feature.city

import androidx.compose.runtime.Immutable
import com.example.weather.core.model.City
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class CityUiState(
    val searchQuery: String,
    val searchResultItems: ImmutableList<CityItemUi>,
    val isLoading: Boolean,
    val error: String?,
)

@Immutable
data class CityItemUi(
    val id: String,
    val name: String,
    val country: String,
)

internal data class CityState(
    val searchQuery: String = "",
    val searchResults: List<City> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    fun toUiState(): CityUiState = CityUiState(
        searchQuery = searchQuery,
        searchResultItems = searchResults.map { c ->
            CityItemUi(id = c.id, name = c.name, country = c.country)
        }.toImmutableList(),
        isLoading = isLoading,
        error = error,
    )
}
