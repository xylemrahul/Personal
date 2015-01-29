package com.techila.july.assign_management;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.techila.july.assign_management.config.Appconstant;
import com.techila.july.assign_management.util.JSONParser;
import com.techila.july.assign_management.util.PrefSingleton;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class PerformanceFragment extends Fragment {

	private String[] grp_str, mem_str;
	private String grp_name, mem_name, s_date, e_date;
	private EditText start_date, end_date;
	private Button submit;
	ArrayList<HashMap<String, String>> MemberList;
	ArrayList<HashMap<String, String>> GroupList;
	Spinner Grp_Spinner, Mem_Spinner;
	private String Grp_Id, Member_Id;
	private static final String MEMBER_URL = "http://phbjharkhand.in/AssignmentApplication/Get_Member_Details.php";
	private static final String PERFORMANCE_URL = "http://phbjharkhand.in/AssignmentApplication/Get_Performance_Report.php";
	private ProgressDialog prg;
	private static Boolean SPINNER_FLAG = false;
	protected PrefSingleton mMyPreferences;

	static final int DATE_DIALOG_ID = 0;
	private int mYear, mMonth, mDay;

	JSONParser jsonParser = new JSONParser();
	HashMap<String, String> map;

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_perf_report,
				container, false);

		Grp_Spinner = (Spinner) rootView.findViewById(R.id.grp_sp_type);
		Mem_Spinner = (Spinner) rootView.findViewById(R.id.mem_sp_type);
		submit = (Button) rootView.findViewById(R.id.btn_submit);
		start_date = (EditText) rootView.findViewById(R.id.dt_start);
		end_date = (EditText) rootView.findViewById(R.id.dt_end);

		start_date.setInputType(InputType.TYPE_NULL);
		end_date.setInputType(InputType.TYPE_NULL);

		GroupList = (ArrayList<HashMap<String, String>>) getArguments()
				.getSerializable("List");
		MemberList = new ArrayList<HashMap<String, String>>();

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getActivity());

		final Calendar c = Calendar.getInstance();

		LoadGrp();

		start_date.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mYear = c.get(Calendar.YEAR);
				mMonth = c.get(Calendar.MONTH);
				mDay = c.get(Calendar.DAY_OF_MONTH);

				DatePickerDialog mDatePicker = new DatePickerDialog(
						getActivity(), mDateSetListener, mYear, mMonth, mDay);
				mDatePicker.setCanceledOnTouchOutside(false);
				mDatePicker.getDatePicker().setCalendarViewShown(false);
				mDatePicker.getDatePicker().setMaxDate(
						System.currentTimeMillis());
				mDatePicker.setTitle("Select date");
				mDatePicker.show();
			}
		});

		end_date.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mYear = c.get(Calendar.YEAR);
				mMonth = c.get(Calendar.MONTH);
				mDay = c.get(Calendar.DAY_OF_MONTH);

				DatePickerDialog mDatePicker = new DatePickerDialog(
						getActivity(), mDateSetListener1, mYear, mMonth, mDay);
				mDatePicker.setCanceledOnTouchOutside(false);
				mDatePicker.getDatePicker().setCalendarViewShown(false);
				mDatePicker.getDatePicker().setMaxDate(
						System.currentTimeMillis());
				mDatePicker.setTitle("Select date");
				mDatePicker.show();
			}
		});

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (SPINNER_FLAG) {
					getReports();
				} else {
					Toast.makeText(getActivity(),
							"No members assigned to this group",
							Toast.LENGTH_SHORT).show();
				}

			}
		});
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;

			updateStartDate();

		}
	};
	private DatePickerDialog.OnDateSetListener mDateSetListener1 = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;

			updateEndDate();

		}
	};

	// updates the date in the EditText
	private void updateStartDate() {
		start_date.setText(getString(R.string.strSelectedDate,
				new StringBuilder()

				.append(mYear).append("-").append(mMonth + 1).append("-")
						.append(mDay).append(" ")));
	}

	private void updateEndDate() {
		end_date.setText(getString(R.string.strSelectedDate,
				new StringBuilder()

				.append(mYear).append("-").append(mMonth + 1).append("-")
						.append(mDay).append(" ")));
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

		ArrayAdapter<String> adp1 = new ArrayAdapter<String>(getActivity(),
				R.layout.spn_txt, grp_str);
		// adp1.setDropDownViewResource(R.layout.spn_edittext);
		Grp_Spinner.setAdapter(adp1);
		Grp_Spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Toast.makeText(getActivity(), grp_str[position],
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

		final ArrayAdapter<String> adp1 = new ArrayAdapter<String>(
				getActivity(), R.layout.spn_txt, mem_str);
		// adp1.setDropDownViewResource(R.layout.spn_edittext);
		Mem_Spinner.setAdapter(adp1);
		Mem_Spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Toast.makeText(getActivity(), mem_str[position],
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

	private void getReports() {

		grp_name = Grp_Spinner.getSelectedItem().toString();
		mem_name = Mem_Spinner.getSelectedItem().toString();
		s_date = start_date.getText().toString().trim();
		e_date = end_date.getText().toString().trim();

		if (grp_name.equals("") || mem_name.equals("") || s_date.equals("")
				|| e_date.equals("")) {

			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			alert.setTitle("Alert!");
			alert.setCancelable(false);
			alert.setMessage("Please select all fields");
			alert.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			alert.show();
		} else {
			new FetchReports().execute();
		}
	}

	class LoadOnStartMember extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			prg = new ProgressDialog(getActivity());
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
				Toast.makeText(getActivity(),
						"No members assigned to this group",
						Toast.LENGTH_SHORT).show();
				LoadMember();
			} else {

				LoadMember();
			}
		}
	}

	class FetchReports extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();

			prg = new ProgressDialog(getActivity());
			prg.setIndeterminate(true);
			prg.setMessage("Fetching Reports...");
			prg.setCanceledOnTouchOutside(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			List<NameValuePair> params1 = new ArrayList<NameValuePair>();

			params1.add(new BasicNameValuePair("GroupId", Grp_Id));
			params1.add(new BasicNameValuePair("MemberId", Member_Id));
			params1.add(new BasicNameValuePair("FromDate", s_date));
			params1.add(new BasicNameValuePair("ToDate", e_date));

			String error_code = null;
			Log.e("URL ", "is" + MEMBER_URL);
			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(PERFORMANCE_URL,
					"POST", params1);
			// Check your log cat for JSON response
			Log.d("Inbox JSON: ", json.toString());
			try {
				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");

			} catch (Exception e) {
				e.printStackTrace();
			}
			return Integer.parseInt(error_code);
		}

		@Override
		protected void onPostExecute(Integer error_num) {
			
			super.onPostExecute(error_num);

			if (prg.isShowing()) {
				prg.dismiss();
			}

			if (error_num == 2) {
				Toast.makeText(getActivity(), "2", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
