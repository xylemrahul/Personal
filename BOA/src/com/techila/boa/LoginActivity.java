package com.techila.boa;

import static com.techila.boa.CommonUtilities.DISPLAY_MESSAGE_ACTION;
//import static com.techila.boa.CommonUtilities.EXTRA_MESSAGE;
import static com.techila.boa.CommonUtilities.SENDER_ID;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.google.android.gcm.GCMRegistrar;
import com.techila.boa.R;
import com.techila.boa.config.Appconstant;
import com.techila.boa.util.JSONParser;
import com.techila.boa.util.PrefSingleton;

public class LoginActivity extends Activity implements OnClickListener {

	AlertDialogManager alertdialog;
	EditText username, password;
	Button login;
	TextView forgotPassword;
	ProgressDialog prg;
	String uname, upass, mail;
	static String android_id;
	CheckBox remember;
	private PrefSingleton mMyPreferences;
	JSONParser jsonParser = new JSONParser();
	public static String contactID;
	private static boolean check_flag;
	Properties prop;

	// Asyntask
	AsyncTask<Void, Void, Void> mRegisterTask;
	static ConnectionDetector cd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		initViews();
		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());

		if (!mMyPreferences.getPreference("Session").equals("false")) {
			if (mMyPreferences.getPreference("user_Type").equals("Admin")) {

				Intent intent = new Intent(getApplicationContext(),
						Company_manage.class);
				startActivity(intent);
				LoginActivity.this.finish();
			} else if (mMyPreferences.getPreference("user_Type").equals("User")) {
				Intent intent = new Intent(getApplicationContext(),
						User_selection.class);
				startActivity(intent);
				LoginActivity.this.finish();
			}
		}

		alertdialog = new AlertDialogManager();
		android_id = Secure.getString(LoginActivity.this.getContentResolver(),
				Secure.ANDROID_ID);

		if (check_flag) {
			remember.setChecked(true);
			username.setText(mMyPreferences.getPreference("username"));
		} else {
			remember.setChecked(false);
			username.setText("");
		}

		Resources resources = this.getResources();
		AssetManager assetManager = resources.getAssets();
		prop = new Properties();

		try {
			InputStream inputStream = assetManager.open("jsonURL.properties");
			prop.load(inputStream);
			System.out.println("The properties are now loaded");
			System.out.println("properties: " + prop);
		} catch (IOException e) {
			System.err.println("Failed to open jsonURL property file");
			e.printStackTrace();
		}

		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest was properly set
		GCMRegistrar.checkManifest(this);

		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				DISPLAY_MESSAGE_ACTION));

		login.setOnClickListener(this);
		forgotPassword.setOnClickListener(this);

		remember.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					check_flag = true;
					mMyPreferences.setPreference("username", username.getText()
							.toString());
				} else {
					check_flag = false;
				}
			}
		});
	}

	void initViews() {

		username = (EditText) findViewById(R.id.uname);
		password = (EditText) findViewById(R.id.upass);
		login = (Button) findViewById(R.id.login);
		remember = (CheckBox) findViewById(R.id.rememberMe);
		forgotPassword = (TextView) findViewById(R.id.FgtPass);

	}

	@Override
	public void onClick(View v) {
 
		switch (v.getId()) {
		
		case R.id.login:
			
			cd = new ConnectionDetector(getApplicationContext());
			uname = username.getText().toString().trim();
			upass = password.getText().toString().trim();

			if (uname.length() == 0 || upass.length() == 0) {

				if (uname.length() == 0) {
					alertdialog.showAlertDialog(LoginActivity.this, "Alert!",
							"Please enter username", false);

				} else if (upass.length() == 0) {

					alertdialog.showAlertDialog(LoginActivity.this, "Alert!",
							"Please enter password", false);

				}
			} else {
				// Check if Internet present
				if (!cd.isConnectingToInternet()) {
					// Internet Connection is not present
					AlertDialogManager alert = new AlertDialogManager();
					alert.showAlertDialog(LoginActivity.this,
							"Internet Connection Error",
							"Please connect to working Internet connection", false);
					// stop executing code by return
					return;
				} else {
					new LoginUser().execute();
				}
			}
			break;

		case R.id.FgtPass:
			
			AlertDialog.Builder alertdialog = new AlertDialog.Builder(
					LoginActivity.this);
			alertdialog.setTitle("Enter registered Email Id");

			LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			final View view = layoutInflater.inflate(
					R.layout.dialog_forgot_password, null);
			final EditText send_mail = (EditText) view.findViewById(R.id.sendEmail);
			alertdialog.setPositiveButton("Send",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							mail = send_mail.getText().toString();
							if (mail.equals("")) {
								Toast.makeText(getApplicationContext(),
										"Please enter valid id", Toast.LENGTH_SHORT)
										.show();
							} else {
								new SendEmail().execute();
							}

							dialog.cancel();
						}
					});

			alertdialog.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							dialog.cancel();
						}
					});

			alertdialog.setView(view);
			alertdialog.show();
		
			break;
			
		default:
			break;
		}
		
	};

	protected void registerOnServer() {

		// Get GCM registration id
		final String regId = GCMRegistrar.getRegistrationId(this);

		// Check if regid already presents
		if (regId.equals("")) {
			// Registration is not present, register now with GCM
			GCMRegistrar.register(this, SENDER_ID);
		} else {
			// Device is already registered on GCM
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.

			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				final Context context = this;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						// Register on our server
						// On server creates a new user
						ServerUtilities.register(context, contactID, regId,
								android_id);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}
				};
				mRegisterTask.execute(null, null, null);
			}
		}
	}

	class LoginUser extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(LoginActivity.this);
			prg.setMessage("Logging...");
			prg.setIndeterminate(false);
			prg.setCancelable(false);
			prg.show();
		}

		@Override
		protected Integer doInBackground(Void... voids) {

			List<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("userName", uname));
			nvp.add(new BasicNameValuePair("passWord", upass));
			nvp.add(new BasicNameValuePair("DeviceId", android_id));

			String error_code = null;
			Log.e("URL ", "is" + Appconstant.LOGIN_URL);
			try {

				// getting JSON string from URL
				JSONObject json = jsonParser.makeHttpRequest(
						Appconstant.LOGIN_URL, "POST", nvp);

				// Check your log cat for JSON response
				Log.d("Inbox JSON: ", json.toString());

				JSONObject jsonObject = json.getJSONObject("data");
				error_code = jsonObject.getString("Error_Code");

				if ("1".equals(error_code) || "3".equals(error_code)) {
					JSONArray jsonArray = null;
					jsonArray = jsonObject.getJSONArray("result");
					for (int i = 0; i < jsonArray.length(); i++) {

						JSONObject c = jsonArray.getJSONObject(i);
						contactID = c.getString(Appconstant.TAG_LOGIN_ID);
						String type = c.getString("type");
						String com_id = c.getString(Appconstant.TAG_COMPANY_ID);

						mMyPreferences.setPreference("C_ID", com_id);
						mMyPreferences.setPreference("LoginId", contactID);
						mMyPreferences.setPreference("user_Type", type);

						if (type.equals("Admin")) {
							error_code = "3";

						} else if (type.equals("User")) {

							error_code = "4";
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

			if (result == 3) {

				registerOnServer();

				Toast.makeText(getApplicationContext(), "Login Successfull",
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(getApplicationContext(),
						Company_manage.class);
				startActivity(intent);
				finish();

			} else if (result == 4) {

				registerOnServer();

				Toast.makeText(getApplicationContext(), "Login Successful",
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(getApplicationContext(),
						User_selection.class);
				startActivity(intent);
				finish();

			} else if (result == 2) {

				Toast.makeText(getApplicationContext(),
						"Invalid username/password", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private class SendEmail extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			prg = new ProgressDialog(LoginActivity.this);
			prg.setMessage("Sending Mail ...");
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
			params1.add(new BasicNameValuePair("emailId", mail));

			Log.e("URL ", "is" + Appconstant.FORGOT_PASSWORD_URL);
			json = jsonParser.makeHttpRequest(Appconstant.FORGOT_PASSWORD_URL,
					"POST", params1);
			// Check your log cat for JSON response
			Log.d("Inbox JSON: ", json.toString());

			try {
				JSONObject jsonObj = json.getJSONObject("data");
				error_code = jsonObj.getString("Error_Code");

				if ("1".equals(error_code)) {
					
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
			if (result == 1) {
				Toast.makeText(getApplicationContext(),
						"E-Mail successfully sent", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"Please enter valid Email Address", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	/**
	 * Receiving push messages
	 * */
	final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			// Waking up mobile if it is sleeping
			WakeLocker.acquire(getApplicationContext());

			/**
			 * Take appropriate action on this message depending upon your app
			 * requirement For now i am just displaying it on the screen
			 * */

			// Showing received message
			/*
			 * Toast.makeText(getApplicationContext(), "New Message: " +
			 * newMessage, Toast.LENGTH_LONG).show();
			 */

			// Releasing wake lock
			WakeLocker.release();
		}
	};

	@Override
	protected void onDestroy() {

		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		try {
			unregisterReceiver(mHandleMessageReceiver);
			GCMRegistrar.onDestroy(this);
		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}
}