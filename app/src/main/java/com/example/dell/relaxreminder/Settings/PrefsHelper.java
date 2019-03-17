package com.example.dell.relaxreminder.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by DELL on 3/17/2019.
 */

public class PrefsHelper {
    public static int getRelaxNeedPrefs(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PreferenceKey.PREF_RELAX_NEED,5);
    }

    public static boolean getNotificationsPrefs(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return  prefs.getBoolean(PreferenceKey.PREF_IS_ENABLED,true);

    }


    public static boolean getSoundsPrefs(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PreferenceKey.PREF_SOUND,false);
    }

    public static boolean getFirstTimeRunPrefs(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean("first_time_run", true) ;
    }

    public static void setFirstTimeRunPrefs(Context context, boolean b) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("first_time_run", b);
        editor.commit();
    }
}
