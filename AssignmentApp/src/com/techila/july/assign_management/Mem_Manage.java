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
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mem_Manage extends ListActivity {

	private PrefSingleton mMyPreferences;
	ProgressDialog prg;
	EditText Mem_name, contact_no, mem_age, mail_id, postal_add, pass, search;
	String name, phone, age, email, address, password, Grp_Id, type,
			abbreviation, mem_id, Member_Id;
	String[] str;
	RadioButton sex;
	TextView details;
	RadioGroup gender;
	public boolean emailcheck = true;
	Spinner sp_type, Grp_Spinner;
	Button addMember;
	int radio;

	private Handler mHandler;

	JSONParser jsonParser = new JSONParser();

	// DEFINING AN ARRAYLIST OF HASHMAP
	ArrayList<HashMap<String, String>> MemberList;
	ArrayList<HashMap<String, String>> GroupList;
	// Creating a clone of the arraylist
	ArrayList<HashMap<String, String>> CloneList;

	// creating new HashMap
	HashMap<String, String> map = null;

	// Group JSON url
	private static final String MEMBER_URL = "http://phbjharkhand.in/AssignmentApplication/Get_Member_Details.php";

	CustomAdapterListView adapter1;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		setContentView(R.layout.activity_member_manage);

		addMember = (Button) findViewById(R.id.addBtn);
		details = (TextView) findViewById(R.id.viewDetails);
		search = (EditText) findViewById(R.id.search);
		Grp_Spinner = (Spinner) findViewById(R.id.grp_sp_type);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());

//		-----------------Loading the items in the spinner---------------------
		
		GroupList = (ArrayList<HashMap<String, String>>) getIntent()
				.getSerializableExtra("List");

		str = new String[GroupList.size()];
		for(int i = 0 ; i < GroupList.size() ; i++){
			
			HashMap<String, String> items = GroupList.get(i);
			
			str[i] = items.get(Appconstant.TAG_GRP_NAME);
			Log.e("", "");
		}
		
		ArrayAdapter<String> adp1=new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,str);
		adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Grp_Spinner.setAdapter(adp1);
		
//		------------------------------xxxxxxxx---------------------------
		
		Grp_Spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Toast.makeText(getApplicationContext(), str[position],Toast.LENGTH_SHORT).show();
				HashMap<String, String> items = GroupList.get(position);
				Grp_Id = items.get(Appconstant.TAG__GRP_ID);
				// Loading Member in Background Thread
				new LoadOnStartMember().execute();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
