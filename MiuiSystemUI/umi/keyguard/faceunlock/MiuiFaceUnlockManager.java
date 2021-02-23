package com.android.keyguard.faceunlock;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.biometrics.BiometricSourceType;
import android.hardware.face.Face;
import android.hardware.face.FaceManager;
import android.hardware.miuiface.BaseMiuiFaceManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import miui.os.Build;

public class MiuiFaceUnlockManager {
    private final ArrayList<WeakReference<FaceUnlockCallback>> mCallbacks = new ArrayList<>();
    /* access modifiers changed from: private */
    public Context mContext;
    private boolean mDisableLockScreenFaceUnlockAnim = false;
    private int mFaceDetectTypeForCamera = 0;
    private int mFaceFailConunt;
    /* access modifiers changed from: private */
    public boolean mFaceLockedOut;
    private BaseMiuiFaceManager mFaceManager;
    /* access modifiers changed from: private */
    public FaceRemoveCallback mFaceRemoveCallback;
    /* access modifiers changed from: private */
    public boolean mFaceUnlockApplyForKeyguard;
    ContentObserver mFaceUnlockApplyForKeyguardObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            super.onChange(z);
            MiuiFaceUnlockManager miuiFaceUnlockManager = MiuiFaceUnlockManager.this;
            boolean z2 = false;
            if (Settings.Secure.getIntForUser(miuiFaceUnlockManager.mContext.getContentResolver(), "face_unlcok_apply_for_lock", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0) {
                z2 = true;
            }
            boolean unused = miuiFaceUnlockManager.mFaceUnlockApplyForKeyguard = z2;
        }
    };
    /* access modifiers changed from: private */
    public boolean mFaceUnlockStartByNotificationScreenOn;
    ContentObserver mFaceUnlockStartByNotificationScreenOnObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            super.onChange(z);
            MiuiFaceUnlockManager miuiFaceUnlockManager = MiuiFaceUnlockManager.this;
            boolean z2 = false;
            if (Settings.Secure.getIntForUser(miuiFaceUnlockManager.mContext.getContentResolver(), "face_unlock_by_notification_screen_on", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0) {
                z2 = true;
            }
            boolean unused = miuiFaceUnlockManager.mFaceUnlockStartByNotificationScreenOn = z2;
        }
    };
    /* access modifiers changed from: private */
    public boolean mFaceUnlockSuccessShowMessage;
    ContentObserver mFaceUnlockSuccessShowMessageObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            super.onChange(z);
            MiuiFaceUnlockManager miuiFaceUnlockManager = MiuiFaceUnlockManager.this;
            boolean z2 = false;
            if (Settings.Secure.getIntForUser(miuiFaceUnlockManager.mContext.getContentResolver(), "face_unlock_success_show_message", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0) {
                z2 = true;
            }
            boolean unused = miuiFaceUnlockManager.mFaceUnlockSuccessShowMessage = z2;
        }
    };
    /* access modifiers changed from: private */
    public boolean mFaceUnlockSuccessStayScreen;
    ContentObserver mFaceUnlockSuccessStayScreenObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            super.onChange(z);
            MiuiFaceUnlockManager miuiFaceUnlockManager = MiuiFaceUnlockManager.this;
            boolean z2 = false;
            if (Settings.Secure.getIntForUser(miuiFaceUnlockManager.mContext.getContentResolver(), "face_unlock_success_stay_screen", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0) {
                z2 = true;
            }
            boolean unused = miuiFaceUnlockManager.mFaceUnlockSuccessStayScreen = z2;
        }
    };
    private final ArrayList<WeakReference<MiuiKeyguardFaceUnlockView>> mFaceViewList = new ArrayList<>();
    protected HandlerThread mHandlerThread = new HandlerThread("face_unlock");
    protected boolean mHasFace;
    /* access modifiers changed from: private */
    public boolean mKeyguardShowing;
    private MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
        public void onRegionChanged() {
            Log.d("miui_face", "onRegionChanged");
            if (Build.IS_INTERNATIONAL_BUILD && !MiuiFaceUnlockUtils.isHardwareDetected(MiuiFaceUnlockManager.this.mContext)) {
                MiuiFaceUnlockManager.this.deleteFeature("0", (FaceRemoveCallback) null);
            }
        }

        public void onChargeAnimationShowingChanged(boolean z) {
            MiuiFaceUnlockManager.this.mUpdateMonitor.requestFaceAuth();
        }

        public void onStartedWakingUpWithReason(String str) {
            if (!"android.policy:POWER".equalsIgnoreCase(str) && !"com.android.systemui:PICK_UP".equalsIgnoreCase(str)) {
                return;
            }
            if (!MiuiFaceUnlockManager.this.isFaceAuthEnabled() || MiuiFaceUnlockManager.this.isStayScreenWhenFaceUnlockSuccess() || !MiuiFaceUnlockUtils.isSupportScreenOnDelayed(MiuiFaceUnlockManager.this.mContext) || !MiuiFaceUnlockManager.this.isFaceUnlockInited()) {
                MiuiFaceUnlockUtils.setScreenTurnOnDelayed(false);
                long unused = MiuiFaceUnlockManager.this.mScreenOnDelay = 0;
                return;
            }
            MiuiFaceUnlockUtils.setScreenTurnOnDelayed(true);
            Slog.d("miui_face", "face unlock when screen on delayed");
            long unused2 = MiuiFaceUnlockManager.this.mScreenOnDelay = 550;
        }

        public void onBiometricHelp(int i, String str, BiometricSourceType biometricSourceType) {
            if (i != 5 && i != 10001) {
                MiuiFaceUnlockManager.this.mHasFace = true;
            }
        }

        public void onBiometricError(int i, String str, BiometricSourceType biometricSourceType) {
            if (biometricSourceType == BiometricSourceType.FACE) {
                if (i == 7 || i == 9) {
                    boolean unused = MiuiFaceUnlockManager.this.mFaceLockedOut = true;
                }
                MiuiFaceUnlockManager miuiFaceUnlockManager = MiuiFaceUnlockManager.this;
                if (miuiFaceUnlockManager.mHasFace) {
                    miuiFaceUnlockManager.handleFaceDetectError();
                }
                MiuiFaceUnlockManager.this.mHasFace = false;
            }
        }

        public void onKeyguardShowingChanged(boolean z) {
            if (MiuiFaceUnlockManager.this.mKeyguardShowing != z && !z && MiuiFaceUnlockManager.this.mUpdateMonitorInjector.isFaceUnlock() && !MiuiFaceUnlockManager.this.isStayScreenWhenFaceUnlockSuccess()) {
                Slog.d("miui_face", "face unlock success and keyguard dismiss");
            }
            boolean unused = MiuiFaceUnlockManager.this.mKeyguardShowing = z;
        }
    };
    FaceManager.RemovalCallback mRemovalCallback = new FaceManager.RemovalCallback() {
        public void onRemovalError(Face face, int i, CharSequence charSequence) {
            Slog.i("miui_face", "mRemovalCallback, onRemovalError code:" + i + " msg:" + charSequence + ";id=" + face.getBiometricId());
            if (MiuiFaceUnlockManager.this.mFaceRemoveCallback != null) {
                MiuiFaceUnlockManager.this.mFaceRemoveCallback.onFailed();
            }
        }

        public void onRemovalSucceeded(Face face, int i) {
            Slog.i("miui_face", "mRemovalCallback, onRemovalSucceeded id=" + face.getBiometricId() + ";remaining=" + i);
            if (MiuiFaceUnlockManager.this.mFaceRemoveCallback != null) {
                MiuiFaceUnlockManager.this.mFaceRemoveCallback.onRemoved();
            }
            MiuiFaceUnlockUtils.resetFaceUnlockSettingValues(MiuiFaceUnlockManager.this.mContext);
        }
    };
    /* access modifiers changed from: private */
    public long mScreenOnDelay;
    private volatile float mScrollProgress;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitorInjector mUpdateMonitorInjector;
    private boolean mWakeupByNotification;
    protected Handler mWorkerHandler;

    public MiuiFaceUnlockManager(Context context) {
        Log.d("miui_face", "MiuiFaceUnlockManager");
        this.mContext = context;
        this.mFaceManager = (BaseMiuiFaceManager) context.getSystemService("miui_face");
        this.mHandlerThread.start();
        this.mWorkerHandler = new Handler(this.mHandlerThread.getLooper());
    }

    public void start() {
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mUpdateMonitorInjector = (KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class);
        this.mUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        registerFaceUnlockContentObserver();
    }

    private void registerFaceUnlockContentObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("face_unlcok_apply_for_lock"), false, this.mFaceUnlockApplyForKeyguardObserver, 0);
        this.mFaceUnlockApplyForKeyguardObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("face_unlock_success_stay_screen"), false, this.mFaceUnlockSuccessStayScreenObserver, 0);
        this.mFaceUnlockSuccessStayScreenObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("face_unlock_success_show_message"), false, this.mFaceUnlockSuccessShowMessageObserver, 0);
        this.mFaceUnlockSuccessShowMessageObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("face_unlock_by_notification_screen_on"), false, this.mFaceUnlockStartByNotificationScreenOnObserver, 0);
        this.mFaceUnlockStartByNotificationScreenOnObserver.onChange(false);
    }

    public boolean isFaceUnlockApplyForKeyguard() {
        return this.mFaceUnlockApplyForKeyguard;
    }

    public boolean isStayScreenWhenFaceUnlockSuccess() {
        return this.mFaceUnlockSuccessStayScreen;
    }

    public boolean isFaceUnlockStartByNotificationScreenOn() {
        return this.mFaceUnlockStartByNotificationScreenOn;
    }

    public boolean isFaceUnlockSuccessAndStayScreen() {
        return ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFaceUnlock() && this.mFaceUnlockSuccessStayScreen;
    }

    public boolean isShowMessageWhenFaceUnlockSuccess() {
        return this.mFaceUnlockApplyForKeyguard && this.mFaceUnlockSuccessStayScreen && this.mFaceUnlockSuccessShowMessage;
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

    public void updateFaceUnlockView() {
        for (int i = 0; i < this.mFaceViewList.size(); i++) {
            MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView = (MiuiKeyguardFaceUnlockView) this.mFaceViewList.get(i).get();
            if (miuiKeyguardFaceUnlockView != null) {
                miuiKeyguardFaceUnlockView.updateFaceUnlockIconStatus();
            }
        }
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

    private void sendFaceUnlockUpdates(FaceUnlockCallback faceUnlockCallback) {
        faceUnlockCallback.onFaceEnableChange(isFaceAuthEnabled(), isStayScreenWhenFaceUnlockSuccess());
    }

    public void removeFaceUnlockCallback(FaceUnlockCallback faceUnlockCallback) {
        for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
            if (this.mCallbacks.get(size).get() == faceUnlockCallback) {
                this.mCallbacks.remove(size);
            }
        }
    }

    private void runOnFaceUnlockWorkerThread(Runnable runnable) {
        HandlerThread handlerThread = this.mHandlerThread;
        if (handlerThread != null && this.mWorkerHandler != null) {
            if (handlerThread.getThreadId() == Process.myTid()) {
                runnable.run();
            } else {
                this.mWorkerHandler.post(runnable);
            }
        }
    }

    public void initAll() {
        if (!((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).userNeedsStrongAuth() && isFaceAuthEnabled()) {
            runOnFaceUnlockWorkerThread(new Runnable() {
                public final void run() {
                    MiuiFaceUnlockManager.this.lambda$initAll$0$MiuiFaceUnlockManager();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initAll$0 */
    public /* synthetic */ void lambda$initAll$0$MiuiFaceUnlockManager() {
        this.mFaceManager.preInitAuthen();
    }

    public boolean isFaceUnlockInited() {
        return this.mFaceManager.isFaceUnlockInited();
    }

    public void deleteFeature(String str, FaceRemoveCallback faceRemoveCallback) {
        Slog.i("miui_face", "deleteFeature faceId=" + str);
        this.mFaceRemoveCallback = faceRemoveCallback;
        this.mFaceManager.remove(new Face((CharSequence) null, Integer.parseInt(str), 0), 0, this.mRemovalCallback);
    }

    public boolean shouldStartFaceDetectForCamera() {
        boolean isSupportLiftingCamera = MiuiFaceUnlockUtils.isSupportLiftingCamera(this.mContext);
        if (this.mFaceDetectTypeForCamera != 0 || isSupportLiftingCamera) {
            return (this.mFaceDetectTypeForCamera == 1 && isSupportLiftingCamera && this.mScrollProgress == 0.0f) || this.mFaceDetectTypeForCamera == 2;
        }
        return true;
    }

    public boolean isFaceAuthEnabled() {
        if (MiuiFaceUnlockUtils.isHardwareDetected(this.mContext) && MiuiFaceUnlockUtils.isFaceFeatureEnabled(this.mContext) && MiuiFaceUnlockUtils.hasEnrolledTemplates(this.mContext) && isFaceUnlockApplyForKeyguard()) {
            KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
            if (KeyguardUpdateMonitor.getCurrentUser() == 0) {
                return true;
            }
        }
        return false;
    }

    public void updateHorizontalMoveLeftProgress(float f) {
        this.mScrollProgress = f;
    }

    public void setWakeupByNotification(boolean z) {
        this.mWakeupByNotification = z;
    }

    public boolean isWakeupByNotification() {
        return this.mWakeupByNotification;
    }

    public void disableLockScreenFaceUnlockAnim(boolean z) {
        if (z != this.mDisableLockScreenFaceUnlockAnim) {
            this.mDisableLockScreenFaceUnlockAnim = z;
            if (!((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isBouncerShowing()) {
                updateFaceUnlockView();
            }
        }
    }

    public boolean isDisableLockScreenFaceUnlockAnim() {
        return this.mDisableLockScreenFaceUnlockAnim;
    }

    public void startedGoingToSleep(int i) {
        MiuiFaceUnlockUtils.setScreenTurnOnDelayed(false);
        initAll();
        this.mScreenOnDelay = 0;
    }

    public long getScreenOnDelyTime() {
        return this.mScreenOnDelay;
    }

    public void updateFaceDetectTypeForCamera(int i) {
        this.mFaceDetectTypeForCamera = i;
    }

    public void onKeyguardHide() {
        this.mFaceLockedOut = false;
        this.mFaceFailConunt = 0;
        if (isFaceAuthEnabled()) {
            this.mUpdateMonitor.cancelFaceAuth();
        }
    }

    /* access modifiers changed from: private */
    public void handleFaceDetectError() {
        int i = this.mFaceFailConunt + 1;
        this.mFaceFailConunt = i;
        if (!this.mFaceLockedOut && i >= 5 && !MiuiFaceUnlockUtils.isSupportTeeFaceunlock()) {
            this.mUpdateMonitor.handleReeFaceLockout();
            for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
                ((FaceUnlockCallback) this.mCallbacks.get(i2).get()).onFaceAuthLocked();
            }
        }
    }

    public boolean isFaceTemporarilyLockout() {
        return this.mFaceLockedOut;
    }

    public boolean shouldShowFaceUnlockRetryMessageInBouncer() {
        return !((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFaceDetectionRunning() && isFaceAuthEnabled() && ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).shouldListenForFace();
    }

    public void printCannotListenFaceLog(boolean z, boolean z2) {
        KeyguardUpdateMonitorInjector keyguardUpdateMonitorInjector;
        if (this.mUpdateMonitor != null && (keyguardUpdateMonitorInjector = this.mUpdateMonitorInjector) != null && !keyguardUpdateMonitorInjector.isFaceUnlock() && !this.mUpdateMonitorInjector.isFingerprintUnlock() && !z2) {
            if (this.mUpdateMonitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser())) {
                Slog.d("miui_face", "start face unlock fail,device can skip bouncer");
            } else if (!z) {
                Slog.d("miui_face", "start face unlock fail,strongauth not allow scanning");
            } else if (isWakeupByNotification() && !isFaceUnlockStartByNotificationScreenOn()) {
                Slog.e("miui_face", "wake up by notificaiton but start face unlock not checked");
            } else if (this.mUpdateMonitorInjector.isChargeAnimationShowing()) {
                Slog.e("miui_face", "start face unlock fail charge animation showing");
            } else if (this.mUpdateMonitorInjector.isKeyguardOccluded() && (!this.mUpdateMonitor.isBouncerShowing() || MiuiKeyguardUtils.isTopActivityCameraApp(this.mContext))) {
                Slog.e("miui_face", "start face unlock fail, isBouncerShowing=" + this.mUpdateMonitor.isBouncerShowing() + ";isTopActivityCameraApp=" + MiuiKeyguardUtils.isTopActivityCameraApp(this.mContext));
            } else if (this.mFaceLockedOut) {
                Slog.e("miui_face", "start face unlock fail because is locked");
            } else if (KeyguardUpdateMonitor.getCurrentUser() != 0) {
                Slog.e("miui_face", "start face unlock fail because is not PrimaryUser");
            } else if (this.mUpdateMonitor.userNeedsStrongAuth()) {
                Slog.e("miui_face", "start face unlock fail because user nedd strong auth");
            } else if (this.mUpdateMonitorInjector.isSimLocked()) {
                Slog.e("miui_face", "start face unlock fail because sim locked");
            } else if (this.mUpdateMonitor.isSimPinSecure()) {
                Slog.e("miui_face", "start face unlock fail because sim pin secure");
            } else if (MiuiKeyguardUtils.isLargeScreen(this.mContext)) {
                Slog.e("miui_face", "start face unlock fail because in large screen");
            } else {
                Slog.e("miui_face", "start face unlock fail, mKeyguardShowing =" + this.mUpdateMonitorInjector.isKeyguardShowing() + ";isDeviceInteractive =" + this.mUpdateMonitor.isDeviceInteractive() + ";isSwitchingUser =" + this.mUpdateMonitor.isSwitchingUser() + ";isKeyguardGoingAway =" + z2);
            }
        }
    }

    public void printUnlockWithFaceImPossibleLog() {
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mUpdateMonitorInjector = (KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class);
        if (!this.mUpdateMonitor.isFaceAuthEnabledForUser(KeyguardUpdateMonitor.getCurrentUser())) {
            StringBuilder sb = new StringBuilder();
            sb.append("start face unlock fail:;isSupportFaceUnlock=");
            sb.append(MiuiFaceUnlockUtils.isHardwareDetected(this.mContext));
            sb.append(";isFaceFeatureEnabled=");
            sb.append(MiuiFaceUnlockUtils.isFaceFeatureEnabled(this.mContext));
            sb.append(";hasEnrolledFaces=");
            sb.append(MiuiFaceUnlockUtils.hasEnrolledTemplates(this.mContext));
            sb.append(";isFaceUnlockApplyForKeyguard=");
            sb.append(isFaceUnlockApplyForKeyguard());
            sb.append(";isOwnerUser=");
            sb.append(KeyguardUpdateMonitor.getCurrentUser() == 0);
            Slog.e("miui_face", sb.toString());
        } else if (this.mUpdateMonitor.isSimPinSecure()) {
            Slog.e("miui_face", "start face unlock fail simPinSecure");
        } else {
            Slog.e("miui_face", "start face unlock fail KEYGUARD_DISABLE_FACE");
        }
    }
}
