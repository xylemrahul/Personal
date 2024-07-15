package com.shutterfly.interviewapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.shutterfly.interviewapp.apiclient.ApiClient
import com.shutterfly.interviewapp.models.Movie
import com.shutterfly.interviewapp.viewmodel.MovieDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class MovieDetailsActivity : AppCompatActivity() {
	private val movieId = 1891
	//For importing viewmodels in activity without jetpack compose
	private val movieViewModel by viewModels<MovieDetailsViewModel>()

	private val movieObserver = Observer<Movie> {
		movie -> updateUI(movie)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_movie_details)
	}

	override fun onResume() {
		super.onResume()
		movieViewModel.refreshMovieDetails(movieId)
		movieViewModel.movieDetails.observe(this, movieObserver)
	}

	private fun updateUI(movie: Movie) {
		val titleTextView = findViewById<TextView>(R.id.movie_title)
		titleTextView.text = movie.title
	}
}
