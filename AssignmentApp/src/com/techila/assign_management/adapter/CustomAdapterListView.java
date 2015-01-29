package com.techila.assign_management.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.techila.assign_management.Assign_Details;
import com.techila.assign_management.Grp_Manage;
import com.techila.assign_management.Mem_Manage;
import com.techila.assign_management.config.Appconstant;
import com.techila.assign_management.util.JSONParser;
import com.techila.assign_management.util.PrefSingleton;
import com.techila.assign_management.R;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	private class ViewHolder {
		TextView tvName, tvDetails;
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
			vh.tvDetails = (TextView) row.findViewById(R.id.viewDetails);

			row.setTag(vh);

		} else {
			vh = (ViewHolder) row.getTag();
		}

		if (act.equals("Mem")) {
			getMember(position, vh);

		} else if (act.equals("Grp")) {
			getGroup(position, vh);

		} else if (act.equals("Ass")) {
			getAssignment(position, vh);
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
					String age = element.get(Appconstant.TAG_AGE);
					String sex = element.get(Appconstant.TAG_SEX);
					String mail_address = element.get(Appconstant.TAG_EMAIL);
					String postal_address = element
							.get(Appconstant.TAG_ADDRESS);
					String pass = element.get(Appconstant.TAG_PASS);
					String uname = element.get(Appconstant.TAG_UNAME);
					String type = element.get(Appconstant.TAG_TYPE);
					showDialogMember(CreatedDate, contact, MemberName, age,
							sex, mail_address, postal_address, pass, uname,
							type);

					Log.e("", "Member");
				} else if (act.equals("Ass")) {

					HashMap<String, String> element = items.get(position);
					String CreatedDate = element.get(Appconstant.TAG_DATE);
					String Job_Date = element.get(Appconstant.TAG_JOB_DATE);
					String description = element.get(Appconstant.TAG_DESC);
					String AssignmentName = element
							.get(Appconstant.TAG_ASS_NAME);
					String Status = element.get(Appconstant.TAG_STATUS);
					String priority = element.get(Appconstant.TAG_PRIORITY);
					String type = element.get(Appconstant.TAG_TYPE);
					String recurrence = element.get(Appconstant.TAG_RECURRENCE);
					showDialogAssignment(CreatedDate, Job_Date, description,
							AssignmentName, Status, priority, type, recurrence);

					Log.e("", "Assignment");
				}
			}
		});

		row.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (act.equals("Mem")) {

					HashMap<String, String> element = items.get(position);
					String MemberId = element.get(Appconstant.TAG_MEM_ID);
					mMyPreferences.setPreference("Mem_Id", MemberId);
					Intent i = new Intent(context, Assign_Details.class);
					context.startActivity(i);

				} else if (act.equals("Grp")) {

					HashMap<String, String> element = items.get(position);
					String GroupId = element.get(Appconstant.TAG__GRP_ID);
					String abbreviation = element.get(Appconstant.TAG_ABBR);
					mMyPreferences.setPreference("Abb", abbreviation);
					mMyPreferences.setPreference("Id", GroupId);
					Intent i = new Intent(context, Mem_Manage.class);
					context.startActivity(i);

				} else if (act.equals("Ass")) {
					if ("Assignee".equals(mMyPreferences
							.getPreference("Context"))) {

						HashMap<String, String> element = items.get(position);
						String Status = element.get(Appconstant.TAG_STATUS);
						assignment_id = element.get(Appconstant.TAG_ASS_ID);
						alertdialog = new AlertDialog.Builder(context);
						alertdialog.setTitle("The Current status is " + Status);

						LayoutInflater layoutInflater = (LayoutInflater) context
								.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

						view = layoutInflater.inflate(
								R.layout.dialog_set_status, null);

						alertdialog.setMessage(" Change the current status to");

						Grpstatus = (RadioGroup) view.findViewById(R.id.status);
						rd_prg = (RadioButton) Grpstatus
								.findViewById(R.id.inProgress);
						rd_done = (RadioButton) Grpstatus
								.findViewById(R.id.done);
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
					} else {

					}
				}
			}
		});

		return row;
	}

	// Calling methods definitions
	protected void showDialog() {
		// TODO Auto-generated method stub
		alertdialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

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
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});

		alertdialog.setView(view);
		alertdialog.show();
	}

	class changeStatus extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
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
					.toString()));
			list.add(new BasicNameValuePair("Description", description));
			String error_code = null;
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
			} else if (error_num == 2) {
				Toast.makeText(context, "Status update failed",
						Toast.LENGTH_SHORT).show();
			}

		}

	}

	protected void showDialogGroup(String createdDate, String abbreviation,
			String groupName, String description) {

		final AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle(" " + "Details of " + " " + groupName);
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = layoutInflater.inflate(R.layout.dialog_grp_details,
				null);

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
			String memberName, String age, String sex, String mail_address,
			String postal_address, String pass, String uname, String type) {

		final AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle(" " + " Details of " + " " + memberName);
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = layoutInflater.inflate(R.layout.dialog_mem_details,
				null);
		TextView name = (TextView) view.findViewById(R.id.tv_memname);
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
			String description, String assignmentName, String status,
			String priority, String type, String recurrence) {

		final AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle(" " + "Details of " + " " + assignmentName);
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = layoutInflater.inflate(
				R.layout.dialog_assignment_details, null);

		TextView name = (TextView) view.findViewById(R.id.tv_assname);
		TextView created_date = (TextView) view.findViewById(R.id.tv_date);
		TextView job_date = (TextView) view.findViewById(R.id.tv_job);
		TextView Status = (TextView) view.findViewById(R.id.tv_status);
		TextView Priority = (TextView) view.findViewById(R.id.tv_priority);
		TextView Type = (TextView) view.findViewById(R.id.tv_type);
		TextView Recurrence = (TextView) view.findViewById(R.id.tv_recurrence);
		TextView desc = (TextView) view.findViewById(R.id.tv_desc);

		name.setText(assignmentName);
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
}
