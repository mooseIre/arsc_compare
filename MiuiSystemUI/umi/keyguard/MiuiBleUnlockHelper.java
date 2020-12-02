package com.android.keyguard;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.StatusBarManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.security.MiuiLockPatternUtils;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.widget.Toast;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import java.util.List;
import miui.bluetooth.ble.MiBleProfile;
import miui.bluetooth.ble.MiBleUnlockProfile;
import miui.telephony.SubscriptionManager;

public class MiuiBleUnlockHelper {
    private MiBleUnlockProfile.OnUnlockStateChangeListener mBleListener = new MiBleUnlockProfile.OnUnlockStateChangeListener() {
        public void onUnlocked(byte b) {
            Slog.i("MiuiBleUnlockHelper", "mBleListener state: " + b);
            if (MiuiBleUnlockHelper.this.mUpdateMonitor.userNeedsStrongAuth() || MiuiBleUnlockHelper.this.mUpdateMonitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser())) {
                Log.i("MiuiBleUnlockHelper", "mBleListener cancel");
                return;
            }
            if (b == 2) {
                MiuiBleUnlockHelper.this.setBLEUnlockState(BLEUnlockState.SUCCEED);
            } else {
                AnalyticsHelper.getInstance(MiuiBleUnlockHelper.this.mContext).recordUnlockWay("band", false);
                MiuiBleUnlockHelper.this.setBLEUnlockState(BLEUnlockState.FAILED);
            }
            MiuiBleUnlockHelper.this.setBLEStatusBarIcon(b);
        }
    };
    /* access modifiers changed from: private */
    public boolean mBouncerVisible = false;
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
    /* access modifiers changed from: private */
    public boolean mHasUnlockByBle = false;
    /* access modifiers changed from: private */
    public boolean mIsMXtelcelActivity;
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
    public final TaskStackChangeListener mTaskStackListener = new TaskStackChangeListener() {
        public void onTaskStackChanged() {
            try {
                List tasks = ActivityTaskManager.getService().getTasks(1);
                if (tasks.isEmpty() || !MiuiBleUnlockHelper.this.isMXtelcelActivity((ActivityManager.RunningTaskInfo) tasks.get(0))) {
                    boolean unused = MiuiBleUnlockHelper.this.mIsMXtelcelActivity = false;
                } else {
                    boolean unused2 = MiuiBleUnlockHelper.this.mIsMXtelcelActivity = true;
                }
            } catch (RemoteException e) {
                Log.e("MiuiBleUnlockHelper", "am.getTasks fail " + e.getStackTrace());
                e.printStackTrace();
            }
        }
    };
    /* access modifiers changed from: private */
    public MiBleUnlockProfile mUnlockProfile;
    /* access modifiers changed from: private */
    public final KeyguardUpdateMonitor mUpdateMonitor;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onKeyguardBouncerChanged(boolean z) {
            boolean unused = MiuiBleUnlockHelper.this.mBouncerVisible = z;
            boolean unused2 = MiuiBleUnlockHelper.this.mHasUnlockByBle = false;
            if (z && MiuiBleUnlockHelper.this.isAllowUnlockForBle()) {
                MiuiBleUnlockHelper.this.unlockByBle();
            }
        }
    };
    private UserSwitcherController mUserContextController;
    /* access modifiers changed from: private */
    public KeyguardViewMediator mViewMediator;
    protected final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() {
        public void onStartedWakingUp() {
            MiuiBleUnlockHelper.this.verifyBLEDeviceRssi();
        }

        public void onFinishedGoingToSleep() {
            MiuiBleUnlockHelper.this.unregisterUnlockListener();
            ActivityManagerWrapper.getInstance().unregisterTaskStackListener(MiuiBleUnlockHelper.this.mTaskStackListener);
        }
    };

    public enum BLEUnlockState {
        FAILED,
        SUCCEED,
        PROCESSING
    }

    public MiuiBleUnlockHelper(Context context, KeyguardViewMediator keyguardViewMediator) {
        this.mContext = context;
        this.mViewMediator = keyguardViewMediator;
        this.mLockPatternUtils = new MiuiLockPatternUtils(context);
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mStatusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
        this.mUserContextController = (UserSwitcherController) Dependency.get(UserSwitcherController.class);
        this.mUpdateMonitor.registerCallback(this.mUpdateMonitorCallback);
        ((WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class)).addObserver(this.mWakefulnessObserver);
        registerBleUnlockReceiver();
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
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
        if (!this.mViewMediator.isShowing() || ((this.mViewMediator.isOccluded() && !this.mIsMXtelcelActivity) || this.mViewMediator.isHiding() || !this.mUserContextController.isOwnerUser() || !this.mUpdateMonitor.isDeviceInteractive() || !this.mUpdateMonitor.getStrongAuthTracker().hasOwnerUserAuthenticatedSinceBoot() || !isUnlockWithBlePossible() || this.mUpdateMonitor.userNeedsStrongAuth() || this.mUpdateMonitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser()))) {
            Log.d("MiuiBleUnlockHelper", "verifyBLEDeviceRssi  isShowing = " + this.mViewMediator.isShowing() + " isOccluded = " + this.mViewMediator.isOccluded() + " isMXtelcel = " + this.mIsMXtelcelActivity + " isHiding = " + this.mViewMediator.isHiding() + " isDeviceInteractive = " + this.mUpdateMonitor.isDeviceInteractive());
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
        if (this.mUserContextController.isOwnerUser() && this.mUpdateMonitor.getStrongAuthTracker().hasOwnerUserAuthenticatedSinceBoot() && isUnlockWithBlePossible()) {
            setBLEUnlockState(BLEUnlockState.FAILED);
            try {
                if (this.mUnlockProfile != null) {
                    this.mUnlockProfile.disconnect();
                }
            } catch (Exception e) {
                Log.e("MiuiBleUnlockHelper", e.getMessage(), e);
            }
            MiBleUnlockProfile miBleUnlockProfile = new MiBleUnlockProfile(this.mContext, this.mLockPatternUtils.getBluetoothAddressToUnlock(), this.mStateChangeCallback);
            this.mUnlockProfile = miBleUnlockProfile;
            miBleUnlockProfile.connect();
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
            ActivityManagerWrapper.getInstance().unregisterTaskStackListener(this.mTaskStackListener);
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
        setBLEUnlockState(BLEUnlockState.FAILED);
    }

    /* access modifiers changed from: private */
    public void setBLEStatusBarIcon(int i) {
        int i2;
        if (i == 0) {
            i2 = C0013R$drawable.ble_unlock_statusbar_icon_unverified;
        } else if (i == 2) {
            i2 = C0013R$drawable.ble_unlock_statusbar_icon_verified_near;
        } else {
            i2 = C0013R$drawable.ble_unlock_statusbar_icon_verified_far;
        }
        this.mStatusBarManager.setIcon("ble_unlock_mode", i2, 0, (String) null);
    }

    /* access modifiers changed from: private */
    public boolean isMXtelcelActivity(ActivityManager.RunningTaskInfo runningTaskInfo) {
        return "com.celltick.lockscreen".equals(runningTaskInfo.topActivity.getPackageName());
    }

    /* access modifiers changed from: private */
    public void setBLEUnlockState(BLEUnlockState bLEUnlockState) {
        ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setBLEUnlockState(bLEUnlockState);
        if (bLEUnlockState == BLEUnlockState.SUCCEED) {
            this.mUpdateMonitor.putUserBleAuthenticated(this.mUserContextController.getCurrentUserId(), true);
            if (isAllowUnlockForBle()) {
                unlockByBle();
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isAllowUnlockForBle() {
        if (!((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isBleUnlockSuccess() || !this.mBouncerVisible || this.mHasUnlockByBle || !this.mUpdateMonitor.isUnlockingWithBiometricAllowed(false) || SubscriptionManager.isValidSubscriptionId(this.mUpdateMonitor.getNextSubIdForState(2)) || SubscriptionManager.isValidSubscriptionId(this.mUpdateMonitor.getNextSubIdForState(3))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void unlockByBle() {
        this.mViewMediator.keyguardDone();
        boolean z = true;
        this.mHasUnlockByBle = true;
        int currentUserId = this.mUserContextController.getCurrentUserId();
        if (!this.mUpdateMonitor.getUserBleAuthenticated(currentUserId) || this.mUpdateMonitor.getUserUnlockedWithBiometric(currentUserId)) {
            z = false;
        }
        if (z) {
            handleBleUnlockSucceed();
        }
    }

    private void handleBleUnlockSucceed() {
        Toast.makeText(this.mContext, C0021R$string.miui_keyguard_ble_unlock_succeed_msg, 0).show();
    }
}
