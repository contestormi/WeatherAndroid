package com.example.weather.feature.city

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.example.weather.core.database.CitiesRepository
import com.example.weather.core.model.City
import com.example.weather.core.network.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CityViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val citiesRepository: CitiesRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(CityState())
    val state: StateFlow<CityState> = _state.asStateFlow()

    private val _navigation = MutableSharedFlow<CityNavigation>()
    val navigation: SharedFlow<CityNavigation> = _navigation.asSharedFlow()

    private var searchJob: Job? = null

    fun handleIntent(intent: CityIntent) {
        when (intent) {
            is CityIntent.SearchQueryChanged -> onSearchQueryChanged(intent.query)
            is CityIntent.CitySelected -> onCitySelected(intent.city)
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
                            error = e.message ?: context.getString(com.example.weather.core.strings.R.string.error_search),
                        )
                    }
                },
            )
        }
    }

    private fun onCitySelected(city: City) {
        viewModelScope.launch {
            citiesRepository.saveCity(city, setAsSelected = true)
            _navigation.emit(CityNavigation.CitySelected(city))
        }
    }
}

sealed class CityNavigation {
    data class CitySelected(val city: City) : CityNavigation()
    data object Dismiss : CityNavigation()
}
