package com.example.weather.feature.forecast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.weather.core.model.HourlyForecast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourlyForecastScreen(
    dayTitle: String,
    hourlyForecasts: List<HourlyForecast>,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dayTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(com.example.weather.core.strings.R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(hourlyForecasts) { hour ->
                HourlyForecastItem(hour = hour)
            }
        }
    }
}

@Composable
private fun HourlyForecastItem(hour: HourlyForecast) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val weather = hour.weather.firstOrNull()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = timeFormat.format(Date(hour.time * 1000)),
                    style = MaterialTheme.typography.titleMedium,
                )
                weather?.let {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://openweathermap.org/img/wn/${it.icon}@2x.png")
                            .build(),
                        contentDescription = it.description,
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${hour.temp.toInt()}°",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(com.example.weather.core.strings.R.string.feels_like, hour.feelsLike.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            weather?.let {
                Text(
                    text = it.description.replaceFirstChar { c -> c.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
