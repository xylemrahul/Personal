package com.techila.boa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.techila.boa.adapter.CustomListAdapter;
import com.techila.boa.config.Appconstant;
import com.techila.boa.util.PrefSingleton;

public class Company_manage extends Activity implements OnClickListener {

	static boolean flag;
	static ArrayList<HashMap<String, String>> CompanyList;
	private HashMap<String, String> map;
	private String getCompanyUrl;

	private PrefSingleton mMyPreferences;
	Button add;
	ImageView logout;
	ProgressDialog prg;
	ListView lv;
	CustomListAdapter adapter1;
	Properties prop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.list_company);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());

		mMyPreferences.setPreference("Session", "true");
		initViews();

		CompanyList = new ArrayList<HashMap<String, String>>();

		adapter1 = new CustomListAdapter(Company_manage.this, CompanyList);
		lv.setAdapter(adapter1);

		setUrl();
		flag = true;

		add.setOnClickListener(this);
		logout.setOnClickListener(this);
	}

	private void initViews() {
		lv = (ListView) findViewById(R.id.companyList);
		add = (Button) findViewById(R.id.btn);
		logout = (ImageView) findViewById(R.id.logout);
		prg = new ProgressDialog(Company_manage.this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn:

			Intent intent = new Intent(getApplicationContext(),
					Add_Company.class);
			startActivityForResult(intent, 1);
			break;

		case R.id.logout:

			mMyPreferences.setPreference("Session", "false");
			Intent logout = new Intent(Company_manage.this, LoginActivity.class);
			startActivity(logout);
			finish();
			break;

		default:
			break;
		}
	}

	private void setUrl() {
		getCompanyUrl = String.format(Appconstant.GET_COMPANY_URL + "?"
				+ "userID=%s", mMyPreferences.getPreference("LoginId"));
		getCompanyDetails();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {

			if (resultCode == RESULT_OK) {
				adapter1.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(),
						"Company added successfully", Toast.LENGTH_SHORT)
						.show();
			}

			if (resultCode == RESULT_CANCELED) {

			}
		}
	}

	// ----------------------------------------**************************------------------------------------------

	private void getCompanyDetails() {

		showpDialog();
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				getCompanyUrl, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {
						try {
							// Check your log cat for JSON response
							Log.d("Inbox JSON: ", json.toString());

							JSONObject jsonObject = json.getJSONObject("data");
							error_code = jsonObject.getString("Error_Code");
							CompanyList.clear();
							if ("1".equals(error_code)) {

								JSONArray jsonArray = null;
								jsonArray = jsonObject.getJSONArray("result");
								for (int i = 0; i < jsonArray.length(); i++) {

									map = new HashMap<String, String>();
									JSONObject c = jsonArray.getJSONObject(i);

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
									map.put(Appconstant.TAG_REQ_NO,
											c.getString(Appconstant.TAG_REQ_NO));

									CompanyList.add(map);
								}
							}

							if (error_code == "2") {

								Toast.makeText(getApplicationContext(),
										"No companies available",
										Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();

						} catch (NullPointerException e) {
							// Internet Connection is not present
							AlertDialogManager alert = new AlertDialogManager();
							alert.showAlertDialog(
									Company_manage.this,
									"Internet Connection Error",
									"Please connect to working Internet connection",
									false);
							// stop executing code by return
							return;
						} catch (Exception e) {
							e.printStackTrace();
						}
						hidepDialog();
						adapter1.notifyDataSetChanged();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						hidepDialog();
					}
				});

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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!flag) {
			setUrl();
			flag = true;
		}
	}

}
