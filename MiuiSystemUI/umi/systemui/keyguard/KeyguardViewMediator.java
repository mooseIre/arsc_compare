package com.android.systemui.keyguard;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManagerCompat;
import android.app.trust.TrustManager;
import android.app.trust.TrustManagerCompat;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserHandleCompat;
import android.os.UserManager;
import android.os.UserManagerCompat;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardStateCallbackCompat;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardDisplayManager;
import com.android.keyguard.KeyguardSensorManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.LatencyTracker;
import com.android.keyguard.MiuiBleUnlockHelper;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.analytics.KeyguardSettingsAnalytics;
import com.android.keyguard.faceunlock.FaceUnlockController;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.keyguard.faceunlock.MiuiFaceUnlockUtils;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.magazine.LockScreenMagazineUtils;
import com.android.keyguard.smartcover.SmartCoverHelper;
import com.android.keyguard.wallpaper.KeyguardWallpaperHelper;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.events.ScreenOnEvent;
import com.android.systemui.miui.ActivityObserver;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.statusbar.phone.FingerprintUnlockController;
import com.android.systemui.statusbar.phone.PanelBar;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.UnlockMethodCache;
import com.android.systemui.util.Utils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import miui.security.SecurityManager;
import miui.view.MiuiHapticFeedbackConstants;

