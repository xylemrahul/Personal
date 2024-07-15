package com.shutterfly.interviewapp.service

import com.shutterfly.interviewapp.models.Movie
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieService {
    @GET("movie/{movieId}")
    suspend fun getMovieDetails(@Path("movieId") movieId: Int, @Query("api_key") apiKey: String): Movie?
}