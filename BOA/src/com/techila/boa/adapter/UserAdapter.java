package com.techila.boa.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.techila.boa.AlertDialogManager;
import com.techila.boa.ChequeActivity;
import com.techila.boa.R;
import com.techila.boa.config.Appconstant;
import com.techila.boa.fragments.UserPay;
import com.techila.boa.fragments.UserWid;
import com.techila.boa.model.TransactionModel;
import com.techila.boa.util.JSONParser;
import com.techila.boa.util.PrefSingleton;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UserAdapter extends BaseAdapter {

	private static LayoutInflater inflater = null;
	ArrayList<TransactionModel> items = null;
	private TransactionModel element;
	Activity context;
	TransactionModel map;
	private String act, id, F_BankId, amt, req_url;
	AlertDialog.Builder alert;
	private ProgressDialog prg;
	private PrefSingleton mMyPreferences;

	public UserAdapter(Activity context, ArrayList<TransactionModel> items,
			String act) {

		this.items = items;
		this.act = act;
		this.context = context;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		prg = new ProgressDialog(context);
	}

	@Override
	public int getCount() {

		return items.size();
	}

	@Override
	public Object getItem(int position) {

		return items.get(position);
	}

	@Override
	public long getItemId(int position) {

		return 0;
	}

	private class ViewHolder {
		TextView date, status;
		ImageView details;
		Button remove;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View row = convertView;
		ViewHolder vh = null;

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(context);

		if (row == null) {
			row = inflater.inflate(R.layout.custom_request, null);
			vh = new ViewHolder();
			vh.date = (TextView) row.findViewById(R.id.req_date);
			vh.status = (TextView) row.findViewById(R.id.req_status);
			vh.details = (ImageView) row.findViewById(R.id.details);
			vh.remove = (Button) row.findViewById(R.id.remove);

			row.setTag(vh);
		} else {

			vh = (ViewHolder) row.getTag();
		}

		if (act.equals("adminpay") || (act.equals("adminwid"))) {

			getCompanyRequests(position, vh);
			row.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					element = items.get(position);
					id = element.getReqId();
					ShowDialog();
				}
			});
		} else {

			getRequestDetails(position, vh, row);

		}

		vh.details.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				element = items.get(position);
				String sourceBank = element.getSource();
				String destBank = element.getDestination();
				String amount = element.getAmount();
				String date = element.getReq_dt();
				String time = element.getReq_time();
				String type = element.getPay_type();
				showDialogDetails(sourceBank, destBank, amount, date, type,
						time);
			}
		});
		return row;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {

			if (resultCode == Activity.RESULT_OK) {
				Toast.makeText(context, "Request sent successfully",
						Toast.LENGTH_SHORT).show();
				// new GetRequests().execute();
			}

			if (resultCode == Activity.RESULT_CANCELED) {

			}
		}
	}

	private void getCompanyRequests(int position, ViewHolder vh) {

		element = items.get(position);
		String name = element.getName();
		String date = element.getReq_dt();
		vh.date.setText(name);
		vh.status.setText(date);
	}

	private void getRequestDetails(int position, ViewHolder vh, View row) {

		element = items.get(position);
		String date = element.getReq_dt();
		String status = element.getStatus();

		vh.date.setText(date);
		vh.status.setText(status);

		if (vh.status.getText().equals("Accepted")) {

			vh.remove.setVisibility(ImageView.INVISIBLE);
			row.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (act.equals("userpay")) {
						if (element.getPay_type().equals("Net Banking")) {
							setUrl(5, "");
						} else {
							Intent intent = new Intent(context,
									ChequeActivity.class);
							intent.putExtra("obj", element);
							((Activity) context).startActivityForResult(intent,
									1);
						}
					} else if (act.equals("userwid")) {
						setUrl(3, "");
					}
				}
			});
		} else if (vh.status.getText().equals("Pending")) {

			vh.remove.setVisibility(ImageView.INVISIBLE);
		} else if (vh.status.getText().equals("Declined")) {
			vh.remove.setVisibility(ImageView.VISIBLE);
			vh.remove.setTag(position);
			vh.remove.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int pos = (Integer) v.getTag();
					try {
						if (act.equals("upay")) {
							UserPay.RequestList.remove(pos);
						} else if (act.equals("uwid")) {
							UserWid.RequestList.remove(pos);
						}
						notifyDataSetChanged();
					} catch (ArrayIndexOutOfBoundsException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	private void showDialogDetails(String sBank, String dBank, String amt,
			String date, String Ptype, String time) {

		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = layoutInflater.inflate(R.layout.dialog_bank_details,
				null);

		TextView Date = (TextView) view.findViewById(R.id.req_date);
		TextView Source_bank = (TextView) view.findViewById(R.id.tv_fbank);
		TextView Dest_bank = (TextView) view.findViewById(R.id.tv_dbank);
		TextView type = (TextView) view.findViewById(R.id.tv_type);
		TextView amount = (TextView) view.findViewById(R.id.tv_amt);

		Date.setText(date + "\t" + time);
		Source_bank.setText(sBank);
		Dest_bank.setText(dBank);
		type.setText(Ptype);
		amount.setText(amt);

		alert.setView(view);
		alert.show();
	}

	private void ShowDialog() {

		alert = new AlertDialog.Builder(context);
		alert.setMessage("\t \t What do you want to do ?");
		alert.setPositiveButton("Accept",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						setUrl(1, "Accepted");
						dialog.dismiss();
					}
				});
		alert.setNegativeButton("Decline",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						setUrl(1, "Declined");
						dialog.dismiss();
					}
				});
		alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alert.setCancelable(false);
		alert.show();
	}

	private void setUrl(int i, String str) {

		switch (i) {
		case 1:

			req_url = String.format(Appconstant.UPDATE_STATUS_URL + "?"
					+ "user_request_id=%s" + "&status=%s", element.getReqId(),
					str);
			CheckStatus();
			break;
		case 2:

			req_url = String.format(Appconstant.GET_COMPANYREQ_URL + "?"
					+ "companyID=%s", element.getCompanyId());
			getRequestbyCompany();
			break;

		case 3:
//			giving harcoded reason as the reason while sending request is not being returned as the response in 
//			required services.
//			To be implemented in the next version.Need to add "reason" param in "newrequest" service and need to return
//			it as response when getting the requests in Userpay and UserWid.
			req_url = String
					.format(Appconstant.PAYMENT_DETAILS_URL
							+ "?"
							+ "companyID=%s&userID=%s&userType=%s"
							+ "&fromBankID=%s&toBankID=%s&payment_id=%s&amount=%s&paymentType=%s&paymentReason=%s"
							+ "&user_request_id=%s", element.getCompanyId(),
							element.getLoginId(), element.getUserType(),
							element.getSource(), "", element.getPay_Id(),
							element.getAmount(), element.getPay_type(),
							"withdrawal", element.getReqId(),
							element.getCompanyId());
			Withdraw();
			break;

		case 4:

			req_url = String.format(Appconstant.GET_REQUESTS_URL + "?"
					+ "userID=%s", element.getLoginId());
			ReloadList();
			break;

		case 5:
//			giving harcoded reason as the reason while sending request is not being returned as the response in 
//			required services.
//			To be implemented in the next version.Need to add "reason" param in "newrequest" service and need to return
//			it as response when getting the requests in Userpay and UserWid.
			req_url = String
					.format(Appconstant.PAYMENT_DETAILS_URL
							+ "?"
							+ "companyID=%s&userID=%s&userType=%s"
							+ "&fromBankID=%s&toBankID=%s&payment_id=%s&amount=%s&paymentType=%s&paymentReason=%s"
							+ "&user_request_id=%s", element.getCompanyId(),
							element.getLoginId(), element.getUserType(),
							element.getSource(), element.getDestination(),
							element.getPay_Id(), element.getAmount(), "net",
							"net%20transfer", element.getReqId());
			
			NetBankingTransfer();
			break;

		case 6:

			req_url = String.format(Appconstant.GET_REQUESTS_URL + "?"
					+ "userID=%s", element.getLoginId());
			ReloadListPayment();
			break;
		}
	}

	// ---------------------------
	private void CheckStatus() {

		showpDialog();
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				req_url, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {
						try {
							// Check your log cat for JSON response
							Log.d("Inbox JSON: ", json.toString());

							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");
							if (error_code.equals("1")) {
								Toast.makeText(context,
										"Status successfully updated",
										Toast.LENGTH_SHORT).show();
								setUrl(2, "");
							} else if (error_code.equals("2")) {
								Toast.makeText(context, "Status update failed",
										Toast.LENGTH_SHORT).show();
							} else if (error_code.equals("3")) {

								Toast.makeText(context,
										"Insufficent funds for transfer",
										Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						hidepDialog();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						hidepDialog();
					}
				});
		Volley.newRequestQueue(context).add(jsonObjReq);
	}

	private void getRequestbyCompany() {

		showpDialog();
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				req_url, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {
						items.clear();
						try {
							// Check your log cat for JSON response
							Log.d("Inbox JSON: ", json.toString());

							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");
							if ("1".equals(error_code)) {

								JSONArray jArray = jsonObj
										.getJSONArray("result");
								for (int i = 0; i < jArray.length(); i++) {
									map = new TransactionModel();
									JSONObject jsonObj1 = jArray
											.getJSONObject(i);
									TransactionModel map = new TransactionModel();

									map.setSource(jsonObj1
											.getString("payment_from_bank_id"));
									map.setDestination(jsonObj1
											.getString("payment_to_bank_id"));
									map.setReq_dt(jsonObj1
											.getString("request_created_date"));
									map.setReq_time(jsonObj1
											.getString("request_created_time"));
									map.setName(jsonObj1.getString("name"));
									map.setReqId(jsonObj1
											.getString("user_request_id"));
									map.setAmount(jsonObj1.getString("amount"));
									map.setPay_type(jsonObj1
											.getString("payment_type"));
									map.setStatus(jsonObj1.getString("status"));
									map.setReason(jsonObj1
											.getString("payment_reason"));

									if (map.getPay_type().equals("Cheque")
											|| map.getPay_type().equals(
													"Net Banking")) {
										items.add(map);
									} else {
										error_code = "2";
									}
								}
							}
						} catch (JSONException e) {

							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						notifyDataSetChanged();
						hidepDialog();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e("Volley", "Error" + arg0);
						hidepDialog();
					}
				});
		Volley.newRequestQueue(context).add(jsonObjReq);
	}

	private void Withdraw() {

		showpDialog();
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				req_url, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {
						// Check your log cat for JSON response
						Log.d("Inbox JSON: ", json.toString());
						try {

							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");
							if (error_code.equals("2")) {

							} else if (error_code.equals("3")) {

								Toast.makeText(context,
										"Insufficient funds,cannot transfer",
										Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(context, "Payment Successfull",
										Toast.LENGTH_SHORT).show();
								setUrl(4, "");
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						hidepDialog();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						hidepDialog();
					}
				});

		Volley.newRequestQueue(context).add(jsonObjReq);
	}

	private void ReloadList() {

		showpDialog();
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				req_url, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {
						try {

							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");
							items.clear();

							if ("1".equals(error_code)) {

								JSONArray jArray = jsonObj
										.getJSONArray("result");
								for (int i = 0; i < jArray.length(); i++) {

									JSONObject jsonObj1 = jArray
											.getJSONObject(i);
									String p_type = jsonObj1
											.getString("payment_type");

									if (p_type.equals("Cheque")
											|| p_type.equals("Net Banking")) {

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

										items.add(map);
									}
								}
							}
							if (error_code.equals("3")) {
								Toast.makeText(context,
										"Withdrawals processed successfully",
										Toast.LENGTH_SHORT).show();
							}
							if (error_code.equals("2")) {
								Toast.makeText(context,
										"There are no pending withdrawals",
										Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (NumberFormatException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						notifyDataSetChanged();
						hidepDialog();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {

					}
				});
		Volley.newRequestQueue(context).add(jsonObjReq);
	}

	private void NetBankingTransfer() {

		showpDialog();
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				req_url, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {
						try {
							// Check your log cat for JSON response
							Log.d("Inbox JSON: ", json.toString());

							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");

							if (error_code.equals("3")) {
								Toast.makeText(context,
										"Insufficient funds,cannot transfer",
										Toast.LENGTH_LONG).show();
							} else if (error_code.equals("2")) {
								Toast.makeText(context, "Cannot save details",
										Toast.LENGTH_SHORT).show();
							}else if(error_code.equals("1")){
								setUrl(6, "");
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						hidepDialog();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						hidepDialog();
					}
				});
		Volley.newRequestQueue(context).add(jsonObjReq);
	}

	private void ReloadListPayment() {

		showpDialog();
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				req_url, null, new Response.Listener<JSONObject>() {
					String error_code;

					@Override
					public void onResponse(JSONObject json) {
						try {
							// Check your log cat for JSON response
							Log.d("Inbox JSON: ", json.toString());

							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");
							items.clear();

							if ("1".equals(error_code)) {

								JSONArray jArray = jsonObj
										.getJSONArray("result");
								for (int i = 0; i < jArray.length(); i++) {
									map = new TransactionModel();

									JSONObject jsonObj1 = jArray
											.getJSONObject(i);
									String p_type = jsonObj1
											.getString("payment_type");
									map.setSource(jsonObj1
											.getString("payment_from_bank_id"));
									map.setDestination(jsonObj1
											.getString("payment_to_bank_id"));
									map.setFBankName(jsonObj1
											.getString("fBank"));
									map.setTBankName(jsonObj1
											.getString("tBank"));
									map.setAmount(jsonObj1.getString("amount"));
									map.setPay_type(p_type);
									map.setStatus(jsonObj1.getString("status"));
									map.setReq_dt(jsonObj1
											.getString("request_created_date"));
									map.setReqId(jsonObj1
											.getString("user_request_id"));
									if (p_type.equals("Cheque")
											|| p_type.equals("Net Banking")) {
										items.add(map);
									}
								}
							}
							if (error_code.equals("2")) {
								Toast.makeText(context,
										"There are no pending Payments",
										Toast.LENGTH_SHORT).show();
							}
							notifyDataSetChanged();
						} catch (JSONException e) {

							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						hidepDialog();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						hidepDialog();
					}
				});
		Volley.newRequestQueue(context).add(jsonObjReq);
	}

	private void hidepDialog() {
		if (prg.isShowing())
			prg.dismiss();
	}

	private void showpDialog() {
		if (!prg.isShowing())
			prg.setMessage("Loading...");
		prg.show();
		prg.setCanceledOnTouchOutside(false);
	}
}