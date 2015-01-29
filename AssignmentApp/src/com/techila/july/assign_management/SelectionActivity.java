package com.techila.july.assign_management;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.techila.july.assign_management.config.Appconstant;
import com.techila.july.assign_management.util.JSONParser;
import com.techila.july.assign_management.util.PrefSingleton;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectionActivity extends Activity {

	Button manage_grp, manage_mem, manage_assign, manage_self, reports;
	ProgressDialog prg;
	ImageView logout;
	private PrefSingleton mMyPreferences;

	JSONParser jsonParser = new JSONParser();

	// DEFINING AN ARRAYLIST OF HASHMAP
	ArrayList<HashMap<String, String>> AllAssignmentList;
	ArrayList<HashMap<String, String>> GroupList;

	// creating new HashMap
	HashMap<String, String> map = null;
	HashMap<String, String> map1 = null;

	// Group JSON url
	private static final String GET_ALL_ASSIGNMENT_URL = "http://phbjharkhand.in/AssignmentApplication/Get_StatusWise_All_Assignment_Details.php";
	private static final String GROUP_URL = "http://phbjharkhand.in/AssignmentApplication/Get_Group_Details.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selection);

		LoginActivity.cd = new ConnectionDetector(SelectionActivity.this);
		// Check if Internet present
		if (!LoginActivity.cd.isConnectingToInternet()) {
			// Internet Connection is not present
			AlertDialogManager alert = new AlertDialogManager();
			alert.showAlertDialog(SelectionActivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}

		manage_grp = (Button) findViewById(R.id.grp_details);
		manage_mem = (Button) findViewById(R.id.mem_details);
		manage_assign = (Button) findViewById(R.id.assign_details);
		manage_self = (Button) findViewById(R.id.self_details);
		reports = (Button) findViewById(R.id.report);
		logout = (ImageView) findViewById(R.id.logout);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());

		AllAssignmentList = new ArrayList<HashMap<String, String>>();
		GroupList = new ArrayList<HashMap<String, String>>();

		manage_grp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mMyPreferences.setPreference("context", "grp");
				new LoadOnStart().execute();
			}
		});

		manage_mem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mMyPreferences.setPreference("context", "mem");
				new LoadOnStart().execute();
			}
		});

		manage_assign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMyPreferences.setPreference("context", "admin_assgn");
				new LoadOnStart().execute();
			}
		});

		manage_self.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMyPreferences.setPreference("context", "self");
				new LoadOnStart().execute();
			}
		});

		reports.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mMyPreferences.setPreference("context", "reports");
				new LoadOnStart().execute();
			}
		});

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

	private void callLiasoning() {

		Intent intent = new Intent(getApplicationContext(),
				Assign_Details.class);
		intent.putExtra("List", GroupList);
		startActivity(intent);
	}

	private void callGroup() {

		Intent intent = new Intent(getApplicationContext(), Grp_Manage.class);
		intent.putExtra("List", GroupList);
		startActivity(intent);
	}

	private void callMember() {

		Intent intent = new Intent(getApplicationContext(), Mem_Manage.class);
		intent.putExtra("List", GroupList);
		startActivity(intent);
	}

	private void callAssignment() {

		Intent i = new Intent(getApplicationContext(), JobActivity.class);
		i.putExtra("List", GroupList);
		startActivity(i);
	}

	private void callReports() {

		Intent intent = new Intent(getApplicationContext(),
				ReportsActivity.class);
		intent.putExtra("List", GroupList);
		startActivity(intent);
	}

	class LoadOnStart extends AsyncTask<Void, String, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(SelectionActivity.this);
			prg.setMessage("Loading List ...");
			prg.setIndeterminate(false);
			prg.setCancelable(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			// Building Parameters
			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			String error_code = null;
			Log.e("URL ", "is" + GROUP_URL);
			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(GROUP_URL, "POST",
					params1);

			// Check your log cat for JSON reponse
			Log.d("Inbox JSON: ", json.toString());

			try {
				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
				if ("1".equals(error_code)) {

					JSONArray jsonArray = null;
					jsonArray = jsonObj.getJSONArray(Appconstant.TAG_RESULT);
					GroupList.clear();
					// looping through All messages
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject c = jsonArray.getJSONObject(i);

						map = new HashMap<String, String>();
						// Storing each json item in variable
						String id = c.getString(Appconstant.TAG__GRP_ID);
						String name = c.getString(Appconstant.TAG_GRP_NAME);
						String date = c.getString(Appconstant.TAG_DATE);
						String abbr = c.getString(Appconstant.TAG_ABBR);
						String desc = c.getString(Appconstant.TAG_DESC);

						// adding each child node to HashMap key => value
						map.put(Appconstant.TAG__GRP_ID, id);
						map.put(Appconstant.TAG_GRP_NAME, name);
						map.put(Appconstant.TAG_ABBR, abbr);
						map.put(Appconstant.TAG_DATE, date);
						map.put(Appconstant.TAG_DESC, desc);
						// adding HashList to ArrayList
						GroupList.add(map);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return Integer.parseInt(error_code);
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(Integer error_num) {

			// dismiss the dialog after getting all products
			if (prg.isShowing()) {
				prg.dismiss();
			}

			if (error_num == 2) {
				Toast.makeText(getApplicationContext(),
						"There are no groups assigned", Toast.LENGTH_LONG)
						.show();
			}

			String context = mMyPreferences.getPreference("context");

			if (context.equals("grp")) {
				callGroup();
			} else if (context.equals("mem")) {
				callMember();
			} else if (context.equals("admin_assgn")) {
				callAssignment();
			} else if (context.equals("self")) {
				callLiasoning();
			} else if (context.equals("reports")) {
				callReports();
			}
		}
	}

	protected class LoadGetAllAssignments extends
			AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(SelectionActivity.this);
			prg.setMessage("Loading List ...");
			prg.setIndeterminate(false);
			prg.setCancelable(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			// Building Parameters
			List<NameValuePair> params1 = new ArrayList<NameValuePair>();

			String error_code = null;
			Log.e("URL ", "is" + GET_ALL_ASSIGNMENT_URL);
			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(
					GET_ALL_ASSIGNMENT_URL, "POST", params1);

			// Check your log cat for JSON response
			Log.d("Inbox JSON: ", json.toString());

			try {
				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
				if ("1".equals(error_code)) {

					JSONArray statusWise = new JSONArray();
					JSONArray jsonArray = jsonObj
							.getJSONArray(Appconstant.TAG_RESULT);

					// looping through All messages
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject c = jsonArray.getJSONObject(i);
						statusWise.put(c);

					}
					mMyPreferences.setPreference("List", statusWise.toString());
					mMyPreferences.setPreference("Context", "ass");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return Integer.parseInt(error_code);
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(Integer error_num) {

			// dismiss the dialog after getting all products
			if (prg.isShowing()) {
				prg.dismiss();
			}

			if (error_num == 2) {

				Toast.makeText(getApplicationContext(),
						"There are no assignments assigned to this member",
						Toast.LENGTH_LONG).show();
			}

			Intent intent = new Intent(getApplicationContext(),
					AssignFragmentListView.class);
			startActivity(intent);
		}
	}
}
