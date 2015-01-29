package com.example.movielist.main;

import com.android.volley.toolbox.NetworkImageView;
import com.example.movielist.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public final class ImageFragment extends Fragment {
	String url = "http://image.tmdb.org/t/p/w185", img_path;

	public ImageFragment() {
		super();
	}

	public static Fragment newInstance(String img_path) {
		ImageFragment fragment = new ImageFragment();
		fragment.img_path = img_path;
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.vp_image, container, false);
		NetworkImageView image = (NetworkImageView) view
				.findViewById(R.id.image_detail);
		image.setImageUrl(url + img_path,
				AppController.getInstance().getImageLoader());
		return view;
	}
}