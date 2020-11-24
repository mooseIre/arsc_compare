package com.android.systemui.keyguard;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.hardware.biometrics.BiometricSourceType;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.internal.util.LatencyTracker;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardDisplayManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.KeyguardViewController;
import com.android.keyguard.MiuiBleUnlockHelper;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiSmartCoverHelper;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.faceunlock.MiuiFaceUnlockUtils;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.injector.KeyguardViewMediatorInjector;
import com.android.keyguard.wallpaper.MiuiWallpaperClient;
import com.android.systemui.C0007R$bool;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.InjectionInflationController;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import miui.security.SecurityManager;

public class KeyguardViewMediator extends SystemUI implements Dumpable {
    private static final Intent USER_PRESENT_INTENT = new Intent("android.intent.action.USER_PRESENT").addFlags(606076928);
    /* access modifiers changed from: private */
    public static SparseIntArray mUnlockTrackSimStates = new SparseIntArray();
    private AlarmManager mAlarmManager;
    private boolean mAodShowing;
    private AudioManager mAudioManager;
    /* access modifiers changed from: private */
    public MiuiBleUnlockHelper mBleUnlockHelper;
    private boolean mBootCompleted;
    private boolean mBootSendUserPresent;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
                synchronized (KeyguardViewMediator.this) {
                    boolean unused = KeyguardViewMediator.this.mShuttingDown = true;
                }
            }
        }
    };
    private final BroadcastReceiver mDelayedLockBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD".equals(intent.getAction())) {
                int intExtra = intent.getIntExtra("seq", 0);
                Log.d("KeyguardViewMediator", "received DELAYED_KEYGUARD_ACTION with seq = " + intExtra + ", mDelayedShowingSequence = " + KeyguardViewMediator.this.mDelayedShowingSequence);
                synchronized (KeyguardViewMediator.this) {
                    if (KeyguardViewMediator.this.mDelayedShowingSequence == intExtra) {
                        KeyguardViewMediator.this.doKeyguardLocked((Bundle) null);
                    }
                }
            } else if ("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK".equals(intent.getAction())) {
                int intExtra2 = intent.getIntExtra("seq", 0);
                int intExtra3 = intent.getIntExtra("android.intent.extra.USER_ID", 0);
                if (intExtra3 != 0) {
                    synchronized (KeyguardViewMediator.this) {
                        if (KeyguardViewMediator.this.mDelayedProfileShowingSequence == intExtra2) {
                            KeyguardViewMediator.this.lockProfile(intExtra3);
                        }
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mDelayedProfileShowingSequence;
    /* access modifiers changed from: private */
    public int mDelayedShowingSequence;
    private DeviceConfigProxy mDeviceConfig;
    /* access modifiers changed from: private */
    public boolean mDeviceInteractive;
    private final DismissCallbackRegistry mDismissCallbackRegistry;
    private boolean mDozing;
    private IKeyguardDrawnCallback mDrawnCallback;
    private IKeyguardExitCallback mExitSecureCallback;
    private boolean mExternallyEnabled = true;
    /* access modifiers changed from: private */
    public final FalsingManager mFalsingManager;
    private boolean mGoingToSleep;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler(Looper.myLooper(), (Handler.Callback) null, true) {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    KeyguardViewMediator.this.handleShow((Bundle) message.obj);
                    return;
                case 2:
                    KeyguardViewMediator.this.handleHide();
                    return;
                case 3:
                    KeyguardViewMediator.this.handleReset();
                    return;
                case 4:
                    Trace.beginSection("KeyguardViewMediator#handleMessage VERIFY_UNLOCK");
                    KeyguardViewMediator.this.handleVerifyUnlock();
                    Trace.endSection();
                    return;
                case 5:
                    KeyguardViewMediator.this.handleNotifyFinishedGoingToSleep();
                    return;
                case 6:
                    Trace.beginSection("KeyguardViewMediator#handleMessage NOTIFY_SCREEN_TURNING_ON");
                    KeyguardViewMediator.this.handleNotifyScreenTurningOn((IKeyguardDrawnCallback) message.obj);
                    Trace.endSection();
                    return;
                case 7:
                    Trace.beginSection("KeyguardViewMediator#handleMessage KEYGUARD_DONE");
                    KeyguardViewMediator.this.handleKeyguardDone();
                    Trace.endSection();
                    return;
                case 8:
                    Trace.beginSection("KeyguardViewMediator#handleMessage KEYGUARD_DONE_DRAWING");
                    KeyguardViewMediator.this.handleKeyguardDoneDrawing();
                    Trace.endSection();
                    return;
                case 9:
                    Trace.beginSection("KeyguardViewMediator#handleMessage SET_OCCLUDED");
                    KeyguardViewMediator keyguardViewMediator = KeyguardViewMediator.this;
                    boolean z = true;
                    boolean z2 = message.arg1 != 0;
                    if (message.arg2 == 0) {
                        z = false;
                    }
                    keyguardViewMediator.handleSetOccluded(z2, z);
                    Trace.endSection();
                    return;
                case 10:
                    synchronized (KeyguardViewMediator.this) {
                        KeyguardViewMediator.this.doKeyguardLocked((Bundle) message.obj);
                    }
                    return;
                case 11:
                    DismissMessage dismissMessage = (DismissMessage) message.obj;
                    KeyguardViewMediator.this.handleDismiss(dismissMessage.getCallback(), dismissMessage.getMessage());
                    return;
                case 12:
                    Trace.beginSection("KeyguardViewMediator#handleMessage START_KEYGUARD_EXIT_ANIM");
                    StartKeyguardExitAnimParams startKeyguardExitAnimParams = (StartKeyguardExitAnimParams) message.obj;
                    KeyguardViewMediator.this.handleStartKeyguardExitAnimation(startKeyguardExitAnimParams.startTime, startKeyguardExitAnimParams.fadeoutDuration);
                    KeyguardViewMediator.this.mFalsingManager.onSuccessfulUnlock();
                    Trace.endSection();
                    return;
                case 13:
                    Trace.beginSection("KeyguardViewMediator#handleMessage KEYGUARD_DONE_PENDING_TIMEOUT");
                    Log.w("KeyguardViewMediator", "Timeout while waiting for activity drawn!");
                    Trace.endSection();
                    return;
                case 14:
                    Trace.beginSection("KeyguardViewMediator#handleMessage NOTIFY_STARTED_WAKING_UP");
                    KeyguardViewMediator.this.handleNotifyStartedWakingUp();
                    Trace.endSection();
                    return;
                case 15:
                    Trace.beginSection("KeyguardViewMediator#handleMessage NOTIFY_SCREEN_TURNED_ON");
                    KeyguardViewMediator.this.handleNotifyScreenTurnedOn();
                    Trace.endSection();
                    return;
                case 16:
                    KeyguardViewMediator.this.handleNotifyScreenTurnedOff();
                    return;
                case 17:
                    KeyguardViewMediator.this.handleNotifyStartedGoingToSleep();
                    return;
                case 18:
                    KeyguardViewMediator.this.handleSystemReady();
                    return;
                default:
                    return;
            }
        }
    };
    private Animation mHideAnimation;
    /* access modifiers changed from: private */
    public final Runnable mHideAnimationFinishedRunnable = new Runnable() {
        public final void run() {
            KeyguardViewMediator.this.lambda$new$6$KeyguardViewMediator();
        }
    };
    /* access modifiers changed from: private */
    public boolean mHideAnimationRun = false;
    /* access modifiers changed from: private */
    public boolean mHideAnimationRunning = false;
    private boolean mHiding;
    private boolean mInGestureNavigationMode;
    private boolean mInputRestricted;
    /* access modifiers changed from: private */
    public KeyguardDisplayManager mKeyguardDisplayManager;
    /* access modifiers changed from: private */
    public boolean mKeyguardDonePending = false;
    private final Runnable mKeyguardGoingAwayRunnable = new Runnable() {
        public void run() {
            Trace.beginSection("KeyguardViewMediator.mKeyGuardGoingAwayRunnable");
            Log.d("KeyguardViewMediator", "keyguardGoingAway");
            ((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).keyguardGoingAway();
            int i = (((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).shouldDisableWindowAnimationsForUnlock() || (KeyguardViewMediator.this.mWakeAndUnlocking && !KeyguardViewMediator.this.mPulsing)) ? 2 : 0;
            if (((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).isGoingToNotificationShade() || (KeyguardViewMediator.this.mWakeAndUnlocking && KeyguardViewMediator.this.mPulsing)) {
                i |= 1;
            }
            if (((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).isUnlockWithWallpaper()) {
                i |= 4;
            }
            if (((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).shouldSubtleWindowAnimationsForUnlock()) {
                i |= 8;
            }
            KeyguardViewMediator.this.mUpdateMonitor.setKeyguardGoingAway(true);
            ((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).setKeyguardGoingAwayState(true);
            KeyguardViewMediator.this.mUiBgExecutor.execute(new Runnable(i) {
                public final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    KeyguardViewMediator.AnonymousClass7.lambda$run$0(this.f$0);
                }
            });
            Trace.endSection();
        }

        static /* synthetic */ void lambda$run$0(int i) {
            try {
                ActivityTaskManager.getService().keyguardGoingAway(i);
            } catch (RemoteException e) {
                Log.e("KeyguardViewMediator", "Error while calling WindowManager", e);
            }
        }
    };
    /* access modifiers changed from: private */
    public final ArrayList<IKeyguardStateCallback> mKeyguardStateCallbacks = new ArrayList<>();
    /* access modifiers changed from: private */
    public final Lazy<KeyguardViewController> mKeyguardViewControllerLazy;
    /* access modifiers changed from: private */
    public final SparseIntArray mLastSimStates = new SparseIntArray();
    private boolean mLockLater;
    /* access modifiers changed from: private */
    public final LockPatternUtils mLockPatternUtils;
    private int mLockSoundId;
    private int mLockSoundStreamId;
    private float mLockSoundVolume;
    private SoundPool mLockSounds;
    private boolean mNeedToReshowWhenReenabled = false;
    private boolean mOccluded = false;
    private final DeviceConfig.OnPropertiesChangedListener mOnPropertiesChangedListener = new DeviceConfig.OnPropertiesChangedListener() {
        public void onPropertiesChanged(DeviceConfig.Properties properties) {
            if (properties.getKeyset().contains("nav_bar_handle_show_over_lockscreen")) {
                boolean unused = KeyguardViewMediator.this.mShowHomeOverLockscreen = properties.getBoolean("nav_bar_handle_show_over_lockscreen", false);
            }
        }
    };
    private final PowerManager mPM;
    private boolean mPendingLock;
    private boolean mPendingReset;
    private String mPhoneState = TelephonyManager.EXTRA_STATE_IDLE;
    /* access modifiers changed from: private */
    public boolean mPulsing;
    /* access modifiers changed from: private */
    public boolean mShowHomeOverLockscreen;
    private PowerManager.WakeLock mShowKeyguardWakeLock;
    /* access modifiers changed from: private */
    public boolean mShowing;
    /* access modifiers changed from: private */
    public boolean mShuttingDown;
    private boolean mSimLockedOrMissing;
    private MiuiSmartCoverHelper mSmartCoverHelper;
    private StatusBarManager mStatusBarManager;
    private boolean mSystemReady;
    private final TrustManager mTrustManager;
    private int mTrustedSoundId;
    /* access modifiers changed from: private */
    public final Executor mUiBgExecutor;
    private int mUiSoundsStreamType;
    private int mUnlockSoundId;
    KeyguardUpdateMonitorCallback mUpdateCallback = new MiuiKeyguardUpdateMonitorCallback() {
        public void onUserInfoChanged(int i) {
        }

        public void onUserSwitching(int i) {
            synchronized (KeyguardViewMediator.this) {
                KeyguardViewMediator.this.resetKeyguardDonePendingLocked();
                if (KeyguardViewMediator.this.mLockPatternUtils.isLockScreenDisabled(i)) {
                    KeyguardViewMediator.this.dismiss((IKeyguardDismissCallback) null, (CharSequence) null);
                } else {
                    KeyguardViewMediator.this.resetStateLocked();
                }
                KeyguardViewMediator.this.adjustStatusBarLocked();
            }
        }

        public void onUserSwitchComplete(int i) {
            UserInfo userInfo;
            if (i != 0 && (userInfo = UserManager.get(KeyguardViewMediator.this.mContext).getUserInfo(i)) != null && !KeyguardViewMediator.this.mLockPatternUtils.isSecure(i)) {
                if (userInfo.isGuest() || userInfo.isDemo()) {
                    KeyguardViewMediator.this.dismiss((IKeyguardDismissCallback) null, (CharSequence) null);
                }
            }
        }

        public void onClockVisibilityChanged() {
            KeyguardViewMediator.this.adjustStatusBarLocked();
        }

        public void onDeviceProvisioned() {
            KeyguardViewMediator.this.sendUserPresentBroadcast();
            synchronized (KeyguardViewMediator.this) {
                if (KeyguardViewMediator.this.mustNotUnlockCurrentUser()) {
                    KeyguardViewMediator.this.doKeyguardLocked((Bundle) null);
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:103:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:104:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:105:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:106:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:107:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b4, code lost:
            if (r9 == 1) goto L_0x0160;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b6, code lost:
            if (r9 == 2) goto L_0x013e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x00b8, code lost:
            if (r9 == 3) goto L_0x013e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x00ba, code lost:
            if (r9 == 5) goto L_0x0104;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x00bd, code lost:
            if (r9 == 6) goto L_0x0160;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00c0, code lost:
            if (r9 == 7) goto L_0x00da;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x00c2, code lost:
            android.util.Log.v("KeyguardViewMediator", "Unspecific state: " + r9);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x00da, code lost:
            r8 = r6.this$0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x00dc, code lost:
            monitor-enter(r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x00e3, code lost:
            if (com.android.systemui.keyguard.KeyguardViewMediator.access$1300(r6.this$0) != false) goto L_0x00f2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x00e5, code lost:
            android.util.Log.d("KeyguardViewMediator", "PERM_DISABLED and keygaurd isn't showing.");
            com.android.systemui.keyguard.KeyguardViewMediator.access$700(r6.this$0, (android.os.Bundle) null);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:0x00f2, code lost:
            android.util.Log.d("KeyguardViewMediator", "PERM_DISABLED, resetStateLocked toshow permanently disabled message in lockscreen.");
            com.android.systemui.keyguard.KeyguardViewMediator.access$300(r6.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x00fe, code lost:
            monitor-exit(r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:51:0x0104, code lost:
            r8 = r6.this$0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:52:0x0106, code lost:
            monitor-enter(r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
            android.util.Log.d("KeyguardViewMediator", "READY, reset state? " + com.android.systemui.keyguard.KeyguardViewMediator.access$1300(r6.this$0));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:56:0x0129, code lost:
            if (com.android.systemui.keyguard.KeyguardViewMediator.access$1300(r6.this$0) == false) goto L_0x0139;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:0x012b, code lost:
            if (r0 == false) goto L_0x0139;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:58:0x012d, code lost:
            android.util.Log.d("KeyguardViewMediator", "SIM moved to READY when the previous state was locked. Reset the state.");
            com.android.systemui.keyguard.KeyguardViewMediator.access$300(r6.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:59:0x0139, code lost:
            monitor-exit(r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:64:0x013e, code lost:
            r8 = r6.this$0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:65:0x0140, code lost:
            monitor-enter(r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:68:0x0147, code lost:
            if (com.android.systemui.keyguard.KeyguardViewMediator.access$1300(r6.this$0) != false) goto L_0x0156;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:69:0x0149, code lost:
            android.util.Log.d("KeyguardViewMediator", "INTENT_VALUE_ICC_LOCKED and keygaurd isn't showing; need to show keyguard so user can enter sim pin");
            com.android.systemui.keyguard.KeyguardViewMediator.access$700(r6.this$0, (android.os.Bundle) null);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:70:0x0156, code lost:
            com.android.systemui.keyguard.KeyguardViewMediator.access$300(r6.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:71:0x015b, code lost:
            monitor-exit(r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:76:0x0160, code lost:
            r8 = r6.this$0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:77:0x0162, code lost:
            monitor-enter(r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:80:0x0169, code lost:
            if (com.android.systemui.keyguard.KeyguardViewMediator.access$1200(r6.this$0) == false) goto L_0x0185;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:82:0x0171, code lost:
            if (com.android.systemui.keyguard.KeyguardViewMediator.access$1300(r6.this$0) != false) goto L_0x0180;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:83:0x0173, code lost:
            android.util.Log.d("KeyguardViewMediator", "ICC_ABSENT isn't showing, we need to show the keyguard since the device isn't provisioned yet.");
            com.android.systemui.keyguard.KeyguardViewMediator.access$700(r6.this$0, (android.os.Bundle) null);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:84:0x0180, code lost:
            com.android.systemui.keyguard.KeyguardViewMediator.access$300(r6.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:85:0x0185, code lost:
            if (r9 != 1) goto L_0x0195;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:86:0x0187, code lost:
            if (r0 == false) goto L_0x0195;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:87:0x0189, code lost:
            android.util.Log.d("KeyguardViewMediator", "SIM moved to ABSENT when the previous state was locked. Reset the state.");
            com.android.systemui.keyguard.KeyguardViewMediator.access$300(r6.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:88:0x0195, code lost:
            monitor-exit(r8);
         */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x008e  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x009f  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onSimStateChanged(int r7, int r8, int r9) {
            /*
                r6 = this;
                java.lang.StringBuilder r0 = new java.lang.StringBuilder
                r0.<init>()
                java.lang.String r1 = "onSimStateChanged(subId="
                r0.append(r1)
                r0.append(r7)
                java.lang.String r7 = ", slotId="
                r0.append(r7)
                r0.append(r8)
                java.lang.String r7 = ",state="
                r0.append(r7)
                r0.append(r9)
                java.lang.String r7 = ")"
                r0.append(r7)
                java.lang.String r7 = r0.toString()
                java.lang.String r0 = "KeyguardViewMediator"
                android.util.Log.d(r0, r7)
                com.android.systemui.keyguard.KeyguardViewMediator r7 = com.android.systemui.keyguard.KeyguardViewMediator.this
                java.util.ArrayList r7 = r7.mKeyguardStateCallbacks
                int r7 = r7.size()
                com.android.systemui.keyguard.KeyguardViewMediator r0 = com.android.systemui.keyguard.KeyguardViewMediator.this
                com.android.keyguard.KeyguardUpdateMonitor r0 = r0.mUpdateMonitor
                boolean r0 = r0.isSimPinSecure()
                r1 = 1
                int r7 = r7 - r1
            L_0x0041:
                if (r7 < 0) goto L_0x006b
                com.android.systemui.keyguard.KeyguardViewMediator r2 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ RemoteException -> 0x0053 }
                java.util.ArrayList r2 = r2.mKeyguardStateCallbacks     // Catch:{ RemoteException -> 0x0053 }
                java.lang.Object r2 = r2.get(r7)     // Catch:{ RemoteException -> 0x0053 }
                com.android.internal.policy.IKeyguardStateCallback r2 = (com.android.internal.policy.IKeyguardStateCallback) r2     // Catch:{ RemoteException -> 0x0053 }
                r2.onSimSecureStateChanged(r0)     // Catch:{ RemoteException -> 0x0053 }
                goto L_0x0068
            L_0x0053:
                r2 = move-exception
                java.lang.String r3 = "KeyguardViewMediator"
                java.lang.String r4 = "Failed to call onSimSecureStateChanged"
                android.util.Slog.w(r3, r4, r2)
                boolean r2 = r2 instanceof android.os.DeadObjectException
                if (r2 == 0) goto L_0x0068
                com.android.systemui.keyguard.KeyguardViewMediator r2 = com.android.systemui.keyguard.KeyguardViewMediator.this
                java.util.ArrayList r2 = r2.mKeyguardStateCallbacks
                r2.remove(r7)
            L_0x0068:
                int r7 = r7 + -1
                goto L_0x0041
            L_0x006b:
                com.android.systemui.keyguard.KeyguardViewMediator r7 = com.android.systemui.keyguard.KeyguardViewMediator.this
                monitor-enter(r7)
                com.android.systemui.keyguard.KeyguardViewMediator r0 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x019a }
                android.util.SparseIntArray r0 = r0.mLastSimStates     // Catch:{ all -> 0x019a }
                int r0 = r0.get(r8)     // Catch:{ all -> 0x019a }
                r2 = 3
                r3 = 2
                if (r0 == r3) goto L_0x0081
                if (r0 != r2) goto L_0x007f
                goto L_0x0081
            L_0x007f:
                r0 = 0
                goto L_0x0082
            L_0x0081:
                r0 = r1
            L_0x0082:
                com.android.systemui.keyguard.KeyguardViewMediator r4 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x019a }
                android.util.SparseIntArray r4 = r4.mLastSimStates     // Catch:{ all -> 0x019a }
                r4.append(r8, r9)     // Catch:{ all -> 0x019a }
                r4 = 5
                if (r9 != r4) goto L_0x0095
                android.util.SparseIntArray r5 = com.android.systemui.keyguard.KeyguardViewMediator.mUnlockTrackSimStates     // Catch:{ all -> 0x019a }
                r5.put(r8, r9)     // Catch:{ all -> 0x019a }
            L_0x0095:
                android.util.SparseIntArray r5 = com.android.systemui.keyguard.KeyguardViewMediator.mUnlockTrackSimStates     // Catch:{ all -> 0x019a }
                int r5 = r5.get(r8)     // Catch:{ all -> 0x019a }
                if (r5 != r4) goto L_0x00b2
                if (r9 == r3) goto L_0x00a9
                android.util.SparseIntArray r5 = com.android.systemui.keyguard.KeyguardViewMediator.mUnlockTrackSimStates     // Catch:{ all -> 0x019a }
                r5.put(r8, r9)     // Catch:{ all -> 0x019a }
                goto L_0x00b2
            L_0x00a9:
                java.lang.String r6 = "KeyguardViewMediator"
                java.lang.String r8 = "ship the unnecessary SIM_STATE_PIN_REQUIRED state"
                android.util.Log.e(r6, r8)     // Catch:{ all -> 0x019a }
                monitor-exit(r7)     // Catch:{ all -> 0x019a }
                return
            L_0x00b2:
                monitor-exit(r7)     // Catch:{ all -> 0x019a }
                r7 = 0
                if (r9 == r1) goto L_0x0160
                if (r9 == r3) goto L_0x013e
                if (r9 == r2) goto L_0x013e
                if (r9 == r4) goto L_0x0104
                r8 = 6
                if (r9 == r8) goto L_0x0160
                r8 = 7
                if (r9 == r8) goto L_0x00da
                java.lang.StringBuilder r6 = new java.lang.StringBuilder
                r6.<init>()
                java.lang.String r7 = "Unspecific state: "
                r6.append(r7)
                r6.append(r9)
                java.lang.String r6 = r6.toString()
                java.lang.String r7 = "KeyguardViewMediator"
                android.util.Log.v(r7, r6)
                goto L_0x0196
            L_0x00da:
                com.android.systemui.keyguard.KeyguardViewMediator r8 = com.android.systemui.keyguard.KeyguardViewMediator.this
                monitor-enter(r8)
                com.android.systemui.keyguard.KeyguardViewMediator r9 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x0101 }
                boolean r9 = r9.mShowing     // Catch:{ all -> 0x0101 }
                if (r9 != 0) goto L_0x00f2
                java.lang.String r9 = "KeyguardViewMediator"
                java.lang.String r0 = "PERM_DISABLED and keygaurd isn't showing."
                android.util.Log.d(r9, r0)     // Catch:{ all -> 0x0101 }
                com.android.systemui.keyguard.KeyguardViewMediator r6 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x0101 }
                r6.doKeyguardLocked(r7)     // Catch:{ all -> 0x0101 }
                goto L_0x00fe
            L_0x00f2:
                java.lang.String r7 = "KeyguardViewMediator"
                java.lang.String r9 = "PERM_DISABLED, resetStateLocked toshow permanently disabled message in lockscreen."
                android.util.Log.d(r7, r9)     // Catch:{ all -> 0x0101 }
                com.android.systemui.keyguard.KeyguardViewMediator r6 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x0101 }
                r6.resetStateLocked()     // Catch:{ all -> 0x0101 }
            L_0x00fe:
                monitor-exit(r8)     // Catch:{ all -> 0x0101 }
                goto L_0x0196
            L_0x0101:
                r6 = move-exception
                monitor-exit(r8)     // Catch:{ all -> 0x0101 }
                throw r6
            L_0x0104:
                com.android.systemui.keyguard.KeyguardViewMediator r8 = com.android.systemui.keyguard.KeyguardViewMediator.this
                monitor-enter(r8)
                java.lang.String r7 = "KeyguardViewMediator"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x013b }
                r9.<init>()     // Catch:{ all -> 0x013b }
                java.lang.String r1 = "READY, reset state? "
                r9.append(r1)     // Catch:{ all -> 0x013b }
                com.android.systemui.keyguard.KeyguardViewMediator r1 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x013b }
                boolean r1 = r1.mShowing     // Catch:{ all -> 0x013b }
                r9.append(r1)     // Catch:{ all -> 0x013b }
                java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x013b }
                android.util.Log.d(r7, r9)     // Catch:{ all -> 0x013b }
                com.android.systemui.keyguard.KeyguardViewMediator r7 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x013b }
                boolean r7 = r7.mShowing     // Catch:{ all -> 0x013b }
                if (r7 == 0) goto L_0x0139
                if (r0 == 0) goto L_0x0139
                java.lang.String r7 = "KeyguardViewMediator"
                java.lang.String r9 = "SIM moved to READY when the previous state was locked. Reset the state."
                android.util.Log.d(r7, r9)     // Catch:{ all -> 0x013b }
                com.android.systemui.keyguard.KeyguardViewMediator r6 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x013b }
                r6.resetStateLocked()     // Catch:{ all -> 0x013b }
            L_0x0139:
                monitor-exit(r8)     // Catch:{ all -> 0x013b }
                goto L_0x0196
            L_0x013b:
                r6 = move-exception
                monitor-exit(r8)     // Catch:{ all -> 0x013b }
                throw r6
            L_0x013e:
                com.android.systemui.keyguard.KeyguardViewMediator r8 = com.android.systemui.keyguard.KeyguardViewMediator.this
                monitor-enter(r8)
                com.android.systemui.keyguard.KeyguardViewMediator r9 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x015d }
                boolean r9 = r9.mShowing     // Catch:{ all -> 0x015d }
                if (r9 != 0) goto L_0x0156
                java.lang.String r9 = "KeyguardViewMediator"
                java.lang.String r0 = "INTENT_VALUE_ICC_LOCKED and keygaurd isn't showing; need to show keyguard so user can enter sim pin"
                android.util.Log.d(r9, r0)     // Catch:{ all -> 0x015d }
                com.android.systemui.keyguard.KeyguardViewMediator r6 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x015d }
                r6.doKeyguardLocked(r7)     // Catch:{ all -> 0x015d }
                goto L_0x015b
            L_0x0156:
                com.android.systemui.keyguard.KeyguardViewMediator r6 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x015d }
                r6.resetStateLocked()     // Catch:{ all -> 0x015d }
            L_0x015b:
                monitor-exit(r8)     // Catch:{ all -> 0x015d }
                goto L_0x0196
            L_0x015d:
                r6 = move-exception
                monitor-exit(r8)     // Catch:{ all -> 0x015d }
                throw r6
            L_0x0160:
                com.android.systemui.keyguard.KeyguardViewMediator r8 = com.android.systemui.keyguard.KeyguardViewMediator.this
                monitor-enter(r8)
                com.android.systemui.keyguard.KeyguardViewMediator r2 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x0197 }
                boolean r2 = r2.shouldWaitForProvisioning()     // Catch:{ all -> 0x0197 }
                if (r2 == 0) goto L_0x0185
                com.android.systemui.keyguard.KeyguardViewMediator r2 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x0197 }
                boolean r2 = r2.mShowing     // Catch:{ all -> 0x0197 }
                if (r2 != 0) goto L_0x0180
                java.lang.String r2 = "KeyguardViewMediator"
                java.lang.String r3 = "ICC_ABSENT isn't showing, we need to show the keyguard since the device isn't provisioned yet."
                android.util.Log.d(r2, r3)     // Catch:{ all -> 0x0197 }
                com.android.systemui.keyguard.KeyguardViewMediator r2 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x0197 }
                r2.doKeyguardLocked(r7)     // Catch:{ all -> 0x0197 }
                goto L_0x0185
            L_0x0180:
                com.android.systemui.keyguard.KeyguardViewMediator r7 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x0197 }
                r7.resetStateLocked()     // Catch:{ all -> 0x0197 }
            L_0x0185:
                if (r9 != r1) goto L_0x0195
                if (r0 == 0) goto L_0x0195
                java.lang.String r7 = "KeyguardViewMediator"
                java.lang.String r9 = "SIM moved to ABSENT when the previous state was locked. Reset the state."
                android.util.Log.d(r7, r9)     // Catch:{ all -> 0x0197 }
                com.android.systemui.keyguard.KeyguardViewMediator r6 = com.android.systemui.keyguard.KeyguardViewMediator.this     // Catch:{ all -> 0x0197 }
                r6.resetStateLocked()     // Catch:{ all -> 0x0197 }
            L_0x0195:
                monitor-exit(r8)     // Catch:{ all -> 0x0197 }
            L_0x0196:
                return
            L_0x0197:
                r6 = move-exception
                monitor-exit(r8)     // Catch:{ all -> 0x0197 }
                throw r6
            L_0x019a:
                r6 = move-exception
                monitor-exit(r7)     // Catch:{ all -> 0x019a }
                throw r6
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.keyguard.KeyguardViewMediator.AnonymousClass2.onSimStateChanged(int, int, int):void");
        }

        public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (KeyguardViewMediator.this.mLockPatternUtils.isSecure(currentUser)) {
                KeyguardViewMediator.this.mLockPatternUtils.getDevicePolicyManager().reportFailedBiometricAttempt(currentUser);
            }
        }

        public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
            if (KeyguardViewMediator.this.mLockPatternUtils.isSecure(i)) {
                KeyguardViewMediator.this.mLockPatternUtils.getDevicePolicyManager().reportSuccessfulBiometricAttempt(i);
            }
        }

        public void onTrustChanged(int i) {
            if (i == KeyguardUpdateMonitor.getCurrentUser()) {
                synchronized (KeyguardViewMediator.this) {
                    KeyguardViewMediator.this.notifyTrustedChangedLocked(KeyguardViewMediator.this.mUpdateMonitor.getUserHasTrust(i));
                }
            }
        }

        public void onHasLockscreenWallpaperChanged(boolean z) {
            synchronized (KeyguardViewMediator.this) {
                KeyguardViewMediator.this.notifyHasLockscreenWallpaperChanged(z);
            }
        }

        public void onSimLockedStateChanged(boolean z) {
            if (z) {
                KeyguardViewMediator.this.doKeyguardLocked((Bundle) null);
            }
        }
    };
    /* access modifiers changed from: private */
    public final KeyguardUpdateMonitor mUpdateMonitor;
    ViewMediatorCallback mViewMediatorCallback = new ViewMediatorCallback() {
        public void userActivity() {
            KeyguardViewMediator.this.userActivity();
        }

        public void keyguardDone(boolean z, int i) {
            if (i == ActivityManager.getCurrentUser()) {
                Log.d("KeyguardViewMediator", "keyguardDone");
                KeyguardViewMediator.this.tryKeyguardDone();
                if (z) {
                    ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).reportSuccessfulStrongAuthUnlockAttempt();
                }
            }
        }

        public void keyguardDoneDrawing() {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#keyguardDoneDrawing");
            KeyguardViewMediator.this.mHandler.sendEmptyMessage(8);
            Trace.endSection();
        }

        public void setNeedsInput(boolean z) {
            ((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).setNeedsInput(z);
        }

        public void keyguardDonePending(boolean z, int i) {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#keyguardDonePending");
            Log.d("KeyguardViewMediator", "keyguardDonePending");
            if (i != ActivityManager.getCurrentUser()) {
                Trace.endSection();
                return;
            }
            boolean unused = KeyguardViewMediator.this.mKeyguardDonePending = true;
            boolean unused2 = KeyguardViewMediator.this.mHideAnimationRun = true;
            boolean unused3 = KeyguardViewMediator.this.mHideAnimationRunning = true;
            ((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).startPreHideAnimation(KeyguardViewMediator.this.mHideAnimationFinishedRunnable);
            KeyguardViewMediator.this.mHandler.sendEmptyMessageDelayed(13, 3000);
            Trace.endSection();
        }

        public void keyguardGone() {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#keyguardGone");
            Log.d("KeyguardViewMediator", "keyguardGone");
            ((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).setKeyguardGoingAwayState(false);
            KeyguardViewMediator.this.mKeyguardDisplayManager.hide();
            Trace.endSection();
        }

        public void readyForKeyguardDone() {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#readyForKeyguardDone");
            if (KeyguardViewMediator.this.mKeyguardDonePending) {
                boolean unused = KeyguardViewMediator.this.mKeyguardDonePending = false;
                KeyguardViewMediator.this.tryKeyguardDone();
            }
            Trace.endSection();
        }

        public void resetKeyguard() {
            KeyguardViewMediator.this.resetStateLocked();
        }

        public void onBouncerVisiblityChanged(boolean z) {
            synchronized (KeyguardViewMediator.this) {
                KeyguardViewMediator.this.adjustStatusBarLocked(z, false);
            }
        }

        public void playTrustedSound() {
            KeyguardViewMediator.this.playTrustedSound();
        }

        public boolean isScreenOn() {
            return KeyguardViewMediator.this.mDeviceInteractive;
        }

        public int getBouncerPromptReason() {
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            boolean isTrustUsuallyManaged = KeyguardViewMediator.this.mUpdateMonitor.isTrustUsuallyManaged(currentUser);
            boolean isUnlockingWithBiometricsPossible = KeyguardViewMediator.this.mUpdateMonitor.isUnlockingWithBiometricsPossible(currentUser);
            boolean isUnlockWithBlePossible = KeyguardViewMediator.this.mBleUnlockHelper.isUnlockWithBlePossible();
            boolean z = isTrustUsuallyManaged || isUnlockingWithBiometricsPossible || isUnlockWithBlePossible;
            KeyguardUpdateMonitor.StrongAuthTracker strongAuthTracker = KeyguardViewMediator.this.mUpdateMonitor.getStrongAuthTracker();
            int strongAuthForUser = strongAuthTracker.getStrongAuthForUser(currentUser);
            Log.i("KeyguardViewMediator", "getBouncerPromptReason trust = " + isTrustUsuallyManaged + " biometrics = " + isUnlockingWithBiometricsPossible + " bleUnlock = " + isUnlockWithBlePossible + " strongAuth = " + strongAuthForUser);
            if (z && !strongAuthTracker.hasUserAuthenticatedSinceBoot()) {
                return 1;
            }
            if (z && (strongAuthForUser & 16) != 0) {
                return 2;
            }
            if (z && (strongAuthForUser & 2) != 0) {
                return 3;
            }
            if (isTrustUsuallyManaged && (strongAuthForUser & 4) != 0) {
                return 4;
            }
            if (z && (strongAuthForUser & 8) != 0) {
                return 5;
            }
            if (z && (strongAuthForUser & 64) != 0) {
                return 6;
            }
            if (!z || (strongAuthForUser & 128) == 0) {
                return 0;
            }
            return 7;
        }
    };
    private boolean mWaitingUntilKeyguardVisible = false;
    /* access modifiers changed from: private */
    public boolean mWakeAndUnlocking;

    public void onShortPowerPressedGoHome() {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public KeyguardViewMediator(Context context, FalsingManager falsingManager, LockPatternUtils lockPatternUtils, BroadcastDispatcher broadcastDispatcher, Lazy<KeyguardViewController> lazy, DismissCallbackRegistry dismissCallbackRegistry, KeyguardUpdateMonitor keyguardUpdateMonitor, DumpManager dumpManager, Executor executor, PowerManager powerManager, TrustManager trustManager, DeviceConfigProxy deviceConfigProxy, NavigationModeController navigationModeController) {
        super(context);
        DeviceConfigProxy deviceConfigProxy2 = deviceConfigProxy;
        this.mFalsingManager = falsingManager;
        this.mLockPatternUtils = lockPatternUtils;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mKeyguardViewControllerLazy = lazy;
        this.mDismissCallbackRegistry = dismissCallbackRegistry;
        this.mUiBgExecutor = executor;
        this.mUpdateMonitor = keyguardUpdateMonitor;
        this.mPM = powerManager;
        this.mTrustManager = trustManager;
        DumpManager dumpManager2 = dumpManager;
        dumpManager.registerDumpable(KeyguardViewMediator.class.getName(), this);
        this.mDeviceConfig = deviceConfigProxy2;
        this.mShowHomeOverLockscreen = deviceConfigProxy2.getBoolean("systemui", "nav_bar_handle_show_over_lockscreen", true);
        DeviceConfigProxy deviceConfigProxy3 = this.mDeviceConfig;
        Handler handler = this.mHandler;
        Objects.requireNonNull(handler);
        deviceConfigProxy3.addOnPropertiesChangedListener("systemui", new Executor(handler) {
            public final /* synthetic */ Handler f$0;

            {
                this.f$0 = r1;
            }

            public final void execute(Runnable runnable) {
                this.f$0.post(runnable);
            }
        }, this.mOnPropertiesChangedListener);
        this.mInGestureNavigationMode = QuickStepContract.isGesturalMode(navigationModeController.addListener(new NavigationModeController.ModeChangedListener() {
            public final void onNavigationModeChanged(int i) {
                KeyguardViewMediator.this.lambda$new$0$KeyguardViewMediator(i);
            }
        }));
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$KeyguardViewMediator(int i) {
        this.mInGestureNavigationMode = QuickStepContract.isGesturalMode(i);
    }

    public void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
    }

    /* access modifiers changed from: package-private */
    public boolean mustNotUnlockCurrentUser() {
        return UserManager.isSplitSystemUser() && KeyguardUpdateMonitor.getCurrentUser() == 0;
    }

    private void setupLocked() {
        PowerManager.WakeLock newWakeLock = this.mPM.newWakeLock(1, "show keyguard");
        this.mShowKeyguardWakeLock = newWakeLock;
        boolean z = false;
        newWakeLock.setReferenceCounted(false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mBroadcastDispatcher.registerReceiver(this.mBroadcastReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD");
        intentFilter2.addAction("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK");
        this.mContext.registerReceiver(this.mDelayedLockBroadcastReceiver, intentFilter2, "com.android.systemui.permission.SELF", (Handler) null);
        this.mKeyguardDisplayManager = new KeyguardDisplayManager(this.mContext, new InjectionInflationController(SystemUIFactory.getInstance().getRootComponent()));
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        KeyguardUpdateMonitor.setCurrentUser(ActivityManager.getCurrentUser());
        if (this.mContext.getResources().getBoolean(C0007R$bool.config_enableKeyguardService)) {
            if (!shouldWaitForProvisioning() && !this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser())) {
                z = true;
            }
            setShowingLocked(z, true);
        } else {
            setShowingLocked(false, true);
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        this.mDeviceInteractive = this.mPM.isInteractive();
        this.mLockSounds = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setContentType(4).build()).build();
        String string = Settings.Global.getString(contentResolver, "lock_sound");
        if (string != null) {
            this.mLockSoundId = this.mLockSounds.load(string, 1);
        }
        if (string == null || this.mLockSoundId == 0) {
            Log.w("KeyguardViewMediator", "failed to load lock sound from " + string);
        }
        String string2 = Settings.Global.getString(contentResolver, "unlock_sound");
        if (string2 != null) {
            this.mUnlockSoundId = this.mLockSounds.load(string2, 1);
        }
        if (string2 == null || this.mUnlockSoundId == 0) {
            Log.w("KeyguardViewMediator", "failed to load unlock sound from " + string2);
        }
        String string3 = Settings.Global.getString(contentResolver, "trusted_sound");
        if (string3 != null) {
            this.mTrustedSoundId = this.mLockSounds.load(string3, 1);
        }
        if (string3 == null || this.mTrustedSoundId == 0) {
            Log.w("KeyguardViewMediator", "failed to load trusted sound from " + string3);
        }
        this.mLockSoundVolume = (float) Math.pow(10.0d, (double) (((float) this.mContext.getResources().getInteger(17694824)) / 20.0f));
        this.mHideAnimation = AnimationUtils.loadAnimation(this.mContext, 17432683);
        new WorkLockActivityController(this.mContext);
        this.mBleUnlockHelper = new MiuiBleUnlockHelper(this.mContext, this);
        this.mSmartCoverHelper = new MiuiSmartCoverHelper(this.mContext, this);
        ((KeyguardViewMediatorInjector) Dependency.get(KeyguardViewMediatorInjector.class)).setup();
    }

    public void start() {
        synchronized (this) {
            setupLocked();
        }
    }

    public void onSystemReady() {
        this.mHandler.obtainMessage(18).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void handleSystemReady() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "onSystemReady");
            this.mSystemReady = true;
            doKeyguardLocked((Bundle) null);
            this.mUpdateMonitor.registerCallback(this.mUpdateCallback);
            ((MiuiWallpaperClient) Dependency.get(MiuiWallpaperClient.class)).bindService();
        }
        maybeSendUserPresentBroadcast();
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x004e A[Catch:{ RemoteException -> 0x005b }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x006e A[Catch:{ RemoteException -> 0x005b }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onStartedGoingToSleep(int r11) {
        /*
            r10 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "onStartedGoingToSleep("
            r0.append(r1)
            r0.append(r11)
            java.lang.String r1 = ")"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "KeyguardViewMediator"
            android.util.Log.d(r1, r0)
            monitor-enter(r10)
            r0 = 0
            r10.mDeviceInteractive = r0     // Catch:{ all -> 0x00af }
            r1 = 1
            r10.mGoingToSleep = r1     // Catch:{ all -> 0x00af }
            com.android.keyguard.KeyguardUpdateMonitor r2 = r10.mUpdateMonitor     // Catch:{ all -> 0x00af }
            r2.dispatchKeyguardGoingAway(r0)     // Catch:{ all -> 0x00af }
            int r2 = com.android.keyguard.KeyguardUpdateMonitor.getCurrentUser()     // Catch:{ all -> 0x00af }
            com.android.internal.widget.LockPatternUtils r3 = r10.mLockPatternUtils     // Catch:{ all -> 0x00af }
            boolean r3 = r3.getPowerButtonInstantlyLocks(r2)     // Catch:{ all -> 0x00af }
            if (r3 != 0) goto L_0x003e
            com.android.internal.widget.LockPatternUtils r3 = r10.mLockPatternUtils     // Catch:{ all -> 0x00af }
            boolean r3 = r3.isSecure(r2)     // Catch:{ all -> 0x00af }
            if (r3 != 0) goto L_0x003c
            goto L_0x003e
        L_0x003c:
            r3 = r0
            goto L_0x003f
        L_0x003e:
            r3 = r1
        L_0x003f:
            int r4 = com.android.keyguard.KeyguardUpdateMonitor.getCurrentUser()     // Catch:{ all -> 0x00af }
            long r4 = r10.getLockTimeout(r4)     // Catch:{ all -> 0x00af }
            r10.mLockLater = r0     // Catch:{ all -> 0x00af }
            com.android.internal.policy.IKeyguardExitCallback r6 = r10.mExitSecureCallback     // Catch:{ all -> 0x00af }
            r7 = 3
            if (r6 == 0) goto L_0x006e
            java.lang.String r2 = "KeyguardViewMediator"
            java.lang.String r3 = "pending exit secure callback cancelled"
            android.util.Log.d(r2, r3)     // Catch:{ all -> 0x00af }
            com.android.internal.policy.IKeyguardExitCallback r2 = r10.mExitSecureCallback     // Catch:{ RemoteException -> 0x005b }
            r2.onKeyguardExitResult(r0)     // Catch:{ RemoteException -> 0x005b }
            goto L_0x0063
        L_0x005b:
            r0 = move-exception
            java.lang.String r2 = "KeyguardViewMediator"
            java.lang.String r3 = "Failed to call onKeyguardExitResult(false)"
            android.util.Slog.w(r2, r3, r0)     // Catch:{ all -> 0x00af }
        L_0x0063:
            r0 = 0
            r10.mExitSecureCallback = r0     // Catch:{ all -> 0x00af }
            boolean r0 = r10.mExternallyEnabled     // Catch:{ all -> 0x00af }
            if (r0 != 0) goto L_0x0092
            r10.hideLocked()     // Catch:{ all -> 0x00af }
            goto L_0x0092
        L_0x006e:
            boolean r0 = r10.mShowing     // Catch:{ all -> 0x00af }
            if (r0 == 0) goto L_0x0075
            r10.mPendingReset = r1     // Catch:{ all -> 0x00af }
            goto L_0x0092
        L_0x0075:
            if (r11 != r7) goto L_0x007d
            r8 = 0
            int r0 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1))
            if (r0 > 0) goto L_0x0082
        L_0x007d:
            r0 = 2
            if (r11 != r0) goto L_0x0088
            if (r3 != 0) goto L_0x0088
        L_0x0082:
            r10.doKeyguardLaterLocked(r4)     // Catch:{ all -> 0x00af }
            r10.mLockLater = r1     // Catch:{ all -> 0x00af }
            goto L_0x0092
        L_0x0088:
            com.android.internal.widget.LockPatternUtils r0 = r10.mLockPatternUtils     // Catch:{ all -> 0x00af }
            boolean r0 = r0.isLockScreenDisabled(r2)     // Catch:{ all -> 0x00af }
            if (r0 != 0) goto L_0x0092
            r10.mPendingLock = r1     // Catch:{ all -> 0x00af }
        L_0x0092:
            boolean r0 = r10.mPendingLock     // Catch:{ all -> 0x00af }
            if (r0 == 0) goto L_0x00a2
            if (r11 == r7) goto L_0x00a2
            r10.playSounds(r1)     // Catch:{ all -> 0x00af }
            android.os.Handler r0 = r10.mHandler     // Catch:{ all -> 0x00af }
            com.android.systemui.keyguard.-$$Lambda$KeyguardViewMediator$qBMIp_d-hf-PXyuB_VH11Sbw3J4 r1 = com.android.systemui.keyguard.$$Lambda$KeyguardViewMediator$qBMIp_dhfPXyuB_VH11Sbw3J4.INSTANCE     // Catch:{ all -> 0x00af }
            r0.post(r1)     // Catch:{ all -> 0x00af }
        L_0x00a2:
            monitor-exit(r10)     // Catch:{ all -> 0x00af }
            com.android.keyguard.KeyguardUpdateMonitor r0 = r10.mUpdateMonitor
            r0.dispatchStartedGoingToSleep(r11)
            r10.notifyStartedGoingToSleep()
            r10.resetAppLock()
            return
        L_0x00af:
            r11 = move-exception
            monitor-exit(r10)     // Catch:{ all -> 0x00af }
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.keyguard.KeyguardViewMediator.onStartedGoingToSleep(int):void");
    }

    public void onFinishedGoingToSleep(int i, boolean z) {
        Log.d("KeyguardViewMediator", "onFinishedGoingToSleep(" + i + ")");
        synchronized (this) {
            this.mDeviceInteractive = false;
            this.mGoingToSleep = false;
            this.mWakeAndUnlocking = false;
            resetKeyguardDonePendingLocked();
            this.mHideAnimationRun = false;
            notifyFinishedGoingToSleep();
            if (z) {
                Log.i("KeyguardViewMediator", "Camera gesture was triggered, preventing Keyguard locking.");
                ((PowerManager) this.mContext.getSystemService(PowerManager.class)).wakeUp(SystemClock.uptimeMillis(), 5, "com.android.systemui:CAMERA_GESTURE_PREVENT_LOCK");
                this.mPendingLock = false;
                this.mPendingReset = false;
            }
            if (this.mPendingReset) {
                resetStateLocked();
                this.mPendingReset = false;
            }
            if (this.mPendingLock) {
                doKeyguardLocked((Bundle) null);
                this.mPendingLock = false;
            }
            if (!this.mLockLater && !z) {
                doKeyguardForChildProfilesLocked();
            }
        }
        this.mUpdateMonitor.dispatchFinishedGoingToSleep(i);
    }

    private long getLockTimeout(int i) {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        long j = (long) Settings.Secure.getInt(contentResolver, "lock_screen_lock_after_timeout", 0);
        long maximumTimeToLock = this.mLockPatternUtils.getDevicePolicyManager().getMaximumTimeToLock((ComponentName) null, i);
        return maximumTimeToLock <= 0 ? j : Math.max(Math.min(maximumTimeToLock - Math.max((long) Settings.System.getInt(contentResolver, "screen_off_timeout", 30000), 0), j), 0);
    }

    private void doKeyguardLaterLocked() {
        long lockTimeout = getLockTimeout(KeyguardUpdateMonitor.getCurrentUser());
        if (lockTimeout == 0) {
            doKeyguardLocked((Bundle) null);
        } else {
            doKeyguardLaterLocked(lockTimeout);
        }
    }

    private void doKeyguardLaterLocked(long j) {
        long elapsedRealtime = SystemClock.elapsedRealtime() + j;
        Intent intent = new Intent("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD");
        intent.putExtra("seq", this.mDelayedShowingSequence);
        intent.addFlags(268435456);
        this.mAlarmManager.setExactAndAllowWhileIdle(2, elapsedRealtime, PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456));
        Log.d("KeyguardViewMediator", "setting alarm to turn off keyguard, seq = " + this.mDelayedShowingSequence);
        doKeyguardLaterForChildProfilesLocked();
    }

    private void doKeyguardLaterForChildProfilesLocked() {
        for (int i : UserManager.get(this.mContext).getEnabledProfileIds(UserHandle.myUserId())) {
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(i)) {
                long lockTimeout = getLockTimeout(i);
                if (lockTimeout == 0) {
                    doKeyguardForChildProfilesLocked();
                } else {
                    long elapsedRealtime = SystemClock.elapsedRealtime() + lockTimeout;
                    Intent intent = new Intent("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK");
                    intent.putExtra("seq", this.mDelayedProfileShowingSequence);
                    intent.putExtra("android.intent.extra.USER_ID", i);
                    intent.addFlags(268435456);
                    this.mAlarmManager.setExactAndAllowWhileIdle(2, elapsedRealtime, PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456));
                }
            }
        }
    }

    private void doKeyguardForChildProfilesLocked() {
        for (int i : UserManager.get(this.mContext).getEnabledProfileIds(UserHandle.myUserId())) {
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(i)) {
                lockProfile(i);
            }
        }
    }

    private void cancelDoKeyguardLaterLocked() {
        this.mDelayedShowingSequence++;
    }

    private void cancelDoKeyguardForChildProfilesLocked() {
        this.mDelayedProfileShowingSequence++;
    }

    public void onStartedWakingUp() {
        Trace.beginSection("KeyguardViewMediator#onStartedWakingUp");
        synchronized (this) {
            this.mDeviceInteractive = true;
            cancelDoKeyguardLaterLocked();
            cancelDoKeyguardForChildProfilesLocked();
            Log.d("KeyguardViewMediator", "onStartedWakingUp, seq = " + this.mDelayedShowingSequence);
            notifyStartedWakingUp();
        }
        this.mUpdateMonitor.dispatchStartedWakingUp();
        maybeSendUserPresentBroadcast();
        Trace.endSection();
    }

    public void onStartedWakingUp(String str) {
        this.mUpdateMonitor.dispatchStartedWakingUpWithReason(str);
    }

    public void onScreenTurningOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        Trace.beginSection("KeyguardViewMediator#onScreenTurningOn");
        notifyScreenOn(iKeyguardDrawnCallback);
        Trace.endSection();
    }

    public void onScreenTurnedOn() {
        Trace.beginSection("KeyguardViewMediator#onScreenTurnedOn");
        notifyScreenTurnedOn();
        this.mUpdateMonitor.dispatchScreenTurnedOn();
        Trace.endSection();
    }

    public void onScreenTurnedOff() {
        notifyScreenTurnedOff();
        this.mUpdateMonitor.dispatchScreenTurnedOff();
    }

    private void maybeSendUserPresentBroadcast() {
        if (this.mSystemReady && this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser())) {
            sendUserPresentBroadcast();
        } else if (this.mSystemReady && shouldWaitForProvisioning()) {
            getLockPatternUtils().userPresent(KeyguardUpdateMonitor.getCurrentUser());
        }
    }

    public void onDreamingStarted() {
        this.mUpdateMonitor.dispatchDreamingStarted();
        synchronized (this) {
            if (this.mDeviceInteractive && this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser())) {
                doKeyguardLaterLocked();
            }
        }
    }

    public void onDreamingStopped() {
        this.mUpdateMonitor.dispatchDreamingStopped();
        synchronized (this) {
            if (this.mDeviceInteractive) {
                cancelDoKeyguardLaterLocked();
            }
        }
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(7:29|30|31|32|42|39|27) */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a3, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x008b, code lost:
        continue;
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x0093 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setKeyguardEnabled(boolean r4) {
        /*
            r3 = this;
            monitor-enter(r3)
            java.lang.String r0 = "KeyguardViewMediator"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00a4 }
            r1.<init>()     // Catch:{ all -> 0x00a4 }
            java.lang.String r2 = "setKeyguardEnabled("
            r1.append(r2)     // Catch:{ all -> 0x00a4 }
            r1.append(r4)     // Catch:{ all -> 0x00a4 }
            java.lang.String r2 = ")"
            r1.append(r2)     // Catch:{ all -> 0x00a4 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00a4 }
            android.util.Log.d(r0, r1)     // Catch:{ all -> 0x00a4 }
            r3.mExternallyEnabled = r4     // Catch:{ all -> 0x00a4 }
            r0 = 1
            if (r4 != 0) goto L_0x0042
            boolean r1 = r3.mShowing     // Catch:{ all -> 0x00a4 }
            if (r1 == 0) goto L_0x0042
            com.android.internal.policy.IKeyguardExitCallback r4 = r3.mExitSecureCallback     // Catch:{ all -> 0x00a4 }
            if (r4 == 0) goto L_0x0032
            java.lang.String r4 = "KeyguardViewMediator"
            java.lang.String r0 = "in process of verifyUnlock request, ignoring"
            android.util.Log.d(r4, r0)     // Catch:{ all -> 0x00a4 }
            monitor-exit(r3)     // Catch:{ all -> 0x00a4 }
            return
        L_0x0032:
            java.lang.String r4 = "KeyguardViewMediator"
            java.lang.String r1 = "remembering to reshow, hiding keyguard, disabling status bar expansion"
            android.util.Log.d(r4, r1)     // Catch:{ all -> 0x00a4 }
            r3.mNeedToReshowWhenReenabled = r0     // Catch:{ all -> 0x00a4 }
            r3.updateInputRestrictedLocked()     // Catch:{ all -> 0x00a4 }
            r3.hideLocked()     // Catch:{ all -> 0x00a4 }
            goto L_0x00a2
        L_0x0042:
            if (r4 == 0) goto L_0x00a2
            boolean r4 = r3.mNeedToReshowWhenReenabled     // Catch:{ all -> 0x00a4 }
            if (r4 == 0) goto L_0x00a2
            java.lang.String r4 = "KeyguardViewMediator"
            java.lang.String r1 = "previously hidden, reshowing, reenabling status bar expansion"
            android.util.Log.d(r4, r1)     // Catch:{ all -> 0x00a4 }
            r4 = 0
            r3.mNeedToReshowWhenReenabled = r4     // Catch:{ all -> 0x00a4 }
            r3.updateInputRestrictedLocked()     // Catch:{ all -> 0x00a4 }
            com.android.internal.policy.IKeyguardExitCallback r1 = r3.mExitSecureCallback     // Catch:{ all -> 0x00a4 }
            r2 = 0
            if (r1 == 0) goto L_0x0075
            java.lang.String r0 = "KeyguardViewMediator"
            java.lang.String r1 = "onKeyguardExitResult(false), resetting"
            android.util.Log.d(r0, r1)     // Catch:{ all -> 0x00a4 }
            com.android.internal.policy.IKeyguardExitCallback r0 = r3.mExitSecureCallback     // Catch:{ RemoteException -> 0x0067 }
            r0.onKeyguardExitResult(r4)     // Catch:{ RemoteException -> 0x0067 }
            goto L_0x006f
        L_0x0067:
            r4 = move-exception
            java.lang.String r0 = "KeyguardViewMediator"
            java.lang.String r1 = "Failed to call onKeyguardExitResult(false)"
            android.util.Slog.w(r0, r1, r4)     // Catch:{ all -> 0x00a4 }
        L_0x006f:
            r3.mExitSecureCallback = r2     // Catch:{ all -> 0x00a4 }
            r3.resetStateLocked()     // Catch:{ all -> 0x00a4 }
            goto L_0x00a2
        L_0x0075:
            r3.showLocked(r2)     // Catch:{ all -> 0x00a4 }
            r3.mWaitingUntilKeyguardVisible = r0     // Catch:{ all -> 0x00a4 }
            android.os.Handler r4 = r3.mHandler     // Catch:{ all -> 0x00a4 }
            r0 = 8
            r1 = 2000(0x7d0, double:9.88E-321)
            r4.sendEmptyMessageDelayed(r0, r1)     // Catch:{ all -> 0x00a4 }
            java.lang.String r4 = "KeyguardViewMediator"
            java.lang.String r0 = "waiting until mWaitingUntilKeyguardVisible is false"
            android.util.Log.d(r4, r0)     // Catch:{ all -> 0x00a4 }
        L_0x008b:
            boolean r4 = r3.mWaitingUntilKeyguardVisible     // Catch:{ all -> 0x00a4 }
            if (r4 == 0) goto L_0x009b
            r3.wait()     // Catch:{ InterruptedException -> 0x0093 }
            goto L_0x008b
        L_0x0093:
            java.lang.Thread r4 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x00a4 }
            r4.interrupt()     // Catch:{ all -> 0x00a4 }
            goto L_0x008b
        L_0x009b:
            java.lang.String r4 = "KeyguardViewMediator"
            java.lang.String r0 = "done waiting for mWaitingUntilKeyguardVisible"
            android.util.Log.d(r4, r0)     // Catch:{ all -> 0x00a4 }
        L_0x00a2:
            monitor-exit(r3)     // Catch:{ all -> 0x00a4 }
            return
        L_0x00a4:
            r4 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x00a4 }
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.keyguard.KeyguardViewMediator.setKeyguardEnabled(boolean):void");
    }

    public void verifyUnlock(IKeyguardExitCallback iKeyguardExitCallback) {
        Trace.beginSection("KeyguardViewMediator#verifyUnlock");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "verifyUnlock");
            if (shouldWaitForProvisioning()) {
                Log.d("KeyguardViewMediator", "ignoring because device isn't provisioned");
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(false);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e);
                }
            } else if (this.mExternallyEnabled) {
                Log.w("KeyguardViewMediator", "verifyUnlock called when not externally disabled");
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(false);
                } catch (RemoteException e2) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e2);
                }
            } else if (this.mExitSecureCallback != null) {
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(false);
                } catch (RemoteException e3) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e3);
                }
            } else if (!isSecure()) {
                this.mExternallyEnabled = true;
                this.mNeedToReshowWhenReenabled = false;
                updateInputRestricted();
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(true);
                } catch (RemoteException e4) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e4);
                }
            } else {
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(false);
                } catch (RemoteException e5) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e5);
                }
            }
        }
        Trace.endSection();
    }

    public boolean isShowingAndNotOccluded() {
        return this.mShowing && !this.mOccluded;
    }

    public void setOccluded(boolean z, boolean z2) {
        Trace.beginSection("KeyguardViewMediator#setOccluded");
        Log.d("KeyguardViewMediator", "setOccluded " + z);
        this.mHandler.removeMessages(9);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(9, z ? 1 : 0, z2 ? 1 : 0));
        Trace.endSection();
    }

    public static int getUnlockTrackSimState(int i) {
        return mUnlockTrackSimStates.get(i);
    }

    public boolean isHiding() {
        return this.mHiding;
    }

    /* access modifiers changed from: private */
    public void handleSetOccluded(boolean z, boolean z2) {
        boolean z3;
        Trace.beginSection("KeyguardViewMediator#handleSetOccluded");
        synchronized (this) {
            if (this.mHiding && z) {
                startKeyguardExitAnimation(0, 0);
            }
            if (this.mOccluded != z) {
                this.mOccluded = z;
                this.mUpdateMonitor.setKeyguardShowingAndOccluded(this.mShowing, z);
                if (!z && this.mDeviceInteractive) {
                    this.mBleUnlockHelper.verifyBLEDeviceRssi();
                }
                KeyguardViewController keyguardViewController = this.mKeyguardViewControllerLazy.get();
                if (!((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isSimPinSecure()) {
                    if (z2 && this.mDeviceInteractive) {
                        z3 = true;
                        keyguardViewController.setOccluded(z, z3);
                        adjustStatusBarLocked();
                    }
                }
                z3 = false;
                keyguardViewController.setOccluded(z, z3);
                adjustStatusBarLocked();
            }
        }
        Trace.endSection();
    }

    public void doKeyguardTimeout(Bundle bundle) {
        this.mHandler.removeMessages(10);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(10, bundle));
    }

    public boolean isInputRestricted() {
        return this.mShowing || this.mNeedToReshowWhenReenabled;
    }

    private void updateInputRestricted() {
        synchronized (this) {
            updateInputRestrictedLocked();
        }
    }

    private void updateInputRestrictedLocked() {
        boolean isInputRestricted = isInputRestricted();
        if (this.mInputRestricted != isInputRestricted) {
            this.mInputRestricted = isInputRestricted;
            for (int size = this.mKeyguardStateCallbacks.size() - 1; size >= 0; size--) {
                IKeyguardStateCallback iKeyguardStateCallback = this.mKeyguardStateCallbacks.get(size);
                try {
                    iKeyguardStateCallback.onInputRestrictedStateChanged(isInputRestricted);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onDeviceProvisioned", e);
                    if (e instanceof DeadObjectException) {
                        this.mKeyguardStateCallbacks.remove(iKeyguardStateCallback);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void doKeyguardLocked(Bundle bundle) {
        if (KeyguardUpdateMonitor.CORE_APPS_ONLY) {
            Log.d("KeyguardViewMediator", "doKeyguard: not showing because booting to cryptkeeper");
            return;
        }
        boolean z = true;
        if (!this.mExternallyEnabled) {
            Log.d("KeyguardViewMediator", "doKeyguard: not showing because externally disabled");
            this.mNeedToReshowWhenReenabled = true;
        } else if (this.mKeyguardViewControllerLazy.get().isShowing()) {
            Log.d("KeyguardViewMediator", "doKeyguard: not showing because it is already showing");
            resetStateLocked();
        } else {
            if (!mustNotUnlockCurrentUser() || !this.mUpdateMonitor.isDeviceProvisioned()) {
                boolean z2 = this.mUpdateMonitor.isSimPinSecure() || ((SubscriptionManager.isValidSubscriptionId(this.mUpdateMonitor.getNextSubIdForState(1)) || SubscriptionManager.isValidSubscriptionId(this.mUpdateMonitor.getNextSubIdForState(7))) && (SystemProperties.getBoolean("keyguard.no_require_sim", false) ^ true));
                this.mSimLockedOrMissing = z2;
                if (z2 || !shouldWaitForProvisioning()) {
                    if (bundle == null || !bundle.getBoolean("force_show", false)) {
                        z = false;
                    }
                    if (this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser()) && !this.mSimLockedOrMissing && !z) {
                        Log.d("KeyguardViewMediator", "doKeyguard: not showing because lockscreen is off");
                        return;
                    } else if (this.mLockPatternUtils.checkVoldPassword(KeyguardUpdateMonitor.getCurrentUser())) {
                        Log.d("KeyguardViewMediator", "Not showing lock screen since just decrypted");
                        setShowingLocked(false);
                        hideLocked();
                        return;
                    } else if (this.mSmartCoverHelper.isHideLockForLid() && !this.mSimLockedOrMissing && !isSecure()) {
                        Slog.w("KeyguardViewMediator", "Not showing lock screen since in smart cover mode");
                        if (this.mShowing) {
                            handleHide();
                            return;
                        }
                        return;
                    }
                } else {
                    Log.d("KeyguardViewMediator", "doKeyguard: not showing because device isn't provisioned and the sim is not locked or missing");
                    return;
                }
            }
            Log.d("KeyguardViewMediator", "doKeyguard: showing the lock screen");
            showLocked(bundle);
        }
    }

    public boolean isSimLockedOrMissing() {
        return this.mSimLockedOrMissing;
    }

    /* access modifiers changed from: private */
    public void lockProfile(int i) {
        this.mTrustManager.setDeviceLockedForUser(i, true);
    }

    /* access modifiers changed from: private */
    public boolean shouldWaitForProvisioning() {
        return !this.mUpdateMonitor.isDeviceProvisioned() && !isSecure();
    }

    /* access modifiers changed from: private */
    public void handleDismiss(IKeyguardDismissCallback iKeyguardDismissCallback, CharSequence charSequence) {
        if (this.mShowing) {
            if (iKeyguardDismissCallback != null) {
                this.mDismissCallbackRegistry.addCallback(iKeyguardDismissCallback);
            }
            this.mKeyguardViewControllerLazy.get().dismissAndCollapse();
        } else if (iKeyguardDismissCallback != null) {
            new DismissCallbackWrapper(iKeyguardDismissCallback).notifyDismissError();
        }
    }

    public void dismiss(IKeyguardDismissCallback iKeyguardDismissCallback, CharSequence charSequence) {
        this.mHandler.obtainMessage(11, new DismissMessage(iKeyguardDismissCallback, charSequence)).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void resetStateLocked() {
        Log.e("KeyguardViewMediator", "resetStateLocked");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3));
    }

    private void notifyStartedGoingToSleep() {
        Log.d("KeyguardViewMediator", "notifyStartedGoingToSleep");
        this.mHandler.sendEmptyMessage(17);
    }

    private void notifyFinishedGoingToSleep() {
        Log.d("KeyguardViewMediator", "notifyFinishedGoingToSleep");
        this.mHandler.sendEmptyMessage(5);
    }

    private void notifyStartedWakingUp() {
        Log.d("KeyguardViewMediator", "notifyStartedWakingUp");
        this.mHandler.sendEmptyMessage(14);
    }

    private void notifyScreenOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        Log.d("KeyguardViewMediator", "notifyScreenOn");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(6, iKeyguardDrawnCallback));
    }

    private void notifyScreenTurnedOn() {
        Log.d("KeyguardViewMediator", "notifyScreenTurnedOn");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(15));
    }

    private void notifyScreenTurnedOff() {
        Log.d("KeyguardViewMediator", "notifyScreenTurnedOff");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(16));
    }

    private void showLocked(Bundle bundle) {
        Trace.beginSection("KeyguardViewMediator#showLocked aqcuiring mShowKeyguardWakeLock");
        Log.d("KeyguardViewMediator", "showLocked");
        this.mShowKeyguardWakeLock.acquire();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, bundle));
        Trace.endSection();
    }

    private void hideLocked() {
        Trace.beginSection("KeyguardViewMediator#hideLocked");
        Log.d("KeyguardViewMediator", "hideLocked");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
        Trace.endSection();
    }

    public boolean isSecure() {
        return isSecure(KeyguardUpdateMonitor.getCurrentUser());
    }

    public boolean isSecure(int i) {
        return this.mLockPatternUtils.isSecure(i) || this.mUpdateMonitor.isSimPinSecure();
    }

    public void setSwitchingUser(boolean z) {
        this.mUpdateMonitor.setSwitchingUser(z);
    }

    public void setCurrentUser(int i) {
        KeyguardUpdateMonitor.setCurrentUser(i);
        synchronized (this) {
            notifyTrustedChangedLocked(this.mUpdateMonitor.getUserHasTrust(i));
        }
    }

    public void keyguardDone() {
        Trace.beginSection("KeyguardViewMediator#keyguardDone");
        Log.d("KeyguardViewMediator", "keyguardDone()");
        userActivity();
        EventLog.writeEvent(70000, 2);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(7));
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    public void tryKeyguardDone() {
        Log.d("KeyguardViewMediator", "tryKeyguardDone: pending - " + this.mKeyguardDonePending + ", animRan - " + this.mHideAnimationRun + " animRunning - " + this.mHideAnimationRunning);
        if (!this.mKeyguardDonePending && this.mHideAnimationRun && !this.mHideAnimationRunning) {
            handleKeyguardDone();
        } else if (!this.mHideAnimationRun) {
            Log.d("KeyguardViewMediator", "tryKeyguardDone: starting pre-hide animation");
            this.mHideAnimationRun = true;
            this.mHideAnimationRunning = true;
            this.mKeyguardViewControllerLazy.get().startPreHideAnimation(this.mHideAnimationFinishedRunnable);
        }
    }

    /* access modifiers changed from: private */
    public void handleKeyguardDone() {
        Trace.beginSection("KeyguardViewMediator#handleKeyguardDone");
        this.mUiBgExecutor.execute(new Runnable(KeyguardUpdateMonitor.getCurrentUser()) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                KeyguardViewMediator.this.lambda$handleKeyguardDone$2$KeyguardViewMediator(this.f$1);
            }
        });
        Log.d("KeyguardViewMediator", "handleKeyguardDone");
        synchronized (this) {
            resetKeyguardDonePendingLocked();
        }
        this.mBleUnlockHelper.unregisterUnlockListener();
        if (this.mGoingToSleep) {
            this.mUpdateMonitor.clearBiometricRecognized();
            Log.i("KeyguardViewMediator", "Device is going to sleep, aborting keyguardDone");
            return;
        }
        IKeyguardExitCallback iKeyguardExitCallback = this.mExitSecureCallback;
        if (iKeyguardExitCallback != null) {
            try {
                iKeyguardExitCallback.onKeyguardExitResult(true);
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult()", e);
            }
            this.mExitSecureCallback = null;
            this.mExternallyEnabled = true;
            this.mNeedToReshowWhenReenabled = false;
            updateInputRestricted();
        }
        handleHide();
        this.mUpdateMonitor.clearBiometricRecognized();
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleKeyguardDone$2 */
    public /* synthetic */ void lambda$handleKeyguardDone$2$KeyguardViewMediator(int i) {
        if (this.mLockPatternUtils.isSecure(i)) {
            this.mLockPatternUtils.getDevicePolicyManager().reportKeyguardDismissed(i);
        }
    }

    /* access modifiers changed from: private */
    public void sendUserPresentBroadcast() {
        synchronized (this) {
            if (this.mBootCompleted) {
                int currentUser = KeyguardUpdateMonitor.getCurrentUser();
                this.mUiBgExecutor.execute(new Runnable((UserManager) this.mContext.getSystemService("user"), new UserHandle(currentUser), currentUser) {
                    public final /* synthetic */ UserManager f$1;
                    public final /* synthetic */ UserHandle f$2;
                    public final /* synthetic */ int f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void run() {
                        KeyguardViewMediator.this.lambda$sendUserPresentBroadcast$3$KeyguardViewMediator(this.f$1, this.f$2, this.f$3);
                    }
                });
            } else {
                this.mBootSendUserPresent = true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$sendUserPresentBroadcast$3 */
    public /* synthetic */ void lambda$sendUserPresentBroadcast$3$KeyguardViewMediator(UserManager userManager, UserHandle userHandle, int i) {
        for (int of : userManager.getProfileIdsWithDisabled(userHandle.getIdentifier())) {
            this.mContext.sendBroadcastAsUser(USER_PRESENT_INTENT, UserHandle.of(of));
        }
        getLockPatternUtils().userPresent(i);
    }

    /* access modifiers changed from: private */
    public void handleKeyguardDoneDrawing() {
        Trace.beginSection("KeyguardViewMediator#handleKeyguardDoneDrawing");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleKeyguardDoneDrawing");
            if (this.mWaitingUntilKeyguardVisible) {
                Log.d("KeyguardViewMediator", "handleKeyguardDoneDrawing: notifying mWaitingUntilKeyguardVisible");
                this.mWaitingUntilKeyguardVisible = false;
                notifyAll();
                this.mHandler.removeMessages(8);
            }
        }
        Trace.endSection();
    }

    private void playSounds(boolean z) {
        playSound(z ? this.mLockSoundId : this.mUnlockSoundId);
    }

    private void playSound(int i) {
        if (i != 0 && Settings.System.getInt(this.mContext.getContentResolver(), "lockscreen_sounds_enabled", 1) == 1) {
            this.mLockSounds.stop(this.mLockSoundStreamId);
            if (this.mAudioManager == null) {
                AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
                this.mAudioManager = audioManager;
                if (audioManager != null) {
                    this.mUiSoundsStreamType = audioManager.getUiSoundsStreamType();
                } else {
                    return;
                }
            }
            this.mUiBgExecutor.execute(new Runnable(i) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    KeyguardViewMediator.this.lambda$playSound$4$KeyguardViewMediator(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$playSound$4 */
    public /* synthetic */ void lambda$playSound$4$KeyguardViewMediator(int i) {
        if (!this.mAudioManager.isStreamMute(this.mUiSoundsStreamType)) {
            SoundPool soundPool = this.mLockSounds;
            float f = this.mLockSoundVolume;
            int play = soundPool.play(i, f, f, 1, 0, 1.0f);
            synchronized (this) {
                this.mLockSoundStreamId = play;
            }
        }
    }

    /* access modifiers changed from: private */
    public void playTrustedSound() {
        playSound(this.mTrustedSoundId);
    }

    private void updateActivityLockScreenState(boolean z, boolean z2) {
        this.mUiBgExecutor.execute(new Runnable(z, z2) {
            public final /* synthetic */ boolean f$0;
            public final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run() {
                KeyguardViewMediator.lambda$updateActivityLockScreenState$5(this.f$0, this.f$1);
            }
        });
    }

    static /* synthetic */ void lambda$updateActivityLockScreenState$5(boolean z, boolean z2) {
        Log.d("KeyguardViewMediator", "updateActivityLockScreenState(" + z + ", " + z2 + ")");
        try {
            ActivityTaskManager.getService().setLockScreenShown(z, z2);
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: private */
    public void handleShow(Bundle bundle) {
        Trace.beginSection("KeyguardViewMediator#handleShow");
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (this.mLockPatternUtils.isSecure(currentUser)) {
            this.mLockPatternUtils.getDevicePolicyManager().reportKeyguardSecured(currentUser);
        }
        synchronized (this) {
            if (!this.mSystemReady) {
                Log.d("KeyguardViewMediator", "ignoring handleShow because system is not ready.");
                return;
            }
            Log.d("KeyguardViewMediator", "handleShow");
            this.mHiding = false;
            this.mWakeAndUnlocking = false;
            setShowingLocked(true);
            this.mKeyguardViewControllerLazy.get().show(bundle);
            resetKeyguardDonePendingLocked();
            this.mHideAnimationRun = false;
            adjustStatusBarLocked();
            userActivity();
            this.mUpdateMonitor.setKeyguardGoingAway(false);
            this.mKeyguardViewControllerLazy.get().setKeyguardGoingAwayState(false);
            ((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).resetFastUnlockState();
            MiuiGxzwManager.getInstance().onKeyguardShow();
            this.mShowKeyguardWakeLock.release();
            this.mKeyguardDisplayManager.show();
            this.mLockPatternUtils.scheduleNonStrongBiometricIdleTimeout(KeyguardUpdateMonitor.getCurrentUser());
            resetAppLock();
            Trace.endSection();
        }
    }

    private void resetAppLock() {
        SecurityManager securityManager = (SecurityManager) this.mContext.getSystemService("security");
        if (securityManager != null) {
            securityManager.removeAccessControlPassAsUser("*", -1);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$6 */
    public /* synthetic */ void lambda$new$6$KeyguardViewMediator() {
        Log.e("KeyguardViewMediator", "mHideAnimationFinishedRunnable#run");
        this.mHideAnimationRunning = false;
        tryKeyguardDone();
    }

    /* access modifiers changed from: private */
    public void handleHide() {
        Trace.beginSection("KeyguardViewMediator#handleHide");
        if (this.mAodShowing) {
            ((PowerManager) this.mContext.getSystemService(PowerManager.class)).wakeUp(SystemClock.uptimeMillis(), 4, "com.android.systemui:BOUNCER_DOZING");
        }
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleHide");
            if (mustNotUnlockCurrentUser()) {
                Log.d("KeyguardViewMediator", "Split system user, quit unlocking.");
                return;
            }
            this.mHiding = true;
            if (!this.mShowing || this.mOccluded) {
                handleStartKeyguardExitAnimation(SystemClock.uptimeMillis() + this.mHideAnimation.getStartOffset(), this.mHideAnimation.getDuration());
            } else {
                this.mKeyguardGoingAwayRunnable.run();
            }
            MiuiGxzwManager.getInstance().onKeyguardHide();
            if (MiuiFaceUnlockUtils.isScreenTurnOnDelayed()) {
                ((KeyguardViewMediatorInjector) Dependency.get(KeyguardViewMediatorInjector.class)).unblockScreenOn(this.mKeyguardStateCallbacks);
                MiuiFaceUnlockUtils.setScreenTurnOnDelayed(false);
            }
            ((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).onKeyguardHide();
            Trace.endSection();
        }
    }

    /* access modifiers changed from: private */
    public void handleStartKeyguardExitAnimation(long j, long j2) {
        Trace.beginSection("KeyguardViewMediator#handleStartKeyguardExitAnimation");
        Log.d("KeyguardViewMediator", "handleStartKeyguardExitAnimation startTime=" + j + " fadeoutDuration=" + j2);
        synchronized (this) {
            if (!this.mHiding) {
                setShowingLocked(this.mShowing, true);
                return;
            }
            this.mHiding = false;
            if (this.mWakeAndUnlocking && this.mDrawnCallback != null) {
                this.mKeyguardViewControllerLazy.get().getViewRootImpl().setReportNextDraw();
                notifyDrawn(this.mDrawnCallback);
                this.mDrawnCallback = null;
            }
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(this.mPhoneState)) {
                playSounds(false);
            }
            setShowingLocked(false);
            this.mWakeAndUnlocking = false;
            this.mDismissCallbackRegistry.notifyDismissSucceeded();
            this.mKeyguardViewControllerLazy.get().hide(j, j2);
            resetKeyguardDonePendingLocked();
            this.mHideAnimationRun = false;
            adjustStatusBarLocked();
            sendUserPresentBroadcast();
            Trace.endSection();
        }
    }

    /* access modifiers changed from: private */
    public void adjustStatusBarLocked() {
        adjustStatusBarLocked(false, false);
    }

    /* access modifiers changed from: private */
    public void adjustStatusBarLocked(boolean z, boolean z2) {
        if (this.mStatusBarManager == null) {
            this.mStatusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
        }
        StatusBarManager statusBarManager = this.mStatusBarManager;
        if (statusBarManager == null) {
            Log.w("KeyguardViewMediator", "Could not get status bar manager");
            return;
        }
        int i = 0;
        if (z2) {
            statusBarManager.disable(0);
        }
        if (z || isShowingAndNotOccluded()) {
            if (!this.mShowHomeOverLockscreen || !this.mInGestureNavigationMode) {
                i = 6291456;
            }
            i |= 16777216;
        }
        Log.d("KeyguardViewMediator", "adjustStatusBarLocked: mShowing=" + this.mShowing + " mOccluded=" + this.mOccluded + " isSecure=" + isSecure() + " force=" + z + " --> flags=0x" + Integer.toHexString(i));
        this.mStatusBarManager.disable(i);
    }

    /* access modifiers changed from: private */
    public void handleReset() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleReset");
            this.mKeyguardViewControllerLazy.get().reset(true);
        }
    }

    /* access modifiers changed from: private */
    public void handleVerifyUnlock() {
        Trace.beginSection("KeyguardViewMediator#handleVerifyUnlock");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleVerifyUnlock");
            setShowingLocked(true);
            this.mKeyguardViewControllerLazy.get().dismissAndCollapse();
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    public void handleNotifyStartedGoingToSleep() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyStartedGoingToSleep");
            this.mKeyguardViewControllerLazy.get().onStartedGoingToSleep();
        }
    }

    /* access modifiers changed from: private */
    public void handleNotifyFinishedGoingToSleep() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyFinishedGoingToSleep");
            this.mKeyguardViewControllerLazy.get().onFinishedGoingToSleep();
        }
    }

    /* access modifiers changed from: private */
    public void handleNotifyStartedWakingUp() {
        Trace.beginSection("KeyguardViewMediator#handleMotifyStartedWakingUp");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyWakingUp");
            this.mKeyguardViewControllerLazy.get().onStartedWakingUp();
            ((KeyguardViewMediatorInjector) Dependency.get(KeyguardViewMediatorInjector.class)).sendKeyguardScreenOnBroadcast();
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    public void handleNotifyScreenTurningOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        Trace.beginSection("KeyguardViewMediator#handleNotifyScreenTurningOn");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyScreenTurningOn");
            this.mKeyguardViewControllerLazy.get().onScreenTurningOn();
            if (iKeyguardDrawnCallback != null) {
                if (this.mWakeAndUnlocking) {
                    this.mDrawnCallback = iKeyguardDrawnCallback;
                } else if (MiuiFaceUnlockUtils.isScreenTurnOnDelayed()) {
                    ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).notifyDrawnWhenScreenOn(iKeyguardDrawnCallback);
                } else {
                    notifyDrawn(iKeyguardDrawnCallback);
                }
            }
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    public void handleNotifyScreenTurnedOn() {
        Trace.beginSection("KeyguardViewMediator#handleNotifyScreenTurnedOn");
        if (LatencyTracker.isEnabled(this.mContext)) {
            LatencyTracker.getInstance(this.mContext).onActionEnd(5);
        }
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyScreenTurnedOn");
            this.mKeyguardViewControllerLazy.get().onScreenTurnedOn();
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    public void handleNotifyScreenTurnedOff() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyScreenTurnedOff");
            this.mDrawnCallback = null;
        }
    }

    private void notifyDrawn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        Trace.beginSection("KeyguardViewMediator#notifyDrawn");
        try {
            iKeyguardDrawnCallback.onDrawn();
        } catch (RemoteException e) {
            Slog.w("KeyguardViewMediator", "Exception calling onDrawn():", e);
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    public void resetKeyguardDonePendingLocked() {
        this.mKeyguardDonePending = false;
        this.mHandler.removeMessages(13);
    }

    public void onBootCompleted() {
        synchronized (this) {
            this.mBootCompleted = true;
            adjustStatusBarLocked(false, true);
            if (this.mBootSendUserPresent) {
                sendUserPresentBroadcast();
            }
        }
    }

    public void onWakeAndUnlocking() {
        Trace.beginSection("KeyguardViewMediator#onWakeAndUnlocking");
        this.mWakeAndUnlocking = true;
        keyguardDone();
        Trace.endSection();
    }

    public void startKeyguardExitAnimation(long j, long j2) {
        Trace.beginSection("KeyguardViewMediator#startKeyguardExitAnimation");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(12, new StartKeyguardExitAnimParams(j, j2)));
        Trace.endSection();
    }

    public ViewMediatorCallback getViewMediatorCallback() {
        return this.mViewMediatorCallback;
    }

    public LockPatternUtils getLockPatternUtils() {
        return this.mLockPatternUtils;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("  mSystemReady: ");
        printWriter.println(this.mSystemReady);
        printWriter.print("  mBootCompleted: ");
        printWriter.println(this.mBootCompleted);
        printWriter.print("  mBootSendUserPresent: ");
        printWriter.println(this.mBootSendUserPresent);
        printWriter.print("  mExternallyEnabled: ");
        printWriter.println(this.mExternallyEnabled);
        printWriter.print("  mShuttingDown: ");
        printWriter.println(this.mShuttingDown);
        printWriter.print("  mNeedToReshowWhenReenabled: ");
        printWriter.println(this.mNeedToReshowWhenReenabled);
        printWriter.print("  mShowing: ");
        printWriter.println(this.mShowing);
        printWriter.print("  mInputRestricted: ");
        printWriter.println(this.mInputRestricted);
        printWriter.print("  mOccluded: ");
        printWriter.println(this.mOccluded);
        printWriter.print("  mDelayedShowingSequence: ");
        printWriter.println(this.mDelayedShowingSequence);
        printWriter.print("  mExitSecureCallback: ");
        printWriter.println(this.mExitSecureCallback);
        printWriter.print("  mDeviceInteractive: ");
        printWriter.println(this.mDeviceInteractive);
        printWriter.print("  mGoingToSleep: ");
        printWriter.println(this.mGoingToSleep);
        printWriter.print("  mHiding: ");
        printWriter.println(this.mHiding);
        printWriter.print("  mDozing: ");
        printWriter.println(this.mDozing);
        printWriter.print("  mAodShowing: ");
        printWriter.println(this.mAodShowing);
        printWriter.print("  mWaitingUntilKeyguardVisible: ");
        printWriter.println(this.mWaitingUntilKeyguardVisible);
        printWriter.print("  mKeyguardDonePending: ");
        printWriter.println(this.mKeyguardDonePending);
        printWriter.print("  mHideAnimationRun: ");
        printWriter.println(this.mHideAnimationRun);
        printWriter.print("  mPendingReset: ");
        printWriter.println(this.mPendingReset);
        printWriter.print("  mPendingLock: ");
        printWriter.println(this.mPendingLock);
        printWriter.print("  mWakeAndUnlocking: ");
        printWriter.println(this.mWakeAndUnlocking);
        printWriter.print("  mDrawnCallback: ");
        printWriter.println(this.mDrawnCallback);
    }

    public void setDozing(boolean z) {
        if (z != this.mDozing) {
            this.mDozing = z;
            setShowingLocked(this.mShowing);
        }
    }

    public void setPulsing(boolean z) {
        this.mPulsing = z;
    }

    private static class StartKeyguardExitAnimParams {
        long fadeoutDuration;
        long startTime;

        private StartKeyguardExitAnimParams(long j, long j2) {
            this.startTime = j;
            this.fadeoutDuration = j2;
        }
    }

    private void setShowingLocked(boolean z) {
        setShowingLocked(z, false);
    }

    private void setShowingLocked(boolean z, boolean z2) {
        boolean z3 = true;
        boolean z4 = this.mDozing && !this.mWakeAndUnlocking;
        if (z == this.mShowing && z4 == this.mAodShowing && !z2) {
            z3 = false;
        }
        this.mShowing = z;
        this.mAodShowing = z4;
        this.mUpdateMonitor.setKeyguardShowingAndOccluded(z, this.mOccluded);
        this.mUpdateMonitor.updateShowingState(this.mShowing);
        if (z3) {
            notifyDefaultDisplayCallbacks(z);
            updateActivityLockScreenState(z, z4);
        }
    }

    private void notifyDefaultDisplayCallbacks(boolean z) {
        DejankUtils.whitelistIpcs((Runnable) new Runnable(z) {
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                KeyguardViewMediator.this.lambda$notifyDefaultDisplayCallbacks$7$KeyguardViewMediator(this.f$1);
            }
        });
        updateInputRestrictedLocked();
        this.mUiBgExecutor.execute(new Runnable() {
            public final void run() {
                KeyguardViewMediator.this.lambda$notifyDefaultDisplayCallbacks$8$KeyguardViewMediator();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$notifyDefaultDisplayCallbacks$7 */
    public /* synthetic */ void lambda$notifyDefaultDisplayCallbacks$7$KeyguardViewMediator(boolean z) {
        for (int size = this.mKeyguardStateCallbacks.size() - 1; size >= 0; size--) {
            IKeyguardStateCallback iKeyguardStateCallback = this.mKeyguardStateCallbacks.get(size);
            try {
                iKeyguardStateCallback.onShowingStateChanged(z);
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call onShowingStateChanged", e);
                if (e instanceof DeadObjectException) {
                    this.mKeyguardStateCallbacks.remove(iKeyguardStateCallback);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$notifyDefaultDisplayCallbacks$8 */
    public /* synthetic */ void lambda$notifyDefaultDisplayCallbacks$8$KeyguardViewMediator() {
        this.mTrustManager.reportKeyguardShowingChanged();
    }

    /* access modifiers changed from: private */
    public void notifyTrustedChangedLocked(boolean z) {
        for (int size = this.mKeyguardStateCallbacks.size() - 1; size >= 0; size--) {
            try {
                this.mKeyguardStateCallbacks.get(size).onTrustedChanged(z);
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call notifyTrustedChangedLocked", e);
                if (e instanceof DeadObjectException) {
                    this.mKeyguardStateCallbacks.remove(size);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyHasLockscreenWallpaperChanged(boolean z) {
        for (int size = this.mKeyguardStateCallbacks.size() - 1; size >= 0; size--) {
            try {
                this.mKeyguardStateCallbacks.get(size).onHasLockscreenWallpaperChanged(z);
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call onHasLockscreenWallpaperChanged", e);
                if (e instanceof DeadObjectException) {
                    this.mKeyguardStateCallbacks.remove(size);
                }
            }
        }
    }

    public void addStateMonitorCallback(IKeyguardStateCallback iKeyguardStateCallback) {
        synchronized (this) {
            this.mKeyguardStateCallbacks.add(iKeyguardStateCallback);
            try {
                iKeyguardStateCallback.onSimSecureStateChanged(this.mUpdateMonitor.isSimPinSecure());
                iKeyguardStateCallback.onShowingStateChanged(this.mShowing);
                iKeyguardStateCallback.onInputRestrictedStateChanged(this.mInputRestricted);
                iKeyguardStateCallback.onTrustedChanged(this.mUpdateMonitor.getUserHasTrust(KeyguardUpdateMonitor.getCurrentUser()));
                iKeyguardStateCallback.onHasLockscreenWallpaperChanged(this.mUpdateMonitor.hasLockscreenWallpaper());
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call to IKeyguardStateCallback", e);
            }
        }
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public boolean isOccluded() {
        return this.mOccluded;
    }

    private static class DismissMessage {
        private final IKeyguardDismissCallback mCallback;
        private final CharSequence mMessage;

        DismissMessage(IKeyguardDismissCallback iKeyguardDismissCallback, CharSequence charSequence) {
            this.mCallback = iKeyguardDismissCallback;
            this.mMessage = charSequence;
        }

        public IKeyguardDismissCallback getCallback() {
            return this.mCallback;
        }

        public CharSequence getMessage() {
            return this.mMessage;
        }
    }

    public synchronized boolean isGoingToShowKeyguard() {
        boolean z;
        z = true;
        if (!this.mGoingToSleep && (this.mUpdateMonitor.isDeviceInteractive() || !this.mHandler.hasMessages(1))) {
            z = false;
        }
        return z;
    }
}
