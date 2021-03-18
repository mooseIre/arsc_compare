package com.android.keyguard;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.os.UserHandle;
import android.security.MiuiLockPatternUtils;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import java.util.List;
import miui.bluetooth.ble.MiBleProfile;
import miui.bluetooth.ble.MiBleUnlockProfile;
import miui.telephony.SubscriptionManager;

public class MiuiBleUnlockHelper {
    private MiBleUnlockProfile.OnUnlockStateChangeListener mBleListener = new MiBleUnlockProfile.OnUnlockStateChangeListener() {
        /* class com.android.keyguard.MiuiBleUnlockHelper.AnonymousClass5 */

        public void onUnlocked(byte b) {
            Slog.i("MiuiBleUnlockHelper", "mBleListener state: " + ((int) b));
            if (MiuiBleUnlockHelper.this.mUpdateMonitor.userNeedsStrongAuth() || MiuiBleUnlockHelper.this.mUpdateMonitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser())) {
                Log.i("MiuiBleUnlockHelper", "mBleListener cancel");
                return;
            }
            if (b == 2) {
                MiuiBleUnlockHelper.this.setBLEUnlockState(BLEUnlockState.SUCCEED);
            } else {
                MiuiBleUnlockHelper.this.setBLEUnlockState(BLEUnlockState.FAILED);
            }
            ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setKeyguardUnlockWay("band", b == 2);
            MiuiBleUnlockHelper.this.setBLEStatusBarIcon(b);
        }
    };
    private boolean mBouncerVisible = false;
    private Context mContext;
    private final BroadcastReceiver mGlobalBluetoothBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.keyguard.MiuiBleUnlockHelper.AnonymousClass4 */

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
    private boolean mHasUnlockByBle = false;
    private boolean mIsMXtelcelActivity;
    private MiuiLockPatternUtils mLockPatternUtils;
    private MiBleProfile.IProfileStateChangeCallback mStateChangeCallback = new MiBleProfile.IProfileStateChangeCallback() {
        /* class com.android.keyguard.MiuiBleUnlockHelper.AnonymousClass3 */

        public void onState(int i) {
            Log.d("MiuiBleUnlockHelper", "Ble state change onState: " + i);
            if (i != 4) {
                MiuiBleUnlockHelper.this.unregisterUnlockListener();
            } else if (MiuiBleUnlockHelper.this.mUnlockProfile != null && MiuiBleUnlockHelper.this.mUpdateMonitor.isDeviceInteractive() && MiuiBleUnlockHelper.this.mViewMediator.isShowingAndNotOccluded()) {
                MiuiBleUnlockHelper.this.registerUnlockListener();
            }
        }
    };
    private final TaskStackChangeListener mTaskStackListener = new TaskStackChangeListener() {
        /* class com.android.keyguard.MiuiBleUnlockHelper.AnonymousClass6 */

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskStackChanged() {
            try {
                List tasks = ActivityTaskManager.getService().getTasks(1);
                if (tasks.isEmpty() || !MiuiBleUnlockHelper.this.isMXtelcelActivity((ActivityManager.RunningTaskInfo) tasks.get(0))) {
                    MiuiBleUnlockHelper.this.mIsMXtelcelActivity = false;
                } else {
                    MiuiBleUnlockHelper.this.mIsMXtelcelActivity = true;
                }
            } catch (RemoteException e) {
                Log.e("MiuiBleUnlockHelper", "am.getTasks fail " + e.getStackTrace());
                e.printStackTrace();
            }
        }
    };
    private MiBleUnlockProfile mUnlockProfile;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        /* class com.android.keyguard.MiuiBleUnlockHelper.AnonymousClass2 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardBouncerChanged(boolean z) {
            MiuiBleUnlockHelper.this.mBouncerVisible = z;
            MiuiBleUnlockHelper.this.mHasUnlockByBle = false;
            if (z && MiuiBleUnlockHelper.this.isAllowUnlockForBle()) {
                MiuiBleUnlockHelper.this.unlockByBle();
            }
        }
    };
    private UserSwitcherController mUserContextController;
    private KeyguardViewMediator mViewMediator;
    protected final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() {
        /* class com.android.keyguard.MiuiBleUnlockHelper.AnonymousClass1 */

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedWakingUp() {
            MiuiBleUnlockHelper.this.verifyBLEDeviceRssi();
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
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
        this.mContext.registerReceiverAsUser(this.mGlobalBluetoothBroadcastReceiver, UserHandle.ALL, intentFilter, null, null);
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
    /* access modifiers changed from: public */
    private void connectBLEDevice() {
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
    /* access modifiers changed from: public */
    private void disconnectBleDeviceIfNecessary() {
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
    /* access modifiers changed from: public */
    private void registerUnlockListener() {
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
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).setIconVisibility("ble_unlock_mode", false);
            ((MiuiDripLeftStatusBarIconControllerImpl) Dependency.get(MiuiDripLeftStatusBarIconControllerImpl.class)).setIconVisibility("ble_unlock_mode", false);
        }
        setBLEUnlockState(BLEUnlockState.FAILED);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setBLEStatusBarIcon(int i) {
        int i2;
        if (i == 0) {
            i2 = C0013R$drawable.ble_unlock_statusbar_icon_unverified;
        } else if (i == 2) {
            i2 = C0013R$drawable.ble_unlock_statusbar_icon_verified_near;
        } else {
            i2 = C0013R$drawable.ble_unlock_statusbar_icon_verified_far;
        }
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).setIcon("ble_unlock_mode", i2, null);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).setIconVisibility("ble_unlock_mode", true);
        ((MiuiDripLeftStatusBarIconControllerImpl) Dependency.get(MiuiDripLeftStatusBarIconControllerImpl.class)).setIcon("ble_unlock_mode", i2, null);
        ((MiuiDripLeftStatusBarIconControllerImpl) Dependency.get(MiuiDripLeftStatusBarIconControllerImpl.class)).setIconVisibility("ble_unlock_mode", true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMXtelcelActivity(ActivityManager.RunningTaskInfo runningTaskInfo) {
        return "com.celltick.lockscreen".equals(runningTaskInfo.topActivity.getPackageName());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setBLEUnlockState(BLEUnlockState bLEUnlockState) {
        ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setBLEUnlockState(bLEUnlockState);
        if (bLEUnlockState == BLEUnlockState.SUCCEED) {
            this.mUpdateMonitor.putUserBleAuthenticated(this.mUserContextController.getCurrentUserId(), true);
            if (isAllowUnlockForBle()) {
                unlockByBle();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAllowUnlockForBle() {
        if (!((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isBleUnlockSuccess() || !this.mBouncerVisible || this.mHasUnlockByBle || !this.mUpdateMonitor.isUnlockingWithBiometricAllowed(false) || SubscriptionManager.isValidSubscriptionId(this.mUpdateMonitor.getNextSubIdForState(2)) || SubscriptionManager.isValidSubscriptionId(this.mUpdateMonitor.getNextSubIdForState(3))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unlockByBle() {
        this.mViewMediator.keyguardDone();
        this.mHasUnlockByBle = true;
        MiuiKeyguardUtils.handleBleUnlockSucceed(this.mContext);
    }
}
