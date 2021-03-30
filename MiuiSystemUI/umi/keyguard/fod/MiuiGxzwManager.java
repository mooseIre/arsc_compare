package com.android.keyguard.fod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.biometrics.BiometricSourceType;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import android.widget.Toast;
import com.android.internal.util.DumpUtils;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.CommandQueue;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

public class MiuiGxzwManager extends Binder implements CommandQueue.Callbacks, Dumpable {
    private static volatile MiuiGxzwManager sService;
    private int mAuthFingerprintId = 0;
    private boolean mBouncer = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.keyguard.fod.MiuiGxzwManager.AnonymousClass5 */

        public void onReceive(Context context, Intent intent) {
            boolean z = false;
            if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
                MiuiGxzwManager.this.dismissGxzwView();
                MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(false);
                MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(false);
            } else if ("miui.intent.action.HANG_UP_CHANGED".equals(intent.getAction())) {
                MiuiGxzwManager.this.mMiuiGxzwOverlayView.onHandUpChange(intent.getBooleanExtra("hang_up_enable", false));
            } else if ("miui.action.handymode_change".equals(intent.getAction())) {
                if (intent.getIntExtra("handymode", 0) != 0) {
                    z = true;
                }
                MiuiGxzwManager.this.updateGxzwInfoInHandyMode(z);
            }
        }
    };
    private final ArrayList<WeakReference<MiuiGxzwCallback>> mCallbacks = new ArrayList<>();
    private ContentObserver mContentObserver = new ContentObserver(this.mHandler) {
        /* class com.android.keyguard.fod.MiuiGxzwManager.AnonymousClass6 */

        public void onChange(boolean z) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable(MiuiGxzwUtils.isFodAodShowEnable(MiuiGxzwManager.this.mContext)) {
                /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwManager$6$UylK6Yz83tpqm4ZwgjnGsxRUqE */
                public final /* synthetic */ boolean f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    boolean z = this.f$0;
                    MiuiGxzwUtils.setTouchMode(16, r1 ? 1 : 0);
                }
            });
        }
    };
    private Context mContext;
    private boolean mDisableFingerprintIcon = false;
    private volatile boolean mDisableLockScreenFod = false;
    private boolean mDisableLockScreenFodAnim = false;
    private boolean mDozing = false;
    private PowerManager.WakeLock mDrawWakeLock;
    public int mDrawWakeLockStatus = -1;
    private MiuiFastUnlockController.FastUnlockCallback mFastUnlockCallback = new MiuiFastUnlockController.FastUnlockCallback() {
        /* class com.android.keyguard.fod.MiuiGxzwManager.AnonymousClass4 */

        @Override // com.android.keyguard.MiuiFastUnlockController.FastUnlockCallback
        public void onStartFastUnlock() {
            if (MiuiGxzwManager.this.isUnlockByGxzw()) {
                Log.i("MiuiGxzwManager", "onStartFastUnlock");
                MiuiGxzwManager.this.mMiuiGxzwOverlayView.restoreScreenEffect();
            }
        }

        @Override // com.android.keyguard.MiuiFastUnlockController.FastUnlockCallback
        public void onFinishFastUnlock() {
            if (MiuiGxzwManager.this.isUnlockByGxzw()) {
                MiuiGxzwManager.this.mMiuiGxzwIconView.preHideIconView();
                Log.i("MiuiGxzwManager", "onFinishFastUnlock");
            }
        }
    };
    private boolean mFingerprintLockout = false;
    private int mGxzwUnlockMode = 0;
    private Handler mHandler = new Handler() {
        /* class com.android.keyguard.fod.MiuiGxzwManager.AnonymousClass1 */

        public void handleMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case 1001:
                    MiuiGxzwManager.this.setKeyguardAuthen(((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFingerprintDetectionRunning());
                    MiuiGxzwManager.this.setHealthAppAuthen(false);
                    MiuiGxzwManager miuiGxzwManager = MiuiGxzwManager.this;
                    if (message.arg1 != 1) {
                        z = false;
                    }
                    miuiGxzwManager.showGxzwView(z);
                    MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(false);
                    MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(false);
                    return;
                case 1002:
                    if (!MiuiGxzwManager.this.mHealthAppAuthen) {
                        MiuiGxzwManager.this.dismissGxzwView();
                        MiuiGxzwManager.this.setKeyguardAuthen(false);
                        MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(false);
                        MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(false);
                        return;
                    }
                    return;
                case 1003:
                    if (!MiuiGxzwManager.this.getKeyguardAuthen()) {
                        MiuiGxzwManager.this.setKeyguardAuthen(false);
                        MiuiGxzwManager.this.setHealthAppAuthen(false);
                        MiuiGxzwManager.this.dismissGxzwView();
                        return;
                    }
                    return;
                case 1004:
                    if (!MiuiGxzwManager.this.getKeyguardAuthen()) {
                        MiuiGxzwManager.this.setKeyguardAuthen(false);
                        MiuiGxzwManager.this.setHealthAppAuthen(false);
                        MiuiGxzwManager.this.dismissGxzwView();
                        MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(false);
                        MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(false);
                        return;
                    }
                    return;
                case 1005:
                    MiuiGxzwManager.this.setKeyguardAuthen(false);
                    MiuiGxzwManager.this.setHealthAppAuthen(false);
                    MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(true);
                    MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(true);
                    MiuiGxzwManager.this.showGxzwView(false);
                    return;
                case 1006:
                    int i = message.arg1;
                    if (!MiuiGxzwManager.this.mHealthAppAuthen) {
                        if ((i == 5 && !MiuiGxzwManager.this.getKeyguardAuthen()) || i == 8) {
                            MiuiGxzwManager.this.dismissGxzwView();
                            MiuiGxzwManager.this.setKeyguardAuthen(false);
                            MiuiGxzwManager.this.setHealthAppAuthen(false);
                            MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(false);
                            MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(false);
                            return;
                        }
                        return;
                    }
                    return;
                case 1007:
                    MiuiGxzwManager.this.setKeyguardAuthen(false);
                    MiuiGxzwManager.this.setHealthAppAuthen(true);
                    MiuiGxzwManager miuiGxzwManager2 = MiuiGxzwManager.this;
                    if (message.arg1 != 1) {
                        z = false;
                    }
                    miuiGxzwManager2.showGxzwView(z);
                    MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(false);
                    MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(false);
                    return;
                case 1008:
                    MiuiGxzwManager.this.setHealthAppAuthen(false);
                    if (!MiuiGxzwManager.this.getKeyguardAuthen()) {
                        MiuiGxzwManager.this.dismissGxzwView();
                        MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(false);
                        MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(false);
                        return;
                    }
                    return;
                case 1009:
                    if (!MiuiGxzwManager.this.getKeyguardAuthen()) {
                        MiuiGxzwManager.this.mMiuiGxzwIconView.setHightlightTransparen();
                        MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(false);
                        MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(false);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHandyMode;
    private boolean mHealthAppAuthen = false;
    private IntentFilter mIntentFilter;
    private boolean mKeyguardAuthen = false;
    private boolean mKeyguardShow;
    private MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
        /* class com.android.keyguard.fod.MiuiGxzwManager.AnonymousClass3 */
        private Runnable mDelayRunnable = new Runnable() {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwManager$3$lHqDm19XYAx9u5h57eXCHEzDk78 */

            public final void run() {
                MiuiGxzwManager.AnonymousClass3.this.lambda$$1$MiuiGxzwManager$3();
            }
        };

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
            if (MiuiGxzwManager.this.mLockScreenMagazinePreViewVisible != z) {
                MiuiGxzwManager.this.mLockScreenMagazinePreViewVisible = z;
                MiuiGxzwManager.this.updateGxzwState();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardBouncerChanged(boolean z) {
            super.onKeyguardBouncerChanged(z);
            Log.d("MiuiGxzwManager", "onKeyguardBouncerChanged: bouncer = " + z);
            MiuiGxzwManager.this.mBouncer = z;
            MiuiGxzwManager.this.updateGxzwState();
            if (((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFingerprintTemporarilyLockout()) {
                AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                    /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwManager$3$oRVDuUw5TdCmzkrNvYoJlnMCY */

                    public final void run() {
                        MiuiGxzwManager.AnonymousClass3.this.lambda$onKeyguardBouncerChanged$0$MiuiGxzwManager$3();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onKeyguardBouncerChanged$0 */
        public /* synthetic */ void lambda$onKeyguardBouncerChanged$0$MiuiGxzwManager$3() {
            MiuiGxzwUtils.setTouchMode(10, MiuiGxzwManager.this.mBouncer ? 0 : 3);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOn() {
            super.onScreenTurnedOn();
            Log.d("MiuiGxzwManager", "onScreenTurnedOn");
            MiuiGxzwManager.this.mMiuiGxzwOverlayView.onScreenTurnedOn();
            MiuiGxzwManager.this.mMiuiGxzwIconView.onScreenTurnedOn();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
            super.onBiometricAuthFailed(biometricSourceType);
            if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                MiuiGxzwManager.this.notifyGxzwAuthFailed();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
            super.onBiometricAuthenticated(i, biometricSourceType, z);
            if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                MiuiGxzwManager.this.notifyGxzwAuthSucceeded();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricError(int i, String str, BiometricSourceType biometricSourceType) {
            super.onBiometricError(i, str, biometricSourceType);
            if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                Log.d("MiuiGxzwManager", "onFingerprintError: msgId = " + i + ", errString = " + str);
                if ((i == 7 || i == 9) && !MiuiGxzwManager.this.mShowed) {
                    MiuiGxzwManager.this.showGxzwInKeyguardWhenLockout();
                }
                if (i == 7 || i == 9) {
                    MiuiGxzwManager.this.mHandler.removeCallbacks(this.mDelayRunnable);
                    MiuiGxzwManager.this.mFingerprintLockout = true;
                    MiuiGxzwManager.this.updateGxzwState();
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricRunningStateChanged(boolean z, BiometricSourceType biometricSourceType) {
            super.onBiometricRunningStateChanged(z, biometricSourceType);
            if (biometricSourceType == BiometricSourceType.FINGERPRINT && z) {
                MiuiGxzwManager.this.mHandler.removeCallbacks(this.mDelayRunnable);
                MiuiGxzwManager.this.mHandler.postDelayed(this.mDelayRunnable, 200);
            }
        }

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onFingerprintLockoutReset() {
            super.onFingerprintLockoutReset();
            MiuiGxzwManager.this.mHandler.removeCallbacks(this.mDelayRunnable);
            MiuiGxzwManager.this.mHandler.postDelayed(this.mDelayRunnable, 200);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$$1 */
        public /* synthetic */ void lambda$$1$MiuiGxzwManager$3() {
            MiuiGxzwManager.this.mFingerprintLockout = false;
            MiuiGxzwManager.this.updateGxzwState();
        }

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onKeyguardOccludedChanged(boolean z) {
            super.onKeyguardOccludedChanged(z);
            MiuiGxzwManager.this.updateGxzwState();
        }

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onChargeAnimationShowingChanged(boolean z, boolean z2) {
            super.onChargeAnimationShowingChanged(z, z2);
            if (!z2) {
                MiuiGxzwManager.this.updateGxzwState();
            }
        }
    };
    private boolean mLockScreenMagazinePreViewVisible;
    private ContentObserver mLowlightContentObserver = new ContentObserver(this.mHandler) {
        /* class com.android.keyguard.fod.MiuiGxzwManager.AnonymousClass7 */

        public void onChange(boolean z) {
            MiuiGxzwUtils.notifySurfaceFlinger(1112, MiuiGxzwUtils.isFodAodLowlightShowEnable(MiuiGxzwManager.this.mContext) ? 1 : 0);
        }
    };
    private MiuiGxzwIconView mMiuiGxzwIconView;
    private MiuiGxzwOverlayView mMiuiGxzwOverlayView;
    private boolean mPanelExpanded;
    private boolean mQsExpanded;
    private KeyguardSecurityModel.SecurityMode mSecurityMode = KeyguardSecurityModel.SecurityMode.None;
    private boolean mShouldShowGxzwIcon = true;
    private boolean mShowLockoutView = false;
    private boolean mShowed = false;
    private boolean mStrongAuthUnlocking = false;
    private boolean mSurfaceFlingerStatusbarShow = true;
    private Toast mToast;
    protected final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() {
        /* class com.android.keyguard.fod.MiuiGxzwManager.AnonymousClass2 */

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedWakingUp() {
            MiuiGxzwManager.this.stopDozing();
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedGoingToSleep() {
            Log.d("MiuiGxzwManager", "onStartedGoingToSleep");
            MiuiGxzwManager.this.mMiuiGxzwOverlayView.onStartedGoingToSleep();
            MiuiGxzwManager.this.mMiuiGxzwIconView.onStartedGoingToSleep();
            MiuiGxzwManager.this.startDozing();
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedGoingToSleep() {
            MiuiGxzwManager.this.mMiuiGxzwOverlayView.onFinishedGoingToSleep();
        }
    };
    private boolean moveHelperCanShow;

    public static boolean isGxzwSensor() {
        return true;
    }

    public static MiuiGxzwManager getInstance() {
        if (sService == null) {
            synchronized (MiuiGxzwManager.class) {
                if (sService == null) {
                    sService = (MiuiGxzwManager) Dependency.get(MiuiGxzwManager.class);
                    addToServiceManager(sService);
                }
            }
        }
        return sService;
    }

    private static void addToServiceManager(MiuiGxzwManager miuiGxzwManager) {
        if (MiuiKeyguardUtils.isSystemProcess()) {
            try {
                ServiceManager.addService("android.app.fod.ICallback", miuiGxzwManager);
                Log.d("MiuiGxzwManager", "add MiuiGxzwManager successfully");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("MiuiGxzwManager", "add MiuiGxzwManager fail");
            }
        } else {
            Slog.w("MiuiGxzwManager", "second space should not init MiuiGxzwManager:" + new Throwable());
        }
    }

    public static Rect getFodPosition(Context context) {
        MiuiGxzwUtils.caculateGxzwIconSize(context);
        Rect rect = new Rect();
        rect.left = MiuiGxzwUtils.GXZW_ICON_X;
        rect.top = MiuiGxzwUtils.GXZW_ICON_Y;
        rect.right = MiuiGxzwUtils.GXZW_ICON_X + MiuiGxzwUtils.GXZW_ICON_WIDTH;
        rect.bottom = MiuiGxzwUtils.GXZW_ICON_Y + MiuiGxzwUtils.GXZW_ICON_HEIGHT;
        return rect;
    }

    public boolean isDozing() {
        return this.mDozing;
    }

    public void onKeyguardShow() {
        Log.d("MiuiGxzwManager", "onKeyguardShow");
        this.mKeyguardShow = true;
        this.mStrongAuthUnlocking = false;
        setGxzwUnlockMode(0);
        setGxzwAuthFingerprintID(0);
        if (((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFingerprintDetectionRunning() && !this.mShowed) {
            this.mHandler.removeMessages(1001);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1001, 0, 0));
        }
    }

    public void onKeyguardHide() {
        Log.d("MiuiGxzwManager", "onKeyguardHide");
        this.mKeyguardShow = false;
        this.mStrongAuthUnlocking = false;
        dismissGxzwView();
    }

    public void notifyKeycodeGoto() {
        this.mMiuiGxzwIconView.onKeycodeGoto();
    }

    public void dismissGxzwIconView(boolean z) {
        if (this.mShouldShowGxzwIcon != (!z) && this.mShowed) {
            if (!getKeyguardAuthen() || !isUnlockByGxzw()) {
                Log.i("MiuiGxzwManager", "dismissGxzwIconView: dismiss = " + z);
                this.mShouldShowGxzwIcon = z ^ true;
                this.mMiuiGxzwIconView.dismissGxzwIconView(z);
            }
        }
    }

    public boolean isBouncer() {
        return this.mBouncer;
    }

    public synchronized void resetGxzwUnlockMode() {
        setGxzwUnlockMode(0);
        setGxzwAuthFingerprintID(0);
    }

    public synchronized boolean isUnlockByGxzw() {
        boolean z;
        z = true;
        if (!(this.mGxzwUnlockMode == 1 || this.mGxzwUnlockMode == 2)) {
            z = false;
        }
        return z;
    }

    public boolean isShouldShowGxzwIcon() {
        return this.mShouldShowGxzwIcon;
    }

    public void setShowLockoutView(boolean z) {
        this.mShowLockoutView = z;
        updateGxzwState();
    }

    public void setSecurityMode(KeyguardSecurityModel.SecurityMode securityMode) {
        this.mSecurityMode = securityMode;
        updateGxzwState();
    }

    public boolean isShowFodInBouncer() {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        boolean isUnlockingWithBiometricAllowed = keyguardUpdateMonitor.isUnlockingWithBiometricAllowed(true);
        boolean isUnlockWithFingerprintPossible = keyguardUpdateMonitor.isUnlockWithFingerprintPossible(currentUser);
        KeyguardSecurityModel.SecurityMode securityMode = this.mSecurityMode;
        if ((securityMode == KeyguardSecurityModel.SecurityMode.Pattern || securityMode == KeyguardSecurityModel.SecurityMode.PIN || securityMode == KeyguardSecurityModel.SecurityMode.Password) && !this.mShowLockoutView && isUnlockingWithBiometricAllowed && isUnlockWithFingerprintPossible && !this.mStrongAuthUnlocking && !this.mFingerprintLockout) {
            return true;
        }
        return false;
    }

    public void reportSuccessfulStrongAuthUnlockAttempt() {
        if (this.mKeyguardShow) {
            this.mStrongAuthUnlocking = true;
        }
    }

    public synchronized boolean getKeyguardAuthen() {
        return this.mKeyguardAuthen;
    }

    public void nofifySurfaceFlinger(boolean z) {
        if (this.mSurfaceFlingerStatusbarShow != z) {
            this.mSurfaceFlingerStatusbarShow = z;
            MiuiGxzwUtils.notifySurfaceFlinger(1103, z ? 1 : 0);
            Log.i("MiuiGxzwManager", "nofifySurfaceFlinger: statusbarShow = " + z);
        }
    }

    public void registerCallback(MiuiGxzwCallback miuiGxzwCallback) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            if (this.mCallbacks.get(i).get() == miuiGxzwCallback) {
                return;
            }
        }
        this.mCallbacks.add(new WeakReference<>(miuiGxzwCallback));
        removeCallback(null);
        sendUpdates(miuiGxzwCallback);
    }

    public void removeCallback(MiuiGxzwCallback miuiGxzwCallback) {
        for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
            if (this.mCallbacks.get(size).get() == miuiGxzwCallback) {
                this.mCallbacks.remove(size);
            }
        }
    }

    public void disableLockScreenFod(boolean z) {
        if (z != this.mDisableLockScreenFod) {
            Slog.i("MiuiGxzwManager", "disableLockScreenFod: disable = " + z);
            this.mDisableLockScreenFod = z;
            updateGxzwState();
        }
    }

    public void disableLockScreenFodAnim(boolean z) {
        if (z != this.mDisableLockScreenFodAnim) {
            Slog.i("MiuiGxzwManager", "disableLockScreenFodAnim: disable = " + z);
            this.mDisableLockScreenFodAnim = z;
            if (!this.mBouncer && !this.mDozing) {
                this.mMiuiGxzwIconView.refreshIcon();
            }
            this.mMiuiGxzwIconView.disableLockScreenFodAnim(this.mDisableLockScreenFodAnim);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyGxzwTouchDown() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            MiuiGxzwCallback miuiGxzwCallback = this.mCallbacks.get(i).get();
            if (miuiGxzwCallback != null) {
                miuiGxzwCallback.onGxzwTouchDown();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyGxzwTouchUp() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            MiuiGxzwCallback miuiGxzwCallback = this.mCallbacks.get(i).get();
            if (miuiGxzwCallback != null) {
                miuiGxzwCallback.onGxzwTouchUp();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyGxzwAuthFailed() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            MiuiGxzwCallback miuiGxzwCallback = this.mCallbacks.get(i).get();
            if (miuiGxzwCallback != null) {
                miuiGxzwCallback.onGxzwAuthFailed();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyGxzwAuthSucceeded() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            MiuiGxzwCallback miuiGxzwCallback = this.mCallbacks.get(i).get();
            if (miuiGxzwCallback != null) {
                miuiGxzwCallback.onGxzwAuthSucceeded();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized int getGxzwAuthFingerprintID() {
        return this.mAuthFingerprintId;
    }

    /* access modifiers changed from: package-private */
    public void requestDrawWackLock(long j) {
        this.mDrawWakeLock.acquire(j);
    }

    /* access modifiers changed from: package-private */
    public void releaseDrawWackLock() {
        Log.i("MiuiGxzwManager", "releaseDrawWackLock");
        this.mDrawWakeLockStatus = 2;
        this.mDrawWakeLock.release();
    }

    /* access modifiers changed from: package-private */
    public boolean isHbmAlwaysOnWhenDoze() {
        return !MiuiKeyguardUtils.isAodEnable(this.mContext);
    }

    public MiuiGxzwManager(Context context, WakefulnessLifecycle wakefulnessLifecycle) {
        this.mContext = context;
        MiuiGxzwUtils.caculateGxzwIconSize(context);
        this.mMiuiGxzwOverlayView = new MiuiGxzwOverlayView(this.mContext);
        MiuiGxzwIconView miuiGxzwIconView = new MiuiGxzwIconView(this.mContext);
        this.mMiuiGxzwIconView = miuiGxzwIconView;
        miuiGxzwIconView.setCollectGxzwListener(this.mMiuiGxzwOverlayView);
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        keyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        wakefulnessLifecycle.addObserver(this.mWakefulnessObserver);
        IntentFilter intentFilter = new IntentFilter();
        this.mIntentFilter = intentFilter;
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mIntentFilter.addAction("miui.intent.action.HANG_UP_CHANGED");
        this.mIntentFilter.addAction("miui.action.handymode_change");
        this.mDrawWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(128, "gxzw");
        if (keyguardUpdateMonitor.isFingerprintDetectionRunning()) {
            dealCallback(1, 0);
        }
        this.mHandler.post(new Runnable(keyguardUpdateMonitor) {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwManager$dK2K2wOnVTIZgFamDmEBRnbwgg */
            public final /* synthetic */ KeyguardUpdateMonitor f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwManager.this.lambda$new$0$MiuiGxzwManager(this.f$1);
            }
        });
        ((CommandQueue) Dependency.get(CommandQueue.class)).addCallback((CommandQueue.Callbacks) this);
        ((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).registerCallback(this.mFastUnlockCallback);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("gxzw_icon_aod_show_enable"), false, this.mContentObserver, 0);
        this.mContentObserver.onChange(false);
        if (MiuiGxzwUtils.isSupportLowlight()) {
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("gxzw_icon_aod_lowlight_show_enable"), false, this.mLowlightContentObserver, 0);
            this.mLowlightContentObserver.onChange(false);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$MiuiGxzwManager(KeyguardUpdateMonitor keyguardUpdateMonitor) {
        if (keyguardUpdateMonitor.isDeviceInteractive()) {
            stopDozing();
        } else {
            startDozing();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showGxzwInKeyguardWhenLockout() {
        if (!this.mShowed && this.mKeyguardShow) {
            setKeyguardAuthen(true);
            showGxzwView(false);
            this.mMiuiGxzwIconView.setEnrolling(false);
            this.mMiuiGxzwOverlayView.setEnrolling(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startDozing() {
        Log.i("MiuiGxzwManager", "startDozing");
        this.mDozing = true;
        this.mMiuiGxzwOverlayView.startDozing();
        this.mMiuiGxzwIconView.startDozing();
        updateGxzwState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopDozing() {
        Log.i("MiuiGxzwManager", "stopDozing");
        this.mDozing = false;
        this.mMiuiGxzwOverlayView.stopDozing();
        this.mMiuiGxzwIconView.stopDozing();
        updateGxzwState();
    }

    private synchronized void setGxzwUnlockMode(int i) {
        this.mGxzwUnlockMode = i;
    }

    private synchronized void setGxzwAuthFingerprintID(int i) {
        this.mAuthFingerprintId = i;
    }

    private int dealCallback(int i, int i2) {
        Log.i("MiuiGxzwManager", "dealCallback, cmd: " + i + " param: " + i2);
        if (i == 101) {
            this.mHandler.removeMessages(1005);
            this.mHandler.sendEmptyMessage(1005);
            return 1;
        } else if (i == 102) {
            this.mHandler.removeMessages(1004);
            this.mHandler.sendEmptyMessage(1004);
            return 1;
        } else if (i == 400001) {
            this.mHandler.removeMessages(1007);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1007, i2, 0));
            return 1;
        } else if (i == 400004) {
            this.mHandler.removeMessages(1008);
            this.mHandler.sendEmptyMessage(1008);
            return 1;
        } else if (i != 400006) {
            switch (i) {
                case 1:
                    this.mHandler.removeMessages(1001);
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(1001, i2, 0));
                    return 1;
                case 2:
                case 6:
                    this.mHandler.removeMessages(1002);
                    this.mHandler.sendEmptyMessage(1002);
                    return 1;
                case 3:
                    processVendorSucess(i2);
                    return 1;
                case 4:
                    this.mHandler.removeMessages(1006);
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(1006, i2, 0));
                    return 1;
                case 5:
                    this.mHandler.removeMessages(1003);
                    this.mHandler.sendEmptyMessage(1003);
                    return 1;
                default:
                    return 1;
            }
        } else {
            this.mHandler.removeMessages(1009);
            this.mHandler.sendEmptyMessage(1009);
            return 1;
        }
    }

    private void processVendorSucess(int i) {
        if (i == 0) {
            Handler handler = this.mHandler;
            MiuiGxzwIconView miuiGxzwIconView = this.mMiuiGxzwIconView;
            Objects.requireNonNull(miuiGxzwIconView);
            handler.post(new Runnable() {
                /* class com.android.keyguard.fod.$$Lambda$fv0cJN4LV3JuB_dZRWwb5OrdZ7E */

                public final void run() {
                    MiuiGxzwIconView.this.setHightlightTransparen();
                }
            });
        } else if (getKeyguardAuthen()) {
            int authUserId = MiuiKeyguardUtils.getAuthUserId(this.mContext, i);
            int i2 = 1;
            boolean isBiometricAllowedForUser = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getStrongAuthTracker().isBiometricAllowedForUser(true, authUserId);
            if (isBiometricAllowedForUser && KeyguardUpdateMonitor.getCurrentUser() != authUserId && !MiuiKeyguardUtils.canSwitchUser(this.mContext, authUserId)) {
                isBiometricAllowedForUser = false;
            }
            if (isBiometricAllowedForUser) {
                Log.i("MiuiGxzwManager", "onAuthenticated:start to unlock");
                if (!isDozing()) {
                    i2 = 2;
                }
                setGxzwUnlockMode(i2);
                if (this.mDisableLockScreenFod) {
                    this.mHandler.post(new Runnable() {
                        /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwManager$gxRpTG63r0JItbfop_x0vVLPPwA */

                        public final void run() {
                            MiuiGxzwManager.this.lambda$processVendorSucess$1$MiuiGxzwManager();
                        }
                    });
                }
                setGxzwAuthFingerprintID(i);
                if (KeyguardUpdateMonitor.getCurrentUser() != authUserId) {
                    this.mHandler.removeMessages(1002);
                    this.mHandler.sendEmptyMessage(1002);
                }
            }
        } else {
            this.mHandler.removeMessages(1002);
            this.mHandler.sendEmptyMessage(1002);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$processVendorSucess$1 */
    public /* synthetic */ void lambda$processVendorSucess$1$MiuiGxzwManager() {
        disableLockScreenFod(false);
    }

    @Override // android.os.Binder
    public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
        if (i != 1) {
            return super.onTransact(i, parcel, parcel2, i2);
        }
        parcel.enforceInterface("android.app.fod.ICallback");
        int dealCallback = dealCallback(parcel.readInt(), parcel.readInt());
        parcel2.writeNoException();
        parcel2.writeInt(dealCallback);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showGxzwView(boolean z) {
        Log.i("MiuiGxzwManager", "showGxzwView: lightIcon = " + z + ", mShowed = " + this.mShowed + ", mShouldShowGxzwIcon = " + this.mShouldShowGxzwIcon + ", keyguardAuthen = " + getKeyguardAuthen());
        if (!this.mShowed) {
            this.mShowed = true;
            updateHightlightBackground();
            updateGxzwState();
            MiuiGxzwUtils.caculateGxzwIconSize(this.mContext);
            this.mMiuiGxzwOverlayView.show();
            this.mMiuiGxzwIconView.show(z);
            if (!this.mShouldShowGxzwIcon) {
                this.mMiuiGxzwIconView.dismissGxzwIconView(true);
            }
            this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
            updateGxzwInfoInHandyMode(this.mHandyMode);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissGxzwView() {
        Log.i("MiuiGxzwManager", "dismissGxzwView: mShowed = " + this.mShowed);
        if (this.mShowed) {
            this.mShouldShowGxzwIcon = true;
            this.mMiuiGxzwIconView.dismiss();
            this.mMiuiGxzwOverlayView.dismiss();
            this.mShowed = false;
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            updateGxzwInfoInHandyMode(this.mHandyMode);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateGxzwInfoInHandyMode(boolean z) {
        this.mHandyMode = z;
        Toast toast = this.mToast;
        if (toast != null) {
            toast.cancel();
        }
        if (this.mShowed && !this.mKeyguardAuthen && z) {
            Toast makeText = Toast.makeText(this.mContext, C0021R$string.finger_error_single_mode, 0);
            this.mToast = makeText;
            makeText.show();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void setKeyguardAuthen(boolean z) {
        boolean z2 = this.mKeyguardAuthen;
        this.mKeyguardAuthen = z;
        if (z2 != z) {
            this.mMiuiGxzwOverlayView.onKeyguardAuthen(z);
            this.mMiuiGxzwIconView.onKeyguardAuthen(z);
            updateGxzwState();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void setHealthAppAuthen(boolean z) {
        this.mHealthAppAuthen = z;
    }

    public synchronized boolean getHealthAppAuthen() {
        return this.mHealthAppAuthen;
    }

    private void updateHightlightBackground() {
        if (this.mHealthAppAuthen) {
            this.mMiuiGxzwIconView.updateHightlightBackground();
        }
    }

    private void sendUpdates(MiuiGxzwCallback miuiGxzwCallback) {
        miuiGxzwCallback.onGxzwEnableChange(isFodEnable());
    }

    private boolean isFodEnable() {
        return ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser());
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        boolean z2 = this.mDisableFingerprintIcon;
        if ((i2 & 2048) != 0) {
            this.mDisableFingerprintIcon = true;
        } else {
            this.mDisableFingerprintIcon = false;
        }
        if (z2 != this.mDisableFingerprintIcon) {
            Slog.i("MiuiGxzwManager", "disable: mDisableFingerprintIcon = " + this.mDisableFingerprintIcon);
            updateGxzwState();
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (DumpUtils.checkDumpPermission(this.mContext, "MiuiGxzwManager", printWriter)) {
            printWriter.println("MiuiGxzwManager state:");
            printWriter.println("mDrawWakeLockStatus=" + this.mDrawWakeLockStatus);
            printWriter.println("mKeyguardAuthen=" + getKeyguardAuthen());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x008a, code lost:
        if (r9.mDisableFingerprintIcon == false) goto L_0x008c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00b4, code lost:
        if (r9.mDisableLockScreenFod == false) goto L_0x008c;
     */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00bb  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateGxzwState() {
        /*
        // Method dump skipped, instructions count: 295
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.fod.MiuiGxzwManager.updateGxzwState():void");
    }

    public void updatePanelExpandedStatus(boolean z) {
        if (this.mPanelExpanded != z) {
            this.mPanelExpanded = z;
            updateGxzwState();
        }
    }

    public void updateQsExpandedStatus(boolean z) {
        if (this.mQsExpanded != z) {
            this.mQsExpanded = z;
            updateGxzwState();
        }
    }

    public void setCanShowGxzw(boolean z) {
        this.moveHelperCanShow = z;
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            getInstance().updateGxzwState();
        }
    }

    public Bitmap getGxzwAnimBitmap() {
        int i = 0;
        switch (Settings.System.getIntForUser(this.mContext.getContentResolver(), "fod_animation_type", MiuiGxzwAnimManager.getDefaultAnimType(), 0)) {
            case 6:
                i = C0013R$drawable.gxzw_light_recognizing_anim_11;
                break;
            case 7:
                i = C0013R$drawable.gxzw_star_recognizing_anim_15;
                break;
            case 8:
                i = C0013R$drawable.gxzw_aurora_recognizing_anim_15;
                break;
            case 9:
                i = C0013R$drawable.gxzw_pulse_recognizing_anim_10;
                break;
        }
        return BitmapFactory.decodeResource(this.mContext.getResources(), i);
    }
}
