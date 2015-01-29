package com.techila.boa.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.techila.boa.Add_Company;
import com.techila.boa.AlertDialogManager;
import com.techila.boa.R;
import com.techila.boa.NewWidReq;
import com.techila.boa.adapter.UserAdapter;
import com.techila.boa.config.Appconstant;
import com.techila.boa.model.TransactionModel;
import com.techila.boa.util.PrefSingleton;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class UserWid extends Fragment implements OnClickListener {

	ProgressDialog prg;
	Properties prop;
	private String req_url;
	private PrefSingleton mMyPreferences;

	public static ArrayList<TransactionModel> RequestList;
	TransactionModel map;
	UserAdapter req_adp;
	ListView req;
	Button new_req;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.user_requests, container,
				false);

		initViews(rootView);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getActivity());
		RequestList = new ArrayList<TransactionModel>();

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

		req_adp = new UserAdapter(getActivity(), RequestList, "userwid");
		req.setAdapter(req_adp);

		new_req.setOnClickListener(this);
		setUrl();
	}

	private void initViews(View v) {
		req = (ListView) v.findViewById(R.id.req_list);
		new_req = (Button) v.findViewById(R.id.footer_btn);
		prg = new ProgressDialog(getActivity());
	}

	@Override
	public void onClick(View v) {

		Intent intent = new Intent(getActivity(), NewWidReq.class);
		startActivityForResult(intent, 1);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1) {

			if (resultCode == Activity.RESULT_OK) {
				Toast.makeText(getActivity(), "Request sent successfully",
						Toast.LENGTH_SHORT).show();
				setUrl();
			}

			if (resultCode == Activity.RESULT_CANCELED) {

			}
		}
	}

	private void setUrl() {

		req_url = String.format(Appconstant.GET_REQUESTS_URL + "?"
				+ "userID=%s", mMyPreferences.getPreference("LoginId"));
		GetRequests();
	}

	private void GetRequests() {
		showpDialog();
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				req_url, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {
						try {

							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");
							RequestList.clear();

							if ("1".equals(error_code)) {

								JSONArray jArray = jsonObj
										.getJSONArray("result");
								for (int i = 0; i < jArray.length(); i++) {

									JSONObject jsonObj1 = jArray
											.getJSONObject(i);
									String p_type = jsonObj1
											.getString("payment_type");

									if (p_type.equals("Withdrawal")) {

										String status = jsonObj1
												.getString("status");
										// if the payment is accepted then the
										// payment_id is
										// returned as the response
										String Payment = "";
										if (status.equals("Accepted")) {
											Payment = jsonObj1
													.getString("payment_id");
										}

										map = new TransactionModel(
												mMyPreferences
														.getPreference("C_ID"),
												mMyPreferences
														.getPreference("LoginId"),
												mMyPreferences
														.getPreference("user_Type"),
												jsonObj1.getString("payment_from_bank_id"),
												jsonObj1.getString("payment_to_bank_id"),
												jsonObj1.getString("amount"),
												jsonObj1.getString("user_request_id"),
												p_type,
												"",
												Payment,
												jsonObj1.getString("request_created_date"),
												jsonObj1.getString("request_created_time"),
												status, "", jsonObj1
														.getString("fBank"),
												jsonObj1.getString("tBank"));

										RequestList.add(map);
									}
								}
							}
							if (error_code == "2") {
								Toast.makeText(getActivity(),
										"No User Request Details Available",
										Toast.LENGTH_SHORT).show();
							} else if (error_code == "3") {
								Toast.makeText(getActivity(),
										"Withdrawals processed successfully",
										Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (NumberFormatException e) {
							e.printStackTrace();
						} catch (NullPointerException e) {

							// Internet Connection is not present
							AlertDialogManager alert = new AlertDialogManager();
							alert.showAlertDialog(
									getActivity(),
									"Internet Connection Error",
									"Please connect to working Internet connection",
									false);
							// stop executing code by return
							return;
						} catch (Exception e) {
							e.printStackTrace();
						}
						req_adp.notifyDataSetChanged();
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
			prg.setMessage("Loading...");
		prg.show();
		prg.setCanceledOnTouchOutside(false);
	}

	private void hidepDialog() {
		if (prg.isShowing())
			prg.dismiss();
	}
}
