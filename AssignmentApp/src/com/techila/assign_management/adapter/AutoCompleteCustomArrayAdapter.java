package com.techila.assign_management.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;

public class AutoCompleteCustomArrayAdapter extends ArrayAdapter<String>{

	public AutoCompleteCustomArrayAdapter(Context context, int resource,
			ArrayList<String> objects) {
		super(context, resource, objects);
		
	}
	
}
