package com.techila.boa;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.techila.boa.R;
import com.techila.boa.config.Appconstant;
import com.techila.boa.model.TransactionModel;
import com.techila.boa.util.JSONParser;
import com.techila.boa.util.PrefSingleton;

public class ChequeActivity extends Activity implements OnClickListener {

	EditText et_chq, et_payee, et_date, et_reason;
	String Cheque_No, Payee, Pay_Date, Reason;
	Button save, cancel;

	private PrefSingleton mMyPreferences;
	ProgressDialog prg;
	JSONParser jsonParser = new JSONParser();
	Properties prop;
	private TransactionModel model;

	static final int DATE_DIALOG_ID = 0;
	private int mYear, mMonth, mDay;
	final Calendar c = Calendar.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cheque_details);

		initViews();
		Bundle b = getIntent().getExtras();
		if (b != null) {
			model = b.getParcelable("obj");
		}

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

		et_date.setInputType(InputType.TYPE_NULL);

		save.setOnClickListener(this);
		cancel.setOnClickListener(this);
		et_date.setOnClickListener(this);
	}

	private void initViews() {

		et_chq = (EditText) findViewById(R.id.chq_no);
		et_payee = (EditText) findViewById(R.id.payee);
		et_date = (EditText) findViewById(R.id.date);
		et_reason = (EditText) findViewById(R.id.reason);
		save = (Button) findViewById(R.id.save);
		cancel = (Button) findViewById(R.id.cancel);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.save:
			boolean isValid = validate();
			if (isValid) {
				new ChqTransfer().execute();
			} else {

				AlertDialogManager alert = new AlertDialogManager();
				alert.showAlertDialog(ChequeActivity.this, "Alert",
						"Please enter all the fields", false);
			}

			break;

		case R.id.cancel:

			Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED, returnIntent);
			finish();

			break;

		case R.id.date:

			mYear = c.get(Calendar.YEAR);
			mMonth = c.get(Calendar.MONTH);
			mDay = c.get(Calendar.DAY_OF_MONTH);

			DatePickerDialog mDatePicker = new DatePickerDialog(
					ChequeActivity.this, mDateSetListener, mYear, mMonth, mDay);
			mDatePicker.setCanceledOnTouchOutside(false);
			mDatePicker.getDatePicker().setCalendarViewShown(false);
			mDatePicker.getDatePicker().setMinDate(
					System.currentTimeMillis() - 1000);
			mDatePicker.setTitle("Select date");
			mDatePicker.show();
			break;

		default:
			break;
		}
	}

	protected Boolean validate() {

		Cheque_No = et_chq.getText().toString();
		Payee = et_payee.getText().toString();
		Pay_Date = et_date.getText().toString();
		Reason = et_reason.getText().toString();

		if (Cheque_No.equals("") || Payee.equals("") || Pay_Date.equals("")
				|| Reason.equals("")) {

			return false;
		} else {
			model.setReason(Reason);
			return true;
		}
	}

	// ---------------------Date Listener------------------

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;

			updateStartDate();

		}
	};

	// ----------------updates the date in the EditText--------------------
	private void updateStartDate() {
		et_date.setText(getString(R.string.strSelectedDate, new StringBuilder()

		.append(mYear).append("-").append(mMonth + 1).append("-").append(mDay)
				.append(" ")));
	}

	private class ChqTransfer extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			prg = new ProgressDialog(ChequeActivity.this);
			prg.setIndeterminate(true);
			prg.setMessage("Performing Transfer...");
			prg.setCanceledOnTouchOutside(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			params1.add(new BasicNameValuePair("companyID", model
					.getCompanyId()));
			params1.add(new BasicNameValuePair("userID", mMyPreferences
					.getPreference("LoginId")));
			params1.add(new BasicNameValuePair("userType", mMyPreferences
					.getPreference("user_Type")));
			params1.add(new BasicNameValuePair("fromBankID", model.getSource()));
			params1.add(new BasicNameValuePair("toBankID", model
					.getDestination()));
			params1.add(new BasicNameValuePair("amount", model.getAmount()));
			params1.add(new BasicNameValuePair("user_request_id", model
					.getReqId()));

			params1.add(new BasicNameValuePair("paymentType", model
					.getPay_type()));
			params1.add(new BasicNameValuePair("paymentReason", model
					.getReason()));
			params1.add(new BasicNameValuePair("chequeDate", Pay_Date));
			params1.add(new BasicNameValuePair("chequeIssued", Payee));
			params1.add(new BasicNameValuePair("chequeNo", Cheque_No));

			if (mMyPreferences.getPreference("user_Type").equals("User")) {
				params1.add(new BasicNameValuePair("payment_id", model.getPay_Id()));
			}

			String error_code = null;
			Log.e("URL ", "is" + prop.getProperty("PAYMENT_DETAILS_URL"));

			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(
					Appconstant.PAYMENT_DETAILS_URL, "POST", params1);

			// Check your log cat for JSON response
			Log.d("Inbox JSON: ", json.toString());

			try {

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
				prg.cancel();
			}
			if (result == 3) {

				Toast.makeText(getApplicationContext(),
						"Insufficient funds,cannot transfer", Toast.LENGTH_LONG)
						.show();
			} else if (result == 2) {
				Toast.makeText(getApplicationContext(), "Cannot save details",
						Toast.LENGTH_SHORT).show();
			}
			Intent returnIntent = new Intent();
			setResult(RESULT_OK, returnIntent);
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}