//		Member_Id = mMyPreferences.getPreference("Mem_Id_Login");
		Member_Id = mMyPreferences.getPreference("Mem_Id");

		abbreviation = mMyPreferences.getPreference("Abb");
		
		// Hashmap for ListView
		MemberList = new ArrayList<HashMap<String, String>>();

		ListView lv = getListView();
		adapter1 = new CustomAdapterListView(this, MemberList);
		lv.setAdapter(adapter1);

		/*// Loading Member in Background Thread
		new LoadOnStartMember().execute();*/

		addMember.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				addItems();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	// METHOD WHICH WILL HANDLE DYNAMIC INSERTION
	public void addItems() {

		showAddMembersDialog();
	}

	private void showAddMembersDialog() {

		final AlertDialog.Builder alertdialog = new AlertDialog.Builder(
				Mem_Manage.this);
		alertdialog.setTitle("Add New Member");
		alertdialog.setCancelable(false);
		LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final View view = layoutInflater.inflate(R.layout.dialog_add_member,
				null);

		Mem_name = (EditText) view.findViewById(R.id.tx_mname);
		contact_no = (EditText) view.findViewById(R.id.tx_contact);
		mem_age = (EditText) view.findViewById(R.id.tx_age);
		mail_id = (EditText) view.findViewById(R.id.tx_email);
		postal_add = (EditText) view.findViewById(R.id.tx_address);
		pass = (EditText) view.findViewById(R.id.tx_pass);
		sp_type = (Spinner) view.findViewById(R.id.type_sp);
		gender = (RadioGroup) view.findViewById(R.id.tx_sex);

		alertdialog.setPositiveButton("Save",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						try {

							name = Mem_name.getText().toString();
							phone = contact_no.getText().toString();
							age = mem_age.getText().toString();
							radio = gender.getCheckedRadioButtonId();
							sex = (RadioButton) view.findViewById(radio);
							email = mail_id.getText().toString();
							address = postal_add.getText().toString();
							password = pass.getText().toString();
							type = String.valueOf(sp_type.getSelectedItem());

							if (name.length() == 0 || phone.length() == 0
									|| age.length() == 0 || email.length() == 0
									|| address.length() == 0
									|| password.length() == 0) {

								AlertDialog.Builder alert = new AlertDialog.Builder(
										Mem_Manage.this);
								alert.setCancelable(false);
								alert.setTitle("Invalid !");
								alert.setMessage("Please enter all the fields");
								((ViewGroup) view.getParent()).removeView(view);
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

								checkemail(email);
								if (emailcheck == false) {
									Toast.makeText(getApplicationContext(),
											"Please enter valid email address",
											Toast.LENGTH_SHORT).show();
								} else {

									prg = new ProgressDialog(Mem_Manage.this);
									prg.setIndeterminate(true);
									prg.setMessage("Saving...");
									prg.setCanceledOnTouchOutside(false);
									prg.show();

									new Thread(new Runnable() {

										@Override
										public void run() {
											try {
												String str = createMember();
												Message msg = new Message();
												msg.obj = str.toString();
												mHandler.sendMessage(msg);
											} catch (Exception e) {
												Mem_Manage.this
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
														Toast.makeText(
																getApplicationContext(),
																"The username and password has been succesfully"
																		+ " sent to the registered Email Address",
																Toast.LENGTH_SHORT)
																.show();
														JSONArray jsonArray = jsonObj1
																.getJSONArray("result");
														JSONObject jsonObj2 = jsonArray
																.getJSONObject(0);
														mem_id = jsonObj2
																.getString("memberID");
														String mem_name = jsonObj2
																.getString("memberName");
														String createdDate = jsonObj2
																.getString("createdDate");
														String age = jsonObj2
																.getString(Appconstant.TAG_AGE);
														String sex = jsonObj2
																.getString(Appconstant.TAG_SEX);
														String contact = jsonObj2
																.getString(Appconstant.TAG_CONTACT);
														String email = jsonObj2
																.getString(Appconstant.TAG_EMAIL);
														String address = jsonObj2
																.getString(Appconstant.TAG_ADDRESS);
														String password = jsonObj2
																.getString(Appconstant.TAG_PASS);
														String username = jsonObj2
																.getString(Appconstant.TAG_UNAME);
														String type = jsonObj2
																.getString(Appconstant.TAG_TYPE);
														map.put(Appconstant.TAG_MEM_ID,
																mem_id);
														map.put(Appconstant.TAG_MEM_NAME,
																mem_name);
														map.put(Appconstant.TAG_DATE,
																createdDate);
														map.put(Appconstant.TAG_AGE,
																age);
														map.put(Appconstant.TAG_SEX,
																sex);
														map.put(Appconstant.TAG_CONTACT,
																contact);
														map.put(Appconstant.TAG_EMAIL,
																email);
														map.put(Appconstant.TAG_ADDRESS,
																address);
														map.put(Appconstant.TAG_PASS,
																password);
														map.put(Appconstant.TAG_UNAME,
																username);
														map.put(Appconstant.TAG_TYPE,
																type);
														MemberList.add(map);

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
									dialog.cancel();
								}
							}
						} catch (Exception e) {

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

	public void checkemail(String email) {
		Pattern pattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
		Matcher matcher = pattern.matcher(email);
		emailcheck = matcher.matches();
	}

	private String createMember() {

		ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();

		nvp.add(new BasicNameValuePair("GroupID", "" + Grp_Id));
		nvp.add(new BasicNameValuePair("MemberID", "" + Member_Id));
		nvp.add(new BasicNameValuePair("MemberName", name));
		nvp.add(new BasicNameValuePair("Phone", phone));
		nvp.add(new BasicNameValuePair("Age", age));
		nvp.add(new BasicNameValuePair("Sex", sex.getText().toString()));
		nvp.add(new BasicNameValuePair("EmailID", email));
		nvp.add(new BasicNameValuePair("Address", address));
		nvp.add(new BasicNameValuePair("Password", password));
		nvp.add(new BasicNameValuePair("Type", type));
		nvp.add(new BasicNameValuePair("Abbreviation", abbreviation));

		String jsonResponse = null;
		String url = "http://phbjharkhand.in/AssignmentApplication/Add_Member_Details.php";
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

	class LoadOnStartMember extends AsyncTask<Void, String, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(Mem_Manage.this);
			prg.setMessage("Loading List ...");
			prg.setIndeterminate(false);
			prg.setCancelable(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			// Building Parameters
			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			params1.add(new BasicNameValuePair("groupID", Grp_Id));

			String error_code = null;
			Log.e("URL ", "is"+MEMBER_URL);
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
						String grpname = c.getString(Appconstant.TAG_GRP_NAME);
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
						map.put(Appconstant.TAG_GRP_NAME, grpname);
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
						"There are no members assigned to this group",
						Toast.LENGTH_SHORT).show();
			}
			/**
			 * Updating parsed JSON data into ListView
			 * */

			// updating listview
			adapter1.notifyDataSetChanged();
			
		}
	}
}