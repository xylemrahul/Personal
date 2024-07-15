package com.shutterfly.interviewapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shutterfly.interviewapp.apiclient.ApiClient
import com.shutterfly.interviewapp.models.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieDetailsViewModel : ViewModel() {

    private val _movieDetails = MutableLiveData<Movie>()
    val movieDetails : LiveData<Movie> = _movieDetails

    fun refreshMovieDetails(movieId: Int) {
        viewModelScope.launch {
            fetchMovieFromServer(movieId)
        }
    }

    private suspend fun fetchMovieFromServer(movieId: Int) {
        val movie = withContext(Dispatchers.IO) {
            ApiClient.movieService()
                .getMovieDetails(movieId, "8db86f877df1b0947df8bceba159ed89")
        }

        if (movie != null) {
            withContext(Dispatchers.Main) {
                _movieDetails.value = movie
            }
        }
    }
}