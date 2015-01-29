package com.techila.july.assign_management.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.techila.july.assign_management.CancelFragment;
import com.techila.july.assign_management.DeferredFragment;
import com.techila.july.assign_management.DoneFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {
		
		switch (index) {
		case 0:
			
			return new CancelFragment();
		case 1:
			
			return new DoneFragment();
		case 2:
			
			return new DeferredFragment();
		}

		return null;
	}

	@Override
	public int getCount() {
		return 3;
	}
}
