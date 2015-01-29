package com.techila.boa;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.techila.boa.R;
import com.techila.boa.config.Appconstant;
import com.techila.boa.model.BankModel;
import com.techila.boa.util.PrefSingleton;

public class Add_Bank extends Activity {

	EditText ben_name, act_type, act_num, ifsc_code, bank_name, initial_blnc;
	Button submit, cancel;

	String CompanyId, addBankUrl;
	Boolean isValid;

	private HashMap<String, String> map;
	private PrefSingleton mMyPreferences;

	ProgressDialog prg = null;
	Properties prop;
	BankModel model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_add_bank);

		initView();

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

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				isValid = validate();
				if (isValid) {
					prg = new ProgressDialog(Add_Bank.this);
					// Showing progress dialog before making http request
					prg.setMessage("Loading...");
					prg.show();
					prg.setCanceledOnTouchOutside(false);

					addBankUrl = String
							.format(Appconstant.ADD_BANK_URL
									+ "?"
									+ "companyID=%s&custName=%s&accNumber=%s&accType=%s&bankName=%s&ifsc=%s&initialBalance=%s",
									CompanyId, model.getBenName(),
									model.getAct_no(), model.getActType(),
									model.getBankName(), model.getifsc(),
									model.getInitialBlnc());

					addBankDetails();
				} else {
					// Showing the alertdialog
					// showAlert();
					AlertDialogManager alert = new AlertDialogManager();
					alert.showAlertDialog(Add_Bank.this, "Alert",
							"Please enter all the fields ", false);
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

	private void initView() {

		ben_name = (EditText) findViewById(R.id.et_name);
		act_type = (EditText) findViewById(R.id.et_type);
		act_num = (EditText) findViewById(R.id.et_num);
		bank_name = (EditText) findViewById(R.id.et_bname);
		ifsc_code = (EditText) findViewById(R.id.et_ifsc);
		initial_blnc = (EditText) findViewById(R.id.et_blnc);
		submit = (Button) findViewById(R.id.add);
		cancel = (Button) findViewById(R.id.cancel);

	}

	protected Boolean validate() {

		String BankName = bank_name.getText().toString();
		String BeneficiaryName = ben_name.getText().toString();
		String Account_No = act_num.getText().toString();
		String AccountType = act_type.getText().toString();
		String IFSC = ifsc_code.getText().toString();
		String InitialBalance = initial_blnc.getText().toString();

		if (BankName.equals("") || BeneficiaryName.equals("")
				|| Account_No.equals("") || AccountType.equals("")
				|| IFSC.equals("") || InitialBalance.equals("")) {

			return false;
		} else {
			model = new BankModel(BeneficiaryName, Account_No, AccountType,
					BankName, IFSC, InitialBalance);
			return true;
		}
	}

	private void addBankDetails() {

		showpDialog();

		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				addBankUrl, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {
						try {
							map = new HashMap<String, String>();
							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");
							if ("1".equals(error_code)) {
								JSONArray jArray = jsonObj
										.getJSONArray(Appconstant.TAG_RESULT);
								JSONObject jsonObj1 = jArray.getJSONObject(0);

								map.put(Appconstant.TAG_ACC_TYPE, jsonObj1
										.getString(Appconstant.TAG_ACC_TYPE));
								map.put(Appconstant.TAG_ACC_NO, jsonObj1
										.getString(Appconstant.TAG_ACC_NO));
								map.put(Appconstant.TAG_COMPANY_ID, jsonObj1
										.getString(Appconstant.TAG_COMPANY_ID));
								map.put(Appconstant.TAG_INIT_BLNC, jsonObj1
										.getString(Appconstant.TAG_INIT_BLNC));
								map.put(Appconstant.TAG_BANK_NAME, jsonObj1
										.getString(Appconstant.TAG_BANK_NAME));
								map.put(Appconstant.TAG_CUST_NAME, jsonObj1
										.getString(Appconstant.TAG_CUST_NAME));
								map.put(Appconstant.TAG_BANK_ID, jsonObj1
										.getString(Appconstant.TAG_BANK_ID));
								map.put(Appconstant.TAG_IFSC, jsonObj1
										.getString(Appconstant.TAG_IFSC));

								Bank_Details.BankList.add(map);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (error_code == "2") {

							Toast.makeText(getApplicationContext(),
									"Cannot save into database",
									Toast.LENGTH_SHORT).show();
							Intent returnIntent = new Intent();
							setResult(RESULT_CANCELED, returnIntent);
							finish();
						}
						Intent returnIntent = new Intent();
						setResult(RESULT_OK, returnIntent);
						finish();
						hidepDialog();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						// TODO Auto-generated method stub
						hidepDialog();
					}
				});
		// Adding request to request queue
		Volley.newRequestQueue(getApplicationContext()).add(jsonObjReq);
	}

	private void showpDialog() {
		if (!prg.isShowing())
			prg.show();
		prg.setCanceledOnTouchOutside(false);
	}

	private void hidepDialog() {
		if (prg.isShowing())
			prg.dismiss();
	}
}
