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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.techila.boa.R;
import com.techila.boa.config.Appconstant;
import com.techila.boa.model.CompanyModel;
import com.techila.boa.util.PrefSingleton;

public class Add_Company extends Activity implements OnClickListener {

	EditText CName, CTan, CPan, CAddress;
	ProgressDialog prg;
	Button save, cancel;
	private String addCompanyUrl;

	HashMap<String, String> map;
	private PrefSingleton mMyPreferences;

	CompanyModel Cmodel;
	static ConnectionDetector cd;
	Properties prop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_company);

		initViews();

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

		save.setOnClickListener(this);
		cancel.setOnClickListener(this);
	}

	private void initViews() {

		CName = (EditText) findViewById(R.id.tx_cname);
		CTan = (EditText) findViewById(R.id.tx_tan);
		CPan = (EditText) findViewById(R.id.tx_pan);
		CAddress = (EditText) findViewById(R.id.tx_add);
		save = (Button) findViewById(R.id.add);
		cancel = (Button) findViewById(R.id.cancel);
	}

	protected Boolean validate() {

		String company_name = CName.getText().toString();
		String tan = CTan.getText().toString();
		String pan = CPan.getText().toString();
		String company_address = CAddress.getText().toString();

		if (company_name.equals("") || tan.equals("") || pan.equals("")
				|| company_address.equals("")) {

			return false;
		} else {
			Cmodel = new CompanyModel(company_name, tan, pan, company_address);
			return true;
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.add:

			cd = new ConnectionDetector(getApplicationContext());
			boolean isValid = validate();
			if (isValid) {
				if (!cd.isConnectingToInternet()) {
					// Internet Connection is not present
					AlertDialogManager alert = new AlertDialogManager();
					alert.showAlertDialog(Add_Company.this,
							"Internet Connection Error",
							"Please connect to working Internet connection",
							false);
					// stop executing code by return
					return;
				} else {

					prg = new ProgressDialog(Add_Company.this);
					// Showing progress dialog before making http request
					prg.setMessage("Loading...");
					prg.show();
					prg.setCanceledOnTouchOutside(false);

					addCompanyUrl = String
							.format(Appconstant.ADD_COMPANY_URL
									+ "?"
									+ "companyName=%s&companyTanNo=%s&companyPanNo=%s&companyAddress=%s&userID=%s",
									Cmodel.getCompany(), Cmodel.getTan(),
									Cmodel.getPan(), Cmodel.getAddress(),
									mMyPreferences.getPreference("LoginId"));
					CreateCompany();
				}
			} else {

				AlertDialogManager alert = new AlertDialogManager();
				alert.showAlertDialog(Add_Company.this, "Alert",
						"Please enter all the fields", false);
			}
			break;

		case R.id.cancel:
			Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED, returnIntent);
			finish();

		default:
			break;
		}
	}

	private void CreateCompany() {

		showpDialog();

		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				addCompanyUrl, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {
						try {
							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");

							if ("1".equals(error_code)) {

								JSONArray jsonArray = null;
								jsonArray = jsonObj.getJSONArray("result");
								for (int i = 0; i < jsonArray.length(); i++) {

									map = new HashMap<String, String>();
									JSONObject c = jsonArray.getJSONObject(i);

									// Adding values to map
									map.put(Appconstant.TAG_COMPANY_ID,
											c.getString(Appconstant.TAG_COMPANY_ID));
									map.put(Appconstant.TAG_COMPANY_NAME,
											c.getString(Appconstant.TAG_COMPANY_NAME));
									map.put(Appconstant.TAG_TAN,
											c.getString(Appconstant.TAG_TAN));
									map.put(Appconstant.TAG_PAN,
											c.getString(Appconstant.TAG_PAN));
									map.put(Appconstant.TAG_COM_ADDRESS,
											c.getString(Appconstant.TAG_COM_ADDRESS));
									// Adding Hashmap to Arraylist
									Company_manage.CompanyList.add(map);
								}
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
