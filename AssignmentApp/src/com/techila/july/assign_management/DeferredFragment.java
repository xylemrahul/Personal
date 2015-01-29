package com.techila.july.assign_management;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.techila.july.assign_management.config.Appconstant;
import com.techila.july.assign_management.util.JSONParser;
import com.techila.july.assign_management.util.PrefSingleton;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeferredFragment extends ListFragment {

    JSONParser jsonParser = new JSONParser();
    ArrayList<HashMap<String, String>> AssignmentList;
    private PrefSingleton mMyPreferences;
    HashMap<String, String> map;
    HashMap<String, String> map1;
    // products JSONArray
    JSONArray inbox = null;
    ProgressDialog prg;
    String Member_Id, jsonArray = null;

    // Inbox JSON url
    private static final String ASSIGNMENT_URL = "http://phbjharkhand.in/AssignmentApplication/Get_Type_Member_Status_Wise_Details.php";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_deferred, container,
                false);

        mMyPreferences = PrefSingleton.getInstance();
        mMyPreferences.Initialize(getActivity());
        // Hashmap for ListView
        AssignmentList = new ArrayList<HashMap<String, String>>();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

        Member_Id = mMyPreferences.getPreference("Mem_Id");

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            // Loading INBOX in Background Thread
            new LoadDeferred().execute();
        }
    }

    class LoadDeferred extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            prg = new ProgressDialog(getActivity());
            prg.setMessage("Loading List ...");
            prg.setIndeterminate(false);
            prg.setCancelable(false);
            prg.show();

            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {

            List<NameValuePair> params1 = new ArrayList<NameValuePair>();

            if ("One Time Job".equals(mMyPreferences.getPreference("JobType"))) {
                params1.add(new BasicNameValuePair("assType", mMyPreferences
                        .getPreference("JobType")));
            } else if ("Short Term Job".equals(mMyPreferences
                    .getPreference("JobType"))) {
                params1.add(new BasicNameValuePair("assType", mMyPreferences
                        .getPreference("JobType")));
            } else if ("Long Term Job".equals(mMyPreferences
                    .getPreference("JobType"))) {
                params1.add(new BasicNameValuePair("assType", mMyPreferences
                        .getPreference("JobType")));
            } else if ("Specific Date Job".equals(mMyPreferences
                    .getPreference("JobType"))) {
                params1.add(new BasicNameValuePair("assType", mMyPreferences
                        .getPreference("JobType")));
            }

            params1.add(new BasicNameValuePair("memberID", Member_Id));
            params1.add(new BasicNameValuePair("status", "Deferred"));

            String error_code = null;

            // getting JSON string from URL
            JSONObject json = jsonParser.makeHttpRequest(ASSIGNMENT_URL,
                    "POST", params1);

            // Check your log cat for JSON response
            Log.d("Deferred JSON: ", json.toString());

            try {
                JSONObject jsonObj = json.getJSONObject("data");
                error_code = jsonObj.getString("Error_Code");
                if ("1".equals(error_code)) {

                    JSONArray jsonArray = null;
                    jsonArray = jsonObj.getJSONArray(Appconstant.TAG_RESULT);
                    // looping through All messages
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);

                        map = new HashMap<String, String>();
                        // Storing each json item in variable
                        String date = c.getString(Appconstant.TAG_DATE);
                        String status = c.getString(Appconstant.TAG_STATUS);
                        String name = c.getString(Appconstant.TAG_ASS_NAME);
                        map.put(Appconstant.TAG_DATE, date);
                        map.put(Appconstant.TAG_ASS_NAME, name);
                        map.put(Appconstant.TAG_STATUS, status);

                        if (status.equals("Deferred")) {
                            // adding HashList to ArrayList
                            AssignmentList.add(map);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return Integer.parseInt(error_code);
        }

        @Override
        protected void onPostExecute(Integer error_num) {
            // TODO Auto-generated method stub
            super.onPostExecute(error_num);
            if (prg.isShowing()) {
                prg.dismiss();
            }

            if (error_num == 2) {

                Toast.makeText(getActivity(),
						"There are no assignments", Toast.LENGTH_LONG).show();
            }

            ListAdapter adapter = new SimpleAdapter(getActivity(),
                    AssignmentList, R.layout.activity_tab_list_view,
                    new String[]{Appconstant.TAG_ASS_NAME, Appconstant.TAG_DATE}, new int[]{
                    R.id.status, R.id.created_date});
            setListAdapter(adapter);

        }
    }
}
