package com.example.movielist.main;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request.Priority;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.movielist.R;
import com.example.movielist.adapter.ImagePosterAdapter;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MovieDetails extends FragmentActivity {

	ImagePosterAdapter mAdapter;
	ViewPager mPager;
	PageIndicator mIndicator;

	private AlertDialog alertDialog;
	private ConnectionDetector cd;
	private String request_url, image_url;
	private ProgressDialog pDialog;
	private TextView tv_title, tv_desc, tv_rating;
	private ArrayList<String> imageList = new ArrayList<String>();
	ActionBar action;
	
	private static final String url = "https://api.themoviedb.org/3/movie/";
	private static final String i_url = "https://api.themoviedb.org/3/movie/";
	private static final String key = "b7cd3340a794e5a2f35e3abb820b497f";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		String id = getIntent().getExtras().getString("Id");

		initViews();
		
		action = getActionBar();
		
		action.setBackgroundDrawable(new ColorDrawable(Color
				.parseColor("#1b1b1b")));

		mAdapter = new ImagePosterAdapter(
				getSupportFragmentManager(), imageList);
		mPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mPager);
		
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
			request_url = String.format(url + id + "?" + "api_key=%s", key);
			image_url = String.format(i_url + id + "/images?" + "api_key=%s",
					key);
			makeJsonObjectRequest();
			makeJsonImageRequest();
		}
	}

	private void initViews() {

		tv_title = (TextView) findViewById(R.id.title);
		tv_desc = (TextView) findViewById(R.id.desc);
		tv_rating = (TextView) findViewById(R.id.rating);
		mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		mPager = (ViewPager) findViewById(R.id.pager);
	}

	private void makeJsonObjectRequest() {

		showpDialog();

		final Priority priority = Priority.NORMAL;
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET,
				request_url, null, new Response.Listener<JSONObject>() {

					String overview, popularity, title;

					@Override
					public void onResponse(JSONObject response) {
						Log.d("", response.toString());

						try {
							overview = response.getString("overview");
							popularity = response.getString("popularity");
							title = response.getString("title");

							tv_title.setText(title);
							tv_desc.setText(overview);
							action.setTitle(title);

						} catch (JSONException e) {

							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						hidepDialog();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// Toast.makeText(getApplicationContext(),
						// error.getMessage(), Toast.LENGTH_SHORT).show();
						// hide the progress dialog
						hidepDialog();
					}
				}) {
			@Override
			public com.android.volley.Request.Priority getPriority() {
				// TODO Auto-generated method stub
				return priority;
			}
		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(jsonObjReq);
	}

	private void makeJsonImageRequest() {

		showpDialog();

		final Priority priority = Priority.HIGH;
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET,
				image_url, null, new Response.Listener<JSONObject>() {

					String image;

					@Override
					public void onResponse(JSONObject response) {
						Log.d("", response.toString());

						try {
							JSONArray jArray = response.getJSONArray("posters");

							for (int i = 0; i < jArray.length(); i++) {
								JSONObject jObj = jArray.getJSONObject(i);
								image = jObj.getString("file_path");
								imageList.add(image);
							}
							Log.e("", "" + imageList);
							mAdapter.notifyDataSetChanged();
						} catch (JSONException e) {

							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						hidepDialog();
						// notifying list adapter about data changes
						// so that it renders the list view with updated data
						mAdapter.notifyDataSetChanged();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// Toast.makeText(getApplicationContext(),
						// error.getMessage(), Toast.LENGTH_SHORT).show();
						// hide the progress dialog
						hidepDialog();
					}
				}) {
			@Override
			public com.android.volley.Request.Priority getPriority() {
				// TODO Auto-generated method stub
				return priority;
			}
		};

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
}