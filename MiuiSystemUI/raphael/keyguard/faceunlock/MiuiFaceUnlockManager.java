package com.android.keyguard.faceunlock;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.biometrics.CryptoObject;
import android.hardware.miuiface.IMiuiFaceManager;
import android.hardware.miuiface.MiuiFaceFactory;
import android.hardware.miuiface.Miuiface;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.systemui.Dependency;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import miui.os.Build;

public class MiuiFaceUnlockManager {
    IMiuiFaceManager.AuthenticationCallback mAuthenCallback = new IMiuiFaceManager.AuthenticationCallback() {
        public void onAuthenticationSucceeded(Miuiface miuiface) {
            Slog.i("miui_face", " authenCallback, onAuthenticationSucceeded");
            MiuiFaceUnlockManager miuiFaceUnlockManager = MiuiFaceUnlockManager.this;
            miuiFaceUnlockManager.mFaceAuthSucceed = true;
            miuiFaceUnlockManager.mMainHandler.sendEmptyMessage(1003);
            MiuiFaceUnlockManager.this.stopFaceUnlock();
        }

        public void onAuthenticationHelp(int i, CharSequence charSequence) {
            Slog.i("miui_face", " authenCallback, onAuthenticationHelp helpCode:" + i + " helpString:" + charSequence);
            MiuiFaceUnlockManager miuiFaceUnlockManager = MiuiFaceUnlockManager.this;
            miuiFaceUnlockManager.mHelpCode = i;
            miuiFaceUnlockManager.mMainHandler.sendEmptyMessage(1002);
            if (i != 5) {
                MiuiFaceUnlockManager.this.mHasFace = true;
            }
            if (i == 14) {
                MiuiFaceUnlockManager miuiFaceUnlockManager2 = MiuiFaceUnlockManager.this;
                int i2 = miuiFaceUnlockManager2.mLiveAttackValue + 1;
                miuiFaceUnlockManager2.mLiveAttackValue = i2;
                if (i2 >= 3) {
                    miuiFaceUnlockManager2.mLiveAttack = true;
                }
            } else {
                MiuiFaceUnlockManager.this.mLiveAttackValue = 0;
            }
            if (MiuiFaceUnlockUtils.isScreenTurnOnDelayed()) {
                MiuiFaceUnlockManager miuiFaceUnlockManager3 = MiuiFaceUnlockManager.this;
                int i3 = miuiFaceUnlockManager3.mNoFaceDetectedValue + 1;
                miuiFaceUnlockManager3.mNoFaceDetectedValue = i3;
                if (i3 >= 3) {
                    miuiFaceUnlockManager3.mMiuiFaceUnlockCallback.unblockScreenOn();
                    MiuiFaceUnlockUtils.setScreenTurnOnDelayed(false);
                }
            }
        }

        public void onAuthenticationFailed() {
            Slog.i("miui_face", "authenCallback, onAuthenticationFailed");
            MiuiFaceUnlockManager miuiFaceUnlockManager = MiuiFaceUnlockManager.this;
            miuiFaceUnlockManager.mFaceAuthTimeOut = true;
            miuiFaceUnlockManager.mMainHandler.sendEmptyMessage(1005);
            MiuiFaceUnlockManager.this.stopFaceUnlock();
        }

        public void onAuthenticationError(int i, CharSequence charSequence) {
            Slog.i("miui_face", "authenCallback, onAuthenticationError code:" + i + " msg:" + charSequence);
        }
    };
    private final ArrayList<WeakReference<FaceUnlockCallback>> mCallbacks = new ArrayList<>();
    /* access modifiers changed from: private */
    public Context mContext;
    private boolean mDisableLockScreenFaceUnlockAnim = false;
    protected boolean mFaceAuthSucceed;
    protected boolean mFaceAuthTimeOut;
    private int mFaceDetectTypeForCamera = 0;
    private IMiuiFaceManager mFaceManager;
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
    protected int mFailedCount;
    protected HandlerThread mHandlerThread = new HandlerThread("face_unlock");
    protected boolean mHasFace;
    protected int mHelpCode;
    protected boolean mLiveAttack;
    protected int mLiveAttackValue;
    protected Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1002:
                    MiuiFaceUnlockManager.this.mMiuiFaceUnlockCallback.onFaceAuthHelp(MiuiFaceUnlockManager.this.mHelpCode);
                    return;
                case 1003:
                    MiuiFaceUnlockManager.this.mMiuiFaceUnlockCallback.onFaceAuthenticated();
                    return;
                case 1004:
                    MiuiFaceUnlockManager.this.mMiuiFaceUnlockCallback.onFaceAuthFailed();
                    return;
                case 1005:
                    MiuiFaceUnlockManager.this.mMiuiFaceUnlockCallback.onFaceAuthTimeOut(MiuiFaceUnlockManager.this.mHasFace);
                    return;
                case 1006:
                    MiuiFaceUnlockManager.this.mMiuiFaceUnlockCallback.onFaceAuthLocked();
                    return;
                default:
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public FaceUnlockCallback mMiuiFaceUnlockCallback;
    protected int mNoFaceDetectedValue = 0;
    IMiuiFaceManager.RemovalCallback mRemovalCallback = new IMiuiFaceManager.RemovalCallback() {
        public void onRemovalError(Miuiface miuiface, int i, CharSequence charSequence) {
            Slog.i("miui_face", "mRemovalCallback, onRemovalError code:" + i + " msg:" + charSequence + ";id=" + miuiface.getMiuifaceId());
            if (MiuiFaceUnlockManager.this.mFaceRemoveCallback != null) {
                MiuiFaceUnlockManager.this.mFaceRemoveCallback.onFailed();
            }
        }

        public void onRemovalSucceeded(Miuiface miuiface, int i) {
            Slog.i("miui_face", "mRemovalCallback, onRemovalSucceeded id=" + miuiface.getMiuifaceId() + ";remaining=" + i);
            if (MiuiFaceUnlockManager.this.mFaceRemoveCallback != null) {
                MiuiFaceUnlockManager.this.mFaceRemoveCallback.onRemoved();
            }
            MiuiFaceUnlockUtils.resetFaceUnlockSettingValues(MiuiFaceUnlockManager.this.mContext);
        }
    };
    private long mScreenOnDelay;
    private volatile float mScrollProgress;
    private boolean mWakeupByNotification;
    protected Handler mWorkerHandler;

