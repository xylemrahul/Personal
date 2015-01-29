package com.techila.july.assign_management.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class AutoCompleteCustomArrayAdapter extends ArrayAdapter<String>{

	public AutoCompleteCustomArrayAdapter(Context context, int resource,
			ArrayList<String> objects) {
		super(context, resource, objects);
		
	}
	
}
