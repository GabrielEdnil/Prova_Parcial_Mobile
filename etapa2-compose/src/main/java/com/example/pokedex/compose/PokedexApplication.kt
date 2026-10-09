package com.example.pokedex.compose

import android.app.Application
import com.example.pokedex.compose.di.AppContainer
import com.example.pokedex.compose.di.DefaultAppContainer

class PokedexApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
