package com.android.systemui.util.leak;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;

public class GarbageMonitor {
    private final Handler mHandler;
    private final LeakReporter mLeakReporter;
    private final TrackedGarbage mTrackedGarbage;

    public GarbageMonitor(Looper looper, LeakDetector leakDetector, LeakReporter leakReporter) {
        this.mHandler = looper != null ? new Handler(looper) : null;
        this.mTrackedGarbage = leakDetector.getTrackedGarbage();
        this.mLeakReporter = leakReporter;
    }

    public void start() {
        if (this.mTrackedGarbage != null) {
            scheduleInspectGarbage(new Runnable() {
                public void run() {
                    GarbageMonitor.this.inspectGarbage();
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleInspectGarbage(Runnable runnable) {
        this.mHandler.postDelayed(runnable, 300000);
    }

    /* access modifiers changed from: private */
    public void inspectGarbage() {
        if (this.mTrackedGarbage.countOldGarbage() > 5) {
            Runtime.getRuntime().gc();
            scheduleReinspectGarbage(new Runnable() {
                public void run() {
                    GarbageMonitor.this.reinspectGarbageAfterGc();
                }
            });
        }
        scheduleInspectGarbage(new Runnable() {
            public void run() {
                GarbageMonitor.this.inspectGarbage();
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void scheduleReinspectGarbage(Runnable runnable) {
        this.mHandler.postDelayed(runnable, 100);
    }

    /* access modifiers changed from: private */
    public void reinspectGarbageAfterGc() {
        int countOldGarbage = this.mTrackedGarbage.countOldGarbage();
        if (countOldGarbage > 5) {
            this.mLeakReporter.dumpLeak(countOldGarbage);
        }
    }

    public static class Service extends SystemUI {
        private static final boolean ENABLED;
        private GarbageMonitor mGarbageMonitor;

        static {
            boolean z = false;
            if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("debug.enable_leak_reporting", false)) {
                z = true;
            }
            ENABLED = z;
        }

        public void start() {
            if (ENABLED) {
                this.mGarbageMonitor = (GarbageMonitor) Dependency.get(GarbageMonitor.class);
                this.mGarbageMonitor.start();
            }
        }
    }
}
