package com.techila.assign_management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

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

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.costum.android.widget.LoadMoreListView.OnLoadMoreListener;
import com.techila.assign_management.adapter.CustomAdapterListView;
import com.techila.assign_management.config.Appconstant;
import com.techila.assign_management.util.JSONParser;
import com.techila.assign_management.util.PrefSingleton;

public class Assign_Details extends ListActivity {

	Button remove;
	private PrefSingleton mMyPreferences;
	EditText et_name, et_desc, search;
	RadioButton radio = null, recurrence = null;
	Button addAssignment, select_recurrence, getStatus;
	TextView details;
	RadioGroup et_priority, recurrence_rd;
	Spinner type_sp, type_recurrence;
	String name, type, description, Member_Id, assgn_id;
	private int priority, recc;
	protected Handler mHandler;
	ProgressDialog prg;
	StringBuilder date;

	JSONParser jsonParser = new JSONParser();

	// DEFINING AN ARRAYLIST OF HASHMAP
	ArrayList<HashMap<String, String>> AssignmentList;

	// Creating a clone of the arraylist
	ArrayList<HashMap<String, String>> CloneList;

	// creating new HashMap
	HashMap<String, String> map = null;

	// Group JSON url
	private static final String ASSIGNMENT_URL = "http://phbjharkhand.in/AssignmentApplication/Get_All_Assignment_Details.php";

	CustomAdapterListView adapter1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		setContentView(R.layout.activity_assign_manage);

