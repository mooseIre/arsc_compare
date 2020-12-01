package com.android.keyguard.fod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Rect;
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
import com.android.internal.util.DumpUtils;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.miui.systemui.annotation.Inject;
import com.xiaomi.stat.c.b;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

public class MiuiGxzwManager extends Binder implements CommandQueue.Callbacks, Dumpable {
    private static volatile MiuiGxzwManager sService;
    private int mAuthFingerprintId = 0;
    /* access modifiers changed from: private */
    public boolean mBouncer = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
                MiuiGxzwManager.this.dismissGxzwView();
                MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(false);
                MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(false);
            } else if ("miui.intent.action.HANG_UP_CHANGED".equals(intent.getAction())) {
                MiuiGxzwManager.this.mMiuiGxzwOverlayView.onHandUpChange(intent.getBooleanExtra("hang_up_enable", false));
            }
        }
    };
    private final ArrayList<WeakReference<MiuiGxzwCallback>> mCallbacks = new ArrayList<>();
    private ContentObserver mContentObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable(MiuiGxzwUtils.isFodAodShowEnable(MiuiGxzwManager.this.mContext)) {
                private final /* synthetic */ boolean f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    MiuiGxzwUtils.setTouchMode(16, r1 ? 1 : 0);
                }
            });
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    private boolean mDisableFingerprintIcon = false;
    private volatile boolean mDisableLockScreenFod = false;
    private boolean mDisableLockScreenFodAnim = false;
    private boolean mDismissFodInBouncer = false;
    private boolean mDozing = false;
    private PowerManager.WakeLock mDrawWakeLock;
    public int mDrawWakeLockStatus = -1;
    private MiuiFastUnlockController.FastUnlockCallback mFastUnlockCallback = new MiuiFastUnlockController.FastUnlockCallback() {
        public void onStartFastUnlock() {
            if (MiuiGxzwManager.this.isUnlockByGxzw()) {
                Log.i("MiuiGxzwManager", "onStartFastUnlock");
                if (MiuiGxzwManager.this.mMiuiGxzwOverlayTypeManager.isOverlayTypeUrsa()) {
                    MiuiGxzwManager.this.mMiuiGxzwOverlayView.dismiss();
                }
                MiuiGxzwManager.this.mMiuiGxzwOverlayView.restoreScreenEffect();
            }
        }

        public void onFinishFastUnlock() {
            if (MiuiGxzwManager.this.isUnlockByGxzw()) {
                MiuiGxzwManager.this.mMiuiGxzwIconView.preHideIconView();
                Log.i("MiuiGxzwManager", "onFinishFastUnlock");
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mFingerprintLockout = false;
    private int mGxzwUnlockMode = 0;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case b.a /*1001*/:
                    MiuiGxzwManager.this.setKeyguardAuthen(KeyguardUpdateMonitor.getInstance(MiuiGxzwManager.this.mContext).isFingerprintDetectionRunning());
                    MiuiGxzwManager.this.setHealthAppAuthen(false);
                    MiuiGxzwManager miuiGxzwManager = MiuiGxzwManager.this;
                    if (message.arg1 != 1) {
                        z = false;
                    }
                    miuiGxzwManager.showGxzwView(z);
                    MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(false);
                    MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(false);
                    return;
                case b.b /*1002*/:
                    if (!MiuiGxzwManager.this.mHealthAppAuthen) {
                        MiuiGxzwManager.this.dismissGxzwView();
                        MiuiGxzwManager.this.setKeyguardAuthen(false);
                        MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(false);
                        MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(false);
                        return;
                    }
                    return;
                case b.c /*1003*/:
                    if (!MiuiGxzwManager.this.getKeyguardAuthen()) {
                        MiuiGxzwManager.this.setKeyguardAuthen(false);
                        MiuiGxzwManager.this.setHealthAppAuthen(false);
                        MiuiGxzwManager.this.dismissGxzwView();
                        return;
                    }
                    return;
                case b.d /*1004*/:
                    if (!MiuiGxzwManager.this.getKeyguardAuthen()) {
                        MiuiGxzwManager.this.setKeyguardAuthen(false);
                        MiuiGxzwManager.this.setHealthAppAuthen(false);
                        MiuiGxzwManager.this.dismissGxzwView();
                        MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(false);
                        MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(false);
                        return;
                    }
                    return;
                case b.e /*1005*/:
                    MiuiGxzwManager.this.setKeyguardAuthen(false);
                    MiuiGxzwManager.this.setHealthAppAuthen(false);
                    MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(true);
                    MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(true);
                    MiuiGxzwManager.this.showGxzwView(false);
                    return;
                case b.f /*1006*/:
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
                case b.g /*1007*/:
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
                case b.h /*1008*/:
                    MiuiGxzwManager.this.setHealthAppAuthen(false);
                    if (!MiuiGxzwManager.this.getKeyguardAuthen()) {
                        MiuiGxzwManager.this.dismissGxzwView();
                        MiuiGxzwManager.this.mMiuiGxzwIconView.setEnrolling(false);
                        MiuiGxzwManager.this.mMiuiGxzwOverlayView.setEnrolling(false);
                        return;
                    }
                    return;
                case b.i /*1009*/:
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
    /* access modifiers changed from: private */
    public boolean mHealthAppAuthen = false;
    private IntentFilter mIntentFilter;
    private boolean mKeyguardAuthen = false;
    private boolean mKeyguardShow;
    private KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        private Runnable mDelayRunnable = new Runnable() {
            public final void run() {
                MiuiGxzwManager.AnonymousClass2.this.lambda$$0$MiuiGxzwManager$2();
            }
        };

        public void onKeyguardBouncerChanged(boolean z) {
            super.onKeyguardBouncerChanged(z);
            Log.d("MiuiGxzwManager", "onKeyguardBouncerChanged: bouncer = " + z);
            boolean unused = MiuiGxzwManager.this.mBouncer = z;
            MiuiGxzwManager.this.updateGxzwState();
            if (!z) {
                MiuiGxzwManager.this.setDimissFodInBouncer(false);
            }
        }

        public void onStartedWakingUp() {
            super.onStartedWakingUp();
            MiuiGxzwManager.this.stopDozing();
        }

        public void onScreenTurnedOn() {
            super.onScreenTurnedOn();
            Log.d("MiuiGxzwManager", "onScreenTurnedOn");
            MiuiGxzwManager.this.mMiuiGxzwOverlayView.onScreenTurnedOn();
            MiuiGxzwManager.this.mMiuiGxzwIconView.onScreenTurnedOn();
        }

        public void onFingerprintAuthFailed() {
            super.onFingerprintAuthFailed();
            MiuiGxzwManager.this.notifyGxzwAuthFailed();
        }

        public void onFingerprintAuthenticated(int i) {
            super.onFingerprintAuthenticated(i);
            MiuiGxzwManager.this.notifyGxzwAuthSucceeded();
        }

        public void onStartedGoingToSleep(int i) {
            super.onStartedGoingToSleep(i);
            Log.d("MiuiGxzwManager", "onStartedGoingToSleep");
            MiuiGxzwManager.this.mMiuiGxzwOverlayView.onStartedGoingToSleep();
            MiuiGxzwManager.this.mMiuiGxzwIconView.onStartedGoingToSleep();
            MiuiGxzwManager.this.startDozing();
        }

        public void onFinishedGoingToSleep(int i) {
            super.onFinishedGoingToSleep(i);
            MiuiGxzwManager.this.mMiuiGxzwOverlayView.onFinishedGoingToSleep();
        }

        public void onFingerprintError(int i, String str) {
            super.onFingerprintError(i, str);
            Log.d("MiuiGxzwManager", "onFingerprintError: msgId = " + i + ", errString = " + str);
            if ((i == 7 || i == 9) && !MiuiGxzwManager.this.mShowed) {
                MiuiGxzwManager.this.showGxzwInKeyguardWhenLockout();
            }
            if (i == 7 || i == 9) {
                MiuiGxzwManager.this.mHandler.removeCallbacks(this.mDelayRunnable);
                boolean unused = MiuiGxzwManager.this.mFingerprintLockout = true;
                MiuiGxzwManager.this.updateGxzwState();
            }
        }

        public void onFingerprintRunningStateChanged(boolean z) {
            super.onFingerprintRunningStateChanged(z);
            if (z) {
                MiuiGxzwManager.this.mHandler.removeCallbacks(this.mDelayRunnable);
                MiuiGxzwManager.this.mHandler.postDelayed(this.mDelayRunnable, 200);
            }
        }

        public void onFingerprintLockoutReset() {
            super.onFingerprintLockoutReset();
            MiuiGxzwManager.this.mHandler.removeCallbacks(this.mDelayRunnable);
            MiuiGxzwManager.this.mHandler.postDelayed(this.mDelayRunnable, 200);
        }

        public /* synthetic */ void lambda$$0$MiuiGxzwManager$2() {
            boolean unused = MiuiGxzwManager.this.mFingerprintLockout = false;
            MiuiGxzwManager.this.updateGxzwState();
        }
    };
    private ContentObserver mLowlightContentObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            MiuiGxzwUtils.notifySurfaceFlinger(1112, MiuiGxzwUtils.isFodAodLowlightShowEnable(MiuiGxzwManager.this.mContext) ? 1 : 0);
        }
    };
    /* access modifiers changed from: private */
    public MiuiGxzwIconView mMiuiGxzwIconView;
    /* access modifiers changed from: private */
    public MiuiGxzwOverlayTypeManager mMiuiGxzwOverlayTypeManager;
    /* access modifiers changed from: private */
    public MiuiGxzwOverlayView mMiuiGxzwOverlayView;
    private NotificationPanelView mNotificationPanelView;
    private KeyguardSecurityModel.SecurityMode mSecurityMode = KeyguardSecurityModel.SecurityMode.None;
    private boolean mShouldShowGxzwIcon = true;
    private final boolean mShowFodWithPassword;
    private boolean mShowLockoutView = false;
    /* access modifiers changed from: private */
    public boolean mShowed = false;
    private boolean mStrongAuthUnlocking = false;
    private final boolean mSupportHbmAlwaysOn;
    private final boolean mSupportNotifySurfaceFlinger;
    private final boolean mSupportWakeLockIcon;
    private boolean mSurfaceFlingerStatusbarShow = true;

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

    public static boolean isQuickOpenEnable(Context context) {
        return MiuiGxzwQuickOpenUtil.isQuickOpenEnable(context);
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
        if (KeyguardUpdateMonitor.getInstance(this.mContext).isFingerprintDetectionRunning() && !this.mShowed) {
            this.mHandler.removeMessages(b.a);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(b.a, 0, 0));
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

    public void setUnlockLockout(boolean z) {
        this.mMiuiGxzwIconView.setUnlockLockout(z);
    }

    public static boolean isSupportLowlight() {
        return MiuiGxzwUtils.isSupportLowlight();
    }

    public boolean isShouldShowGxzwIcon() {
        return this.mShouldShowGxzwIcon;
    }

    public void setNotificationPanelView(NotificationPanelView notificationPanelView) {
        this.mNotificationPanelView = notificationPanelView;
    }

    public void updateGxzwState() {
        NotificationPanelView notificationPanelView = this.mNotificationPanelView;
        if (notificationPanelView != null) {
            notificationPanelView.updateGxzwState();
        }
    }

    private void updateHightlightBackground() {
        if (this.mHealthAppAuthen) {
            this.mMiuiGxzwIconView.updateHightlightBackground();
        }
    }

    public boolean isShowFodWithPassword() {
        return this.mShowFodWithPassword;
    }

    public boolean isShowFodInBouncer() {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(this.mContext);
        boolean isUnlockingWithFingerprintAllowed = instance.isUnlockingWithFingerprintAllowed(currentUser);
        boolean isUnlockWithFingerprintPossible = instance.isUnlockWithFingerprintPossible(currentUser);
        KeyguardSecurityModel.SecurityMode securityMode = this.mSecurityMode;
        boolean z = (securityMode == KeyguardSecurityModel.SecurityMode.Pattern || securityMode == KeyguardSecurityModel.SecurityMode.PIN || securityMode == KeyguardSecurityModel.SecurityMode.Password) && !this.mShowLockoutView && isUnlockingWithFingerprintAllowed && isUnlockWithFingerprintPossible && !this.mStrongAuthUnlocking && !this.mFingerprintLockout;
        if (!z || isShowFodWithPassword()) {
            return z;
        }
        return z && !this.mDismissFodInBouncer;
    }

    public void setSecurityMode(KeyguardSecurityModel.SecurityMode securityMode) {
        this.mSecurityMode = securityMode;
        updateGxzwState();
    }

    public void setShowLockoutView(boolean z) {
        this.mShowLockoutView = z;
        updateGxzwState();
    }

    public void setDimissFodInBouncer(boolean z) {
        if (this.mDismissFodInBouncer != z) {
            this.mDismissFodInBouncer = z;
            updateGxzwState();
        }
    }

    public void reportSuccessfulStrongAuthUnlockAttempt() {
        if (this.mKeyguardShow) {
            this.mStrongAuthUnlocking = true;
        }
    }

    public boolean isDisableFingerprintIcon() {
        return this.mDisableFingerprintIcon;
    }

    public synchronized boolean getKeyguardAuthen() {
        return this.mKeyguardAuthen;
    }

    public boolean isShow() {
        return this.mShowed;
    }

    public void nofifySurfaceFlinger(boolean z) {
        if (this.mSupportNotifySurfaceFlinger && this.mSurfaceFlingerStatusbarShow != z) {
            this.mSurfaceFlingerStatusbarShow = z;
            MiuiGxzwUtils.notifySurfaceFlinger(1103, z ? 1 : 0);
            Log.i("MiuiGxzwManager", "nofifySurfaceFlinger: statusbarShow = " + z);
        }
    }

    public void registerCallback(MiuiGxzwCallback miuiGxzwCallback) {
        int i = 0;
        while (i < this.mCallbacks.size()) {
            if (this.mCallbacks.get(i).get() != miuiGxzwCallback) {
                i++;
            } else {
                return;
            }
        }
        this.mCallbacks.add(new WeakReference(miuiGxzwCallback));
        removeCallback((MiuiGxzwCallback) null);
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

    public boolean isDisableLockScreenFod() {
        return this.mDisableLockScreenFod;
    }

    /* access modifiers changed from: package-private */
    public void notifyGxzwTouchDown() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            MiuiGxzwCallback miuiGxzwCallback = (MiuiGxzwCallback) this.mCallbacks.get(i).get();
            if (miuiGxzwCallback != null) {
                miuiGxzwCallback.onGxzwTouchDown();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyGxzwTouchUp() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            MiuiGxzwCallback miuiGxzwCallback = (MiuiGxzwCallback) this.mCallbacks.get(i).get();
            if (miuiGxzwCallback != null) {
                miuiGxzwCallback.onGxzwTouchUp();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyGxzwAuthFailed() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            MiuiGxzwCallback miuiGxzwCallback = (MiuiGxzwCallback) this.mCallbacks.get(i).get();
            if (miuiGxzwCallback != null) {
                miuiGxzwCallback.onGxzwAuthFailed();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyGxzwAuthSucceeded() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            MiuiGxzwCallback miuiGxzwCallback = (MiuiGxzwCallback) this.mCallbacks.get(i).get();
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
        return this.mSupportHbmAlwaysOn && !MiuiKeyguardUtils.isAodEnable(this.mContext);
    }

    /* access modifiers changed from: package-private */
    public boolean isSupportWakeLockIcon() {
        return this.mSupportWakeLockIcon;
    }

    public MiuiGxzwManager(@Inject Context context) {
        this.mContext = context;
        this.mMiuiGxzwOverlayTypeManager = new MiuiGxzwOverlayTypeManager(this.mContext);
        this.mSupportNotifySurfaceFlinger = !this.mMiuiGxzwOverlayTypeManager.isOverlayTypeUrsa();
        this.mShowFodWithPassword = context.getResources().getBoolean(R.bool.config_enableShowFodWithPassword) && !MiuiGxzwUtils.isLargeFod();
        this.mSupportHbmAlwaysOn = !this.mMiuiGxzwOverlayTypeManager.isOverlayTypeUrsa();
        this.mSupportWakeLockIcon = !this.mMiuiGxzwOverlayTypeManager.isOverlayTypeUrsa();
        MiuiGxzwUtils.caculateGxzwIconSize(context);
        this.mMiuiGxzwOverlayView = new MiuiGxzwOverlayView(this.mContext, this.mMiuiGxzwOverlayTypeManager);
        this.mMiuiGxzwIconView = new MiuiGxzwIconView(this.mContext);
        this.mMiuiGxzwIconView.setCollectGxzwListener(this.mMiuiGxzwOverlayView);
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(this.mContext);
        instance.registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mIntentFilter.addAction("miui.intent.action.HANG_UP_CHANGED");
        this.mDrawWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(128, "gxzw");
        if (instance.isFingerprintDetectionRunning()) {
            dealCallback(1, 0);
        }
        this.mHandler.post(new Runnable(instance) {
            private final /* synthetic */ KeyguardUpdateMonitor f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwManager.this.lambda$new$0$MiuiGxzwManager(this.f$1);
            }
        });
        ((CommandQueue) SystemUI.getComponent(this.mContext, CommandQueue.class)).addCallbacks(this);
        ((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).registerCallback(this.mFastUnlockCallback);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("gxzw_icon_aod_show_enable"), false, this.mContentObserver, 0);
        this.mContentObserver.onChange(false);
        if (MiuiGxzwUtils.isSupportLowlight()) {
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("gxzw_icon_aod_lowlight_show_enable"), false, this.mLowlightContentObserver, 0);
            this.mLowlightContentObserver.onChange(false);
        }
    }

    public /* synthetic */ void lambda$new$0$MiuiGxzwManager(KeyguardUpdateMonitor keyguardUpdateMonitor) {
        if (keyguardUpdateMonitor.isDeviceInteractive()) {
            stopDozing();
        } else {
            startDozing();
        }
    }

    /* access modifiers changed from: private */
    public void showGxzwInKeyguardWhenLockout() {
        if (!this.mShowed && this.mKeyguardShow) {
            setKeyguardAuthen(true);
            showGxzwView(false);
            this.mMiuiGxzwIconView.setEnrolling(false);
            this.mMiuiGxzwOverlayView.setEnrolling(false);
        }
    }

    /* access modifiers changed from: private */
    public void startDozing() {
        Log.i("MiuiGxzwManager", "startDozing");
        this.mDozing = true;
        this.mMiuiGxzwOverlayView.startDozing();
        this.mMiuiGxzwIconView.startDozing();
        NotificationPanelView notificationPanelView = this.mNotificationPanelView;
        if (notificationPanelView != null) {
            notificationPanelView.updateGxzwState();
        }
    }

    /* access modifiers changed from: private */
    public void stopDozing() {
        Log.i("MiuiGxzwManager", "stopDozing");
        this.mDozing = false;
        this.mMiuiGxzwOverlayView.stopDozing();
        this.mMiuiGxzwIconView.stopDozing();
        NotificationPanelView notificationPanelView = this.mNotificationPanelView;
        if (notificationPanelView != null) {
            notificationPanelView.updateGxzwState();
        }
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
            this.mHandler.removeMessages(b.e);
            this.mHandler.sendEmptyMessage(b.e);
            return 1;
        } else if (i == 102) {
            this.mHandler.removeMessages(b.d);
            this.mHandler.sendEmptyMessage(b.d);
            return 1;
        } else if (i == 400001) {
            this.mHandler.removeMessages(b.g);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(b.g, i2, 0));
            return 1;
        } else if (i == 400004) {
            this.mHandler.removeMessages(b.h);
            this.mHandler.sendEmptyMessage(b.h);
            return 1;
        } else if (i != 400006) {
            switch (i) {
                case 1:
                    this.mHandler.removeMessages(b.a);
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(b.a, i2, 0));
                    return 1;
                case 2:
                case 6:
                    this.mHandler.removeMessages(b.b);
                    this.mHandler.sendEmptyMessage(b.b);
                    return 1;
                case 3:
                    processVendorSucess(i2);
                    return 1;
                case 4:
                    this.mHandler.removeMessages(b.f);
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(b.f, i2, 0));
                    return 1;
                case 5:
                    this.mHandler.removeMessages(b.c);
                    this.mHandler.sendEmptyMessage(b.c);
                    return 1;
                default:
                    return 1;
            }
        } else {
            this.mHandler.removeMessages(b.i);
            this.mHandler.sendEmptyMessage(b.i);
            return 1;
        }
    }

    private void processVendorSucess(int i) {
        if (i == 0) {
            Handler handler = this.mHandler;
            MiuiGxzwIconView miuiGxzwIconView = this.mMiuiGxzwIconView;
            Objects.requireNonNull(miuiGxzwIconView);
            handler.post(new Runnable() {
                public final void run() {
                    MiuiGxzwIconView.this.setHightlightTransparen();
                }
            });
        } else if (getKeyguardAuthen()) {
            int authUserId = MiuiKeyguardUtils.getAuthUserId(this.mContext, i);
            boolean isUnlockingWithFingerprintAllowed = KeyguardUpdateMonitor.getInstance(this.mContext).isUnlockingWithFingerprintAllowed(authUserId);
            if (isUnlockingWithFingerprintAllowed && KeyguardUpdateMonitor.getCurrentUser() != authUserId && !MiuiKeyguardUtils.canSwitchUser(this.mContext, authUserId)) {
                isUnlockingWithFingerprintAllowed = false;
            }
            if (isUnlockingWithFingerprintAllowed) {
                Log.i("MiuiGxzwManager", "onAuthenticated:start to unlock");
                setGxzwUnlockMode(isDozing() ? 1 : 2);
                if (this.mDisableLockScreenFod) {
                    this.mHandler.post(new Runnable() {
                        public final void run() {
                            MiuiGxzwManager.this.lambda$processVendorSucess$1$MiuiGxzwManager();
                        }
                    });
                }
                setGxzwAuthFingerprintID(i);
                if (KeyguardUpdateMonitor.getCurrentUser() != authUserId) {
                    this.mHandler.removeMessages(b.b);
                    this.mHandler.sendEmptyMessage(b.b);
                }
            }
        } else {
            this.mHandler.removeMessages(b.b);
            this.mHandler.sendEmptyMessage(b.b);
        }
    }

    public /* synthetic */ void lambda$processVendorSucess$1$MiuiGxzwManager() {
        disableLockScreenFod(false);
    }

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
    public void showGxzwView(boolean z) {
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
        }
    }

    /* access modifiers changed from: private */
    public void dismissGxzwView() {
        Log.i("MiuiGxzwManager", "dismissGxzwView: mShowed = " + this.mShowed);
        if (this.mShowed) {
            this.mShouldShowGxzwIcon = true;
            this.mMiuiGxzwIconView.dismiss();
            this.mMiuiGxzwOverlayView.dismiss();
            this.mShowed = false;
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        }
    }

    /* access modifiers changed from: private */
    public synchronized void setKeyguardAuthen(boolean z) {
        boolean z2 = this.mKeyguardAuthen;
        this.mKeyguardAuthen = z;
        if (z2 != z) {
            this.mMiuiGxzwOverlayView.onKeyguardAuthen(z);
            this.mMiuiGxzwIconView.onKeyguardAuthen(z);
            updateGxzwState();
        }
    }

    /* access modifiers changed from: private */
    public synchronized void setHealthAppAuthen(boolean z) {
        this.mHealthAppAuthen = z;
    }

    public synchronized boolean getHealthAppAuthen() {
        return this.mHealthAppAuthen;
    }

    private void sendUpdates(MiuiGxzwCallback miuiGxzwCallback) {
        miuiGxzwCallback.onGxzwEnableChange(isFodEnable());
    }

    private boolean isFodEnable() {
        return KeyguardUpdateMonitor.getInstance(this.mContext).isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser());
    }

    public void disable(int i, int i2, boolean z) {
        boolean z2 = this.mDisableFingerprintIcon;
        if ((i & 2048) != 0) {
            this.mDisableFingerprintIcon = true;
        } else {
            this.mDisableFingerprintIcon = false;
        }
        if (z2 != this.mDisableFingerprintIcon) {
            Slog.i("MiuiGxzwManager", "disable: mDisableFingerprintIcon = " + this.mDisableFingerprintIcon);
            updateGxzwState();
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (DumpUtils.checkDumpPermission(this.mContext, "MiuiGxzwManager", printWriter)) {
            printWriter.println("MiuiGxzwManager state:");
            printWriter.println("mDrawWakeLockStatus=" + this.mDrawWakeLockStatus);
            printWriter.println("mKeyguardAuthen=" + getKeyguardAuthen());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006f, code lost:
        if (r0 != 3) goto L_0x007b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.graphics.Bitmap getGxzwAnimBitmap() {
        /*
            r10 = this;
            android.content.Context r0 = r10.mContext
            android.content.ContentResolver r0 = r0.getContentResolver()
            int r1 = com.android.keyguard.fod.MiuiGxzwAnimManager.getDefaultAnimType()
            r2 = 0
            java.lang.String r3 = "fod_animation_type"
            int r0 = android.provider.Settings.System.getIntForUser(r0, r3, r1, r2)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "getGxzwAnimBitmap type="
            r1.append(r3)
            r1.append(r0)
            java.lang.String r1 = r1.toString()
            java.lang.String r3 = "MiuiGxzwManager"
            android.util.Log.d(r3, r1)
            boolean r1 = com.android.keyguard.fod.MiuiGxzwAnimManager.SUPPORT_AURORA
            r3 = 2131232044(0x7f08052c, float:1.8080186E38)
            r4 = 2131231802(0x7f08043a, float:1.8079695E38)
            r5 = 2131232025(0x7f080519, float:1.8080148E38)
            r6 = 5
            r7 = 3
            r8 = 1
            if (r1 == 0) goto L_0x0044
            if (r0 == 0) goto L_0x007a
            if (r0 == r8) goto L_0x0078
            if (r0 == r7) goto L_0x0072
            if (r0 == r6) goto L_0x0040
            goto L_0x007b
        L_0x0040:
            r2 = 2131231828(0x7f080454, float:1.8079748E38)
            goto L_0x007b
        L_0x0044:
            java.lang.String r1 = android.os.Build.DEVICE
            java.lang.String r9 = "cas"
            boolean r1 = r1.equals(r9)
            if (r1 == 0) goto L_0x0068
            if (r0 == 0) goto L_0x0063
            if (r0 == r8) goto L_0x005f
            if (r0 == r7) goto L_0x005b
            if (r0 == r6) goto L_0x0057
            goto L_0x007b
        L_0x0057:
            r0 = 2131231827(0x7f080453, float:1.8079746E38)
            goto L_0x0066
        L_0x005b:
            r0 = 2131232037(0x7f080525, float:1.8080172E38)
            goto L_0x0066
        L_0x005f:
            r0 = 2131231849(0x7f080469, float:1.807979E38)
            goto L_0x0066
        L_0x0063:
            r0 = 2131232120(0x7f080578, float:1.808034E38)
        L_0x0066:
            r2 = r0
            goto L_0x007b
        L_0x0068:
            if (r0 == 0) goto L_0x007a
            if (r0 == r8) goto L_0x0078
            r1 = 2
            if (r0 == r1) goto L_0x0074
            if (r0 == r7) goto L_0x0072
            goto L_0x007b
        L_0x0072:
            r2 = r3
            goto L_0x007b
        L_0x0074:
            r2 = 2131232106(0x7f08056a, float:1.8080312E38)
            goto L_0x007b
        L_0x0078:
            r2 = r4
            goto L_0x007b
        L_0x007a:
            r2 = r5
        L_0x007b:
            android.content.Context r10 = r10.mContext
            android.content.res.Resources r10 = r10.getResources()
            android.graphics.Bitmap r10 = android.graphics.BitmapFactory.decodeResource(r10, r2)
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.fod.MiuiGxzwManager.getGxzwAnimBitmap():android.graphics.Bitmap");
    }
}
