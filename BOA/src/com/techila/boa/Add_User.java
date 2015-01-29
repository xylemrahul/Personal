package com.techila.boa;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.techila.boa.R;
import com.techila.boa.config.Appconstant;
import com.techila.boa.model.UserModel;
import com.techila.boa.util.PrefSingleton;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Add_User extends Activity implements OnClickListener {

	EditText Fname, Lname, age, sex, email, p_add, user_type;
	Button submit, cancel;

	String sex_array[] = { "Male", "Female" };
	String type_array[] = { "User", "Admin" };
	private String addUserUrl;

	ArrayAdapter<String> spinner_type;
	ArrayAdapter<String> spinner_sex;

	private boolean flag;
	private boolean emailcheck = false;
	ProgressDialog prg = null;
	UserModel u_model;

	Properties prop;
	HashMap<String, String> map;

	private PrefSingleton mMyPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_user);

		initViews();

		mMyPreferences = PrefSingleton.getInstance();
		mMyPreferences.Initialize(getApplicationContext());

		Resources resources = this.getResources();
		AssetManager assetManager = resources.getAssets();
		try {
			InputStream inputStream = assetManager.open("jsonURL.properties");
			prop = new Properties();
			prop.load(inputStream);
		} catch (IOException e) {
			System.err.println("Failed to open jsonURL property file");
			e.printStackTrace();
		}

		spinner_type = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.row, type_array);

		spinner_sex = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.row, sex_array);

		user_type.setInputType(InputType.TYPE_NULL);
		sex.setInputType(InputType.TYPE_NULL);

		sex.setOnClickListener(this);
		user_type.setOnClickListener(this);
		submit.setOnClickListener(this);
		cancel.setOnClickListener(this);
	}

	private void initViews() {

		Fname = (EditText) findViewById(R.id.fname);
		Lname = (EditText) findViewById(R.id.lname);
		age = (EditText) findViewById(R.id.age);
		sex = (EditText) findViewById(R.id.sex);
		email = (EditText) findViewById(R.id.mailid);
		p_add = (EditText) findViewById(R.id.postal);
		user_type = (EditText) findViewById(R.id.type);
		submit = (Button) findViewById(R.id.save);
		cancel = (Button) findViewById(R.id.cancel);

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.type:

			new AlertDialog.Builder(Add_User.this)
					.setTitle("Select Type")
					.setAdapter(spinner_type,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									user_type.setText(type_array[which]
											.toString());
								}
							}).create().show();

			break;
		case R.id.sex:

			new AlertDialog.Builder(Add_User.this)
					.setTitle("Select Gender")
					.setAdapter(spinner_sex,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									sex.setText(sex_array[which].toString());
								}
							}).create().show();
			break;

		case R.id.save:

			boolean isValid = validate();
			if (isValid) {

				prg = new ProgressDialog(Add_User.this);
				// Showing progress dialog before making http request
				prg.setMessage("Loading...");
				prg.show();
				prg.setCanceledOnTouchOutside(false);

				addUserUrl = String
						.format(Appconstant.ADD_USER_URL
								+ "?"
								+ "adminId=%s&companyID=%s&userFname=%s&userLname=%s&age=%s&sex=%s"
								+ "&emailId=%s&address=%s&userType=%s",
								mMyPreferences.getPreference("LoginId"),
								mMyPreferences.getPreference("C_ID"),
								u_model.getFirstName(), u_model.getLastName(),
								u_model.getAge(), u_model.getSex(),
								u_model.getEmail(), u_model.getPostal(),
								u_model.getType());

				AddUser();
			} else {
				if (!flag) {
					AlertDialogManager alert = new AlertDialogManager();
					alert.showAlertDialog(Add_User.this, "Alert",
							"Please enter all the fields", false);
				}
			}

			break;

		case R.id.cancel:
			Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED, returnIntent);
			finish();

		default:
			break;
		}
	}

	protected Boolean validate() {

		String FirstName = Fname.getText().toString();
		String LastName = Lname.getText().toString();
		String Age = age.getText().toString();
		String Sex = sex.getText().toString();
		String Email = email.getText().toString();
		String Postal = p_add.getText().toString();
		String Type = user_type.getText().toString();

		if (FirstName.equals("") || LastName.equals("") || Age.equals("")
				|| Sex.equals("") || Email.equals("") || Postal.equals("")
				|| Type.equals("")) {

			return false;

		} else {

			checkemail(Email);
			if (emailcheck) {
				u_model = new UserModel(FirstName, LastName, Age, Sex, Email,
						Postal, Type);

				return true;
			} else {
				AlertDialogManager alert = new AlertDialogManager();
				alert.showAlertDialog(Add_User.this, "Alert",
						"Please enter valid email id", false);
				flag = true;
				return false;
			}
		}
	}

	private void checkemail(String email) {
		Pattern pattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
		Matcher matcher = pattern.matcher(email);
		emailcheck = matcher.matches();
	}

	private void AddUser() {

		showpDialog();

		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				addUserUrl, null, new Response.Listener<JSONObject>() {

					String error_code;

					@Override
					public void onResponse(JSONObject json) {
						try {

							JSONObject jsonObj = json.getJSONObject("data");
							error_code = jsonObj.getString("Error_Code");
							if ("1".equals(error_code)) {
								JSONArray jsonArray = null;
								jsonArray = jsonObj.getJSONArray("result");
								for (int i = 0; i < jsonArray.length(); i++) {

									map = new HashMap<String, String>();
									// Adding values to map
									map.put(Appconstant.TAG_FNAME,
											u_model.getFirstName());
									map.put(Appconstant.TAG_LNAME,
											u_model.getLastName());
									map.put(Appconstant.TAG_MAIL,
											u_model.getEmail());
									map.put(Appconstant.TAG_SEX,
											u_model.getSex());
									map.put(Appconstant.TAG_USER_ADD,
											u_model.getPostal());
									map.put(Appconstant.TAG_USER_TYPE,
											u_model.getType());
									map.put(Appconstant.TAG_AGE,
											u_model.getAge());

									User_Details.UserList.add(map);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (error_code == "2") {

							Toast.makeText(getApplicationContext(),
									"Please try again", Toast.LENGTH_SHORT)
									.show();
							Intent returnIntent = new Intent();
							setResult(RESULT_CANCELED, returnIntent);
							finish();
						}
						Intent returnIntent = new Intent();
						setResult(RESULT_OK, returnIntent);
						finish();
						hidepDialog();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						// TODO Auto-generated method stub
						hidepDialog();
					}
				});
		// Adding request to request queue
		Volley.newRequestQueue(getApplicationContext()).add(jsonObjReq);

	}

	private void showpDialog() {
		if (!prg.isShowing())
			prg.show();
		prg.setCanceledOnTouchOutside(false);
	}

	private void hidepDialog() {
		if (prg.isShowing())
			prg.dismiss();
	}
}
