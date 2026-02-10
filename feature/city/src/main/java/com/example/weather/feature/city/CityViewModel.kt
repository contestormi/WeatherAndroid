package com.example.weather.feature.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.core.database.CitiesRepository
import com.example.weather.core.model.City
import com.example.weather.core.network.WeatherRepository
import com.example.weather.core.strings.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CityViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val citiesRepository: CitiesRepository,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(CityState())
    val state: StateFlow<CityUiState> = _state
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = _state.value.toUiState(),
        )

    private val _navigation = MutableSharedFlow<CityNavigation>()
    val navigation: SharedFlow<CityNavigation> = _navigation.asSharedFlow()

    private var searchJob: Job? = null

    fun handleIntent(intent: CityIntent) {
        when (intent) {
            is CityIntent.SearchQueryChanged -> onSearchQueryChanged(intent.query)
            is CityIntent.CitySelected -> onCitySelected(intent.cityId)
            is CityIntent.Dismiss -> viewModelScope.launch {
                _navigation.emit(CityNavigation.Dismiss)
            }
        }
    }

    private fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query, error = null) }
        searchJob?.cancel()
        if (query.length < 2) {
            _state.update { it.copy(searchResults = emptyList()) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _state.update { it.copy(isLoading = true) }
            weatherRepository.searchCities(query).fold(
                onSuccess = { cities ->
                    _state.update {
                        it.copy(
                            searchResults = cities,
                            isLoading = false,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { state ->
                        state.copy(
                            searchResults = emptyList(),
                            isLoading = false,
                            error = e.message ?: resourceProvider.getString(com.example.weather.core.strings.R.string.error_search),
                        )
                    }
                },
            )
        }
    }

    private fun onCitySelected(cityId: String) {
        viewModelScope.launch {
            val city = _state.value.searchResults.find { it.id == cityId } ?: return@launch
            citiesRepository.saveCity(city, setAsSelected = true)
            _navigation.emit(CityNavigation.CitySelected(city))
        }
    }
}

sealed class CityNavigation {
    data class CitySelected(val city: City) : CityNavigation()
    data object Dismiss : CityNavigation()
}
