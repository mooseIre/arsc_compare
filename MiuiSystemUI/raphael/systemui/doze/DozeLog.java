package com.android.systemui.doze;

import android.util.TimeUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dumpable;
import com.android.systemui.doze.DozeMachine;
import com.android.systemui.dump.DumpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Array;

public class DozeLog implements Dumpable {
    private SummaryStats mEmergencyCallStats;
    private final KeyguardUpdateMonitorCallback mKeyguardCallback = new KeyguardUpdateMonitorCallback() {
        /* class com.android.systemui.doze.DozeLog.AnonymousClass1 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onEmergencyCallAction() {
            DozeLog.this.traceEmergencyCall();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardBouncerChanged(boolean z) {
            DozeLog.this.traceKeyguardBouncerChanged(z);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedWakingUp() {
            DozeLog.this.traceScreenOn();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFinishedGoingToSleep(int i) {
            DozeLog.this.traceScreenOff(i);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            DozeLog.this.traceKeyguard(z);
        }
    };
    private final DozeLogger mLogger;
    private SummaryStats mNotificationPulseStats;
    private SummaryStats mPickupPulseNearVibrationStats;
    private SummaryStats mPickupPulseNotNearVibrationStats;
    private SummaryStats[][] mProxStats;
    private boolean mPulsing;
    private SummaryStats mScreenOnNotPulsingStats;
    private SummaryStats mScreenOnPulsingStats;
    private long mSince;

    public DozeLog(KeyguardUpdateMonitor keyguardUpdateMonitor, DumpManager dumpManager, DozeLogger dozeLogger) {
        this.mLogger = dozeLogger;
        this.mSince = System.currentTimeMillis();
        this.mPickupPulseNearVibrationStats = new SummaryStats();
        this.mPickupPulseNotNearVibrationStats = new SummaryStats();
        this.mNotificationPulseStats = new SummaryStats();
        this.mScreenOnPulsingStats = new SummaryStats();
        this.mScreenOnNotPulsingStats = new SummaryStats();
        this.mEmergencyCallStats = new SummaryStats();
        this.mProxStats = (SummaryStats[][]) Array.newInstance(SummaryStats.class, 10, 2);
        for (int i = 0; i < 10; i++) {
            this.mProxStats[i][0] = new SummaryStats();
            this.mProxStats[i][1] = new SummaryStats();
        }
        if (keyguardUpdateMonitor != null) {
            keyguardUpdateMonitor.registerCallback(this.mKeyguardCallback);
        }
        dumpManager.registerDumpable("DumpStats", this);
    }

    public void tracePickupWakeUp(boolean z) {
        SummaryStats summaryStats;
        this.mLogger.logPickupWakeup(z);
        if (z) {
            summaryStats = this.mPickupPulseNearVibrationStats;
        } else {
            summaryStats = this.mPickupPulseNotNearVibrationStats;
        }
        summaryStats.append();
    }

    public void tracePulseStart(int i) {
        this.mLogger.logPulseStart(i);
        this.mPulsing = true;
    }

    public void tracePulseFinish() {
        this.mLogger.logPulseFinish();
        this.mPulsing = false;
    }

    public void traceNotificationPulse() {
        this.mLogger.logNotificationPulse();
        this.mNotificationPulseStats.append();
    }

    public void traceDozing(boolean z) {
        this.mLogger.logDozing(z);
        this.mPulsing = false;
    }

    public void traceFling(boolean z, boolean z2, boolean z3, boolean z4) {
        this.mLogger.logFling(z, z2, z3, z4);
    }

    public void traceEmergencyCall() {
        this.mLogger.logEmergencyCall();
        this.mEmergencyCallStats.append();
    }

    public void traceKeyguardBouncerChanged(boolean z) {
        this.mLogger.logKeyguardBouncerChanged(z);
    }

    public void traceScreenOn() {
        this.mLogger.logScreenOn(this.mPulsing);
        (this.mPulsing ? this.mScreenOnPulsingStats : this.mScreenOnNotPulsingStats).append();
        this.mPulsing = false;
    }

    public void traceScreenOff(int i) {
        this.mLogger.logScreenOff(i);
    }

    public void traceMissedTick(String str) {
        this.mLogger.logMissedTick(str);
    }

    public void traceTimeTickScheduled(long j, long j2) {
        this.mLogger.logTimeTickScheduled(j, j2);
    }

    public void traceKeyguard(boolean z) {
        this.mLogger.logKeyguardVisibilityChange(z);
        if (!z) {
            this.mPulsing = false;
        }
    }

    public void traceState(DozeMachine.State state) {
        this.mLogger.logDozeStateChanged(state);
    }

    public void traceWakeDisplay(boolean z) {
        this.mLogger.logWakeDisplay(z);
    }

    public void traceProximityResult(boolean z, long j, int i) {
        this.mLogger.logProximityResult(z, j, i);
        this.mProxStats[i][!z ? 1 : 0].append();
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        synchronized (DozeLog.class) {
            printWriter.print("  Doze summary stats (for ");
            TimeUtils.formatDuration(System.currentTimeMillis() - this.mSince, printWriter);
            printWriter.println("):");
            this.mPickupPulseNearVibrationStats.dump(printWriter, "Pickup pulse (near vibration)");
            this.mPickupPulseNotNearVibrationStats.dump(printWriter, "Pickup pulse (not near vibration)");
            this.mNotificationPulseStats.dump(printWriter, "Notification pulse");
            this.mScreenOnPulsingStats.dump(printWriter, "Screen on (pulsing)");
            this.mScreenOnNotPulsingStats.dump(printWriter, "Screen on (not pulsing)");
            this.mEmergencyCallStats.dump(printWriter, "Emergency call");
            for (int i = 0; i < 10; i++) {
                String reasonToString = reasonToString(i);
                this.mProxStats[i][0].dump(printWriter, "Proximity near (" + reasonToString + ")");
                this.mProxStats[i][1].dump(printWriter, "Proximity far (" + reasonToString + ")");
            }
        }
    }

    public void tracePulseDropped(boolean z, DozeMachine.State state, boolean z2) {
        this.mLogger.logPulseDropped(z, state, z2);
    }

    public void tracePulseDropped(String str) {
        this.mLogger.logPulseDropped(str);
    }

    public void tracePulseTouchDisabledByProx(boolean z) {
        this.mLogger.logPulseTouchDisabledByProx(z);
    }

    public void traceSensor(int i) {
        this.mLogger.logSensorTriggered(i);
    }

    public void traceDozeSuppressed(DozeMachine.State state) {
        this.mLogger.logDozeSuppressed(state);
    }

    /* access modifiers changed from: private */
    public class SummaryStats {
        private int mCount;

        private SummaryStats() {
        }

        public void append() {
            this.mCount++;
        }

        public void dump(PrintWriter printWriter, String str) {
            if (this.mCount != 0) {
                printWriter.print("    ");
                printWriter.print(str);
                printWriter.print(": n=");
                printWriter.print(this.mCount);
                printWriter.print(" (");
                printWriter.print((((double) this.mCount) / ((double) (System.currentTimeMillis() - DozeLog.this.mSince))) * 1000.0d * 60.0d * 60.0d);
                printWriter.print("/hr)");
                printWriter.println();
            }
        }
    }

    public static String reasonToString(int i) {
        switch (i) {
            case 0:
                return "intent";
            case 1:
                return "notification";
            case 2:
                return "sigmotion";
            case 3:
                return "pickup";
            case 4:
                return "doubletap";
            case 5:
                return "longpress";
            case 6:
                return "docking";
            case 7:
                return "wakeup";
            case 8:
                return "wakelockscreen";
            case 9:
                return "tap";
            default:
                throw new IllegalArgumentException("invalid reason: " + i);
        }
    }
}
