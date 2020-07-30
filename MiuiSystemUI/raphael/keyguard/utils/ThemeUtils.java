package com.android.keyguard.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import com.android.keyguard.common.BackgroundThread;
import miui.content.res.ThemeNativeUtils;

public class ThemeUtils {
    public static void tellThemeLockWallpaperPath(final Context context, final String str) {
        BackgroundThread.post(new Runnable() {
            public void run() {
                Uri parse = Uri.parse("content://com.android.thememanager.provider/lockscreen");
                ContentValues contentValues = new ContentValues();
                contentValues.put("key_lockscreen_path", str);
                ContentProviderUtils.updateData(context, parse, contentValues);
            }
        });
    }

    public static boolean updateFilePermissionWithThemeContext(String str) {
        return ThemeNativeUtils.updateFilePermissionWithThemeContext(str);
    }
}
