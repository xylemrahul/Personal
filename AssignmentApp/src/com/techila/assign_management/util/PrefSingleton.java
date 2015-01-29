package com.techila.assign_management.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PrefSingleton{
	
	   private static PrefSingleton mInstance;
	   private Context mContext;
	   private SharedPreferences mMyPreferences;

	   private PrefSingleton(){
		   
	   }

	   public static PrefSingleton getInstance(){
	       if (mInstance == null)
	    	   mInstance = new PrefSingleton();
	       return mInstance;
	   }

	   public void Initialize(Context ctxt){
	       mContext = ctxt;
	       mMyPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
	   }
	   
	   public void setPreference(String key,String value){
		   
		   Editor e = mMyPreferences.edit();
		   e.putString(key,value);
		   e.commit();
	   }
	   
	   public String getPreference(String key){
		    
		  String val = "";
		  val = mMyPreferences.getString(key, ""+0);
		  return val;
	   }
	}