package com.example.weather.feature.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.weather.core.model.DailyForecast
import com.example.weather.core.model.HourlyForecast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onNavigateToCitySearch: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onNavigateToHourlyForecast: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigation.collect { nav ->
            when (nav) {
                is WeatherNavigation.ToCitySearch -> onNavigateToCitySearch()
                is WeatherNavigation.RequestLocationPermission -> onRequestLocationPermission()
                is WeatherNavigation.ToHourlyForecast -> onNavigateToHourlyForecast()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.city?.name
                            ?: stringResource(com.example.weather.core.strings.R.string.weather),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.handleIntent(WeatherIntent.RequestLocation) }) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = stringResource(com.example.weather.core.strings.R.string.my_location)
                        )
                    }
                    IconButton(onClick = { viewModel.handleIntent(WeatherIntent.OpenCitySearch) }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(com.example.weather.core.strings.R.string.search_city)
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading && state.weatherData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null && state.weatherData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        state.error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(onClick = { viewModel.handleIntent(WeatherIntent.Refresh) }) {
                        Icon(
                            Icons.Default.Refresh,
                            stringResource(com.example.weather.core.strings.R.string.retry)
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                state.weatherData?.let { data ->
                    CurrentWeatherSection(current = data.current)
                    DailyForecastSection(
                        days = data.daily,
                        selectedIndex = state.selectedDayIndex,
                        onDayClick = {
                            viewModel.handleIntent(
                                WeatherIntent.SelectDay(
                                    it
                                )
                            )
                        },
                    )
                    AnimatedVisibility(visible = state.hourlyForSelectedDay.isNotEmpty()) {
                        Column {
                            HourlyPreviewSection(
                                hourly = state.hourlyForSelectedDay,
                                onShowAllClick = {
                                    viewModel.handleIntent(
                                        WeatherIntent.OpenHourlyForecast
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentWeatherSection(current: com.example.weather.core.model.CurrentWeather) {
    val weather = current.weather.firstOrNull()
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.5f
            ),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            weather?.let {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://openweathermap.org/img/wn/${it.icon}@4x.png")
                        .build(),
                    contentDescription = it.description,
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Text(
                text = "${current.temp.toInt()}°",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = stringResource(
                    com.example.weather.core.strings.R.string.feels_like,
                    current.feelsLike.toInt()
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            weather?.let {
                Text(
                    text = it.description.replaceFirstChar { c -> c.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                WeatherDetailItem(
                    stringResource(com.example.weather.core.strings.R.string.humidity),
                    "${current.humidity}%"
                )
                WeatherDetailItem(
                    stringResource(com.example.weather.core.strings.R.string.pressure),
                    "${current.pressure} ${stringResource(com.example.weather.core.strings.R.string.pressure_unit)}"
                )
                WeatherDetailItem(
                    stringResource(com.example.weather.core.strings.R.string.wind),
                    "${current.windSpeed.toInt()} ${stringResource(com.example.weather.core.strings.R.string.wind_speed_unit)}"
                )
            }
        }
    }
}

@Composable
private fun WeatherDetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun DailyForecastSection(
    days: List<DailyForecast>,
    selectedIndex: Int,
    onDayClick: (Int) -> Unit,
) {
    val dateFormat = SimpleDateFormat("EE, d MMM", Locale("ru"))
    Text(
        text = stringResource(com.example.weather.core.strings.R.string.weekly_forecast),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(16.dp, 8.dp),
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(days) { index, day ->
            val isSelected = index == selectedIndex
            Card(
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .clickable { onDayClick(index) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = dateFormat.format(Date(day.date * 1000)),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    day.weather.firstOrNull()?.let { weather ->
                        AsyncImage(
                            model = "https://openweathermap.org/img/wn/${weather.icon}.png",
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                    Text(
                        text = "${day.tempMin.toInt()}° / ${day.tempMax.toInt()}°",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun HourlyPreviewSection(
    hourly: List<HourlyForecast>,
    onShowAllClick: () -> Unit = {},
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(com.example.weather.core.strings.R.string.hourly_forecast),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(com.example.weather.core.strings.R.string.more_details),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onShowAllClick),
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(hourly.take(8)) { _, hour ->
                HourlyPreviewCard(hour = hour, timeFormat = timeFormat)
            }
        }
    }
}

@Composable
private fun HourlyPreviewCard(
    hour: HourlyForecast,
    timeFormat: SimpleDateFormat,
) {
    Card(
        modifier = Modifier.widthIn(min = 70.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = timeFormat.format(Date(hour.time * 1000)),
                style = MaterialTheme.typography.labelSmall,
            )
            hour.weather.firstOrNull()?.let { weather ->
                AsyncImage(
                    model = "https://openweathermap.org/img/wn/${weather.icon}.png",
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            }
            Text(
                text = "${hour.temp.toInt()}°",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
