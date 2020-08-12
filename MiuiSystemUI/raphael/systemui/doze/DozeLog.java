package com.android.systemui.doze;

import android.os.Build;
import android.util.Log;
import android.util.TimeUtils;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DozeLog {
    private static final boolean DEBUG = Log.isLoggable("DozeLog", 3);
    static final SimpleDateFormat FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
    private static final int SIZE = (Build.IS_DEBUGGABLE ? 400 : 50);
    private static int sCount;
    private static SummaryStats sEmergencyCallStats;
    private static final KeyguardUpdateMonitorCallback sKeyguardCallback = new KeyguardUpdateMonitorCallback() {
        public void onEmergencyCallAction() {
            DozeLog.traceEmergencyCall();
            throw null;
        }

        public void onKeyguardBouncerChanged(boolean z) {
            DozeLog.traceKeyguardBouncerChanged(z);
        }

        public void onStartedWakingUp() {
            DozeLog.traceScreenOn();
            throw null;
        }

        public void onFinishedGoingToSleep(int i) {
            DozeLog.traceScreenOff(i);
        }

        public void onKeyguardVisibilityChanged(boolean z) {
            DozeLog.traceKeyguard(z);
        }
    };
    private static String[] sMessages;
    private static SummaryStats sPickupPulseNearVibrationStats;
    private static int sPosition;
    private static boolean sPulsing;
    private static boolean sRegisterKeyguardCallback = true;
    private static SummaryStats sScreenOnNotPulsingStats;
    private static SummaryStats sScreenOnPulsingStats;
    private static long sSince;
    private static long[] sTimes;

    private static class SummaryStats {
        public void append() {
            throw null;
        }

        public void dump(PrintWriter printWriter, String str) {
            throw null;
        }
    }

    public static void tracePulseStart(int i) {
        sPulsing = true;
        log("pulseStart reason=" + pulseReasonToString(i));
    }

    public static void tracePulseFinish() {
        sPulsing = false;
        log("pulseFinish");
    }

    public static void traceFling(boolean z, boolean z2, boolean z3, boolean z4) {
        log("fling expand=" + z + " aboveThreshold=" + z2 + " thresholdNeeded=" + z3 + " screenOnFromTouch=" + z4);
    }

    public static void traceEmergencyCall() {
        log("emergencyCall");
        sEmergencyCallStats.append();
        throw null;
    }

    public static void traceKeyguardBouncerChanged(boolean z) {
        log("bouncer " + z);
    }

    public static void traceScreenOn() {
        log("screenOn pulsing=" + sPulsing);
        (sPulsing ? sScreenOnPulsingStats : sScreenOnNotPulsingStats).append();
        throw null;
    }

    public static void traceScreenOff(int i) {
        log("screenOff why=" + i);
    }

    public static void traceKeyguard(boolean z) {
        log("keyguard " + z);
        if (!z) {
            sPulsing = false;
        }
    }

    public static String pulseReasonToString(int i) {
        if (i == 0) {
            return "intent";
        }
        if (i == 1) {
            return "notification";
        }
        if (i == 2) {
            return "sigmotion";
        }
        if (i == 3) {
            return "pickup";
        }
        if (i == 4) {
            return "doubletap";
        }
        throw new IllegalArgumentException("bad reason: " + i);
    }

    public static void dump(PrintWriter printWriter) {
        synchronized (DozeLog.class) {
            if (sMessages != null) {
                printWriter.println("  Doze log:");
                int i = ((sPosition - sCount) + SIZE) % SIZE;
                for (int i2 = 0; i2 < sCount; i2++) {
                    int i3 = (i + i2) % SIZE;
                    printWriter.print("    ");
                    printWriter.print(FORMAT.format(new Date(sTimes[i3])));
                    printWriter.print(' ');
                    printWriter.println(sMessages[i3]);
                }
                printWriter.print("  Doze summary stats (for ");
                TimeUtils.formatDuration(System.currentTimeMillis() - sSince, printWriter);
                printWriter.println("):");
                sPickupPulseNearVibrationStats.dump(printWriter, "Pickup pulse (near vibration)");
                throw null;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0031, code lost:
        if (DEBUG == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0033, code lost:
        android.util.Log.d("DozeLog", r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void log(java.lang.String r5) {
        /*
            java.lang.Class<com.android.systemui.doze.DozeLog> r0 = com.android.systemui.doze.DozeLog.class
            monitor-enter(r0)
            java.lang.String[] r1 = sMessages     // Catch:{ all -> 0x0039 }
            if (r1 != 0) goto L_0x0009
            monitor-exit(r0)     // Catch:{ all -> 0x0039 }
            return
        L_0x0009:
            long[] r1 = sTimes     // Catch:{ all -> 0x0039 }
            int r2 = sPosition     // Catch:{ all -> 0x0039 }
            long r3 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x0039 }
            r1[r2] = r3     // Catch:{ all -> 0x0039 }
            java.lang.String[] r1 = sMessages     // Catch:{ all -> 0x0039 }
            int r2 = sPosition     // Catch:{ all -> 0x0039 }
            r1[r2] = r5     // Catch:{ all -> 0x0039 }
            int r1 = sPosition     // Catch:{ all -> 0x0039 }
            int r1 = r1 + 1
            int r2 = SIZE     // Catch:{ all -> 0x0039 }
            int r1 = r1 % r2
            sPosition = r1     // Catch:{ all -> 0x0039 }
            int r1 = sCount     // Catch:{ all -> 0x0039 }
            int r1 = r1 + 1
            int r2 = SIZE     // Catch:{ all -> 0x0039 }
            int r1 = java.lang.Math.min(r1, r2)     // Catch:{ all -> 0x0039 }
            sCount = r1     // Catch:{ all -> 0x0039 }
            monitor-exit(r0)     // Catch:{ all -> 0x0039 }
            boolean r0 = DEBUG
            if (r0 == 0) goto L_0x0038
            java.lang.String r0 = "DozeLog"
            android.util.Log.d(r0, r5)
        L_0x0038:
            return
        L_0x0039:
            r5 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0039 }
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.doze.DozeLog.log(java.lang.String):void");
    }
}
