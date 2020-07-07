package com.android.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.util.SparseLongArray;

public class LatencyTracker {
    private static final String[] NAMES = {"expand panel", "toggle recents", "fingerprint wake-and-unlock", "check credential", "check credential unlocked", "turn on screen"};
    private static LatencyTracker sLatencyTracker;
    private boolean mEnabled;
    private final SparseLongArray mStartRtc = new SparseLongArray();

    public static LatencyTracker getInstance(Context context) {
        if (sLatencyTracker == null) {
            sLatencyTracker = new LatencyTracker(context);
        }
        return sLatencyTracker;
    }

    private LatencyTracker(Context context) {
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                LatencyTracker.this.reloadProperty();
            }
        }, new IntentFilter("com.android.systemui.RELOAD_LATENCY_TRACKER_PROPERTY"));
        reloadProperty();
    }

    /* access modifiers changed from: private */
    public void reloadProperty() {
        this.mEnabled = SystemProperties.getBoolean("debug.systemui.latency_tracking", false);
    }

    public static boolean isEnabled(Context context) {
        return Build.IS_DEBUGGABLE && getInstance(context).mEnabled;
    }

    public void onActionStart(int i) {
        if (this.mEnabled) {
            Trace.asyncTraceBegin(4096, NAMES[i], 0);
            this.mStartRtc.put(i, SystemClock.elapsedRealtime());
        }
    }

    public void onActionEnd(int i) {
        if (this.mEnabled) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            long j = this.mStartRtc.get(i, -1);
            if (j != -1) {
                this.mStartRtc.delete(i);
                Trace.asyncTraceEnd(4096, NAMES[i], 0);
                Log.i("LatencyTracker", "action=" + i + " latency=" + (elapsedRealtime - j));
            }
        }
    }
}
