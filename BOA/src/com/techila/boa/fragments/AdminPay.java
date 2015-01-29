package com.techila.boa.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.techila.boa.AlertDialogManager;
import com.techila.boa.ChequeActivity;
import com.techila.boa.R;
import com.techila.boa.config.Appconstant;
import com.techila.boa.model.TransactionModel;
import com.techila.boa.util.PrefSingleton;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class AdminPay extends Fragment implements OnClickListener,Parcelable {

	static String balance;
	static String CompanyId;

	// public static String F_BankId;
	// public static String T_BankId;

	private String[] act_str, all_act_str;
	private String type_arr[] = { "Cheque", "Net Banking" };
	private ArrayAdapter<String> all_act_adp;
	private ArrayAdapter<String> act_adp;
	private ArrayAdapter<String> type_adp;
	protected static ArrayList<HashMap<String, String>> BankList;
	protected static ArrayList<HashMap<String, String>> AllBankList;
	private HashMap<String, String> map;
	private boolean flag = true;
	private String bankUrl, allBankUrl, chkBlncUrl, netTransferUrl;

	Spinner transfer_act, dest_act;
	TextView act_blnc, transfer_type;
	EditText transfer_amt, pay_reason;
	Button pay;

	ProgressDialog prg;
	AlertDialogManager alert;
	private TransactionModel model;
	private PrefSingleton mMyPreferences;
	Properties prop;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.admin_pay, container, false);

		initViews(rootView);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

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
		model.setCompanyId(CompanyId);
		type_adp = new ArrayAdapter<String>(getActivity(), R.layout.row,
				type_arr);
		pay.setEnabled(false);
		setUrl(1);
	}

	private void initViews(View rootView) {

		transfer_act = (Spinner) rootView.findViewById(R.id.tranfer_from);
		dest_act = (Spinner) rootView.findViewById(R.id.tranfer_to);
		act_blnc = (TextView) rootView.findViewById(R.id.act_balance);
		transfer_amt = (EditText) rootView.findViewById(R.id.transfer_amt);
		transfer_type = (TextView) rootView.findViewById(R.id.p_type);
		pay_reason = (EditText) rootView.findViewById(R.id.reason);
		pay = (Button) rootView.findViewById(R.id.pay);

		prg = new ProgressDialog(getActivity());
		alert = new AlertDialogManager();
		BankList = new ArrayList<HashMap<String, String>>();
		AllBankList = new ArrayList<HashMap<String, String>>();
		model = new TransactionModel();

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getActivity());

		pay_reason.setEnabled(false);
		pay.setOnClickListener(this);
		transfer_type.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.p_type:

			new AlertDialog.Builder(getActivity())
					.setTitle("\t \t Select Transfer Type")
					.setAdapter(type_adp,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									transfer_type.setText(type_arr[which]
											.toString());
									model.setPay_type(type_arr[which]
											.toString());
									if (type_arr[which].toString().equals(
											"Cheque")) {
										pay_reason.setEnabled(false);
									} else {
										pay_reason.setEnabled(true);
									}
									if (flag) {
										setUrl(3);
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

					new AlertDialog.Builder(getActivity())
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
					alert.showAlertDialog(getActivity(), "Alert",
							"\t \tPlease enter valid amount", null);
				} else {

					model.setAmount(transfer_amt.getText().toString());
					if (transfer_type.getText().toString().equals("Cheque")) {
						Intent intent = new Intent(getActivity(),
								ChequeActivity.class);
						intent.putExtra("obj", model);
						startActivityForResult(intent, 1);
					} else {
						if (!pay_reason.getText().toString().equals("")) {
							model.setReason(pay_reason.getText().toString());
							setUrl(4);
						} else {
							AlertDialogManager alert = new AlertDialogManager();
							alert.showAlertDialog(getActivity(), "Alert",
									"Please enter all the fields", false);
						}
					}
				}
			} catch (NumberFormatException e) {
				Toast.makeText(getActivity(), "Please enter amount",
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		default:
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1) {

			if (resultCode == Activity.RESULT_OK) {
				Toast.makeText(getActivity(), "Details submitted successfully",
						Toast.LENGTH_SHORT).show();
				setUrl(1);
			}

			if (resultCode == Activity.RESULT_CANCELED) {

			}
		}
	}

	// ----------------LOAD COMPANY SPECIFIC BRANCH IN SPINNER-------------
	private void LoadBank() {

		act_str = new String[BankList.size()];
		for (int i = 0; i < BankList.size(); i++) {
			HashMap<String, String> items = BankList.get(i);
			act_str[i] = items.get(Appconstant.TAG_BANK_NAME);
		}

		act_adp = new ArrayAdapter<String>(getActivity(), R.layout.row, act_str);
		transfer_act.setAdapter(act_adp);

		transfer_act.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				HashMap<String, String> items = BankList.get(position);
				String F_BankId = items.get(Appconstant.TAG_BANK_ID);
				// mMyPreferences.setPreference("FromBank", F_BankId);
				model.setSource(F_BankId);
				setUrl(2);
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
		}

		all_act_adp = new ArrayAdapter<String>(getActivity(), R.layout.row,
				all_act_str);
		dest_act.setAdapter(all_act_adp);

		dest_act.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				HashMap<String, String> items = AllBankList.get(position);
				String T_BankId = items.get(Appconstant.TAG_BANK_ID);
				// mMyPreferences.setPreference("ToBank", T_BankId);
				model.setDestination(T_BankId);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	private void setUrl(int a) {

		switch (a) {
		case 1:
			bankUrl = String.format(Appconstant.GET_BANK_URL + "?"
					+ "companyID=%s", CompanyId);

			GetBankDetails();
			break;

		case 2:
			chkBlncUrl = String.format(Appconstant.CHECK_BLNC_URL + "?"
					+ "bankID=%s", model.getSource());
			CheckBalance();
			break;

		case 3:
			allBankUrl = String.format(Appconstant.GET_ALL_BANK_URL, "");
			GetAllBankDetails();
			break;

		case 4:
			netTransferUrl = String
					.format(Appconstant.PAYMENT_DETAILS_URL
							+ "?"
							+ "companyID=%s&userID=%s&userType=%s&fromBankID=%s&toBankID=%s&amount=%s"
							+ "&paymentReason=%s&paymentType=%s", CompanyId,
							mMyPreferences.getPreference("LoginId"),
							mMyPreferences.getPreference("user_Type"),
							model.getSource(), "", model.getAmount(),
							model.getReason(), model.getPay_type());

			NetBankingTransfer();
			break;
		default:
			break;
		}
	}

	// ----------------xxxxxxxxxxxxxxxxxxxx-----------------------

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
							BankList.clear();

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

									BankList.add(map);
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

	// -------------------------xxxxxxxxxxxxxxxxx-------------------------

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
						transfer_amt.setText(balance);
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

	// ------------------xxxxxxxxxxxxxx----------------------

	// ---------------GETTING THE LIST OF ALL THE BANKS-------------------

	private void GetAllBankDetails() {

		showpDialog();
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				allBankUrl, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {

						try {
							Log.d("Inbox JSON: ", json.toString());

							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");
							AllBankList.clear();

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

									AllBankList.add(map);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						pay.setEnabled(true);

						LoadAllBanks();
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

	// ---------------------xxxxxxxxxxxxxxxxxxx----------------------

	private void NetBankingTransfer() {
		showpDialog();
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				netTransferUrl, null, new Response.Listener<JSONObject>() {

					String error_code;
					@Override
					public void onResponse(JSONObject json) {

						Log.d("Inbox JSON: ", json.toString());
						try {
							Log.d("Inbox JSON: ", json.toString());

							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");

						} catch (JSONException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (error_code == "3") {
							Toast.makeText(getActivity(),
									"Insufficient funds,cannot transfer",
									Toast.LENGTH_LONG).show();
						} else if (error_code == "2") {
							Toast.makeText(getActivity(),
									"Cannot save details", Toast.LENGTH_SHORT)
									.show();
						}
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

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
}