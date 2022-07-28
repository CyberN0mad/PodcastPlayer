package com.kotlin.podplayer.di

import com.kotlin.podplayer.service.ItunesService
import com.kotlin.podplayer.service.RssFeedService
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val retrofit = createRetrofit()
val PODCAST_API: ItunesService = retrofit.create(ItunesService::class.java)
val RSS_SERVICE = getInstance()

val networkModule = module {
    single { PODCAST_API }
    single { RSS_SERVICE }
}

fun createRetrofit(): Retrofit {
      val retrofit = Retrofit.Builder()
          .baseUrl("https://itunes.apple.com")
          .addConverterFactory(GsonConverterFactory.create())
          .build()
    return  retrofit
    }

fun getInstance() = RssFeedService.instance