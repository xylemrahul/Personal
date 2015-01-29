package com.techila.boa.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.techila.boa.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ReportsAdapter extends BaseAdapter {

	ArrayList<HashMap<String, String>> items;
	Activity activity;
	LayoutInflater inflater;

	public ReportsAdapter(Activity activity,
			ArrayList<HashMap<String, String>> items) {
		this.activity = activity;
		this.items = items;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {

		return items.size();
	}

	@Override
	public Object getItem(int position) {

		return null;
	}

	@Override
	public long getItemId(int position) {

		return 0;
	}

	private class ViewHolder {

		TextView date, source, destination, type, chqDate, amt;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;
		ViewHolder vh = null;

		if (row == null) {

			row = inflater.inflate(R.layout.custom_report, null);
			vh = new ViewHolder();
			vh.date = (TextView) row.findViewById(R.id.tx_date);
			vh.source = (TextView) row.findViewById(R.id.tx_SBank);
			vh.destination = (TextView) row.findViewById(R.id.tx_DBank);
			vh.type = (TextView) row.findViewById(R.id.tx_type);
			vh.chqDate = (TextView) row.findViewById(R.id.tx_chqDt);
			vh.amt = (TextView) row.findViewById(R.id.tx_amt);

		} else {
			vh = (ViewHolder) row.getTag();
		}

		HashMap<String, String> elements = items.get(position);
		
		String source = elements.get("sBank");
		String type = elements.get("type");
		String amt = elements.get("Amount");
		String dest = elements.get("dBank");

		if (type.equals("net")) {
			String pDate = elements.get("pDate");
			vh.chqDate.setText("NA");
			vh.date.setText(pDate);
			vh.source.setText(source);
			vh.destination.setText(dest);
			vh.amt.setText(amt);
			vh.type.setText(type);

		}else if (type.equals("withdrawl")) {
			String pDate[] = elements.get("pDate").split(" ");
			vh.chqDate.setText("NA");
			vh.destination.setText("NA");
			vh.date.setText(pDate[0]);
			vh.source.setText(source);
			vh.amt.setText(amt);
			vh.type.setText("withdrawal");
		}else if(type.equals("cheque")){
			String chqDate[] = elements.get("cDate").split(" ");
			vh.chqDate.setText(chqDate[0]);
			vh.source.setText(source);
			vh.destination.setText(dest);
			vh.amt.setText(amt);
			vh.type.setText(type);
		}
		return row;
	}
}