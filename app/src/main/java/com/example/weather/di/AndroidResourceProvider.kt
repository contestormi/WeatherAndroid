package com.example.weather.di

import android.content.Context
import com.example.weather.core.strings.ResourceProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidResourceProvider @Inject constructor(
    private val context: Context,
) : ResourceProvider {

    override fun getString(resId: Int): String =
        context.getString(resId)

    override fun getString(resId: Int, vararg formatArgs: Any): String =
        context.getString(resId, *formatArgs)
}
