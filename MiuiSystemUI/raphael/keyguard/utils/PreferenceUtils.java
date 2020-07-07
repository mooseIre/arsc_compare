package com.android.keyguard.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.core.content.ContextCompat;

public class PreferenceUtils {
    public static void putBoolean(Context context, String str, boolean z) {
        getSharedPreferences(context).edit().putBoolean(str, z).apply();
    }

    public static void putString(Context context, String str, String str2) {
        getSharedPreferences(context).edit().putString(str, str2).apply();
    }

    public static String getString(Context context, String str, String str2) {
        return getSharedPreferences(context).getString(str, str2);
    }

    public static int getInt(Context context, String str, int i) {
        return getSharedPreferences(context).getInt(str, i);
    }

    public static void putInt(Context context, String str, int i) {
        getSharedPreferences(context).edit().putInt(str, i).apply();
    }

    public static void putLong(Context context, String str, long j) {
        getSharedPreferences(context).edit().putLong(str, j).apply();
    }

    public static long getLong(Context context, String str, long j) {
        return getSharedPreferences(context).getLong(str, j);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return getContext(context).getSharedPreferences("keyguard_sharedpreference", 0);
    }

    private static Context getContext(Context context) {
        Context createDeviceProtectedStorageContext = ContextCompat.createDeviceProtectedStorageContext(context);
        return createDeviceProtectedStorageContext != null ? createDeviceProtectedStorageContext : context;
    }

    public static void removeKey(Context context, String str) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.contains(str)) {
            sharedPreferences.edit().remove(str).apply();
        }
    }
}
