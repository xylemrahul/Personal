package com.techila.boa;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.widget.ImageView;

import com.techila.boa.R;

public class SelectionActivity extends Activity implements OnClickListener {

	ImageView act, user, payment, withdrawal, requests, reports;

	// public static boolean flag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_selection);

		initViews();

		act.setOnClickListener(this);
		user.setOnClickListener(this);
		payment.setOnClickListener(this);
		requests.setOnClickListener(this);
		reports.setOnClickListener(this);
	}

	private void initViews() {

		act = (ImageView) findViewById(R.id.acct);
		user = (ImageView) findViewById(R.id.users);
		payment = (ImageView) findViewById(R.id.payment);
		requests = (ImageView) findViewById(R.id.requests);
		reports = (ImageView) findViewById(R.id.reports);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case (R.id.acct):

			startActivity(new Intent(getApplicationContext(),
					Bank_Details.class));
			break;

		case (R.id.users):

			startActivity(new Intent(getApplicationContext(),
					User_Details.class));
			break;

		case (R.id.payment):
			Intent i = new Intent(getApplicationContext(),
					TransactionFragment.class);
			i.putExtra("Req", "sdh");
			startActivity(i);
			break;

		case (R.id.requests):

			Intent intent = new Intent(getApplicationContext(),
					TransactionFragment.class);
			intent.putExtra("Req", "request");
			startActivity(intent);
			break;

		case (R.id.reports):

			startActivity(new Intent(getApplicationContext(),
					ReportsActivity.class));
			break;
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Company_manage.flag = false;
	}
}
