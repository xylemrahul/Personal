package com.techila.boa.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.techila.boa.Bank_Details;
import com.techila.boa.Company_manage;
import com.techila.boa.R;
import com.techila.boa.SelectionActivity;
import com.techila.boa.config.Appconstant;
import com.techila.boa.util.PrefSingleton;

public class CustomListAdapter extends BaseAdapter {

	private static LayoutInflater inflater = null;
	ArrayList<HashMap<String, String>> items = null;
	public static List<String> chkList = new ArrayList<String>();
	public static ArrayList<HashMap<String, String>> banks = new ArrayList<HashMap<String,String>>();
	HashMap<String, String> map;
	public static boolean[] itemsChecked;
	Activity context;
	String act;

	private PrefSingleton mMyPreferences = null;

	public CustomListAdapter(Activity context,
			ArrayList<HashMap<String, String>> items) {

		this.items = items;
		this.context = context;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (context instanceof Company_manage) {
			act = "com";
			Log.e("constructorC", "" + items.size());
		} else if (context instanceof Bank_Details) {

			itemsChecked = new boolean[items.size()];
			Log.e("constructor", "" + itemsChecked.length);
			act = "bank";
		}
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

	private class ViewHolder {
		TextView tvName, req_no;
		CheckBox ch;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(context);

		View row = convertView;
		ViewHolder vh = null;

		if (row == null) {
			vh = new ViewHolder();
			if (act.equals("com")) {
				row = inflater.inflate(R.layout.custom_list_com, null);
				vh.req_no = (TextView) row.findViewById(R.id.req_no);
			} else {
				row = inflater.inflate(R.layout.custom_list_act, null);
				vh.ch = (CheckBox) row.findViewById(R.id.chkMail);
			}

			vh.tvName = (TextView) row.findViewById(R.id.name);
			row.setTag(vh);
		} else {

			vh = (ViewHolder) row.getTag();
		}

		if (act.equals("com")) {
			getCompanyDetails(position, vh);
			
		} else if (act.equals("bank")) {

			vh.ch.setChecked(false);
			getBankDetails(position, vh);
			vh.ch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {

					HashMap<String, String> element = items.get(position);
					String id = element.get(Appconstant.TAG_BANK_ID);

					try {
						if (isChecked) {
							itemsChecked[position] = true;
							chkList.add(id);
							banks.add(element);
						} else {
							itemsChecked[position] = false;
							chkList.remove(id);
							banks.remove(element);
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}

		row.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (act.equals("com")) {

					HashMap<String, String> element = items.get(position);
					String id = element.get(Appconstant.TAG_COMPANY_ID);
					mMyPreferences.setPreference("C_ID", id);

					Intent intent = new Intent(context, SelectionActivity.class);
					context.startActivity(intent);
				}
			}
		});

		return row;
	}

	private void getCompanyDetails(int position, ViewHolder vh) {

		HashMap<String, String> element = items.get(position);
		String name = element.get(Appconstant.TAG_COMPANY_NAME);
		String number = element.get(Appconstant.TAG_REQ_NO);
		vh.tvName.setText(name);
		vh.req_no.setText(number);
	}

	private void getBankDetails(int position, ViewHolder vh) {

		HashMap<String, String> element = items.get(position);
		String name = element.get(Appconstant.TAG_BANK_NAME);
		vh.tvName.setText(name);
	}

	/*
	 * private void setCheckListener(final int position, ViewHolder vh) {
	 * 
	 * vh.ch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	 * 
	 * @Override public void onCheckedChanged(CompoundButton buttonView, boolean
	 * isChecked) {
	 * 
	 * if (isChecked) { map = new HashMap<String, String>();
	 * Toast.makeText(context, "" + position, Toast.LENGTH_SHORT) .show();
	 * HashMap<String, String> element = items.get(position); String id =
	 * element.get(Appconstant.TAG_BANK_ID); map.put("B_id", id);
	 * mailList.add(map); } Log.e("", ""); } }); }
	 */
}
