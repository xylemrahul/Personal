package com.techila.assign_management;

import com.techila.assign_management.R;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class AssignTabListView extends ActivityGroup {
	// TabSpec Names
	private static final String PENDING = "Pending";
	private static final String DONE = "Done";
	private static final String DEFERRED = "Deferred";

	TabHost my_tab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assignment_tabs);

		my_tab = (TabHost) findViewById(R.id.tabhost);
		my_tab.setup(this.getLocalActivityManager());

		TabSpec pendingSpec = my_tab.newTabSpec(PENDING);
		pendingSpec.setContent(new Intent(this, PendingActivity.class));
		pendingSpec.setIndicator("",
				getResources().getDrawable(R.drawable.icon_pending));
		my_tab.addTab(pendingSpec);

		TabSpec doneSpec = my_tab.newTabSpec(DONE);
		Intent done = new Intent().setClass(this, DoneActivity.class);
		doneSpec.setContent(done);
		doneSpec.setIndicator("",
				getResources().getDrawable(R.drawable.icon_done));
		my_tab.addTab(doneSpec);

		TabSpec deferredSpec = my_tab.newTabSpec(DEFERRED);
		Intent deferred = new Intent().setClass(this, DeferredActivity.class);
		deferredSpec.setContent(deferred);
		deferredSpec.setIndicator("",
				getResources().getDrawable(R.drawable.icon_deferred));
		my_tab.addTab(deferredSpec);
	}
}