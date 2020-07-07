package com.android.systemui;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;

public final class Prefs {
    public static boolean getBoolean(Context context, String str, boolean z) {
        return get(context).getBoolean(str, z);
    }

    public static void putBoolean(Context context, String str, boolean z) {
        get(context).edit().putBoolean(str, z).apply();
    }

    public static Map<String, ?> getAll(Context context) {
        return get(context).getAll();
    }

    private static SharedPreferences get(Context context) {
        return context.getSharedPreferences(context.getPackageName(), 0);
    }
}
