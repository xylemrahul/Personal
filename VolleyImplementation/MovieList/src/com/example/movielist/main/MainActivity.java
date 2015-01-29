package com.example.movielist.main;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.movielist.R;
import com.example.movielist.adapter.CustomListAdapter;
import com.example.movielist.model.Movie;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends Activity {

	// Log tag
	private static final String TAG = MainActivity.class.getSimpleName();

	// Movies json url
	private static final String url = "https://api.themoviedb.org/3/movie/upcoming?";
	private static final String key = "b7cd3340a794e5a2f35e3abb820b497f";
	private ProgressDialog pDialog;
	private ListView listView;
	private CustomListAdapter adapter;
	private List<Movie> movieList = new ArrayList<Movie>();
	private ConnectionDetector cd;
	private AlertDialog alertDialog;
	private String request_url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();

		ActionBar action = getActionBar();
		action.setTitle("Upcoming Movies");
		action.setBackgroundDrawable(new ColorDrawable(Color
				.parseColor("#333333")));

		pDialog = new ProgressDialog(this);
		// Showing progress dialog before making http request
		pDialog.setMessage("Loading...");
		pDialog.show();
		pDialog.setCanceledOnTouchOutside(false);

		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present

			alertDialog.setTitle("Internet Connection Error");
			alertDialog
					.setMessage("Please connect to working Internet connection");
			alertDialog.show();
		} else {
			request_url = String.format(url + "api_key=%s", key);
			makeJsonObjectRequest();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			startActivity(new Intent(getApplicationContext(), Info.class));
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	void initViews() {
		listView = (ListView) findViewById(R.id.list);
		adapter = new CustomListAdapter(this, movieList);
		listView.setAdapter(adapter);
	}

	private void makeJsonObjectRequest() {

		showpDialog();

		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET,
				request_url, null, new Response.Listener<JSONObject>() {

					String poster, name, rel_date, adult, movie_id;

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, response.toString());

						try {
							JSONArray jArray = response.getJSONArray("results");

							for (int i = 0; i < jArray.length(); i++) {
								JSONObject jObj = jArray.getJSONObject(i);
								poster = jObj.getString("poster_path");
								name = jObj.getString("title");
								rel_date = jObj.getString("release_date");
								adult = jObj.getString("adult");
								movie_id = jObj.getString("id");

								Movie movie = new Movie();
								movie.setAdult(adult);
								movie.setRelDate(rel_date);
								movie.setThumbnailUrl(poster);
								movie.setTitle(name);
								movie.setMovieId(movie_id);

								movieList.add(movie);
							}
						} catch (JSONException e) {

							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						hidepDialog();
						// notifying list adapter about data changes
						// so that it renders the list view with updated data
						adapter.notifyDataSetChanged();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.d(TAG, "Error: " + error.getMessage());
						// Toast.makeText(getApplicationContext(),
						// error.getMessage(), Toast.LENGTH_SHORT).show();
						// hide the progress dialog
						hidepDialog();
					}
				});
		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(jsonObjReq);
	}

	private void showpDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
		pDialog.setCanceledOnTouchOutside(false);
	}

	private void hidepDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		hidepDialog();
	}
}