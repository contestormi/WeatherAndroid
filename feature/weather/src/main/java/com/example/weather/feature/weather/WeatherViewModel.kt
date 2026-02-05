package com.example.weather.feature.weather

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.core.database.CitiesRepository
import com.example.weather.core.location.LocationProvider
import com.example.weather.core.model.City
import com.example.weather.core.network.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val citiesRepository: CitiesRepository,
    private val locationProvider: LocationProvider,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherState())
    val state: StateFlow<WeatherState> = _state.asStateFlow()

    private val _navigation = MutableSharedFlow<WeatherNavigation>()
    val navigation: SharedFlow<WeatherNavigation> = _navigation.asSharedFlow()

    fun handleIntent(intent: WeatherIntent) {
        when (intent) {
            is WeatherIntent.LoadInitial -> loadInitialWeather()
            is WeatherIntent.LoadWeather -> loadWeather()
            is WeatherIntent.Refresh -> refresh()
            is WeatherIntent.SelectDay -> _state.update { it.copy(selectedDayIndex = intent.dayIndex) }
            is WeatherIntent.OpenCitySearch -> viewModelScope.launch {
                _navigation.emit(WeatherNavigation.ToCitySearch)
            }
            is WeatherIntent.CitySelected -> onCitySelected(intent.city)
            is WeatherIntent.RequestLocation -> requestLocationAndLoad()
            is WeatherIntent.OpenHourlyForecast -> viewModelScope.launch {
                _navigation.emit(WeatherNavigation.ToHourlyForecast)
            }
        }
    }

    private fun loadInitialWeather() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val selectedCity = citiesRepository.getSelectedCity()
            if (selectedCity != null) {
                loadWeatherForCity(selectedCity)
            } else {
                val firstCity = citiesRepository.getFirstCity()
                if (firstCity != null) {
                    citiesRepository.selectCity(firstCity)
                    loadWeatherForCity(firstCity)
                } else {
                    requestLocationAndLoad()
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun loadWeather() {
        viewModelScope.launch {
            val city = _state.value.city ?: return@launch
            loadWeatherForCity(city)
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            loadWeather()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun requestLocationAndLoad() {
        viewModelScope.launch {
            if (!locationProvider.hasLocationPermission()) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = context.getString(com.example.weather.core.strings.R.string.error_location_permission_required),
                    )
                }
                _navigation.emit(WeatherNavigation.RequestLocationPermission)
                return@launch
            }
            val location = locationProvider.getCurrentLocation()
            if (location != null) {
                weatherRepository.getCityFromCoordinates(location.latitude, location.longitude)
                    .onSuccess { city ->
                        if (city != null) {
                            citiesRepository.saveCity(city, setAsSelected = true)
                            loadWeatherForCity(city)
                        } else {
                            loadWeatherByCoords(location.latitude, location.longitude)
                        }
                    }
                    .onFailure {
                        loadWeatherByCoords(location.latitude, location.longitude)
                    }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = context.getString(com.example.weather.core.strings.R.string.error_location_failed),
                    )
                }
            }
        }
    }

    private fun loadWeatherByCoords(lat: Double, lon: Double) {
        viewModelScope.launch {
            weatherRepository.getWeather(lat, lon).fold(
                onSuccess = { data ->
                    _state.update {
                        it.copy(
                            city = City(
                                id = "${lat}_${lon}",
                                name = context.getString(com.example.weather.core.strings.R.string.current_location),
                                country = "",
                                lat = lat,
                                lon = lon,
                            ),
                            weatherData = data,
                            error = null,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(error = e.message ?: context.getString(com.example.weather.core.strings.R.string.error_loading))
                    }
                },
            )
        }
    }

    private fun loadWeatherForCity(city: City) {
        viewModelScope.launch {
            weatherRepository.getWeather(city.lat, city.lon).fold(
                onSuccess = { data ->
                    _state.update {
                        it.copy(
                            city = city,
                            weatherData = data,
                            error = null,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(error = e.message ?: context.getString(com.example.weather.core.strings.R.string.error_loading))
                    }
                },
            )
        }
    }

    private fun onCitySelected(city: City) {
        viewModelScope.launch {
            citiesRepository.selectCity(city)
            loadWeatherForCity(city)
        }
    }
}

sealed class WeatherNavigation {
    data object ToCitySearch : WeatherNavigation()
    data object RequestLocationPermission : WeatherNavigation()
    data object ToHourlyForecast : WeatherNavigation()
}
