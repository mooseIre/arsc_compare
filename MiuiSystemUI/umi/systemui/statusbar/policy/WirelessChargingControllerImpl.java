package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.statusbar.policy.WirelessChargingController;
import com.miui.systemui.annotation.Inject;
import com.xiaomi.stat.MiStat;
import java.util.ArrayList;
import java.util.Iterator;
import miui.util.IWirelessSwitch;

public class WirelessChargingControllerImpl implements WirelessChargingController {
    private final ArrayList<WirelessChargingController.Callback> mCallbacks = new ArrayList<>();
    /* access modifiers changed from: private */
    public final Context mContext;
    private final Receiver mReceiver = new Receiver();

    public WirelessChargingControllerImpl(@Inject Context context) {
        this.mContext = context;
    }

    public boolean isWirelessChargingSupported() {
        return IWirelessSwitch.getInstance().isWirelessChargingSupported();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0043, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addCallback(com.android.systemui.statusbar.policy.WirelessChargingController.Callback r5) {
        /*
            r4 = this;
            java.util.ArrayList<com.android.systemui.statusbar.policy.WirelessChargingController$Callback> r0 = r4.mCallbacks
            monitor-enter(r0)
            if (r5 == 0) goto L_0x0042
            java.util.ArrayList<com.android.systemui.statusbar.policy.WirelessChargingController$Callback> r1 = r4.mCallbacks     // Catch:{ all -> 0x0044 }
            boolean r1 = r1.contains(r5)     // Catch:{ all -> 0x0044 }
            if (r1 == 0) goto L_0x000e
            goto L_0x0042
        L_0x000e:
            java.lang.String r1 = "WirelessChargingController"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0044 }
            r2.<init>()     // Catch:{ all -> 0x0044 }
            java.lang.String r3 = "addCallback "
            r2.append(r3)     // Catch:{ all -> 0x0044 }
            r2.append(r5)     // Catch:{ all -> 0x0044 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0044 }
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x0044 }
            java.util.ArrayList<com.android.systemui.statusbar.policy.WirelessChargingController$Callback> r1 = r4.mCallbacks     // Catch:{ all -> 0x0044 }
            r1.add(r5)     // Catch:{ all -> 0x0044 }
            com.android.systemui.statusbar.policy.WirelessChargingControllerImpl$Receiver r1 = r4.mReceiver     // Catch:{ all -> 0x0044 }
            java.util.ArrayList<com.android.systemui.statusbar.policy.WirelessChargingController$Callback> r2 = r4.mCallbacks     // Catch:{ all -> 0x0044 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x0044 }
            if (r2 != 0) goto L_0x0035
            r2 = 1
            goto L_0x0036
        L_0x0035:
            r2 = 0
        L_0x0036:
            r1.setListening(r2)     // Catch:{ all -> 0x0044 }
            boolean r4 = r4.isWirelessChargingEnabled()     // Catch:{ all -> 0x0044 }
            r5.onWirelessChargingChanged(r4)     // Catch:{ all -> 0x0044 }
            monitor-exit(r0)     // Catch:{ all -> 0x0044 }
            return
        L_0x0042:
            monitor-exit(r0)     // Catch:{ all -> 0x0044 }
            return
        L_0x0044:
            r4 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0044 }
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.WirelessChargingControllerImpl.addCallback(com.android.systemui.statusbar.policy.WirelessChargingController$Callback):void");
    }

    public void removeCallback(WirelessChargingController.Callback callback) {
        if (callback != null) {
            Log.d("WirelessChargingController", "removeCallback " + callback);
            synchronized (this.mCallbacks) {
                this.mCallbacks.remove(callback);
                this.mReceiver.setListening(!this.mCallbacks.isEmpty());
            }
        }
    }

    public boolean isWirelessChargingEnabled() {
        return IWirelessSwitch.getInstance().getWirelessChargingStatus() == 0;
    }

    public void setWirelessChargingEnabled(boolean z) {
        if (!z) {
            IWirelessSwitch.getInstance().setWirelessChargingEnabled(false);
            sendUpdateStatusBroadCast(1);
        } else if (!isLowBatteryLevelOrWirelessCharged()) {
            showConfirmDialog();
        }
    }

    private void sendUpdateStatusBroadCast(int i) {
        Intent intent = new Intent("miui.intent.action.ACTION_WIRELESS_CHARGING");
        intent.addFlags(822083584);
        intent.putExtra("miui.intent.extra.WIRELESS_CHARGING", i);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void showConfirmDialog() {
        Intent intent = new Intent("miui.intent.action.ACTIVITY_WIRELESS_CHG_CONFIRM");
        intent.addFlags(268435456);
        this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
    }

    private void showWirelessChargingWarningDialog(int i) {
        Intent intent = new Intent("miui.intent.action.ACTIVITY_WIRELESS_CHG_WARNING");
        intent.addFlags(268435456);
        intent.putExtra("plugstatus", i);
        this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
    }

    private boolean isLowBatteryLevelOrWirelessCharged() {
        Intent registerReceiver = this.mContext.registerReceiver((BroadcastReceiver) null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (registerReceiver == null) {
            return false;
        }
        int intExtra = registerReceiver.getIntExtra("plugged", -1);
        int intExtra2 = registerReceiver.getIntExtra(MiStat.Param.LEVEL, -1);
        Log.d("WirelessChargingController", "current pulg status: " + intExtra + "  current battery level: " + intExtra2);
        if (4 != intExtra && (intExtra > 0 || intExtra2 >= 30)) {
            return false;
        }
        showWirelessChargingWarningDialog(intExtra);
        return true;
    }

    /* access modifiers changed from: private */
    public void fireCallback(boolean z) {
        synchronized (this.mCallbacks) {
            Iterator<WirelessChargingController.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onWirelessChargingChanged(z);
            }
        }
    }

    private final class Receiver extends BroadcastReceiver {
        private boolean mRegistered;

        private Receiver() {
        }

        public void setListening(boolean z) {
            if (z && !this.mRegistered) {
                Log.d("WirelessChargingController", "Registering receiver");
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("miui.intent.action.ACTION_WIRELESS_CHARGING");
                WirelessChargingControllerImpl.this.mContext.registerReceiver(this, intentFilter);
                this.mRegistered = true;
            } else if (!z && this.mRegistered) {
                Log.d("WirelessChargingController", "Unregistering receiver");
                WirelessChargingControllerImpl.this.mContext.unregisterReceiver(this);
                this.mRegistered = false;
            }
        }

        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            int intExtra = intent.getIntExtra("miui.intent.extra.WIRELESS_CHARGING", 1);
            Log.d("WirelessChargingController", "onReceive " + intExtra);
            WirelessChargingControllerImpl wirelessChargingControllerImpl = WirelessChargingControllerImpl.this;
            if (intExtra != 0) {
                z = false;
            }
            wirelessChargingControllerImpl.fireCallback(z);
        }
    }
}
