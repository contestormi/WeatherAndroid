package com.example.weather.core.database

import com.example.weather.core.database.entity.toCity
import com.example.weather.core.database.entity.toEntity
import com.example.weather.core.model.City
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CitiesRepository @Inject constructor(
    private val dao: CitiesDao,
) {

    suspend fun getSelectedCity(): City? = dao.getSelectedCity()?.toCity()

    suspend fun saveCity(city: City, setAsSelected: Boolean = false) {
        if (setAsSelected) {
            dao.clearSelection()
        }
        val order = dao.getCount()
        dao.insert(city.toEntity(isSelected = setAsSelected, order = order))
    }

    suspend fun selectCity(city: City) {
        dao.clearSelection()
        dao.insert(city.toEntity(isSelected = true, order = 0))
    }

    suspend fun getFirstCity(): City? = dao.getFirstCity()?.toCity()
}
