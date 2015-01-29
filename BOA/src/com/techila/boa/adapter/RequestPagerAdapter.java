package com.techila.boa.adapter;

import java.util.ArrayList;
import java.util.List;

import com.techila.boa.fragments.AdminPayReq;
import com.techila.boa.fragments.AdminWidReq;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class RequestPagerAdapter extends FragmentPagerAdapter{

	private List<Fragment> fragments;
	
	public RequestPagerAdapter(FragmentManager fm) {
		super(fm);

		this.fragments = new ArrayList<Fragment>();
		fragments.add(new AdminPayReq());
		fragments.add(new AdminWidReq());
		
	}

	@Override
	public Fragment getItem(int index) {
		return fragments.get(index);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	
	
}
