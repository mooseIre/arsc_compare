package com.android.systemui.classifier;

import android.os.SystemProperties;
import android.util.Log;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import miui.os.Build;

public class FalsingLog {
    public static final boolean ENABLED = SystemProperties.getBoolean("debug.falsing_log", Build.IS_DEVELOPMENT_VERSION);
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
    }

    public static void w(String str, String str2) {
        if (LOGCAT) {
            Log.w("FalsingLog", str + "\t" + str2);
        }
        log("W", str, str2);
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
                return;
            }
            if (sInstance != null) {
                if (!sInstance.mLog.isEmpty()) {
                    Iterator<String> it = sInstance.mLog.iterator();
                    while (it.hasNext()) {
                        printWriter.println(it.next());
                    }
                    printWriter.println();
                    return;
                }
            }
            printWriter.println("<empty>");
            printWriter.println();
        }
    }
}
