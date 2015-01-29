package com.techila.assign_management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.techila.assign_management.config.Appconstant;
import com.techila.assign_management.util.JSONParser;
import com.techila.assign_management.util.PrefSingleton;
import com.techila.assign_management.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class DeferredActivity extends ListActivity {

	JSONParser jsonParser = new JSONParser();
	ArrayList<HashMap<String, String>> AssignmentList;
	ArrayList<HashMap<String, String>> AllAssignmentList;
	private PrefSingleton mMyPreferences;
	HashMap<String, String> map;
	HashMap<String, String> map1;
	// products JSONArray
	JSONArray inbox = null;
	ProgressDialog prg;
	String Member_Id,jsonArray = null;

	// Inbox JSON url
	private static final String ASSIGNMENT_URL = "http://phbjharkhand.in/AssignmentApplication/Get_All_Assignment_Details.php";

	// ALL JSON node names
	private static final String TAG_DATE = "createdDate";
	public static final String TAG_RESULT = "result";
	private static final String TAG_STATUS = "status";
	private static final String TAG_NAME = "assignmentName";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pending);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());
		// Hashmap for ListView
		AssignmentList = new ArrayList<HashMap<String, String>>();
		AllAssignmentList = new ArrayList<HashMap<String,String>>();
		
		Member_Id = mMyPreferences.getPreference("Mem_Id");
		
		jsonArray = mMyPreferences.getPreference("List");
		if (mMyPreferences.getPreference("Context").equals("ass")) {

			LoadAllInDone(jsonArray);
		} else {

			// Loading INBOX in Background Thread
			new LoadDeferred().execute();
		}
	}

	/**
	 * Background Async Task to Load all INBOX messages by making HTTP Request
	 * */
	class LoadDeferred extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(DeferredActivity.this);
			prg.setMessage("Loading List ...");
			prg.setIndeterminate(false);
			prg.setCancelable(false);
			prg.show();

			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			params1.add(new BasicNameValuePair("memberID", Member_Id));

			String error_code = null;

			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(ASSIGNMENT_URL,
					"POST", params1);

			// Check your log cat for JSON response
			Log.d("Deferred JSON: ", json.toString());

			try {
				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
				if ("1".equals(error_code)) {

					JSONArray jsonArray = null;
					jsonArray = jsonObj.getJSONArray(TAG_RESULT);
					// looping through All messages
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject c = jsonArray.getJSONObject(i);

						map = new HashMap<String, String>();
						// Storing each json item in variable
						String date = c.getString(TAG_DATE);
						String status = c.getString(TAG_STATUS);
						String name = c.getString(TAG_NAME);
						map.put(TAG_DATE, date);
						map.put(TAG_NAME, name);
						map.put(TAG_STATUS, status);
						if (status.equals("Deferred")) {
							// adding HashList to ArrayList
							AssignmentList.add(map);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return Integer.parseInt(error_code);
		}

		@Override
		protected void onPostExecute(Integer error_num) {
			// TODO Auto-generated method stub
			super.onPostExecute(error_num);
			if (prg.isShowing()) {
				prg.dismiss();
			}

			if (error_num == 2) {

				Toast.makeText(getApplicationContext(),
						"There are no assignments assigned to this member",
						Toast.LENGTH_LONG).show();
			}

			ListAdapter adapter = new SimpleAdapter(DeferredActivity.this,
					AssignmentList, R.layout.activity_tab_list_view,
					new String[] { TAG_NAME, TAG_DATE }, new int[] {
							R.id.status, R.id.created_date });
			setListAdapter(adapter);

		}
	}
	
	private void LoadAllInDone(String array) {

		JSONArray jsonArray = null;
		try {

			jsonArray = new JSONArray(array);
		} catch (JSONException e) {

			e.printStackTrace();
		}
		for (int i = 0; i < jsonArray.length(); i++) {

			try {
				JSONObject c = jsonArray.getJSONObject(i);
				map1 = new HashMap<String, String>();
				// Storing each json item in variable
				String id = c.getString(Appconstant.TAG_ASS_ID);
				String mem_id = c.getString(Appconstant.TAG_MEM_ID);
				String name = c.getString(Appconstant.TAG_ASS_NAME);
				String mem_name = c.getString(Appconstant.TAG_MEM_NAME);
				String grp_name = c.getString(Appconstant.TAG_GRP_NAME);
				String date = c.getString(Appconstant.TAG_DATE);
				String status = c.getString(Appconstant.TAG_STATUS);
				String priority = c.getString(Appconstant.TAG_PRIORITY);
				String assgn_type = c.getString(Appconstant.TAG_TYPE);
				String recurrence = c.getString(Appconstant.TAG_RECURRENCE);
				String description = c.getString(Appconstant.TAG_DESC);
				String job_date = c.getString(Appconstant.TAG_JOB_DATE);

				// adding each child node to HashMap key => value
				map1.put(Appconstant.TAG_ASS_ID, id);
				map1.put(Appconstant.TAG_MEM_ID, mem_id);
				map1.put(Appconstant.TAG_ASS_NAME, name);
				map1.put(Appconstant.TAG_MEM_NAME, mem_name);
				map1.put(Appconstant.TAG_GRP_NAME, grp_name);
				map1.put(Appconstant.TAG_DATE, date);
				map1.put(Appconstant.TAG_STATUS, status);
				map1.put(Appconstant.TAG_DESC, description);
				map1.put(Appconstant.TAG_JOB_DATE, job_date);
				map1.put(Appconstant.TAG_PRIORITY, priority);
				map1.put(Appconstant.TAG_TYPE, assgn_type);
				map1.put(Appconstant.TAG_RECURRENCE, recurrence);
				if (status.equals("Deferred")) {
					// adding HashList to ArrayList
					AllAssignmentList.add(map1);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ListAdapter adapter = new SimpleAdapter(
				DeferredActivity.this,
				AllAssignmentList,
				R.layout.activity_tab_list_view,
				new String[] { Appconstant.TAG_ASS_NAME, Appconstant.TAG_DATE },
				new int[] { R.id.status, R.id.created_date });
		setListAdapter(adapter);
	}
	
}
