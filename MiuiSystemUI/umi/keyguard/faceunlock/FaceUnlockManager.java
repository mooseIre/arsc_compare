package com.android.keyguard.faceunlock;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.MiuiKeyguardFaceUnlockView;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import com.miui.systemui.annotation.Inject;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import miui.os.Build;

public class FaceUnlockManager {
    /* access modifiers changed from: private */
    public static Context sContext;
    private static int sFaceType;
    private static BaseFaceUnlockManager sFaceUnlockManagerImpl;
    private static volatile FaceUnlockManager sInstance;
    /* access modifiers changed from: private */
    public final ArrayList<WeakReference<FaceUnlockCallback>> mCallbacks = new ArrayList<>();
    private volatile boolean mChargeAnimationWindowShowing = false;
    private boolean mDisableLockScreenFaceUnlockAnim = false;
    /* access modifiers changed from: private */
    public boolean mFaceAuthLocked;
    /* access modifiers changed from: private */
    public boolean mFaceAuthStarted;
    /* access modifiers changed from: private */
    public boolean mFaceUnlockApplyForKeyguard;
    ContentObserver mFaceUnlockApplyForKeyguardObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            super.onChange(z);
            FaceUnlockManager faceUnlockManager = FaceUnlockManager.this;
            boolean z2 = false;
            if (Settings.Secure.getIntForUser(FaceUnlockManager.sContext.getContentResolver(), "face_unlcok_apply_for_lock", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0) {
                z2 = true;
            }
            boolean unused = faceUnlockManager.mFaceUnlockApplyForKeyguard = z2;
        }
    };
    private FaceUnlockCallback mFaceUnlockCallback = new FaceUnlockCallback() {
        public void onFaceAuthStart() {
            Slog.d("miui_face", "onFaceAuthStart");
            for (int i = 0; i < FaceUnlockManager.this.mCallbacks.size(); i++) {
                FaceUnlockCallback faceUnlockCallback = (FaceUnlockCallback) ((WeakReference) FaceUnlockManager.this.mCallbacks.get(i)).get();
                if (faceUnlockCallback != null) {
                    faceUnlockCallback.onFaceAuthStart();
                }
            }
        }

        public void onFaceAuthHelp(int i) {
            Slog.d("miui_face", "onHelp=" + MiuiFaceUnlockUtils.getFaceHelpInfo(FaceUnlockManager.sContext, i));
            for (int i2 = 0; i2 < FaceUnlockManager.this.mCallbacks.size(); i2++) {
                FaceUnlockCallback faceUnlockCallback = (FaceUnlockCallback) ((WeakReference) FaceUnlockManager.this.mCallbacks.get(i2)).get();
                if (faceUnlockCallback != null) {
                    faceUnlockCallback.onFaceAuthHelp(i);
                }
            }
        }

        public void onFaceAuthenticated() {
            Slog.d("miui_face", "onFaceAuthenticated");
            boolean unused = FaceUnlockManager.this.mFaceAuthStarted = false;
            FaceUnlockManager.this.mUpdateMonitor.putUserFaceAuthenticated(0);
            for (int i = 0; i < FaceUnlockManager.this.mCallbacks.size(); i++) {
                FaceUnlockCallback faceUnlockCallback = (FaceUnlockCallback) ((WeakReference) FaceUnlockManager.this.mCallbacks.get(i)).get();
                if (faceUnlockCallback != null) {
                    faceUnlockCallback.onFaceAuthenticated();
                }
            }
        }

        public void onFaceAuthFailed() {
            Slog.d("miui_face", "onFaceAuthFailed");
            boolean unused = FaceUnlockManager.this.mFaceAuthStarted = false;
            for (int i = 0; i < FaceUnlockManager.this.mCallbacks.size(); i++) {
                FaceUnlockCallback faceUnlockCallback = (FaceUnlockCallback) ((WeakReference) FaceUnlockManager.this.mCallbacks.get(i)).get();
                if (faceUnlockCallback != null) {
                    faceUnlockCallback.onFaceAuthFailed();
                }
            }
        }

        public void onFaceAuthTimeOut(boolean z) {
            Slog.d("miui_face", "onFaceAuthTimeOut  hasFace=" + z);
            boolean unused = FaceUnlockManager.this.mFaceAuthStarted = false;
            for (int i = 0; i < FaceUnlockManager.this.mCallbacks.size(); i++) {
                FaceUnlockCallback faceUnlockCallback = (FaceUnlockCallback) ((WeakReference) FaceUnlockManager.this.mCallbacks.get(i)).get();
                if (faceUnlockCallback != null) {
                    faceUnlockCallback.onFaceAuthTimeOut(z);
                }
            }
        }

        public void onFaceAuthLocked() {
            Slog.d("miui_face", "onFaceAuthLocked");
            boolean unused = FaceUnlockManager.this.mFaceAuthStarted = false;
            boolean unused2 = FaceUnlockManager.this.mFaceAuthLocked = true;
            for (int i = 0; i < FaceUnlockManager.this.mCallbacks.size(); i++) {
                FaceUnlockCallback faceUnlockCallback = (FaceUnlockCallback) ((WeakReference) FaceUnlockManager.this.mCallbacks.get(i)).get();
                if (faceUnlockCallback != null) {
                    faceUnlockCallback.onFaceAuthLocked();
                }
            }
        }

        public void onFaceEnableChange(boolean z, boolean z2) {
            Slog.d("miui_face", "onFaceEnableChange enable=" + z + ";stay=" + z2);
            for (int i = 0; i < FaceUnlockManager.this.mCallbacks.size(); i++) {
                FaceUnlockCallback faceUnlockCallback = (FaceUnlockCallback) ((WeakReference) FaceUnlockManager.this.mCallbacks.get(i)).get();
                if (faceUnlockCallback != null) {
                    faceUnlockCallback.onFaceEnableChange(z, z2);
                }
            }
        }

        public void unblockScreenOn() {
            Slog.d("miui_face", "unblockScreenOn");
            for (int i = 0; i < FaceUnlockManager.this.mCallbacks.size(); i++) {
                FaceUnlockCallback faceUnlockCallback = (FaceUnlockCallback) ((WeakReference) FaceUnlockManager.this.mCallbacks.get(i)).get();
                if (faceUnlockCallback != null) {
                    faceUnlockCallback.unblockScreenOn();
                }
            }
        }

        public void restartFaceUnlock() {
            Slog.d("miui_face", "restartFaceUnlock");
            for (int i = 0; i < FaceUnlockManager.this.mCallbacks.size(); i++) {
                FaceUnlockCallback faceUnlockCallback = (FaceUnlockCallback) ((WeakReference) FaceUnlockManager.this.mCallbacks.get(i)).get();
                if (faceUnlockCallback != null) {
                    faceUnlockCallback.restartFaceUnlock();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mFaceUnlockStartByNotificationScreenOn;
    ContentObserver mFaceUnlockStartByNotificationScreenOnObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            super.onChange(z);
            FaceUnlockManager faceUnlockManager = FaceUnlockManager.this;
            boolean z2 = false;
            if (Settings.Secure.getIntForUser(FaceUnlockManager.sContext.getContentResolver(), "face_unlock_by_notification_screen_on", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0) {
                z2 = true;
            }
            boolean unused = faceUnlockManager.mFaceUnlockStartByNotificationScreenOn = z2;
        }
    };
    /* access modifiers changed from: private */
    public boolean mFaceUnlockSuccessShowMessage;
    ContentObserver mFaceUnlockSuccessShowMessageObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            super.onChange(z);
            FaceUnlockManager faceUnlockManager = FaceUnlockManager.this;
            boolean z2 = false;
            if (Settings.Secure.getIntForUser(FaceUnlockManager.sContext.getContentResolver(), "face_unlock_success_show_message", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0) {
                z2 = true;
            }
            boolean unused = faceUnlockManager.mFaceUnlockSuccessShowMessage = z2;
        }
    };
    /* access modifiers changed from: private */
    public boolean mFaceUnlockSuccessStayScreen;
    ContentObserver mFaceUnlockSuccessStayScreenObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            super.onChange(z);
            FaceUnlockManager faceUnlockManager = FaceUnlockManager.this;
            boolean z2 = false;
            if (Settings.Secure.getIntForUser(FaceUnlockManager.sContext.getContentResolver(), "face_unlock_success_stay_screen", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0) {
                z2 = true;
            }
            boolean unused = faceUnlockManager.mFaceUnlockSuccessStayScreen = z2;
        }
    };
    private MiuiKeyguardFaceUnlockView mFaceUnlockView;
    private final ArrayList<WeakReference<MiuiKeyguardFaceUnlockView>> mFaceViewList = new ArrayList<>();
    private long mScreenOnDelay;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;
    private boolean mWakeupByNotification;

    public static FaceUnlockManager getInstance() {
        return getInstance(0);
    }

    public static FaceUnlockManager getInstance(int i) {
        Class cls = FaceUnlockManager.class;
        if (sInstance == null) {
            synchronized (cls) {
                if (sInstance == null) {
                    sInstance = (FaceUnlockManager) Dependency.get(cls);
                }
            }
        }
        sFaceType = i;
        if (sFaceUnlockManagerImpl == null) {
            if (MiuiFaceUnlockUtils.isSupportFaceUnlock(sContext)) {
                sFaceUnlockManagerImpl = new FaceUnlockManagerSupport(sContext, sFaceType);
            } else {
                sFaceUnlockManagerImpl = new FaceUnlockManagerNoSupport(sContext, sFaceType);
            }
        }
        sFaceUnlockManagerImpl.updateFaceUnlockType(i);
        return sInstance;
    }

    public FaceUnlockManager(@Inject Context context) {
        sContext = context;
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        if (MiuiFaceUnlockUtils.isSupportFaceUnlock(sContext)) {
            registerFaceUnlockContentObserver();
        }
    }

    public void addFaceUnlockView(MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView) {
        if (!this.mFaceViewList.contains(miuiKeyguardFaceUnlockView)) {
            this.mFaceViewList.add(new WeakReference(miuiKeyguardFaceUnlockView));
            removeFaceUnlockView((MiuiKeyguardFaceUnlockView) null);
        }
    }

    public void removeFaceUnlockView(MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView) {
        this.mFaceViewList.remove(miuiKeyguardFaceUnlockView);
    }

    private void initFaceUnlock() {
        if (sFaceUnlockManagerImpl == null) {
            Slog.i("miui_face", "face unlock init fail  faceunlockmanager is null");
        } else if (!this.mUpdateMonitor.mustPasswordUnlockDevice() && shouldListenForFaceUnlock()) {
            sFaceUnlockManagerImpl.runOnFaceUnlockWorkerThread($$Lambda$FaceUnlockManager$4K9BiyHSFxhXgOZApIQ6QOpJZao.INSTANCE);
        }
    }

    public boolean isFaceUnlockInited() {
        BaseFaceUnlockManager baseFaceUnlockManager = sFaceUnlockManagerImpl;
        if (baseFaceUnlockManager != null) {
            return baseFaceUnlockManager.isFaceUnlockInited();
        }
        Slog.i("miui_face", "isFaceUnlockInited fail  faceunlockmanager is null");
        return false;
    }

    public void startFaceUnlock() {
        startFaceUnlock(0);
    }

    public void startFaceUnlock(int i) {
        if (sFaceUnlockManagerImpl == null) {
            Slog.i("miui_face", "start face detect fail  faceunlockmanager is null");
        } else if (isFaceUnlockStarted() || !shouldStartFaceDetectForCamera(i) || !shouldStartFaceUnlock()) {
            printStartFaceUnlockFailLog(i);
        } else {
            Slog.i("miui_face", "start face unlock ");
            KeyguardUpdateMonitor.sScreenTurnedOnTime = System.currentTimeMillis();
            this.mFaceAuthStarted = true;
            sFaceUnlockManagerImpl.runOnFaceUnlockWorkerThread(new Runnable(i) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    FaceUnlockManager.this.lambda$startFaceUnlock$1$FaceUnlockManager(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startFaceUnlock$1 */
    public /* synthetic */ void lambda$startFaceUnlock$1$FaceUnlockManager(int i) {
        if (shouldStartFaceUnlock()) {
            sFaceUnlockManagerImpl.startFaceUnlock(this.mFaceUnlockCallback);
            return;
        }
        this.mFaceAuthStarted = false;
        printStartFaceUnlockFailLog(i);
    }

    public void stopFaceUnlock() {
        if (sFaceUnlockManagerImpl == null) {
            Slog.i("miui_face", "stop face detect fail  faceunlockmanager is null");
        } else if (isFaceUnlockStarted()) {
            Slog.i("miui_face", "stop face unlock ");
            setFaceUnlockStarted(false);
            sFaceUnlockManagerImpl.runOnFaceUnlockWorkerThread($$Lambda$FaceUnlockManager$5C24B_qMsxdzOIW2dQGt9cr9_uw.INSTANCE);
        }
    }

    public void deleteFeature(String str, FaceRemoveCallback faceRemoveCallback) {
        BaseFaceUnlockManager baseFaceUnlockManager = sFaceUnlockManagerImpl;
        if (baseFaceUnlockManager == null) {
            Slog.i("miui_face", "deleteFeature fail  faceunlockmanager is null");
        } else {
            baseFaceUnlockManager.deleteFeature(str, faceRemoveCallback);
        }
    }

    public void resetFailCount() {
        BaseFaceUnlockManager baseFaceUnlockManager = sFaceUnlockManagerImpl;
        if (baseFaceUnlockManager == null) {
            Slog.i("miui_face", "resetFailCount fail  faceunlockmanager is null");
        } else {
            baseFaceUnlockManager.resetFailCount();
        }
    }

    public void setFaceUnlockStarted(boolean z) {
        this.mFaceAuthStarted = z;
    }

    public boolean isFaceUnlockStarted() {
        return this.mFaceAuthStarted;
    }

    public void setFaceLocked(boolean z) {
        this.mFaceAuthLocked = z;
    }

    public boolean isFaceLocked() {
        return this.mFaceAuthLocked;
    }

    public void setWakeupByNotification(boolean z) {
        this.mWakeupByNotification = z;
    }

    public boolean isWakeupByNotification() {
        return this.mWakeupByNotification;
    }

    private void registerFaceUnlockContentObserver() {
        if (MiuiFaceUnlockUtils.isSupportFaceUnlock(sContext)) {
            sContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("face_unlcok_apply_for_lock"), false, this.mFaceUnlockApplyForKeyguardObserver, 0);
            this.mFaceUnlockApplyForKeyguardObserver.onChange(false);
            sContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("face_unlock_success_stay_screen"), false, this.mFaceUnlockSuccessStayScreenObserver, 0);
            this.mFaceUnlockSuccessStayScreenObserver.onChange(false);
            sContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("face_unlock_success_show_message"), false, this.mFaceUnlockSuccessShowMessageObserver, 0);
            this.mFaceUnlockSuccessShowMessageObserver.onChange(false);
            sContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("face_unlock_by_notification_screen_on"), false, this.mFaceUnlockStartByNotificationScreenOnObserver, 0);
            this.mFaceUnlockStartByNotificationScreenOnObserver.onChange(false);
        }
    }

    private void unregisterFaceUnlockContentObserver() {
        sContext.getContentResolver().unregisterContentObserver(this.mFaceUnlockApplyForKeyguardObserver);
        sContext.getContentResolver().unregisterContentObserver(this.mFaceUnlockSuccessStayScreenObserver);
        sContext.getContentResolver().unregisterContentObserver(this.mFaceUnlockSuccessShowMessageObserver);
        sContext.getContentResolver().unregisterContentObserver(this.mFaceUnlockStartByNotificationScreenOnObserver);
    }

    public boolean isFaceUnlockApplyForKeyguard() {
        return this.mFaceUnlockApplyForKeyguard;
    }

    public boolean isStayScreenWhenFaceUnlockSuccess() {
        return this.mFaceUnlockSuccessStayScreen;
    }

    public boolean isFaceUnlockSuccessAndStayScreen() {
        return this.mUpdateMonitor.isFaceUnlock() && this.mFaceUnlockSuccessStayScreen;
    }

    public boolean isShowMessageWhenFaceUnlockSuccess() {
        return this.mFaceUnlockApplyForKeyguard && this.mFaceUnlockSuccessStayScreen && this.mFaceUnlockSuccessShowMessage;
    }

    public void disableLockScreenFaceUnlockAnim(boolean z) {
        if (z != this.mDisableLockScreenFaceUnlockAnim) {
            this.mDisableLockScreenFaceUnlockAnim = z;
            if (!this.mUpdateMonitor.isBouncerShowing()) {
                this.mFaceUnlockView.updateFaceUnlockView();
            }
        }
    }

    public boolean isDisableLockScreenFaceUnlockAnim() {
        return this.mDisableLockScreenFaceUnlockAnim;
    }

    public void setShowingChargeAnimationWindow(boolean z) {
        if (this.mChargeAnimationWindowShowing != z) {
            this.mChargeAnimationWindowShowing = z;
            if (this.mChargeAnimationWindowShowing) {
                stopFaceUnlock();
            } else {
                startFaceUnlock();
            }
        }
    }

    public boolean isShowingChargeAnimationWindow() {
        return this.mChargeAnimationWindowShowing;
    }

    public boolean canFaceUnlockWhenOccluded() {
        boolean z = MiuiFaceUnlockUtils.isSupportSlideCamera(sContext) && !MiuiKeyguardUtils.isTopActivitySystemApp(sContext) && !MiuiFaceUnlockUtils.isSCSlideNotOpenCamera(sContext);
        if ((!this.mUpdateMonitor.isBouncerShowing() || MiuiKeyguardUtils.isTopActivityCameraApp(sContext)) && !z) {
            return false;
        }
        return true;
    }

    public boolean shouldStartFaceDetectForCamera(int i) {
        boolean isSupportLiftingCamera = MiuiFaceUnlockUtils.isSupportLiftingCamera(sContext);
        if (i != 0 || isSupportLiftingCamera) {
            return (i == 1 && isSupportLiftingCamera) || i == 2;
        }
        return true;
    }

    public void regionChanged() {
        Log.d("miui_face", "onRegionChanged");
        if (Build.IS_INTERNATIONAL_BUILD) {
            if (MiuiFaceUnlockUtils.isSupportFaceUnlock(sContext)) {
                unregisterFaceUnlockContentObserver();
                registerFaceUnlockContentObserver();
            } else {
                deleteFeature("0", (FaceRemoveCallback) null);
                unregisterFaceUnlockContentObserver();
            }
            sFaceUnlockManagerImpl = null;
            sInstance = null;
        }
    }

    public void keyguardOccludedChanged(boolean z) {
        Log.d("miui_face", "onKeyguardOccludedChanged occluded=" + z);
        if (z) {
            stopFaceUnlock();
        } else {
            startFaceUnlock();
        }
    }

    public void startedGoingToSleep(int i) {
        Log.d("miui_face", "onStartedGoingToSleep");
        stopFaceUnlock();
        MiuiFaceUnlockUtils.setScreenTurnOnDelayed(false);
        initFaceUnlock();
        this.mScreenOnDelay = 0;
    }

    public void updateScreenOnDelyTime(String str) {
        if (!"android.policy:POWER".equalsIgnoreCase(str) && !"android.policy:SLIDE".equalsIgnoreCase(str) && !"com.android.systemui:PICK_UP".equalsIgnoreCase(str)) {
            return;
        }
        if (!shouldListenForFaceUnlock() || isStayScreenWhenFaceUnlockSuccess() || !MiuiFaceUnlockUtils.isSupportScreenOnDelayed(sContext) || !isFaceUnlockInited()) {
            MiuiFaceUnlockUtils.setScreenTurnOnDelayed(false);
            this.mScreenOnDelay = 0;
            return;
        }
        MiuiFaceUnlockUtils.setScreenTurnOnDelayed(true);
        Slog.d("miui_face", "face unlock when screen on delayed");
        this.mScreenOnDelay = 550;
    }

    public long getScreenOnDelyTime() {
        return this.mScreenOnDelay;
    }

    public void onKeyguardHide() {
        if (shouldListenForFaceUnlock()) {
            this.mUpdateMonitor.setFaceUnlockMode(0);
            setFaceLocked(false);
            stopFaceUnlock();
            resetFailCount();
            if (MiuiFaceUnlockUtils.isScreenTurnOnDelayed()) {
                this.mFaceUnlockCallback.unblockScreenOn();
                MiuiFaceUnlockUtils.setScreenTurnOnDelayed(false);
            }
        }
    }

    public void printFaceUnlockTime() {
        if (!isStayScreenWhenFaceUnlockSuccess() && !((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock()) {
            Slog.i("miui_face", "face unlock success time=" + (System.currentTimeMillis() - KeyguardUpdateMonitor.sScreenTurnedOnTime));
        }
    }

    public boolean shouldListenForFaceUnlock() {
        return MiuiFaceUnlockUtils.isSupportFaceUnlock(sContext) && MiuiFaceUnlockUtils.isFaceFeatureEnabled(sContext) && MiuiFaceUnlockUtils.hasEnrolledFaces(sContext) && isFaceUnlockApplyForKeyguard() && KeyguardUpdateMonitor.isOwnerUser();
    }

    public boolean shouldShowFaceUnlockRetryMessageInBouncer() {
        return !isFaceUnlockStarted() && shouldStartFaceUnlock();
    }

    public boolean shouldStartFaceUnlock() {
        return (!isWakeupByNotification() || this.mFaceUnlockStartByNotificationScreenOn) && !isShowingChargeAnimationWindow() && shouldListenForFaceUnlock() && !this.mUpdateMonitor.isSwitchingUser() && this.mUpdateMonitor.isKeyguardShowing() && this.mUpdateMonitor.isDeviceInteractive() && (!this.mUpdateMonitor.isKeyguardOccluded() || canFaceUnlockWhenOccluded()) && MiuiFaceUnlockUtils.isSlideCoverOpened(sContext) && !this.mUpdateMonitor.isKeyguardHide() && !this.mUpdateMonitor.isSimPinSecure() && !isFaceLocked() && !this.mUpdateMonitor.mustPasswordUnlockDevice() && !this.mUpdateMonitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser());
    }

    public void printStartFaceUnlockFailLog(int i) {
        if (!shouldListenForFaceUnlock()) {
            Slog.e("miui_face", "start face unlock  fail:;isSupportFaceUnlock=" + MiuiFaceUnlockUtils.isSupportFaceUnlock(sContext) + ";isFaceFeatureEnabled=" + MiuiFaceUnlockUtils.isFaceFeatureEnabled(sContext) + ";hasEnrolledFaces=" + MiuiFaceUnlockUtils.hasEnrolledFaces(sContext) + ";isFaceUnlockApplyForKeyguard=" + isFaceUnlockApplyForKeyguard() + ";isOwnerUser=" + KeyguardUpdateMonitor.isOwnerUser());
        } else if (!shouldStartFaceDetectForCamera(i)) {
            Slog.e("miui_face", "start face unlock fail ,faceDetectTypeForCamera=" + i);
        } else if (isWakeupByNotification() && !this.mFaceUnlockStartByNotificationScreenOn) {
            Slog.e("miui_face", "wake up by notificaiton but start face unlock not checked");
        } else if (!this.mUpdateMonitor.isKeyguardShowing() || !this.mUpdateMonitor.isDeviceInteractive()) {
            Slog.e("miui_face", "start face unlock fail, mKeyguardShowing=" + this.mUpdateMonitor.isKeyguardShowing() + ";isDeviceInteractive=" + this.mUpdateMonitor.isDeviceInteractive());
        } else if (this.mUpdateMonitor.isKeyguardOccluded() && !canFaceUnlockWhenOccluded()) {
            Slog.e("miui_face", "start face unlock fail, mKeyguardOccluded=" + this.mUpdateMonitor.isKeyguardOccluded() + ";canFaceUnlockWhenOccluded=" + canFaceUnlockWhenOccluded());
        } else if (this.mUpdateMonitor.mustPasswordUnlockDevice()) {
            Slog.e("miui_face", "start face unlock fail because password status not enable");
        } else if (isFaceLocked()) {
            Slog.e("miui_face", "start face unlock fail because is locked");
        } else if (this.mUpdateMonitor.isFingerprintUnlock() || this.mUpdateMonitor.isFaceUnlock() || this.mUpdateMonitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser())) {
            Slog.e("miui_face", "start face unlock fail because device has unlocked");
        } else {
            Slog.e("miui_face", "start face unlock  fail:;mChargeAnimationWindowShowing=" + isShowingChargeAnimationWindow() + ";mSwitchingUser=" + this.mUpdateMonitor.isSwitchingUser() + ";mFaceAuthStarted=" + isFaceUnlockStarted() + ";isSlideCoverOpened=" + MiuiFaceUnlockUtils.isSlideCoverOpened(sContext) + ";isKeyguardHide=" + this.mUpdateMonitor.isKeyguardHide() + ";isSimPinSecure=" + this.mUpdateMonitor.isSimPinSecure());
        }
    }

    private void sendFaceUnlockUpdates(FaceUnlockCallback faceUnlockCallback) {
        faceUnlockCallback.onFaceEnableChange(shouldListenForFaceUnlock(), isStayScreenWhenFaceUnlockSuccess());
    }

    public void registerFaceUnlockCallback(FaceUnlockCallback faceUnlockCallback) {
        int i = 0;
        while (i < this.mCallbacks.size()) {
            if (this.mCallbacks.get(i).get() != faceUnlockCallback) {
                i++;
            } else {
                return;
            }
        }
        this.mCallbacks.add(new WeakReference(faceUnlockCallback));
        removeFaceUnlockCallback((FaceUnlockCallback) null);
        sendFaceUnlockUpdates(faceUnlockCallback);
    }

    public void removeFaceUnlockCallback(FaceUnlockCallback faceUnlockCallback) {
        for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
            if (this.mCallbacks.get(size).get() == faceUnlockCallback) {
                this.mCallbacks.remove(size);
            }
        }
    }
}
