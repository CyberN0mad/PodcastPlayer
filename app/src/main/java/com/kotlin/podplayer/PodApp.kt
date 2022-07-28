package com.kotlin.podplayer

import android.app.Application
import com.kotlin.podplayer.di.dataBaseModule
import com.kotlin.podplayer.di.networkModule
import com.kotlin.podplayer.di.repositoryModule
import com.kotlin.podplayer.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PodApp: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@PodApp)
            modules(provideModules())
        }
    }

    private fun provideModules() = listOf(
            viewModelModule,
            dataBaseModule,
            networkModule,
            repositoryModule
    )
}