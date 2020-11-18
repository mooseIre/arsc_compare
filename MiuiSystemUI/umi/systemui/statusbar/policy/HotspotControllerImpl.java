package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.net.TetheringManager;
import android.net.wifi.WifiClient;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.Looper;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.util.ConcurrentUtils;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.HotspotController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class HotspotControllerImpl implements HotspotController, WifiManager.SoftApCallback {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("HotspotController:v30", 3);
    private final ArrayList<HotspotController.Callback> mCallbacks = new ArrayList<>();
    private final Context mContext;
    /* access modifiers changed from: private */
    public volatile boolean mHasTetherableWifiRegexs = true;
    private int mHotspotState;
    /* access modifiers changed from: private */
    public volatile boolean mIsTetheringSupported = true;
    private final Handler mMainHandler;
    private volatile int mNumConnectedDevices;
    private TetheringManager.TetheringEventCallback mTetheringCallback = new TetheringManager.TetheringEventCallback() {
        public void onTetheringSupported(boolean z) {
            if (HotspotControllerImpl.this.mIsTetheringSupported != z) {
                boolean unused = HotspotControllerImpl.this.mIsTetheringSupported = z;
                HotspotControllerImpl.this.fireHotspotAvailabilityChanged();
            }
        }

        public void onTetherableInterfaceRegexpsChanged(TetheringManager.TetheringInterfaceRegexps tetheringInterfaceRegexps) {
            boolean z = tetheringInterfaceRegexps.getTetherableWifiRegexs().size() != 0;
            if (HotspotControllerImpl.this.mHasTetherableWifiRegexs != z) {
                boolean unused = HotspotControllerImpl.this.mHasTetherableWifiRegexs = z;
                HotspotControllerImpl.this.fireHotspotAvailabilityChanged();
            }
        }
    };
    private final TetheringManager mTetheringManager;
    private boolean mWaitingForTerminalState;
    private final WifiManager mWifiManager;

    private static String stateToString(int i) {
        switch (i) {
            case 10:
                return "DISABLING";
            case 11:
                return "DISABLED";
            case 12:
                return "ENABLING";
            case 13:
                return "ENABLED";
            case 14:
                return "FAILED";
            default:
                return null;
        }
    }

    public HotspotControllerImpl(Context context) {
        this.mContext = context;
        this.mTetheringManager = (TetheringManager) context.getSystemService(TetheringManager.class);
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mMainHandler = (Handler) Dependency.get(Dependency.MAIN_HANDLER);
        this.mTetheringManager.registerTetheringEventCallback(new HandlerExecutor(new Handler((Looper) Dependency.get(Dependency.BG_LOOPER))), this.mTetheringCallback);
    }

    public boolean isHotspotSupported() {
        return this.mIsTetheringSupported && this.mHasTetherableWifiRegexs && UserManager.get(this.mContext).isUserAdmin(ActivityManager.getCurrentUser());
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("HotspotController state:");
        printWriter.print("  available=");
        printWriter.println(isHotspotSupported());
        printWriter.print("  mHotspotState=");
        printWriter.println(stateToString(this.mHotspotState));
        printWriter.print("  mNumConnectedDevices=");
        printWriter.println(this.mNumConnectedDevices);
        printWriter.print("  mWaitingForTerminalState=");
        printWriter.println(this.mWaitingForTerminalState);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0052, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0054, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addCallback(com.android.systemui.statusbar.policy.HotspotController.Callback r5) {
        /*
            r4 = this;
            java.util.ArrayList<com.android.systemui.statusbar.policy.HotspotController$Callback> r0 = r4.mCallbacks
            monitor-enter(r0)
            if (r5 == 0) goto L_0x0053
            java.util.ArrayList<com.android.systemui.statusbar.policy.HotspotController$Callback> r1 = r4.mCallbacks     // Catch:{ all -> 0x0055 }
            boolean r1 = r1.contains(r5)     // Catch:{ all -> 0x0055 }
            if (r1 == 0) goto L_0x000e
            goto L_0x0053
        L_0x000e:
            boolean r1 = DEBUG     // Catch:{ all -> 0x0055 }
            if (r1 == 0) goto L_0x0028
            java.lang.String r1 = "HotspotController:v30"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0055 }
            r2.<init>()     // Catch:{ all -> 0x0055 }
            java.lang.String r3 = "addCallback "
            r2.append(r3)     // Catch:{ all -> 0x0055 }
            r2.append(r5)     // Catch:{ all -> 0x0055 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0055 }
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x0055 }
        L_0x0028:
            java.util.ArrayList<com.android.systemui.statusbar.policy.HotspotController$Callback> r1 = r4.mCallbacks     // Catch:{ all -> 0x0055 }
            r1.add(r5)     // Catch:{ all -> 0x0055 }
            android.net.wifi.WifiManager r1 = r4.mWifiManager     // Catch:{ all -> 0x0055 }
            if (r1 == 0) goto L_0x0051
            java.util.ArrayList<com.android.systemui.statusbar.policy.HotspotController$Callback> r1 = r4.mCallbacks     // Catch:{ all -> 0x0055 }
            int r1 = r1.size()     // Catch:{ all -> 0x0055 }
            r2 = 1
            if (r1 != r2) goto L_0x0047
            android.net.wifi.WifiManager r5 = r4.mWifiManager     // Catch:{ all -> 0x0055 }
            android.os.HandlerExecutor r1 = new android.os.HandlerExecutor     // Catch:{ all -> 0x0055 }
            android.os.Handler r2 = r4.mMainHandler     // Catch:{ all -> 0x0055 }
            r1.<init>(r2)     // Catch:{ all -> 0x0055 }
            r5.registerSoftApCallback(r1, r4)     // Catch:{ all -> 0x0055 }
            goto L_0x0051
        L_0x0047:
            android.os.Handler r1 = r4.mMainHandler     // Catch:{ all -> 0x0055 }
            com.android.systemui.statusbar.policy.-$$Lambda$HotspotControllerImpl$C17PPPxxCR-pTmr2izVaDhyC9AQ r2 = new com.android.systemui.statusbar.policy.-$$Lambda$HotspotControllerImpl$C17PPPxxCR-pTmr2izVaDhyC9AQ     // Catch:{ all -> 0x0055 }
            r2.<init>(r5)     // Catch:{ all -> 0x0055 }
            r1.post(r2)     // Catch:{ all -> 0x0055 }
        L_0x0051:
            monitor-exit(r0)     // Catch:{ all -> 0x0055 }
            return
        L_0x0053:
            monitor-exit(r0)     // Catch:{ all -> 0x0055 }
            return
        L_0x0055:
            r4 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0055 }
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.HotspotControllerImpl.addCallback(com.android.systemui.statusbar.policy.HotspotController$Callback):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$addCallback$0 */
    public /* synthetic */ void lambda$addCallback$0$HotspotControllerImpl(HotspotController.Callback callback) {
        callback.onHotspotChanged(isHotspotEnabled());
    }

    public void removeCallback(HotspotController.Callback callback) {
        if (callback != null) {
            if (DEBUG) {
                Log.d("HotspotController:v30", "removeCallback " + callback);
            }
            synchronized (this.mCallbacks) {
                this.mCallbacks.remove(callback);
                if (this.mCallbacks.isEmpty() && this.mWifiManager != null) {
                    this.mWifiManager.unregisterSoftApCallback(this);
                }
            }
        }
    }

    public boolean isHotspotEnabled() {
        return this.mHotspotState == 13;
    }

    public boolean isHotspotTransient() {
        return this.mWaitingForTerminalState || this.mHotspotState == 12;
    }

    public void setHotspotEnabled(boolean z) {
        if (this.mWaitingForTerminalState) {
            if (DEBUG) {
                Log.d("HotspotController:v30", "Ignoring setHotspotEnabled; waiting for terminal state.");
            }
        } else if (z) {
            this.mWaitingForTerminalState = true;
            if (DEBUG) {
                Log.d("HotspotController:v30", "Starting tethering");
            }
            this.mTetheringManager.startTethering(new TetheringManager.TetheringRequest.Builder(0).build(), ConcurrentUtils.DIRECT_EXECUTOR, new TetheringManager.StartTetheringCallback() {
                public void onTetheringFailed(int i) {
                    if (HotspotControllerImpl.DEBUG) {
                        Log.d("HotspotController:v30", "onTetheringFailed");
                    }
                    HotspotControllerImpl.this.maybeResetSoftApState();
                    HotspotControllerImpl.this.fireHotspotChangedCallback();
                }
            });
        } else {
            this.mTetheringManager.stopTethering(0);
        }
    }

    /* access modifiers changed from: private */
    public void fireHotspotChangedCallback() {
        ArrayList<HotspotController.Callback> arrayList;
        synchronized (this.mCallbacks) {
            arrayList = new ArrayList<>(this.mCallbacks);
        }
        for (HotspotController.Callback onHotspotChanged : arrayList) {
            onHotspotChanged.onHotspotChanged(isHotspotEnabled());
        }
    }

    /* access modifiers changed from: private */
    public void fireHotspotAvailabilityChanged() {
        ArrayList<HotspotController.Callback> arrayList;
        synchronized (this.mCallbacks) {
            arrayList = new ArrayList<>(this.mCallbacks);
        }
        for (HotspotController.Callback onHotspotAvailabilityChanged : arrayList) {
            onHotspotAvailabilityChanged.onHotspotAvailabilityChanged(isHotspotSupported());
        }
    }

    public void onStateChanged(int i, int i2) {
        this.mHotspotState = i;
        maybeResetSoftApState();
        if (!isHotspotEnabled()) {
            this.mNumConnectedDevices = 0;
        }
        fireHotspotChangedCallback();
    }

    /* access modifiers changed from: private */
    public void maybeResetSoftApState() {
        if (this.mWaitingForTerminalState) {
            int i = this.mHotspotState;
            if (!(i == 11 || i == 13)) {
                if (i == 14) {
                    this.mTetheringManager.stopTethering(0);
                } else {
                    return;
                }
            }
            this.mWaitingForTerminalState = false;
        }
    }

    public void onConnectedClientsChanged(List<WifiClient> list) {
        this.mNumConnectedDevices = list.size();
        fireHotspotChangedCallback();
    }

    public boolean isHotspotReady() {
        int i = this.mHotspotState;
        return i == 13 || i == 11 || i == 14;
    }
}
