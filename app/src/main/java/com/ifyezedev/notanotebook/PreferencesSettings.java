package com.ifyezedev.notanotebook;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesSettings {
    private static final String PREF_FILE = "com.ifyezedev.notanotebook.SETTINGS_PREF";
    private static final String PIN_KEY = "com.ifyezedev.notanotebook.PIN_KEY";
    private static final String SECURITY_QUESTION = "com.ifyezedev.notanotebook.SECURITY_QUESTION";
    private static final String SECURITY_ANSWER = "com.ifyezedev.notanotebook.SECURITY_ANSWER";

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

    static void saveQuestionToPref(Context context, String str) {
        final SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SECURITY_QUESTION, str);
        editor.apply();
    }

    static String getQuestion(Context context) {
        final SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        final String defaultValue = "";
        return sharedPref.getString(SECURITY_QUESTION, defaultValue);
    }

    static void saveAnswerToPref(Context context, String str) {
        final SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SECURITY_ANSWER, str);
        editor.apply();
    }

    static String getAnswer(Context context) {
        final SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        final String defaultValue = "";
        return sharedPref.getString(SECURITY_ANSWER, defaultValue);
    }
}
