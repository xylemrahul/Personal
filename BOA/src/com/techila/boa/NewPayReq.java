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

public class NewPayReq extends Activity implements OnClickListener {

	static String CompanyId, F_BankId, balance, T_BankId;
	private String[] act_str, all_act_str;
	String type_arr[] = { "Cheque", "Net Banking" };

	ProgressDialog prg;
	AlertDialogManager alert;
	private PrefSingleton mMyPreferences;

	Spinner transfer_act, dest_act;
	TextView act_blnc, transfer_type;
	EditText transfer_amt, pay_reason;
	Button request;

	private ArrayAdapter<String> all_act_adp;
	private ArrayAdapter<String> act_adp;
	private ArrayAdapter<String> type_adp;

	JSONParser jsonParser = new JSONParser();
	Properties prop;
	private boolean flag = true;

	protected static ArrayList<HashMap<String, String>> BankList;
	protected static ArrayList<HashMap<String, String>> AllBankList;
	HashMap<String, String> map;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin_pay);
		initViews();

		actionBar = getActionBar();
		actionBar.hide();

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

		type_adp = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.row, type_arr);
		request.setText("Send Request");
		request.setEnabled(false);
		new GetBankDetails().execute();
	}

	private void initViews() {

		transfer_act = (Spinner) findViewById(R.id.tranfer_from);
		dest_act = (Spinner) findViewById(R.id.tranfer_to);
		act_blnc = (TextView) findViewById(R.id.act_balance);
		transfer_amt = (EditText) findViewById(R.id.transfer_amt);
		transfer_type = (TextView) findViewById(R.id.p_type);
		request = (Button) findViewById(R.id.pay);
		pay_reason = (EditText) findViewById(R.id.reason);

		alert = new AlertDialogManager();
		BankList = new ArrayList<HashMap<String, String>>();
		AllBankList = new ArrayList<HashMap<String, String>>();

		pay_reason.setEnabled(false);
		request.setOnClickListener(this);
		transfer_type.setOnClickListener(this);
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

		switch (v.getId()) {
		case R.id.p_type:

			new AlertDialog.Builder(NewPayReq.this)
					.setTitle("\t \t Select Transfer Type")
					.setAdapter(type_adp,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									transfer_type.setText(type_arr[which]
											.toString());
									if (type_arr[which].toString().equals(
											"Cheque")) {
										pay_reason.setEnabled(false);
									} else {
										pay_reason.setEnabled(true);
									}
									if (flag) {
										new GetAllBankDetails().execute();
										flag = false;
									}
								}
							}).create().show();

			break;

		case R.id.pay:
			try {
				int blnc = Integer.parseInt(act_blnc.getText().toString());
				int tblnc = Integer.parseInt(transfer_amt.getText().toString());

				if (blnc < tblnc) {

					new AlertDialog.Builder(NewPayReq.this)
							.setTitle("\t \t Enter Valid Amount")
							.setMessage("Funds not available for transfer")
							.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											dialog.dismiss();
										}
									}).create().show();
				} else if (tblnc == 0) {
					alert.showAlertDialog(NewPayReq.this, "Alert",
							"\t \tPlease enter valid amount", null);
				} else {
					if (transfer_type.getText().toString().equals("Cheque")) {
						new SendRequest().execute();
					} else {
						if (!pay_reason.getText().toString().equals("")) {
							new SendRequest().execute();
						} else {
							AlertDialogManager alert = new AlertDialogManager();
							alert.showAlertDialog(NewPayReq.this, "Alert",
									"Please enter all the fields", false);
						}
					}
				}
			} catch (NumberFormatException e) {
				Toast.makeText(getApplicationContext(), "Please enter amount",
						Toast.LENGTH_SHORT).show();
			}

			break;

		default:
			break;
		}
	}

	// ----------------LOAD COMPANY SPECIFIC BRANCH IN SPINNER-------------
	private void LoadBank() {

		act_str = new String[BankList.size()];
		for (int i = 0; i < BankList.size(); i++) {

			HashMap<String, String> items = BankList.get(i);

			act_str[i] = items.get(Appconstant.TAG_BANK_NAME);
			Log.e("", "");
		}

		act_adp = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.row, act_str);
		transfer_act.setAdapter(act_adp);

		transfer_act.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				HashMap<String, String> items = BankList.get(position);
				F_BankId = items.get(Appconstant.TAG_BANK_ID);
				new CheckBalance().execute();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	// ----------------xxxxxxxxxxxxxxxxx-----------------------

	// ------------------LOAD ALL BANKS IN SPINNER---------------

	private void LoadAllBanks() {

		all_act_str = new String[AllBankList.size()];
		for (int i = 0; i < AllBankList.size(); i++) {

			HashMap<String, String> items = AllBankList.get(i);

			all_act_str[i] = items.get(Appconstant.TAG_BANK_NAME);
			Log.e("", "");
		}

		all_act_adp = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.row, all_act_str);
		dest_act.setAdapter(all_act_adp);

		dest_act.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				HashMap<String, String> items = AllBankList.get(position);
				T_BankId = items.get(Appconstant.TAG_BANK_ID);
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
			prg = new ProgressDialog(NewPayReq.this);
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
				BankList.clear();

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

						BankList.add(map);
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

	// -------------------------xxxxxxxxxxxxxxxxx-------------------------

	// ------------------GETTING THE BALANCE IN EACH ACCOUNT--------------

	class CheckBalance extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(NewPayReq.this);
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
			transfer_amt.setText(balance);
		}
	}

	// ------------------xxxxxxxxxxxxxx----------------------

	// ---------------GETTING THE LIST OF ALL THE BANKS-------------------

	private class GetAllBankDetails extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			prg = new ProgressDialog(NewPayReq.this);
			prg.setMessage("Fetching banks...");
			prg.setIndeterminate(true);
			prg.setCanceledOnTouchOutside(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			params1.add(new BasicNameValuePair("", ""));

			String error_code = null;
			Log.e("URL ", "is" + prop.getProperty("GET_ALL_BANK_URL"));

			try {
				// getting JSON string from URL
				JSONObject json = jsonParser.makeHttpRequest(
						Appconstant.GET_ALL_BANK_URL, "POST", params1);

				// Check your log cat for JSON response
				Log.d("Inbox JSON: ", json.toString());

				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
				AllBankList.clear();

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

						AllBankList.add(map);
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
						"There are no banks available", Toast.LENGTH_SHORT)
						.show();
				LoadAllBanks();

			} else {
				request.setEnabled(true);
				LoadAllBanks();
			}
		}
	}

	private class SendRequest extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			prg = new ProgressDialog(NewPayReq.this);
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
			params1.add(new BasicNameValuePair("toBankID", T_BankId));
			params1.add(new BasicNameValuePair("amount", ""
					+ transfer_amt.getText().toString()));
			params1.add(new BasicNameValuePair("paymentType", transfer_type
					.getText().toString()));
			
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
