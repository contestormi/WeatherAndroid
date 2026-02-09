package com.example.weather.core.strings

interface ResourceProvider {

    fun getString(resId: Int): String

    fun getString(resId: Int, vararg formatArgs: Any): String
}
