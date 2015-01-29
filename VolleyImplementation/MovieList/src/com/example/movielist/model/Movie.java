package com.example.movielist.model;

public class Movie {
	private String title, thumbnailUrl,adult,date,id;

	public Movie() {
	}

	public Movie(String title, String thumbnailUrl, String date, String adult,
			String id) {
		this.title = title;
		this.thumbnailUrl = thumbnailUrl;
		this.date = date;
		this.adult = adult;
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String name) {
		this.title = name;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getRelDate() {
		return date;
	}

	public void setRelDate(String date) {
		this.date = date;
	}

	public String getAdult() {
		return adult;
	}

	public void setAdult(String adult) {
		this.adult = adult;
	}

	public String getMovieId(){
		return id;
	}
	
	public void setMovieId(String movieid){
		this.id = movieid;
	}
}