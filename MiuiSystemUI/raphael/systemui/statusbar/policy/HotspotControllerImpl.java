package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.ConnectivityManagerCompat;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.statusbar.policy.HotspotController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class HotspotControllerImpl implements HotspotController {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("HotspotController", 3);
    private final ArrayList<HotspotController.Callback> mCallbacks = new ArrayList<>();
    private final ConnectivityManager mConnectivityManager;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public int mHotspotState = 11;
    private final Receiver mReceiver = new Receiver();
    /* access modifiers changed from: private */
    public boolean mWaitingForCallback;
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
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
    }

    public boolean isHotspotSupported() {
        return this.mConnectivityManager.isTetheringSupported() && this.mConnectivityManager.getTetherableWifiRegexs().length != 0 && UserManager.get(this.mContext).isAdminUser();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("HotspotController state:");
        printWriter.print("  mHotspotEnabled=");
        printWriter.println(stateToString(this.mHotspotState));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0047, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addCallback(com.android.systemui.statusbar.policy.HotspotController.Callback r5) {
        /*
            r4 = this;
            java.util.ArrayList<com.android.systemui.statusbar.policy.HotspotController$Callback> r0 = r4.mCallbacks
            monitor-enter(r0)
            if (r5 == 0) goto L_0x0046
            java.util.ArrayList<com.android.systemui.statusbar.policy.HotspotController$Callback> r1 = r4.mCallbacks     // Catch:{ all -> 0x0048 }
            boolean r1 = r1.contains(r5)     // Catch:{ all -> 0x0048 }
            if (r1 == 0) goto L_0x000e
            goto L_0x0046
        L_0x000e:
            boolean r1 = DEBUG     // Catch:{ all -> 0x0048 }
            if (r1 == 0) goto L_0x0028
            java.lang.String r1 = "HotspotController"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0048 }
            r2.<init>()     // Catch:{ all -> 0x0048 }
            java.lang.String r3 = "addCallback "
            r2.append(r3)     // Catch:{ all -> 0x0048 }
            r2.append(r5)     // Catch:{ all -> 0x0048 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0048 }
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x0048 }
        L_0x0028:
            java.util.ArrayList<com.android.systemui.statusbar.policy.HotspotController$Callback> r1 = r4.mCallbacks     // Catch:{ all -> 0x0048 }
            r1.add(r5)     // Catch:{ all -> 0x0048 }
            com.android.systemui.statusbar.policy.HotspotControllerImpl$Receiver r1 = r4.mReceiver     // Catch:{ all -> 0x0048 }
            java.util.ArrayList<com.android.systemui.statusbar.policy.HotspotController$Callback> r2 = r4.mCallbacks     // Catch:{ all -> 0x0048 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x0048 }
            if (r2 != 0) goto L_0x0039
            r2 = 1
            goto L_0x003a
        L_0x0039:
            r2 = 0
        L_0x003a:
            r1.setListening(r2)     // Catch:{ all -> 0x0048 }
            boolean r4 = r4.isHotspotEnabled()     // Catch:{ all -> 0x0048 }
            r5.onHotspotChanged(r4)     // Catch:{ all -> 0x0048 }
            monitor-exit(r0)     // Catch:{ all -> 0x0048 }
            return
        L_0x0046:
            monitor-exit(r0)     // Catch:{ all -> 0x0048 }
            return
        L_0x0048:
            r4 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0048 }
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.HotspotControllerImpl.addCallback(com.android.systemui.statusbar.policy.HotspotController$Callback):void");
    }

    public void removeCallback(HotspotController.Callback callback) {
        if (callback != null) {
            if (DEBUG) {
                Log.d("HotspotController", "removeCallback " + callback);
            }
            synchronized (this.mCallbacks) {
                this.mCallbacks.remove(callback);
                this.mReceiver.setListening(!this.mCallbacks.isEmpty());
            }
        }
    }

    public boolean isHotspotEnabled() {
        return this.mHotspotState == 13;
    }

    public boolean isHotspotReady() {
        int i = this.mHotspotState;
        return i == 13 || i == 11 || i == 14;
    }

    public boolean isHotspotTransient() {
        return this.mWaitingForCallback || this.mHotspotState == 12;
    }

    public void setHotspotEnabled(boolean z) {
        if (Build.VERSION.SDK_INT < 24) {
            setHotspotEnabledWithWifiManager(z);
        } else {
            setHotspotEnabledWithConnectivityManager(z);
        }
    }

    private void setHotspotEnabledWithConnectivityManager(boolean z) {
        Log.d("HotspotController", "setHotspotEnabledWithConnectivityManager: enabled=" + z);
        if (z) {
            OnStartTetheringCallback onStartTetheringCallback = new OnStartTetheringCallback();
            this.mWaitingForCallback = true;
            if (DEBUG) {
                Log.d("HotspotController", "Starting tethering");
            }
            ConnectivityManagerCompat.startTethering(this.mConnectivityManager, 0, false, onStartTetheringCallback);
            fireCallback(isHotspotEnabled());
            return;
        }
        ConnectivityManagerCompat.stopTethering(this.mConnectivityManager, 0);
    }

    private void setHotspotEnabledWithWifiManager(boolean z) {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        int wifiState = this.mWifiManager.getWifiState();
        if (Build.VERSION.SDK_INT < 23 && z && (wifiState == 2 || wifiState == 3)) {
            this.mWifiManager.setWifiEnabled(false);
            Settings.Global.putInt(contentResolver, "wifi_saved_state", 1);
        }
        fireCallback(isHotspotEnabled());
        if (Build.VERSION.SDK_INT < 23 && !z && Settings.Global.getInt(contentResolver, "wifi_saved_state", 0) == 1) {
            this.mWifiManager.setWifiEnabled(true);
            Settings.Global.putInt(contentResolver, "wifi_saved_state", 0);
        }
    }

    /* access modifiers changed from: private */
    public void fireCallback(boolean z) {
        synchronized (this.mCallbacks) {
            Iterator<HotspotController.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onHotspotChanged(z);
            }
        }
    }

    private final class OnStartTetheringCallback extends ConnectivityManagerCompat.OnStartTetheringCallback {
        private OnStartTetheringCallback() {
        }

        public void onTetheringStarted() {
            if (HotspotControllerImpl.DEBUG) {
                Log.d("HotspotController", "onTetheringStarted");
            }
            boolean unused = HotspotControllerImpl.this.mWaitingForCallback = false;
        }

        public void onTetheringFailed() {
            if (HotspotControllerImpl.DEBUG) {
                Log.d("HotspotController", "onTetheringFailed");
            }
            boolean unused = HotspotControllerImpl.this.mWaitingForCallback = false;
            HotspotControllerImpl hotspotControllerImpl = HotspotControllerImpl.this;
            hotspotControllerImpl.fireCallback(hotspotControllerImpl.isHotspotEnabled());
        }
    }

    private final class Receiver extends BroadcastReceiver {
        private boolean mRegistered;

        private Receiver() {
        }

        public void setListening(boolean z) {
            if (z && !this.mRegistered) {
                if (HotspotControllerImpl.DEBUG) {
                    Log.d("HotspotController", "Registering receiver");
                }
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
                HotspotControllerImpl.this.mContext.registerReceiver(this, intentFilter);
                this.mRegistered = true;
            } else if (!z && this.mRegistered) {
                if (HotspotControllerImpl.DEBUG) {
                    Log.d("HotspotController", "Unregistering receiver");
                }
                HotspotControllerImpl.this.mContext.unregisterReceiver(this);
                this.mRegistered = false;
            }
        }

        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            if (intent.getIntExtra("wifi_ap_mode", -1) == 1) {
                int intExtra = intent.getIntExtra("wifi_state", 14);
                if (HotspotControllerImpl.DEBUG) {
                    Log.d("HotspotController", "onReceive " + intExtra);
                }
                int unused = HotspotControllerImpl.this.mHotspotState = intExtra;
                HotspotControllerImpl hotspotControllerImpl = HotspotControllerImpl.this;
                if (hotspotControllerImpl.mHotspotState != 13) {
                    z = false;
                }
                hotspotControllerImpl.fireCallback(z);
            }
        }
    }
}
