package com.android.systemui.classifier;

import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class FalsingLog {
    public static final boolean ENABLED = SystemProperties.getBoolean("debug.falsing_log", Build.IS_DEBUGGABLE);
    private static final boolean LOGCAT = SystemProperties.getBoolean("debug.falsing_logcat", false);
    private static final int MAX_SIZE = SystemProperties.getInt("debug.falsing_log_size", 100);
    private static FalsingLog sInstance;
    private final SimpleDateFormat mFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US);
    private final ArrayDeque<String> mLog = new ArrayDeque<>(MAX_SIZE);

    private FalsingLog() {
    }

    public static void i(String str, String str2) {
        if (LOGCAT) {
            Log.i("FalsingLog", str + "\t" + str2);
        }
        log("I", str, str2);
    }

    public static void wLogcat(String str, String str2) {
        Log.w("FalsingLog", str + "\t" + str2);
        log("W", str, str2);
    }

    public static void e(String str, String str2) {
        if (LOGCAT) {
            Log.e("FalsingLog", str + "\t" + str2);
        }
        log("E", str, str2);
    }

    public static synchronized void log(String str, String str2, String str3) {
        synchronized (FalsingLog.class) {
            if (ENABLED) {
                if (sInstance == null) {
                    sInstance = new FalsingLog();
                }
                if (sInstance.mLog.size() >= MAX_SIZE) {
                    sInstance.mLog.removeFirst();
                }
                sInstance.mLog.add(sInstance.mFormat.format(new Date()) + " " + str + " " + str2 + " " + str3);
            }
        }
    }

    public static synchronized void dump(PrintWriter printWriter) {
        synchronized (FalsingLog.class) {
            printWriter.println("FALSING LOG:");
            if (!ENABLED) {
                printWriter.println("Disabled, to enable: setprop debug.falsing_log 1");
                printWriter.println();
            } else if (sInstance == null || sInstance.mLog.isEmpty()) {
                printWriter.println("<empty>");
                printWriter.println();
            } else {
                Iterator<String> it = sInstance.mLog.iterator();
                while (it.hasNext()) {
                    printWriter.println(it.next());
                }
                printWriter.println();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0085  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void wtf(java.lang.String r7, java.lang.String r8, java.lang.Throwable r9) {
        /*
        // Method dump skipped, instructions count: 182
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.classifier.FalsingLog.wtf(java.lang.String, java.lang.String, java.lang.Throwable):void");
    }
}
