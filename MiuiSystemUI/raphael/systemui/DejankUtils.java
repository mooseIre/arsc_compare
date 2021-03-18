package com.android.systemui;

import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.view.Choreographer;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.util.Assert;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;
import java.util.function.Supplier;

public class DejankUtils {
    public static final boolean STRICT_MODE_ENABLED;
    private static final Runnable sAnimationCallbackRunnable = $$Lambda$DejankUtils$SyBRIrRRZtwJZ1Fy9Pe5WnzuioU.INSTANCE;
    private static Stack<String> sBlockingIpcs = new Stack<>();
    private static final Choreographer sChoreographer = Choreographer.getInstance();
    private static final Handler sHandler = new Handler();
    private static boolean sImmediate;
    private static final Object sLock = new Object();
    private static final ArrayList<Runnable> sPendingRunnables = new ArrayList<>();
    private static final Binder.ProxyTransactListener sProxy = new Binder.ProxyTransactListener() {
        /* class com.android.systemui.DejankUtils.AnonymousClass1 */

        public void onTransactEnded(Object obj) {
        }

        public Object onTransactStarted(IBinder iBinder, int i) {
            return null;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
            r1 = r2.getInterfaceDescriptor();
            r2 = com.android.systemui.DejankUtils.sLock;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x002a, code lost:
            monitor-enter(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0033, code lost:
            if (com.android.systemui.DejankUtils.sWhitelistedFrameworkClasses.contains(r1) == false) goto L_0x0037;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0035, code lost:
            monitor-exit(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0036, code lost:
            return null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0037, code lost:
            monitor-exit(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x003c, code lost:
            r1 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x003d, code lost:
            r1.printStackTrace();
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.lang.Object onTransactStarted(android.os.IBinder r2, int r3, int r4) {
            /*
                r1 = this;
                java.lang.Object r1 = com.android.systemui.DejankUtils.access$000()
                monitor-enter(r1)
                r3 = 1
                r4 = r4 & r3
                r0 = 0
                if (r4 == r3) goto L_0x005f
                java.util.Stack r3 = com.android.systemui.DejankUtils.access$100()     // Catch:{ all -> 0x0061 }
                boolean r3 = r3.empty()     // Catch:{ all -> 0x0061 }
                if (r3 != 0) goto L_0x005f
                boolean r3 = com.android.settingslib.utils.ThreadUtils.isMainThread()     // Catch:{ all -> 0x0061 }
                if (r3 == 0) goto L_0x005f
                boolean r3 = com.android.systemui.DejankUtils.access$200()     // Catch:{ all -> 0x0061 }
                if (r3 == 0) goto L_0x0021
                goto L_0x005f
            L_0x0021:
                monitor-exit(r1)     // Catch:{ all -> 0x0061 }
                java.lang.String r1 = r2.getInterfaceDescriptor()     // Catch:{ RemoteException -> 0x003c }
                java.lang.Object r2 = com.android.systemui.DejankUtils.access$000()     // Catch:{ RemoteException -> 0x003c }
                monitor-enter(r2)     // Catch:{ RemoteException -> 0x003c }
                java.util.HashSet r3 = com.android.systemui.DejankUtils.access$300()     // Catch:{ all -> 0x0039 }
                boolean r1 = r3.contains(r1)     // Catch:{ all -> 0x0039 }
                if (r1 == 0) goto L_0x0037
                monitor-exit(r2)     // Catch:{ all -> 0x0039 }
                return r0
            L_0x0037:
                monitor-exit(r2)     // Catch:{ all -> 0x0039 }
                goto L_0x0040
            L_0x0039:
                r1 = move-exception
                monitor-exit(r2)     // Catch:{ all -> 0x0039 }
                throw r1
            L_0x003c:
                r1 = move-exception
                r1.printStackTrace()
            L_0x0040:
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "IPC detected on critical path: "
                r1.append(r2)
                java.util.Stack r2 = com.android.systemui.DejankUtils.access$100()
                java.lang.Object r2 = r2.peek()
                java.lang.String r2 = (java.lang.String) r2
                r1.append(r2)
                java.lang.String r1 = r1.toString()
                android.os.StrictMode.noteSlowCall(r1)
                return r0
            L_0x005f:
                monitor-exit(r1)
                return r0
            L_0x0061:
                r2 = move-exception
                monitor-exit(r1)
                throw r2
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.DejankUtils.AnonymousClass1.onTransactStarted(android.os.IBinder, int, int):java.lang.Object");
        }
    };
    private static boolean sTemporarilyIgnoreStrictMode;
    private static final HashSet<String> sWhitelistedFrameworkClasses = new HashSet<>();

    static {
        boolean z = false;
        if (Build.IS_ENG || SystemProperties.getBoolean("persist.sysui.strictmode", false)) {
            z = true;
        }
        STRICT_MODE_ENABLED = z;
        if (STRICT_MODE_ENABLED) {
            sWhitelistedFrameworkClasses.add("android.view.IWindowSession");
            sWhitelistedFrameworkClasses.add("com.android.internal.policy.IKeyguardStateCallback");
            sWhitelistedFrameworkClasses.add("android.os.IPowerManager");
            sWhitelistedFrameworkClasses.add("com.android.internal.statusbar.IStatusBarService");
            Binder.setProxyTransactListener(sProxy);
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectCustomSlowCalls().penaltyFlashScreen().penaltyLog().build());
        }
    }

    static /* synthetic */ void lambda$static$0() {
        for (int i = 0; i < sPendingRunnables.size(); i++) {
            sHandler.post(sPendingRunnables.get(i));
        }
        sPendingRunnables.clear();
    }

    public static void startDetectingBlockingIpcs(String str) {
        if (STRICT_MODE_ENABLED) {
            synchronized (sLock) {
                sBlockingIpcs.push(str);
            }
        }
    }

    public static void stopDetectingBlockingIpcs(String str) {
        if (STRICT_MODE_ENABLED) {
            synchronized (sLock) {
                sBlockingIpcs.remove(str);
            }
        }
    }

    public static void whitelistIpcs(Runnable runnable) {
        if (!STRICT_MODE_ENABLED || sTemporarilyIgnoreStrictMode) {
            runnable.run();
            return;
        }
        synchronized (sLock) {
            sTemporarilyIgnoreStrictMode = true;
        }
        try {
            runnable.run();
            synchronized (sLock) {
                sTemporarilyIgnoreStrictMode = false;
            }
        } catch (Throwable th) {
            synchronized (sLock) {
                sTemporarilyIgnoreStrictMode = false;
                throw th;
            }
        }
    }

    public static <T> T whitelistIpcs(Supplier<T> supplier) {
        if (!STRICT_MODE_ENABLED || sTemporarilyIgnoreStrictMode) {
            return supplier.get();
        }
        synchronized (sLock) {
            sTemporarilyIgnoreStrictMode = true;
        }
        try {
            T t = supplier.get();
            synchronized (sLock) {
                sTemporarilyIgnoreStrictMode = false;
            }
            return t;
        } catch (Throwable th) {
            synchronized (sLock) {
                sTemporarilyIgnoreStrictMode = false;
                throw th;
            }
        }
    }

    public static void postAfterTraversal(Runnable runnable) {
        if (sImmediate) {
            runnable.run();
            return;
        }
        Assert.isMainThread();
        sPendingRunnables.add(runnable);
        postAnimationCallback();
    }

    public static void removeCallbacks(Runnable runnable) {
        Assert.isMainThread();
        sPendingRunnables.remove(runnable);
        sHandler.removeCallbacks(runnable);
    }

    private static void postAnimationCallback() {
        sChoreographer.postCallback(1, sAnimationCallbackRunnable, null);
    }

    @VisibleForTesting
    public static void setImmediate(boolean z) {
        sImmediate = z;
    }
}
