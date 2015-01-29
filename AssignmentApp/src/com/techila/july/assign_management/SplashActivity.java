package com.techila.july.assign_management;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.techila.july.assign_management.R;
import com.techila.july.assign_management.util.PrefSingleton;

public class SplashActivity extends Activity {

	// Splash screen timer
	private static int SPLASH_TIME_OUT = 2000;
	PrefSingleton mMyPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// This method will be executed once the timer is over
				// Start your app main activity
				if (mMyPreferences.getPreference("Session").equals("true")) {
					if (mMyPreferences.getPreference("Context").equals(
							"Assignee")) {
						Intent i = new Intent(getApplicationContext(),
								JobActivity.class);
						startActivity(i);
					} else {
						Intent i = new Intent(getApplicationContext(),
								SelectionActivity.class);
						startActivity(i);
					}
				} else {
					Intent i = new Intent(getApplicationContext(),
							LoginActivity.class);
					startActivity(i);
				}

				// close this activity
				finish();
			}
		}, SPLASH_TIME_OUT);
	}
}
