package com.example.movielist.main;

import com.example.movielist.R;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

public class Info extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		
		ActionBar action = getActionBar();
		action.setTitle("Information");
		action.setBackgroundDrawable(new ColorDrawable(Color
				.parseColor("#333333")));
	}
}
