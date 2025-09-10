package com.movieswipe

import android.app.Application
import com.movieswipe.di.AppContainer

class MovieSwipeApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
