package com.example.weather.feature.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.core.database.CitiesRepository
import com.example.weather.core.location.LocationProvider
import com.example.weather.core.model.City
import com.example.weather.core.model.HourlyForecast
import com.example.weather.core.network.WeatherRepository
import com.example.weather.core.strings.R
import com.example.weather.core.strings.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@HiltViewModel
class WeatherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val weatherRepository: WeatherRepository,
    private val citiesRepository: CitiesRepository,
    private val locationProvider: LocationProvider,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _backingState = MutableStateFlow(WeatherScreenBackingState())
    val state: StateFlow<WeatherScreenUiState> = _backingState
        .map { toWeatherScreenUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = toWeatherScreenUiState(_backingState.value),
        )

    private fun toWeatherScreenUiState(s: WeatherScreenBackingState): WeatherScreenUiState =
        WeatherScreenUiState(
            cityName = s.city?.name,
            isLoading = s.isLoading,
            isRefreshing = s.isRefreshing,
            hasWeatherData = s.weatherData != null,
            error = s.error,
            selectedDayIndex = s.selectedDayIndex,
        )

    private val _navigation = MutableSharedFlow<WeatherNavigation>()
    val navigation: SharedFlow<WeatherNavigation> = _navigation.asSharedFlow()

    val currentWeatherUi: StateFlow<CurrentWeatherUi?> =
        _backingState.map { buildCurrentWeatherUi(it) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildCurrentWeatherUi(_backingState.value),
        )

    private fun buildCurrentWeatherUi(backing: WeatherScreenBackingState): CurrentWeatherUi? {
        val current = backing.weatherData?.current ?: return null
        val description =
            current.weather.firstOrNull()?.description?.replaceFirstChar { c -> c.uppercase() }
        val iconUrl = current.weather.firstOrNull()?.icon?.let { icon ->
            "${WeatherConstants.WEATHER_ICON_BASE_URL}/$icon${WeatherConstants.WEATHER_ICON_SUFFIX_4X}"
        }
        return CurrentWeatherUi(
            tempC = current.temp.toInt(),
            feelsLikeC = current.feelsLike.toInt(),
            description = description,
            humidity = current.humidity,
            pressure = current.pressure,
            windSpeedKph = current.windSpeed.toInt(),
            iconUrl = iconUrl,
        )
    }

    val dailyForecastItems: StateFlow<ImmutableList<DailyForecastItem>> =
        _backingState.map { buildDailyForecastItems(it) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildDailyForecastItems(_backingState.value),
        )

    private fun buildDailyForecastItems(backing: WeatherScreenBackingState): ImmutableList<DailyForecastItem> {
        val dateFormat = SimpleDateFormat(
            WeatherConstants.DAY_CARD_DATE_PATTERN,
            Locale(WeatherConstants.DAY_CARD_LOCALE)
        )
        return (backing.weatherData?.daily?.mapIndexed { index, day ->
            DailyForecastItem(
                dateText = dateFormat.format(Date(day.date * WeatherConstants.MILLIS_PER_SECOND)),
                tempRangeText = "${day.tempMin.toInt()}° / ${day.tempMax.toInt()}°",
                iconUrl = day.weather.firstOrNull()?.icon?.let { "${WeatherConstants.WEATHER_ICON_BASE_URL}/$it${WeatherConstants.WEATHER_ICON_SUFFIX}" },
            )
        } ?: emptyList()).toImmutableList()
    }

    val hourlyPreviewItems: StateFlow<ImmutableList<HourlyPreviewItem>> =
        _backingState.map { buildHourlyPreviewItems(it) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildHourlyPreviewItems(_backingState.value),
        )

    private fun buildHourlyPreviewItems(backing: WeatherScreenBackingState): ImmutableList<HourlyPreviewItem> {
        val timeFormat = SimpleDateFormat(
            WeatherConstants.HOURLY_TIME_PATTERN,
            Locale.getDefault()
        )
        return backing.hourlyForSelectedDay.map { hour ->
            HourlyPreviewItem(
                timeText = timeFormat.format(Date(hour.time * WeatherConstants.MILLIS_PER_SECOND)),
                tempText = "${hour.temp.toInt()}°",
                iconUrl = hour.weather.firstOrNull()?.icon?.let { "${WeatherConstants.WEATHER_ICON_BASE_URL}/$it${WeatherConstants.WEATHER_ICON_SUFFIX}" },
            )
        }.toImmutableList()
    }

    fun hourlyScreenState(dayIndex: Int): StateFlow<HourlyScreenUiState> =
        _backingState.map { buildHourlyScreenUiState(it, dayIndex) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildHourlyScreenUiState(
                _backingState.value,
                dayIndex
            ),
        )

    private fun buildHourlyScreenUiState(
        backing: WeatherScreenBackingState,
        dayIndex: Int
    ): HourlyScreenUiState {
        val day = backing.weatherData?.daily?.getOrNull(dayIndex)
        val dayTitle = day?.let {
            SimpleDateFormat(
                WeatherConstants.DAY_TITLE_DATE_PATTERN,
                Locale(WeatherConstants.DAY_TITLE_LOCALE),
            ).format(Date(it.date * WeatherConstants.MILLIS_PER_SECOND))
        }
            ?: resourceProvider.getString(com.example.weather.core.strings.R.string.forecast)
        val timeFormat = SimpleDateFormat(
            WeatherConstants.HOURLY_TIME_PATTERN,
            Locale.getDefault()
        )
        val items = backing.hourlyForDayIndex(dayIndex).map { hour ->
            HourlyForecastItemUi(
                timeText = timeFormat.format(Date(hour.time * WeatherConstants.MILLIS_PER_SECOND)),
                tempText = "${hour.temp.toInt()}°",
                feelsLikeText = resourceProvider.getString(
                    com.example.weather.core.strings.R.string.feels_like,
                    hour.feelsLike.toInt()
                ),
                description = hour.weather.firstOrNull()?.description?.replaceFirstChar { c -> c.uppercase() },
                iconUrl = hour.weather.firstOrNull()?.icon?.let { "${WeatherConstants.WEATHER_ICON_BASE_URL}/$it${WeatherConstants.WEATHER_ICON_SUFFIX_2X}" },
            )
        }.toImmutableList()
        return HourlyScreenUiState(dayTitle = dayTitle, items = items)
    }

    fun handleIntent(intent: WeatherIntent) {
        when (intent) {
            is WeatherIntent.LoadInitial -> loadInitialWeather()
            is WeatherIntent.LoadWeather -> loadWeather()
            is WeatherIntent.Refresh -> refresh()
            is WeatherIntent.SelectDay -> _backingState.update {
                it.copy(
                    selectedDayIndex = intent.dayIndex
                )
            }

            is WeatherIntent.OpenCitySearch -> viewModelScope.launch {
                _navigation.emit(WeatherNavigation.ToCitySearch)
            }

            is WeatherIntent.CitySelected -> onCitySelected(intent.city)
            is WeatherIntent.RequestLocation -> requestLocationAndLoad()
            is WeatherIntent.OpenHourlyForecast -> viewModelScope.launch {
                _navigation.emit(
                    WeatherNavigation.ToHourlyForecast(
                        _backingState.value.selectedDayIndex
                    )
                )
            }
        }
    }

    private fun loadInitialWeather() {
        viewModelScope.launch {
            _backingState.update { it.copy(isLoading = true, error = null) }
            suspendDoLoadInitialWeather()
            _backingState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun suspendDoLoadInitialWeather() {
        val selectedCity = citiesRepository.getSelectedCity()
        if (selectedCity != null) {
            suspendLoadWeatherForCity(selectedCity)
        } else {
            val firstCity = citiesRepository.getFirstCity()
            if (firstCity != null) {
                citiesRepository.selectCity(firstCity)
                suspendLoadWeatherForCity(firstCity)
            } else {
                suspendRequestLocationAndLoad()
            }
        }
    }

    private fun loadWeather() {
        viewModelScope.launch {
            val city = _backingState.value.city ?: return@launch
            loadWeatherForCity(city)
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _backingState.update { it.copy(isRefreshing = true, error = null) }
            val city = _backingState.value.city
            if (city != null) {
                suspendLoadWeatherForCity(city)
            } else {
                suspendDoLoadInitialWeather()
            }
            _backingState.update { it.copy(isRefreshing = false) }
        }
    }

    private suspend fun suspendLoadWeatherForCity(city: City) {
        weatherRepository.getWeather(city.lat, city.lon).fold(
            onSuccess = { data ->
                _backingState.update {
                    it.copy(
                        city = city,
                        weatherData = data,
                        error = null,
                    )
                }
            },
            onFailure = { e ->
                _backingState.update {
                    it.copy(
                        error = e.message ?: resourceProvider.getString(
                            com.example.weather.core.strings.R.string.error_loading
                        )
                    )
                }
            },
        )
    }

    private fun requestLocationAndLoad() {
        viewModelScope.launch {
            suspendRequestLocationAndLoad()
        }
    }

    private suspend fun suspendRequestLocationAndLoad() {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            _backingState.update {
                it.copy(
                    isLoading = false,
                    error = resourceProvider.getString(com.example.weather.core.strings.R.string.error_location_permission_required),
                )
            }
            _navigation.emit(WeatherNavigation.RequestLocationPermission)
            return
        }
        val location = try {
            locationProvider.getCurrentLocation()
        } catch (_: SecurityException) {
            _backingState.update {
                it.copy(
                    isLoading = false,
                    error = resourceProvider.getString(com.example.weather.core.strings.R.string.error_location_permission_required),
                )
            }
            _navigation.emit(WeatherNavigation.RequestLocationPermission)
            return
        }
        if (location != null) {
            weatherRepository.getCityFromCoordinates(
                location.latitude.value,
                location.longitude.value
            )
                .onSuccess { city ->
                    if (city != null) {
                        citiesRepository.saveCity(
                            city,
                            setAsSelected = true
                        )
                        suspendLoadWeatherForCity(city)
                    } else {
                        suspendLoadWeatherByCoords(
                            location.latitude.value,
                            location.longitude.value
                        )
                    }
                }
                .onFailure {
                    suspendLoadWeatherByCoords(
                        location.latitude.value,
                        location.longitude.value
                    )
                }
        } else {
            _backingState.update {
                it.copy(
                    isLoading = false,
                    error = resourceProvider.getString(com.example.weather.core.strings.R.string.error_location_failed),
                )
            }
        }
    }

    private fun loadWeatherByCoords(lat: Double, lon: Double) {
        viewModelScope.launch {
            suspendLoadWeatherByCoords(lat, lon)
        }
    }

    private suspend fun suspendLoadWeatherByCoords(lat: Double, lon: Double) {
        weatherRepository.getWeather(lat, lon).fold(
            onSuccess = { data ->
                _backingState.update {
                    it.copy(
                        city = City(
                            id = "${lat}_${lon}",
                            name = resourceProvider.getString(com.example.weather.core.strings.R.string.current_location),
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
                _backingState.update {
                    it.copy(
                        error = e.message ?: resourceProvider.getString(
                            com.example.weather.core.strings.R.string.error_loading
                        )
                    )
                }
            },
        )
    }

    private fun loadWeatherForCity(city: City) {
        viewModelScope.launch {
            suspendLoadWeatherForCity(city)
        }
    }

    private fun onCitySelected(city: City) {
        viewModelScope.launch {
            citiesRepository.selectCity(city)
            loadWeatherForCity(city)
        }
    }
}

@Immutable
data class CurrentWeatherUi(
    val tempC: Int,
    val feelsLikeC: Int,
    val description: String?,
    val humidity: Int,
    val pressure: Int,
    val windSpeedKph: Int,
    val iconUrl: String?,
)

@Immutable
data class DailyForecastItem(
    val dateText: String,
    val tempRangeText: String,
    val iconUrl: String?,
)

@Immutable
data class HourlyPreviewItem(
    val timeText: String,
    val tempText: String,
    val iconUrl: String?,
)

@Immutable
data class HourlyForecastItemUi(
    val timeText: String,
    val tempText: String,
    val feelsLikeText: String,
    val description: String?,
    val iconUrl: String?,
)

@Immutable
data class HourlyScreenUiState(
    val dayTitle: String,
    val items: ImmutableList<HourlyForecastItemUi>,
)

sealed interface WeatherNavigation {
    data object ToCitySearch : WeatherNavigation
    data object RequestLocationPermission : WeatherNavigation
    data class ToHourlyForecast(val dayIndex: Int) : WeatherNavigation
}