public class KeyguardViewMediator extends SystemUI {
    /* access modifiers changed from: private */
    public static final Intent USER_PRESENT_INTENT = new Intent("android.intent.action.USER_PRESENT").addFlags(606076928);
    public final int OFF_BECAUSE_OF_ADMIN = 1;
    public final int OFF_BECAUSE_OF_TIMEOUT = 3;
    public final int OFF_BECAUSE_OF_USER = 2;
    private AlarmManager mAlarmManager;
    private boolean mAodShowing;
    /* access modifiers changed from: private */
    public AudioManager mAudioManager;
    private MiuiBleUnlockHelper mBleUnlockHelper;
    private boolean mBootCompleted;
    private boolean mBootSendUserPresent;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD".equals(intent.getAction())) {
                int intExtra = intent.getIntExtra("seq", 0);
                Slog.w("KeyguardViewMediator", "received DELAYED_KEYGUARD_ACTION with seq = " + intExtra + ", mDelayedShowingSequence = " + KeyguardViewMediator.this.mDelayedShowingSequence);
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
            } else if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
                synchronized (KeyguardViewMediator.this) {
                    boolean unused = KeyguardViewMediator.this.mShuttingDown = true;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mDelayedProfileShowingSequence;
    /* access modifiers changed from: private */
    public int mDelayedShowingSequence;
    /* access modifiers changed from: private */
    public boolean mDeviceInteractive;
    private final DismissCallbackRegistry mDismissCallbackRegistry = new DismissCallbackRegistry();
    /* access modifiers changed from: private */
    public Display mDisplay;
    private IKeyguardDrawnCallback mDrawnCallback;
    /* access modifiers changed from: private */
    public Sensor mEllipticSensor = null;
    /* access modifiers changed from: private */
    public SensorEventListener mEllipticSensorListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.values != null) {
                Slog.i("KeyguardViewMediator", "event.values[0]=" + sensorEvent.values[0]);
            } else {
                Slog.e("KeyguardViewMediator", "elliptic sensor values null");
            }
            float[] fArr = sensorEvent.values;
            if (fArr != null && ((double) fArr[0]) < 1.0d && !KeyguardViewMediator.this.mHiding && KeyguardViewMediator.this.mShowing && !KeyguardViewMediator.this.mUpdateMonitor.isFingerprintUnlock()) {
                Slog.i("KeyguardViewMediator", "keyguard_screen_off_reason:elliptic sensor too close");
                KeyguardViewMediator.this.mPM.goToSleep(SystemClock.uptimeMillis());
            }
            KeyguardViewMediator.this.unregisterEllipticSensor();
        }
    };
    private IKeyguardExitCallback mExitSecureCallback;
    /* access modifiers changed from: private */
    public boolean mExternallyEnabled = true;
    private FaceUnlockController mFaceUnlockController;
    private FingerprintUnlockController mFingerprintUnlockController;
    private long mFpAuthTime = 0;
    private volatile boolean mGoingToSleep;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler(Looper.myLooper(), (Handler.Callback) null, true) {
        public void handleMessage(Message message) {
            boolean z = true;
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
                    boolean z2 = message.arg1 != 0;
                    if (message.arg2 == 0) {
                        z = false;
                    }
                    keyguardViewMediator.handleSetOccluded(z2, z);
                    Trace.endSection();
                    return;
                case 10:
                    synchronized (KeyguardViewMediator.this) {
                        Slog.w("KeyguardViewMediator", "fw call doKeyguardTimeout");
                        KeyguardViewMediator.this.doKeyguardLocked((Bundle) message.obj);
                    }
                    return;
                case 11:
                    KeyguardViewMediator.this.handleDismiss((IKeyguardDismissCallback) message.obj);
                    return;
                case 12:
                    Trace.beginSection("KeyguardViewMediator#handleMessage START_KEYGUARD_EXIT_ANIM");
                    StartKeyguardExitAnimParams startKeyguardExitAnimParams = (StartKeyguardExitAnimParams) message.obj;
                    KeyguardViewMediator.this.handleStartKeyguardExitAnimation(startKeyguardExitAnimParams.startTime, startKeyguardExitAnimParams.fadeoutDuration);
                    FalsingManager.getInstance(KeyguardViewMediator.this.mContext).onSucccessfulUnlock();
                    Trace.endSection();
                    return;
                case 13:
                    Trace.beginSection("KeyguardViewMediator#handleMessage KEYGUARD_DONE_PENDING_TIMEOUT");
                    Log.w("KeyguardViewMediator", "Timeout while waiting for activity drawn!");
                    KeyguardViewMediator.this.mViewMediatorCallback.readyForKeyguardDone();
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
                    Trace.beginSection("KeyguardViewMediator#handleMessage SET_SWITCHING_USER");
                    KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(KeyguardViewMediator.this.mContext);
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    instance.setSwitchingUser(z);
                    Trace.endSection();
                    return;
                case 19:
                    Slog.w("KeyguardViewMediator", "fw call startKeyguardExitAnimation timeout");
                    KeyguardViewMediator.this.startKeyguardExitAnimation(SystemClock.uptimeMillis(), 0);
                    return;
                default:
                    return;
            }
        }
    };
    private Animation mHideAnimation;
    /* access modifiers changed from: private */
    public final Runnable mHideAnimationFinishedRunnable = new Runnable() {
        public void run() {
            boolean unused = KeyguardViewMediator.this.mHideAnimationRunning = false;
            KeyguardViewMediator.this.tryKeyguardDone();
        }
    };
    /* access modifiers changed from: private */
    public boolean mHideAnimationRun = false;
    /* access modifiers changed from: private */
    public boolean mHideAnimationRunning = false;
    private boolean mHideLockForLid;
    /* access modifiers changed from: private */
    public volatile boolean mHiding;
    private boolean mInputRestricted;
    private boolean mIsDeviceSupportEllipticSensor = false;
    private boolean mIsDeviceSupportLargeAreaTouch = false;
    /* access modifiers changed from: private */
    public KeyguardDisplayManager mKeyguardDisplayManager;
    /* access modifiers changed from: private */
    public boolean mKeyguardDonePending = false;
    private final Runnable mKeyguardGoingAwayRunnable = new Runnable() {
        public void run() {
            Trace.beginSection("KeyguardViewMediator.mKeyGuardGoingAwayRunnable");
            KeyguardViewMediator.this.mStatusBarKeyguardViewManager.keyguardGoingAway();
            KeyguardViewMediator.this.mUpdateMonitor.setKeyguardGoingAway(true);
            KeyguardViewMediator.this.mHandler.removeMessages(19);
            KeyguardViewMediator.this.mHandler.sendEmptyMessageDelayed(19, 1000);
            synchronized (KeyguardViewMediator.this) {
                long unused = KeyguardViewMediator.this.mKeyguardGoingAwayTime = System.currentTimeMillis();
            }
            if (((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock()) {
                KeyguardViewMediator.this.startKeyguardExitAnimation(SystemClock.uptimeMillis(), 0);
            } else {
                KeyguardViewMediator.this.keyguardGoingAway();
            }
            Trace.endSection();
        }
    };
    /* access modifiers changed from: private */
    public long mKeyguardGoingAwayTime = 0;
    private final ArrayList<IKeyguardStateCallbackCompat> mKeyguardStateCallbacks = new ArrayList<>();
    /* access modifiers changed from: private */
    public Sensor mLargeAreaTouchSensor = null;
    /* access modifiers changed from: private */
    public SensorEventListener mLargeAreaTouchSensorListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            float[] fArr = sensorEvent.values;
            if (fArr != null && fArr[0] == 1.0f && !KeyguardViewMediator.this.mHiding && KeyguardViewMediator.this.mShowing && !KeyguardViewMediator.this.mUpdateMonitor.isFingerprintUnlock()) {
                if (!KeyguardViewMediator.this.mOccluded || !MiuiKeyguardUtils.keepScreenOnWhenLargeAreaTouch(KeyguardViewMediator.this.mContext)) {
                    Slog.i("KeyguardViewMediator", "keyguard_screen_off_reason:large area touch");
                    KeyguardViewMediator.this.mPM.goToSleep(SystemClock.uptimeMillis());
                    AnalyticsHelper.getInstance(KeyguardViewMediator.this.mContext).record("keyguard_large_area_touch");
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final SparseArray<IccCardConstants.State> mLastSimStates = new SparseArray<>();
    private boolean mLockLater;
    /* access modifiers changed from: private */
    public LockPatternUtils mLockPatternUtils;
    private int mLockSoundId;
    /* access modifiers changed from: private */
    public int mLockSoundStreamId;
    /* access modifiers changed from: private */
    public float mLockSoundVolume;
    /* access modifiers changed from: private */
    public SoundPool mLockSounds;
    /* access modifiers changed from: private */
    public boolean mLockWhenSimRemoved;
    private boolean mNeedToReshowWhenReenabled = false;
    /* access modifiers changed from: private */
    public boolean mOccluded = false;
    /* access modifiers changed from: private */
    public PowerManager mPM;
    private boolean mPendingLock;
    private boolean mPendingReset;
    private String mPhoneState = TelephonyManager.EXTRA_STATE_IDLE;
    ContentObserver mPickupGestureWakeupObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            super.onChange(z);
            KeyguardViewMediator keyguardViewMediator = KeyguardViewMediator.this;
            boolean unused = keyguardViewMediator.mPickupGestureWakeupOpened = MiuiSettings.System.getBooleanForUser(keyguardViewMediator.mContext.getContentResolver(), "pick_up_gesture_wakeup_mode", false, KeyguardUpdateMonitor.getCurrentUser());
            if (!KeyguardViewMediator.this.mHiding && KeyguardViewMediator.this.mShowing && !KeyguardViewMediator.this.mUpdateMonitor.isFingerprintUnlock()) {
                if (KeyguardViewMediator.this.mPickupGestureWakeupOpened) {
                    KeyguardViewMediator.this.registerWakeupAndSleepSensor();
                } else {
                    KeyguardViewMediator.this.unregisterWakeupAndSleepSensor();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mPickupGestureWakeupOpened = false;
    /* access modifiers changed from: private */
    public boolean mReadyForKeyEvent = false;
    /* access modifiers changed from: private */
    public boolean mSendKeyEventScreenOn = false;
    /* access modifiers changed from: private */
    public SensorManager mSensorManager;
    private PowerManager.WakeLock mShowKeyguardWakeLock;
    private final BroadcastReceiver mShowUnlockScreenReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("xiaomi.intent.action.SHOW_SECURE_KEYGUARD".equals(intent.getAction()) && KeyguardViewMediator.this.mShowing) {
                KeyguardViewMediator.this.mHandler.post(new Runnable() {
                    public void run() {
                        KeyguardViewMediator.this.mStatusBarKeyguardViewManager.dismiss();
                    }
                });
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mShowing;
    /* access modifiers changed from: private */
    public boolean mShuttingDown;
    private boolean mSimLockedOrMissing;
    private SmartCoverHelper mSmartCoverHelper;
    /* access modifiers changed from: private */
    public StatusBar mStatusBar;
    /* access modifiers changed from: private */
    public StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    protected IStatusBarService mStatusBarService;
    private boolean mSystemReady;
    private IBinder mToken = new Binder();
    /* access modifiers changed from: private */
    public TrustManager mTrustManager;
    private int mTrustedSoundId;
    private final UiOffloadThread mUiOffloadThread = ((UiOffloadThread) Dependency.get(UiOffloadThread.class));
    /* access modifiers changed from: private */
    public int mUiSoundsStreamType;
    /* access modifiers changed from: private */
    public boolean mUnlockByFingerPrint = false;
    private int mUnlockSoundId;
    KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() {
        public void onUserInfoChanged(int i) {
        }

        public void onUserSwitching(int i) {
            synchronized (KeyguardViewMediator.this) {
                KeyguardViewMediator.this.resetKeyguardDonePendingLocked();
                KeyguardViewMediator.this.resetStateLocked();
                KeyguardViewMediator.this.adjustStatusBarLocked();
            }
        }

        public void onUserSwitchComplete(int i) {
            UserInfo userInfo;
            if (!(i == 0 || (userInfo = UserManager.get(KeyguardViewMediator.this.mContext).getUserInfo(i)) == null || (!userInfo.isGuest() && (userInfo.flags & 512) != 512))) {
                KeyguardViewMediator.this.dismiss((IKeyguardDismissCallback) null);
            }
            KeyguardViewMediator.this.mPickupGestureWakeupObserver.onChange(false);
        }

        public void onPhoneStateChanged(int i) {
            synchronized (KeyguardViewMediator.this) {
                if (i == 0) {
                    if (!KeyguardViewMediator.this.mDeviceInteractive && KeyguardViewMediator.this.mExternallyEnabled) {
                        Slog.w("KeyguardViewMediator", "screen is off and call ended, let's make sure the keyguard is showing");
                        KeyguardViewMediator.this.doKeyguardLocked((Bundle) null);
                    }
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
                    Slog.w("KeyguardViewMediator", "onDeviceProvisioned show keyguard");
                    KeyguardViewMediator.this.doKeyguardLocked((Bundle) null);
                }
            }
        }

        public void onSimStateChanged(int i, int i2, IccCardConstants.State state) {
            boolean z;
            Log.d("KeyguardViewMediator", "onSimStateChanged(subId=" + i + ", slotId=" + i2 + ",state=" + state + ")");
            KeyguardViewMediator.this.handleSimSecureStateChanged();
            synchronized (KeyguardViewMediator.this) {
                IccCardConstants.State state2 = (IccCardConstants.State) KeyguardViewMediator.this.mLastSimStates.get(i2);
                if (state2 != IccCardConstants.State.PIN_REQUIRED) {
                    if (state2 != IccCardConstants.State.PUK_REQUIRED) {
                        z = false;
                        KeyguardViewMediator.this.mLastSimStates.append(i2, state);
                    }
                }
                z = true;
                KeyguardViewMediator.this.mLastSimStates.append(i2, state);
            }
            switch (AnonymousClass26.$SwitchMap$com$android$internal$telephony$IccCardConstants$State[state.ordinal()]) {
                case 1:
                case 2:
                    synchronized (KeyguardViewMediator.this) {
                        if (KeyguardViewMediator.this.shouldWaitForProvisioning()) {
                            if (!KeyguardViewMediator.this.mShowing) {
                                Slog.w("KeyguardViewMediator", "ICC_ABSENT isn't showing, we need to show the keyguard since the device isn't provisioned yet.");
                                KeyguardViewMediator.this.doKeyguardLocked((Bundle) null);
                            } else {
                                KeyguardViewMediator.this.resetStateLocked();
                            }
                        }
                        if (state == IccCardConstants.State.ABSENT && z) {
                            Log.d("KeyguardViewMediator", "SIM moved to ABSENT when the previous state was locked. Reset the state.");
                            KeyguardViewMediator.this.resetStateLocked();
                        }
                    }
                    break;
                case 3:
                case 4:
                    synchronized (KeyguardViewMediator.this) {
                        if (!KeyguardViewMediator.this.mShowing) {
                            Slog.w("KeyguardViewMediator", "INTENT_VALUE_ICC_LOCKED and keygaurd isn't showing; need to show keyguard so user can enter sim pin");
                            KeyguardViewMediator.this.doKeyguardLocked((Bundle) null);
                        } else {
                            KeyguardViewMediator.this.resetStateLocked();
                        }
                    }
                    break;
                case 5:
                    synchronized (KeyguardViewMediator.this) {
                        if (!KeyguardViewMediator.this.mShowing) {
                            Slog.w("KeyguardViewMediator", "PERM_DISABLED and keygaurd isn't showing.");
                            KeyguardViewMediator.this.doKeyguardLocked((Bundle) null);
                        } else {
                            Log.d("KeyguardViewMediator", "PERM_DISABLED, resetStateLocked toshow permanently disabled message in lockscreen.");
                            KeyguardViewMediator.this.resetStateLocked();
                        }
                        onSimAbsentLocked();
                    }
                    break;
                case 6:
                    synchronized (KeyguardViewMediator.this) {
                        Log.d("KeyguardViewMediator", "READY, reset state? " + KeyguardViewMediator.this.mShowing);
                        if (KeyguardViewMediator.this.mShowing && z) {
                            KeyguardViewMediator.this.resetStateLocked();
                        }
                        boolean unused = KeyguardViewMediator.this.mLockWhenSimRemoved = true;
                    }
                    break;
                default:
                    Log.v("KeyguardViewMediator", "Unspecific state: " + state);
                    break;
            }
            if (state != IccCardConstants.State.READY) {
                KeyguardViewMediator.this.mUpdateMonitor.setSimStateEarlyReady(i2, false);
            }
        }

        private void onSimAbsentLocked() {
            if (KeyguardViewMediator.this.isSecure() && KeyguardViewMediator.this.mLockWhenSimRemoved && !KeyguardViewMediator.this.mShuttingDown) {
                boolean unused = KeyguardViewMediator.this.mLockWhenSimRemoved = false;
                KeyguardViewMediator keyguardViewMediator = KeyguardViewMediator.this;
                MetricsLogger.action(keyguardViewMediator.mContext, 496, keyguardViewMediator.mShowing);
                if (!KeyguardViewMediator.this.mShowing) {
                    Slog.w("KeyguardViewMediator", "SIM removed, showing keyguard");
                    KeyguardViewMediator.this.doKeyguardLocked((Bundle) null);
                }
            }
        }

        public void onFingerprintAuthFailed() {
            if (UnlockMethodCache.getInstance(KeyguardViewMediator.this.mContext).isMethodSecure(KeyguardUpdateMonitor.getCurrentUser())) {
                KeyguardViewMediator.this.mLockPatternUtils.getDevicePolicyManager();
            }
            if (KeyguardViewMediator.this.mUpdateMonitor.shouldListenForFingerprintWhenUnlocked()) {
                KeyguardViewMediator.this.mStatusBar.showBouncerIfKeyguard();
            }
        }

        public void onFingerprintAuthenticated(int i) {
            if (UnlockMethodCache.getInstance(KeyguardViewMediator.this.mContext).isMethodSecure(i)) {
                KeyguardViewMediator.this.mLockPatternUtils.getDevicePolicyManager();
                boolean unused = KeyguardViewMediator.this.mUnlockByFingerPrint = true;
            }
        }

        public void onKeyguardBouncerChanged(boolean z) {
            if (KeyguardViewMediator.this.mShowing) {
                KeyguardViewMediator.this.disableFullScreenGesture();
            }
        }
    };
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;
    Runnable mUserActivityRunnable = $$Lambda$KeyguardViewMediator$LXdi2YST5IQ4mr8ng9kZhZ0iNIQ.INSTANCE;
    ViewMediatorCallback mViewMediatorCallback = new ViewMediatorCallback() {
        public void userActivity() {
            KeyguardViewMediator.this.userActivity();
        }

        public void keyguardDone(boolean z, int i) {
            if (i == ActivityManager.getCurrentUser()) {
                KeyguardViewMediator.this.tryKeyguardDone();
                if (z) {
                    KeyguardViewMediator.this.mUpdateMonitor.reportSuccessfulStrongAuthUnlockAttempt();
                }
            }
        }

        public void keyguardDoneDrawing() {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#keyguardDoneDrawing");
            KeyguardViewMediator.this.mHandler.sendEmptyMessage(8);
            Trace.endSection();
        }

        public void setNeedsInput(boolean z) {
            KeyguardViewMediator.this.mStatusBarKeyguardViewManager.setNeedsInput(z);
        }

        public void keyguardDonePending(boolean z, int i) {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#keyguardDonePending");
            if (i != ActivityManager.getCurrentUser()) {
                Trace.endSection();
                return;
            }
            boolean unused = KeyguardViewMediator.this.mKeyguardDonePending = true;
            boolean unused2 = KeyguardViewMediator.this.mHideAnimationRun = true;
            boolean unused3 = KeyguardViewMediator.this.mHideAnimationRunning = true;
            KeyguardViewMediator.this.mStatusBarKeyguardViewManager.startPreHideAnimation(KeyguardViewMediator.this.mHideAnimationFinishedRunnable);
            KeyguardViewMediator.this.mHandler.sendEmptyMessageDelayed(13, 1000);
            if (z) {
                KeyguardViewMediator.this.mUpdateMonitor.reportSuccessfulStrongAuthUnlockAttempt();
            }
            Trace.endSection();
        }

        public void keyguardGone() {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#keyguardGone");
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
            KeyguardViewMediator.this.adjustStatusBarLocked(z);
        }

        public int getBouncerPromptReason() {
            return KeyguardViewMediator.this.getBouncerPromptReason(ActivityManager.getCurrentUser());
        }
    };
    private long mWaitFwTotalTime = 0;
    private volatile boolean mWaitSendUserPresent;
    private boolean mWaitingUntilKeyguardVisible = false;
    private boolean mWakeAndUnlocking;
    /* access modifiers changed from: private */
    public Sensor mWakeupAndSleepSensor = null;
    /* access modifiers changed from: private */
    public SensorEventListener mWakeupAndSleepSensorListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            float[] fArr = sensorEvent.values;
            if (fArr == null || fArr[0] != 1.0f) {
                float[] fArr2 = sensorEvent.values;
                if (fArr2 == null) {
                    return;
                }
                if ((fArr2[0] == 2.0f || fArr2[0] == 0.0f) && KeyguardViewMediator.this.mWakeupByPickUp && KeyguardViewMediator.this.isShowingAndNotOccluded()) {
                    Slog.i("KeyguardViewMediator", "keyguard_screen_off_reason:put down");
                    KeyguardViewMediator.this.mPM.goToSleep(SystemClock.uptimeMillis());
                    if (KeyguardViewMediator.this.mDisplay.getState() == 2) {
                        AnalyticsHelper.getInstance(KeyguardViewMediator.this.mContext).record("keyguard_sleep_by_put_down");
                        return;
                    }
                    return;
                }
                return;
            }
            if (KeyguardViewMediator.this.mDisplay.getState() != 2) {
                AnalyticsHelper.getInstance(KeyguardViewMediator.this.mContext).setWakeupWay("screen_on_by_pick_up");
                boolean unused = KeyguardViewMediator.this.mWakeupByPickUp = true;
            }
            KeyguardViewMediator.this.mPM.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:PICK_UP");
        }
    };
    /* access modifiers changed from: private */
    public boolean mWakeupByPickUp = false;
    private KeyguardWallpaperHelper mWallpaperHelper;
    private WorkLockActivityController mWorkLockController;

    public void onShortPowerPressedGoHome() {
    }

    /* renamed from: com.android.systemui.keyguard.KeyguardViewMediator$26  reason: invalid class name */
    static /* synthetic */ class AnonymousClass26 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$IccCardConstants$State = new int[IccCardConstants.State.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(12:0|1|2|3|4|5|6|7|8|9|10|(3:11|12|14)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x0040 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0014 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x002a */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0035 */
        static {
            /*
                com.android.internal.telephony.IccCardConstants$State[] r0 = com.android.internal.telephony.IccCardConstants.State.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$android$internal$telephony$IccCardConstants$State = r0
                int[] r0 = $SwitchMap$com$android$internal$telephony$IccCardConstants$State     // Catch:{ NoSuchFieldError -> 0x0014 }
                com.android.internal.telephony.IccCardConstants$State r1 = com.android.internal.telephony.IccCardConstants.State.NOT_READY     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                int[] r0 = $SwitchMap$com$android$internal$telephony$IccCardConstants$State     // Catch:{ NoSuchFieldError -> 0x001f }
                com.android.internal.telephony.IccCardConstants$State r1 = com.android.internal.telephony.IccCardConstants.State.ABSENT     // Catch:{ NoSuchFieldError -> 0x001f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                int[] r0 = $SwitchMap$com$android$internal$telephony$IccCardConstants$State     // Catch:{ NoSuchFieldError -> 0x002a }
                com.android.internal.telephony.IccCardConstants$State r1 = com.android.internal.telephony.IccCardConstants.State.PIN_REQUIRED     // Catch:{ NoSuchFieldError -> 0x002a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x002a }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x002a }
            L_0x002a:
                int[] r0 = $SwitchMap$com$android$internal$telephony$IccCardConstants$State     // Catch:{ NoSuchFieldError -> 0x0035 }
                com.android.internal.telephony.IccCardConstants$State r1 = com.android.internal.telephony.IccCardConstants.State.PUK_REQUIRED     // Catch:{ NoSuchFieldError -> 0x0035 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0035 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0035 }
            L_0x0035:
                int[] r0 = $SwitchMap$com$android$internal$telephony$IccCardConstants$State     // Catch:{ NoSuchFieldError -> 0x0040 }
                com.android.internal.telephony.IccCardConstants$State r1 = com.android.internal.telephony.IccCardConstants.State.PERM_DISABLED     // Catch:{ NoSuchFieldError -> 0x0040 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0040 }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0040 }
            L_0x0040:
                int[] r0 = $SwitchMap$com$android$internal$telephony$IccCardConstants$State     // Catch:{ NoSuchFieldError -> 0x004b }
                com.android.internal.telephony.IccCardConstants$State r1 = com.android.internal.telephony.IccCardConstants.State.READY     // Catch:{ NoSuchFieldError -> 0x004b }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x004b }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x004b }
            L_0x004b:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.keyguard.KeyguardViewMediator.AnonymousClass26.<clinit>():void");
        }
    }

    public int getBouncerPromptReason(int i) {
        boolean isTrustUsuallyManaged = TrustManagerCompat.isTrustUsuallyManaged(this.mTrustManager, i);
        boolean isUnlockWithFingerprintPossible = this.mUpdateMonitor.isUnlockWithFingerprintPossible(i);
        boolean shouldListenForFaceUnlock = FaceUnlockManager.getInstance().shouldListenForFaceUnlock();
        boolean isUnlockWithBlePossible = this.mBleUnlockHelper.isUnlockWithBlePossible();
        boolean z = isTrustUsuallyManaged || isUnlockWithFingerprintPossible || shouldListenForFaceUnlock || isUnlockWithBlePossible;
        KeyguardUpdateMonitor.StrongAuthTracker strongAuthTracker = this.mUpdateMonitor.getStrongAuthTracker();
        int strongAuthForUser = strongAuthTracker.getStrongAuthForUser(i);
        Log.i("KeyguardViewMediator", "getBouncerPromptReason trust = " + isTrustUsuallyManaged + " fingerprint = " + isUnlockWithFingerprintPossible + " faceUnlock = " + shouldListenForFaceUnlock + " bleUnlock = " + isUnlockWithBlePossible + " strongAuth = " + strongAuthForUser + ", userId = " + i);
        if (!strongAuthTracker.hasUserAuthenticatedSinceBoot()) {
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
        if (!z || (strongAuthForUser & 8) == 0) {
            return 0;
        }
        return 5;
    }

    public void handleSimSecureStateChanged() {
        int size = this.mKeyguardStateCallbacks.size();
        boolean isSimPinSecure = this.mUpdateMonitor.isSimPinSecure();
        for (int i = size - 1; i >= 0; i--) {
            try {
                this.mKeyguardStateCallbacks.get(i).onSimSecureStateChanged(isSimPinSecure);
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call onSimSecureStateChanged", e);
                if (e instanceof DeadObjectException) {
                    this.mKeyguardStateCallbacks.remove(i);
                }
            }
        }
    }

    public void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
        this.mHandler.removeCallbacks(this.mUserActivityRunnable);
        this.mHandler.post(this.mUserActivityRunnable);
    }

    /* access modifiers changed from: package-private */
    public boolean mustNotUnlockCurrentUser() {
        return (UserManagerCompat.isSplitSystemUser() || com.android.systemui.proxy.UserManager.isDeviceInDemoMode(this.mContext)) && KeyguardUpdateMonitor.getCurrentUser() == 0;
    }

    private void setupLocked() {
        this.mPM = (PowerManager) this.mContext.getSystemService("power");
        this.mTrustManager = (TrustManager) this.mContext.getSystemService("trust");
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        this.mShowKeyguardWakeLock = this.mPM.newWakeLock(1, "show keyguard");
        this.mShowKeyguardWakeLock.setReferenceCounted(false);
        this.mBleUnlockHelper = new MiuiBleUnlockHelper(this.mContext, this);
        this.mSmartCoverHelper = new SmartCoverHelper(this.mContext, this);
        this.mWallpaperHelper = new KeyguardWallpaperHelper(this.mContext);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD");
        intentFilter.addAction("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK");
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mContext.registerReceiverAsUser(this.mShowUnlockScreenReceiver, UserHandle.ALL, new IntentFilter("xiaomi.intent.action.SHOW_SECURE_KEYGUARD"), (String) null, (Handler) null);
        this.mKeyguardDisplayManager = new KeyguardDisplayManager(this.mContext);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mUpdateMonitor.setKeyguardViewMediator(this);
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        KeyguardUpdateMonitor.setCurrentUser(ActivityManager.getCurrentUser());
        if (this.mContext.getResources().getBoolean(R.bool.config_enableKeyguardService)) {
            setShowingLocked(!shouldWaitForProvisioning() && !this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser()), true);
        }
        this.mStatusBarKeyguardViewManager = SystemUIFactory.getInstance().createStatusBarKeyguardViewManager(this.mContext, this.mViewMediatorCallback, this.mLockPatternUtils);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        this.mDeviceInteractive = this.mPM.isInteractive();
        this.mLockSounds = new SoundPool(1, 1, 0);
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
        this.mLockSoundVolume = (float) Math.pow(10.0d, (double) (((float) this.mContext.getResources().getInteger(17694822)) / 20.0f));
        this.mHideAnimation = AnimationUtils.loadAnimation(this.mContext, 17432680);
        this.mWorkLockController = new WorkLockActivityController(this.mContext);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("pick_up_gesture_wakeup_mode"), false, this.mPickupGestureWakeupObserver, -1);
        this.mPickupGestureWakeupObserver.onChange(false);
        this.mIsDeviceSupportLargeAreaTouch = isDeviceSupportLargeAreaTouch();
        this.mIsDeviceSupportEllipticSensor = isDeviceSupportEllipticSensor();
    }

    public void start() {
        synchronized (this) {
            setupLocked();
        }
        putComponent(KeyguardViewMediator.class, this);
    }

    public void onSystemReady() {
        synchronized (this) {
            Slog.w("KeyguardViewMediator", "onSystemReady");
            this.mSystemReady = true;
            doKeyguardLocked((Bundle) null);
            this.mUpdateMonitor.registerCallback(this.mUpdateCallback);
        }
        maybeSendUserPresentBroadcast();
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x006b A[Catch:{ RemoteException -> 0x0078 }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x008b A[Catch:{ RemoteException -> 0x0078 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onStartedGoingToSleep(int r6) {
        /*
            r5 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "onStartedGoingToSleep("
            r0.append(r1)
            r0.append(r6)
            java.lang.String r1 = ")"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "KeyguardViewMediator"
            android.util.Log.d(r1, r0)
            com.android.systemui.statusbar.phone.FingerprintUnlockController r0 = r5.mFingerprintUnlockController
            r0.resetCancelingPendingLock()
            com.android.keyguard.faceunlock.FaceUnlockController r0 = r5.mFaceUnlockController
            r0.resetFaceUnlockMode()
            monitor-enter(r5)
            r0 = 0
            r5.mDeviceInteractive = r0     // Catch:{ all -> 0x00ef }
            r1 = 1
            r5.mGoingToSleep = r1     // Catch:{ all -> 0x00ef }
            r5.mWakeupByPickUp = r0     // Catch:{ all -> 0x00ef }
            r5.mUnlockByFingerPrint = r0     // Catch:{ all -> 0x00ef }
            r5.mReadyForKeyEvent = r0     // Catch:{ all -> 0x00ef }
            r5.mSendKeyEventScreenOn = r0     // Catch:{ all -> 0x00ef }
            android.content.Context r2 = r5.mContext     // Catch:{ all -> 0x00ef }
            com.android.keyguard.analytics.AnalyticsHelper r2 = com.android.keyguard.analytics.AnalyticsHelper.getInstance(r2)     // Catch:{ all -> 0x00ef }
            r2.resetAnalyticsParams()     // Catch:{ all -> 0x00ef }
            r5.resetFingerprintUnlockState()     // Catch:{ all -> 0x00ef }
            int r2 = com.android.keyguard.KeyguardUpdateMonitor.getCurrentUser()     // Catch:{ all -> 0x00ef }
            android.content.Context r3 = r5.mContext     // Catch:{ all -> 0x00ef }
            com.android.systemui.statusbar.phone.UnlockMethodCache r3 = com.android.systemui.statusbar.phone.UnlockMethodCache.getInstance(r3)     // Catch:{ all -> 0x00ef }
            r3.updateSecure()     // Catch:{ all -> 0x00ef }
            com.android.internal.widget.LockPatternUtils r3 = r5.mLockPatternUtils     // Catch:{ all -> 0x00ef }
            boolean r3 = r3.getPowerButtonInstantlyLocks(r2)     // Catch:{ all -> 0x00ef }
            if (r3 != 0) goto L_0x0064
            android.content.Context r3 = r5.mContext     // Catch:{ all -> 0x00ef }
            com.android.systemui.statusbar.phone.UnlockMethodCache r3 = com.android.systemui.statusbar.phone.UnlockMethodCache.getInstance(r3)     // Catch:{ all -> 0x00ef }
            boolean r3 = r3.isMethodSecure(r2)     // Catch:{ all -> 0x00ef }
            if (r3 != 0) goto L_0x0062
            goto L_0x0064
        L_0x0062:
            r3 = r0
            goto L_0x0065
        L_0x0064:
            r3 = r1
        L_0x0065:
            r5.mLockLater = r0     // Catch:{ all -> 0x00ef }
            com.android.internal.policy.IKeyguardExitCallback r4 = r5.mExitSecureCallback     // Catch:{ all -> 0x00ef }
            if (r4 == 0) goto L_0x008b
            java.lang.String r2 = "KeyguardViewMediator"
            java.lang.String r3 = "pending exit secure callback cancelled"
            android.util.Log.d(r2, r3)     // Catch:{ all -> 0x00ef }
            com.android.internal.policy.IKeyguardExitCallback r2 = r5.mExitSecureCallback     // Catch:{ RemoteException -> 0x0078 }
            r2.onKeyguardExitResult(r0)     // Catch:{ RemoteException -> 0x0078 }
            goto L_0x0080
        L_0x0078:
            r0 = move-exception
            java.lang.String r2 = "KeyguardViewMediator"
            java.lang.String r3 = "Failed to call onKeyguardExitResult(false)"
            android.util.Slog.w(r2, r3, r0)     // Catch:{ all -> 0x00ef }
        L_0x0080:
            r0 = 0
            r5.mExitSecureCallback = r0     // Catch:{ all -> 0x00ef }
            boolean r0 = r5.mExternallyEnabled     // Catch:{ all -> 0x00ef }
            if (r0 != 0) goto L_0x00c8
            r5.hideLocked()     // Catch:{ all -> 0x00ef }
            goto L_0x00c8
        L_0x008b:
            boolean r0 = r5.mShowing     // Catch:{ all -> 0x00ef }
            if (r0 == 0) goto L_0x00af
            boolean r0 = r5.mHiding     // Catch:{ all -> 0x00ef }
            if (r0 != 0) goto L_0x00af
            java.lang.Class<com.android.keyguard.MiuiFastUnlockController> r0 = com.android.keyguard.MiuiFastUnlockController.class
            java.lang.Object r0 = com.android.systemui.Dependency.get(r0)     // Catch:{ all -> 0x00ef }
            com.android.keyguard.MiuiFastUnlockController r0 = (com.android.keyguard.MiuiFastUnlockController) r0     // Catch:{ all -> 0x00ef }
            boolean r0 = r0.isFastUnlock()     // Catch:{ all -> 0x00ef }
            if (r0 != 0) goto L_0x00af
            android.os.Handler r0 = r5.mHandler     // Catch:{ all -> 0x00ef }
            r2 = 7
            r0.removeMessages(r2)     // Catch:{ all -> 0x00ef }
            r5.mPendingReset = r1     // Catch:{ all -> 0x00ef }
            com.android.systemui.statusbar.phone.FingerprintUnlockController r0 = r5.mFingerprintUnlockController     // Catch:{ all -> 0x00ef }
            r0.resetMode()     // Catch:{ all -> 0x00ef }
            goto L_0x00c8
        L_0x00af:
            r0 = 2
            if (r6 != r0) goto L_0x00be
            if (r3 != 0) goto L_0x00be
            long r2 = r5.getLockTimeout(r2)     // Catch:{ all -> 0x00ef }
            r5.doKeyguardLaterLocked(r2)     // Catch:{ all -> 0x00ef }
            r5.mLockLater = r1     // Catch:{ all -> 0x00ef }
            goto L_0x00c8
        L_0x00be:
            com.android.internal.widget.LockPatternUtils r0 = r5.mLockPatternUtils     // Catch:{ all -> 0x00ef }
            boolean r0 = r0.isLockScreenDisabled(r2)     // Catch:{ all -> 0x00ef }
            if (r0 != 0) goto L_0x00c8
            r5.mPendingLock = r1     // Catch:{ all -> 0x00ef }
        L_0x00c8:
            r5.registerWakeupAndSleepSensor()     // Catch:{ all -> 0x00ef }
            boolean r0 = r5.mPendingLock     // Catch:{ all -> 0x00ef }
            if (r0 == 0) goto L_0x00d5
            r0 = 3
            if (r6 == r0) goto L_0x00d5
            r5.playSounds(r1)     // Catch:{ all -> 0x00ef }
        L_0x00d5:
            monitor-exit(r5)     // Catch:{ all -> 0x00ef }
            android.content.Context r0 = r5.mContext
            com.android.keyguard.KeyguardUpdateMonitor r0 = com.android.keyguard.KeyguardUpdateMonitor.getInstance(r0)
            r0.dispatchStartedGoingToSleep(r6)
            r5.notifyStartedGoingToSleep()
            com.android.systemui.recents.events.RecentsEventBus r5 = com.android.systemui.recents.events.RecentsEventBus.getDefault()
            com.android.systemui.events.ScreenOffEvent r6 = new com.android.systemui.events.ScreenOffEvent
            r6.<init>()
            r5.post(r6)
            return
        L_0x00ef:
            r6 = move-exception
            monitor-exit(r5)     // Catch:{ all -> 0x00ef }
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.keyguard.KeyguardViewMediator.onStartedGoingToSleep(int):void");
    }

    public void cancelPendingLock() {
        synchronized (this) {
            if (this.mPendingLock) {
                this.mPendingLock = false;
                playSounds(false);
                resetAppLock();
                this.mHandler.removeMessages(1);
            }
        }
    }

    private void resetAppLock() {
        SecurityManager securityManager = (SecurityManager) this.mContext.getSystemService("security");
        if (securityManager != null) {
            securityManager.removeAccessControlPassAsUser("*", -1);
        }
    }

    public void onFinishedGoingToSleep(int i, boolean z) {
        Log.d("KeyguardViewMediator", "onFinishedGoingToSleep(" + i + ")");
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().disableReadingMode();
        }
        synchronized (this) {
            this.mDeviceInteractive = false;
            this.mGoingToSleep = false;
            resetKeyguardDonePendingLocked();
            this.mHideAnimationRun = false;
            notifyFinishedGoingToSleep();
            if (z) {
                Log.i("KeyguardViewMediator", "Camera gesture was triggered, preventing Keyguard locking.");
                ((PowerManager) this.mContext.getSystemService(PowerManager.class)).wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:CAMERA_GESTURE_PREVENT_LOCK");
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
        KeyguardUpdateMonitor.getInstance(this.mContext).dispatchFinishedGoingToSleep(i);
        AnalyticsHelper.getInstance(this.mContext).trackPageEnd("keyguard_view_main_lock_screen", "screen_turned_off");
    }

    private long getLockTimeout(int i) {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        long j = (long) Settings.Secure.getInt(contentResolver, "lock_screen_lock_after_timeout", 5000);
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
        intent.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
        this.mAlarmManager.setExactAndAllowWhileIdle(2, elapsedRealtime, PendingIntent.getBroadcast(this.mContext, 0, intent, MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL));
        Log.d("KeyguardViewMediator", "setting alarm to turn off keyguard, seq = " + this.mDelayedShowingSequence);
        doKeyguardLaterForChildProfilesLocked();
    }

    private void doKeyguardLaterForChildProfilesLocked() {
        for (int i : UserManagerCompat.getEnabledProfileIds(UserManager.get(this.mContext), UserHandle.myUserId())) {
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(i)) {
                long lockTimeout = getLockTimeout(i);
                if (lockTimeout == 0) {
                    doKeyguardForChildProfilesLocked();
                } else {
                    long elapsedRealtime = SystemClock.elapsedRealtime() + lockTimeout;
                    Intent intent = new Intent("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK");
                    intent.putExtra("seq", this.mDelayedProfileShowingSequence);
                    intent.putExtra("android.intent.extra.USER_ID", i);
                    intent.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
                    this.mAlarmManager.setExactAndAllowWhileIdle(2, elapsedRealtime, PendingIntent.getBroadcast(this.mContext, 0, intent, MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL));
                }
            }
        }
    }

    private void doKeyguardForChildProfilesLocked() {
        for (int i : UserManagerCompat.getEnabledProfileIds(UserManager.get(this.mContext), UserHandle.myUserId())) {
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
        KeyguardUpdateMonitor.getInstance(this.mContext).dispatchStartedWakingUp();
        maybeSendUserPresentBroadcast();
        RecentsEventBus.getDefault().post(new ScreenOnEvent());
        Trace.endSection();
    }

    public void onStartedWakingUp(String str) {
        Log.d("KeyguardViewMediator", "onStartedWakingUp, reason = " + str);
        synchronized (this) {
            if (!TextUtils.isEmpty(str)) {
                AnalyticsHelper.getInstance(this.mContext).setWakeupWay(str);
            }
            KeyguardUpdateMonitor.getInstance(this.mContext).dispatchStartedWakingUpWithReason(str);
            this.mFingerprintUnlockController.onStartedWakingUpReason(str);
            ((MiuiKeyguardWallpaperController) Dependency.get(MiuiKeyguardWallpaperController.class)).preWakeUpWithReason(str);
        }
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
        if (this.mSystemReady && (this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser()) || this.mWaitSendUserPresent)) {
            this.mWaitSendUserPresent = false;
            sendUserPresentBroadcast();
        } else if (this.mSystemReady && shouldWaitForProvisioning()) {
            this.mLockPatternUtils.userPresent(KeyguardUpdateMonitor.getCurrentUser());
        }
    }

    public void onDreamingStarted() {
        KeyguardUpdateMonitor.getInstance(this.mContext).dispatchDreamingStarted();
        synchronized (this) {
            if (this.mDeviceInteractive && UnlockMethodCache.getInstance(this.mContext).isMethodSecure()) {
                doKeyguardLaterLocked();
            }
        }
    }

    public void onDreamingStopped() {
        KeyguardUpdateMonitor.getInstance(this.mContext).dispatchDreamingStopped();
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

    public boolean isHiding() {
        return this.mHiding;
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public boolean isOccluded() {
        return this.mOccluded;
    }

    public boolean isShowingAndOccluded() {
        return this.mShowing && this.mOccluded;
    }

    public void setOccluded(boolean z, boolean z2) {
        Trace.beginSection("KeyguardViewMediator#setOccluded");
        Slog.i("KeyguardViewMediator", "setOccluded " + z);
        if (this.mOccluded != z) {
            notifyAodOccludChanged(z);
        }
        this.mHandler.removeMessages(9);
        this.mHandler.sendMessageAtFrontOfQueue(this.mHandler.obtainMessage(9, z ? 1 : 0, z2 ? 1 : 0));
        Trace.endSection();
    }

    public void setAodShowing(boolean z) {
        Log.i("KeyguardViewMediator", "setAodShowing: " + z);
        if (z != this.mAodShowing) {
            this.mAodShowing = z;
            if (this.mAodShowing) {
                notifyAodOccludChanged(this.mOccluded);
            }
            updateActivityLockScreenState(this.mShowing);
        }
    }

    private void notifyAodOccludChanged(boolean z) {
        if (Dependency.getHost() != null && ((MiuiKeyguardWallpaperController) Dependency.get(MiuiKeyguardWallpaperController.class)).isWallpaperSupportsAmbientMode()) {
            Dependency.getHost().sendCommand("keyguard_occluded", z ? 1 : 0, (Bundle) null);
        }
    }

    /* access modifiers changed from: private */
    public void handleSetOccluded(boolean z, boolean z2) {
        Trace.beginSection("KeyguardViewMediator#handleSetOccluded");
        synchronized (this) {
            if (this.mHiding && z) {
                startKeyguardExitAnimation(0, 0);
            }
            if (this.mOccluded != z) {
                this.mOccluded = z;
                this.mUpdateMonitor.setKeyguardShowingAndOccluded(this.mShowing, this.mOccluded);
                this.mSmartCoverHelper.refreshSmartCover();
                if (z) {
                    AnalyticsHelper.getInstance(this.mContext).trackPageEnd("keyguard_view_main_lock_screen", "occluded");
                    AnalyticsHelper.getInstance(this.mContext).trackPageEnd("action_enter_left_view");
                    AnalyticsHelper.getInstance(this.mContext).trackPageEnd("action_enter_camera_view");
                } else {
                    trackPageStart();
                }
                this.mStatusBarKeyguardViewManager.setOccluded(z, z2 && this.mDeviceInteractive);
                if (!z && this.mDeviceInteractive) {
                    this.mBleUnlockHelper.verifyBLEDeviceRssi();
                }
                adjustStatusBarLocked();
                LockScreenMagazineUtils.sendLockScreenMagazineEventBroadcast(this.mContext, this.mOccluded ? "Wallpaper_Covered" : "Wallpaper_Uncovered");
            }
            if (this.mShowing) {
                disableFullScreenGesture();
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
                IKeyguardStateCallbackCompat iKeyguardStateCallbackCompat = this.mKeyguardStateCallbacks.get(size);
                try {
                    iKeyguardStateCallbackCompat.onInputRestrictedStateChanged(isInputRestricted);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onDeviceProvisioned", e);
                    if (e instanceof DeadObjectException) {
                        this.mKeyguardStateCallbacks.remove(iKeyguardStateCallbackCompat);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void doKeyguardLocked(Bundle bundle) {
        if (!this.mExternallyEnabled) {
            Slog.w("KeyguardViewMediator", "doKeyguard: not showing because externally disabled");
        } else if (!this.mStatusBarKeyguardViewManager.isShowing() || ((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock()) {
            if (!mustNotUnlockCurrentUser() || !this.mUpdateMonitor.isDeviceProvisioned()) {
                this.mSimLockedOrMissing = this.mUpdateMonitor.isSimPinSecure() || ((SubscriptionManager.isValidSubscriptionId(this.mUpdateMonitor.getNextSubIdForState(IccCardConstants.State.ABSENT)) || SubscriptionManager.isValidSubscriptionId(this.mUpdateMonitor.getNextSubIdForState(IccCardConstants.State.PERM_DISABLED))) && (SystemProperties.getBoolean("keyguard.no_require_sim", false) ^ true));
                if (this.mSimLockedOrMissing || !shouldWaitForProvisioning()) {
                    UnlockMethodCache.getInstance(this.mContext).updateSecure();
                    boolean z = this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser()) || (!isSecure() && MiuiKeyguardUtils.showMXTelcelLockScreen(this.mContext));
                    boolean z2 = bundle != null && bundle.getBoolean("force_show", false);
                    if (z && !this.mSimLockedOrMissing && !z2) {
                        Slog.w("KeyguardViewMediator", "doKeyguard: not showing because lockscreen is off");
                        return;
                    } else if (this.mLockPatternUtils.checkVoldPassword(KeyguardUpdateMonitor.getCurrentUser())) {
                        Slog.w("KeyguardViewMediator", "Not showing lock screen since just decrypted");
                        setShowingLocked(false);
                        hideLocked();
                        this.mUpdateMonitor.reportSuccessfulStrongAuthUnlockAttempt();
                        return;
                    } else if (this.mHideLockForLid && !this.mSimLockedOrMissing && !isSecure()) {
                        Slog.w("KeyguardViewMediator", "Not showing lock screen since in smart cover mode");
                        if (this.mShowing) {
                            handleHide();
                        }
                        this.mWaitSendUserPresent = true;
                        return;
                    }
                } else {
                    Slog.w("KeyguardViewMediator", "doKeyguard: not showing because device isn't provisioned and the sim is not locked or missing");
                    return;
                }
            }
            if (this.mFingerprintUnlockController.isCancelingPendingLock()) {
                Slog.w("KeyguardViewMediator", "doKeyguard: not showing because canceling pending lock");
                return;
            }
            Slog.w("KeyguardViewMediator", "doKeyguard: showing the lock screen");
            showLocked(bundle);
        } else {
            Slog.w("KeyguardViewMediator", "doKeyguard: not showing because it is already showing");
            resetStateLocked();
        }
    }

    public void setHideLockForLid(boolean z) {
        this.mHideLockForLid = z;
    }

    public boolean isSimLockedOrMissing() {
        return this.mSimLockedOrMissing;
    }

    /* access modifiers changed from: private */
    public void lockProfile(int i) {
        TrustManagerCompat.setDeviceLockedForUser(this.mTrustManager, i, true);
    }

    /* access modifiers changed from: private */
    public boolean shouldWaitForProvisioning() {
        return !this.mUpdateMonitor.isDeviceProvisioned() && !isSecure();
    }

    /* access modifiers changed from: private */
    public void handleDismiss(IKeyguardDismissCallback iKeyguardDismissCallback) {
        if (this.mShowing) {
            if (iKeyguardDismissCallback != null) {
                this.mDismissCallbackRegistry.addCallback(iKeyguardDismissCallback);
            }
            this.mStatusBarKeyguardViewManager.dismissAndCollapse();
        } else if (iKeyguardDismissCallback != null) {
            new DismissCallbackWrapper(iKeyguardDismissCallback).notifyDismissError();
        }
    }

    public void dismiss(IKeyguardDismissCallback iKeyguardDismissCallback) {
        this.mHandler.obtainMessage(11, iKeyguardDismissCallback).sendToTarget();
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
        Slog.w("KeyguardViewMediator", "notifyScreenOn");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(6, iKeyguardDrawnCallback));
        if (this.mFingerprintUnlockController.isCancelingPendingLock()) {
            synchronized (this) {
                if (this.mFpAuthTime != 0) {
                    this.mWaitFwTotalTime = System.currentTimeMillis() - this.mFpAuthTime;
                }
            }
        }
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
        this.mWaitSendUserPresent = false;
        this.mShowKeyguardWakeLock.acquire();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, bundle));
        this.mUpdateMonitor.setKeyguardHide(false);
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
        return UnlockMethodCache.getInstance(this.mContext).isMethodSecure(i) || KeyguardUpdateMonitor.getInstance(this.mContext).isSimPinSecure();
    }

    public void setSwitchingUser(boolean z) {
        Trace.beginSection("KeyguardViewMediator#setSwitchingUser");
        this.mHandler.removeMessages(18);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(18, z ? 1 : 0, 0));
        Trace.endSection();
    }

    public void setCurrentUser(int i) {
        KeyguardUpdateMonitor.setCurrentUser(i);
        PanelBar.LOG(KeyguardViewMediator.class, "setCurrentUser=" + i);
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
        Log.d("KeyguardViewMediator", "tryKeyguardDone mKeyguardDonePending = " + this.mKeyguardDonePending + " mHideAnimationRun = " + this.mHideAnimationRun + " mHideAnimationRunning = " + this.mHideAnimationRunning);
        if (!this.mKeyguardDonePending && this.mHideAnimationRun && !this.mHideAnimationRunning) {
            handleKeyguardDone();
        } else if (!this.mHideAnimationRun) {
            this.mHideAnimationRun = true;
            this.mHideAnimationRunning = true;
            this.mStatusBarKeyguardViewManager.startPreHideAnimation(this.mHideAnimationFinishedRunnable);
        }
    }

    /* access modifiers changed from: private */
    public void handleKeyguardDone() {
        Trace.beginSection("KeyguardViewMediator#handleKeyguardDone");
        final int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        this.mUiOffloadThread.submit(new Runnable() {
            public void run() {
                if (UnlockMethodCache.getInstance(KeyguardViewMediator.this.mContext).isMethodSecure(currentUser)) {
                    DevicePolicyManagerCompat.reportKeyguardDismissed(KeyguardViewMediator.this.mLockPatternUtils.getDevicePolicyManager(), currentUser);
                }
            }
        });
        Log.d("KeyguardViewMediator", "handleKeyguardDone");
        synchronized (this) {
            resetKeyguardDonePendingLocked();
        }
        this.mUpdateMonitor.clearFailedUnlockAttempts();
        this.mUpdateMonitor.clearFingerprintRecognized();
        this.mUpdateMonitor.resetAllFingerprintLockout();
        this.mBleUnlockHelper.unregisterUnlockListener();
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().setUnlockLockout(false);
        }
        if (!this.mGoingToSleep || ((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock()) {
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
            recordUnlockEvent();
            Trace.endSection();
            return;
        }
        this.mFingerprintUnlockController.resetMode();
        this.mFaceUnlockController.resetFaceUnlockMode();
        Log.i("KeyguardViewMediator", "Device is going to sleep, aborting keyguardDone");
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    public void sendUserPresentBroadcast() {
        synchronized (this) {
            if (this.mBootCompleted) {
                final int currentUser = KeyguardUpdateMonitor.getCurrentUser();
                final UserHandle userHandle = new UserHandle(currentUser);
                final UserManager userManager = (UserManager) this.mContext.getSystemService("user");
                this.mUiOffloadThread.submit(new Runnable() {
                    public void run() {
                        for (int of : UserManagerCompat.getProfileIdsWithDisabled(userManager, userHandle.getIdentifier())) {
                            KeyguardViewMediator.this.mContext.sendBroadcastAsUser(KeyguardViewMediator.USER_PRESENT_INTENT, UserHandleCompat.of(of));
                        }
                        KeyguardViewMediator.this.mLockPatternUtils.userPresent(currentUser);
                    }
                });
            } else {
                this.mBootSendUserPresent = true;
            }
        }
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

    private void playSound(final int i) {
        if (i != 0 && Settings.System.getIntForUser(this.mContext.getContentResolver(), "lockscreen_sounds_enabled", 1, KeyguardUpdateMonitor.getCurrentUser()) == 1) {
            this.mLockSounds.stop(this.mLockSoundStreamId);
            if (this.mAudioManager == null) {
                this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
                AudioManager audioManager = this.mAudioManager;
                if (audioManager != null) {
                    this.mUiSoundsStreamType = audioManager.getUiSoundsStreamType();
                } else {
                    return;
                }
            }
            this.mUiOffloadThread.submit(new Runnable() {
                public void run() {
                    if (!KeyguardViewMediator.this.mAudioManager.isStreamMute(KeyguardViewMediator.this.mUiSoundsStreamType)) {
                        int play = KeyguardViewMediator.this.mLockSounds.play(i, KeyguardViewMediator.this.mLockSoundVolume, KeyguardViewMediator.this.mLockSoundVolume, 1, 0, 1.0f);
                        synchronized (this) {
                            int unused = KeyguardViewMediator.this.mLockSoundStreamId = play;
                        }
                    }
                }
            });
        }
    }

    private void updateActivityLockScreenState(boolean z) {
        try {
            ActivityManagerCompat.setLockScreenShown(z, this.mOccluded, this.mAodShowing);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void handleShow(Bundle bundle) {
        Trace.beginSection("KeyguardViewMediator#handleShow");
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (UnlockMethodCache.getInstance(this.mContext).isMethodSecure(currentUser)) {
            DevicePolicyManagerCompat.reportKeyguardSecured(this.mLockPatternUtils.getDevicePolicyManager(), currentUser);
        }
        synchronized (this) {
            if (!this.mSystemReady) {
                Log.d("KeyguardViewMediator", "ignoring handleShow because system is not ready.");
                this.mUpdateMonitor.setKeyguardHide(true);
                Trace.endSection();
                return;
            }
            Log.d("KeyguardViewMediator", "handleShow");
            setShowingLocked(true);
            this.mStatusBarKeyguardViewManager.show(bundle);
            this.mHiding = false;
            this.mWakeAndUnlocking = false;
            resetKeyguardDonePendingLocked();
            this.mHideAnimationRun = false;
            adjustStatusBarLocked();
            userActivity();
            ((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).resetFastUnlockState();
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                MiuiGxzwManager.getInstance().onKeyguardShow();
            }
            this.mShowKeyguardWakeLock.release();
            this.mUpdateMonitor.setKeyguardHide(false);
            this.mKeyguardDisplayManager.show();
            disableFullScreenGesture();
            resetAppLock();
            Trace.endSection();
        }
    }

    /* access modifiers changed from: private */
    public void keyguardGoingAway() {
        int i = ((ActivityObserver) Dependency.get(ActivityObserver.class)).isTopActivityLauncher() ? 2 : 8;
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            this.mUiOffloadThread.submit(new Runnable() {
                public final void run() {
                    KeyguardViewMediator.this.lambda$keyguardGoingAway$1$KeyguardViewMediator();
                }
            });
        }
        if (((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock()) {
            lambda$keyguardGoingAway$2$KeyguardViewMediator(i);
        } else {
            this.mUiOffloadThread.submit(new Runnable(i) {
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    KeyguardViewMediator.this.lambda$keyguardGoingAway$2$KeyguardViewMediator(this.f$1);
                }
            }, 2);
        }
    }

    public /* synthetic */ void lambda$keyguardGoingAway$1$KeyguardViewMediator() {
        MiuiSettings.System.putBooleanForUser(this.mContext.getContentResolver(), "is_fingerprint_unlock", true, KeyguardUpdateMonitor.getCurrentUser());
    }

    /* access modifiers changed from: private */
    /* renamed from: doKeyguardGoingAway */
    public void lambda$keyguardGoingAway$2$KeyguardViewMediator(int i) {
        try {
            ActivityManagerCompat.keyguardGoingAway(i);
            Slog.i("KeyguardViewMediator", "call fw keyguardGoingAway: flags = " + i);
        } catch (RemoteException e) {
            Log.e("KeyguardViewMediator", "Error while calling WindowManager", e);
        }
    }

    /* access modifiers changed from: private */
    public void handleHide() {
        Trace.beginSection("KeyguardViewMediator#handleHide");
        this.mUpdateMonitor.setKeyguardHide(true);
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleHide");
            if (mustNotUnlockCurrentUser()) {
                Log.d("KeyguardViewMediator", "Split system user, quit unlocking.");
                this.mFingerprintUnlockController.resetMode();
                this.mFaceUnlockController.resetFaceUnlockMode();
                Trace.endSection();
                return;
            }
            this.mHiding = true;
            if (!this.mShowing || this.mOccluded) {
                handleStartKeyguardExitAnimation(SystemClock.uptimeMillis() + this.mHideAnimation.getStartOffset(), this.mHideAnimation.getDuration());
            } else {
                this.mKeyguardGoingAwayRunnable.run();
            }
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                MiuiGxzwManager.getInstance().onKeyguardHide();
            }
            FaceUnlockManager.getInstance().onKeyguardHide();
            unregisterWakeupAndSleepSensor();
            KeyguardSensorManager.getInstance(this.mContext).unregisterProximitySensor();
            Trace.endSection();
        }
    }

    /* access modifiers changed from: private */
    public void handleStartKeyguardExitAnimation(long j, long j2) {
        Trace.beginSection("KeyguardViewMediator#handleStartKeyguardExitAnimation");
        if (this.mHiding) {
            Slog.i("KeyguardViewMediator", "handleStartKeyguardExitAnimation startTime=" + j + " fadeoutDuration=" + j2);
        }
        synchronized (this) {
            if (!this.mHiding) {
                Trace.endSection();
                return;
            }
            this.mHiding = false;
            if (this.mWakeAndUnlocking && this.mDrawnCallback != null) {
                this.mStatusBarKeyguardViewManager.getViewRootImpl().setReportNextDraw();
                notifyDrawn(this.mDrawnCallback);
                this.mDrawnCallback = null;
            }
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(this.mPhoneState)) {
                playSounds(false);
            }
            this.mWakeAndUnlocking = false;
            setShowingLocked(false);
            this.mDismissCallbackRegistry.notifyDismissSucceeded();
            this.mStatusBarKeyguardViewManager.hide(j, j2);
            resetKeyguardDonePendingLocked();
            this.mHideAnimationRun = false;
            adjustStatusBarLocked();
            sendUserPresentBroadcast();
            this.mUpdateMonitor.setKeyguardGoingAway(false);
            this.mUiOffloadThread.submit(new Runnable() {
                public void run() {
                    MiuiKeyguardUtils.setUserAuthenticatedSinceBoot();
                }
            });
            if (this.mOccluded && ((this.mUnlockByFingerPrint || this.mUpdateMonitor.isFaceUnlock()) && !MiuiKeyguardUtils.isTopActivitySystemApp(this.mContext) && !MiuiKeyguardUtils.isTopActivityLockScreenMagazine(this.mContext) && !MiuiKeyguardUtils.isTopActivityRemoteController(this.mContext))) {
                this.mUnlockByFingerPrint = false;
                if (this.mReadyForKeyEvent) {
                    sendKeyEvent();
                    this.mReadyForKeyEvent = false;
                    this.mSendKeyEventScreenOn = false;
                } else {
                    this.mSendKeyEventScreenOn = true;
                }
            }
            unregisterLargeAreaTouchSensor();
            unregisterEllipticSensor();
            FaceUnlockManager.getInstance().printFaceUnlockTime();
            recordKeyguardExitEvent();
            printFingerprintUnlockInfo(false);
            Trace.endSection();
        }
    }

    /* access modifiers changed from: private */
    public void sendKeyEvent() {
        this.mUiOffloadThread.submit(new Runnable() {
            public void run() {
                try {
                    long uptimeMillis = SystemClock.uptimeMillis();
                    KeyEvent keyEvent = new KeyEvent(uptimeMillis, uptimeMillis, 0, 3, 0, 0, -1, 0, 8, 257);
                    long j = uptimeMillis;
                    long j2 = uptimeMillis;
                    KeyEvent keyEvent2 = r2;
                    KeyEvent keyEvent3 = new KeyEvent(j, j2, 1, 3, 0, 0, -1, 0, 8, 257);
                    InputManager.getInstance().injectInputEvent(keyEvent, 0);
                    InputManager.getInstance().injectInputEvent(keyEvent2, 0);
                    Log.d("miui_keyguard", "send keyEvent Home");
                } catch (Exception e) {
                    Log.e("miui_keyguard", "send keyEvent Home fail:" + e.toString());
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void adjustStatusBarLocked() {
        adjustStatusBarLocked(false);
    }

    /* access modifiers changed from: private */
    public void adjustStatusBarLocked(boolean z) {
        if (this.mStatusBarService == null) {
            this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        }
        if (this.mStatusBarService == null) {
            Log.w("KeyguardViewMediator", "Could not get status bar service");
            return;
        }
        int i = 0;
        if (this.mShowing) {
            i = 16777216;
        }
        if (z || isShowingAndNotOccluded()) {
            i = i | 2097152 | 8388608 | 4194304;
        }
        Log.d("KeyguardViewMediator", "adjustStatusBarLocked: mShowing=" + this.mShowing + " mOccluded=" + this.mOccluded + " force=" + z + " --> flags=0x" + Integer.toHexString(i));
        try {
            this.mStatusBarService.disableForUser(i, this.mToken, this.mContext.getPackageName(), KeyguardUpdateMonitor.getCurrentUser());
        } catch (RemoteException unused) {
            Log.e("KeyguardViewMediator", "disableForUser remoteException");
        }
    }

    /* access modifiers changed from: private */
    public void handleReset() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleReset");
            this.mStatusBarKeyguardViewManager.reset(true);
            this.mFingerprintUnlockController.resetMode();
        }
    }

    /* access modifiers changed from: private */
    public void handleVerifyUnlock() {
        Trace.beginSection("KeyguardViewMediator#handleVerifyUnlock");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleVerifyUnlock");
            setShowingLocked(true);
            this.mStatusBarKeyguardViewManager.dismissAndCollapse();
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    public void handleNotifyStartedGoingToSleep() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyStartedGoingToSleep");
            this.mStatusBarKeyguardViewManager.onStartedGoingToSleep();
        }
    }

    /* access modifiers changed from: private */
    public void handleNotifyFinishedGoingToSleep() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyFinishedGoingToSleep");
            this.mStatusBarKeyguardViewManager.onFinishedGoingToSleep();
            unregisterLargeAreaTouchSensor();
            unregisterEllipticSensor();
            KeyguardSensorManager.getInstance(this.mContext).unregisterProximitySensor();
        }
    }

    /* access modifiers changed from: private */
    public void handleNotifyStartedWakingUp() {
        Trace.beginSection("KeyguardViewMediator#handleMotifyStartedWakingUp");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyWakingUp");
            this.mStatusBarKeyguardViewManager.onStartedWakingUp();
            registerLargeAreaTouchSensor();
            registerEllipticSensor();
            trackPageStart();
            recordScreenOn();
            sendKeyguardScreenOnBroadcast();
        }
        Trace.endSection();
    }

    private void sendKeyguardScreenOnBroadcast() {
        Intent intent = new Intent("com.android.systemui.SCREEN_ON");
        intent.putExtra("wakeupWay", AnalyticsHelper.getInstance(this.mContext).getUnlockWay());
        this.mContext.sendBroadcast(intent);
    }

    /* access modifiers changed from: private */
    public void handleNotifyScreenTurningOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        Trace.beginSection("KeyguardViewMediator#handleNotifyScreenTurningOn");
        synchronized (this) {
            Slog.w("KeyguardViewMediator", "handleNotifyScreenTurningOn");
            this.mStatusBarKeyguardViewManager.onScreenTurningOn();
            if (iKeyguardDrawnCallback != null) {
                if (this.mWakeAndUnlocking) {
                    this.mDrawnCallback = iKeyguardDrawnCallback;
                } else if (MiuiFaceUnlockUtils.isScreenTurnOnDelayed()) {
                    notifyDrawnWhenScreenOn(iKeyguardDrawnCallback);
                } else {
                    notifyDrawn(iKeyguardDrawnCallback);
                }
            }
            this.mReadyForKeyEvent = true;
            if (this.mSendKeyEventScreenOn) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        KeyguardViewMediator.this.sendKeyEvent();
                        boolean unused = KeyguardViewMediator.this.mReadyForKeyEvent = false;
                        boolean unused2 = KeyguardViewMediator.this.mSendKeyEventScreenOn = false;
                    }
                });
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
            this.mSmartCoverHelper.onScreenTurnedOn();
            this.mStatusBarKeyguardViewManager.onScreenTurnedOn();
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    public void handleNotifyScreenTurnedOff() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyScreenTurnedOff");
            this.mStatusBarKeyguardViewManager.onScreenTurnedOff();
            this.mDrawnCallback = null;
            this.mWakeAndUnlocking = false;
        }
    }

    private void notifyDrawn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        Trace.beginSection("KeyguardViewMediator#notifyDrawn");
        Slog.w("KeyguardViewMediator", "notifyDrawn");
        if (this.mFingerprintUnlockController.isCancelingPendingLock()) {
            printFingerprintUnlockInfo(true);
        }
        try {
            iKeyguardDrawnCallback.onDrawn();
        } catch (RemoteException e) {
            Slog.w("KeyguardViewMediator", "Exception calling onDrawn():", e);
        }
        Trace.endSection();
    }

    private void notifyDrawnWhenScreenOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        Trace.beginSection("KeyguardViewMediator#notifyDrawn");
        screenTurnedOnCallback(iKeyguardDrawnCallback);
        Trace.endSection();
    }

    private static void screenTurnedOnCallback(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        long screenOnDelyTime = FaceUnlockManager.getInstance().getScreenOnDelyTime();
        Slog.w("KeyguardViewMediator", "screenTurnedOnCallback: sScreenOnDelay = " + screenOnDelyTime);
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.android.internal.policy.IKeyguardDrawnCallback");
            obtain.writeLong(screenOnDelyTime);
            iKeyguardDrawnCallback.asBinder().transact(255, obtain, obtain2, 1);
            obtain2.readException();
        } catch (RemoteException e) {
            Log.e("MiuiKeyguardUtils", "something wrong when delayed turn on screen");
            e.printStackTrace();
        } catch (Throwable th) {
            obtain.recycle();
            obtain2.recycle();
            throw th;
        }
        obtain.recycle();
        obtain2.recycle();
    }

    /* access modifiers changed from: private */
    public void resetKeyguardDonePendingLocked() {
        this.mKeyguardDonePending = false;
        this.mHandler.removeMessages(13);
    }

    public void onBootCompleted() {
        this.mUpdateMonitor.dispatchBootCompleted();
        synchronized (this) {
            this.mBootCompleted = true;
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

    public void preHideKeyguard() {
        ((MiuiKeyguardWallpaperController) Dependency.get(MiuiKeyguardWallpaperController.class)).preWakeUpWithReason("fastUnlock");
        this.mUpdateMonitor.setKeyguardGoingAway(true);
        keyguardGoingAway();
        this.mStatusBarKeyguardViewManager.setKeyguardTransparent();
    }

    public StatusBarKeyguardViewManager registerStatusBar(StatusBar statusBar, ViewGroup viewGroup, ScrimController scrimController, FingerprintUnlockController fingerprintUnlockController, FaceUnlockController faceUnlockController) {
        this.mFingerprintUnlockController = fingerprintUnlockController;
        this.mFaceUnlockController = faceUnlockController;
        this.mStatusBar = statusBar;
        this.mStatusBarKeyguardViewManager.registerStatusBar(statusBar, viewGroup, scrimController, fingerprintUnlockController, faceUnlockController, this.mDismissCallbackRegistry);
        return this.mStatusBarKeyguardViewManager;
    }

    public void startKeyguardExitAnimation(long j, long j2) {
        Trace.beginSection("KeyguardViewMediator#startKeyguardExitAnimation");
        synchronized (this) {
            if (this.mKeyguardGoingAwayTime != 0) {
                this.mWaitFwTotalTime = System.currentTimeMillis() - this.mKeyguardGoingAwayTime;
            }
        }
        this.mHandler.removeMessages(19);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(12, new StartKeyguardExitAnimParams(j, j2)));
        Trace.endSection();
    }

    public ViewMediatorCallback getViewMediatorCallback() {
        return this.mViewMediatorCallback;
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
        printWriter.print("  mHideLockForLid: ");
        printWriter.println(this.mHideLockForLid);
        if (this.mLockPatternUtils != null) {
            printWriter.print("  isLockScreenDisabled: ");
            printWriter.println(this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser()));
        }
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
        if (z != this.mShowing || z2) {
            this.mShowing = z;
            this.mUpdateMonitor.setKeyguardShowingAndOccluded(this.mShowing, this.mOccluded);
            this.mUpdateMonitor.updateShowingState(this.mShowing);
            updateActivityLockScreenState(z);
            for (int size = this.mKeyguardStateCallbacks.size() - 1; size >= 0; size--) {
                IKeyguardStateCallbackCompat iKeyguardStateCallbackCompat = this.mKeyguardStateCallbacks.get(size);
                try {
                    iKeyguardStateCallbackCompat.onShowingStateChanged(z);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onShowingStateChanged", e);
                    if (e instanceof DeadObjectException) {
                        this.mKeyguardStateCallbacks.remove(iKeyguardStateCallbackCompat);
                    }
                }
            }
            updateInputRestrictedLocked();
            this.mUiOffloadThread.submit(new Runnable() {
                public void run() {
                    KeyguardViewMediator.this.mTrustManager.reportKeyguardShowingChanged();
                }
            });
        }
    }

    private void notifyTrustedChangedLocked(boolean z) {
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

    public void addStateMonitorCallback(IKeyguardStateCallbackCompat iKeyguardStateCallbackCompat) {
        synchronized (this) {
            this.mKeyguardStateCallbacks.add(iKeyguardStateCallbackCompat);
            try {
                iKeyguardStateCallbackCompat.onSimSecureStateChanged(this.mUpdateMonitor.isSimPinSecure());
                iKeyguardStateCallbackCompat.onShowingStateChanged(this.mShowing);
                iKeyguardStateCallbackCompat.onInputRestrictedStateChanged(this.mInputRestricted);
                iKeyguardStateCallbackCompat.onTrustedChanged(this.mUpdateMonitor.getUserHasTrust(KeyguardUpdateMonitor.getCurrentUser()));
                iKeyguardStateCallbackCompat.onHasLockscreenWallpaperChanged(this.mUpdateMonitor.hasLockscreenWallpaper());
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call to IKeyguardStateCallback", e);
            }
        }
    }

    public void unblockScreenOn() {
        Iterator<IKeyguardStateCallbackCompat> it = this.mKeyguardStateCallbacks.iterator();
        while (it.hasNext()) {
            IKeyguardStateCallbackCompat next = it.next();
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken("com.android.internal.policy.IKeyguardStateCallback");
                next.asBinder().transact(255, obtain, obtain2, 1);
                obtain2.readException();
            } catch (RemoteException e) {
                Log.e("MiuiKeyguardUtils", "something wrong when unblock screen on");
                e.printStackTrace();
            } catch (Throwable th) {
                obtain.recycle();
                obtain2.recycle();
                throw th;
            }
            obtain.recycle();
            obtain2.recycle();
        }
        trackPageStart();
    }

    /* access modifiers changed from: private */
    public void disableFullScreenGesture() {
        boolean z = !this.mOccluded && !this.mUpdateMonitor.isBouncerShowing();
        if (MiuiKeyguardUtils.isFullScreenGestureOpened(this.mContext)) {
            Utils.updateFsgState(this.mContext, "typefrom_keyguard", z);
        }
    }

    private boolean isSupportPickup() {
        SensorManager sensorManager = this.mSensorManager;
        if (sensorManager == null || !this.mPickupGestureWakeupOpened) {
            return false;
        }
        this.mWakeupAndSleepSensor = sensorManager.getDefaultSensor(33171036, true);
        Sensor sensor = this.mWakeupAndSleepSensor;
        if (sensor != null && ("oem7 Pick Up Gesture".equalsIgnoreCase(sensor.getName()) || "pickup  Wakeup".equalsIgnoreCase(this.mWakeupAndSleepSensor.getName()))) {
            return true;
        }
        if (!MiuiKeyguardUtils.isSupportPickupByMTK(this.mContext)) {
            return false;
        }
        this.mWakeupAndSleepSensor = this.mSensorManager.getDefaultSensor(22, true);
        if (this.mWakeupAndSleepSensor != null) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void registerWakeupAndSleepSensor() {
        if (this.mWakeupAndSleepSensor == null && isSupportPickup()) {
            this.mUiOffloadThread.submit(new Runnable() {
                public void run() {
                    if (KeyguardViewMediator.this.mWakeupAndSleepSensor != null) {
                        Slog.i("KeyguardViewMediator", "register pickup sensor");
                        KeyguardViewMediator.this.mSensorManager.registerListener(KeyguardViewMediator.this.mWakeupAndSleepSensorListener, KeyguardViewMediator.this.mWakeupAndSleepSensor, 3);
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void unregisterWakeupAndSleepSensor() {
        if (this.mSensorManager != null && this.mWakeupAndSleepSensor != null) {
            this.mUiOffloadThread.submit(new Runnable() {
                public void run() {
                    Slog.i("KeyguardViewMediator", "unregister pickup sensor");
                    Sensor unused = KeyguardViewMediator.this.mWakeupAndSleepSensor = null;
                    KeyguardViewMediator.this.mSensorManager.unregisterListener(KeyguardViewMediator.this.mWakeupAndSleepSensorListener);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public boolean shouldRegisterLargeAreaSensor() {
        return this.mIsDeviceSupportLargeAreaTouch && this.mSensorManager != null && this.mLargeAreaTouchSensor == null && !this.mHiding && this.mShowing;
    }

    private void registerLargeAreaTouchSensor() {
        if (shouldRegisterLargeAreaSensor()) {
            this.mUiOffloadThread.submit(new Runnable() {
                public void run() {
                    if (KeyguardViewMediator.this.shouldRegisterLargeAreaSensor()) {
                        KeyguardViewMediator keyguardViewMediator = KeyguardViewMediator.this;
                        Sensor unused = keyguardViewMediator.mLargeAreaTouchSensor = keyguardViewMediator.mSensorManager.getDefaultSensor(33171031);
                        KeyguardViewMediator.this.mSensorManager.registerListener(KeyguardViewMediator.this.mLargeAreaTouchSensorListener, KeyguardViewMediator.this.mLargeAreaTouchSensor, 3);
                    }
                }
            });
        }
    }

    private void unregisterLargeAreaTouchSensor() {
        if (this.mIsDeviceSupportLargeAreaTouch) {
            this.mUiOffloadThread.submit(new Runnable() {
                public void run() {
                    if (KeyguardViewMediator.this.mSensorManager != null && KeyguardViewMediator.this.mLargeAreaTouchSensor != null) {
                        Sensor unused = KeyguardViewMediator.this.mLargeAreaTouchSensor = null;
                        KeyguardViewMediator.this.mSensorManager.unregisterListener(KeyguardViewMediator.this.mLargeAreaTouchSensorListener);
                    }
                }
            });
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0004, code lost:
        r1 = r1.getDefaultSensor(33171031);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isDeviceSupportLargeAreaTouch() {
        /*
            r1 = this;
            android.hardware.SensorManager r1 = r1.mSensorManager
            if (r1 == 0) goto L_0x001b
            r0 = 33171031(0x1fa2657, float:9.189051E-38)
            android.hardware.Sensor r1 = r1.getDefaultSensor(r0)
            if (r1 == 0) goto L_0x001b
            java.lang.String r1 = r1.getName()
            java.lang.String r0 = "Touch Sensor"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x001b
            r1 = 1
            return r1
        L_0x001b:
            r1 = 0
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.keyguard.KeyguardViewMediator.isDeviceSupportLargeAreaTouch():boolean");
    }

    /* access modifiers changed from: private */
    public boolean shouldRegisterEllipticSensor() {
        return this.mIsDeviceSupportEllipticSensor && this.mSensorManager != null && this.mEllipticSensor == null && !this.mHiding && this.mShowing;
    }

    private void registerEllipticSensor() {
        if (shouldRegisterEllipticSensor()) {
            this.mUiOffloadThread.submit(new Runnable() {
                public void run() {
                    if (KeyguardViewMediator.this.shouldRegisterEllipticSensor()) {
                        KeyguardViewMediator keyguardViewMediator = KeyguardViewMediator.this;
                        Sensor unused = keyguardViewMediator.mEllipticSensor = keyguardViewMediator.mSensorManager.getDefaultSensor(65555);
                        KeyguardViewMediator.this.mSensorManager.registerListener(KeyguardViewMediator.this.mEllipticSensorListener, KeyguardViewMediator.this.mEllipticSensor, 0);
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void unregisterEllipticSensor() {
        if (this.mIsDeviceSupportEllipticSensor) {
            this.mUiOffloadThread.submit(new Runnable() {
                public void run() {
                    if (KeyguardViewMediator.this.mSensorManager != null && KeyguardViewMediator.this.mEllipticSensor != null) {
                        Sensor unused = KeyguardViewMediator.this.mEllipticSensor = null;
                        KeyguardViewMediator.this.mSensorManager.unregisterListener(KeyguardViewMediator.this.mEllipticSensorListener);
                    }
                }
            });
        }
    }

    private boolean isDeviceSupportEllipticSensor() {
        SensorManager sensorManager;
        if (!SystemProperties.getBoolean("ro.vendor.audio.us.cd", false) || (sensorManager = this.mSensorManager) == null || sensorManager.getDefaultSensor(65555) == null) {
            return false;
        }
        return true;
    }

    private void trackPageStart() {
        if (!MiuiFaceUnlockUtils.isScreenTurnOnDelayed() && !this.mHiding && !this.mOccluded && this.mShowing) {
            AnalyticsHelper.getInstance(this.mContext).trackPageStart("keyguard_view_main_lock_screen");
        }
    }

    private void recordScreenOn() {
        AnalyticsHelper instance = AnalyticsHelper.getInstance(this.mContext);
        boolean z = true;
        boolean z2 = this.mUpdateMonitor.isFingerprintPermanentlyLockout() || this.mUpdateMonitor.isFingerprintTemporarilyLockout();
        if (this.mViewMediatorCallback.getBouncerPromptReason() != 5) {
            z = false;
        }
        instance.recordScreenOn(z2, z, MiuiFaceUnlockUtils.isScreenTurnOnDelayed(), this.mUpdateMonitor.isFingerprintUnlock(), this.mShowing, this.mOccluded, this.mUpdateMonitor.getChargingState(), WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(this.mContext));
    }

    private void recordUnlockEvent() {
        this.mUiOffloadThread.submit(new Runnable() {
            public void run() {
                if (!(KeyguardViewMediator.this.isSecure() || KeyguardUpdateMonitor.getInstance(KeyguardViewMediator.this.mContext).isSimPinSecure())) {
                    AnalyticsHelper.getInstance(KeyguardViewMediator.this.mContext).recordUnlockWay("none", true);
                }
                LockScreenMagazineUtils.sendLockScreenMagazineUnlockBroadcast(KeyguardViewMediator.this.mContext);
            }
        });
        if (!KeyguardSettingsAnalytics.isCurrentDay()) {
            AnalyticsHelper.getInstance(this.mContext).recordKeyguardSettingsEvent();
            AnalyticsHelper.getInstance(this.mContext).recordLockScreenWallperProviderStatus();
        }
    }

    private void recordKeyguardExitEvent() {
        if (AnalyticsHelper.getInstance(this.mContext).isPWUnlock()) {
            AnalyticsHelper.getInstance(this.mContext).trackPageEnd("pw_unlock_time");
        }
        AnalyticsHelper.getInstance(this.mContext).trackPageEnd("keyguard_view_main_lock_screen", "unlocked");
    }

    public synchronized boolean isGoingToShowKeyguard() {
        boolean z;
        z = true;
        if (!this.mGoingToSleep && (this.mUpdateMonitor.isDeviceInteractive() || !this.mHandler.hasMessages(1))) {
            z = false;
        }
        return z;
    }

    public void recordFingerprintUnlockState() {
        synchronized (this) {
            if (this.mFpAuthTime == 0) {
                this.mFpAuthTime = System.currentTimeMillis();
                this.mKeyguardGoingAwayTime = 0;
                this.mWaitFwTotalTime = 0;
            }
        }
    }

    private void resetFingerprintUnlockState() {
        synchronized (this) {
            this.mFpAuthTime = 0;
            this.mKeyguardGoingAwayTime = 0;
            this.mWaitFwTotalTime = 0;
        }
    }

    private void printFingerprintUnlockInfo(boolean z) {
        String str = z ? "wait fw call onScreenTurningOn = " : "wait fw call startKeyguardExitAnimation = ";
        synchronized (this) {
            if (this.mFpAuthTime != 0) {
                long currentTimeMillis = System.currentTimeMillis();
                StringBuilder sb = new StringBuilder();
                sb.append("fingerprint unlock time: ");
                sb.append(str + this.mWaitFwTotalTime + " ms ");
                sb.append("total = " + (currentTimeMillis - this.mFpAuthTime) + " ms");
                Slog.i("miui_keyguard_fingerprint", sb.toString());
                if (!z) {
                    AnalyticsHelper.getInstance(this.mContext).recordFingerprintUnlockTimeEvent(currentTimeMillis - this.mFpAuthTime);
                }
                resetFingerprintUnlockState();
            }
        }
    }
}
