package com.android.systemui;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;
import java.util.Set;

public final class Prefs {
    public static boolean getBoolean(Context context, String str, boolean z) {
        return get(context).getBoolean(str, z);
    }

    public static void putBoolean(Context context, String str, boolean z) {
        get(context).edit().putBoolean(str, z).apply();
    }

    public static int getInt(Context context, String str, int i) {
        return get(context).getInt(str, i);
    }

    public static void putInt(Context context, String str, int i) {
        get(context).edit().putInt(str, i).apply();
    }

    public static void putStringSet(Context context, String str, Set<String> set) {
        get(context).edit().putStringSet(str, set).apply();
    }

    public static Set<String> getStringSet(Context context, String str, Set<String> set) {
        return get(context).getStringSet(str, set);
    }

    public static Map<String, ?> getAll(Context context) {
        return get(context).getAll();
    }

    public static void remove(Context context, String str) {
        get(context).edit().remove(str).apply();
    }

    public static void registerListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        get(context).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public static void unregisterListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        get(context).unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences(context.getPackageName(), 0);
    }

    public static SharedPreferences getNotif(Context context) {
        return context.getSharedPreferences("app_notification", 4);
    }
}
