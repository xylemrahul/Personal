package com.techila.boa;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.techila.boa.R;
import com.techila.boa.adapter.CustomListAdapter;
import com.techila.boa.config.Appconstant;
import com.techila.boa.util.JSONParser;
import com.techila.boa.util.PrefSingleton;

public class Bank_Details extends Activity implements OnClickListener {

	Button addBank, sendEmail, share;
	EditText send_mail, name;
	CheckBox ch;
	ProgressDialog prg;
	String bName, cName, act_no, ifsc, type;

	String CompanyId, mail, Name;
	JSONParser jsonParser = new JSONParser();
	AlertDialogManager alert;

	protected static ArrayList<HashMap<String, String>> BankList;
	List<String> checkedList = null;
	HashMap<String, String> map;

	ListView lv;
	PrefSingleton mMyPreferences = null;
	CustomListAdapter adapter1;
	Properties prop;

	private boolean emailcheck = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.list_bank_details);

		// Initialize variables
		initViews();

		BankList = new ArrayList<HashMap<String, String>>();
		alert = new AlertDialogManager();

		adapter1 = new CustomListAdapter(Bank_Details.this, BankList);
		lv.setAdapter(adapter1);

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

		new GetBankDetails().execute();

		addBank.setOnClickListener(this);
		sendEmail.setOnClickListener(this);
		share.setOnClickListener(this);
	}

	// --------------------------------------Calling the activity for adding
	// bank----------------------------------
	private void addBank() {

		Intent intent = new Intent(getApplicationContext(), Add_Bank.class);
		startActivityForResult(intent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1) {

			if (resultCode == RESULT_OK) {
				CustomListAdapter.itemsChecked = new boolean[BankList.size()];
				adapter1.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(),
						"Bank added successfully", Toast.LENGTH_SHORT).show();
			}

			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getApplicationContext(), "Bank not added",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	// ----------------------------------------**************************------------------------------------------

	private void initViews() {

		lv = (ListView) findViewById(R.id.bankList);
		addBank = (Button) findViewById(R.id.add_bank);
		sendEmail = (Button) findViewById(R.id.mail);
		share = (Button) findViewById(R.id.share);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.add_bank:
			addBank();

			break;

		case R.id.mail:

			Toast.makeText(getApplicationContext(),
					"" + CustomListAdapter.chkList, Toast.LENGTH_SHORT).show();
			Log.e("", "Custom" + CustomListAdapter.chkList);

			for (int i = 0; i < CustomListAdapter.chkList.size(); i++) {

				checkedList = new ArrayList<String>();
				checkedList.addAll(CustomListAdapter.chkList);

				Log.e("", "" + checkedList.toString());
			}

			if (CustomListAdapter.chkList.isEmpty()) {

				alert.showAlertDialog(Bank_Details.this, "Alert",
						"Please select at least one bank", false);
			} else {
				SendMail();
			}

			break;

		case R.id.share:

			if (CustomListAdapter.chkList.isEmpty()) {

				alert.showAlertDialog(Bank_Details.this, "Alert",
						"Please select at least one bank", false);
			} else {
				try {
					String mess[] = new String[CustomListAdapter.chkList.size()];
					for (int i = 0; i < CustomListAdapter.chkList.size(); i++) {

						HashMap<String, String> m = CustomListAdapter.banks
								.get(i);
						mess[i] = "BANK NAME : "
								+ m.get(Appconstant.TAG_BANK_NAME) + "," + "\t"
								+ "ACCOUNT HOLDER NAME : "
								+ m.get(Appconstant.TAG_CUST_NAME) + "," + "\t"
								+ "ACCOUNT NUMBER : "
								+ m.get(Appconstant.TAG_ACC_NO) + "," + "\t"
								+ "IFSC CODE : " + m.get(Appconstant.TAG_IFSC)
								+ "," + "\t" + "ACCOUNT TYPE : "
								+ m.get(Appconstant.TAG_ACC_TYPE) + "\n";

					}
					Intent intent = new Intent(
							android.content.Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(android.content.Intent.EXTRA_TEXT,
							getMyStringMessage(mess));
					intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
							"Book Of Accounts bank details");
					startActivity(Intent.createChooser(intent, "Share via"));

				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}

			break;

		default:
			break;
		}
	}

	private String getMyStringMessage(String[] arr) {
		StringBuilder builder = new StringBuilder();
		for (String s : arr) {
			builder.append(s).append("\n");
		}
		return builder.toString();
	}

	private void SendMail() {

		AlertDialog.Builder alertdialog = new AlertDialog.Builder(
				Bank_Details.this);

		LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final View view = layoutInflater.inflate(R.layout.dialog_send_mail,
				null);
		send_mail = (EditText) view.findViewById(R.id.sendEmail);
		name = (EditText) view.findViewById(R.id.tx_cname);

		alertdialog.setPositiveButton("Send",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						mail = send_mail.getText().toString();
						Name = name.getText().toString();

						checkemail(mail);
						if (Name.equals("") || emailcheck == false) {
							Toast.makeText(getApplicationContext(),
									"Please enter correct information",
									Toast.LENGTH_SHORT).show();
							dialog.dismiss();
						} else {
							new sendEmail().execute();
						}
					}
				});
		alertdialog.setView(view);
		alertdialog.show();
	}

	private class GetBankDetails extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			prg = new ProgressDialog(Bank_Details.this);
			prg.setIndeterminate(true);
			prg.setMessage("Fetching List..");
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

						String cID = jsonObj1
								.getString(Appconstant.TAG_COMPANY_ID);
						String blnc = jsonObj1
								.getString(Appconstant.TAG_INIT_BLNC);
						String bID = jsonObj1
								.getString(Appconstant.TAG_BANK_ID);
						type = jsonObj1.getString(Appconstant.TAG_ACC_TYPE);
						act_no = jsonObj1.getString(Appconstant.TAG_ACC_NO);
						bName = jsonObj1.getString(Appconstant.TAG_BANK_NAME);
						cName = jsonObj1.getString(Appconstant.TAG_CUST_NAME);
						ifsc = jsonObj1.getString(Appconstant.TAG_IFSC);

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
			}
			CustomListAdapter.itemsChecked = new boolean[BankList.size()];
			adapter1.notifyDataSetChanged();
		}
	}

	private class sendEmail extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(Bank_Details.this);
			prg.setIndeterminate(true);
			prg.setMessage("Sending Mail..");
			prg.setCanceledOnTouchOutside(false);
			prg.show();

		}

		@Override
		protected Integer doInBackground(Void... params) {

			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			params1.add(new BasicNameValuePair("fullName", Name));
			params1.add(new BasicNameValuePair("emailID", mail));
			params1.add(new BasicNameValuePair("bankIDs", checkedList
					.toString().substring(1,
							checkedList.toString().length() - 1)));

			String error_code = null;
			Log.e("URL ", "is" + prop.getProperty("SEND_MAIL_URL"));

			try {
				// getting JSON string from URL
				JSONObject json = jsonParser.makeHttpRequest(
						Appconstant.SEND_MAIL_URL, "POST", params1);

				// Check your log cat for JSON response
				Log.d("Inbox JSON: ", json.toString());

				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
				if ("1".equals(error_code)) {

					return Integer.parseInt(error_code);
				} else {
					error_code = "2";
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
				Toast.makeText(getApplicationContext(), "Mail cannot be sent",
						Toast.LENGTH_SHORT).show();
			}
			Toast.makeText(getApplicationContext(), "Mail Sent Successfully",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void checkemail(String email) {
		Pattern pattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
		Matcher matcher = pattern.matcher(email);
		emailcheck = matcher.matches();
	}

	@Override
	protected void onPause() {

		super.onPause();
	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();
		CustomListAdapter.chkList.clear();
	}

}
