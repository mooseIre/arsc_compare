package com.android.keyguard.utils;

import android.content.ContentResolver;
import android.provider.MiuiSettings;

public class MiuiSettingsUtils {
    public static boolean putStringToSystem(ContentResolver contentResolver, String str, String str2) {
        return MiuiSettings.System.putString(contentResolver, str, str2);
    }
}
