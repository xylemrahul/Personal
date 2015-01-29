package com.techila.july.assign_management.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.techila.july.assign_management.Assign_Details;
import com.techila.july.assign_management.Grp_Manage;
import com.techila.july.assign_management.JobActivity;
import com.techila.july.assign_management.Mem_Manage;
import com.techila.july.assign_management.R;
import com.techila.july.assign_management.config.Appconstant;
import com.techila.july.assign_management.util.JSONParser;
import com.techila.july.assign_management.util.PrefSingleton;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CustomAdapterListView extends BaseAdapter {

	ArrayList<HashMap<String, String>> items = null;
	HashMap<String, String> map = null;
	Activity context;
	private static LayoutInflater inflater = null;
	String act, assignment_id, description;
	View view = null, view1 = null;
	private RadioGroup Grpstatus;
	AlertDialog.Builder alertdialog = null;
	private RadioButton status, rd_prg, rd_done, rd_defer, rd_cancel;
	ProgressDialog prg = null;
	PrefSingleton mMyPreferences = null;

	JSONParser jsonParser = new JSONParser();

	private static final String STATUS_URL = "http://phbjharkhand.in/AssignmentApplication/Update_Assignment_Status.php";

	public CustomAdapterListView(Activity context,
			ArrayList<HashMap<String, String>> items) {
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.items = items;

		this.context = context;
		if (context instanceof Mem_Manage) {
			act = "Mem";
		} else if (context instanceof Grp_Manage) {
			act = "Grp";
		} else if (context instanceof Assign_Details) {
			act = "Ass";
		}
	}

	@Override
	public int getCount() {

		return items.size();
	}

	@Override
	public Object getItem(int arg0) {

		return null;
	}

	@Override
	public long getItemId(int arg0) {

		return 0;
	}

	private class ViewHolder {
		TextView tvName;
		ImageView tvDetails, tv_assign;
	}

	public void sortByDate() {
		ArrayList<HashMap<String, String>> date = items;
		Collections.sort(date, new Comparator<HashMap<String, String>>() {

			@Override
			public int compare(HashMap<String, String> lhs,
					HashMap<String, String> rhs) {

				return lhs.get(Appconstant.TAG_DATE).compareTo(
						rhs.get(Appconstant.TAG_DATE));
			}
		});
		notifyDataSetChanged();
	}

	// getView is called as many times as there are items in the listview

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(context);

		View row = convertView;
		ViewHolder vh = null;
		if (row == null) {

			row = inflater.inflate(R.layout.activity_custom_list_manage, null);
			vh = new ViewHolder();
			vh.tvName = (TextView) row.findViewById(R.id.name);
			vh.tvDetails = (ImageView) row.findViewById(R.id.viewDetails);
			vh.tv_assign = (ImageView) row.findViewById(R.id.assign);
			row.setTag(vh);

		} else {
			vh = (ViewHolder) row.getTag();
		}

		if (act.equals("Mem")) {
			getMember(position, vh);

		} else if (act.equals("Grp")) {
			getGroup(position, vh);
			ViewGroup layout = (ViewGroup) vh.tv_assign.getParent();
			if (layout != null) {
				layout.removeView(vh.tv_assign);
			}
		} else if (act.equals("Ass")) {
			getAssignment(position, vh);
			ViewGroup layout = (ViewGroup) vh.tv_assign.getParent();
			if (layout != null) {
				layout.removeView(vh.tv_assign);
			}
		}

		vh.tvDetails.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (act.equals("Grp")) {
					HashMap<String, String> element = items.get(position);
					String CreatedDate = element.get(Appconstant.TAG_DATE);
					String abbreviation = element.get(Appconstant.TAG_ABBR);
					String GroupName = element.get(Appconstant.TAG_GRP_NAME);
					String description = element.get(Appconstant.TAG_DESC);
					showDialogGroup(CreatedDate, abbreviation, GroupName,
							description);
					Log.e("", "Group");

				} else if (act.equals("Mem")) {
					HashMap<String, String> element = items.get(position);
					String CreatedDate = element.get(Appconstant.TAG_DATE);
					String contact = element.get(Appconstant.TAG_CONTACT);
					String MemberName = element.get(Appconstant.TAG_MEM_NAME);
					String GroupName = element.get(Appconstant.TAG_GRP_NAME);
					String age = element.get(Appconstant.TAG_AGE);
					String sex = element.get(Appconstant.TAG_SEX);
					String mail_address = element.get(Appconstant.TAG_EMAIL);
					String postal_address = element
							.get(Appconstant.TAG_ADDRESS);
					String pass = element.get(Appconstant.TAG_PASS);
					String uname = element.get(Appconstant.TAG_UNAME);
					String type = element.get(Appconstant.TAG_TYPE);
					showDialogMember(CreatedDate, contact, MemberName,
							GroupName, age, sex, mail_address, postal_address,
							pass, uname, type);

					Log.e("", "Member");
				} else if (act.equals("Ass")) {

					HashMap<String, String> element = items.get(position);
					String CreatedDate = element.get(Appconstant.TAG_DATE);
					String Job_Date = element.get(Appconstant.TAG_JOB_DATE);
					String description = element.get(Appconstant.TAG_DESC);
					String AssignmentName = element
							.get(Appconstant.TAG_ASS_NAME);
					String MemberName = element.get(Appconstant.TAG_MEM_NAME);
					String GroupName = element.get(Appconstant.TAG_GRP_NAME);
					String Status = element.get(Appconstant.TAG_STATUS);
					String priority = element.get(Appconstant.TAG_PRIORITY);
					String type = element.get(Appconstant.TAG_TYPE);
					String recurrence = element.get(Appconstant.TAG_RECURRENCE);
					showDialogAssignment(CreatedDate, Job_Date, description,
							AssignmentName, MemberName, GroupName, Status,
							priority, type, recurrence);

					Log.e("", "Assignment");
				}
			}
		});

		vh.tv_assign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (act.equals("Mem")) {

					HashMap<String, String> element = items.get(position);
					String MemberId = element.get(Appconstant.TAG_MEM_ID);
					String grpId = element.get(Appconstant.TAG__GRP_ID);
					mMyPreferences.setPreference("Mem_Id", MemberId);
					mMyPreferences.setPreference("Grp_ID", grpId);
					mMyPreferences.setPreference("context", "mem_admin");
					Intent intent = new Intent(context, JobActivity.class);
					context.startActivity(intent);
				}
			}
		});

		row.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (act.equals("Ass")) {

					HashMap<String, String> element = items.get(position);
					String Status = element.get(Appconstant.TAG_STATUS);
					assignment_id = element.get(Appconstant.TAG_ASS_ID);
					alertdialog = new AlertDialog.Builder(context);
					alertdialog.setTitle("The Current status is " + Status);

					LayoutInflater layoutInflater = (LayoutInflater) context
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

					view = layoutInflater.inflate(R.layout.dialog_set_status,
							null);

					alertdialog.setMessage(" Change the current status to");

					Grpstatus = (RadioGroup) view.findViewById(R.id.status);
					rd_prg = (RadioButton) Grpstatus
							.findViewById(R.id.inProgress);
					rd_done = (RadioButton) Grpstatus.findViewById(R.id.done);
					rd_defer = (RadioButton) Grpstatus
							.findViewById(R.id.deferred);
					rd_cancel = (RadioButton) Grpstatus
							.findViewById(R.id.cancel);

					if (Status.equals("Pending")) {

						rd_done.setEnabled(false);
						rd_defer.setEnabled(false);
						rd_cancel.setEnabled(false);

						showDialog();

					} else if (Status.equals("InProgress")) {
						rd_prg.setEnabled(false);
						rd_done.setChecked(true);

						showDialog();
					}

				}
			}
		});

		return row;
	}

	// Calling methods definitions
	protected void showDialog() {

		alertdialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						int i = Grpstatus.getCheckedRadioButtonId();
						status = (RadioButton) view.findViewById(i);
						if ((status.getText().equals("Done"))
								|| (status.getText().equals("Deferred"))
								|| (status.getText().equals("Cancel"))) {

							final AlertDialog.Builder alert = new AlertDialog.Builder(
									context);
							alert.setCancelable(false);
							alert.setTitle("\t \t \t \t" + "Add Description");
							LayoutInflater layoutInflater = (LayoutInflater) context
									.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

							view1 = layoutInflater.inflate(
									R.layout.dialog_feedback, null);
							((ViewGroup) view.getParent()).removeView(view);

							final EditText desc = (EditText) view1
									.findViewById(R.id.fd_desc);
							alert.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog1,
												int arg1) {

											description = desc.getText()
													.toString().trim();
											if (description.equals("")) {
												Toast.makeText(
														context,
														"Please give details of status",
														Toast.LENGTH_SHORT)
														.show();
											} else {
												dialog1.dismiss();
												new changeStatus().execute();
											}
										}
									});
							alert.setView(view1);
							alert.show();
						} else {
							dialog.dismiss();
							new changeStatus().execute();
						}
					}
				});
		alertdialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();
					}
				});

		alertdialog.setView(view);
		alertdialog.show();
	}

	class changeStatus extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			prg = new ProgressDialog(context);
			prg.setMessage("Saving status ...");
			prg.setIndeterminate(false);
			prg.setCancelable(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			List<NameValuePair> list = new ArrayList<NameValuePair>();
			list.add(new BasicNameValuePair("AssignmentID", assignment_id));
			list.add(new BasicNameValuePair("Status", status.getText()
					.toString().replace(" ", "")));
			list.add(new BasicNameValuePair("Description", description));
			String error_code = null;
			Log.e("URL ", "is" + STATUS_URL);
			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(STATUS_URL, "POST",
					list);
			Log.d("Inbox JSON: ", json.toString());

			try {

				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
			} catch (JSONException e) {

				e.printStackTrace();
			} catch (Exception e) {

				e.printStackTrace();
			}

			return Integer.parseInt(error_code);
		}

		@Override
		protected void onPostExecute(Integer error_num) {

			if (prg.isShowing()) {
				prg.dismiss();
			}
			if (error_num == 1) {
				Toast.makeText(context, "Status successfully updated",
						Toast.LENGTH_SHORT).show();
				new LoadOnStartAssignment().execute();
			} else if (error_num == 2) {
				Toast.makeText(context, "Status update failed",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	protected void showDialogGroup(String createdDate, String abbreviation,
			String groupName, String description) {

		final AlertDialog.Builder alert = new AlertDialog.Builder(context);

		// alert.setTitle(" " + "Details of " + " " + groupName);
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = layoutInflater.inflate(R.layout.dialog_grp_details,
				null);

		TextView title = (TextView) view.findViewById(R.id.title_text);
		title.setText(" " + groupName.toUpperCase());

		TextView name = (TextView) view.findViewById(R.id.tv_cname);
		TextView desc = (TextView) view.findViewById(R.id.tv_desc);
		TextView Abbr = (TextView) view.findViewById(R.id.tv_abbreviation);
		TextView date = (TextView) view.findViewById(R.id.tv_date);
		name.setText(groupName);
		Abbr.setText(abbreviation);
		desc.setText(description);
		date.setText(createdDate);

		alert.setView(view);
		alert.show();
	}

	protected void showDialogMember(String createdDate, String contact,
			String memberName, String grpName, String age, String sex,
			String mail_address, String postal_address, String pass,
			String uname, String type) {

		final AlertDialog.Builder alert = new AlertDialog.Builder(context);

		// alert.setTitle(" " + " Details of " + " " + memberName);
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = layoutInflater.inflate(R.layout.dialog_mem_details,
				null);

		TextView title = (TextView) view.findViewById(R.id.title_text);
		title.setText(" " + memberName.toUpperCase());

		TextView name = (TextView) view.findViewById(R.id.tv_memname);
		TextView grpname = (TextView) view.findViewById(R.id.tv_grpname);
		TextView date = (TextView) view.findViewById(R.id.tv_date);
		TextView phone = (TextView) view.findViewById(R.id.tv_contact);
		TextView Age = (TextView) view.findViewById(R.id.tv_age);
		TextView Sex = (TextView) view.findViewById(R.id.tv_sex);
		TextView Email = (TextView) view.findViewById(R.id.tv_email);
		TextView address = (TextView) view.findViewById(R.id.tv_address);
		TextView password = (TextView) view.findViewById(R.id.tv_pass);
		TextView username = (TextView) view.findViewById(R.id.tv_uname);
		TextView Type = (TextView) view.findViewById(R.id.tv_type);

		name.setText(memberName);
		grpname.setText(grpName);
		date.setText(createdDate);
		phone.setText(contact);
		Age.setText(age);
		Sex.setText(sex);
		Email.setText(mail_address);
		address.setText(postal_address);
		password.setText(pass);
		username.setText(uname);
		Type.setText(type);

		alert.setView(view);
		alert.show();

	}

	protected void showDialogAssignment(String createdDate, String job_Date,
			String description, String assignmentName, String memberName,
			String groupName, String status, String priority, String type,
			String recurrence) {

		final AlertDialog.Builder alert = new AlertDialog.Builder(context);

		// alert.setTitle(" " + "Details of " + " " + assignmentName);
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = layoutInflater.inflate(
				R.layout.dialog_assignment_details, null);

		TextView title = (TextView) view.findViewById(R.id.title_text);
		title.setText(" " + assignmentName.toUpperCase());

		TextView name = (TextView) view.findViewById(R.id.tv_assname);
		TextView memname = (TextView) view.findViewById(R.id.tv_memname);
		TextView grpname = (TextView) view.findViewById(R.id.tv_grpname);
		TextView created_date = (TextView) view.findViewById(R.id.tv_date);
		TextView job_date = (TextView) view.findViewById(R.id.tv_job);
		TextView Status = (TextView) view.findViewById(R.id.tv_status);
		TextView Priority = (TextView) view.findViewById(R.id.tv_priority);
		TextView Type = (TextView) view.findViewById(R.id.tv_type);
		TextView Recurrence = (TextView) view.findViewById(R.id.tv_recurrence);
		TextView desc = (TextView) view.findViewById(R.id.tv_desc);

		name.setText(assignmentName);
		memname.setText(memberName);
		grpname.setText(groupName);
		created_date.setText(createdDate);
		job_date.setText(job_Date);
		Status.setText(status);
		Priority.setText(priority);
		Type.setText(type);
		Recurrence.setText(recurrence);
		desc.setText(description);

		alert.setView(view);
		alert.show();
	}

	protected void getGroup(int position, ViewHolder vh) {

		HashMap<String, String> element = items.get(position);
		String name = element.get("groupName");
		vh.tvName.setText(name);
	}

	protected void getMember(int position, ViewHolder vh) {

		HashMap<String, String> element = items.get(position);
		String name = element.get("memberName");
		vh.tvName.setText(name);
	}

	protected void getAssignment(int position, ViewHolder vh) {

		HashMap<String, String> element = items.get(position);
		String name = element.get("assignmentName");
		vh.tvName.setText(name);
	}

	class LoadOnStartAssignment extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(context);
			prg.setMessage("Loading List ...");
			prg.setIndeterminate(false);
			prg.setCancelable(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {

			String error_code = null;
			JSONObject json = null;
			// Building Parameters
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

			if ("Assignee".equals(mMyPreferences.getPreference("Context"))) {
				params1.add(new BasicNameValuePair("memberID", mMyPreferences
						.getPreference("Mem_Id")));
				Log.e("URL ", "is" + Assign_Details.ASSIGNMENT_URL_ASSIGNEE);
				// getting JSON string from URL
				json = jsonParser
						.makeHttpRequest(
								Assign_Details.ASSIGNMENT_URL_ASSIGNEE, "POST",
								params1);

			} else {
				Log.e("URL ", "is" + Assign_Details.ASSIGNMENT_URL);
				// getting JSON string from URL
				json = jsonParser.makeHttpRequest(
						Assign_Details.ASSIGNMENT_URL, "POST", params1);
			}

			// Check your log cat for JSON response
			Log.d("Inbox JSON: ", json.toString());

			try {
				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");
				if ("1".equals(error_code)) {
					items.clear();
					JSONArray jsonArray = null;
					jsonArray = jsonObj.getJSONArray(Appconstant.TAG_RESULT);
					// looping through All messages
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject c = jsonArray.getJSONObject(i);

						map = new HashMap<String, String>();
						// Storing each json item in variable
						String id = c.getString(Appconstant.TAG_ASS_ID);
						String name = c.getString(Appconstant.TAG_ASS_NAME);
						String memname = c.getString(Appconstant.TAG_MEM_NAME);
						String grpname = c.getString(Appconstant.TAG_GRP_NAME);
						String date = c.getString(Appconstant.TAG_DATE);
						String status = c.getString(Appconstant.TAG_STATUS);
						String priority = c.getString(Appconstant.TAG_PRIORITY);
						String assgn_type = c.getString(Appconstant.TAG_TYPE);
						String recurrence = c
								.getString(Appconstant.TAG_RECURRENCE);
						String description = c.getString(Appconstant.TAG_DESC);
						String job_date = c.getString(Appconstant.TAG_JOB_DATE);

						// adding each child node to HashMap key => value
						map.put(Appconstant.TAG_ASS_ID, id);
						map.put(Appconstant.TAG_ASS_NAME, name);
						map.put(Appconstant.TAG_MEM_NAME, memname);
						map.put(Appconstant.TAG_GRP_NAME, grpname);
						map.put(Appconstant.TAG_DATE, date);
						map.put(Appconstant.TAG_STATUS, status);
						map.put(Appconstant.TAG_DESC, description);
						map.put(Appconstant.TAG_JOB_DATE, job_date);
						map.put(Appconstant.TAG_PRIORITY, priority);
						map.put(Appconstant.TAG_TYPE, assgn_type);
						map.put(Appconstant.TAG_RECURRENCE, recurrence);

						// adding HashList to ArrayList
						if ("Assignee".equals(mMyPreferences
								.getPreference("Context"))) {
							if (status.equals("Pending")
									|| status.equals("InProgress")) {
								items.add(map);
							} else {

							}
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
			notifyDataSetChanged();
		}
	}
}
