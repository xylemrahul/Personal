package com.techila.boa.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.techila.boa.R;
import com.techila.boa.config.Appconstant;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdminUserAdapter extends BaseAdapter {

	private static LayoutInflater inflater = null;
	ArrayList<HashMap<String, String>> items = null;
	Activity context;
	HashMap<String, String> map;
//	private PrefSingleton mMyPreferences = null;
	
	public AdminUserAdapter(Activity context,
			ArrayList<HashMap<String, String>> items) {

		this.items = items;
		this.context = context;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}

	@Override
	public int getCount() {

		return items.size();
	}

	@Override
	public Object getItem(int position) {

		return items.get(position);
	}

	@Override
	public long getItemId(int position) {

		return 0;
	}
	
	private class ViewHolder{
		TextView uName;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;
		ViewHolder vh = null;

		if (row == null) {

			row = inflater.inflate(R.layout.custom_list_user, null);
			vh = new ViewHolder();
			
			vh.uName = (TextView) row.findViewById(R.id.name);
			row.setTag(vh);
		} else {

			vh = (ViewHolder) row.getTag();
		}
		
		HashMap<String, String> element = items.get(position);
		String fname = element.get(Appconstant.TAG_FNAME);
		String lname = element.get(Appconstant.TAG_LNAME);
		vh.uName.setText(fname+" "+lname);
		
		return row;
	}
}
