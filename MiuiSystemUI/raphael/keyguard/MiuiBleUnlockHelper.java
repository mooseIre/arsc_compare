package com.android.keyguard;

import android.app.StatusBarManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import android.security.MiuiLockPatternUtils;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.systemui.Util;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.UnlockMethodCache;
import miui.bluetooth.ble.MiBleProfile;
import miui.bluetooth.ble.MiBleUnlockProfile;

public class MiuiBleUnlockHelper {
    private MiBleUnlockProfile.OnUnlockStateChangeListener mBleListener = new MiBleUnlockProfile.OnUnlockStateChangeListener() {
        public void onUnlocked(byte b) {
            Slog.i("MiuiBleUnlockHelper", "mBleListener state: " + b);
            if (MiuiBleUnlockHelper.this.mUpdateMonitor.mustPasswordUnlockDevice() || MiuiBleUnlockHelper.this.mUpdateMonitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser())) {
                Log.i("MiuiBleUnlockHelper", "mBleListener cancel");
                return;
            }
            if (b == 2) {
                MiuiBleUnlockHelper.this.mUpdateMonitor.setBLEUnlockState(BLEUnlockState.SUCCEED);
            } else if (b == 1) {
                AnalyticsHelper.getInstance(MiuiBleUnlockHelper.this.mContext).recordUnlockWay("band", false);
                MiuiBleUnlockHelper.this.mUpdateMonitor.setBLEUnlockState(BLEUnlockState.FAILED);
            } else {
                AnalyticsHelper.getInstance(MiuiBleUnlockHelper.this.mContext).recordUnlockWay("band", false);
                MiuiBleUnlockHelper.this.mUpdateMonitor.setBLEUnlockState(BLEUnlockState.FAILED);
            }
            MiuiBleUnlockHelper.this.setBLEStatusBarIcon(b);
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    private final BroadcastReceiver mGlobalBluetoothBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d("MiuiBleUnlockHelper", "ble action name: " + intent.getAction());
            if ("com.miui.keyguard.bluetoothdeviceunlock.disable".equals(intent.getAction()) || "com.xiaomi.hm.health.ACTION_DEVICE_UNBIND_APPLICATION".equals(intent.getAction())) {
                MiuiBleUnlockHelper.this.disconnectBleDeviceIfNecessary();
            } else if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 10);
                if (intExtra == 12) {
                    MiuiBleUnlockHelper.this.connectBLEDevice();
                } else if (intExtra == 13 || intExtra == 10) {
                    MiuiBleUnlockHelper.this.disconnectBleDeviceIfNecessary();
                }
            } else {
                MiuiBleUnlockHelper.this.connectBLEDevice();
            }
        }
    };
    private MiuiLockPatternUtils mLockPatternUtils;
    private MiBleProfile.IProfileStateChangeCallback mStateChangeCallback = new MiBleProfile.IProfileStateChangeCallback() {
        public void onState(int i) {
            Log.d("MiuiBleUnlockHelper", "Ble state change onState: " + i);
            if (i != 4) {
                MiuiBleUnlockHelper.this.unregisterUnlockListener();
            } else if (MiuiBleUnlockHelper.this.mUnlockProfile != null && MiuiBleUnlockHelper.this.mUpdateMonitor.isDeviceInteractive() && MiuiBleUnlockHelper.this.mViewMediator.isShowingAndNotOccluded()) {
                MiuiBleUnlockHelper.this.registerUnlockListener();
            }
        }
    };
    private StatusBarManager mStatusBarManager;
    /* access modifiers changed from: private */
    public MiBleUnlockProfile mUnlockProfile;
    /* access modifiers changed from: private */
    public final KeyguardUpdateMonitor mUpdateMonitor;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onStartedWakingUp() {
            MiuiBleUnlockHelper.this.verifyBLEDeviceRssi();
        }

        public void onFinishedGoingToSleep(int i) {
            MiuiBleUnlockHelper.this.unregisterUnlockListener();
        }
    };
    /* access modifiers changed from: private */
    public KeyguardViewMediator mViewMediator;

    public enum BLEUnlockState {
        FAILED,
        SUCCEED,
        PROCESSING
    }

    public MiuiBleUnlockHelper(Context context, KeyguardViewMediator keyguardViewMediator) {
        this.mContext = context;
        this.mViewMediator = keyguardViewMediator;
        this.mLockPatternUtils = new MiuiLockPatternUtils(context);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mStatusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mUpdateMonitorCallback);
        registerBleUnlockReceiver();
    }

    private void registerBleUnlockReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.miui.keyguard.bluetoothdeviceunlock");
        intentFilter.addAction("com.miui.keyguard.bluetoothdeviceunlock.disable");
        intentFilter.addAction("com.xiaomi.hm.health.ACTION_DEVICE_UNBIND_APPLICATION");
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        this.mContext.registerReceiverAsUser(this.mGlobalBluetoothBroadcastReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
    }

    public void verifyBLEDeviceRssi() {
        if (!this.mViewMediator.isShowing() || ((this.mViewMediator.isOccluded() && !"com.celltick.lockscreen".equals(Util.getTopActivityPkg(this.mContext))) || this.mViewMediator.isHiding() || !KeyguardUpdateMonitor.isOwnerUser() || !this.mUpdateMonitor.isDeviceInteractive() || !this.mUpdateMonitor.getStrongAuthTracker().hasOwnerUserAuthenticatedSinceBoot() || !UnlockMethodCache.getInstance(this.mContext).isMethodSecure() || !isUnlockWithBlePossible() || this.mUpdateMonitor.mustPasswordUnlockDevice() || this.mUpdateMonitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser()))) {
            Log.d("MiuiBleUnlockHelper", "verifyBLEDeviceRssi  isShowing = " + this.mViewMediator.isShowing() + " isOccluded = " + this.mViewMediator.isOccluded() + " isMXtelcel = " + "com.celltick.lockscreen".equals(Util.getTopActivityPkg(this.mContext)) + " isHiding = " + this.mViewMediator.isHiding() + " isDeviceInteractive = " + this.mUpdateMonitor.isDeviceInteractive());
        } else if (this.mUnlockProfile != null) {
            registerUnlockListener();
        } else {
            Log.d("MiuiBleUnlockHelper", "connectBLEDevice...");
            connectBLEDevice();
        }
    }

    public boolean isUnlockWithBlePossible() {
        return this.mLockPatternUtils.getBluetoothUnlockEnabled() && !TextUtils.isEmpty(this.mLockPatternUtils.getBluetoothAddressToUnlock()) && BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    /* access modifiers changed from: private */
    public void connectBLEDevice() {
        if (UnlockMethodCache.getInstance(this.mContext).isMethodSecure() && KeyguardUpdateMonitor.isOwnerUser() && this.mUpdateMonitor.getStrongAuthTracker().hasOwnerUserAuthenticatedSinceBoot() && isUnlockWithBlePossible()) {
            this.mUpdateMonitor.setBLEUnlockState(BLEUnlockState.FAILED);
            try {
                if (this.mUnlockProfile != null) {
                    this.mUnlockProfile.disconnect();
                }
            } catch (Exception e) {
                Log.e("MiuiBleUnlockHelper", e.getMessage(), e);
            }
            this.mUnlockProfile = new MiBleUnlockProfile(this.mContext, this.mLockPatternUtils.getBluetoothAddressToUnlock(), this.mStateChangeCallback);
            this.mUnlockProfile.connect();
        }
    }

    /* access modifiers changed from: private */
    public void disconnectBleDeviceIfNecessary() {
        try {
            if (this.mUnlockProfile != null) {
                unregisterUnlockListener();
                this.mUnlockProfile.disconnect();
                this.mUnlockProfile = null;
            }
        } catch (Exception e) {
            Log.e("MiuiBleUnlockHelper", e.getMessage(), e);
        }
    }

    /* access modifiers changed from: private */
    public void registerUnlockListener() {
        MiBleUnlockProfile miBleUnlockProfile = this.mUnlockProfile;
        if (miBleUnlockProfile != null) {
            miBleUnlockProfile.registerUnlockListener(this.mBleListener);
            setBLEStatusBarIcon(0);
        }
    }

    public void unregisterUnlockListener() {
        MiBleUnlockProfile miBleUnlockProfile = this.mUnlockProfile;
        if (miBleUnlockProfile != null) {
            miBleUnlockProfile.unregisterUnlockListener();
            this.mStatusBarManager.removeIcon("ble_unlock_mode");
        }
        this.mUpdateMonitor.setBLEUnlockState(BLEUnlockState.FAILED);
    }

    /* access modifiers changed from: private */
    public void setBLEStatusBarIcon(int i) {
        this.mStatusBarManager.setIcon("ble_unlock_mode", i == 0 ? R.drawable.ble_unlock_statusbar_icon_unverified : i == 2 ? R.drawable.ble_unlock_statusbar_icon_verified_near : R.drawable.ble_unlock_statusbar_icon_verified_far, 0, (String) null);
    }
}