		details = (TextView) findViewById(R.id.viewDetails);
		addAssignment = (Button) findViewById(R.id.addBtn);
		getStatus = (Button) findViewById(R.id.get_all_status);
		search = (EditText) findViewById(R.id.search);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());
		search = (EditText) findViewById(R.id.search);

		// Hashmap for listview
		AssignmentList = new ArrayList<HashMap<String, String>>();

		Member_Id = mMyPreferences.getPreference("Mem_Id");

		ListView lv = getListView();
		adapter1 = new CustomAdapterListView(this, AssignmentList);
		lv.setAdapter(adapter1);

		getStatus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(getApplicationContext(),
						AssignTabListView.class);
				startActivity(intent);
			}
		});

		// Loading Member in Background Thread
		new LoadOnStartAssignment().execute();

		if (mMyPreferences.getPreference("Context").equals("Assignee")) {

			ViewGroup layout = (ViewGroup) addAssignment.getParent();
			if (layout != null) {

				layout.removeView(addAssignment);

			}
		} else if (mMyPreferences.getPreference("Context").equals("Admin")) {

			addAssignment.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					addItems();
				}
			});
		}
	}

	// METHOD WHICH WILL HANDLE DYNAMIC INSERTION
	public void addItems() {

		showAssignAssignmentDialog();
	}

	public void showAssignAssignmentDialog() {

		final AlertDialog.Builder alertdialog = new AlertDialog.Builder(
				Assign_Details.this);
		alertdialog.setTitle("Assign New Assignment");
		alertdialog.setCancelable(false);
		LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = layoutInflater.inflate(
				R.layout.dialog_assign_assignment, null);

		et_name = (EditText) view.findViewById(R.id.tx_aname);
		et_priority = (RadioGroup) view.findViewById(R.id.tx_priority);
		type_sp = (Spinner) view.findViewById(R.id.sp_type);
		et_desc = (EditText) view.findViewById(R.id.tx_desc);

		alertdialog.setPositiveButton("Save",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						type = String.valueOf(type_sp.getSelectedItem());
						name = et_name.getText().toString().trim();
						description = et_desc.getText().toString().trim();
						priority = et_priority.getCheckedRadioButtonId();
						radio = (RadioButton) view.findViewById(priority);

						try {

							if (name.length() == 0 || description.length() == 0) {
								AlertDialog.Builder alert = new AlertDialog.Builder(
										Assign_Details.this);
								alert.setTitle("Invalid !");
								alert.setCancelable(false);
								alert.setMessage("Please enter all the fields");
								((ViewGroup) view.getParent()).removeAllViews();
								alert.setPositiveButton("OK",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// TODO Auto-generated method
												// stub
												dialog.dismiss();
												alertdialog.show();
											}
										});
								alert.show();

							} else {

								if (type.equals("Short Term Job")
										|| type.equals("Long Term Job")) {

									final AlertDialog.Builder alert = new AlertDialog.Builder(
											Assign_Details.this);
									alert.setCancelable(false);
									alert.setTitle("Select the recurrence interval");

									LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext()
											.getSystemService(
													Context.LAYOUT_INFLATER_SERVICE);
									final View view1 = layoutInflater.inflate(
											R.layout.dialog_recurrence, null);
									recurrence_rd = (RadioGroup) view1
											.findViewById(R.id.tx_recurrence);
									((ViewGroup) view.getParent())
											.removeAllViews();

									alert.setPositiveButton(
											"OK",
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													recc = recurrence_rd
															.getCheckedRadioButtonId();
													recurrence = (RadioButton) view1
															.findViewById(recc);
													dialog.dismiss();
													callAddService();
												}
											});

									alert.setNegativeButton(
											"Cancel",
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													// TODO Auto-generated
													// method stub
													dialog.dismiss();
												}
											});
									alert.setView(view1);
									alert.show();

								} else if (type.equals("Specific Date Job")) {

									Calendar c = Calendar.getInstance();
									int mYear = c.get(Calendar.YEAR);
									int mMonth = c.get(Calendar.MONTH);
									int mDay = c.get(Calendar.DAY_OF_MONTH);
									System.out.println("the selected " + mDay);
									DatePickerDialog date = new DatePickerDialog(
											Assign_Details.this,
											new mDateSetListener(), mYear,
											mMonth, mDay);
									date.show();

								} else {

									callAddService();
								}
								dialog.cancel();
							}// end of else

						} catch (Exception e) {
							e.printStackTrace();
							Log.v("exception", e.toString());
						}
					}
				});

		alertdialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();

					}
				});

		alertdialog.setView(view);
		alertdialog.show();

	}

	protected void callAddService() {
		prg = new ProgressDialog(Assign_Details.this);
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
					Assign_Details.this.runOnUiThread(new Runnable() {
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
							map.put(Appconstant.TAG_DATE, created_date);
							map.put(Appconstant.TAG_STATUS, status);
							map.put(Appconstant.TAG_DESC, description);
							map.put(Appconstant.TAG_JOB_DATE, job_date);
							map.put(Appconstant.TAG_PRIORITY, priority);
							map.put(Appconstant.TAG_RECURRENCE, recurrence);
							map.put(Appconstant.TAG_TYPE, assgn_type);

							AssignmentList.add(map);
							// Adapter once initialized
							// does not need to be set
							// always
							// We only need to update
							// the adapter by notifying
							// it.
							adapter1.notifyDataSetChanged();
						} else {
							Toast.makeText(getApplicationContext(),
									"Cannot save into database",
									Toast.LENGTH_LONG).show();
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch
						// block
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

			// Building Parameters
			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			params1.add(new BasicNameValuePair("memberID", Member_Id));

			String error_code = null;

			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(ASSIGNMENT_URL,
					"POST", params1);

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
						"There are no assignments assigned to this member",
						Toast.LENGTH_LONG).show();
			}

			/**
			 * Updating parsed JSON data into ListView
			 * */

			// updating listview
			adapter1.notifyDataSetChanged();

			/*
			 * lv.setOnScrollListener(new OnScrollListener() {
			 * 
			 * @Override public void onScrollStateChanged(AbsListView view, int
			 * scrollState) {
			 * 
			 * int threshold = 1; int count = lv.getCount();
			 * 
			 * if (scrollState == SCROLL_STATE_IDLE) { if
			 * (lv.getLastVisiblePosition() >= count - threshold) {
			 * LoadMoreListView();
			 * 
			 * } } }
			 * 
			 * @Override public void onScroll(AbsListView view, int
			 * firstVisibleItem, int visibleItemCount, int totalItemCount) { }
			 * });
			 */
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
			// TODO Auto-generated method stub

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
						"There are no groups assigned", Toast.LENGTH_LONG)
						.show();
			}

			/**
			 * Updating parsed JSON data into ListView
			 * */
			adapter1.notifyDataSetChanged();

			/*
			 * ((com.costum.android.widget.LoadMoreListView) lv)
			 * .onLoadMoreComplete();
			 */
		}

		/*
		 * @Override protected void onCancelled() { // Notify the loading more
		 * operation has finished ((com.costum.android.widget.LoadMoreListView)
		 * lv) .onLoadMoreComplete(); }
		 */
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

			callAddService();
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
					adapter1.notifyDataSetChanged();
				}
			}
		});
	}
}