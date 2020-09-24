package com.ifyezedev.notanotebook;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesSettings {
    private static final String PREF_FILE = "com.example.notanotebook.SETTINGS_PREF";
    private static final String PIN_KEY = "com.example.notanotebook.PIN_KEY";

    static void savePinToPref(Context context, String str) {
        final SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PIN_KEY, str);
        editor.apply();
    }

    static String getPin(Context context) {
        final SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        final String defaultValue = "";
        return sharedPref.getString(PIN_KEY, defaultValue);
    }
}