    public MiuiFaceUnlockManager(Context context) {
        this.mContext = context;
        this.mFaceManager = MiuiFaceFactory.getFaceManager(context, 0);
        this.mHandlerThread.start();
        this.mWorkerHandler = new Handler(this.mHandlerThread.getLooper());
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

    public void authenticate(CryptoObject cryptoObject, CancellationSignal cancellationSignal, int i, FaceUnlockCallback faceUnlockCallback, Handler handler, int i2) {
        runOnFaceUnlockWorkerThread(new Runnable(faceUnlockCallback, cancellationSignal) {
            public final /* synthetic */ FaceUnlockCallback f$1;
            public final /* synthetic */ CancellationSignal f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                MiuiFaceUnlockManager.this.lambda$authenticate$1$MiuiFaceUnlockManager(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$authenticate$1 */
    public /* synthetic */ void lambda$authenticate$1$MiuiFaceUnlockManager(FaceUnlockCallback faceUnlockCallback, CancellationSignal cancellationSignal) {
        this.mLiveAttackValue = 0;
        this.mNoFaceDetectedValue = 0;
        this.mHasFace = false;
        this.mLiveAttack = false;
        this.mFaceAuthSucceed = false;
        this.mFaceAuthTimeOut = false;
        this.mMiuiFaceUnlockCallback = faceUnlockCallback;
        this.mFaceManager.authenticate(cancellationSignal, 0, this.mAuthenCallback, this.mWorkerHandler, 5000);
        this.mHelpCode = 10001;
        this.mMainHandler.sendEmptyMessage(1002);
    }

    public void stopFaceUnlock() {
        if (((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFaceDetectionRunning()) {
            Slog.i("miui_face", "stop face unlock");
            if (!this.mFaceAuthSucceed && (this.mHasFace || this.mLiveAttack)) {
                this.mFailedCount++;
            }
            if (this.mFaceAuthTimeOut) {
                this.mMainHandler.sendEmptyMessage(1005);
            }
            if (isFaceUnlockLocked()) {
                this.mMainHandler.sendEmptyMessage(1006);
            }
            if (!this.mFaceAuthSucceed && !this.mFaceAuthTimeOut && !isFaceUnlockLocked()) {
                this.mMainHandler.sendEmptyMessage(1004);
            }
        }
    }

    public void deleteFeature(String str, FaceRemoveCallback faceRemoveCallback) {
        Slog.i("miui_face", "deleteFeature faceId=" + str);
        this.mFaceRemoveCallback = faceRemoveCallback;
        this.mFaceManager = MiuiFaceFactory.getFaceManager(this.mContext, 0);
        this.mFaceManager.remove(new Miuiface((CharSequence) null, 0, Integer.parseInt(str), 0), this.mRemovalCallback);
    }

    public boolean shouldStartFaceDetectForCamera() {
        boolean isSupportLiftingCamera = MiuiFaceUnlockUtils.isSupportLiftingCamera(this.mContext);
        if (this.mFaceDetectTypeForCamera != 0 || isSupportLiftingCamera) {
            return (this.mFaceDetectTypeForCamera == 1 && isSupportLiftingCamera && this.mScrollProgress == 0.0f) || this.mFaceDetectTypeForCamera == 2;
        }
        return true;
    }

    public boolean isFaceAuthEnabled() {
        if (MiuiFaceUnlockUtils.isSupportFaceUnlock(this.mContext) && MiuiFaceUnlockUtils.isFaceFeatureEnabled(this.mContext) && MiuiFaceUnlockUtils.hasEnrolledFaces(this.mContext) && isFaceUnlockApplyForKeyguard()) {
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

    private void resetFailCount() {
        this.mFailedCount = 0;
    }

    public boolean isFaceUnlockLocked() {
        return this.mFailedCount >= 5;
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

    public void keyguardOccludedChanged(boolean z) {
        Log.d("miui_face", "onKeyguardOccludedChanged occluded=" + z);
        if (z) {
            ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).cancelFaceAuth();
        } else if (this.mScrollProgress == 0.0f) {
            ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).requestFaceAuth();
        }
    }

    public void regionChanged() {
        Log.d("miui_face", "onRegionChanged");
        if (Build.IS_INTERNATIONAL_BUILD && !MiuiFaceUnlockUtils.isSupportFaceUnlock(this.mContext)) {
            deleteFeature("0", (FaceRemoveCallback) null);
        }
    }

    public void startedGoingToSleep(int i) {
        Log.d("miui_face", "onStartedGoingToSleep");
        MiuiFaceUnlockUtils.setScreenTurnOnDelayed(false);
        initAll();
        this.mScreenOnDelay = 0;
    }

    public void updateScreenOnDelyTime(String str) {
        if (!"android.policy:POWER".equalsIgnoreCase(str) && !"com.android.systemui:PICK_UP".equalsIgnoreCase(str)) {
            return;
        }
        if (!isFaceAuthEnabled() || isStayScreenWhenFaceUnlockSuccess() || !MiuiFaceUnlockUtils.isSupportScreenOnDelayed(this.mContext) || !isFaceUnlockInited()) {
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

    public void updateFaceDetectTypeForCamera(int i) {
        this.mFaceDetectTypeForCamera = i;
    }

    public void onKeyguardHide() {
        if (isFaceAuthEnabled()) {
            ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setFaceUnlockMode(0);
            stopFaceUnlock();
            resetFailCount();
        }
    }

    public boolean shouldShowFaceUnlockRetryMessageInBouncer() {
        return !((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFaceDetectionRunning() && isFaceAuthEnabled() && ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).shouldListenForFace();
    }
}
