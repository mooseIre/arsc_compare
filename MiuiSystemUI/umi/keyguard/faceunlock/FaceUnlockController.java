package com.android.keyguard.faceunlock;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManagerCompat;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.telephony.IccCardConstants;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.SlideCoverEventManager;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.statusbar.phone.PanelBar;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;

public class FaceUnlockController extends KeyguardUpdateMonitorCallback {
    /* access modifiers changed from: private */
    public final Context mContext;
    private FaceUnlockCallback mFaceUnlockCallback;
    /* access modifiers changed from: private */
    public FaceUnlockManager mFaceUnlockManager;
    /* access modifiers changed from: private */
    public int mHelpCode;
    /* access modifiers changed from: private */
    public KeyguardViewMediator mKeyguardViewMediator;
    private final SparseArray<IccCardConstants.State> mLastSimStates = new SparseArray<>();
    /* access modifiers changed from: private */
    public int mMode;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    private SlideCoverEventManager mSlideCoverEventManager;
    /* access modifiers changed from: private */
    public StatusBar mStatusBar;
    /* access modifiers changed from: private */
    public StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    /* access modifiers changed from: private */
    public StatusBarWindowManager mStatusBarWindowManager;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;

    public FaceUnlockController(Context context, KeyguardViewMediator keyguardViewMediator, StatusBar statusBar) {
        new Handler();
        this.mFaceUnlockCallback = new FaceUnlockCallback() {
            public void onFaceAuthHelp(int i) {
                int unused = FaceUnlockController.this.mHelpCode = i;
            }

            public void onFaceAuthenticated() {
                Trace.beginSection("FingerprintUnlockController#onFaceAuthenticated");
                boolean isDeviceInteractive = FaceUnlockController.this.mUpdateMonitor.isDeviceInteractive();
                FaceUnlockController faceUnlockController = FaceUnlockController.this;
                int unused = faceUnlockController.mMode = faceUnlockController.calculateMode(0);
                PanelBar.LOG(AnonymousClass2.class, "calculateMode userid=0;mode=" + FaceUnlockController.this.mMode);
                if (!isDeviceInteractive) {
                    Log.i("FaceUnlockController", "fp wakelock: Authenticated, waking up...");
                    FaceUnlockController.this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.policy:FACE");
                }
                FaceUnlockController.this.mUpdateMonitor.setFaceUnlockMode(FaceUnlockController.this.mMode);
                AnalyticsHelper.getInstance(FaceUnlockController.this.mContext).recordFaceUnlockEvent(true, 0);
                FingerprintManagerCompat.resetTimeout(FaceUnlockController.this.mContext, (byte[]) null);
                Trace.beginSection("release wake-and-unlock");
                Trace.endSection();
                int access$300 = FaceUnlockController.this.mMode;
                if (access$300 == 3) {
                    Trace.beginSection("MODE_UNLOCK or MODE_SHOW_BOUNCER");
                    if (!isDeviceInteractive) {
                        FaceUnlockController.this.mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
                    }
                    FaceUnlockController.this.mStatusBarKeyguardViewManager.animateCollapsePanels(1.1f);
                    Trace.endSection();
                } else if (access$300 == 5) {
                    Trace.beginSection("MODE_UNLOCK");
                    if (!isDeviceInteractive) {
                        FaceUnlockController.this.mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
                    }
                    if (FaceUnlockController.this.mStatusBar.canPanelBeCollapsed() || (MiuiFaceUnlockUtils.isSupportSlideCamera(FaceUnlockController.this.mContext) && FaceUnlockController.this.mKeyguardViewMediator.isShowingAndOccluded())) {
                        FaceUnlockController.this.mStatusBarWindowManager.setStatusBarFocusable(false);
                        if (!FaceUnlockController.this.mFaceUnlockManager.isStayScreenWhenFaceUnlockSuccess()) {
                            if (((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).fastUnlock()) {
                                Slog.i("miui_face", "face unlock success time =" + (System.currentTimeMillis() - KeyguardUpdateMonitor.sScreenTurnedOnTime));
                            }
                            FaceUnlockController.this.mKeyguardViewMediator.keyguardDone();
                            FaceUnlockController.this.sendFaceUnlcokSucceedBroadcast();
                        }
                        AnalyticsHelper.getInstance(FaceUnlockController.this.mContext).recordUnlockWay("face", true);
                    }
                    Trace.endSection();
                } else if (access$300 == 6) {
                    Trace.beginSection("MODE_DISMISS");
                    FaceUnlockController.this.mStatusBarKeyguardViewManager.notifyKeyguardAuthenticated(false);
                    AnalyticsHelper.getInstance(FaceUnlockController.this.mContext).recordUnlockWay("face", true);
                    Trace.endSection();
                }
                FaceUnlockController.this.mStatusBarWindowManager.setForceDozeBrightness(false);
                FaceUnlockController.this.mStatusBar.notifyFpAuthModeChanged();
                Trace.endSection();
            }

            public void onFaceAuthFailed() {
                AnalyticsHelper.getInstance(FaceUnlockController.this.mContext).recordFaceUnlockEvent(false, FaceUnlockController.this.mHelpCode);
            }

            public void onFaceAuthTimeOut(boolean z) {
                if (z) {
                    FaceUnlockController.this.mStatusBarKeyguardViewManager.animateCollapsePanels(1.1f);
                }
                AnalyticsHelper.getInstance(FaceUnlockController.this.mContext).recordFaceUnlockEvent(false, FaceUnlockController.this.mHelpCode);
            }

            public void onFaceAuthLocked() {
                FaceUnlockController.this.mStatusBarKeyguardViewManager.animateCollapsePanels(1.1f);
                AnalyticsHelper.getInstance(FaceUnlockController.this.mContext).recordFaceUnlockEvent(false, FaceUnlockController.this.mHelpCode);
            }

            public void unblockScreenOn() {
                FaceUnlockController.this.mKeyguardViewMediator.unblockScreenOn();
            }

            public void restartFaceUnlock() {
                FaceUnlockController.this.mFaceUnlockManager.setFaceUnlockStarted(false);
                FaceUnlockController.this.mFaceUnlockManager.startFaceUnlock(2);
            }
        };
        this.mContext = context;
        this.mPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
        FaceUnlockManager instance = FaceUnlockManager.getInstance();
        this.mFaceUnlockManager = instance;
        instance.registerFaceUnlockCallback(this.mFaceUnlockCallback);
        SlideCoverEventManager instance2 = SlideCoverEventManager.getInstance();
        this.mSlideCoverEventManager = instance2;
        instance2.setStatusBar(statusBar);
        KeyguardUpdateMonitor instance3 = KeyguardUpdateMonitor.getInstance(context);
        this.mUpdateMonitor = instance3;
        instance3.registerCallback(this);
        this.mStatusBarWindowManager = (StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class);
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mStatusBar = statusBar;
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    /* access modifiers changed from: private */
    public int calculateMode(int i) {
        boolean isUnlockingWithFingerprintAllowed = this.mUpdateMonitor.isUnlockingWithFingerprintAllowed(i);
        if (!this.mStatusBarKeyguardViewManager.isShowing()) {
            return 0;
        }
        if (this.mStatusBarKeyguardViewManager.isBouncerShowing() && isUnlockingWithFingerprintAllowed) {
            return 6;
        }
        if (isUnlockingWithFingerprintAllowed) {
            return 5;
        }
        return !this.mStatusBarKeyguardViewManager.isBouncerShowing() ? 3 : 0;
    }

    /* access modifiers changed from: private */
    public void sendFaceUnlcokSucceedBroadcast() {
        if (MiuiFaceUnlockUtils.isScreenTurnOnDelayed()) {
            Intent intent = new Intent("com.miui.keyguard.face_unlock_succeed");
            intent.addFlags(603979776);
            this.mContext.sendBroadcastAsUser(intent, new UserHandle(KeyguardUpdateMonitor.getCurrentUser()));
        }
    }

    public void resetFaceUnlockMode() {
        this.mMode = 0;
        this.mUpdateMonitor.setFaceUnlockMode(0);
    }

    public void onRegionChanged() {
        this.mFaceUnlockManager.regionChanged();
    }

    public void onKeyguardOccludedChanged(boolean z) {
        this.mFaceUnlockManager.keyguardOccludedChanged(z);
    }

    public void updateShowingStatus(boolean z) {
        this.mSlideCoverEventManager.updateShowingStatus(z);
    }

    public void onStartedGoingToSleep(int i) {
        this.mFaceUnlockManager.startedGoingToSleep(i);
    }

    public void onStartedWakingUpWithReason(String str) {
        this.mFaceUnlockManager.updateScreenOnDelyTime(str);
    }

    public void onSimStateChanged(int i, int i2, IccCardConstants.State state) {
        boolean z;
        synchronized (this) {
            IccCardConstants.State state2 = this.mLastSimStates.get(i2);
            if (state2 != IccCardConstants.State.PIN_REQUIRED) {
                if (state2 != IccCardConstants.State.PUK_REQUIRED) {
                    z = false;
                    this.mLastSimStates.append(i2, state);
                }
            }
            z = true;
            this.mLastSimStates.append(i2, state);
        }
        int i3 = AnonymousClass3.$SwitchMap$com$android$internal$telephony$IccCardConstants$State[state.ordinal()];
        if (i3 == 1 || i3 == 2) {
            synchronized (this) {
                if (this.mKeyguardViewMediator.isShowing()) {
                    this.mFaceUnlockManager.stopFaceUnlock();
                }
            }
        } else if (i3 == 3) {
            synchronized (this) {
                if (this.mKeyguardViewMediator.isShowing() && z) {
                    this.mFaceUnlockManager.startFaceUnlock();
                }
            }
        }
    }

    /* renamed from: com.android.keyguard.faceunlock.FaceUnlockController$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$IccCardConstants$State;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.android.internal.telephony.IccCardConstants$State[] r0 = com.android.internal.telephony.IccCardConstants.State.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State = r0
                com.android.internal.telephony.IccCardConstants$State r1 = com.android.internal.telephony.IccCardConstants.State.PIN_REQUIRED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$android$internal$telephony$IccCardConstants$State     // Catch:{ NoSuchFieldError -> 0x001d }
                com.android.internal.telephony.IccCardConstants$State r1 = com.android.internal.telephony.IccCardConstants.State.PUK_REQUIRED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$android$internal$telephony$IccCardConstants$State     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.android.internal.telephony.IccCardConstants$State r1 = com.android.internal.telephony.IccCardConstants.State.READY     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.faceunlock.FaceUnlockController.AnonymousClass3.<clinit>():void");
        }
    }
}
