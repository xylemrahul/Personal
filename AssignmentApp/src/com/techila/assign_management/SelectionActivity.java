package com.techila.assign_management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.techila.assign_management.config.Appconstant;
import com.techila.assign_management.util.JSONParser;
import com.techila.assign_management.util.PrefSingleton;
import com.techila.assign_management.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SelectionActivity extends Activity {

	Button manage_grp, manage_assign, manage_self;
	ProgressDialog prg;
	private PrefSingleton mMyPreferences;

	JSONParser jsonParser = new JSONParser();

	// DEFINING AN ARRAYLIST OF HASHMAP
	ArrayList<HashMap<String, String>> AllAssignmentList;

	// creating new HashMap
	HashMap<String, String> map = null;

	// Group JSON url
	private static final String GET_ALL_ASSIGNMENT_URL = "http://phbjharkhand.in/AssignmentApplication/Get_StatusWise_All_Assignment_Details.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		manage_grp = (Button) findViewById(R.id.grp_details);
		manage_assign = (Button) findViewById(R.id.assign_details);
		manage_self = (Button) findViewById(R.id.self_details);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());
		
		AllAssignmentList = new ArrayList<HashMap<String,String>>();
		
		manage_grp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				callGroup();
			}
		});
		manage_assign.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new LoadGetAllAssignments().execute();
			}
		});
		
		manage_self.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				callLiason();
			}
		});
	}

	private void callLiason() {

	}

	private void callGroup() {

		Intent intent = new Intent(getApplicationContext(), Grp_Manage.class);
		startActivity(intent);

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
					JSONArray jsonArray = jsonObj.getJSONArray(Appconstant.TAG_RESULT);
					
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
			
			Intent intent = new Intent(getApplicationContext(),AssignTabListView.class);
			startActivity(intent);
		}
	}
}
