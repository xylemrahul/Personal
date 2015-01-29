package com.techila.assign_management.db;

import com.techila.assign_management.config.Appconstant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

	public static final String KEY_ROWID = "_id";
	public static final String KEY_GROUP_NAME = "grp_name";
	public static final String KEY_MEMBER_NAME = "mem_name";
	public static final String KEY_USERNAME = "user_name";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_PHONE = "phone";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_AGE = "age";
	public static final String KEY_SEX = "sex";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_TYPE = "type";
	public static final String KEY_DESC = "description";
	private static final String TAG = "DBAdapter";
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;
	private final Context context;

	public static String TABLE_CREATE_NEWGROUP = "create table IF NOT EXISTS "
			+ Appconstant.TABLE_CREATE_NEWGROUP
			+ " ( "
			+ KEY_ROWID
			+ " integer primary key autoincrement, "
			+ KEY_GROUP_NAME
			+ " text not null,"
			+ KEY_DESC
			+ " text, );";
	
	public static String TABLE_CREATE_NEW_MEMBER = "create table IF NOT EXISTS "
			+ Appconstant.TABLE_CREATE_MEMBER
			+ " ( "
			+ KEY_ROWID
			+ " integer primary key autoincrement, "
			+ KEY_MEMBER_NAME
			+ " text, "
			+ KEY_AGE
			+ " text, "
			+ KEY_SEX
			+ " text, "
			+ KEY_PHONE
			+ " text, "
			+ KEY_EMAIL
			+ " text, "
			+ KEY_ADDRESS
			+ " text, "
			+ KEY_USERNAME
			+ " text, "
			+ KEY_PASSWORD
			+ " text, "
			+ KEY_TYPE
			+ " text );";
			
	
	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	/*
	 * public DBAdapter(AlarmManagerBroadcastReceiver alarm) {
	 * 
	 * Alarm = alarm; // DBHelper = new DatabaseHelper(alarm);
	 * 
	 * }
	 */

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, Appconstant.DATABASE_NAME, null,
					Appconstant.DATABASE_VERSION);
			Log.v("DatabaseCreated::", Appconstant.DATABASE_NAME);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			try {

				Log.e("In Oncreate of DBHELPER", "hello");
				db.execSQL(TABLE_CREATE_NEWGROUP);
				db.execSQL(TABLE_CREATE_NEW_MEMBER);

			} catch (SQLException e) {
				e.printStackTrace();
				Log.v("EXCEPTION_DB ::", e.toString());
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			try {
				Log.w(TAG, "upgrading database from version" + oldVersion
						+ "to" + newVersion
						+ ", which will destroy all old data");
				db.execSQL("DROP TABLE IF EXISTS "
						+ Appconstant.TABLE_CREATE_NEWGROUP);
				onCreate(db);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}// class DatabaseHelper ends here

	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		DBHelper.close();
	}

	public long insertIntoGroup(String grp_name ,String members ,String desc){
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_GROUP_NAME, grp_name);
		initialValues.put(KEY_DESC, desc);
		return db.insert(Appconstant.TABLE_CREATE_NEWGROUP, null, initialValues);
	}
	
	public long insertIntoMember(String name,String age,String sex,String phone,String email,String address,String uname,String pass,String type){
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_MEMBER_NAME, name);
		initialValues.put(KEY_AGE, age);
		initialValues.put(KEY_SEX, sex);
		initialValues.put(KEY_PHONE, phone);
		initialValues.put(KEY_EMAIL, email);
		initialValues.put(KEY_ADDRESS, address);
		initialValues.put(KEY_USERNAME, uname);
		initialValues.put(KEY_PASSWORD, pass);
		initialValues.put(KEY_TYPE, type);
		
		return db.insert(Appconstant.TABLE_CREATE_MEMBER, null, initialValues);
		
	}
	
	public boolean updateMember(long rowId,String name,String age,String sex,String phone,String email,String address,String uname,String pass,String type){
		
		ContentValues args = new ContentValues();
		args.put(KEY_MEMBER_NAME, name);
		args.put(KEY_AGE, age);
		args.put(KEY_SEX, sex);
		args.put(KEY_PHONE, phone);
		args.put(KEY_EMAIL, email);
		args.put(KEY_ADDRESS, address);
		args.put(KEY_USERNAME, uname);
		args.put(KEY_PASSWORD, pass);
		args.put(KEY_TYPE, type);
		
		return db.update(Appconstant.TABLE_CREATE_MEMBER, args, KEY_ROWID+"="+rowId, null)>0;
		
	}

	public boolean deleteContact() throws SQLException {
		return false;
	}
	
	/*public Cursor getCountGroup() throws SQLException{
		
		return db.rawQuery("SELECT COUNT(*) FROM TABLE_CREATE_NEWGROUP", null);
	}*/
	
	public Cursor getAllRecordsFromGroup() throws SQLException{
		
		return db.rawQuery("SELECT * FROM TABLE_CREATE_NEWGROUP", null);
	}
	
	/*public Cursor getCountNewMember() throws SQLException{
		
		return db.rawQuery("SELECT COUNT(*) FROM TABLE_CREATE_MEMBER",null);
	}*/
	
	public Cursor getAllRecordsFromNewMember() throws SQLException{
		
		return db.rawQuery("SELECT * FROM TABLE_CREATE_MEMBER",null);
	}
}