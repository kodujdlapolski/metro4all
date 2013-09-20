/******************************************************************************
 * Project:  Metro Access
 * Purpose:  Routing in subway for disabled.
 * Author:   Baryshnikov Dmitriy (aka Bishop), polimax@mail.ru
 ******************************************************************************
*   Copyright (C) 2013 NextGIS
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ****************************************************************************/
package com.nextgis.metroaccess;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class PreferencesActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
	
	public static final String KEY_PREF_USER_TYPE = "user_type";
	public static final String KEY_PREF_MAX_WIDTH = "max_width";
	public static final String KEY_PREF_WHEEL_WIDTH = "wheel_width";
	public static final String KEY_PREF_DOWNLOAD_PATH = "download_path";
	
	//ListPreference mlsNaviType;
	protected EditTextPreference metWheelWidth;
	protected EditTextPreference metMaxWidth;
	protected EditTextPreference metDownloadPath;
	
	protected Map<String, JSONObject> moRemoteData;  
	protected Map<String, CheckBoxPreference> mDBs;
	
	protected String msUrl;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        
        addPreferencesFromResource(R.xml.preferences);
        
    	List<String> aoRouteMetadata = null;

	    Bundle extras = getIntent().getExtras(); 
	    if(extras != null) {
	    	aoRouteMetadata = (List<String>) extras.getSerializable(MainActivity.BUNDLE_METAMAP_KEY);
	    }        
        
        /*mlsNaviType = (ListPreference) findPreference(KEY_PREF_USER_TYPE);
        
        int index = Integer.parseInt((String) mlsNaviType.getValue()) - 1;           
        if(index >= 0){
        	mlsNaviType.setSummary((String) mlsNaviType.getEntries()[index]);
        }
        */
        metMaxWidth = (EditTextPreference) findPreference(KEY_PREF_MAX_WIDTH);
        metMaxWidth.setSummary((String) metMaxWidth.getText() + " " + getString(R.string.sCM));
	    
	    metWheelWidth = (EditTextPreference) findPreference(KEY_PREF_WHEEL_WIDTH);
	    metWheelWidth.setSummary((String) metWheelWidth.getText() + " " + getString(R.string.sCM));
	    
	    //TODO: add data packages list and button update data
	    metDownloadPath = (EditTextPreference) findPreference(KEY_PREF_DOWNLOAD_PATH);
	    msUrl = (String) metDownloadPath.getText();
	    metDownloadPath.setSummary(msUrl);
	    
	    PreferenceCategory targetCategory = (PreferenceCategory)findPreference("data_cat");
	    
		File file = new File(getExternalFilesDir(null), MainActivity.REMOTE_METAFILE);
		String sPayload = MainActivity.readFromFile(file, this);
		moRemoteData = new HashMap<String, JSONObject>();
		mDBs = new HashMap<String, CheckBoxPreference>();
		try{
		    	JSONObject oJSONMetaRemote = new JSONObject(sPayload);
				
			    final JSONArray jsonArray = oJSONMetaRemote.getJSONArray("packages");
			    
			    for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String sLocaleKeyName = "name_" + Locale.getDefault().getLanguage();
					String sLocName = jsonObject.getString(sLocaleKeyName);	
					String sName = jsonObject.getString("name");
					if(sLocName.length() == 0)
						sLocName = sName;
					int nVer = jsonObject.getInt("ver");
					
					final String sKey = "db_" + i;
					moRemoteData.put(sKey, jsonObject);
					  
					//check is exist
					CheckBoxPreference db = new CheckBoxPreference(this);
					db.setKey(sKey); //Refer to get the pref value
					db.setTitle(sLocName);
					db.setSummary("ver." + nVer);
					//
					boolean bChecked = false;
					if(aoRouteMetadata != null){
						for(String sExistName : aoRouteMetadata){
							if(sExistName.equals(sName)){
								bChecked = true;
								break;
							}
						}
					}
					db.setChecked(bChecked);			
					db.setOnPreferenceChangeListener(new MyOnPreferenceChangeListener(sKey));

					targetCategory.addPreference(db);
					
					mDBs.put(sKey,  db);

			    }
		 } 
		 catch (Exception e) {
			 Toast.makeText(this, R.string.sNetworkInvalidData, Toast.LENGTH_LONG).show();
		}		 
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    } 
    
	@Override
	protected void onPause() {	
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);		
		sharedPref.unregisterOnSharedPreferenceChangeListener(this);
		
		super.onPause();
	}
	
    @Override
	public void onResume() {
        super.onResume();
        
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		sharedPref.registerOnSharedPreferenceChangeListener(this);
    }	
    
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		CharSequence newVal = "";
		Preference Pref = findPreference(key);
		if(key.equals(KEY_PREF_WHEEL_WIDTH) || key.equals(KEY_PREF_MAX_WIDTH))
		{
			newVal = sharedPreferences.getString(key, "40");
        	String toIntStr = (String) newVal;
    		Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
    		editor.putInt(key + "_int", Integer.parseInt(toIntStr) * 10);
    		editor.commit();
    		
    		if(newVal.length() > 0)
            	Pref.setSummary(newVal  + " " + getString(R.string.sCM));
        }
		/*else if(key.equals(KEY_PREF_USER_TYPE))
		{
			newVal = sharedPreferences.getString(key, "1");
        	String toIntStr = (String) newVal;
    		Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
    		int index = Integer.parseInt(toIntStr);
    		editor.putInt(key + "_int", index);
    		editor.commit();
    		
    		index--;           
            if(index >= 0){
            	mlsNaviType.setSummary((String) mlsNaviType.getEntries()[index]);
            }
        }	*/
		else if(key.equals(KEY_PREF_DOWNLOAD_PATH)){
			msUrl = sharedPreferences.getString(key, msUrl);			
    		if(msUrl.length() > 0)
            	Pref.setSummary(msUrl);			
		}
		else if(key.startsWith("db_")){
			//update interface
			//set or not set check
			boolean bVal = sharedPreferences.getBoolean(key, true);
			CheckBoxPreference db = mDBs.get(key);
			if(db != null){
				db.setChecked(bVal);
			}

			JSONObject jsonObject = moRemoteData.get(key);
			if(jsonObject != null){
				if(bVal){
					//download and unzip
					try {
						int nVer = jsonObject.getInt("ver");
					
						String sPath = jsonObject.getString("path");
						String sName = jsonObject.getString("name");
						String sLocName = sName;
						if(jsonObject.has("name_" + Locale.getDefault().getLanguage())){
							sLocName = jsonObject.getString("name_" + Locale.getDefault().getLanguage());
						}
						boolean bDirected = false;
						if(jsonObject.has("directed")){
							bDirected = jsonObject.getBoolean("directed");
						}
						if(sLocName.length() == 0){
							sLocName = sName;
						}
						
						DataDownloader uploader = new DataDownloader(this, sPath, sName, sLocName, nVer, bDirected, getResources().getString(R.string.sDownLoading), null);
						uploader.execute(msUrl + sPath + ".zip");
					} catch (JSONException e) {
						e.printStackTrace();
					}			
				}
				else {
					try {
						String sPath = jsonObject.getString("path");
						String sFullPath = getExternalFilesDir(MainActivity.ROUTE_DATA_DIR) + File.separator + sPath;
						DeleteRecursive(new File(sFullPath));
					}
					catch (JSONException e) {
						e.printStackTrace();
					}		
				}
    		}
		}
	}	
	
	
	private void DeleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            DeleteRecursive(child);

	    fileOrDirectory.delete();
	}

	class MyOnPreferenceChangeListener implements OnPreferenceChangeListener{
		protected String msKey;
		protected SharedPreferences mSharedPref;
		
		public MyOnPreferenceChangeListener(String sKey) {
			msKey = sKey;
			mSharedPref = PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this);
		}

		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean currentVal = mSharedPref.getBoolean(msKey, false);
			boolean newVal = (Boolean) newValue;
			if(currentVal == newVal){
				return true;
			}
			else if(newVal == true){
				AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this);
				builder.setTitle(R.string.sDownload)
				.setMessage(R.string.sDownloadData)
				.setCancelable(false)
				.setPositiveButton(R.string.sDownload, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id){
						Editor editor = MyOnPreferenceChangeListener.this.mSharedPref.edit();
						editor.putBoolean(msKey,  true);
						editor.commit();
					}								
				})
				.setNegativeButton(R.string.sCancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				builder.create();
				builder.show();
			}
			else  if(newVal == false){
				AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this);
				builder.setTitle(R.string.sDelete)
				.setMessage(R.string.sDeleteData)
				.setCancelable(false)
				.setPositiveButton(R.string.sDelete, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id){
						Editor editor = MyOnPreferenceChangeListener.this.mSharedPref.edit();
						editor.putBoolean(msKey,  false);
						editor.commit();
					}								
				})
				.setNegativeButton(R.string.sCancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				AlertDialog dlg = builder.create();
				dlg.setCancelable(false);
				dlg.setCanceledOnTouchOutside(false);
				dlg.show();
			}
			return false;
		}
	}
}