package com.techila.boa.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.techila.boa.AlertDialogManager;
import com.techila.boa.R;
import com.techila.boa.config.Appconstant;
import com.techila.boa.model.TransactionModel;
import com.techila.boa.util.PrefSingleton;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class AdminWid extends Fragment implements OnClickListener {

	private String Company_Id, balance;
	private String bankUrl, chkBlncUrl, withdrawalUrl;
	private String[] act_str;

	Spinner withd_act;
	TextView act_blnc;
	EditText withd_amt, pay_reason;
	Button withdraw;
	private boolean flag = true;

	private ArrayAdapter<String> act_adp;
	private HashMap<String, String> map;
	protected static ArrayList<HashMap<String, String>> WidBankList;

	ProgressDialog prg;
	AlertDialogManager alert;
	private PrefSingleton mMyPreferences;
	Properties prop;
	private TransactionModel model;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.admin_wid, container, false);

		initViews(rootView);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getActivity());

		Company_Id = mMyPreferences.getPreference("C_ID");

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

		withdraw.setOnClickListener(this);
	}

	private void initViews(View rootView) {

		act_blnc = (TextView) rootView.findViewById(R.id.act_balance);
		withd_act = (Spinner) rootView.findViewById(R.id.wid_bank);
		withd_amt = (EditText) rootView.findViewById(R.id.wid_amt);
		pay_reason = (EditText) rootView.findViewById(R.id.reason);
		withdraw = (Button) rootView.findViewById(R.id.withdraw);
		alert = new AlertDialogManager();
		model = new TransactionModel();
		prg = new ProgressDialog(getActivity());
	}

	@Override
	public void onClick(View v) {

		try {
			int blnc = Integer.parseInt(act_blnc.getText().toString());
			int wblnc = Integer.parseInt(withd_amt.getText().toString());

			if (blnc < wblnc) {

				new AlertDialog.Builder(getActivity())
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
				alert.showAlertDialog(getActivity(), "Alert",
						"\t \tPlease enter valid amount", null);
			} else {

				boolean isValid = validate();
				if (isValid) {
					model.setPay_type("Withdrawal");
					setUrl(3);

				} else {

					AlertDialogManager alert = new AlertDialogManager();
					alert.showAlertDialog(getActivity(), "Alert",
							"Please enter all the fields", false);
				}
			}
		} catch (NumberFormatException e) {
			Toast.makeText(getActivity(), "Please enter amount",
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			if (flag) {
				setUrl(1);
				flag = false;
			}
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

		act_adp = new ArrayAdapter<String>(getActivity(), R.layout.row, act_str);
		withd_act.setAdapter(act_adp);

		withd_act.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				HashMap<String, String> items = WidBankList.get(position);
				String F_BankId = items.get(Appconstant.TAG_BANK_ID);
				model.setSource(F_BankId);
				setUrl(2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	protected Boolean validate() {

		String reason = pay_reason.getText().toString();
		String amt = withd_amt.getText().toString();

		if (reason.equals("") || amt.equals("")) {
			return false;
		} else {
			model.setReason(reason);
			model.setAmount(amt);
			return true;
		}
	}

	private void setUrl(int a) {

		switch (a) {
		case 1:
			bankUrl = String.format(Appconstant.GET_BANK_URL + "?"
					+ "companyID=%s", Company_Id);

			GetBankDetails();
			break;

		case 2:
			chkBlncUrl = String.format(Appconstant.CHECK_BLNC_URL + "?"
					+ "bankID=%s", model.getSource());
			CheckBalance();
			break;

		case 3:
			withdrawalUrl = String
					.format(Appconstant.PAYMENT_DETAILS_URL
							+ "?"
							+ "companyID=%s&userID=%s&userType=%s&fromBankID=%s&toBankID=%s&amount=%s"
							+ "&paymentReason=%s&paymentType=%s", Company_Id,
							mMyPreferences.getPreference("LoginId"),
							mMyPreferences.getPreference("user_Type"),
							model.getSource(), "", model.getAmount(),
							model.getReason(), model.getPay_type());

			Withdrawal();
			break;
		default:
			break;
		}
	}

	// -----------------------xxxxxxxxxxxxxxxxx---------------------

	// ------GETTING THE LIST OF BANKS UNDER THIS COMPANY-----------------

	private void GetBankDetails() {

		showpDialog();
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				bankUrl, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {
						try {
							Log.d("Inbox JSON: ", json.toString());

							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");
							WidBankList.clear();

							if ("1".equals(error_code)) {

								JSONArray jArray = jsonObj
										.getJSONArray("result");

								for (int i = 0; i < jArray.length(); i++) {

									map = new HashMap<String, String>();
									JSONObject jsonObj1 = jArray
											.getJSONObject(i);

									map.put(Appconstant.TAG_ACC_TYPE,
											jsonObj1.getString(Appconstant.TAG_ACC_TYPE));
									map.put(Appconstant.TAG_ACC_NO, jsonObj1
											.getString(Appconstant.TAG_ACC_NO));
									map.put(Appconstant.TAG_COMPANY_ID,
											jsonObj1.getString(Appconstant.TAG_COMPANY_ID));
									map.put(Appconstant.TAG_INIT_BLNC,
											jsonObj1.getString(Appconstant.TAG_INIT_BLNC));
									map.put(Appconstant.TAG_BANK_NAME,
											jsonObj1.getString(Appconstant.TAG_BANK_NAME));
									map.put(Appconstant.TAG_CUST_NAME,
											jsonObj1.getString(Appconstant.TAG_CUST_NAME));
									map.put(Appconstant.TAG_BANK_ID, jsonObj1
											.getString(Appconstant.TAG_BANK_ID));
									map.put(Appconstant.TAG_IFSC, jsonObj1
											.getString(Appconstant.TAG_IFSC));

									WidBankList.add(map);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (error_code == "2") {
							Toast.makeText(
									getActivity(),
									"There are no banks assigned to this company",
									Toast.LENGTH_SHORT).show();
							LoadBank();
						} else {
							LoadBank();
						}
						hidepDialog();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						hidepDialog();
					}
				});
		Volley.newRequestQueue(getActivity()).add(jsonObjReq);
	}

	// ------------------GETTING THE BALANCE IN EACH ACCOUNT--------------

	private void CheckBalance() {

		showpDialog();
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				chkBlncUrl, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {

						try {
							Log.d("Inbox JSON: ", json.toString());

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
						if (error_code == "2") {
							Toast.makeText(
									getActivity(),
									"There are no banks assigned to this company",
									Toast.LENGTH_SHORT).show();
						}
						act_blnc.setText(balance);
						withd_amt.setText(balance);
						hidepDialog();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						hidepDialog();
					}
				});
		Volley.newRequestQueue(getActivity()).add(jsonObjReq);
	}

	private void Withdrawal() {

		showpDialog();
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				withdrawalUrl, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {

						try {
							Log.d("Inbox JSON: ", json.toString());

							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");

						} catch (JSONException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (error_code == "2") {
							Toast.makeText(getActivity(),
									"Error:Please try again",
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getActivity(),
									"Withdrawal Successful", Toast.LENGTH_SHORT)
									.show();
						}
						pay_reason.setText("");
						setUrl(1);
						hidepDialog();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {

						hidepDialog();
					}
				});
		Volley.newRequestQueue(getActivity()).add(jsonObjReq);

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
