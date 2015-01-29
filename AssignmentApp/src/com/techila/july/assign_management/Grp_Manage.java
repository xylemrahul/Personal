package com.techila.july.assign_management;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.costum.android.widget.LoadMoreListView.*;
import com.techila.july.assign_management.R;
import com.techila.july.assign_management.adapter.CustomAdapterListView;
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
import java.util.HashMap;
import java.util.List;

public class Grp_Manage extends ListActivity {

	Button remove;
	private PrefSingleton mMyPreferences;
	EditText et_name, et_desc, et_abbr, search;
	ProgressDialog prg;
	RadioButton radio;
	TextView details;
	RadioGroup et_priority;
	Spinner type_sp, recurrence_sp;
	String name, description, grp_name, grp_id, abbreviation;
	String[] str;
	Button addGrp;
	int priority;
	protected Handler mHandler;
	com.costum.android.widget.LoadMoreListView lv;

	// Creating JSON Parser object
	JSONParser jsonParser = new JSONParser();

	// DEFINING AN ARRAYLIST OF HASHMAP
	ArrayList<HashMap<String, String>> GroupList;

	// Creating a clone of the arraylist
	ArrayList<HashMap<String, String>> CloneList;

	// creating new HashMap
	HashMap<String, String> map = null;

	// Group JSON url
	private static final String GROUP_URL = "http://phbjharkhand.in/AssignmentApplication/Get_Group_Details.php";

	CustomAdapterListView adapter1;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		setContentView(R.layout.activity_grp_manage);

