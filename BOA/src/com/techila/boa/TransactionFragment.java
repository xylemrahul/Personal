package com.techila.boa;

import com.techila.boa.R;
import com.techila.boa.adapter.RequestPagerAdapter;
import com.techila.boa.adapter.TabsPagerAdapter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;

public class TransactionFragment extends FragmentActivity implements
		ActionBar.TabListener {

	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private RequestPagerAdapter rAdapter;

	// Tab titles
	private String[] tabs = { "Payment", "Withdrawal" };
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle arg0) {

		super.onCreate(arg0);
		setContentView(R.layout.request_fragment);

		String value = getIntent().getExtras().getString("Req");

		// Initilization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();

		if (value.equals("request")) {
			rAdapter = new RequestPagerAdapter(getSupportFragmentManager());
			viewPager.setAdapter(rAdapter);
			// Hide Actionbar Icon
			actionBar.setDisplayShowHomeEnabled(false);

			// Hide Actionbar Title
			actionBar.setDisplayShowTitleEnabled(false);

			// Create Actionbar Tabs
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

			// Adding Tabs
			for (String tab_name : tabs) {
				actionBar.addTab(actionBar.newTab().setText(tab_name)
						.setTabListener(this));
			}

			/**
			 * on swiping the viewpager make respective tab selected
			 * */
			viewPager.setOnPageChangeListener(new OnPageChangeListener() {

				int currentPosition = 0;

				@Override
				public void onPageSelected(int position) {
					actionBar.setSelectedNavigationItem(position);

					currentPosition = position;
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {

				}

				@Override
				public void onPageScrollStateChanged(int arg0) {

				}
			});

		} else {
			mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
			viewPager.setAdapter(mAdapter);
			// Hide Actionbar Icon
			actionBar.setDisplayShowHomeEnabled(false);

			// Hide Actionbar Title
			actionBar.setDisplayShowTitleEnabled(false);

			// Create Actionbar Tabs
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

			// Adding Tabs
			for (String tab_name : tabs) {
				actionBar.addTab(actionBar.newTab().setText(tab_name)
						.setTabListener(this));
			}

			/**
			 * on swiping the viewpager make respective tab selected
			 * */
			viewPager.setOnPageChangeListener(new OnPageChangeListener() {

				int currentPosition = 0;

				@Override
				public void onPageSelected(int position) {
					// on changing the page
					// make respected tab selected
					actionBar.setSelectedNavigationItem(position);

					currentPosition = position;
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {

				}

				@Override
				public void onPageScrollStateChanged(int arg0) {

				}
			});
		}
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// on tab selected
		// show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}
}
