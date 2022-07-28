package com.kotlin.podplayer.di

import com.kotlin.podplayer.db.PodPlayDatabase
import org.koin.dsl.module

val dataBaseModule = module {
    single { PodPlayDatabase.getInstance(get()) }
    single { get<PodPlayDatabase>().podcastDao() }
}