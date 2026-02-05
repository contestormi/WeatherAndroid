package com.example.weather.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weather.core.model.City

@Entity(tableName = "saved_cities")
data class SavedCityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val isSelected: Boolean,
    val order: Int,
)

fun SavedCityEntity.toCity() = City(
    id = id,
    name = name,
    country = country,
    lat = lat,
    lon = lon,
)

fun City.toEntity(isSelected: Boolean = false, order: Int = 0) = SavedCityEntity(
    id = id,
    name = name,
    country = country,
    lat = lat,
    lon = lon,
    isSelected = isSelected,
    order = order,
)
