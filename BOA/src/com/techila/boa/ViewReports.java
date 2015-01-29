package com.techila.boa;

import java.util.ArrayList;
import java.util.HashMap;

import com.techila.boa.adapter.ReportsAdapter;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ListView;

public class ViewReports extends ListActivity {

	ReportsAdapter adapter;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_list);

		try {

		} catch (Exception e) {
			// TODO: handle exception
		}
		ArrayList<HashMap<String, String>> ReportList = (ArrayList<HashMap<String, String>>) getIntent()
				.getSerializableExtra("ReportList");
		ListView lv = getListView();
		adapter = new ReportsAdapter(ViewReports.this, ReportList);
		lv.setAdapter(adapter);
	}
}