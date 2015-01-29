package com.techila.boa.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.techila.boa.R;
import com.techila.boa.ViewReports;
import com.techila.boa.config.Appconstant;
import com.techila.boa.util.JSONParser;
import com.techila.boa.util.PrefSingleton;

public class GenReport extends Fragment implements OnClickListener {

	private EditText start_date, end_date;
	Spinner User_Spinner;
	Button submit;
	Calendar c;
	String CompanyId, User_id;
	protected PrefSingleton mMyPreferences;
	private ProgressDialog prg;
	JSONParser jsonParser = new JSONParser();
	HashMap<String, String> map;
	private static final int DATE_DIALOG_ID = 0;
	private int mYear, mMonth, mDay;
	private boolean SPINNER_FLAG;
	String paymentType;

	ArrayList<HashMap<String, String>> UserList;
	ArrayList<HashMap<String, String>> ReportList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.pay_report, container, false);

		initViews(rootView);
		return rootView;
	}

	public void initViews(View rootView) {

		User_Spinner = (Spinner) rootView.findViewById(R.id.usr_sp_type);
		submit = (Button) rootView.findViewById(R.id.btn_submit);
		start_date = (EditText) rootView.findViewById(R.id.dt_start);
		end_date = (EditText) rootView.findViewById(R.id.dt_end);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getActivity());

		start_date.setInputType(InputType.TYPE_NULL);
		end_date.setInputType(InputType.TYPE_NULL);
		c = Calendar.getInstance();

		CompanyId = mMyPreferences.getPreference("C_ID");
		paymentType = mMyPreferences.getPreference("flag");

		new getUserDetails().execute();

		UserList = new ArrayList<HashMap<String, String>>();
		ReportList = new ArrayList<HashMap<String, String>>();

		start_date.setOnClickListener(this);
		end_date.setOnClickListener(this);
		submit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.dt_start:

			mYear = c.get(Calendar.YEAR);
			mMonth = c.get(Calendar.MONTH);
			mDay = c.get(Calendar.DAY_OF_MONTH);

			DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(),
					mDateSetListener, mYear, mMonth, mDay);
			mDatePicker.setCanceledOnTouchOutside(false);
			mDatePicker.getDatePicker().setCalendarViewShown(false);
			mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
			mDatePicker.setTitle("Select date");
			mDatePicker.show();
			break;

		case R.id.dt_end:

			mYear = c.get(Calendar.YEAR);
			mMonth = c.get(Calendar.MONTH);
			mDay = c.get(Calendar.DAY_OF_MONTH);

			DatePickerDialog mDatePicker1 = new DatePickerDialog(getActivity(),
					mDateSetListener1, mYear, mMonth, mDay);
			mDatePicker1.setCanceledOnTouchOutside(false);
			mDatePicker1.getDatePicker().setCalendarViewShown(false);
			mDatePicker1.getDatePicker().setMaxDate(System.currentTimeMillis());
			mDatePicker1.setTitle("Select date");
			mDatePicker1.show();

			break;

		case R.id.btn_submit:

			getReports();
			if (SPINNER_FLAG) {
				new GetReports().execute();
			}
		}
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
				new StringBuilder().append(mYear).append("-")
						.append(mMonth + 1).append("-").append(mDay)
						.append(" ")));
	}

	private void LoadUser() {

		// -----------------Loading the items in the
		// spinner---------------------
		final String[] user_str = new String[UserList.size()];
		for (int i = 0; i < UserList.size(); i++) {

			HashMap<String, String> items = UserList.get(i);

			user_str[i] = items.get(Appconstant.TAG_FNAME);
			Log.e("", "");
		}

		ArrayAdapter<String> adp1 = new ArrayAdapter<String>(getActivity(),
				R.layout.spn_txt, user_str);
		// adp1.setDropDownViewResource(R.layout.spn_edittext);
		User_Spinner.setAdapter(adp1);
		User_Spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Toast.makeText(getActivity(), user_str[position],
						Toast.LENGTH_SHORT).show();
				HashMap<String, String> items = UserList.get(position);
				User_id = items.get(Appconstant.TAG_USER_ID);
				// Loading Member in Background Thread
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	private void getReports() {

		String user_name = User_Spinner.getSelectedItem().toString();
		String s_date = start_date.getText().toString().trim();
		String e_date = end_date.getText().toString().trim();

		if (user_name.equals("") || s_date.equals("") || e_date.equals("")) {

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
			SPINNER_FLAG = true;
		}
	}

	class getUserDetails extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(getActivity());
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
			Log.e("URL ", "is" + Appconstant.GET_USER_URL);

			try {
				// getting JSON string from URL
				JSONObject json = jsonParser.makeHttpRequest(
						Appconstant.GET_USER_URL, "POST", nvp);

				// Check your log cat for JSON response
				Log.d("Inbox JSON: ", json.toString());
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
						String Id = jsonObj1.getString(Appconstant.TAG_USER_ID);

						// Adding values to map
						map.put(Appconstant.TAG_FNAME, fname);
						map.put(Appconstant.TAG_LNAME, lname);
						map.put(Appconstant.TAG_MAIL, mail);
						map.put(Appconstant.TAG_SEX, sex);
						map.put(Appconstant.TAG_USER_ID, Id);
						map.put(Appconstant.TAG_USER_ADD, address);
						map.put(Appconstant.TAG_USER_TYPE, type);
						map.put(Appconstant.TAG_AGE, age);

						// Adding HashMap to ArrayList
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

				Toast.makeText(getActivity(),
						"There are no users assigned to this company",
						Toast.LENGTH_SHORT).show();
			}
			LoadUser();
		}
	}

	private class GetReports extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(getActivity());
			prg.setMessage("Generating report");
			prg.setIndeterminate(true);
			prg.setCanceledOnTouchOutside(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			List<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("userID", User_id));
			nvp.add(new BasicNameValuePair("payment_type", paymentType));
			nvp.add(new BasicNameValuePair("from_date", start_date.getText()
					.toString()));
			nvp.add(new BasicNameValuePair("to_date", end_date.getText()
					.toString()));

			String error_code = null;
			Log.e("URL ", "is" + Appconstant.REPORTS_URL);

			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(
					Appconstant.REPORTS_URL, "POST", nvp);

			// Check your log cat for JSON response
			Log.d("Inbox JSON: ", json.toString());

			try {
				String dBank = null, chqDate = null;
				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");

				if ("1".equals(error_code)) {

					ReportList.clear();
					JSONArray jArray = jsonObj
							.getJSONArray(Appconstant.TAG_RESULT);
					for (int i = 0; i < jArray.length(); i++) {

						map = new HashMap<String, String>();
						JSONObject jsonObj1 = jArray.getJSONObject(i);

						String sBank = jsonObj1.getString("fromBankName");
						String type = jsonObj1.getString("type");
						String amt = jsonObj1.getString("amount");
						String payment_date = null;
						if (paymentType.equals("2")) {
							dBank = jsonObj1.getString("toBankName");
							if (type.equals("cheque")) {
								chqDate = jsonObj1.getString("cheque_date");
								map.put("cDate", chqDate);
							}
						} else {
							payment_date = jsonObj1.getString("payment_date");
							map.put("pDate", payment_date);
						}
						map.put("sBank", sBank);
						map.put("type", type);
						map.put("Amount", amt);
						map.put("dBank", dBank);
						ReportList.add(map);
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
				Toast.makeText(getActivity(), "No reports to display",
						Toast.LENGTH_SHORT).show();
			} else {
				Intent i = new Intent(getActivity(), ViewReports.class);
				i.putExtra("ReportList", ReportList);
				startActivity(i);
			}
		}
	}
}