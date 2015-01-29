package com.techila.july.assign_management;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.techila.july.assign_management.util.PrefSingleton;

import java.util.ArrayList;
import java.util.HashMap;

public class JobActivity extends Activity {

	Button otj, stj, ltj, sdj;
	private PrefSingleton mMyPreferences;
	LinearLayout layout;
	ImageView logout;

	ArrayList<HashMap<String, String>> GroupList;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_job);

		otj = (Button) findViewById(R.id.otj);
		stj = (Button) findViewById(R.id.stj);
		ltj = (Button) findViewById(R.id.ltj);
		sdj = (Button) findViewById(R.id.sdj);
		layout = (LinearLayout)findViewById(R.id.ll_layout);
		logout = (ImageView) findViewById(R.id.logout);
		
		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());

		GroupList = (ArrayList<HashMap<String, String>>) getIntent()
				.getSerializableExtra("List");

		if(mMyPreferences.getPreference("Context").equals("Assignee")){
			layout.setBackgroundResource(R.drawable.assignee_bg);
			logout.setVisibility(ImageView.VISIBLE);
			
			logout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					mMyPreferences.setPreference("Session", "false");
					Intent intent = new Intent(getApplicationContext(),
							LoginActivity.class);
					startActivity(intent);
					finish();
				}
			});
		}
		
		otj.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mMyPreferences.setPreference("JobType", "One Time Job");

				if (mMyPreferences.getPreference("context")
						.equals("mem_admin")) {
					Intent intent = new Intent(getApplicationContext(),
							NewAssignment.class);
					startActivity(intent);
					finish();
				} else {
					Intent intent = new Intent(getApplicationContext(),
							Assign_Details.class);
					intent.putExtra("List", GroupList);
					startActivity(intent);
				}
			}
		});

		stj.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mMyPreferences.setPreference("JobType", "Short Term Job");
				if (mMyPreferences.getPreference("context")
						.equals("mem_admin")) {
					Intent intent = new Intent(getApplicationContext(),
							NewAssignment.class);
					startActivity(intent);
					finish();
				} else {
					Intent intent = new Intent(getApplicationContext(),
							Assign_Details.class);
					intent.putExtra("List", GroupList);
					startActivity(intent);
				}
			}
		});

		ltj.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mMyPreferences.setPreference("JobType", "Long Term Job");
				if (mMyPreferences.getPreference("context")
						.equals("mem_admin")) {
					Intent intent = new Intent(getApplicationContext(),
							NewAssignment.class);
					startActivity(intent);
					finish();
				} else {
					Intent intent = new Intent(getApplicationContext(),
							Assign_Details.class);
					intent.putExtra("List", GroupList);
					startActivity(intent);
				}
			}
		});

		sdj.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mMyPreferences.setPreference("JobType", "Specific Date Job");

				if (mMyPreferences.getPreference("context")
						.equals("mem_admin")) {
					Intent intent = new Intent(getApplicationContext(),
							NewAssignment.class);
					startActivity(intent);
					finish();
					
				} else {
					Intent intent = new Intent(getApplicationContext(),
							Assign_Details.class);
					intent.putExtra("List", GroupList);
					startActivity(intent);
				}
			}
		});
	}
}