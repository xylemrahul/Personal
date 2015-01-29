package com.techila.july.assign_management;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.costum.android.widget.LoadMoreListView.OnLoadMoreListener;
import com.techila.july.assign_management.R;
import com.techila.july.assign_management.adapter.CustomAdapterAdminAssignListView;
import com.techila.july.assign_management.adapter.CustomAdapterListView;
import com.techila.july.assign_management.config.Appconstant;
import com.techila.july.assign_management.util.JSONParser;
import com.techila.july.assign_management.util.PrefSingleton;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Assign_Details extends ListActivity {

	Button remove;
	private PrefSingleton mMyPreferences;
	EditText et_name, et_desc, search;
	LinearLayout layout_recc;
	static RadioButton radio = null;
	static RadioButton recurrence = null;
	Button addAssignment, select_recurrence, getPastAssignments;
	TextView details;
	RadioGroup et_priority, recurrence_rd;
	Spinner type_recurrence;
	String name;
	String type;
	String description;
	String Member_Id;
	String assgn_id;
	protected Handler mHandler;
	ProgressDialog prg;
	static StringBuilder date;

	JSONParser jsonParser = new JSONParser();

	// DEFINING AN ARRAYLIST OF HASHMAP
	static ArrayList<HashMap<String, String>> AssignmentList;
	ArrayList<HashMap<String, String>> GroupList;

	// Creating a clone of the arraylist
	ArrayList<HashMap<String, String>> CloneList;

	// creating new HashMap
	HashMap<String, String> map = null;

	// Group JSON url
	public static final String ASSIGNMENT_URL = "http://phbjharkhand.in/AssignmentApplication/Get_Type_Wise_Assignment_Details.php";
	public static final String ASSIGNMENT_URL_ASSIGNEE = "http://phbjharkhand.in/AssignmentApplication/Get_Type_Member_Wise_Assignment_Details.php";
	private static final String SELF_ASSIGNMENT_URL = "http://phbjharkhand.in/AssignmentApplication/Get_Assignment_Details.php";

	CustomAdapterListView adapter1;
	CustomAdapterAdminAssignListView adapter;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		setContentView(R.layout.activity_assign_manage);

		details = (TextView) findViewById(R.id.viewDetails);
		addAssignment = (Button) findViewById(R.id.addBtn);
		getPastAssignments = (Button) findViewById(R.id.get_all_past);
		search = (EditText) findViewById(R.id.search);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());

		// Hashmap for listview
		AssignmentList = new ArrayList<HashMap<String, String>>();

		GroupList = (ArrayList<HashMap<String, String>>) getIntent()
				.getSerializableExtra("List");

		Member_Id = mMyPreferences.getPreference("Mem_Id");

		ListView lv = getListView();

		// ---------------------------Checking if the Login person is admin or
		// assignee----------------------------
		// ---------------------Get the preference from
		// LoginActivity----------------------

		if (mMyPreferences.getPreference("Context").equals("Assignee")) {
			adapter1 = new CustomAdapterListView(this, AssignmentList);
			lv.setAdapter(adapter1);
			ViewGroup layout = (ViewGroup) addAssignment.getParent();

			if (layout != null) {

				layout.removeView(addAssignment);

			}

			getPastAssignments.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {

					Intent intent = new Intent(getApplicationContext(),
							AssignFragmentListView.class);
					startActivity(intent);
				}
			});
		} else {

			adapter = new CustomAdapterAdminAssignListView(this, AssignmentList);
			lv.setAdapter(adapter);

			ViewGroup status = (ViewGroup) getPastAssignments.getParent();
			if (status != null) {
				status.removeView(getPastAssignments);
			}
			addAssignment.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					addItems();
				}
			});
		}

		// Loading Member in Background Thread
		new LoadOnStartAssignment().execute();

	}

	// METHOD WHICH WILL HANDLE DYNAMIC INSERTION
	public void addItems() {

		Intent intent = new Intent(getApplicationContext(), NewAssignment.class);
		intent.putExtra("List", GroupList);
		startActivityForResult(intent, 1);

		// showAssignAssignmentDialog();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1) {

			if (!(requestCode == RESULT_OK)) {
				adapter.sortByDate();
				adapter.notifyDataSetChanged();
			}
			if (resultCode == RESULT_CANCELED) {

			}
		}
	}

	// xxxxxxxxxxxxxxxxxxxxxxxx LOAD ASSIGNMENTS ON START
	// xxxxxxxxxxxxxxxxxxxxxxxxx

	class LoadOnStartAssignment extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(Assign_Details.this);
			prg.setMessage("Loading List ...");
			prg.setIndeterminate(false);
			prg.setCancelable(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			String error_code = null;
			JSONObject json = null;
			// Building Parameters
			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			List<NameValuePair> params2 = new ArrayList<NameValuePair>();

			if ("One Time Job".equals(mMyPreferences.getPreference("JobType"))) {
				params1.add(new BasicNameValuePair("assType", mMyPreferences
						.getPreference("JobType")));
			} else if ("Short Term Job".equals(mMyPreferences
					.getPreference("JobType"))) {
				params1.add(new BasicNameValuePair("assType", mMyPreferences
						.getPreference("JobType")));
			} else if ("Long Term Job".equals(mMyPreferences
					.getPreference("JobType"))) {
				params1.add(new BasicNameValuePair("assType", mMyPreferences
						.getPreference("JobType")));
			} else if ("Specific Date Job".equals(mMyPreferences
					.getPreference("JobType"))) {
				params1.add(new BasicNameValuePair("assType", mMyPreferences
						.getPreference("JobType")));
			}

			if ("Assignee".equals(mMyPreferences.getPreference("Context"))) {
				params1.add(new BasicNameValuePair("memberID", Member_Id));
				// getting JSON string from URL
				Log.e("URL ", "is" + ASSIGNMENT_URL_ASSIGNEE);
				json = jsonParser.makeHttpRequest(ASSIGNMENT_URL_ASSIGNEE,
						"POST", params1);

			} else {

				if ("self".equals(mMyPreferences.getPreference("context"))) {
					params2.add(new BasicNameValuePair("memberID", Member_Id));
					json = jsonParser.makeHttpRequest(SELF_ASSIGNMENT_URL,
							"POST", params2);

				} else {
					Log.e("URL ", "is" + ASSIGNMENT_URL);
					// getting JSON string from URL
					json = jsonParser.makeHttpRequest(ASSIGNMENT_URL, "POST",
							params1);
				}
			}

			// Check your log cat for JSON response
			Log.d("Inbox JSON: ", json.toString());

			try {
				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
				if ("1".equals(error_code)) {

					JSONArray jsonArray = null;
					jsonArray = jsonObj.getJSONArray(Appconstant.TAG_RESULT);
					// looping through All messages
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject c = jsonArray.getJSONObject(i);

						map = new HashMap<String, String>();
						// Storing each json item in variable
						String id = c.getString(Appconstant.TAG_ASS_ID);
						String name = c.getString(Appconstant.TAG_ASS_NAME);
						String memname = c.getString(Appconstant.TAG_MEM_NAME);
						String grpname = c.getString(Appconstant.TAG_GRP_NAME);
						String date = c.getString(Appconstant.TAG_DATE);
						String status = c.getString(Appconstant.TAG_STATUS);
						String priority = c.getString(Appconstant.TAG_PRIORITY);
						String assgn_type = c.getString(Appconstant.TAG_TYPE);
						String recurrence = c
								.getString(Appconstant.TAG_RECURRENCE);
						String description = c.getString(Appconstant.TAG_DESC);
						String job_date = c.getString(Appconstant.TAG_JOB_DATE);

						// adding each child node to HashMap key => value
						map.put(Appconstant.TAG_ASS_ID, id);
						map.put(Appconstant.TAG_ASS_NAME, name);
						map.put(Appconstant.TAG_MEM_NAME, memname);
						map.put(Appconstant.TAG_GRP_NAME, grpname);
						map.put(Appconstant.TAG_DATE, date);
						map.put(Appconstant.TAG_STATUS, status);
						map.put(Appconstant.TAG_DESC, description);
						map.put(Appconstant.TAG_JOB_DATE, job_date);
						map.put(Appconstant.TAG_PRIORITY, priority);
						map.put(Appconstant.TAG_TYPE, assgn_type);
						map.put(Appconstant.TAG_RECURRENCE, recurrence);

						// adding HashList to ArrayList
						if ("Assignee".equals(mMyPreferences
								.getPreference("Context"))) {
							if (status.equals("Pending")
									|| status.equals("InProgress")) {
								AssignmentList.add(map);
							} else {
								error_code = "3";
							}
						} else {
							AssignmentList.add(map);
						}
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
		@SuppressWarnings("unchecked")
		protected void onPostExecute(Integer error_num) {

			// dismiss the dialog after getting all products
			if (prg.isShowing()) {
				prg.dismiss();
			}

			if (error_num == 2) {
				Toast.makeText(getApplicationContext(),
						"There are no assignments of this job type",
						Toast.LENGTH_SHORT).show();
			}
			if (error_num == 3) {

				Toast.makeText(getApplicationContext(),
						"There are no current assignments", Toast.LENGTH_SHORT)
						.show();
			}

			/**
			 * Updating parsed JSON data into ListView
			 **/

			if (mMyPreferences.getPreference("Context").equals("Assignee")) {
				// updating listview
				adapter1.sortByDate();
				adapter1.notifyDataSetChanged();

			} else {
				adapter.sortByDate();
				adapter.notifyDataSetChanged();
			}

			try {
				CloneList = (ArrayList<HashMap<String, String>>) AssignmentList
						.clone();
			} catch (Exception e) {
				e.printStackTrace();
			}
			searchItems();

		}
	}

	protected void LoadMoreListView() {

		((com.costum.android.widget.LoadMoreListView) getListView())
				.setOnLoadMoreListener(new OnLoadMoreListener() {

					@Override
					public void onLoadMore() {

						new LoadDataTask().execute();
					}
				});
	}

	private class LoadDataTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... arg0) {

			// Building Parameters
			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			params1.add(new BasicNameValuePair("scrollType", "yes"));
			String error_code = null;

			JSONObject json = jsonParser.makeHttpRequest(ASSIGNMENT_URL,
					"POST", params1);
			Log.d("Inbox JSON: ", json.toString());

			try {

				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
				if ("1".equals(error_code)) {
					JSONArray jsonArray = null;
					jsonArray = jsonObj.getJSONArray(Appconstant.TAG_RESULT);
					// looping through All messages
					AssignmentList.clear();
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject c = jsonArray.getJSONObject(i);

						map = new HashMap<String, String>();
						String id = c.getString(Appconstant.TAG_ASS_ID);
						String name = c.getString(Appconstant.TAG_ASS_NAME);
						String memname = c.getString(Appconstant.TAG_MEM_NAME);
						String grpname = c.getString(Appconstant.TAG_GRP_NAME);
						String date = c.getString(Appconstant.TAG_DATE);
						String status = c.getString(Appconstant.TAG_STATUS);
						String priority = c.getString(Appconstant.TAG_PRIORITY);
						String assgn_type = c.getString(Appconstant.TAG_TYPE);
						String recurrence = c
								.getString(Appconstant.TAG_RECURRENCE);
						String description = c.getString(Appconstant.TAG_DESC);
						String job_date = c.getString(Appconstant.TAG_JOB_DATE);

						// adding each child node to HashMap key => value
						map.put(Appconstant.TAG_ASS_ID, id);
						map.put(Appconstant.TAG_ASS_NAME, name);
						map.put(Appconstant.TAG_MEM_NAME, memname);
						map.put(Appconstant.TAG_GRP_NAME, grpname);
						map.put(Appconstant.TAG_DATE, date);
						map.put(Appconstant.TAG_STATUS, status);
						map.put(Appconstant.TAG_DESC, description);
						map.put(Appconstant.TAG_JOB_DATE, job_date);
						map.put(Appconstant.TAG_PRIORITY, priority);
						map.put(Appconstant.TAG_TYPE, assgn_type);
						map.put(Appconstant.TAG_RECURRENCE, recurrence);

						// adding HashList to ArrayList
						AssignmentList.add(map);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return Integer.parseInt(error_code);
		}

		@Override
		protected void onPostExecute(Integer error_num) {

			if (error_num == 2) {
				Toast.makeText(getApplicationContext(),
						"There are no assignments assigned", Toast.LENGTH_SHORT)
						.show();
			}
			/**
			 * Updating parsed JSON data into ListView
			 * */
			if (mMyPreferences.getPreference("Context").equals("Assignee")) {
				// updating listview
				adapter1.notifyDataSetChanged();
			} else {
				adapter.sortByDate();
				adapter.notifyDataSetChanged();
			}
		}
	}

	protected void searchItems() {

		search.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				String clonListTagName = null;
				AssignmentList.clear();
				for (int i = 0; i < CloneList.size(); i++) {

					// Log.e("",
					// ""+CloneList.get(i).get(TAG_ASS_NAME).toUpperCase().startsWith(s.toString()));
					clonListTagName = CloneList.get(i)
							.get(Appconstant.TAG_ASS_NAME).toLowerCase();
					if (clonListTagName.startsWith(s.toString())) {
						if (AssignmentList.contains(Appconstant.TAG_ASS_NAME)) {
							Log.e("inside", "if");
						} else {
							Log.e("inside", "else");
							AssignmentList.add(CloneList.get(i));
						}
						Log.e("", "OnTextChanged");
					}
					if (AssignmentList.isEmpty()) {
						Log.e("", "empty");
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s != null && !s.toString().equalsIgnoreCase("")) {
					adapter.notifyDataSetChanged();
				}
			}
		});
	}

	@Override
	protected void onResume() {

		super.onResume();

		Log.e("", "OnRESUME");
	}

}