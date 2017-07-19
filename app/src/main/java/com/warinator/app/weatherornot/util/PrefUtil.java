package com.warinator.app.weatherornot.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Warinator on 16.07.2017.
 */

public class PrefUtil {
    private static final String APP_PREFERENCES = "weatherornot_preferences";
    private static final String APP_PREFERENCES_CITY_ID = "city_id";
    public static final int DEFAULT_CITY_ID = 1496747; //Новосибирск

    private SharedPreferences mPreferences;

    public PrefUtil(Context context){
        mPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void setCityID(int cityID){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(APP_PREFERENCES_CITY_ID, cityID);
        editor.apply();
    }

    public int getCityID(){
        if (mPreferences.contains(APP_PREFERENCES_CITY_ID)){
            return mPreferences.getInt(APP_PREFERENCES_CITY_ID, DEFAULT_CITY_ID);
        }
        else {
            return DEFAULT_CITY_ID;
        }
    }
}
