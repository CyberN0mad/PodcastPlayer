package com.kotlin.podplayer.repository

import com.kotlin.podplayer.service.ItunesService

class ItunesRepo(private val itunesService: ItunesService) {

  suspend fun searchByTerm(term: String) = itunesService.searchPodcastByTerm(term)

}
