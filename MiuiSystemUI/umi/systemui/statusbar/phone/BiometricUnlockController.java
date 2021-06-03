package com.android.systemui.statusbar.phone;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricSourceType;
import android.metrics.LogMaker;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.internal.util.LatencyTracker;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardViewController;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.MiuiKeyguardFingerprintUtils$FingerprintIdentificationState;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.MiuiWallpaperClient;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class BiometricUnlockController extends MiuiKeyguardUpdateMonitorCallback implements Dumpable {
    private static final UiEventLogger UI_EVENT_LOGGER = new UiEventLoggerImpl();
    private int mAuthenticatedUserId;
    private BiometricManager mBiometricManager = null;
    private BiometricSourceType mBiometricSourceType;
    private final Context mContext;
    private final DozeParameters mDozeParameters;
    private DozeScrimController mDozeScrimController;
    private boolean mFadedAwayAfterWakeAndUnlock;
    private MiuiKeyguardFingerprintUtils$FingerprintIdentificationState mFpiState;
    private final Handler mHandler;
    protected boolean mHasFace;
    private boolean mHasScreenTurnedOnSinceAuthenticating;
    private final KeyguardBypassController mKeyguardBypassController;
    private final KeyguardStateController mKeyguardStateController;
    private KeyguardViewController mKeyguardViewController;
    private KeyguardViewMediator mKeyguardViewMediator;
    private final NotificationMediaManager mMediaManager;
    private final MetricsLogger mMetricsLogger;
    private int mMode;
    private final NotificationShadeWindowController mNotificationShadeWindowController;
    private PendingAuthenticated mPendingAuthenticated = null;
    private boolean mPendingShowBouncer;
    private final PowerManager mPowerManager;
    private final Runnable mReleaseBiometricWakeLockRunnable = new Runnable() {
        /* class com.android.systemui.statusbar.phone.BiometricUnlockController.AnonymousClass1 */

        public void run() {
            Log.i("BiometricUnlockCtrl", "biometric wakelock: TIMEOUT!!");
            BiometricUnlockController.this.releaseBiometricWakeLock();
        }
    };
    private final ScreenLifecycle.Observer mScreenObserver = new ScreenLifecycle.Observer() {
        /* class com.android.systemui.statusbar.phone.BiometricUnlockController.AnonymousClass4 */

        @Override // com.android.systemui.keyguard.ScreenLifecycle.Observer
        public void onScreenTurnedOn() {
            BiometricUnlockController.this.mHasScreenTurnedOnSinceAuthenticating = true;
        }
    };
    private ScrimController mScrimController;
    private final ShadeController mShadeController;
    private StatusBar mStatusBar;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private PowerManager.WakeLock mWakeLock;
    private final int mWakeUpDelay;
    @VisibleForTesting
    final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() {
        /* class com.android.systemui.statusbar.phone.BiometricUnlockController.AnonymousClass3 */

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedWakingUp() {
            if (BiometricUnlockController.this.mPendingShowBouncer) {
                BiometricUnlockController.this.showBouncer();
            }
        }
    };
    private volatile String mWakingUpReason;

    /* access modifiers changed from: private */
    public static final class PendingAuthenticated {
        public final BiometricSourceType biometricSourceType;
        public final boolean isStrongBiometric;
        public final int userId;

        PendingAuthenticated(int i, BiometricSourceType biometricSourceType2, boolean z) {
            this.userId = i;
            this.biometricSourceType = biometricSourceType2;
            this.isStrongBiometric = z;
        }
    }

    @VisibleForTesting
    public enum BiometricUiEvent implements UiEventLogger.UiEventEnum {
        BIOMETRIC_FINGERPRINT_SUCCESS(396),
        BIOMETRIC_FINGERPRINT_FAILURE(397),
        BIOMETRIC_FINGERPRINT_ERROR(398),
        BIOMETRIC_FACE_SUCCESS(399),
        BIOMETRIC_FACE_FAILURE(400),
        BIOMETRIC_FACE_ERROR(401),
        BIOMETRIC_IRIS_SUCCESS(402),
        BIOMETRIC_IRIS_FAILURE(403),
        BIOMETRIC_IRIS_ERROR(404);
        
        static final Map<BiometricSourceType, BiometricUiEvent> ERROR_EVENT_BY_SOURCE_TYPE = Map.of(BiometricSourceType.FINGERPRINT, BIOMETRIC_FINGERPRINT_ERROR, BiometricSourceType.FACE, BIOMETRIC_FACE_ERROR, BiometricSourceType.IRIS, BIOMETRIC_IRIS_ERROR);
        static final Map<BiometricSourceType, BiometricUiEvent> FAILURE_EVENT_BY_SOURCE_TYPE = Map.of(BiometricSourceType.FINGERPRINT, BIOMETRIC_FINGERPRINT_FAILURE, BiometricSourceType.FACE, BIOMETRIC_FACE_FAILURE, BiometricSourceType.IRIS, BIOMETRIC_IRIS_FAILURE);
        static final Map<BiometricSourceType, BiometricUiEvent> SUCCESS_EVENT_BY_SOURCE_TYPE = Map.of(BiometricSourceType.FINGERPRINT, BIOMETRIC_FINGERPRINT_SUCCESS, BiometricSourceType.FACE, BIOMETRIC_FACE_SUCCESS, BiometricSourceType.IRIS, BIOMETRIC_IRIS_SUCCESS);
        private final int mId;

        private BiometricUiEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public BiometricUnlockController(Context context, DozeScrimController dozeScrimController, KeyguardViewMediator keyguardViewMediator, ScrimController scrimController, StatusBar statusBar, ShadeController shadeController, NotificationShadeWindowController notificationShadeWindowController, KeyguardStateController keyguardStateController, Handler handler, KeyguardUpdateMonitor keyguardUpdateMonitor, Resources resources, KeyguardBypassController keyguardBypassController, DozeParameters dozeParameters, MetricsLogger metricsLogger, DumpManager dumpManager) {
        this.mContext = context;
        this.mPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
        this.mShadeController = shadeController;
        this.mUpdateMonitor = keyguardUpdateMonitor;
        this.mDozeParameters = dozeParameters;
        keyguardUpdateMonitor.registerCallback(this);
        this.mMediaManager = (NotificationMediaManager) Dependency.get(NotificationMediaManager.class);
        ((WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class)).addObserver(this.mWakefulnessObserver);
        ((ScreenLifecycle) Dependency.get(ScreenLifecycle.class)).addObserver(this.mScreenObserver);
        this.mNotificationShadeWindowController = notificationShadeWindowController;
        this.mDozeScrimController = dozeScrimController;
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mScrimController = scrimController;
        this.mStatusBar = statusBar;
        this.mKeyguardStateController = keyguardStateController;
        this.mHandler = handler;
        this.mWakeUpDelay = resources.getInteger(17694925);
        this.mKeyguardBypassController = keyguardBypassController;
        keyguardBypassController.setUnlockController(this);
        this.mMetricsLogger = metricsLogger;
        dumpManager.registerDumpable(BiometricUnlockController.class.getName(), this);
    }

    public void setKeyguardViewController(KeyguardViewController keyguardViewController) {
        this.mKeyguardViewController = keyguardViewController;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseBiometricWakeLock() {
        if (this.mWakeLock != null) {
            this.mHandler.removeCallbacks(this.mReleaseBiometricWakeLockRunnable);
            Log.i("BiometricUnlockCtrl", "releasing biometric wakelock");
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricAcquired(BiometricSourceType biometricSourceType) {
        Trace.beginSection("BiometricUnlockController#onBiometricAcquired");
        if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
            this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.ACQUIRED;
        }
        releaseBiometricWakeLock();
        if (!this.mUpdateMonitor.isDeviceInteractive()) {
            if (LatencyTracker.isEnabled(this.mContext)) {
                int i = 2;
                if (biometricSourceType == BiometricSourceType.FACE) {
                    i = 6;
                }
                LatencyTracker.getInstance(this.mContext).onActionStart(i);
            }
            this.mWakeLock = this.mPowerManager.newWakeLock(1, "wake-and-unlock:wakelock");
            Trace.beginSection("acquiring wake-and-unlock");
            this.mWakeLock.acquire();
            Trace.endSection();
            Log.i("BiometricUnlockCtrl", "biometric acquired, grabbing biometric wakelock");
            this.mHandler.postDelayed(this.mReleaseBiometricWakeLockRunnable, 15000);
        }
        Trace.endSection();
    }

    private boolean pulsingOrAod() {
        ScrimState state = this.mScrimController.getState();
        return state == ScrimState.AOD || state == ScrimState.PULSING;
    }

    @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
    public void onPreBiometricAuthenticated(int i) {
        Trace.beginSection("FingerprintUnlockController#onPreFingerprintAuthenticated");
        if (this.mUpdateMonitor.isUnlockingWithBiometricAllowed(true) && KeyguardUpdateMonitor.getCurrentUser() == i && !this.mUpdateMonitor.isBouncerShowing() && this.mKeyguardViewMediator.isShowingAndNotOccluded()) {
            ((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).wakeAndFastUnlock("android.policy:FINGERPRINT");
        }
        Trace.endSection();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
        int i2;
        Trace.beginSection("BiometricUnlockController#onBiometricAuthenticated");
        this.mAuthenticatedUserId = i;
        if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
            this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.SUCCEEDED;
        }
        if (biometricSourceType == BiometricSourceType.FACE && MiuiKeyguardUtils.isBroadSideFingerprint() && ((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).isFaceUnlockSuccessAndStayScreen()) {
            this.mUpdateMonitor.updateFingerprintListeningState();
        }
        if (!this.mKeyguardViewMediator.isGoingToShowKeyguard() || MiuiKeyguardUtils.isGxzwSensor()) {
            this.mMetricsLogger.write(new LogMaker(1697).setType(10).setSubtype(toSubtype(biometricSourceType)));
            Optional ofNullable = Optional.ofNullable(BiometricUiEvent.SUCCESS_EVENT_BY_SOURCE_TYPE.get(biometricSourceType));
            UiEventLogger uiEventLogger = UI_EVENT_LOGGER;
            Objects.requireNonNull(uiEventLogger);
            ofNullable.ifPresent(new Consumer(uiEventLogger) {
                /* class com.android.systemui.statusbar.phone.$$Lambda$DYeRSGfkACOmMKintAq15p0aYRA */
                public final /* synthetic */ UiEventLogger f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.log((BiometricUnlockController.BiometricUiEvent) obj);
                }
            });
            if (this.mKeyguardBypassController.onBiometricAuthenticated(biometricSourceType, z)) {
                if (biometricSourceType == BiometricSourceType.FACE) {
                    resetTimeout(this.mContext, null);
                }
                this.mKeyguardViewMediator.userActivity();
                startWakeAndUnlock(biometricSourceType, z, i);
                return;
            }
            Log.d("BiometricUnlockCtrl", "onBiometricAuthenticated aborted by bypass controller");
            return;
        }
        boolean isBiometricAllowedForUser = this.mUpdateMonitor.getStrongAuthTracker().isBiometricAllowedForUser(z, i);
        if (!this.mDozeScrimController.isPulsing() || !isBiometricAllowedForUser) {
            i2 = (isBiometricAllowedForUser || !this.mKeyguardStateController.isMethodSecure()) ? 1 : 0;
        } else {
            i2 = 2;
        }
        ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setFingerprintMode(i2);
        if (!this.mKeyguardViewMediator.isShowing() && KeyguardUpdateMonitor.getCurrentUser() == i && (i2 == 2 || i2 == 1)) {
            Slog.i("miui_keyguard_fingerprint", "Unlock by fingerprint, keyguard is not showing and wake up");
            ((MiuiWallpaperClient) Dependency.get(MiuiWallpaperClient.class)).onKeyguardGoingAway(true, false);
            this.mKeyguardViewMediator.cancelPendingLock();
            recordKeyguardUnlockWay(true);
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.policy:FINGERPRINT");
        } else {
            this.mPendingAuthenticated = new PendingAuthenticated(i, biometricSourceType, z);
        }
        Trace.endSection();
    }

    private void startWakeAndUnlock(BiometricSourceType biometricSourceType, boolean z, int i) {
        int i2;
        this.mMode = calculateMode(biometricSourceType, z);
        Log.d("BiometricUnlockCtrl", "onBiometricAuthenticated userId=" + i + ";getCurrentUser=" + KeyguardUpdateMonitor.getCurrentUser() + ";mMode=" + this.mMode + ";canSwitchUser=" + MiuiKeyguardUtils.canSwitchUser(this.mContext, i));
        if (!(KeyguardUpdateMonitor.getCurrentUser() == i || (i2 = this.mMode) == 3 || i2 == 0 || i2 == 4)) {
            if (!MiuiKeyguardUtils.canSwitchUser(this.mContext, i) || ((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).isFaceUnlockSuccessAndStayScreen()) {
                this.mMode = 3;
            } else {
                if (MiuiKeyguardUtils.isGxzwSensor()) {
                    MiuiGxzwManager.getInstance().onKeyguardHide();
                }
                try {
                    ActivityManagerNative.getDefault().switchUser(i);
                } catch (RemoteException e) {
                    Log.e("BiometricUnlockCtrl", "switchUser failed", e);
                }
            }
        }
        this.mBiometricSourceType = biometricSourceType;
        setUnlockMode(biometricSourceType, this.mMode);
        startWakeAndUnlock(this.mMode);
    }

    public void startWakeAndUnlock(BiometricSourceType biometricSourceType, boolean z) {
        startWakeAndUnlock(calculateMode(biometricSourceType, z));
    }

    public void startWakeAndUnlock(int i) {
        Log.v("BiometricUnlockCtrl", "startWakeAndUnlock(" + i + ")");
        boolean isDeviceInteractive = this.mUpdateMonitor.isDeviceInteractive();
        this.mMode = i;
        if (i == 2 && pulsingOrAod()) {
            this.mNotificationShadeWindowController.setForceDozeBrightness(true);
        }
        boolean z = i == 1 && this.mDozeParameters.getAlwaysOn() && this.mWakeUpDelay > 0;
        $$Lambda$BiometricUnlockController$eARUOiIHQidy4dPvrf3UVu6gsv0 r4 = new Runnable(isDeviceInteractive, z) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$BiometricUnlockController$eARUOiIHQidy4dPvrf3UVu6gsv0 */
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                BiometricUnlockController.this.lambda$startWakeAndUnlock$0$BiometricUnlockController(this.f$1, this.f$2);
            }
        };
        if (!z && this.mMode != 0) {
            r4.run();
        }
        int i2 = this.mMode;
        switch (i2) {
            case 1:
            case 2:
            case 6:
                if (i2 == 2) {
                    Trace.beginSection("MODE_WAKE_AND_UNLOCK_PULSING");
                    this.mMediaManager.updateMediaMetaData(false, true);
                } else if (i2 == 1) {
                    Trace.beginSection("MODE_WAKE_AND_UNLOCK");
                } else {
                    Trace.beginSection("MODE_WAKE_AND_UNLOCK_FROM_DREAM");
                    this.mUpdateMonitor.awakenFromDream();
                }
                this.mNotificationShadeWindowController.setNotificationShadeFocusable(false);
                if (z) {
                    this.mHandler.postDelayed(r4, (long) this.mWakeUpDelay);
                } else {
                    this.mKeyguardViewMediator.onWakeAndUnlocking();
                }
                if (this.mStatusBar.getNavigationBarView() != null) {
                    this.mStatusBar.getNavigationBarView().setWakeAndUnlocking(true);
                }
                recordKeyguardUnlockWay(true);
                Trace.endSection();
                break;
            case 3:
            case 5:
                Trace.beginSection("MODE_UNLOCK_COLLAPSING or MODE_SHOW_BOUNCER");
                if (!isDeviceInteractive) {
                    this.mPendingShowBouncer = true;
                } else {
                    showBouncer();
                }
                Trace.endSection();
                break;
            case 7:
            case 8:
                Trace.beginSection("MODE_DISMISS_BOUNCER or MODE_UNLOCK_FADING");
                if (!((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).isFaceUnlockSuccessAndStayScreen() || this.mKeyguardViewController.bouncerIsOrWillBeShowing() || ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFingerprintUnlock()) {
                    if (this.mMode == 7 && ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFaceUnlock()) {
                        ((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).fastUnlock();
                    }
                    this.mKeyguardViewController.notifyKeyguardAuthenticated(false);
                }
                recordKeyguardUnlockWay(true);
                Trace.endSection();
                break;
        }
        this.mStatusBar.notifyBiometricAuthModeChanged();
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startWakeAndUnlock$0 */
    public /* synthetic */ void lambda$startWakeAndUnlock$0$BiometricUnlockController(boolean z, boolean z2) {
        if (!z) {
            Log.i("BiometricUnlockCtrl", "bio wakelock: Authenticated, waking up...");
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), 4, "android.policy:BIOMETRIC");
        }
        if (z2) {
            this.mKeyguardViewMediator.onWakeAndUnlocking();
        }
        Trace.beginSection("release wake-and-unlock");
        releaseBiometricWakeLock();
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showBouncer() {
        if (this.mMode == 3) {
            this.mKeyguardViewController.showBouncer(false);
        }
        this.mShadeController.animateCollapsePanels(0, true, false, 1.1f);
        this.mPendingShowBouncer = false;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onStartedGoingToSleep(int i) {
        resetMode();
        this.mFadedAwayAfterWakeAndUnlock = false;
        this.mPendingAuthenticated = null;
        ((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).startedGoingToSleep(i);
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onFinishedGoingToSleep(int i) {
        Trace.beginSection("BiometricUnlockController#onFinishedGoingToSleep");
        PendingAuthenticated pendingAuthenticated = this.mPendingAuthenticated;
        if (pendingAuthenticated != null) {
            this.mHandler.post(new Runnable(pendingAuthenticated) {
                /* class com.android.systemui.statusbar.phone.$$Lambda$BiometricUnlockController$WXzEzz1fr3GrmjWXzyYSNPAnvmA */
                public final /* synthetic */ BiometricUnlockController.PendingAuthenticated f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    BiometricUnlockController.this.lambda$onFinishedGoingToSleep$1$BiometricUnlockController(this.f$1);
                }
            });
            this.mPendingAuthenticated = null;
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishedGoingToSleep$1 */
    public /* synthetic */ void lambda$onFinishedGoingToSleep$1$BiometricUnlockController(PendingAuthenticated pendingAuthenticated) {
        onBiometricAuthenticated(pendingAuthenticated.userId, pendingAuthenticated.biometricSourceType, pendingAuthenticated.isStrongBiometric);
    }

    public boolean hasPendingAuthentication() {
        PendingAuthenticated pendingAuthenticated = this.mPendingAuthenticated;
        return pendingAuthenticated != null && this.mUpdateMonitor.isUnlockingWithBiometricAllowed(pendingAuthenticated.isStrongBiometric) && this.mPendingAuthenticated.userId == KeyguardUpdateMonitor.getCurrentUser();
    }

    public int getMode() {
        return this.mMode;
    }

    private int calculateMode(BiometricSourceType biometricSourceType, boolean z) {
        if (biometricSourceType == BiometricSourceType.FACE || biometricSourceType == BiometricSourceType.IRIS) {
            return calculateModeForPassiveAuth(true);
        }
        return calculateModeForFingerprint(z);
    }

    private int calculateModeForFingerprint(boolean z) {
        boolean isBiometricAllowedForUser = this.mUpdateMonitor.getStrongAuthTracker().isBiometricAllowedForUser(z, this.mAuthenticatedUserId);
        this.mUpdateMonitor.isDreaming();
        if (!this.mUpdateMonitor.isDeviceInteractive()) {
            if (!this.mKeyguardViewController.isShowing()) {
                return 4;
            }
            if (this.mDozeScrimController.isPulsing() && isBiometricAllowedForUser) {
                return 2;
            }
            if (isBiometricAllowedForUser || !this.mKeyguardStateController.isMethodSecure()) {
                return 1;
            }
            return 3;
        } else if (!this.mKeyguardViewController.isShowing()) {
            return 0;
        } else {
            if (this.mKeyguardViewController.bouncerIsOrWillBeShowing() && isBiometricAllowedForUser) {
                return 8;
            }
            if (isBiometricAllowedForUser) {
                return 7;
            }
            if (!this.mKeyguardViewController.isBouncerShowing()) {
                return 3;
            }
            return 0;
        }
    }

    private int calculateModeForPassiveAuth(boolean z) {
        boolean isBiometricAllowedForUser = this.mUpdateMonitor.getStrongAuthTracker().isBiometricAllowedForUser(z, this.mAuthenticatedUserId);
        this.mUpdateMonitor.isDreaming();
        boolean bypassEnabled = this.mKeyguardBypassController.getBypassEnabled();
        if (!this.mUpdateMonitor.isDeviceInteractive()) {
            if (!this.mKeyguardViewController.isShowing()) {
                return bypassEnabled ? 1 : 4;
            }
            if (!isBiometricAllowedForUser) {
                return bypassEnabled ? 3 : 0;
            }
            if (this.mDozeScrimController.isPulsing()) {
                if (bypassEnabled) {
                    return 2;
                }
                return 4;
            } else if (bypassEnabled) {
                return 2;
            } else {
                return 4;
            }
        } else if (!this.mKeyguardViewController.isShowing()) {
            return 0;
        } else {
            if (this.mKeyguardViewController.bouncerIsOrWillBeShowing() && isBiometricAllowedForUser) {
                return 8;
            }
            if (isBiometricAllowedForUser) {
                return 7;
            }
            return 3;
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricHelp(int i, String str, BiometricSourceType biometricSourceType) {
        if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
            this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.HELP;
        }
        if (i != 5 && i != 10001) {
            this.mHasFace = true;
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
        if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
            this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.FAILED;
        }
        this.mMetricsLogger.write(new LogMaker(1697).setType(11).setSubtype(toSubtype(biometricSourceType)));
        Optional ofNullable = Optional.ofNullable(BiometricUiEvent.FAILURE_EVENT_BY_SOURCE_TYPE.get(biometricSourceType));
        UiEventLogger uiEventLogger = UI_EVENT_LOGGER;
        Objects.requireNonNull(uiEventLogger);
        ofNullable.ifPresent(new Consumer(uiEventLogger) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$DYeRSGfkACOmMKintAq15p0aYRA */
            public final /* synthetic */ UiEventLogger f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.log((BiometricUnlockController.BiometricUiEvent) obj);
            }
        });
        this.mBiometricSourceType = biometricSourceType;
        recordKeyguardUnlockWay(false);
        cleanup();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricError(int i, String str, BiometricSourceType biometricSourceType) {
        this.mMetricsLogger.write(new LogMaker(1697).setType(15).setSubtype(toSubtype(biometricSourceType)).addTaggedData(1741, Integer.valueOf(i)));
        Optional ofNullable = Optional.ofNullable(BiometricUiEvent.ERROR_EVENT_BY_SOURCE_TYPE.get(biometricSourceType));
        UiEventLogger uiEventLogger = UI_EVENT_LOGGER;
        Objects.requireNonNull(uiEventLogger);
        ofNullable.ifPresent(new Consumer(uiEventLogger) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$DYeRSGfkACOmMKintAq15p0aYRA */
            public final /* synthetic */ UiEventLogger f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.log((BiometricUnlockController.BiometricUiEvent) obj);
            }
        });
        cleanup();
        if ((("miui.policy:FINGERPRINT_DPAD_CENTER".equals(this.mWakingUpReason) || "android.policy:KEY".equals(this.mWakingUpReason)) || this.mFpiState != MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.NONE) && (i == 7 || i == 9)) {
            if (this.mUpdateMonitor.isDeviceInteractive()) {
                showBouncer();
            } else {
                this.mPendingShowBouncer = true;
                this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:GOTO_UNLOCK");
            }
            this.mWakingUpReason = null;
        }
        if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
            this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.ERROR;
        }
        if (biometricSourceType == BiometricSourceType.FACE && i == 3 && this.mHasFace) {
            showBouncer();
            this.mHasFace = false;
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricRunningStateChanged(boolean z, BiometricSourceType biometricSourceType) {
        Log.d("BiometricUnlockCtrl", "onBiometricRunningStateChanged running=" + z);
        if (z && biometricSourceType == BiometricSourceType.FINGERPRINT) {
            this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.NONE;
        }
    }

    private void cleanup() {
        releaseBiometricWakeLock();
    }

    public void startKeyguardFadingAway() {
        this.mHandler.postDelayed(new Runnable() {
            /* class com.android.systemui.statusbar.phone.BiometricUnlockController.AnonymousClass2 */

            public void run() {
                BiometricUnlockController.this.mNotificationShadeWindowController.setForceDozeBrightness(false);
            }
        }, 96);
    }

    public void finishKeyguardFadingAway() {
        if (isWakeAndUnlock()) {
            this.mFadedAwayAfterWakeAndUnlock = true;
        }
        resetMode();
    }

    private void resetMode() {
        this.mMode = 0;
        this.mNotificationShadeWindowController.setForceDozeBrightness(false);
        if (this.mStatusBar.getNavigationBarView() != null) {
            this.mStatusBar.getNavigationBarView().setWakeAndUnlocking(false);
        }
        this.mStatusBar.notifyBiometricAuthModeChanged();
        ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setFaceUnlockMode(this.mMode);
        ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setFingerprintMode(this.mMode);
        MiuiGxzwManager.getInstance().resetGxzwUnlockMode();
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(" BiometricUnlockController:");
        printWriter.print("   mMode=");
        printWriter.println(this.mMode);
        printWriter.print("   mWakeLock=");
        printWriter.println(this.mWakeLock);
    }

    public boolean isWakeAndUnlock() {
        int i = this.mMode;
        return i == 1 || i == 2 || i == 6;
    }

    public boolean unlockedByWakeAndUnlock() {
        return isWakeAndUnlock() || this.mFadedAwayAfterWakeAndUnlock;
    }

    public boolean isBiometricUnlock() {
        int i;
        return isWakeAndUnlock() || (i = this.mMode) == 5 || i == 7;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.phone.BiometricUnlockController$5  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$android$hardware$biometrics$BiometricSourceType;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                android.hardware.biometrics.BiometricSourceType[] r0 = android.hardware.biometrics.BiometricSourceType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                com.android.systemui.statusbar.phone.BiometricUnlockController.AnonymousClass5.$SwitchMap$android$hardware$biometrics$BiometricSourceType = r0
                android.hardware.biometrics.BiometricSourceType r1 = android.hardware.biometrics.BiometricSourceType.FINGERPRINT     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = com.android.systemui.statusbar.phone.BiometricUnlockController.AnonymousClass5.$SwitchMap$android$hardware$biometrics$BiometricSourceType     // Catch:{ NoSuchFieldError -> 0x001d }
                android.hardware.biometrics.BiometricSourceType r1 = android.hardware.biometrics.BiometricSourceType.FACE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = com.android.systemui.statusbar.phone.BiometricUnlockController.AnonymousClass5.$SwitchMap$android$hardware$biometrics$BiometricSourceType     // Catch:{ NoSuchFieldError -> 0x0028 }
                android.hardware.biometrics.BiometricSourceType r1 = android.hardware.biometrics.BiometricSourceType.IRIS     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.BiometricUnlockController.AnonymousClass5.<clinit>():void");
        }
    }

    private int toSubtype(BiometricSourceType biometricSourceType) {
        int i = AnonymousClass5.$SwitchMap$android$hardware$biometrics$BiometricSourceType[biometricSourceType.ordinal()];
        if (i == 1) {
            return 0;
        }
        if (i != 2) {
            return i != 3 ? 3 : 2;
        }
        return 1;
    }

    private void setUnlockMode(BiometricSourceType biometricSourceType, int i) {
        if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
            ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setFingerprintMode(i);
        } else if (biometricSourceType == BiometricSourceType.FACE) {
            ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setFaceUnlockMode(i);
        }
    }

    @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
    public void onStartedWakingUpWithReason(String str) {
        this.mWakingUpReason = str;
    }

    private void resetTimeout(Context context, byte[] bArr) {
        if (this.mBiometricManager == null) {
            this.mBiometricManager = (BiometricManager) context.getSystemService("biometric");
        }
        BiometricManager biometricManager = this.mBiometricManager;
        if (biometricManager != null) {
            biometricManager.resetLockout(bArr);
        }
    }

    private void recordKeyguardUnlockWay(boolean z) {
        BiometricSourceType biometricSourceType = this.mBiometricSourceType;
        if (biometricSourceType == BiometricSourceType.FACE) {
            ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setKeyguardUnlockWay("face", z);
        } else if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
            ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setKeyguardUnlockWay("fp", z);
        }
    }
}
