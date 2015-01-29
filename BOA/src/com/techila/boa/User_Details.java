package com.techila.boa;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.techila.boa.R;
import com.techila.boa.adapter.AdminUserAdapter;
import com.techila.boa.config.Appconstant;
import com.techila.boa.util.JSONParser;
import com.techila.boa.util.PrefSingleton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class User_Details extends Activity implements OnClickListener {

	private PrefSingleton mMyPreferences;
	Button addUser;
	ProgressDialog prg;
	String CompanyId;

	static ArrayList<HashMap<String, String>> UserList;
	HashMap<String, String> map;

	JSONParser jsonParser = new JSONParser();
	Properties prop;

	ListView lv;
	AdminUserAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.list_user_details);
		// Initialize variables
		initViews();

		UserList = new ArrayList<HashMap<String, String>>();

		adapter = new AdminUserAdapter(User_Details.this, UserList);
		lv.setAdapter(adapter);
		
		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());
		
		Resources resources = this.getResources();
		AssetManager assetManager = resources.getAssets();
		try {
		    InputStream inputStream = assetManager.open("jsonURL.properties");
		    prop = new Properties();
		    prop.load(inputStream);
		} catch (IOException e) {
		    System.err.println("Failed to open jsonURL property file");
		    e.printStackTrace();
		}
		CompanyId = mMyPreferences.getPreference("C_ID");

		new getUserDetails().execute();
	}

	// --------------------------------------Calling the activity for adding
	// bank----------------------------------
	private void addUser() {

		Intent intent = new Intent(getApplicationContext(), Add_User.class);
		startActivityForResult(intent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1) {

			if (resultCode == RESULT_OK) {
				adapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(),
						"User added successfully", Toast.LENGTH_SHORT).show();
			}

			if (resultCode == RESULT_CANCELED) {
				
			}
		}
	}

	private void initViews() {

		lv = (ListView) findViewById(R.id.userList);
		addUser = (Button) findViewById(R.id.btn_add_user);

		addUser.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_add_user:

			addUser();
			break;

		default:
			break;
		}
	}

	class getUserDetails extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(User_Details.this);
			prg.setMessage("Fetching Users");
			prg.setIndeterminate(true);
			prg.setCanceledOnTouchOutside(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			List<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("companyID", CompanyId));

			String error_code = null;
			Log.e("URL ", "is" + prop.getProperty("GET_USER_URL"));

			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(
					Appconstant.GET_USER_URL, "POST", nvp);

			// Check your log cat for JSON response
			Log.d("Inbox JSON: ", json.toString());
			try {

				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
				if ("1".equals(error_code)) {

					UserList.clear();
					JSONArray jArray = jsonObj
							.getJSONArray(Appconstant.TAG_RESULT);
					for (int i = 0; i < jArray.length(); i++) {

						map = new HashMap<String, String>();
						JSONObject jsonObj1 = jArray.getJSONObject(i);

						String fname = jsonObj1
								.getString(Appconstant.TAG_FNAME);
						String lname = jsonObj1
								.getString(Appconstant.TAG_LNAME);
						String mail = jsonObj1.getString(Appconstant.TAG_MAIL);
						String sex = jsonObj1.getString(Appconstant.TAG_SEX);
						String address = jsonObj1
								.getString(Appconstant.TAG_USER_ADD);
						String type = jsonObj1
								.getString(Appconstant.TAG_USER_TYPE);
						String age = jsonObj1.getString(Appconstant.TAG_AGE);

						// Adding values to map
						map.put(Appconstant.TAG_FNAME, fname);
						map.put(Appconstant.TAG_LNAME, lname);
						map.put(Appconstant.TAG_MAIL, mail);
						map.put(Appconstant.TAG_SEX, sex);
						map.put(Appconstant.TAG_USER_ADD, address);
						map.put(Appconstant.TAG_USER_TYPE, type);
						map.put(Appconstant.TAG_AGE, age);

						// Adding Hashmap to Arraylist
						UserList.add(map);
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
		protected void onPostExecute(Integer result) {

			super.onPostExecute(result);
			if (prg.isShowing()) {
				prg.dismiss();
			}

			if (result == 2) {

				Toast.makeText(getApplicationContext(),
						"There are no users assigned to this company",
						Toast.LENGTH_SHORT).show();
			}
			adapter.notifyDataSetChanged();
		}
	}
}
