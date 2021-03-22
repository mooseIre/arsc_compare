package com.android.systemui.statusbar;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.hardware.biometrics.BiometricSourceType;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.widget.ViewClippingUtil;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiKeyguardFingerprintUtils$FingerprintIdentificationState;
import com.android.keyguard.MiuiKeyguardIndicationTextView;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.charge.MiuiBatteryStatus;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.injector.KeyguardIndicationInjector;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PhoneUtils;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dock.DockManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.KeyguardIndicationTextView;
import com.android.systemui.statusbar.phone.LockscreenLockIconController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.wakelock.SettableWakeLock;
import com.android.systemui.util.wakelock.WakeLock;
import com.miui.systemui.DebugConfig;
import com.miui.systemui.util.MiuiTextUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.IllegalFormatConversionException;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class KeyguardIndicationController implements StatusBarStateController.StateListener, KeyguardStateController.Callback {
    private String mAlignmentIndication;
    private final IBatteryStats mBatteryInfo;
    private int mBatteryLevel;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean mChargeUIEntering;
    private int mChargingSpeed;
    private long mChargingTimeRemaining;
    private int mChargingWattage;
    private String mComputePowerIndication;
    private final Context mContext;
    private boolean mDarkStyle;
    private final DevicePolicyManager mDevicePolicyManager;
    private KeyguardIndicationTextView mDisclosure;
    private float mDisclosureMaxAlpha;
    private final DockManager mDockManager;
    private boolean mDozing;
    private int mFingerprintAuthUserId;
    private int mFingerprintErrorMsgId;
    private MiuiKeyguardFingerprintUtils$FingerprintIdentificationState mFpiState;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class com.android.systemui.statusbar.KeyguardIndicationController.AnonymousClass5 */

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                KeyguardIndicationController.this.hideTransientIndication();
            } else if (i == 2) {
                if (KeyguardIndicationController.this.mLockIconController != null) {
                    KeyguardIndicationController.this.mLockIconController.setTransientBiometricsError(false);
                }
            } else if (i == 3) {
                KeyguardIndicationController.this.showSwipeUpToUnlock();
            } else if (i == Integer.MAX_VALUE) {
                ((KeyguardIndicationInjector) Dependency.get(KeyguardIndicationInjector.class)).handleExitArrowAndTextAnimation(KeyguardIndicationController.this.mUpArrow, KeyguardIndicationController.this.mTextView, new Animation.AnimationListener() {
                    /* class com.android.systemui.statusbar.KeyguardIndicationController.AnonymousClass5.AnonymousClass1 */

                    public void onAnimationRepeat(Animation animation) {
                    }

                    public void onAnimationStart(Animation animation) {
                        KeyguardIndicationController.this.mUpArrowEntering = false;
                        KeyguardIndicationController.this.updateIndication(false);
                    }

                    public void onAnimationEnd(Animation animation) {
                        KeyguardIndicationController.this.updateIndication(false);
                        KeyguardIndicationController.this.mTextView.setVisibility(0);
                    }
                });
            }
        }
    };
    private boolean mHideTransientMessageOnScreenOff;
    private ViewGroup mIndicationArea;
    private ColorStateList mInitialTextColorState;
    private final KeyguardStateController mKeyguardStateController;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private MiuiKeyguardFingerprintUtils$FingerprintIdentificationState mLastFpiState;
    private LockscreenLockIconController mLockIconController;
    private String mMessageToShowOnScreenOn;
    private boolean mPowerCharged;
    private boolean mPowerPluggedIn;
    private boolean mPowerPluggedInWired;
    private final Resources mResources;
    private String mRestingIndication;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final StatusBarStateController mStatusBarStateController;
    private MiuiKeyguardIndicationTextView mTextView;
    private final KeyguardUpdateMonitorCallback mTickReceiver = new KeyguardUpdateMonitorCallback() {
        /* class com.android.systemui.statusbar.KeyguardIndicationController.AnonymousClass4 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTimeChanged() {
            if (KeyguardIndicationController.this.mVisible) {
                KeyguardIndicationController.this.updateIndication(false);
            }
        }
    };
    private CharSequence mTransientIndication;
    private boolean mTransientTextIsError;
    private ImageView mUpArrow;
    private boolean mUpArrowEntering;
    private String mUpArrowIndication;
    private MiuiKeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private final UserManager mUserManager;
    private boolean mVisible;
    private final SettableWakeLock mWakeLock;
    boolean mWasPluggedIn = false;

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.KeyguardIndicationController$1  reason: invalid class name */
    public class AnonymousClass1 implements ViewClippingUtil.ClippingParameters {
    }

    private String getTrustManagedIndication() {
        return null;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
    }

    KeyguardIndicationController(Context context, WakeLock.Builder builder, KeyguardStateController keyguardStateController, StatusBarStateController statusBarStateController, KeyguardUpdateMonitor keyguardUpdateMonitor, DockManager dockManager, BroadcastDispatcher broadcastDispatcher, DevicePolicyManager devicePolicyManager, IBatteryStats iBatteryStats, UserManager userManager) {
        this.mContext = context;
        Resources resources = context.getResources();
        this.mResources = resources;
        this.mUpArrowIndication = resources.getString(C0021R$string.default_lockscreen_unlock_hint_text);
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mDevicePolicyManager = devicePolicyManager;
        this.mKeyguardStateController = keyguardStateController;
        this.mStatusBarStateController = statusBarStateController;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mDockManager = dockManager;
        dockManager.addAlignmentStateListener(new DockManager.AlignmentStateListener() {
            /* class com.android.systemui.statusbar.$$Lambda$KeyguardIndicationController$MNRKvB1L0H3Iaik26PzOwQaf05I */
        });
        builder.setTag("Doze:KeyguardIndication");
        this.mWakeLock = new SettableWakeLock(builder.build(), "KeyguardIndication");
        this.mBatteryInfo = iBatteryStats;
        this.mUserManager = userManager;
        this.mKeyguardUpdateMonitor.registerCallback(getKeyguardCallback());
        this.mKeyguardUpdateMonitor.registerCallback(this.mTickReceiver);
        this.mStatusBarStateController.addCallback(this);
        this.mKeyguardStateController.addCallback(this);
    }

    public void setIndicationArea(ViewGroup viewGroup) {
        this.mIndicationArea = viewGroup;
        MiuiKeyguardIndicationTextView miuiKeyguardIndicationTextView = (MiuiKeyguardIndicationTextView) viewGroup.findViewById(C0015R$id.keyguard_indication_text);
        this.mTextView = miuiKeyguardIndicationTextView;
        miuiKeyguardIndicationTextView.setTextColor(getTextColor());
        this.mUpArrow = (ImageView) viewGroup.getRootView().findViewById(C0015R$id.keyguard_up_arrow);
        ((KeyguardIndicationInjector) Dependency.get(KeyguardIndicationInjector.class)).setDoubleClickListener(this.mTextView);
        MiuiKeyguardIndicationTextView miuiKeyguardIndicationTextView2 = this.mTextView;
        this.mInitialTextColorState = miuiKeyguardIndicationTextView2 != null ? miuiKeyguardIndicationTextView2.getTextColors() : ColorStateList.valueOf(-1);
        KeyguardIndicationTextView keyguardIndicationTextView = (KeyguardIndicationTextView) viewGroup.findViewById(C0015R$id.keyguard_indication_enterprise_disclosure);
        this.mDisclosure = keyguardIndicationTextView;
        this.mDisclosureMaxAlpha = keyguardIndicationTextView.getAlpha();
        updateIndication(false);
        updateDisclosure();
        if (this.mBroadcastReceiver == null) {
            this.mBroadcastReceiver = new BroadcastReceiver() {
                /* class com.android.systemui.statusbar.KeyguardIndicationController.AnonymousClass2 */

                public void onReceive(Context context, Intent intent) {
                    KeyguardIndicationController.this.updateDisclosure();
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
            intentFilter.addAction("android.intent.action.USER_REMOVED");
            this.mBroadcastDispatcher.registerReceiver(this.mBroadcastReceiver, intentFilter);
        }
    }

    public void setLockIconController(LockscreenLockIconController lockscreenLockIconController) {
        this.mLockIconController = lockscreenLockIconController;
    }

    /* access modifiers changed from: protected */
    public KeyguardUpdateMonitorCallback getKeyguardCallback() {
        if (this.mUpdateMonitorCallback == null) {
            this.mUpdateMonitorCallback = new MiuiBaseKeyguardCallback(this, null);
        }
        return this.mUpdateMonitorCallback;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDisclosure() {
        if (((Boolean) DejankUtils.whitelistIpcs(new Supplier() {
            /* class com.android.systemui.statusbar.$$Lambda$KeyguardIndicationController$z0kELVO5O0J_Wr2PuJE1CflZShk */

            @Override // java.util.function.Supplier
            public final Object get() {
                return Boolean.valueOf(KeyguardIndicationController.this.isOrganizationOwnedDevice());
            }
        })).booleanValue()) {
            CharSequence organizationOwnedDeviceOrganizationName = getOrganizationOwnedDeviceOrganizationName();
            if (organizationOwnedDeviceOrganizationName != null) {
                this.mDisclosure.switchIndication(this.mContext.getResources().getString(C0021R$string.do_disclosure_with_name, organizationOwnedDeviceOrganizationName));
            } else {
                this.mDisclosure.switchIndication(C0021R$string.do_disclosure_generic);
            }
            this.mDisclosure.setVisibility(0);
            return;
        }
        this.mDisclosure.setVisibility(8);
    }

    /* access modifiers changed from: private */
    public boolean isOrganizationOwnedDevice() {
        return this.mDevicePolicyManager.isDeviceManaged() || this.mDevicePolicyManager.isOrganizationOwnedDeviceWithManagedProfile();
    }

    private CharSequence getOrganizationOwnedDeviceOrganizationName() {
        if (this.mDevicePolicyManager.isDeviceManaged()) {
            return this.mDevicePolicyManager.getDeviceOwnerOrganizationName();
        }
        if (this.mDevicePolicyManager.isOrganizationOwnedDeviceWithManagedProfile()) {
            return getWorkProfileOrganizationName();
        }
        return null;
    }

    private CharSequence getWorkProfileOrganizationName() {
        int workProfileUserId = getWorkProfileUserId(UserHandle.myUserId());
        if (workProfileUserId == -10000) {
            return null;
        }
        return this.mDevicePolicyManager.getOrganizationNameForUser(workProfileUserId);
    }

    private int getWorkProfileUserId(int i) {
        for (UserInfo userInfo : this.mUserManager.getProfiles(i)) {
            if (userInfo.isManagedProfile()) {
                return userInfo.id;
            }
        }
        return -10000;
    }

    public void setVisible(boolean z) {
        this.mVisible = z;
        this.mIndicationArea.setVisibility(z ? 0 : 8);
        if (z) {
            if (!this.mHandler.hasMessages(1)) {
                hideTransientIndication();
            }
            updateIndication(false);
        } else if (!z) {
            hideTransientIndication();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public String getTrustGrantedIndication() {
        return this.mContext.getString(C0021R$string.keyguard_indication_trust_unlocked);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setPowerPluggedIn(boolean z) {
        this.mPowerPluggedIn = z;
    }

    public void hideTransientIndicationDelayed(long j) {
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(1), j);
    }

    public void showTransientIndication(int i) {
        showTransientIndication(this.mContext.getResources().getString(i));
    }

    public void showTransientIndication(CharSequence charSequence) {
        showTransientIndication(charSequence, false, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showTransientIndication(CharSequence charSequence, boolean z, boolean z2) {
        this.mTransientIndication = charSequence;
        this.mHideTransientMessageOnScreenOff = z2 && charSequence != null;
        this.mTransientTextIsError = z;
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(3);
        if (this.mDozing && !TextUtils.isEmpty(this.mTransientIndication)) {
            this.mWakeLock.setAcquired(true);
            hideTransientIndicationDelayed(5000);
        }
        updateIndication(false);
    }

    public void hideTransientIndication() {
        if (this.mTransientIndication != null) {
            this.mTransientIndication = null;
            this.mHideTransientMessageOnScreenOff = false;
            this.mHandler.removeMessages(1);
            updateIndication(false);
        }
    }

    /* access modifiers changed from: protected */
    public final void updateIndication(boolean z) {
        updateIndication(z, false);
    }

    /* access modifiers changed from: protected */
    public final void updateIndication(boolean z, boolean z2) {
        int i;
        if (TextUtils.isEmpty(this.mTransientIndication)) {
            this.mWakeLock.setAcquired(false);
        }
        if (DebugConfig.DEBUG_KEYGUARD) {
            Log.i("KeyguardIndication", "-----updateIndication: mVisible " + this.mVisible + " mDozing " + this.mDozing + " mTransientIndication " + ((Object) this.mTransientIndication) + " mPowerPluggedIn " + this.mPowerPluggedIn + " mComputePowerIndication " + this.mComputePowerIndication + " chargeSpeedChanged" + z2 + " mUpArrowIndication " + this.mUpArrowIndication + " mChargeUIEntering " + this.mChargeUIEntering + " animate: " + z);
        }
        if (this.mVisible) {
            String trustGrantedIndication = getTrustGrantedIndication();
            String trustManagedIndication = getTrustManagedIndication();
            String str = null;
            if (this.mPowerPluggedIn) {
                str = miuiComputePowerIndication();
            }
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (!TextUtils.isEmpty(this.mTransientIndication)) {
                this.mTextView.switchIndication(this.mTransientIndication);
            } else if (TextUtils.isEmpty(trustGrantedIndication) || !this.mKeyguardUpdateMonitor.getUserHasTrust(currentUser)) {
                if (!TextUtils.isEmpty(this.mAlignmentIndication)) {
                    this.mTextView.switchIndication(this.mAlignmentIndication);
                } else if (!this.mPowerPluggedIn || this.mChargeUIEntering) {
                    if (!TextUtils.isEmpty(trustManagedIndication) && this.mKeyguardUpdateMonitor.getUserTrustIsManaged(currentUser) && !this.mKeyguardUpdateMonitor.getUserHasTrust(currentUser)) {
                        this.mTextView.switchIndication(trustManagedIndication);
                    } else if (TextUtils.isEmpty(this.mUpArrowIndication) || this.mChargeUIEntering) {
                        this.mTextView.switchIndication(this.mRestingIndication);
                    } else {
                        this.mTextView.switchIndication(this.mUpArrowEntering ? "" : this.mUpArrowIndication);
                        ImageView imageView = this.mUpArrow;
                        if (imageView != null) {
                            if (this.mDarkStyle) {
                                i = C0013R$drawable.miui_default_lock_screen_up_arrow_dark;
                            } else {
                                i = C0013R$drawable.miui_default_lock_screen_up_arrow;
                            }
                            imageView.setImageResource(i);
                        }
                    }
                } else if (MiuiTextUtils.isEmpty(str) || z2) {
                    updatePowerIndication(z);
                } else {
                    this.mTextView.switchIndication(str);
                }
            } else if (str != null) {
                this.mTextView.switchIndication(this.mContext.getResources().getString(C0021R$string.keyguard_indication_trust_unlocked_plugged_in, trustGrantedIndication, str));
            } else {
                this.mTextView.switchIndication(trustGrantedIndication);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public String computePowerIndication() {
        int i;
        if (this.mPowerCharged) {
            return this.mContext.getResources().getString(C0021R$string.keyguard_charged);
        }
        boolean z = this.mChargingTimeRemaining > 0;
        if (this.mPowerPluggedInWired) {
            int i2 = this.mChargingSpeed;
            if (i2 != 0) {
                if (i2 != 2) {
                    if (z) {
                        i = C0021R$string.keyguard_indication_charging_time;
                    } else {
                        i = C0021R$string.keyguard_plugged_in;
                    }
                } else if (z) {
                    i = C0021R$string.keyguard_indication_charging_time_fast;
                } else {
                    i = C0021R$string.keyguard_plugged_in_charging_fast;
                }
            } else if (z) {
                i = C0021R$string.keyguard_indication_charging_time_slowly;
            } else {
                i = C0021R$string.keyguard_plugged_in_charging_slowly;
            }
        } else if (z) {
            i = C0021R$string.keyguard_indication_charging_time_wireless;
        } else {
            i = C0021R$string.keyguard_plugged_in_wireless;
        }
        String format = NumberFormat.getPercentInstance().format((double) (((float) this.mBatteryLevel) / 100.0f));
        if (z) {
            String formatShortElapsedTimeRoundingUpToMinutes = Formatter.formatShortElapsedTimeRoundingUpToMinutes(this.mContext, this.mChargingTimeRemaining);
            try {
                return this.mContext.getResources().getString(i, formatShortElapsedTimeRoundingUpToMinutes, format);
            } catch (IllegalFormatConversionException unused) {
                return this.mContext.getResources().getString(i, formatShortElapsedTimeRoundingUpToMinutes);
            }
        } else {
            try {
                return this.mContext.getResources().getString(i, format);
            } catch (IllegalFormatConversionException unused2) {
                return this.mContext.getResources().getString(i);
            }
        }
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showSwipeUpToUnlock() {
        if (!this.mDozing && this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
            this.mStatusBarKeyguardViewManager.showBouncerMessage(this.mContext.getString(C0021R$string.face_unlock_fail_retry), this.mResources.getColor(C0011R$color.secure_keyguard_bouncer_message_content_text_color));
        }
    }

    public void setDozing(boolean z) {
        if (this.mDozing != z) {
            this.mDozing = z;
            if (!this.mHideTransientMessageOnScreenOff || !z) {
                updateIndication(false);
            } else {
                hideTransientIndication();
            }
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardIndicationController:");
        printWriter.println("  mTransientTextIsError: " + this.mTransientTextIsError);
        printWriter.println("  mInitialTextColorState: " + this.mInitialTextColorState);
        printWriter.println("  mPowerPluggedInWired: " + this.mPowerPluggedInWired);
        printWriter.println("  mPowerPluggedIn: " + this.mPowerPluggedIn);
        printWriter.println("  mPowerCharged: " + this.mPowerCharged);
        printWriter.println("  mChargingSpeed: " + this.mChargingSpeed);
        printWriter.println("  mChargingWattage: " + this.mChargingWattage);
        printWriter.println("  mMessageToShowOnScreenOn: " + this.mMessageToShowOnScreenOn);
        printWriter.println("  mDozing: " + this.mDozing);
        printWriter.println("  mBatteryLevel: " + this.mBatteryLevel);
        StringBuilder sb = new StringBuilder();
        sb.append("  mTextView.getText(): ");
        MiuiKeyguardIndicationTextView miuiKeyguardIndicationTextView = this.mTextView;
        sb.append((Object) (miuiKeyguardIndicationTextView == null ? null : miuiKeyguardIndicationTextView.getText()));
        printWriter.println(sb.toString());
        printWriter.println("  computePowerIndication(): " + computePowerIndication());
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        setDozing(z);
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozeAmountChanged(float f, float f2) {
        this.mDisclosure.setAlpha((1.0f - f) * this.mDisclosureMaxAlpha);
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onUnlockedChanged() {
        updateIndication(!this.mDozing);
    }

    /* access modifiers changed from: private */
    public class MiuiBaseKeyguardCallback extends BaseKeyguardCallback {
        private MiuiBaseKeyguardCallback() {
            super();
        }

        /* synthetic */ MiuiBaseKeyguardCallback(KeyguardIndicationController keyguardIndicationController, AnonymousClass1 r2) {
            this();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback, com.android.systemui.statusbar.KeyguardIndicationController.BaseKeyguardCallback
        public void onRefreshBatteryInfo(MiuiBatteryStatus miuiBatteryStatus) {
            super.onRefreshBatteryInfo(miuiBatteryStatus);
            if (KeyguardIndicationController.this.mPowerPluggedIn) {
                KeyguardIndicationController keyguardIndicationController = KeyguardIndicationController.this;
                if (!keyguardIndicationController.mWasPluggedIn) {
                    keyguardIndicationController.clearUpArrowAnimation();
                }
            }
            if (!KeyguardIndicationController.this.mPowerPluggedIn) {
                KeyguardIndicationController keyguardIndicationController2 = KeyguardIndicationController.this;
                if (keyguardIndicationController2.mWasPluggedIn) {
                    keyguardIndicationController2.clearPowerIndication();
                    KeyguardIndicationController keyguardIndicationController3 = KeyguardIndicationController.this;
                    keyguardIndicationController3.mUpArrowIndication = keyguardIndicationController3.mResources.getString(C0021R$string.default_lockscreen_unlock_hint_text);
                    if (KeyguardIndicationController.this.mTextView != null) {
                        KeyguardIndicationController.this.mTextView.clearAnimation();
                        KeyguardIndicationController.this.updateIndication(false);
                    }
                }
            }
        }
    }

    public void updatePowerIndication(boolean z) {
        this.mChargeUIEntering = z;
        ((KeyguardIndicationInjector) Dependency.get(KeyguardIndicationInjector.class)).updatePowerIndication(z, this.mTextView);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearPowerIndication() {
        this.mComputePowerIndication = null;
        this.mChargeUIEntering = false;
    }

    public void showMiuiPowerIndication(String str) {
        this.mComputePowerIndication = str;
        updateIndication(false);
    }

    /* access modifiers changed from: package-private */
    public String miuiComputePowerIndication() {
        return this.mComputePowerIndication;
    }

    public boolean isPowerPluggedIn() {
        return this.mPowerPluggedIn;
    }

    public int getBatteryLevel() {
        return this.mBatteryLevel;
    }

    protected class BaseKeyguardCallback extends MiuiKeyguardUpdateMonitorCallback {
        protected BaseKeyguardCallback() {
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onRefreshBatteryInfo(MiuiBatteryStatus miuiBatteryStatus) {
            int i = miuiBatteryStatus.status;
            boolean z = false;
            boolean z2 = i == 2 || i == 5;
            KeyguardIndicationController keyguardIndicationController = KeyguardIndicationController.this;
            keyguardIndicationController.mWasPluggedIn = keyguardIndicationController.mPowerPluggedIn;
            KeyguardIndicationController.this.mPowerPluggedInWired = miuiBatteryStatus.isPluggedInWired() && z2;
            KeyguardIndicationController.this.mPowerPluggedIn = miuiBatteryStatus.isPluggedIn() && z2;
            KeyguardIndicationController.this.mPowerCharged = miuiBatteryStatus.isCharged();
            KeyguardIndicationController.this.mChargingWattage = miuiBatteryStatus.maxChargingWattage;
            boolean z3 = KeyguardIndicationController.this.mChargingSpeed != miuiBatteryStatus.chargeSpeed;
            KeyguardIndicationController.this.mChargingSpeed = miuiBatteryStatus.chargeSpeed;
            KeyguardIndicationController.this.mBatteryLevel = miuiBatteryStatus.level;
            try {
                KeyguardIndicationController.this.mChargingTimeRemaining = KeyguardIndicationController.this.mPowerPluggedIn ? KeyguardIndicationController.this.mBatteryInfo.computeChargeTimeRemaining() : -1;
            } catch (RemoteException e) {
                Log.e("KeyguardIndication", "Error calling IBatteryStats: ", e);
                KeyguardIndicationController.this.mChargingTimeRemaining = -1;
            }
            KeyguardIndicationController keyguardIndicationController2 = KeyguardIndicationController.this;
            if (!keyguardIndicationController2.mWasPluggedIn && keyguardIndicationController2.mPowerPluggedInWired) {
                z = true;
            }
            keyguardIndicationController2.updateIndication(z, z3);
            if (KeyguardIndicationController.this.mDozing) {
                KeyguardIndicationController keyguardIndicationController3 = KeyguardIndicationController.this;
                if (keyguardIndicationController3.mWasPluggedIn || !keyguardIndicationController3.mPowerPluggedIn) {
                    KeyguardIndicationController keyguardIndicationController4 = KeyguardIndicationController.this;
                    if (keyguardIndicationController4.mWasPluggedIn && !keyguardIndicationController4.mPowerPluggedIn) {
                        KeyguardIndicationController.this.hideTransientIndication();
                        return;
                    }
                    return;
                }
                KeyguardIndicationController keyguardIndicationController5 = KeyguardIndicationController.this;
                keyguardIndicationController5.showTransientIndication(keyguardIndicationController5.miuiComputePowerIndication());
                KeyguardIndicationController.this.hideTransientIndicationDelayed(5000);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricHelp(int i, String str, BiometricSourceType biometricSourceType) {
            if (KeyguardIndicationController.this.mKeyguardUpdateMonitor.isUnlockingWithBiometricAllowed(true)) {
                if (biometricSourceType == BiometricSourceType.FACE) {
                    KeyguardIndicationController.this.handleFaceUnlockBouncerMessage(str);
                }
                if (biometricSourceType != BiometricSourceType.FINGERPRINT) {
                    return;
                }
                if (KeyguardIndicationController.this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                    if (!TextUtils.isEmpty(str) && !MiuiKeyguardUtils.isGxzwSensor()) {
                        KeyguardIndicationController.this.mStatusBarKeyguardViewManager.showBouncerMessage(str, KeyguardIndicationController.this.mResources.getColor(C0011R$color.secure_keyguard_bouncer_message_content_text_color));
                    }
                } else if (KeyguardIndicationController.this.mKeyguardUpdateMonitor.isDeviceInteractive() || (KeyguardIndicationController.this.mDozing && KeyguardIndicationController.this.mKeyguardUpdateMonitor.isScreenOn())) {
                    KeyguardIndicationController.this.showTransientIndication(str, false, false);
                    KeyguardIndicationController.this.hideTransientIndicationDelayed(1300);
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricError(int i, String str, BiometricSourceType biometricSourceType) {
            if (!shouldSuppressBiometricError(i, biometricSourceType, KeyguardIndicationController.this.mKeyguardUpdateMonitor)) {
                if (biometricSourceType == BiometricSourceType.FACE) {
                    KeyguardIndicationController.this.handleFaceUnlockBouncerMessage("");
                    return;
                }
                KeyguardIndicationController.this.mFingerprintErrorMsgId = i;
                KeyguardIndicationController.this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.ERROR;
                KeyguardIndicationController.this.handleFingerprintStateChanged();
            }
        }

        private boolean shouldSuppressBiometricError(int i, BiometricSourceType biometricSourceType, KeyguardUpdateMonitor keyguardUpdateMonitor) {
            if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                return shouldSuppressFingerprintError(i, keyguardUpdateMonitor);
            }
            if (biometricSourceType == BiometricSourceType.FACE) {
                return shouldSuppressFaceError(i, keyguardUpdateMonitor);
            }
            return false;
        }

        private boolean shouldSuppressFingerprintError(int i, KeyguardUpdateMonitor keyguardUpdateMonitor) {
            return (!keyguardUpdateMonitor.isUnlockingWithBiometricAllowed(true) && i != 9) || i == 5;
        }

        private boolean shouldSuppressFaceError(int i, KeyguardUpdateMonitor keyguardUpdateMonitor) {
            return (!keyguardUpdateMonitor.isUnlockingWithBiometricAllowed(true) && i != 9) || i == 5;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTrustAgentErrorMessage(CharSequence charSequence) {
            KeyguardIndicationController.this.showTransientIndication(charSequence, true, false);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricRunningStateChanged(boolean z, BiometricSourceType biometricSourceType) {
            if (z) {
                KeyguardIndicationController.this.hideTransientIndication();
                KeyguardIndicationController.this.mMessageToShowOnScreenOn = null;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
            super.onBiometricAuthenticated(i, biometricSourceType, z);
            if (biometricSourceType == BiometricSourceType.FACE) {
                KeyguardIndicationController.this.handleFaceUnlockBouncerMessage("");
            } else if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                KeyguardIndicationController.this.mFingerprintAuthUserId = i;
                KeyguardIndicationController.this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.SUCCEEDED;
                KeyguardIndicationController.this.handleFingerprintStateChanged();
            }
            KeyguardIndicationController.this.mHandler.sendEmptyMessage(1);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
            super.onBiometricAuthFailed(biometricSourceType);
            if (biometricSourceType == BiometricSourceType.FACE) {
                KeyguardIndicationController.this.handleFaceUnlockBouncerMessage("");
            } else if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                KeyguardIndicationController.this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.FAILED;
                KeyguardIndicationController.this.handleFingerprintStateChanged();
            }
        }

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onFingerprintLockoutReset() {
            super.onFingerprintLockoutReset();
            if (KeyguardIndicationController.this.mFpiState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.ERROR || KeyguardIndicationController.this.mFpiState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.FAILED) {
                KeyguardIndicationController.this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.RESET;
                KeyguardIndicationController.this.handleFingerprintStateChanged();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitchComplete(int i) {
            if (KeyguardIndicationController.this.mVisible) {
                KeyguardIndicationController.this.updateIndication(false);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserUnlocked() {
            if (KeyguardIndicationController.this.mVisible) {
                KeyguardIndicationController.this.updateIndication(false);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardBouncerChanged(boolean z) {
            if (z) {
                KeyguardIndicationController.this.handleShowBouncerMessage();
            }
        }
    }

    public void clearUpArrowAnimation() {
        Log.d("KeyguardIndication", " clearUpArrowAnimation mUpArrow:" + this.mUpArrow + "  mTextView:" + this.mTextView);
        this.mHandler.removeMessages(Integer.MAX_VALUE);
        ImageView imageView = this.mUpArrow;
        if (imageView != null) {
            imageView.clearAnimation();
            this.mUpArrow.setVisibility(4);
        }
        MiuiKeyguardIndicationTextView miuiKeyguardIndicationTextView = this.mTextView;
        if (miuiKeyguardIndicationTextView != null) {
            miuiKeyguardIndicationTextView.clearAnimation();
            updateIndication(false);
        }
    }

    public void handleSingleClickEvent() {
        if (MiuiKeyguardUtils.IS_OPERATOR_CUSTOMIZATION_TEST && !this.mPowerPluggedIn) {
            updateIndication(false);
            this.mHandler.removeMessages(2147483646);
            this.mHandler.sendEmptyMessageDelayed(2147483646, 2000);
        }
    }

    public void onTouchEvent(@NotNull MotionEvent motionEvent, int i, float f, float f2) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mContext);
        if (motionEvent.getAction() == 0) {
            clearUpArrowAnimation();
        } else if (motionEvent.getAction() == 1 && i == 1) {
            float scaledTouchSlop = (float) viewConfiguration.getScaledTouchSlop();
            if (Math.abs(f - motionEvent.getRawX()) < scaledTouchSlop && Math.abs(f2 - motionEvent.getRawY()) < scaledTouchSlop) {
                handleSingleClickEvent();
            }
        }
    }

    private int getTextColor() {
        int i;
        if (this.mDarkStyle) {
            if (this.mPowerPluggedIn) {
                i = C0011R$color.miui_common_unlock_screen_charge_dark_text_color;
            } else {
                i = C0011R$color.miui_common_unlock_screen_common_dark_text_color;
            }
        } else if (this.mPowerPluggedIn) {
            i = C0011R$color.miui_charge_lock_screen_unlock_hint_text_color;
        } else {
            i = C0011R$color.miui_default_lock_screen_unlock_hint_text_color;
        }
        return this.mResources.getColor(i);
    }

    public void setDarkStyle(boolean z) {
        if (this.mDarkStyle != z) {
            this.mDarkStyle = z;
            this.mInitialTextColorState = ColorStateList.valueOf(this.mTextView != null ? getTextColor() : -1);
            updateIndication(!this.mDozing);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceUnlockBouncerMessage(String str) {
        String str2;
        if (this.mKeyguardUpdateMonitor.isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser()) && this.mKeyguardUpdateMonitor.shouldListenForFingerprint() && !this.mKeyguardUpdateMonitor.isFingerprintTemporarilyLockout() && !this.mKeyguardUpdateMonitor.userNeedsStrongAuth()) {
            str2 = this.mResources.getString(C0021R$string.face_unlock_passwork_and_fingerprint);
        } else {
            str2 = this.mResources.getString(C0021R$string.input_password_hint_text);
        }
        if (!this.mKeyguardUpdateMonitor.isFaceDetectionRunning()) {
            if (((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).isFaceTemporarilyLockout()) {
                str = this.mResources.getString(C0021R$string.face_unlock_fail);
            } else if (((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).shouldShowFaceUnlockRetryMessageInBouncer()) {
                str = this.mResources.getString(C0021R$string.face_unlock_fail_retry_global);
            }
        }
        this.mStatusBarKeyguardViewManager.showBouncerMessage(str2, str, this.mResources.getColor(C0011R$color.secure_keyguard_bouncer_message_content_text_color));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintStateChanged() {
        String str;
        String string;
        if (!this.mKeyguardUpdateMonitor.isUnlockingWithBiometricAllowed(true) || ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).shouldListenForFingerprintWhenUnlocked()) {
            this.mLastFpiState = this.mFpiState;
            return;
        }
        MiuiKeyguardFingerprintUtils$FingerprintIdentificationState miuiKeyguardFingerprintUtils$FingerprintIdentificationState = this.mFpiState;
        String str2 = "";
        if (miuiKeyguardFingerprintUtils$FingerprintIdentificationState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.FAILED) {
            str2 = this.mResources.getString(C0021R$string.fingerprint_try_again_text);
            str = this.mResources.getString(C0021R$string.fingerprint_try_again_msg);
        } else if (miuiKeyguardFingerprintUtils$FingerprintIdentificationState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.ERROR) {
            str2 = this.mResources.getString(C0021R$string.fingerprint_not_identified_title);
            str = this.mResources.getString(C0021R$string.fingerprint_not_identified_msg);
        } else {
            if (miuiKeyguardFingerprintUtils$FingerprintIdentificationState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.SUCCEEDED) {
                if (this.mFingerprintAuthUserId != KeyguardUpdateMonitor.getCurrentUser()) {
                    if (MiuiKeyguardUtils.isGreenKidActive(this.mContext)) {
                        string = this.mResources.getString(C0021R$string.input_password_after_boot_msg_can_not_switch_when_greenkid_active);
                    } else if (PhoneUtils.isInCall(this.mContext)) {
                        string = this.mResources.getString(C0021R$string.input_password_after_boot_msg_can_not_switch_when_calling);
                    } else if (MiuiKeyguardUtils.isSuperPowerActive(this.mContext)) {
                        string = this.mResources.getString(C0021R$string.input_password_after_boot_msg_can_not_switch_when_superpower_active);
                    } else if (!this.mKeyguardUpdateMonitor.getStrongAuthTracker().hasUserAuthenticatedSinceBoot(this.mFingerprintAuthUserId)) {
                        str2 = this.mResources.getString(C0021R$string.fingerprint_enter_second_psw_title);
                        str = this.mResources.getString(C0021R$string.fingerprint_enter_second_psw_msg);
                    }
                    str2 = string;
                    str = str2;
                }
            } else if (this.mLastFpiState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.ERROR && miuiKeyguardFingerprintUtils$FingerprintIdentificationState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.RESET) {
                str2 = this.mResources.getString(C0021R$string.fingerprint_not_identified_title);
                str = this.mResources.getString(C0021R$string.fingerprint_again_identified_msg);
            }
            str = str2;
        }
        getTextColor();
        this.mStatusBarKeyguardViewManager.showBouncerMessage(str2, str, this.mResources.getColor(C0011R$color.secure_keyguard_bouncer_message_content_text_color));
        MiuiKeyguardFingerprintUtils$FingerprintIdentificationState miuiKeyguardFingerprintUtils$FingerprintIdentificationState2 = this.mFpiState;
        MiuiKeyguardFingerprintUtils$FingerprintIdentificationState miuiKeyguardFingerprintUtils$FingerprintIdentificationState3 = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.ERROR;
        if (miuiKeyguardFingerprintUtils$FingerprintIdentificationState2 == miuiKeyguardFingerprintUtils$FingerprintIdentificationState3 && this.mLastFpiState != miuiKeyguardFingerprintUtils$FingerprintIdentificationState3 && this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
            this.mStatusBarKeyguardViewManager.applyHintAnimation(500);
        }
        if (this.mFpiState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.FAILED && MiuiKeyguardUtils.isGxzwSensor() && this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
            this.mStatusBarKeyguardViewManager.applyHintAnimation(500);
        }
        if (this.mFpiState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.FAILED && this.mKeyguardUpdateMonitor.isDeviceInteractive() && !MiuiKeyguardUtils.isGxzwSensor()) {
            this.mHandler.removeMessages(1);
            showTransientIndication(this.mContext.getString(C0021R$string.fingerprint_try_again_text), false, false);
            hideTransientIndicationDelayed(5000);
        }
        this.mMessageToShowOnScreenOn = str2;
        this.mLastFpiState = this.mFpiState;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleShowBouncerMessage() {
        String str;
        String str2;
        if (this.mKeyguardUpdateMonitor.isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser()) && this.mKeyguardUpdateMonitor.shouldListenForFingerprint() && !this.mKeyguardUpdateMonitor.isFingerprintTemporarilyLockout() && !this.mKeyguardUpdateMonitor.userNeedsStrongAuth()) {
            str = this.mResources.getString(C0021R$string.face_unlock_passwork_and_fingerprint);
        } else {
            str = this.mResources.getString(C0021R$string.input_password_hint_text);
        }
        if (this.mKeyguardUpdateMonitor.isFingerprintTemporarilyLockout()) {
            str2 = this.mResources.getString(C0021R$string.fingerprint_not_identified_msg);
        } else {
            if (!this.mKeyguardUpdateMonitor.isFaceDetectionRunning()) {
                if (((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).isFaceTemporarilyLockout()) {
                    str2 = this.mResources.getString(C0021R$string.face_unlock_fail);
                } else if (((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).shouldShowFaceUnlockRetryMessageInBouncer()) {
                    str2 = this.mResources.getString(C0021R$string.face_unlock_fail_retry_global);
                }
            }
            str2 = "";
        }
        this.mStatusBarKeyguardViewManager.showBouncerMessage(str, str2, this.mResources.getColor(C0011R$color.secure_keyguard_bouncer_message_content_text_color));
    }

    public void onStartedWakingUp() {
        if (DebugConfig.DEBUG_KEYGUARD) {
            Log.d("KeyguardIndication", "--------onStartedWakingUp keyguard Visible:" + this.mVisible + " indication_visible:" + this.mTextView.getVisibility() + " mPowerPluggedIn:" + this.mPowerPluggedIn + " isUserUnlocked:" + this.mKeyguardUpdateMonitor.isUserUnlocked(KeyguardUpdateMonitor.getCurrentUser()));
        }
        String str = this.mMessageToShowOnScreenOn;
        if (str != null) {
            showTransientIndication(str);
            hideTransientIndicationDelayed(5000);
            this.mMessageToShowOnScreenOn = null;
        }
        if (!this.mVisible) {
            this.mChargeUIEntering = false;
            this.mUpArrowEntering = false;
            updateIndication(false);
        } else if (this.mPowerPluggedIn) {
            this.mUpArrowIndication = null;
            updatePowerIndication(false);
        } else {
            this.mUpArrowIndication = this.mResources.getString(C0021R$string.default_lockscreen_unlock_hint_text);
            this.mUpArrowEntering = true;
            this.mHandler.postDelayed(new Runnable() {
                /* class com.android.systemui.statusbar.$$Lambda$KeyguardIndicationController$6cD5tA_RZkYFgVYHndgGL5szGwc */

                public final void run() {
                    KeyguardIndicationController.this.lambda$onStartedWakingUp$2$KeyguardIndicationController();
                }
            }, 200);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onStartedWakingUp$2 */
    public /* synthetic */ void lambda$onStartedWakingUp$2$KeyguardIndicationController() {
        ((KeyguardIndicationInjector) Dependency.get(KeyguardIndicationInjector.class)).handleEnterArrowAnimation(this.mUpArrow, this.mHandler);
    }

    public void onFinishedGoingToSleep() {
        clearPowerIndication();
        clearUpArrowAnimation();
        if (this.mUpArrowIndication != null) {
            this.mUpArrowIndication = null;
            updateIndication(false);
        }
        hideTransientIndication();
    }
}
