package com.kotlin.podplayer.di

import com.kotlin.podplayer.viewmodel.PodcastViewModel
import com.kotlin.podplayer.viewmodel.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

var viewModelModule = module {
    viewModel{ SearchViewModel(get()) }
    viewModel{ PodcastViewModel(get()) }
}