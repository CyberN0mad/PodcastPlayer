package com.kotlin.podplayer.di

import com.kotlin.podplayer.repository.ItunesRepo
import com.kotlin.podplayer.repository.PodcastRepo
import org.koin.dsl.module

val repositoryModule = module {
    single { PodcastRepo(get(), get()) }
    single { ItunesRepo(get()) }
}