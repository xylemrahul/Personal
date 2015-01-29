package com.techila.boa.fragments;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.techila.boa.R;
import com.techila.boa.adapter.UserAdapter;
import com.techila.boa.config.Appconstant;
import com.techila.boa.model.TransactionModel;
import com.techila.boa.util.JSONParser;
import com.techila.boa.util.PrefSingleton;

import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class AdminWidReq extends Fragment {

	Button new_req;
	ListView req;

	private PrefSingleton mMyPreferences;
	private ProgressDialog prg;
	private JSONParser jsonParser = new JSONParser();
	UserAdapter req_adp;

	boolean flag;
	ArrayList<TransactionModel> widList;
	TransactionModel map;

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

		widList = new ArrayList<TransactionModel>();

		ViewGroup layout = (ViewGroup) new_req.getParent();
		if (layout != null) {
			layout.removeView(new_req);
		}

		req_adp = new UserAdapter(getActivity(), widList, "adminwid");
		req.setAdapter(req_adp);
	}

	private void initViews(View v) {

		req = (ListView) v.findViewById(R.id.req_list);
		new_req = (Button) v.findViewById(R.id.footer_btn);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			if (!flag) {
				new GetRequestbyCompany().execute();
				flag = true;
			}
		}
	}

	private class GetRequestbyCompany extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			prg = new ProgressDialog(getActivity());
			prg.setIndeterminate(true);
			prg.setMessage("Fetching Requests...");
			prg.setCanceledOnTouchOutside(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			params1.add(new BasicNameValuePair("companyID", mMyPreferences
					.getPreference("C_ID")));

			String error_code = null;
			Log.e("URL ", "is" + Appconstant.GET_COMPANYREQ_URL);

			try {
				// getting JSON string from URL
				JSONObject json = jsonParser.makeHttpRequest(
						Appconstant.GET_COMPANYREQ_URL, "POST", params1);

				// Check your log cat for JSON response
				Log.d("Inbox JSON: ", json.toString());

				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
				widList.clear();

				if ("1".equals(error_code)) {

					JSONArray jArray = jsonObj.getJSONArray("result");
					for (int i = 0; i < jArray.length(); i++) {

						JSONObject jsonObj1 = jArray.getJSONObject(i);
						String p_type = jsonObj1.getString("payment_type");

						map = new TransactionModel(
								mMyPreferences.getPreference("C_ID"), "", "",
								jsonObj1.getString("payment_from_bank_id"),
								jsonObj1.getString("payment_to_bank_id"),
								jsonObj1.getString("amount"),
								jsonObj1.getString("user_request_id"), p_type,
								"", "",
								jsonObj1.getString("request_created_date"),
								jsonObj1.getString("request_created_time"),
								jsonObj1.getString("status"),
								jsonObj1.getString("name"),
								jsonObj1.getString("fBank"),
								jsonObj1.getString("tBank"));

						if (p_type.equals("Withdrawal")) {
							widList.add(map);
						}
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
				Toast.makeText(getActivity(),
						"There are no withdrawal requests", Toast.LENGTH_SHORT)
						.show();
			}
			req_adp.notifyDataSetChanged();
		}
	}
}