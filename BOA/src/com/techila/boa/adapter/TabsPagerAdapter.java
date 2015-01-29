package com.techila.boa.adapter;

import java.util.ArrayList;
import java.util.List;

import com.techila.boa.fragments.AdminPay;
import com.techila.boa.fragments.AdminWid;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	private List<Fragment> fragments;

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
		this.fragments = new ArrayList<Fragment>();
		fragments.add(new AdminPay());
		fragments.add(new AdminWid());
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
