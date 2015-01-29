package com.techila.july.assign_management;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.techila.july.assign_management.config.Appconstant;
import com.techila.july.assign_management.util.JSONParser;
import com.techila.july.assign_management.util.PrefSingleton;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class NewAssignment extends Activity {

	private EditText et_name, et_desc;
	private RadioGroup et_priority, recurrence_rd;
	private RadioButton radio = null, recurrence = null;
	private int priority, recc;
	private Button assign, cancel;
	private LinearLayout layout_recc;
	private String type, name, description, Member_Id, assgn_id, Grp_Id;
	private String[] grp_str, mem_str;
	private static Boolean flag = false;
	private static Boolean SPINNER_FLAG = false;
	protected Handler mHandler;
	ProgressDialog prg;
	Spinner Grp_Spinner, Mem_Spinner;
	private PrefSingleton mMyPreferences;
	StringBuilder date;

	ArrayList<HashMap<String, String>> MemberList;
	ArrayList<HashMap<String, String>> GroupList;

	// creating new HashMap
	HashMap<String, String> map = null;

	JSONParser jsonParser = new JSONParser();
	private static final String MEMBER_URL = "http://phbjharkhand.in/AssignmentApplication/Get_Member_Details.php";

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_assign_assignment);

		et_name = (EditText) findViewById(R.id.tx_aname);
		et_priority = (RadioGroup) findViewById(R.id.tx_priority);
		et_desc = (EditText) findViewById(R.id.tx_desc);
		layout_recc = (LinearLayout) findViewById(R.id.recurrence_layout);
		assign = (Button) findViewById(R.id.Assign);
		cancel = (Button) findViewById(R.id.Cancel);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());

		GroupList = (ArrayList<HashMap<String, String>>) getIntent()
				.getSerializableExtra("List");
		MemberList = new ArrayList<HashMap<String, String>>();

		type = mMyPreferences.getPreference("JobType");

		if (type.equals("One Time Job")) {

			layout_recc.setVisibility(View.GONE);
			flag = false;
		} else if (type.equals("Specific Date Job")) {

			layout_recc.setVisibility(View.GONE);
			flag = false;
		} else if (type.equals("Short Term Job")
				|| ("self".equals(mMyPreferences.getPreference("context")))) {

			recurrence_rd = (RadioGroup) findViewById(R.id.tx_recurrence);
			flag = true;
		} else if (type.equals("Long Term Job")
				|| ("self".equals(mMyPreferences.getPreference("context")))) {

			recurrence_rd = (RadioGroup) findViewById(R.id.tx_recurrence);
			flag = true;
		}

		assign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				name = et_name.getText().toString().trim();
				description = et_desc.getText().toString().trim();
				priority = et_priority.getCheckedRadioButtonId();
				radio = (RadioButton) findViewById(priority);

				try {

					if (name.length() == 0 || description.length() == 0) {
						AlertDialog.Builder alert = new AlertDialog.Builder(
								NewAssignment.this);
						alert.setTitle("Invalid !");
						alert.setCancelable(false);
						alert.setMessage("Please enter all the fields");

						alert.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										dialog.dismiss();
									}
								});
						alert.show();
					} else if (flag) {
						recc = recurrence_rd.getCheckedRadioButtonId();
						recurrence = (RadioButton) findViewById(recc);
						if ("self".equals(mMyPreferences
								.getPreference("context"))) {
							Member_Id = mMyPreferences.getPreference("Mem_Id");
							Grp_Id = mMyPreferences.getPreference("Grp_ID");
							callAddService();
						} else if ("mem_admin".equals(mMyPreferences
								.getPreference("context"))) {
							Member_Id = mMyPreferences.getPreference("Mem_Id");
							Grp_Id = mMyPreferences.getPreference("Grp_ID");
							callAddService();
						} else {
							selectGrpMember();
						}
					} else if (type.equals("Specific Date Job")) {

						Calendar c = Calendar.getInstance();
						int mYear = c.get(Calendar.YEAR);
						int mMonth = c.get(Calendar.MONTH);
						int mDay = c.get(Calendar.DAY_OF_MONTH);
						System.out.println("the selected " + mDay);
						DatePickerDialog date = new DatePickerDialog(
								NewAssignment.this, new mDateSetListener(),
								mYear, mMonth, mDay);
						date.show();
					} else {
						if ("mem_admin".equals(mMyPreferences
								.getPreference("context"))) {
							Member_Id = mMyPreferences.getPreference("Mem_Id");
							Grp_Id = mMyPreferences.getPreference("Grp_ID");
							callAddService();
						} else {
							selectGrpMember();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.v("exception", e.toString());
				}
			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED, returnIntent);
				finish();
			}
		});
	}

	protected void selectGrpMember() {

		AlertDialog.Builder alert = new AlertDialog.Builder(NewAssignment.this);
		alert.setTitle("\t \t \t Select the Details");
		alert.setCancelable(false);

		LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = layoutInflater.inflate(R.layout.dialog_grpmember,
				null);

		Grp_Spinner = (Spinner) view.findViewById(R.id.grp_sp_type);
		Mem_Spinner = (Spinner) view.findViewById(R.id.mem_sp_type);

		LoadGrp();

		alert.setPositiveButton("Submit",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						if (SPINNER_FLAG) {
							callAddService();
						} else {
							Toast.makeText(getApplicationContext(),
									"No members assigned to this group",
									Toast.LENGTH_SHORT).show();
							selectGrpMember();
						}
					}
				});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();
					}
				});
		alert.setView(view);
		alert.show();
	}

	private void LoadGrp() {

		// -----------------Loading the items in the
		// spinner---------------------
		grp_str = new String[GroupList.size()];
		for (int i = 0; i < GroupList.size(); i++) {

			HashMap<String, String> items = GroupList.get(i);

			grp_str[i] = items.get(Appconstant.TAG_GRP_NAME);
			Log.e("", "");
		}

		ArrayAdapter<String> adp1 = new ArrayAdapter<String>(this,
				R.layout.spn_txt, grp_str);
		// adp1.setDropDownViewResource(R.layout.spn_edittext);
		Grp_Spinner.setAdapter(adp1);
		Grp_Spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Toast.makeText(getApplicationContext(), grp_str[position],
						Toast.LENGTH_SHORT).show();
				HashMap<String, String> items = GroupList.get(position);
				Grp_Id = items.get(Appconstant.TAG__GRP_ID);
				// Loading Member in Background Thread
				new LoadOnStartMember().execute();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	private void LoadMember() {
		mem_str = new String[MemberList.size()];
		for (int i = 0; i < MemberList.size(); i++) {

			HashMap<String, String> items = MemberList.get(i);

			mem_str[i] = items.get(Appconstant.TAG_MEM_NAME);
			Log.e("", "");
		}
		if (mem_str.length == 0) {
			SPINNER_FLAG = false;
		}

		final ArrayAdapter<String> adp1 = new ArrayAdapter<String>(this,
				R.layout.spn_txt, mem_str);
		// adp1.setDropDownViewResource(R.layout.spn_edittext);
		Mem_Spinner.setAdapter(adp1);

		Mem_Spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Toast.makeText(getApplicationContext(), mem_str[position],
						Toast.LENGTH_SHORT).show();
				HashMap<String, String> items = MemberList.get(position);
				Member_Id = items.get(Appconstant.TAG_MEM_ID);
				SPINNER_FLAG = true;

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	class mDateSetListener implements DatePickerDialog.OnDateSetListener {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			int mYear = year;
			int mMonth = monthOfYear;
			int mDay = dayOfMonth;

			date = new StringBuilder().append(mYear).append("/")
					.append(mMonth + 1).append("/").append(mDay).append(" ");

			if ("mem_admin".equals(mMyPreferences.getPreference("context"))
					|| ("self".equals(mMyPreferences.getPreference("context")))) {
				Member_Id = mMyPreferences.getPreference("Mem_Id");
				Grp_Id = mMyPreferences.getPreference("Grp_ID");
				callAddService();

			} else {
				selectGrpMember();
			}
		}
	}

	protected void callAddService() {

		prg = new ProgressDialog(NewAssignment.this);
		prg.setIndeterminate(true);
		prg.setMessage("Saving...");
		prg.setCanceledOnTouchOutside(false);
		prg.show();

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String str = AssignAssignment();
					Message msg = new Message();
					msg.obj = str.toString();
					mHandler.sendMessage(msg);
				} catch (Exception e) {
					NewAssignment.this.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(
									getApplicationContext(),
									"Cannot connect to a working"
											+ " internet connection",
									Toast.LENGTH_SHORT).show();
							prg.dismiss();
						}
					});
					e.printStackTrace();
				}
			}
		}).start();

		mHandler = new Handler() {

			public void handleMessage(Message msg) {

				String response = (String) msg.obj;
				map = new HashMap<String, String>();

				if (response != null) {

					JSONObject jsonObj;
					try {
						prg.dismiss();
						jsonObj = new JSONObject(response);
						JSONObject jsonObj1 = jsonObj.getJSONObject("data");
						String error_code = jsonObj1.getString("Error_Code");
						if ("1".equals(error_code)) {

							JSONArray jArray = jsonObj1
									.getJSONArray(Appconstant.TAG_RESULT);
							JSONObject JsonObj2 = jArray.getJSONObject(0);
							assgn_id = JsonObj2
									.getString(Appconstant.TAG_ASS_ID);
							mMyPreferences.setPreference("Ass_Id", assgn_id);
							String assg_name = JsonObj2
									.getString(Appconstant.TAG_ASS_NAME);
							String memname = JsonObj2
									.getString(Appconstant.TAG_MEM_NAME);
							String grpname = JsonObj2
									.getString(Appconstant.TAG_GRP_NAME);
							String created_date = JsonObj2
									.getString(Appconstant.TAG_DATE);
							String status = JsonObj2
									.getString(Appconstant.TAG_STATUS);
							String priority = JsonObj2
									.getString(Appconstant.TAG_PRIORITY);
							String assgn_type = JsonObj2
									.getString(Appconstant.TAG_TYPE);
							String recurrence = JsonObj2
									.getString(Appconstant.TAG_RECURRENCE);
							String description = JsonObj2
									.getString(Appconstant.TAG_DESC);
							String job_date = JsonObj2
									.getString(Appconstant.TAG_JOB_DATE);

							map.put(Appconstant.TAG_ASS_ID, assgn_id);
							map.put(Appconstant.TAG_ASS_NAME, assg_name);
							map.put(Appconstant.TAG_MEM_NAME, memname);
							map.put(Appconstant.TAG_GRP_NAME, grpname);
							map.put(Appconstant.TAG_DATE, created_date);
							map.put(Appconstant.TAG_STATUS, status);
							map.put(Appconstant.TAG_DESC, description);
							map.put(Appconstant.TAG_JOB_DATE, job_date);
							map.put(Appconstant.TAG_PRIORITY, priority);
							map.put(Appconstant.TAG_RECURRENCE, recurrence);
							map.put(Appconstant.TAG_TYPE, assgn_type);
							if (!"mem_admin".equals(mMyPreferences
									.getPreference("context"))) {
								Assign_Details.AssignmentList.add(map);
								Intent returnIntent = new Intent();
								setResult(RESULT_OK, returnIntent);
								finish();
							} else {
								Intent intent = new Intent(
										getApplicationContext(),
										Mem_Manage.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
								startActivity(intent);
								finish();
							}
						} else {
							Toast.makeText(getApplicationContext(),
									"Cannot save into database",
									Toast.LENGTH_LONG).show();
							Intent returnIntent = new Intent();
							setResult(RESULT_CANCELED, returnIntent);
							finish();
						}
					} catch (JSONException e) {

						e.printStackTrace();
					}
				}
			}
		};
	}

	private String AssignAssignment() {

		ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();

		nvp.add(new BasicNameValuePair("memberID", Member_Id));
		nvp.add(new BasicNameValuePair("assignmentName", name));
		nvp.add(new BasicNameValuePair("Priority", radio.getText().toString()));
		nvp.add(new BasicNameValuePair("assignmentType", type));
		nvp.add(new BasicNameValuePair("Description", description));
		nvp.add(new BasicNameValuePair("Status", "Pending"));

		if (date != null) {
			nvp.add(new BasicNameValuePair("JobDate", date.toString()));
		} else {
			nvp.add(new BasicNameValuePair("JobDate", "not applicable"));
		}
		if (recurrence != null) {
			nvp.add(new BasicNameValuePair("Recurrence", recurrence.getText()
					.toString()));
		} else {
			nvp.add(new BasicNameValuePair("Recurrence", "not applicable"));
		}

		String jsonResponse = null;
		String url = "http://phbjharkhand.in/AssignmentApplication/Add_Assignment_Details.php";
		Log.e("URL ", "is" + url);
		HttpParams httpParams = new BasicHttpParams();
		int timeout = (int) (30 * DateUtils.SECOND_IN_MILLIS);
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);

		try {
			if (url != null) {

				HttpPost httppost = new HttpPost(url);
				httppost.setEntity(new UrlEncodedFormEntity(nvp));

				HttpClient httpclient = new DefaultHttpClient();
				HttpResponse response = httpclient.execute(httppost);
				StatusLine status = response.getStatusLine();

				if (status.getStatusCode() == HttpStatus.SC_OK) {

					jsonResponse = EntityUtils.toString(response.getEntity());
					Log.e("_________feedback response_json_________",
							jsonResponse.toString());
				} else {

					jsonResponse = null;
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		} catch (SQLException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonResponse;
	}

	class LoadOnStartMember extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			prg = new ProgressDialog(NewAssignment.this);
			prg.setIndeterminate(true);
			prg.setMessage("Loading Members...");
			prg.setCanceledOnTouchOutside(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			// Building Parameters
			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			params1.add(new BasicNameValuePair("groupID", Grp_Id));

			String error_code = null;
			Log.e("URL ", "is" + MEMBER_URL);
			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(MEMBER_URL, "POST",
					params1);

			// Check your log cat for JSON response
			Log.d("Inbox JSON: ", json.toString());

			try {
				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
				MemberList.clear();
				if ("1".equals(error_code)) {

					JSONArray jsonArray = null;
					jsonArray = jsonObj.getJSONArray(Appconstant.TAG_RESULT);

					// looping through All messages
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject c = jsonArray.getJSONObject(i);

						map = new HashMap<String, String>();
						// Storing each json item in variable
						String id = c.getString(Appconstant.TAG_MEM_ID);
						String name = c.getString(Appconstant.TAG_MEM_NAME);
						String date = c.getString(Appconstant.TAG_DATE);
						String contact = c.getString(Appconstant.TAG_CONTACT);
						String age = c.getString(Appconstant.TAG_AGE);
						String sex = c.getString(Appconstant.TAG_SEX);
						String email = c.getString(Appconstant.TAG_EMAIL);
						String address = c.getString(Appconstant.TAG_ADDRESS);
						String password = c.getString(Appconstant.TAG_PASS);
						String username = c.getString(Appconstant.TAG_UNAME);
						String type = c.getString(Appconstant.TAG_TYPE);

						// adding each child node to HashMap key => value
						map.put(Appconstant.TAG_MEM_ID, id);
						map.put(Appconstant.TAG_MEM_NAME, name);
						map.put(Appconstant.TAG_DATE, date);
						map.put(Appconstant.TAG_AGE, age);
						map.put(Appconstant.TAG_SEX, sex);
						map.put(Appconstant.TAG_CONTACT, contact);
						map.put(Appconstant.TAG_EMAIL, email);
						map.put(Appconstant.TAG_ADDRESS, address);
						map.put(Appconstant.TAG_PASS, password);
						map.put(Appconstant.TAG_UNAME, username);
						map.put(Appconstant.TAG_TYPE, type);
						// adding HashList to ArrayList
						MemberList.add(map);
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

			super.onPostExecute(error_num);

			// dismiss the dialog after getting all products
			if (prg.isShowing()) {
				prg.dismiss();
			}

			if (error_num == 2) {

				LoadMember();
			} else {

				LoadMember();
			}
		}
	}

	@Override
	public void onBackPressed() {
	}
}