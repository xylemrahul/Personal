package com.techila.assign_management;

import static com.techila.assign_management.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.techila.assign_management.CommonUtilities.EXTRA_MESSAGE;
import static com.techila.assign_management.CommonUtilities.SENDER_ID;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.techila.assign_management.R;
import com.techila.assign_management.util.PrefSingleton;
import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	AlertDialog.Builder alertdialog;
	TextView forgot_password;
	EditText username, password, send_mail;
	String uname, upass, mail;
	static String contactID;
	static String android_id;
	Button login;
	protected Handler mHandler, mHandler1;
	private static Boolean flag = false;
	private PrefSingleton mMyPreferences;

	// Asyntask
	AsyncTask<Void, Void, Void> mRegisterTask;
	// Alert dialog manager
	AlertDialogManager alert = new AlertDialogManager();
	// Connection detector
	ConnectionDetector cd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		username = (EditText) findViewById(R.id.uname);
		password = (EditText) findViewById(R.id.upass);
		login = (Button) findViewById(R.id.login);
		forgot_password = (TextView) findViewById(R.id.FgtPass);

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());

		android_id = Secure.getString(LoginActivity.this.getContentResolver(),
				Secure.ANDROID_ID);

		cd = new ConnectionDetector(getApplicationContext());

		//Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			AlertDialogManager alert = new AlertDialogManager();
			alert.showAlertDialog(LoginActivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}

		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest was properly set
		GCMRegistrar.checkManifest(this);

		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				DISPLAY_MESSAGE_ACTION));

		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				LoginUser();
			}
		});

		forgot_password.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				retrievePassword();
			}
		});
	}

	protected void LoginUser() {
		// TODO Auto-generated method stub
		try {
			alertdialog = new AlertDialog.Builder(LoginActivity.this);
			alertdialog.setCancelable(false);
			uname = username.getText().toString().trim();
			upass = password.getText().toString().trim();

			if (uname.length() == 0 || upass.length() == 0) {

				alertdialog = new AlertDialog.Builder(LoginActivity.this);
				alertdialog.setTitle("Alert! ");

				if (uname.length() == 0) {

					alertdialog.setMessage("Please enter the username");
					alertdialog.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
				} else if (upass.length() == 0) {

					alertdialog.setMessage("Please enter the password");
					alertdialog.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
				}
				alertdialog.show();
			} else {

				alertdialog.setMessage("Are you sure you want to continue?");
				alertdialog.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {

							ProgressDialog prg = null;
							Handler mHandler;

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								prg = new ProgressDialog(LoginActivity.this);
								prg.setIndeterminate(true);
								prg.setMessage("Logging In...");
								prg.setCanceledOnTouchOutside(false);
								prg.show();

								try {
									new Thread(new Runnable() {

										@Override
										public void run() {
											try {

												String str = login();
												Message msg = new Message();
												msg.obj = str.toString();
												mHandler.sendMessage(msg);
											} catch (Exception e) {
												LoginActivity.this
														.runOnUiThread(new Runnable() {
															public void run() {
																Toast.makeText(
																		getApplicationContext(),
																		"Cannot connect to a working internet connection",
																		Toast.LENGTH_SHORT)
																		.show();
																prg.dismiss();
															}
														});
												e.printStackTrace();
											}
										}
									}).start();

									mHandler = new Handler() {

										public void handleMessage(Message msg) {
											String response = (String) msg.obj;
											if (response != null) {
												try {
													prg.dismiss();

													JSONObject jsonObj = new JSONObject(
															response);
													final JSONObject jsonObj1 = jsonObj
															.getJSONObject("data");
													final String error_code = jsonObj1
															.getString("Error_Code");
													if ("1".equals(error_code)) {

														JSONArray resultArray = jsonObj1
																.getJSONArray("result");
														JSONObject jsonObj2 = resultArray
																.getJSONObject(0);
														contactID = jsonObj2
																.getString("MemberID");
														mMyPreferences
																.setPreference(
																		"Mem_Id",
																		contactID);
														String type = jsonObj2
																.getString("Type");
														registerOnServer();
														if (type.equals("Admin")) {
															mMyPreferences
																	.setPreference(
																			"Context",
																			"Admin");
															Intent i = new Intent(
																	getApplicationContext(),
																	SelectionActivity.class);
															startActivity(i);
															finish();
														} else if (type
																.equals("Assignee")) {

															mMyPreferences
																	.setPreference(
																			"Context",
																			"Assignee");
															Intent i = new Intent(
																	getApplicationContext(),
																	Assign_Details.class);
															startActivity(i);
															finish();
														}
													} else if ("3"
															.equals(error_code)) {
														flag = true;
														alertdialog
																.setMessage("Are you sure you want to login with this device");
														alertdialog.setCancelable(false);														alertdialog
																.setPositiveButton(
																		"Ok",
																		new DialogInterface.OnClickListener() {

																			@Override
																			public void onClick(
																					DialogInterface dialog,
																					int which) {
																				prg.show();
																				new Thread(
																						new Runnable() {

																							@Override
																							public void run() {
																								try {
																									String result = login();
																									Message msg = new Message();
																									msg.obj = result
																											.toString();
																									mHandler.sendMessage(msg);

																								} catch (Exception e) {
																									e.printStackTrace();
																								}
																							}
																						})
																						.start();
																			}
																		});

														alertdialog
																.setNegativeButton(
																		"Cancel",
																		new DialogInterface.OnClickListener() {

																			@Override
																			public void onClick(
																					DialogInterface dialog,
																					int which) {

																				dialog.dismiss();
																			}
																		});
														alertdialog.show();
													} else if ("2"
															.equals(error_code)) {
														Toast.makeText(
																getApplicationContext(),
																"Please enter valid credentials",
																Toast.LENGTH_LONG)
																.show();
													}
												} catch (JSONException e) {

												}
											}
										}
									};
									dialog.dismiss();
								} catch (Exception e) {

								}
							}
						});
				alertdialog.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
			}
			alertdialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
				Toast.makeText(getApplicationContext(),
						"Already registered with GCM", Toast.LENGTH_LONG)
						.show();
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
						ServerUtilities.register(context, contactID, regId);
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

	protected void retrievePassword() {

		alertdialog = new AlertDialog.Builder(LoginActivity.this);
		alertdialog.setTitle("Enter registered Email Id");

		LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final View view = layoutInflater.inflate(
				R.layout.dialog_forgot_password, null);

		alertdialog.setPositiveButton("Send",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						send_mail = (EditText) view
								.findViewById(R.id.sendEmail);
						mail = send_mail.getText().toString();

						new Thread(new Runnable() {

							@Override
							public void run() {

								try {
									String result = sendEmail();
									Message msg = new Message();
									msg.obj = result.toString();
									mHandler.sendMessage(msg);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}).start();

						mHandler = new Handler() {

							@Override
							public void handleMessage(Message msg) {
								// TODO Auto-generated method stub
								super.handleMessage(msg);

								String response = (String) msg.obj;
								if (response != null) {

									JSONObject jsonObj;
									try {
										jsonObj = new JSONObject(response);
										JSONObject jsonObj1 = jsonObj
												.getJSONObject("data");
										String error_code = jsonObj1
												.getString("Error_Code");
										if ("1".equals(error_code)) {

											Toast.makeText(
													getApplicationContext(),
													"Email sent successfully",
													Toast.LENGTH_LONG).show();
										} else {

											Toast.makeText(
													getApplicationContext(),
													"Please enter valid email address",
													Toast.LENGTH_LONG).show();
										}

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						};
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
	}

	private String login() {

		ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("UserName", uname));
		nvp.add(new BasicNameValuePair("Password", upass));
		nvp.add(new BasicNameValuePair("DeviceId", android_id));
		nvp.add(new BasicNameValuePair("Flag", "" + flag));

		String jsonResponse = null;
		String url = "http://phbjharkhand.in/AssignmentApplication/User_Login.php";

		HttpParams httpParams = new BasicHttpParams();
		int timeout = (int) (30 * DateUtils.SECOND_IN_MILLIS);
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);

		try {
			if (url != null) {

				HttpPost httppost = new HttpPost(url);
				httppost.setEntity(new UrlEncodedFormEntity(nvp));

				HttpClient httpclient = new DefaultHttpClient();
				HttpResponse response = httpclient.execute(httppost);
				StatusLine status = response.getStatusLine();

				if (status.getStatusCode() == HttpStatus.SC_OK) {

					jsonResponse = EntityUtils.toString(response.getEntity());
					Log.e("_________feedback response_json_________",
							jsonResponse.toString());
				} else {

					jsonResponse = null;
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		} catch (SQLException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonResponse;
	}

	private String sendEmail() {

		ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("emailID", mail));

		String jsonResponse = null;
		String url = "http://phbjharkhand.in/AssignmentApplication/Forgot_Password_Details.php";

		HttpParams httpParams = new BasicHttpParams();
		int timeout = (int) (30 * DateUtils.SECOND_IN_MILLIS);
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);

		try {
			if (url != null) {

				HttpPost httppost = new HttpPost(url);
				httppost.setEntity(new UrlEncodedFormEntity(nvp));

				HttpClient httpclient = new DefaultHttpClient();
				HttpResponse response = httpclient.execute(httppost);
				StatusLine status = response.getStatusLine();

				if (status.getStatusCode() == HttpStatus.SC_OK) {

					jsonResponse = EntityUtils.toString(response.getEntity());
					Log.e("_________feedback response_json_________",
							jsonResponse.toString());
				} else {

					jsonResponse = null;
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		} catch (SQLException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonResponse;
	}

	/**
	 * Receiving push messages
	 * */
	final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			// Waking up mobile if it is sleeping
			WakeLocker.acquire(getApplicationContext());

			/**
			 * Take appropriate action on this message depending upon your app
			 * requirement For now i am just displaying it on the screen
			 * */
			
			// Showing received message
			Toast.makeText(getApplicationContext(),
					"New Message: " + newMessage, Toast.LENGTH_LONG).show();

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
	};
}