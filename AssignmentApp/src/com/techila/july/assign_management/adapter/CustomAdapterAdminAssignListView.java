package com.techila.july.assign_management.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.techila.july.assign_management.R;
import com.techila.july.assign_management.config.Appconstant;
import com.techila.july.assign_management.util.PrefSingleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class CustomAdapterAdminAssignListView extends BaseAdapter {

	ArrayList<HashMap<String, String>> items = null;
	HashMap<String, String> map = null;
	Activity context;
	int k = 0;
	PrefSingleton mMyPreferences = null;

	private static LayoutInflater inflater;

	public CustomAdapterAdminAssignListView(Activity context,
			ArrayList<HashMap<String, String>> items) {

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.items = items;
		this.context = context;
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
		TextView tvName, tvStatus;
	}

	public void sortByDate() {
		ArrayList<HashMap<String, String>> date = items;
		Collections.sort(date , new Comparator<HashMap<String, String>>() {

			@Override
			public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
				
				return lhs.get(Appconstant.TAG_DATE).compareTo(rhs.get(Appconstant.TAG_DATE));
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
		ViewHolder vh;
		if (row == null) {
			row = inflater.inflate(R.layout.activity_custom_admin_assign, null);
			vh = new ViewHolder();
			vh.tvName = (TextView) row.findViewById(R.id.name);
			vh.tvStatus = (TextView) row.findViewById(R.id.status_display);

			row.setTag(vh);

		} else {
			vh = (ViewHolder) row.getTag();
		}

		getAssignment(position, vh);

		row.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				HashMap<String, String> element = items.get(position);
				String CreatedDate = element.get(Appconstant.TAG_DATE);
				String Job_Date = element.get(Appconstant.TAG_JOB_DATE);
				String description = element.get(Appconstant.TAG_DESC);
				String AssignmentName = element.get(Appconstant.TAG_ASS_NAME);
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
		});

		return row;
	}

	protected void showDialogAssignment(String createdDate, String job_Date,
			String description, String assignmentName, String memberName,
			String groupName, String status, String priority, String type,
			String recurrence) {

		final AlertDialog.Builder alert = new AlertDialog.Builder(context);

//		alert.setTitle(" " + "Details of " + " " + assignmentName);
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = layoutInflater.inflate(
				R.layout.dialog_assignment_details, null);

		TextView title = (TextView)view.findViewById(R.id.title_text);
		title.setText(" "+ assignmentName.toUpperCase());
		
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

	protected void getAssignment(int position, ViewHolder vh) {

		HashMap<String, String> element = items.get(position);
		String name = element.get(Appconstant.TAG_ASS_NAME);
		String status = element.get(Appconstant.TAG_STATUS);
		vh.tvName.setText(name);
		vh.tvStatus.setText(status);
	}
}
