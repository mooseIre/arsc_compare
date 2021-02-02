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

    /* JADX WARNING: Removed duplicated region for block: B:28:0x007d A[SYNTHETIC, Splitter:B:28:0x007d] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0085 A[Catch:{ all -> 0x0081 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void wtf(java.lang.String r7, java.lang.String r8, java.lang.Throwable r9) {
        /*
            java.lang.Class<com.android.systemui.classifier.FalsingLog> r0 = com.android.systemui.classifier.FalsingLog.class
            monitor-enter(r0)
            boolean r1 = ENABLED     // Catch:{ all -> 0x00b3 }
            if (r1 != 0) goto L_0x0009
            monitor-exit(r0)
            return
        L_0x0009:
            e(r7, r8)     // Catch:{ all -> 0x00b3 }
            android.app.Application r1 = android.app.ActivityThread.currentApplication()     // Catch:{ all -> 0x00b3 }
            java.lang.String r2 = ""
            boolean r3 = android.os.Build.IS_DEBUGGABLE     // Catch:{ all -> 0x00b3 }
            if (r3 == 0) goto L_0x0089
            if (r1 == 0) goto L_0x0089
            java.io.File r3 = new java.io.File     // Catch:{ all -> 0x00b3 }
            java.io.File r1 = r1.getDataDir()     // Catch:{ all -> 0x00b3 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b3 }
            r4.<init>()     // Catch:{ all -> 0x00b3 }
            java.lang.String r5 = "falsing-"
            r4.append(r5)     // Catch:{ all -> 0x00b3 }
            java.text.SimpleDateFormat r5 = new java.text.SimpleDateFormat     // Catch:{ all -> 0x00b3 }
            java.lang.String r6 = "yyyy-MM-dd-HH-mm-ss"
            r5.<init>(r6)     // Catch:{ all -> 0x00b3 }
            java.util.Date r6 = new java.util.Date     // Catch:{ all -> 0x00b3 }
            r6.<init>()     // Catch:{ all -> 0x00b3 }
            java.lang.String r5 = r5.format(r6)     // Catch:{ all -> 0x00b3 }
            r4.append(r5)     // Catch:{ all -> 0x00b3 }
            java.lang.String r5 = ".txt"
            r4.append(r5)     // Catch:{ all -> 0x00b3 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x00b3 }
            r3.<init>(r1, r4)     // Catch:{ all -> 0x00b3 }
            r1 = 0
            java.io.PrintWriter r4 = new java.io.PrintWriter     // Catch:{ IOException -> 0x0071, all -> 0x006f }
            r4.<init>(r3)     // Catch:{ IOException -> 0x0071, all -> 0x006f }
            dump(r4)     // Catch:{ IOException -> 0x006d }
            r4.close()     // Catch:{ IOException -> 0x006d }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x006d }
            r1.<init>()     // Catch:{ IOException -> 0x006d }
            java.lang.String r5 = "Log written to "
            r1.append(r5)     // Catch:{ IOException -> 0x006d }
            java.lang.String r3 = r3.getAbsolutePath()     // Catch:{ IOException -> 0x006d }
            r1.append(r3)     // Catch:{ IOException -> 0x006d }
            java.lang.String r1 = r1.toString()     // Catch:{ IOException -> 0x006d }
            r4.close()     // Catch:{ all -> 0x00b3 }
            r2 = r1
            goto L_0x0090
        L_0x006d:
            r1 = move-exception
            goto L_0x0074
        L_0x006f:
            r7 = move-exception
            goto L_0x0083
        L_0x0071:
            r3 = move-exception
            r4 = r1
            r1 = r3
        L_0x0074:
            java.lang.String r3 = "FalsingLog"
            java.lang.String r5 = "Unable to write falsing log"
            android.util.Log.e(r3, r5, r1)     // Catch:{ all -> 0x0081 }
            if (r4 == 0) goto L_0x0090
            r4.close()     // Catch:{ all -> 0x00b3 }
            goto L_0x0090
        L_0x0081:
            r7 = move-exception
            r1 = r4
        L_0x0083:
            if (r1 == 0) goto L_0x0088
            r1.close()     // Catch:{ all -> 0x00b3 }
        L_0x0088:
            throw r7     // Catch:{ all -> 0x00b3 }
        L_0x0089:
            java.lang.String r1 = "FalsingLog"
            java.lang.String r3 = "Unable to write log, build must be debuggable."
            android.util.Log.e(r1, r3)     // Catch:{ all -> 0x00b3 }
        L_0x0090:
            java.lang.String r1 = "FalsingLog"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b3 }
            r3.<init>()     // Catch:{ all -> 0x00b3 }
            r3.append(r7)     // Catch:{ all -> 0x00b3 }
            java.lang.String r7 = " "
            r3.append(r7)     // Catch:{ all -> 0x00b3 }
            r3.append(r8)     // Catch:{ all -> 0x00b3 }
            java.lang.String r7 = "; "
            r3.append(r7)     // Catch:{ all -> 0x00b3 }
            r3.append(r2)     // Catch:{ all -> 0x00b3 }
            java.lang.String r7 = r3.toString()     // Catch:{ all -> 0x00b3 }
            android.util.Log.wtf(r1, r7, r9)     // Catch:{ all -> 0x00b3 }
            monitor-exit(r0)
            return
        L_0x00b3:
            r7 = move-exception
            monitor-exit(r0)
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.classifier.FalsingLog.wtf(java.lang.String, java.lang.String, java.lang.Throwable):void");
    }
}
