package com.example.movielist.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.movielist.R;
import com.example.movielist.main.AppController;
import com.example.movielist.main.ImageFragment;
import com.viewpagerindicator.IconPagerAdapter;

public class ImagePosterAdapter extends FragmentPagerAdapter {

	private ArrayList<String> images;

	public ImagePosterAdapter(FragmentManager fm) {
		super(fm);
	}

	public ImagePosterAdapter(FragmentManager fm, ArrayList<String> images) {
		super(fm);
		this.images = images;
	}

	@Override
	public Fragment getItem(int position) {
		return ImageFragment.newInstance(images.get(position));
	}

	@Override
	public int getCount() {
		if (images.size() > 5) {
			return 5;
		} else {
			return images.size();
		}
	}
}