		addGrp = (Button) findViewById(R.id.addBtn);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());
		search = (EditText) findViewById(R.id.search);

		// Hashmap for ListView
		GroupList = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("List");

		lv = (com.costum.android.widget.LoadMoreListView) getListView();
		// Pass the results into CustomAdapterListView.java
		adapter1 = new CustomAdapterListView(Grp_Manage.this, GroupList);
		
		/**
		 * Updating parsed JSON data into ListView
		 * */
		lv.setAdapter(adapter1);
		
		setScrollLoad();

		addGrp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				addItems();
			}
		});
	}

	// METHOD WHICH WILL HANDLE DYNAMIC INSERTION
	public void addItems() {

		showaddGroupDialog();
	}

	public void showaddGroupDialog() {

		final AlertDialog.Builder alertdialog = new AlertDialog.Builder(
				Grp_Manage.this);
		alertdialog.setTitle("Assign New Group");
		alertdialog.setCancelable(false);
		LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = layoutInflater.inflate(R.layout.dialog_add_grp, null);

		et_name = (EditText) view.findViewById(R.id.tx_cname);
		et_desc = (EditText) view.findViewById(R.id.tx_desc);
		et_abbr = (EditText) view.findViewById(R.id.tx_abbreviation);

		alertdialog.setPositiveButton("Save",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						try {
							name = et_name.getText().toString();
							description = et_desc.getText().toString();
							abbreviation = et_abbr.getText().toString();

							if (name.length() == 0 || description.length() == 0
									|| abbreviation.length() == 0) {
								AlertDialog.Builder alert = new AlertDialog.Builder(
										Grp_Manage.this);
								alert.setCancelable(false);
								alert.setTitle("Invalid !");
								((ViewGroup) view.getParent()).removeView(view);
								alert.setMessage("Please enter all the fields");
								alert.setPositiveButton("OK",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.dismiss();
												alertdialog.show();
											}
										});
								alert.show();

							} else {

								try {

									prg = new ProgressDialog(Grp_Manage.this);
									prg.setIndeterminate(true);
									prg.setMessage("Saving...");
									prg.setCanceledOnTouchOutside(false);
									prg.show();

									new Thread(new Runnable() {

										@Override
										public void run() {
											try {
												String str = createGrp();
												Message msg = new Message();
												msg.obj = str.toString();
												mHandler.sendMessage(msg);
											} catch (Exception e) {
												Grp_Manage.this
														.runOnUiThread(new Runnable() {
															public void run() {
																Toast.makeText(
																		getApplicationContext(),
																		"Cannot connect to a working internet connection",
																		Toast.LENGTH_SHORT)
																		.show();
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
													jsonObj = new JSONObject(
															response);
													JSONObject jsonObj1 = jsonObj
															.getJSONObject("data");
													String error_code = jsonObj1
															.getString("Error_Code");
													if ("1".equals(error_code)) {

														JSONArray jsonArray = jsonObj1
																.getJSONArray("result");
														JSONObject jsonObj2 = jsonArray
																.getJSONObject(0);
														grp_id = jsonObj2
																.getString(Appconstant.TAG__GRP_ID);
														String grp_name = jsonObj2
																.getString(Appconstant.TAG_GRP_NAME);
														String created_date = jsonObj2
																.getString(Appconstant.TAG_DATE);
														String abbr = jsonObj2
																.getString(Appconstant.TAG_ABBR);
														String desc = jsonObj2
																.getString(Appconstant.TAG_DESC);
														map.put(Appconstant.TAG__GRP_ID,
																grp_id);
														map.put(Appconstant.TAG_GRP_NAME,
																grp_name);
														map.put(Appconstant.TAG_DATE,
																created_date);
														map.put(Appconstant.TAG_ABBR,
																abbr);
														map.put(Appconstant.TAG_DESC,
																desc);
														GroupList.add(map);

														// updating listview
														adapter1.notifyDataSetChanged();
													} else {
														Toast.makeText(
																getApplicationContext(),
																"Cannot save into database",
																Toast.LENGTH_LONG)
																.show();
													}
												} catch (JSONException e) {

													e.printStackTrace();
												} catch (Exception e) {

													e.printStackTrace();
												}
											}
										}
									};
									dialog.dismiss();
								} catch (Exception e) {
									e.printStackTrace();
								}

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

	private String createGrp() {

		ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();

		nvp.add(new BasicNameValuePair("groupName", name));
		nvp.add(new BasicNameValuePair("description", description));
		nvp.add(new BasicNameValuePair("Abbreviation", abbreviation));

		String jsonResponse = null;
		String url = "http://phbjharkhand.in/AssignmentApplication/Add_Group_Details.php";
		Log.e("URL ", "is"+url);
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

	@SuppressWarnings("unchecked")
	private void setScrollLoad() {

		lv.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

				int threshold = 1;
				int count = lv.getCount();

				if (scrollState == SCROLL_STATE_IDLE) {
					if (lv.getLastVisiblePosition() >= count - threshold) {
						LoadMoreListView();

					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});

		try {
			CloneList = (ArrayList<HashMap<String, String>>) GroupList.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
		searchItems();
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
		protected Integer doInBackground(Void... params) {
			// Building Parameters
			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			params1.add(new BasicNameValuePair("scrollType", "yes"));
			String error_code = null;
			
			Log.e("URL ", "is"+GROUP_URL);
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
					// looping through All messages
					GroupList.clear();
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

		protected void onPostExecute(Integer error_num) {

			if (error_num == 2) {
				Toast.makeText(getApplicationContext(),
						"There are no groups assigned", Toast.LENGTH_SHORT)
						.show();
			}

			/**
			 * Updating parsed JSON data into ListView
			 * */
			adapter1.notifyDataSetChanged();

			((com.costum.android.widget.LoadMoreListView) lv)
					.onLoadMoreComplete();

		}

		@Override
		protected void onCancelled() {
			// Notify the loading more operation has finished
			((com.costum.android.widget.LoadMoreListView) lv)
					.onLoadMoreComplete();
		}
	}

	protected void searchItems() {

		search.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				String clonListTagName = null;
				GroupList.clear();
				for (int i = 0; i < CloneList.size(); i++) {

					// Log.e("",
					// ""+CloneList.get(i).get(TAG_GRP_NAME).toUpperCase().startsWith(s.toString()));
					clonListTagName = CloneList.get(i)
							.get(Appconstant.TAG_GRP_NAME).toLowerCase();
					if (clonListTagName.startsWith(s.toString().toLowerCase())) {
						if (GroupList.contains(Appconstant.TAG_GRP_NAME)) {
							Log.e("inside", "if");
						} else {
							Log.e("inside", "else");
							GroupList.add(CloneList.get(i));
						}
						Log.e("", "OnTextChanged");
					}
					if (GroupList.isEmpty()) {
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