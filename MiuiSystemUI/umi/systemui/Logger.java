package com.android.systemui;

import android.os.AsyncTask;
import miui.os.Build;
import miui.util.Log;

public final class Logger {
    private static final boolean IS_STABLE_VERSION = Build.IS_STABLE_VERSION;

    public static void i(String str, String str2) {
        Log.i(str, str2);
    }

    public static void w(String str, String str2) {
        Log.w(str, str2);
    }

    public static void e(String str) {
        e("SystemUI_LOG", str);
    }

    public static void e(String str, String str2) {
        Log.e(str, str2);
    }

    public static void fileI(final String str, final String str2) {
        if (!IS_STABLE_VERSION) {
            AsyncTask.execute(new Runnable() {
                public void run() {
                    Log.getFileLogger().info(str, str2);
                }
            });
        }
    }

    public static void fileW(final String str, final String str2) {
        if (!IS_STABLE_VERSION) {
            AsyncTask.execute(new Runnable() {
                public void run() {
                    Log.getFileLogger().warn(str, str2);
                }
            });
        }
    }

    public static void fileE(final String str, final String str2) {
        if (!IS_STABLE_VERSION) {
            AsyncTask.execute(new Runnable() {
                public void run() {
                    Log.getFileLogger().error(str, str2);
                }
            });
        }
    }

    public static void fullI(String str, String str2) {
        i(str, str2);
        fileI(str, str2);
    }

    public static void fullW(String str, String str2) {
        w(str, str2);
        fileW(str, str2);
    }

    public static void fullE(String str, String str2) {
        e(str, str2);
        fileE(str, str2);
    }
}
