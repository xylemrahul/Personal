package com.example.movielist.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.movielist.R;
import com.example.movielist.main.AppController;
import com.example.movielist.main.MovieDetails;
import com.example.movielist.model.Movie;

public class CustomListAdapter extends BaseAdapter {
	private Activity activity;
	private LayoutInflater inflater;
	private List<Movie> movieItems;
	ImageLoader imageLoader = AppController.getInstance().getImageLoader();

	public CustomListAdapter(Activity activity, List<Movie> movieItems) {
		this.activity = activity;
		this.movieItems = movieItems;
	}

	@Override
	public int getCount() {
		return movieItems.size();
	}

	@Override
	public Object getItem(int location) {
		return movieItems.get(location);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (inflater == null)
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			convertView = inflater.inflate(R.layout.list_row, null);

		if (imageLoader == null)
			imageLoader = AppController.getInstance().getImageLoader();
		NetworkImageView thumbNail = (NetworkImageView) convertView
				.findViewById(R.id.thumbnail);
		TextView title = (TextView) convertView.findViewById(R.id.title);
		TextView adult = (TextView) convertView.findViewById(R.id.adult);
		TextView date = (TextView) convertView.findViewById(R.id.rel_date);

		// getting movie data for the row
		final Movie m = movieItems.get(position);

		// thumbnail image
		thumbNail.setImageUrl(
				"http://image.tmdb.org/t/p/w185" + m.getThumbnailUrl(),
				imageLoader);

		// title
		title.setText(m.getTitle());

		String adult1 = m.getAdult();
		if (adult1.equals("false")) {
			adult.setText("(U/A)");
		} else {
			adult.setText("(A)");
		}
		// release year
		date.setText(m.getRelDate());

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, MovieDetails.class);
				intent.putExtra("Id", m.getMovieId());
				activity.startActivity(intent);
			}
		});

		return convertView;
	}
}