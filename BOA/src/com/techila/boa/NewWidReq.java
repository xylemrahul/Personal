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
import com.techila.boa.config.Appconstant;
import com.techila.boa.util.JSONParser;
import com.techila.boa.util.PrefSingleton;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class NewWidReq extends Activity implements OnClickListener {

	Spinner withd_act;
	TextView act_blnc;
	EditText withd_amt;
	Button request;

	private String CompanyId, F_BankId, balance;
	private String[] act_str;

	ProgressDialog prg;
	AlertDialogManager alert;
	private PrefSingleton mMyPreferences;
	private ArrayAdapter<String> act_adp;
	JSONParser jsonParser = new JSONParser();
	Properties prop;

	protected static ArrayList<HashMap<String, String>> WidBankList;
	HashMap<String, String> map;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin_wid);

		initViews();

		actionBar = getActionBar();
		actionBar.hide();

		alert = new AlertDialogManager();
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

		WidBankList = new ArrayList<HashMap<String, String>>();
		CompanyId = mMyPreferences.getPreference("C_ID");
		new GetBankDetails().execute();
		request.setText("Send Request");
		request.setOnClickListener(this);
	}

	private void initViews() {
		act_blnc = (TextView) findViewById(R.id.act_balance);
		withd_act = (Spinner) findViewById(R.id.wid_bank);
		withd_amt = (EditText) findViewById(R.id.wid_amt);
		request = (Button) findViewById(R.id.withdraw);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1) {

			if (resultCode == Activity.RESULT_OK) {
				Toast.makeText(getApplicationContext(),
						"Details submitted successfully", Toast.LENGTH_SHORT)
						.show();
				// new GetBankDetails().execute();
			}

			if (resultCode == Activity.RESULT_CANCELED) {

			}
		}
	}

	@Override
	public void onClick(View v) {

		try {
			int blnc = Integer.parseInt(act_blnc.getText().toString());
			int wblnc = Integer.parseInt(withd_amt.getText().toString());

			if (blnc < wblnc) {

				new AlertDialog.Builder(NewWidReq.this)
						.setTitle("\t \t Enter Valid Amount")
						.setMessage("Insufficient Balance")
						.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										dialog.dismiss();
									}
								}).create().show();
			} else if (wblnc == 0) {

				alert.showAlertDialog(NewWidReq.this, "Alert",
						"\t \t Please enter valid amount", null);
			} else {
				new SendRequest().execute();
			}
		} catch (NumberFormatException e) {
			Toast.makeText(getApplicationContext(), "Please enter amount",
					Toast.LENGTH_SHORT).show();
		}

	}

	// ----------------LOAD COMPANY SPECIFIC BRANCH IN SPINNER-----------------

	private void LoadBank() {

		act_str = new String[WidBankList.size()];
		for (int i = 0; i < WidBankList.size(); i++) {

			HashMap<String, String> items = WidBankList.get(i);

			act_str[i] = items.get(Appconstant.TAG_BANK_NAME);
			Log.e("", "");
		}

		act_adp = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.row, act_str);
		withd_act.setAdapter(act_adp);

		withd_act.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				HashMap<String, String> items = WidBankList.get(position);
				F_BankId = items.get(Appconstant.TAG_BANK_ID);
				new CheckBalance().execute();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	// ----------------xxxxxxxxxxxxxxxxx-----------------------

	// ------GETTING THE LIST OF BANKS UNDER THIS COMPANY-----------------

	private class GetBankDetails extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			prg = new ProgressDialog(NewWidReq.this);
			prg.setIndeterminate(true);
			prg.setMessage("Fetching Banks..");
			prg.setCanceledOnTouchOutside(false);
			prg.show();

		}

		@Override
		protected Integer doInBackground(Void... params) {

			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			params1.add(new BasicNameValuePair("companyID", CompanyId));

			String error_code = null;
			Log.e("URL ", "is" + prop.getProperty("GET_BANK_URL"));

			try {
				// getting JSON string from URL
				JSONObject json = jsonParser.makeHttpRequest(
						Appconstant.GET_BANK_URL, "POST", params1);

				// Check your log cat for JSON response
				Log.d("Inbox JSON: ", json.toString());

				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
				WidBankList.clear();

				if ("1".equals(error_code)) {

					JSONArray jArray = jsonObj.getJSONArray("result");

					for (int i = 0; i < jArray.length(); i++) {

						map = new HashMap<String, String>();
						JSONObject jsonObj1 = jArray.getJSONObject(i);

						String type = jsonObj1
								.getString(Appconstant.TAG_ACC_TYPE);
						String act_no = jsonObj1
								.getString(Appconstant.TAG_ACC_NO);
						String cID = jsonObj1
								.getString(Appconstant.TAG_COMPANY_ID);
						String blnc = jsonObj1
								.getString(Appconstant.TAG_INIT_BLNC);
						String bName = jsonObj1
								.getString(Appconstant.TAG_BANK_NAME);
						String cName = jsonObj1
								.getString(Appconstant.TAG_CUST_NAME);
						String bID = jsonObj1
								.getString(Appconstant.TAG_BANK_ID);
						String ifsc = jsonObj1.getString(Appconstant.TAG_IFSC);

						map.put(Appconstant.TAG_ACC_TYPE, type);
						map.put(Appconstant.TAG_ACC_NO, act_no);
						map.put(Appconstant.TAG_COMPANY_ID, cID);
						map.put(Appconstant.TAG_INIT_BLNC, blnc);
						map.put(Appconstant.TAG_BANK_NAME, bName);
						map.put(Appconstant.TAG_CUST_NAME, cName);
						map.put(Appconstant.TAG_BANK_ID, bID);
						map.put(Appconstant.TAG_IFSC, ifsc);

						WidBankList.add(map);
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
						"There are no banks assigned to this company",
						Toast.LENGTH_SHORT).show();
				LoadBank();
			} else {
				LoadBank();
			}
		}
	}

	// ------------------GETTING THE BALANCE IN EACH ACCOUNT--------------

	class CheckBalance extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(NewWidReq.this);
			prg.setMessage("Checking Balance...");
			prg.setIndeterminate(true);
			prg.setCanceledOnTouchOutside(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			List<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("bankID", F_BankId));

			String error_code = null;
			Log.e("URL ", "is" + prop.getProperty("CHECK_BLNC_URL"));
			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(
					Appconstant.CHECK_BLNC_URL, "POST", nvp);

			// Check your log cat for JSON response
			Log.d("Inbox JSON: ", json.toString());

			try {

				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");

				if ("1".equals(error_code)) {
					JSONArray jsonArray = null;
					jsonArray = jsonObj.getJSONArray("result");
					for (int i = 0; i < jsonArray.length(); i++) {

						map = new HashMap<String, String>();
						JSONObject c = jsonArray.getJSONObject(i);
						balance = c.getString("current_balance");
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
						"There are no banks assigned to this company",
						Toast.LENGTH_SHORT).show();
			}
			act_blnc.setText(balance);
		}
	}

	// ---------------xxxxxxxxxxxxxxxxxxxxxxx--------------------

	private class SendRequest extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			prg = new ProgressDialog(NewWidReq.this);
			prg.setMessage("Sending request...");
			prg.setIndeterminate(true);
			prg.setCanceledOnTouchOutside(false);
			prg.show();
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			params1.add(new BasicNameValuePair("userID", mMyPreferences
					.getPreference("LoginId")));
			params1.add(new BasicNameValuePair("fromBankID", F_BankId));
			params1.add(new BasicNameValuePair("toBankID", ""));
			/*
			 * params1.add(new BasicNameValuePair("paymentDate", ""));
			 * params1.add(new BasicNameValuePair("paymentReason", ""));
			 */
			params1.add(new BasicNameValuePair("amount", ""
					+ withd_amt.getText().toString()));
			params1.add(new BasicNameValuePair("paymentType", "Withdrawal"));

			String error_code = null;
			Log.e("URL ", "is" + prop.getProperty("SEND_REQUEST_URL"));

			try {
				// getting JSON string from URL
				JSONObject json = jsonParser.makeHttpRequest(
						Appconstant.SEND_REQUEST_URL, "POST", params1);

				// Check your log cat for JSON response
				Log.d("Inbox JSON: ", json.toString());

				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
				if ("1".equals(error_code)) {

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
				Toast.makeText(getApplicationContext(), "Please try again",
						Toast.LENGTH_SHORT).show();
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED, returnIntent);
				finish();
			} else {
				Intent returnIntent = new Intent();
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		}
	}
}
