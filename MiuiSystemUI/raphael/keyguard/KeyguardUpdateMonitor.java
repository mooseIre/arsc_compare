package com.android.keyguard;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.AlarmManager;
import android.app.UserSwitchObserverCompat;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfoCompat;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManagerCompat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerCompat;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManagerCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.WindowManager;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiBleUnlockHelper;
import com.android.keyguard.PhoneSignalController;
import com.android.keyguard.charge.BatteryStatus;
import com.android.keyguard.charge.MiuiChargeManager;
import com.android.keyguard.faceunlock.FaceRemoveCallback;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.keyguard.faceunlock.MiuiFaceUnlockUtils;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.magazine.mode.LockScreenMagazineWallpaperInfo;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.miui.ActivityObserver;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.google.android.collect.Lists;
import com.xiaomi.stat.d.i;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import miui.maml.ScreenContext;
import miui.maml.ScreenElementRoot;
import miui.maml.elements.AdvancedSlider;
import miui.maml.elements.ScreenElement;
import miui.maml.elements.ScreenElementFactory;
import miui.maml.util.ZipResourceLoader;
import miui.os.Build;
import org.w3c.dom.Element;

public class KeyguardUpdateMonitor implements TrustManager.TrustListener {
    private static final ComponentName FALLBACK_HOME_COMPONENT = new ComponentName("com.android.settings", "com.android.settings.FallbackHome");
    /* access modifiers changed from: private */
    public static int sCurrentUser;
    private static KeyguardUpdateMonitor sInstance;
    /* access modifiers changed from: private */
    public static boolean sIsDarkMode;
    /* access modifiers changed from: private */
    public static boolean sIsDarkWallpaperMode;
    /* access modifiers changed from: private */
    public static int sKidSpaceUser;
    public static long sScreenTurnedOnTime;
    /* access modifiers changed from: private */
    public static int sSecondUser;
    /* access modifiers changed from: private */
    public static String sVideo24WallpaperThumnailName;
    private static boolean sWallpaperColorLight = false;
    private ContentObserver mAODObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.this;
            boolean unused = keyguardUpdateMonitor.mAodEnable = MiuiKeyguardUtils.isAodEnable(keyguardUpdateMonitor.mContext);
            KeyguardUpdateMonitor keyguardUpdateMonitor2 = KeyguardUpdateMonitor.this;
            boolean unused2 = keyguardUpdateMonitor2.mAodUsingSuperWallpaperStyle = MiuiKeyguardUtils.isAodUsingSuperWallpaperStyle(keyguardUpdateMonitor2.mContext);
        }
    };
    private ActivityObserver.ActivityObserverCallback mActivityStateObserver = new ActivityObserver.ActivityObserverCallback() {
        public void activityResumed(Intent intent) {
            if (intent != null && intent.getComponent() != null) {
                KeyguardUpdateMonitor.this.updateFingerprintListeningState();
            }
        }
    };
    private AlarmManager mAlarmManager;
    /* access modifiers changed from: private */
    public boolean mAodEnable;
    /* access modifiers changed from: private */
    public boolean mAodUsingSuperWallpaperStyle;
    private FingerprintManager.AuthenticationCallback mAuthenticationCallback = new FingerprintManager.AuthenticationCallback() {
        public void onAuthenticationFailed() {
            Slog.w("miui_keyguard_fingerprint", "onAuthenticationFailed");
            KeyguardUpdateMonitor.this.handleFingerprintAuthFailed();
        }

        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult authenticationResult) {
            int authUserId = MiuiKeyguardUtils.getAuthUserId(KeyguardUpdateMonitor.this.mContext, FingerprintCompat.getFingerIdForFingerprint(authenticationResult.getFingerprint()));
            KeyguardUpdateMonitor.this.handlePreFingerprintAuthenticated(authUserId);
            if (((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock()) {
                DejankUtils.postAfterTraversal(new Runnable(authUserId) {
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        KeyguardUpdateMonitor.AnonymousClass16.this.lambda$onAuthenticationSucceeded$0$KeyguardUpdateMonitor$16(this.f$1);
                    }
                });
            } else {
                lambda$onAuthenticationSucceeded$0$KeyguardUpdateMonitor$16(authUserId);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: doOnAuthenticationSucceeded */
        public void lambda$onAuthenticationSucceeded$0$KeyguardUpdateMonitor$16(int i) {
            Trace.beginSection("KeyguardUpdateMonitor#onAuthenticationSucceeded");
            Slog.i("miui_keyguard_fingerprint", "onAuthenticationSucceeded: authUserId = " + i);
            KeyguardUpdateMonitor.this.handleFingerprintAuthenticated(i);
            Trace.endSection();
        }

        public void onAuthenticationHelp(int i, CharSequence charSequence) {
            KeyguardUpdateMonitor.this.handleFingerprintHelp(i, charSequence.toString());
        }

        public void onAuthenticationError(int i, CharSequence charSequence) {
            Slog.i("miui_keyguard_fingerprint", "onAuthenticationError: errMsgId = " + i + ", errString = " + charSequence);
            KeyguardUpdateMonitor.this.handleFingerprintError(i, TextUtils.isEmpty(charSequence) ? "" : charSequence.toString());
        }

        public void onAuthenticationAcquired(int i) {
            KeyguardUpdateMonitor.this.handleFingerprintAcquired(i);
        }
    };
    private MiuiBleUnlockHelper.BLEUnlockState mBLEUnlockState;
    private BatteryStatus mBatteryStatus;
    private boolean mBootCompleted;
    private boolean mBouncer;
    private final BroadcastReceiver mBroadcastAllReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.app.action.NEXT_ALARM_CLOCK_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(301);
            } else if ("android.intent.action.USER_INFO_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(317, intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId()), 0));
            } else if ("com.android.facelock.FACE_UNLOCK_STARTED".equals(action)) {
                Trace.beginSection("KeyguardUpdateMonitor.mBroadcastAllReceiver#onReceive ACTION_FACE_UNLOCK_STARTED");
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(327, 1, getSendingUserId()));
                Trace.endSection();
            } else if ("com.android.facelock.FACE_UNLOCK_STOPPED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(327, 0, getSendingUserId()));
            } else if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(309);
            } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(334);
            } else if ("face_unlock_release".equals(action)) {
                FaceUnlockManager.getInstance().deleteFeature("0", (FaceRemoveCallback) null);
            } else if ("miui.intent.action.MIUI_REGION_CHANGED".equals(action)) {
                String unused = KeyguardUpdateMonitor.this.mCurrentRegion = Build.getRegion();
                KeyguardUpdateMonitor.this.handleRegionChanged();
            }
        }
    };
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("KeyguardUpdateMonitor", "received broadcast " + action);
            if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                if (!intent.getBooleanExtra("rebroadcastOnUnlock", false)) {
                    SimData fromIntent = SimData.fromIntent(intent);
                    Log.v("KeyguardUpdateMonitor", "action " + action + " state: " + intent.getStringExtra("ss") + " slotId: " + fromIntent.slotId + " subid: " + fromIntent.subId);
                    KeyguardUpdateMonitor.this.mHandler.obtainMessage(304, fromIntent.subId, fromIntent.slotId, fromIntent.simState).sendToTarget();
                }
            } else if ("android.media.RINGER_MODE_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(305, intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1), 0));
            } else if ("android.intent.action.PHONE_STATE".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(306, intent.getStringExtra("state")));
            } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(329);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                KeyguardUpdateMonitor.this.dispatchBootCompleted();
            } else if ("android.intent.action.SERVICE_STATE".equals(action)) {
                ServiceState newFromBundle = ServiceState.newFromBundle(intent.getExtras());
                int intExtra = intent.getIntExtra("subscription", -1);
                Log.v("KeyguardUpdateMonitor", "action " + action + " serviceState=" + newFromBundle + " subId=" + intExtra);
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(330, intExtra, 0, newFromBundle));
            } else if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(500);
            }
        }
    };
    /* access modifiers changed from: private */
    public final ArrayList<WeakReference<KeyguardUpdateMonitorCallback>> mCallbacks = Lists.newArrayList();
    private Runnable mCancelFingerprintRunningTimeout = new Runnable() {
        public void run() {
            Slog.w("KeyguardUpdateMonitor", "something wrong with FingerprintService, cancel timeout");
            KeyguardUpdateMonitor.this.setFingerprintRunningState(0);
            KeyguardUpdateMonitor.this.updateFingerprintListeningState();
        }
    };
    private boolean mChargeAnimationWindowShowing;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public String mCurrentRegion = Build.getRegion();
    private ContentObserver mDarkModeObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            boolean unused = KeyguardUpdateMonitor.sIsDarkMode = MiuiKeyguardUtils.isNightMode(KeyguardUpdateMonitor.this.mContext);
            Iterator it = KeyguardUpdateMonitor.this.mWallpaperChangeCallbacks.iterator();
            while (it.hasNext()) {
                ((WallpaperChangeCallback) it.next()).onWallpaperChange(true);
            }
        }
    };
    private ContentObserver mDarkWallpaperModeObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            boolean unused = KeyguardUpdateMonitor.sIsDarkWallpaperMode = MiuiSettings.System.getBoolean(KeyguardUpdateMonitor.this.mContext.getContentResolver(), "darken_wallpaper_under_dark_mode", true);
            Iterator it = KeyguardUpdateMonitor.this.mWallpaperChangeCallbacks.iterator();
            while (it.hasNext()) {
                ((WallpaperChangeCallback) it.next()).onWallpaperChange(true);
            }
        }
    };
    private boolean mDeviceInteractive;
    /* access modifiers changed from: private */
    public boolean mDeviceProvisioned;
    private ContentObserver mDeviceProvisionedObserver;
    private DisplayClientState mDisplayClientState = new DisplayClientState();
    private Map<Integer, Boolean> mDpmFingerprintDisable = new HashMap();
    private int mFaceUnlockMode = 0;
    private SparseIntArray mFailedAttempts = new SparseIntArray();
    private CancellationSignal mFingerprintCancelSignal;
    private int mFingerprintMode = 0;
    private Map<Integer, Boolean> mFingerprintPossibleMap = new HashMap();
    private int mFingerprintRunningState = 0;
    private FingerprintManager mFpm;
    private boolean mGoingToSleep;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 301) {
                KeyguardUpdateMonitor.this.handleTimeUpdate();
            } else if (i != 302) {
                if (i != 500) {
                    switch (i) {
                        case 304:
                            KeyguardUpdateMonitor.this.handleSimStateChange(message.arg1, message.arg2, (IccCardConstants.State) message.obj);
                            return;
                        case 305:
                            KeyguardUpdateMonitor.this.handleRingerModeChange(message.arg1);
                            return;
                        case 306:
                            KeyguardUpdateMonitor.this.handlePhoneStateChanged((String) message.obj);
                            return;
                        default:
                            switch (i) {
                                case 308:
                                    KeyguardUpdateMonitor.this.handleDeviceProvisioned();
                                    return;
                                case 309:
                                    KeyguardUpdateMonitor.this.handleDevicePolicyManagerStateChanged();
                                    return;
                                case 310:
                                    KeyguardUpdateMonitor.this.handleUserSwitching(message.arg1, (IRemoteCallback) message.obj);
                                    return;
                                default:
                                    switch (i) {
                                        case 312:
                                            KeyguardUpdateMonitor.this.handleKeyguardReset();
                                            return;
                                        case 313:
                                            KeyguardUpdateMonitor.this.handleBootCompleted();
                                            return;
                                        case 314:
                                            KeyguardUpdateMonitor.this.handleUserSwitchComplete(message.arg1);
                                            return;
                                        default:
                                            switch (i) {
                                                case 317:
                                                    KeyguardUpdateMonitor.this.handleUserInfoChanged(message.arg1);
                                                    return;
                                                case 318:
                                                    KeyguardUpdateMonitor.this.handleReportEmergencyCallAction();
                                                    return;
                                                case 319:
                                                    Trace.beginSection("KeyguardUpdateMonitor#handler MSG_STARTED_WAKING_UP");
                                                    KeyguardUpdateMonitor.this.handleStartedWakingUp();
                                                    Trace.endSection();
                                                    return;
                                                case 320:
                                                    KeyguardUpdateMonitor.this.handleFinishedGoingToSleep(message.arg1);
                                                    return;
                                                case 321:
                                                    KeyguardUpdateMonitor.this.handleStartedGoingToSleep(message.arg1);
                                                    return;
                                                case 322:
                                                    KeyguardUpdateMonitor.this.handleKeyguardBouncerChanged(message.arg1);
                                                    return;
                                                default:
                                                    switch (i) {
                                                        case 327:
                                                            Trace.beginSection("KeyguardUpdateMonitor#handler MSG_FACE_UNLOCK_STATE_CHANGED");
                                                            KeyguardUpdateMonitor.this.handleFaceUnlockStateChanged(message.arg1 != 0, message.arg2);
                                                            Trace.endSection();
                                                            return;
                                                        case 328:
                                                            KeyguardUpdateMonitor.this.handleSimSubscriptionInfoChanged();
                                                            return;
                                                        case 329:
                                                            KeyguardUpdateMonitor.this.handleAirplaneModeChanged();
                                                            return;
                                                        case 330:
                                                            KeyguardUpdateMonitor.this.handleServiceStateChange(message.arg1, (ServiceState) message.obj);
                                                            return;
                                                        case 331:
                                                            KeyguardUpdateMonitor.this.handleScreenTurnedOn();
                                                            return;
                                                        case 332:
                                                            Trace.beginSection("KeyguardUpdateMonitor#handler MSG_SCREEN_TURNED_ON");
                                                            KeyguardUpdateMonitor.this.handleScreenTurnedOff();
                                                            Trace.endSection();
                                                            return;
                                                        case 333:
                                                            KeyguardUpdateMonitor.this.handleDreamingStateChanged(message.arg1);
                                                            return;
                                                        case 334:
                                                            KeyguardUpdateMonitor.this.handleUserUnlocked();
                                                            break;
                                                        case 335:
                                                            KeyguardUpdateMonitor.this.handleShowingStateChange(message.arg1);
                                                            return;
                                                        case 336:
                                                            Trace.beginSection("KeyguardUpdateMonitor#handler MSG_STARTED_WAKING_UP");
                                                            KeyguardUpdateMonitor.this.handleStartedWakingUpWithReason((String) message.obj);
                                                            Trace.endSection();
                                                            return;
                                                        default:
                                                            super.handleMessage(message);
                                                            return;
                                                    }
                                            }
                                    }
                            }
                    }
                }
                KeyguardUpdateMonitor.this.handleLocaleChanged();
            } else {
                KeyguardUpdateMonitor.this.handleBatteryUpdate((BatteryStatus) message.obj);
            }
        }
    };
    /* access modifiers changed from: private */
    public int mHardwareUnavailableRetryCount = 0;
    private boolean mHasLockscreenWallpaper;
    private boolean mIsFingerprintPermanentlyLockout;
    private boolean mIsFingerprintTemporarilyLockout;
    private boolean mIsLockScreenMagazinePkgExist = true;
    private boolean mIsPsensorDisabled = false;
    /* access modifiers changed from: private */
    public boolean mIsSuperSaveModePower;
    private boolean mIsSupportLockScreenMagazineLeft;
    private boolean mIsSupportLockScreenMagazineLeftOverlay;
    private boolean mKeyguardGoingAway;
    private boolean mKeyguardHide = false;
    private boolean mKeyguardIsVisible;
    private KeyguardViewMediator mKeyguardMediator;
    private boolean mKeyguardOccluded;
    private boolean mKeyguardShowing;
    private boolean mKeyguardShowingAndOccluded;
    private ContentObserver mKidSpaceUserProviderObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            synchronized (KeyguardUpdateMonitor.class) {
                int unused = KeyguardUpdateMonitor.sKidSpaceUser = Settings.Secure.getIntForUser(KeyguardUpdateMonitor.this.mContext.getContentResolver(), "kid_user_id", -10000, 0);
            }
        }
    };
    /* access modifiers changed from: private */
    public LockPatternUtils mLockPatternUtils;
    private boolean mLockScreenLeftOverlayAvailable;
    private LockScreenMagazineWallpaperInfo mLockScreenMagazineWallpaperInfo = new LockScreenMagazineWallpaperInfo();
    private final FingerprintManager.LockoutResetCallback mLockoutResetCallback = new FingerprintManager.LockoutResetCallback() {
        public void onLockoutReset() {
            KeyguardUpdateMonitor.this.handleFingerprintLockoutReset();
            KeyguardUpdateMonitor.this.resetAllFingerprintLockout();
        }
    };
    private boolean mNeedsSlowUnlockTransition;
    PhoneSignalController.PhoneSignalChangeCallback mPhoneSignalChangeCallback = new PhoneSignalController.PhoneSignalChangeCallback() {
        public void onSignalChange(boolean z) {
            for (int i = 0; i < KeyguardUpdateMonitor.this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) ((WeakReference) KeyguardUpdateMonitor.this.mCallbacks.get(i)).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onPhoneSignalChanged(z);
                }
            }
        }
    };
    private PhoneSignalController mPhoneSignalController;
    private int mPhoneState;
    private Runnable mRetryFingerprintAuthentication = new Runnable() {
        public void run() {
            Log.w("KeyguardUpdateMonitor", "Retrying fingerprint after HW unavailable, attempt " + KeyguardUpdateMonitor.this.mHardwareUnavailableRetryCount);
            KeyguardUpdateMonitor.this.updateFingerprintListeningState();
        }
    };
    private int mRingMode;
    private boolean mScreenOn;
    private ContentObserver mSecondUserProviderObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            synchronized (KeyguardUpdateMonitor.class) {
                int unused = KeyguardUpdateMonitor.sSecondUser = Settings.Secure.getIntForUser(KeyguardUpdateMonitor.this.mContext.getContentResolver(), "second_user_id", -10000, 0);
            }
        }
    };
    HashMap<Integer, ServiceState> mServiceStates = new HashMap<>();
    HashMap<Integer, SimData> mSimDatas = new HashMap<>();
    HashMap<Integer, Boolean> mSimStateEarlyReadyStatus = new HashMap<>();
    private int mStatusBarHeight;
    private final BroadcastReceiver mStrongAuthTimeoutReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.android.systemui.ACTION_STRONG_AUTH_TIMEOUT".equals(intent.getAction())) {
                int intExtra = intent.getIntExtra("com.android.systemui.USER_ID", -1);
                KeyguardUpdateMonitor.this.mLockPatternUtils.requireStrongAuth(16, intExtra);
                KeyguardUpdateMonitor.this.notifyStrongAuthStateChanged(intExtra);
            }
        }
    };
    private final StrongAuthTracker mStrongAuthTracker;
    private List<SubscriptionInfo> mSubscriptionInfo;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        public void onSubscriptionsChanged() {
            KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(328);
        }
    };
    private SubscriptionManager mSubscriptionManager;
    private ContentObserver mSuperSavePowerObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.this;
            boolean unused = keyguardUpdateMonitor.mIsSuperSaveModePower = MiuiSettings.System.isSuperSaveModeOpen(keyguardUpdateMonitor.mContext, KeyguardUpdateMonitor.sCurrentUser);
            KeyguardUpdateMonitor.this.handleSuperSavePowerModeChanged();
        }
    };
    private boolean mSwitchingUser;
    private final BroadcastReceiver mTimeTickReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(301);
        }
    };
    private TrustManager mTrustManager;
    private SparseBooleanArray mUserBleAuthenticated = new SparseBooleanArray();
    private SparseBooleanArray mUserFaceAuthenticated = new SparseBooleanArray();
    private SparseBooleanArray mUserFaceUnlockRunning = new SparseBooleanArray();
    private SparseBooleanArray mUserFingerprintAuthenticated = new SparseBooleanArray();
    private SparseBooleanArray mUserHasTrust = new SparseBooleanArray();
    private UserManager mUserManager;
    private SparseBooleanArray mUserTrustIsManaged = new SparseBooleanArray();
    private int mWallpaperBlurColor = -1;
    /* access modifiers changed from: private */
    public ArrayList<WallpaperChangeCallback> mWallpaperChangeCallbacks = Lists.newArrayList();

    public interface WallpaperChangeCallback {
        void onWallpaperChange(boolean z);
    }

    public void onTrustChanged(boolean z, int i, int i2) {
    }

    public void onTrustError(CharSequence charSequence) {
    }

    public void onTrustManagedChanged(boolean z, int i) {
    }

    public void setLockScreenMagazineWallpaperInfo(LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo) {
        this.mLockScreenMagazineWallpaperInfo = lockScreenMagazineWallpaperInfo;
    }

    public LockScreenMagazineWallpaperInfo getLockScreenMagazineWallpaperInfo() {
        return this.mLockScreenMagazineWallpaperInfo;
    }

    public void setLockScreenMagazinePkgExist(boolean z) {
        this.mIsLockScreenMagazinePkgExist = z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0016, code lost:
        updateFingerprintListeningState();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
        r1.mHandler.postAtFrontOfQueue(new com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass1(r1));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0014, code lost:
        if (android.os.Looper.myLooper() != r1.mHandler.getLooper()) goto L_0x001a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setKeyguardHide(boolean r2) {
        /*
            r1 = this;
            monitor-enter(r1)
            boolean r0 = r1.mKeyguardHide     // Catch:{ all -> 0x0025 }
            if (r0 != r2) goto L_0x0007
            monitor-exit(r1)     // Catch:{ all -> 0x0025 }
            return
        L_0x0007:
            r1.mKeyguardHide = r2     // Catch:{ all -> 0x0025 }
            monitor-exit(r1)     // Catch:{ all -> 0x0025 }
            android.os.Looper r2 = android.os.Looper.myLooper()
            android.os.Handler r0 = r1.mHandler
            android.os.Looper r0 = r0.getLooper()
            if (r2 != r0) goto L_0x001a
            r1.updateFingerprintListeningState()
            goto L_0x0024
        L_0x001a:
            android.os.Handler r2 = r1.mHandler
            com.android.keyguard.KeyguardUpdateMonitor$1 r0 = new com.android.keyguard.KeyguardUpdateMonitor$1
            r0.<init>()
            r2.postAtFrontOfQueue(r0)
        L_0x0024:
            return
        L_0x0025:
            r2 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0025 }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardUpdateMonitor.setKeyguardHide(boolean):void");
    }

    public boolean isKeyguardHide() {
        boolean z;
        synchronized (this) {
            z = this.mKeyguardHide;
        }
        return z;
    }

    public boolean isLockScreenMagazinePkgExist() {
        return this.mIsLockScreenMagazinePkgExist;
    }

    public static synchronized int getMaintenanceModeId() {
        int i;
        synchronized (KeyguardUpdateMonitor.class) {
            try {
                Class<?> cls = Class.forName("android.os.UserHandle");
                i = cls.getField("MAINTENANCE_MODE_ID").getInt(cls);
            } catch (Exception e) {
                e.printStackTrace();
                return -10000;
            }
        }
        return i;
    }

    public static synchronized void setCurrentUser(int i) {
        synchronized (KeyguardUpdateMonitor.class) {
            sCurrentUser = i;
        }
    }

    public static synchronized int getCurrentUser() {
        int i;
        synchronized (KeyguardUpdateMonitor.class) {
            i = sCurrentUser;
        }
        return i;
    }

    public static synchronized int getSecondUser() {
        int i;
        synchronized (KeyguardUpdateMonitor.class) {
            i = sSecondUser;
        }
        return i;
    }

    public static synchronized int getKidSpaceUser() {
        int i;
        synchronized (KeyguardUpdateMonitor.class) {
            i = sKidSpaceUser;
        }
        return i;
    }

    public void setKeyguardShowingAndOccluded(boolean z, boolean z2) {
        this.mKeyguardShowingAndOccluded = z && z2;
        if (z2 != this.mKeyguardOccluded) {
            this.mKeyguardOccluded = z2;
            handleKeyguardOccludedChanged(this.mKeyguardOccluded);
        }
        if (this.mKeyguardShowing != z) {
            this.mKeyguardShowing = z;
            handleKeyguardShowingChanged(this.mKeyguardShowing);
            this.mFingerprintPossibleMap.clear();
        }
        updateFingerprintListeningState();
        ActivityObserver activityObserver = (ActivityObserver) Dependency.get(ActivityObserver.class);
        if (this.mKeyguardShowingAndOccluded) {
            activityObserver.removeCallback(this.mActivityStateObserver);
            activityObserver.addCallback(this.mActivityStateObserver);
            return;
        }
        activityObserver.removeCallback(this.mActivityStateObserver);
    }

    public boolean isKeyguardOccluded() {
        return this.mKeyguardOccluded;
    }

    public boolean isKeyguardShowing() {
        return this.mKeyguardShowing;
    }

    public boolean isBouncerShowing() {
        return this.mBouncer;
    }

    public static synchronized boolean isOwnerUser() {
        boolean z;
        synchronized (KeyguardUpdateMonitor.class) {
            z = sCurrentUser == 0;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void handleSimSubscriptionInfoChanged() {
        Log.v("KeyguardUpdateMonitor", "onSubscriptionInfoChanged()");
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(false);
        List<SubscriptionInfo> subscriptionInfo2 = getSubscriptionInfo(true);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < subscriptionInfo2.size(); i++) {
            SubscriptionInfo subscriptionInfo3 = subscriptionInfo2.get(i);
            if (refreshSimState(subscriptionInfo3.getSubscriptionId(), subscriptionInfo3.getSimSlotIndex())) {
                arrayList.add(subscriptionInfo3);
            }
        }
        if (subscriptionInfo.isEmpty() && !subscriptionInfo2.isEmpty() && arrayList.isEmpty()) {
            this.mKeyguardMediator.handleSimSecureStateChanged();
        }
        for (int i2 = 0; i2 < arrayList.size(); i2++) {
            SimData simData = this.mSimDatas.get(Integer.valueOf(((SubscriptionInfo) arrayList.get(i2)).getSimSlotIndex()));
            for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i3).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onSimStateChanged(simData.subId, simData.slotId, simData.simState);
                }
            }
        }
        for (int i4 = 0; i4 < this.mCallbacks.size(); i4++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback2 = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i4).get();
            if (keyguardUpdateMonitorCallback2 != null) {
                keyguardUpdateMonitorCallback2.onRefreshCarrierInfo();
            }
        }
        if (Dependency.getHost() != null) {
            Dependency.getHost().onSimPinSecureChanged(isSimPinSecure());
        }
    }

    /* access modifiers changed from: private */
    public void handleAirplaneModeChanged() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onAirplaneModeChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSuperSavePowerModeChanged() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onSuperSavePowerChanged(this.mIsSuperSaveModePower);
            }
        }
    }

    public List<SubscriptionInfo> getSubscriptionInfo(boolean z) {
        List<SubscriptionInfo> list = this.mSubscriptionInfo;
        if (list == null || z) {
            list = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        }
        if (list == null) {
            this.mSubscriptionInfo = new ArrayList();
        } else {
            this.mSubscriptionInfo = list;
        }
        return this.mSubscriptionInfo;
    }

    public void setKeyguardGoingAway(boolean z) {
        this.mKeyguardGoingAway = z;
        if (z) {
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onKeyguardGoingAway();
                }
            }
        }
    }

    public boolean getKeyguardGoingAway() {
        return this.mKeyguardGoingAway;
    }

    private void onFingerprintAuthenticated(int i) {
        Trace.beginSection("KeyGuardUpdateMonitor#onFingerPrintAuthenticated");
        this.mUserFingerprintAuthenticated.put(i, true);
        this.mFingerprintCancelSignal = null;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFingerprintAuthenticated(i);
            }
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    public void handleFingerprintAuthFailed() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFingerprintAuthFailed();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleFingerprintAcquired(int i) {
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFingerprintAcquired(i);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handlePreFingerprintAuthenticated(int i) {
        Trace.beginSection("KeyGuardUpdateMonitor#handlePreFingerprintAuthenticated");
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onPreFingerprintAuthenticated(i);
            }
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    public void handleFingerprintAuthenticated(int i) {
        Trace.beginSection("KeyGuardUpdateMonitor#handlerFingerPrintAuthenticated");
        try {
            if (isFingerprintDisabled(i)) {
                Log.d("KeyguardUpdateMonitor", "Fingerprint disabled by DPM for userId: " + i);
                return;
            }
            onFingerprintAuthenticated(i);
            setFingerprintRunningState(0);
            updateFingerprintListeningState();
            Trace.endSection();
        } finally {
            setFingerprintRunningState(0);
            updateFingerprintListeningState();
        }
    }

    /* access modifiers changed from: private */
    public void handleFingerprintHelp(int i, String str) {
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFingerprintHelp(i, str);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleFingerprintError(int i, String str) {
        int i2;
        int i3;
        if (i == 5 && ((i3 = this.mFingerprintRunningState) == 2 || i3 == 3)) {
            this.mHandler.removeCallbacks(this.mCancelFingerprintRunningTimeout);
        }
        if (i == 5 && this.mFingerprintRunningState == 3) {
            setFingerprintRunningState(0);
            updateFingerprintListeningState();
        } else {
            setFingerprintRunningState(0);
        }
        if (i == 1 && (i2 = this.mHardwareUnavailableRetryCount) < 3) {
            this.mHardwareUnavailableRetryCount = i2 + 1;
            this.mHandler.removeCallbacks(this.mRetryFingerprintAuthentication);
            this.mHandler.postDelayed(this.mRetryFingerprintAuthentication, 3000);
        }
        if (i == 9) {
            this.mLockPatternUtils.requireStrongAuth(8, getCurrentUser());
            this.mIsFingerprintPermanentlyLockout = true;
        }
        if (i == 7) {
            this.mIsFingerprintTemporarilyLockout = true;
        }
        for (int i4 = 0; i4 < this.mCallbacks.size(); i4++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i4).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFingerprintError(i, str);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleFingerprintLockoutReset() {
        updateFingerprintListeningState();
        if (this.mKeyguardIsVisible) {
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onFingerprintLockoutReset();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setFingerprintRunningState(int i) {
        boolean z = false;
        boolean z2 = this.mFingerprintRunningState == 1;
        if (i == 1) {
            z = true;
        }
        this.mFingerprintRunningState = i;
        if (z2 != z) {
            notifyFingerprintRunningStateChanged();
        }
        this.mHandler.removeCallbacks(this.mCancelFingerprintRunningTimeout);
        int i2 = this.mFingerprintRunningState;
        if (i2 == 2 || i2 == 3) {
            this.mHandler.postDelayed(this.mCancelFingerprintRunningTimeout, 10000);
        }
    }

    private void notifyFingerprintRunningStateChanged() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFingerprintRunningStateChanged(isFingerprintDetectionRunning());
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleFaceUnlockStateChanged(boolean z, int i) {
        this.mUserFaceUnlockRunning.put(i, z);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFaceUnlockStateChanged(z, i);
            }
        }
    }

    public boolean isFaceUnlockRunning(int i) {
        return this.mUserFaceUnlockRunning.get(i);
    }

    public boolean isFingerprintDetectionRunning() {
        return this.mFingerprintRunningState == 1;
    }

    private boolean isTrustDisabled(int i) {
        return isSimPinSecure();
    }

    private boolean isFingerprintDisabled(int i) {
        if (!this.mDpmFingerprintDisable.containsKey(Integer.valueOf(i)) || this.mDpmFingerprintDisable.get(Integer.valueOf(i)) == null) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
            this.mDpmFingerprintDisable.put(Integer.valueOf(i), Boolean.valueOf((devicePolicyManager == null || (devicePolicyManager.getKeyguardDisabledFeatures((ComponentName) null, i) & 32) == 0) ? false : true));
        }
        if (this.mDpmFingerprintDisable.get(Integer.valueOf(i)).booleanValue() || isSimPinSecure()) {
            return true;
        }
        return false;
    }

    public boolean getUserCanSkipBouncer(int i) {
        return getUserHasTrust(i) || (isUnlockingWithFingerprintAllowed(i) && (this.mUserFingerprintAuthenticated.get(i) || ((this.mUserBleAuthenticated.get(i) || this.mUserFaceAuthenticated.get(i)) && !isSimPinSecure())));
    }

    public boolean getUserHasTrust(int i) {
        return !isTrustDisabled(i) && this.mUserHasTrust.get(i);
    }

    public boolean getUserTrustIsManaged(int i) {
        return this.mUserTrustIsManaged.get(i) && !isTrustDisabled(i);
    }

    public boolean getUserFingerprintAuthenticated(int i) {
        return this.mUserFingerprintAuthenticated.get(i);
    }

    public boolean getUserFaceAuthenticated(int i) {
        return this.mUserFaceAuthenticated.get(i);
    }

    public void putUserFaceAuthenticated(int i) {
        this.mUserFaceAuthenticated.put(i, true);
    }

    public boolean getUserBleAuthenticated(int i) {
        return this.mUserBleAuthenticated.get(i);
    }

    public boolean isUnlockingWithFingerprintAllowed() {
        return this.mStrongAuthTracker.isUnlockingWithFingerprintAllowed();
    }

    public boolean isUnlockingWithFingerprintAllowed(int i) {
        return this.mStrongAuthTracker.isUnlockingWithFingerprintAllowed(i);
    }

    public boolean needsSlowUnlockTransition() {
        return this.mNeedsSlowUnlockTransition;
    }

    public StrongAuthTracker getStrongAuthTracker() {
        return this.mStrongAuthTracker;
    }

    public void reportSuccessfulStrongAuthUnlockAttempt() {
        if (this.mFpm != null) {
            FingerprintManagerCompat.resetTimeout(this.mContext, (byte[]) null);
        }
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().reportSuccessfulStrongAuthUnlockAttempt();
        }
    }

    /* access modifiers changed from: private */
    public void notifyStrongAuthStateChanged(int i) {
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStrongAuthStateChanged(i);
            }
        }
    }

    public boolean isScreenOn() {
        return this.mScreenOn;
    }

    static class DisplayClientState {
        DisplayClientState() {
        }
    }

    public boolean shouldListenForFingerprintWhenUnlocked() {
        return FaceUnlockManager.getInstance().isFaceUnlockSuccessAndStayScreen() || isBleUnlockSuccess();
    }

    private static class SimData {
        public IccCardConstants.State simState;
        public int slotId;
        public int subId;

        SimData(IccCardConstants.State state, int i, int i2) {
            this.simState = state;
            this.slotId = i;
            this.subId = i2;
        }

        static SimData fromIntent(Intent intent) {
            IccCardConstants.State state;
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                String stringExtra = intent.getStringExtra("ss");
                int intExtra = intent.getIntExtra("phone", 0);
                int intExtra2 = intent.getIntExtra("subscription", -1);
                if ("ABSENT".equals(stringExtra)) {
                    if ("PERM_DISABLED".equals(intent.getStringExtra("reason"))) {
                        state = IccCardConstants.State.PERM_DISABLED;
                    } else {
                        state = IccCardConstants.State.ABSENT;
                    }
                } else if ("READY".equals(stringExtra)) {
                    state = IccCardConstants.State.READY;
                } else if ("LOCKED".equals(stringExtra)) {
                    String stringExtra2 = intent.getStringExtra("reason");
                    if ("PIN".equals(stringExtra2)) {
                        state = IccCardConstants.State.PIN_REQUIRED;
                    } else if ("PUK".equals(stringExtra2)) {
                        state = IccCardConstants.State.PUK_REQUIRED;
                    } else {
                        state = IccCardConstants.State.UNKNOWN;
                    }
                } else if ("NETWORK".equals(stringExtra)) {
                    state = IccCardConstants.State.NETWORK_LOCKED;
                } else if ("CARD_IO_ERROR".equals(stringExtra)) {
                    state = IccCardConstants.State.CARD_IO_ERROR;
                } else if ("LOADED".equals(stringExtra) || "IMSI".equals(stringExtra)) {
                    state = IccCardConstants.State.READY;
                } else if ("NOT_READY".equals(stringExtra)) {
                    state = IccCardConstants.State.NOT_READY;
                } else {
                    state = IccCardConstants.State.UNKNOWN;
                }
                return new SimData(state, intExtra, intExtra2);
            }
            throw new IllegalArgumentException("only handles intent ACTION_SIM_STATE_CHANGED");
        }

        public String toString() {
            return "SimData{state=" + this.simState + ",slotId=" + this.slotId + ",subId=" + this.subId + "}";
        }
    }

    public class StrongAuthTracker extends AbstractStrongAuthTracker {
        public StrongAuthTracker(Context context) {
            super(context);
        }

        public boolean isUnlockingWithFingerprintAllowed() {
            return isFingerprintAllowedForUser(KeyguardUpdateMonitor.getCurrentUser());
        }

        public boolean isUnlockingWithFingerprintAllowed(int i) {
            return isFingerprintAllowedForUser(i);
        }

        public boolean hasUserAuthenticatedSinceBoot(int i) {
            return (getStrongAuthForUser(i) & 1) == 0;
        }

        public boolean hasUserAuthenticatedSinceBoot() {
            return (getStrongAuthForUser(KeyguardUpdateMonitor.getCurrentUser()) & 1) == 0;
        }

        public boolean hasOwnerUserAuthenticatedSinceBoot() {
            return (getStrongAuthForUser(0) & 1) == 0;
        }

        public void onStrongAuthRequiredChanged(int i) {
            KeyguardUpdateMonitor.this.notifyStrongAuthStateChanged(i);
        }

        public int getStrongAuthForUser(int i) {
            return KeyguardUpdateMonitor.super.getStrongAuthForUser(i) & -3;
        }
    }

    public static KeyguardUpdateMonitor getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new KeyguardUpdateMonitor(context);
        }
        return sInstance;
    }

    /* access modifiers changed from: protected */
    public void handleStartedWakingUp() {
        Trace.beginSection("KeyguardUpdateMonitor#handleStartedWakingUp");
        if (FaceUnlockManager.getInstance().isWakeupByNotification()) {
            ((StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class)).setUserActivityTime(6000);
        }
        FaceUnlockManager.getInstance().startFaceUnlock();
        FaceUnlockManager.getInstance().setWakeupByNotification(false);
        updateFingerprintListeningState();
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStartedWakingUp();
            }
        }
        Trace.endSection();
    }

    /* access modifiers changed from: protected */
    public void handleStartedWakingUpWithReason(String str) {
        Trace.beginSection("KeyguardUpdateMonitor#handleStartedWakingUpWithReason");
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStartedWakingUpWithReason(str);
            }
        }
        Trace.endSection();
    }

    /* access modifiers changed from: protected */
    public void handleStartedGoingToSleep(int i) {
        clearFingerprintRecognized();
        ((StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class)).setUserActivityTime(i.a);
        int size = this.mCallbacks.size();
        for (int i2 = 0; i2 < size; i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStartedGoingToSleep(i);
            }
        }
        this.mGoingToSleep = true;
        updateFingerprintListeningState();
    }

    /* access modifiers changed from: protected */
    public void handleFinishedGoingToSleep(int i) {
        this.mGoingToSleep = false;
        int size = this.mCallbacks.size();
        for (int i2 = 0; i2 < size; i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFinishedGoingToSleep(i);
            }
        }
        updateFingerprintListeningState();
    }

    /* access modifiers changed from: private */
    public void handleScreenTurnedOn() {
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onScreenTurnedOn();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleScreenTurnedOff() {
        this.mHardwareUnavailableRetryCount = 0;
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onScreenTurnedOff();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleDreamingStateChanged(int i) {
        int size = this.mCallbacks.size();
        boolean z = true;
        if (i != 1) {
            z = false;
        }
        for (int i2 = 0; i2 < size; i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDreamingStateChanged(z);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleUserInfoChanged(int i) {
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserInfoChanged(i);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleUserUnlocked() {
        this.mNeedsSlowUnlockTransition = resolveNeedsSlowUnlockTransition();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserUnlocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleRegionChanged() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRegionChanged();
            }
        }
    }

    public String getCurrentRegion() {
        return this.mCurrentRegion;
    }

    private KeyguardUpdateMonitor(Context context) {
        this.mContext = context;
        this.mSubscriptionManager = SubscriptionManager.from(context);
        this.mAlarmManager = (AlarmManager) context.getSystemService(AlarmManager.class);
        this.mDeviceProvisioned = MiuiKeyguardUtils.isDeviceProvisionedInSettingsDb(this.mContext);
        this.mStrongAuthTracker = new StrongAuthTracker(context);
        if (!this.mDeviceProvisioned) {
            watchForDeviceProvisioning();
        }
        this.mBatteryStatus = new BatteryStatus(1, 0, 0, 0, 0, -1);
        Dependency.get(MiuiChargeManager.class);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        context.registerReceiverAsUser(this.mTimeTickReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) Dependency.get(Dependency.TIME_TICK_HANDLER));
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter2.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter2.addAction("android.intent.action.LOCALE_CHANGED");
        intentFilter2.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter2.addAction("android.intent.action.SERVICE_STATE");
        intentFilter2.addAction("android.intent.action.PHONE_STATE");
        intentFilter2.addAction("android.media.RINGER_MODE_CHANGED");
        context.registerReceiver(this.mBroadcastReceiver, intentFilter2, (String) null, this.mHandler);
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.setPriority(1000);
        intentFilter3.addAction("android.intent.action.BOOT_COMPLETED");
        context.registerReceiver(this.mBroadcastReceiver, intentFilter3, (String) null, this.mHandler);
        IntentFilter intentFilter4 = new IntentFilter();
        intentFilter4.addAction("android.intent.action.USER_INFO_CHANGED");
        intentFilter4.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        intentFilter4.addAction("com.android.facelock.FACE_UNLOCK_STARTED");
        intentFilter4.addAction("com.android.facelock.FACE_UNLOCK_STOPPED");
        intentFilter4.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        intentFilter4.addAction("android.intent.action.USER_UNLOCKED");
        intentFilter4.addAction("face_unlock_release");
        intentFilter4.addAction("miui.intent.action.MIUI_REGION_CHANGED");
        context.registerReceiverAsUser(this.mBroadcastAllReceiver, UserHandle.ALL, intentFilter4, (String) null, this.mHandler);
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
        try {
            ActivityManagerCompat.registerUserSwitchObserver(new UserSwitchObserverCompat() {
                public void onUserSwitching(int i, IRemoteCallback iRemoteCallback) {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(310, i, 0, iRemoteCallback));
                }

                public void onUserSwitchComplete(int i) throws RemoteException {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(314, i, 0));
                }
            }, "KeyguardUpdateMonitor");
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
        this.mTrustManager = (TrustManager) context.getSystemService("trust");
        this.mTrustManager.registerTrustListener(this);
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mLockPatternUtils.registerStrongAuthTracker(this.mStrongAuthTracker);
        this.mStatusBarHeight = this.mContext.getResources().getDimensionPixelOffset(R.dimen.status_bar_height);
        this.mFpm = (FingerprintManager) context.getSystemService("fingerprint");
        updateFingerprintListeningState();
        FingerprintManager fingerprintManager = this.mFpm;
        if (fingerprintManager != null) {
            fingerprintManager.addLockoutResetCallback(this.mLockoutResetCallback);
        }
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("second_user_id"), false, this.mSecondUserProviderObserver, 0);
        this.mSecondUserProviderObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("kid_user_id"), false, this.mKidSpaceUserProviderObserver, 0);
        this.mKidSpaceUserProviderObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("power_supersave_mode_open"), false, this.mSuperSavePowerObserver, -1);
        this.mSuperSavePowerObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("ui_night_mode"), false, this.mDarkModeObserver, -1);
        this.mDarkModeObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("darken_wallpaper_under_dark_mode"), false, this.mDarkWallpaperModeObserver, -1);
        this.mDarkWallpaperModeObserver.onChange(false);
        updateWallpaper(true);
        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction("com.miui.keyguard.setwallpaper");
        context.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean booleanExtra = intent.getBooleanExtra("set_lock_wallpaper_result", true);
                Log.d("KeyguardUpdateMonitor", "set_lock_wallpaper_result:" + booleanExtra);
                if (booleanExtra) {
                    String stringExtra = intent.getStringExtra("video24_wallpaper");
                    if (!TextUtils.isEmpty(stringExtra)) {
                        String unused = KeyguardUpdateMonitor.sVideo24WallpaperThumnailName = stringExtra;
                    }
                    KeyguardUpdateMonitor.this.updateWallpaper(booleanExtra);
                }
            }
        }, UserHandle.ALL, intentFilter5, (String) null, (Handler) null);
        this.mIsPsensorDisabled = MiuiKeyguardUtils.isPsensorDisabled(this.mContext);
        if (MiuiKeyguardUtils.IS_OPERATOR_CUSTOMIZATION_TEST) {
            this.mPhoneSignalController = new PhoneSignalController(this.mContext);
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(MiuiKeyguardUtils.AOD_MODE), false, this.mAODObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("aod_using_super_wallpaper"), false, this.mAODObserver, -1);
        this.mAODObserver.onChange(false);
        if (!MiuiKeyguardUtils.isSystemProcess()) {
            Slog.w("KeyguardUpdateMonitor", "second space should not init KeyguardUpdateMonitor:" + new Throwable());
        }
    }

    /* access modifiers changed from: private */
    public void updateWallpaper(final boolean z) {
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            public Void doInBackground(Void... voidArr) {
                if (!z) {
                    return null;
                }
                KeyguardUpdateMonitor.this.processKeyguardWallpaper();
                return null;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void voidR) {
                Iterator it = KeyguardUpdateMonitor.this.mWallpaperChangeCallbacks.iterator();
                while (it.hasNext()) {
                    ((WallpaperChangeCallback) it.next()).onWallpaperChange(z);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public void registerWallpaperChangeCallback(WallpaperChangeCallback wallpaperChangeCallback) {
        if (!this.mWallpaperChangeCallbacks.contains(wallpaperChangeCallback)) {
            this.mWallpaperChangeCallbacks.add(wallpaperChangeCallback);
            wallpaperChangeCallback.onWallpaperChange(false);
        }
    }

    public void unregisterWallpaperChangeCallback(WallpaperChangeCallback wallpaperChangeCallback) {
        this.mWallpaperChangeCallbacks.remove(wallpaperChangeCallback);
    }

    public void registerPhoneSignalChangeCallback() {
        PhoneSignalController phoneSignalController = this.mPhoneSignalController;
        if (phoneSignalController != null) {
            phoneSignalController.registerPhoneSignalChangeCallback(this.mPhoneSignalChangeCallback);
        }
    }

    public void unRegisterPhoneSignalChangeCallback() {
        PhoneSignalController phoneSignalController = this.mPhoneSignalController;
        if (phoneSignalController != null) {
            phoneSignalController.removePhoneSignalChangeCallback(this.mPhoneSignalChangeCallback);
        }
    }

    public static String getVideo24WallpaperThumnailName() {
        return sVideo24WallpaperThumnailName;
    }

    public void processKeyguardWallpaper() {
        Drawable lockWallpaperPreview = KeyguardWallpaperUtils.getLockWallpaperPreview(this.mContext);
        if (lockWallpaperPreview != null) {
            sWallpaperColorLight = MiuiKeyguardUtils.getBitmapColorMode(((BitmapDrawable) lockWallpaperPreview).getBitmap()) == 2;
            updateWallpaperBlurColor();
        }
    }

    public void updateWallpaperBlurColor() {
        int i;
        Drawable lockWallpaperPreview = KeyguardWallpaperUtils.getLockWallpaperPreview(this.mContext);
        int color = this.mContext.getResources().getColor(R.color.wallpaper_des_text_dark_color);
        Bitmap awesomeLockScreen = getAwesomeLockScreen(this.mContext, lockWallpaperPreview);
        if (awesomeLockScreen != null) {
            i = MiuiKeyguardUtils.getFastBlurColor(this.mContext, awesomeLockScreen);
        } else {
            i = MiuiKeyguardUtils.getFastBlurColor(this.mContext, lockWallpaperPreview);
        }
        if (awesomeLockScreen != null) {
            awesomeLockScreen.recycle();
        }
        if (i != -1) {
            color = MiuiKeyguardUtils.addTwoColor(i, color);
        }
        this.mWallpaperBlurColor = color;
    }

    private Bitmap getAwesomeLockScreen(Context context, Drawable drawable) {
        Bitmap bitmap;
        if (new File("/data/system/theme/lockscreen").exists()) {
            ZipResourceLoader zipResourceLoader = new ZipResourceLoader("/data/system/theme/lockscreen", "advance/");
            try {
                zipResourceLoader.setLocal(context.getResources().getConfiguration().locale);
                ScreenElementRoot screenElementRoot = new ScreenElementRoot(new ScreenContext(context, zipResourceLoader, new LockscreenElementFactory()));
                if (!screenElementRoot.load()) {
                    return null;
                }
                screenElementRoot.init();
                Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
                DisplayMetrics displayMetrics = new DisplayMetrics();
                defaultDisplay.getMetrics(displayMetrics);
                if (drawable == null || !(drawable instanceof BitmapDrawable)) {
                    bitmap = Bitmap.createBitmap(displayMetrics.widthPixels, displayMetrics.heightPixels, Bitmap.Config.ARGB_8888);
                } else {
                    bitmap = ((BitmapDrawable) drawable).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
                }
                Canvas canvas = new Canvas(bitmap);
                screenElementRoot.tick(SystemClock.elapsedRealtime());
                screenElementRoot.render(canvas);
                screenElementRoot.setKeepResource(true);
                screenElementRoot.finish();
                return bitmap;
            } catch (Exception e) {
                Log.e("KeyguardUpdateMonitor", "get awesone lock screen fail", e);
            }
        }
        return null;
    }

    private static class LockscreenElementFactory extends ScreenElementFactory {
        LockscreenElementFactory() {
        }

        public ScreenElement createInstance(Element element, ScreenElementRoot screenElementRoot) {
            return element.getTagName().equalsIgnoreCase("Unlocker") ? new AdvancedSlider(element, screenElementRoot) : KeyguardUpdateMonitor.super.createInstance(element, screenElementRoot);
        }
    }

    public static boolean isWallpaperColorLight(Context context) {
        if (WallpaperAuthorityUtils.isLiveWallpaper(context)) {
            return sWallpaperColorLight;
        }
        return sWallpaperColorLight && (!sIsDarkMode || !sIsDarkWallpaperMode);
    }

    public boolean needDarkenWallpaper() {
        return !sWallpaperColorLight;
    }

    public int getWallpaperBlurColor() {
        return this.mWallpaperBlurColor;
    }

    public boolean mustPasswordUnlockDevice() {
        return this.mKeyguardMediator.getViewMediatorCallback().getBouncerPromptReason() != 0;
    }

    public void updateFingerprintListeningState() {
        this.mHandler.removeCallbacks(this.mRetryFingerprintAuthentication);
        boolean shouldListenForFingerprint = shouldListenForFingerprint();
        if (this.mFingerprintRunningState == 1 && !shouldListenForFingerprint) {
            stopListeningForFingerprint();
        } else if (this.mFingerprintRunningState != 1 && shouldListenForFingerprint) {
            startListeningForFingerprint();
        }
    }

    public boolean shouldListenForFingerprint() {
        boolean z = (this.mKeyguardIsVisible || !this.mDeviceInteractive || ((this.mBouncer && !this.mKeyguardGoingAway) || this.mGoingToSleep || this.mKeyguardShowingAndOccluded)) && !this.mSwitchingUser && !isFingerprintDisabled(getCurrentUser()) && (!isKeyguardHide() || this.mGoingToSleep) && !isFingerprintUnlock() && MiuiKeyguardUtils.isSystemProcess() && (!isFaceUnlock() || !MiuiKeyguardUtils.isBroadSideFingerprint());
        if (!z || !this.mKeyguardOccluded || (this.mBouncer && !this.mKeyguardGoingAway)) {
            return z;
        }
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            return !this.mDeviceInteractive;
        }
        return !MiuiKeyguardUtils.isTopActivitySystemApp(this.mContext);
    }

    private void startListeningForFingerprint() {
        int i = this.mFingerprintRunningState;
        if (i == 2) {
            setFingerprintRunningState(3);
        } else if (i != 3) {
            Log.v("KeyguardUpdateMonitor", "startListeningForFingerprint()");
            int currentUser = getCurrentUser();
            if (isUnlockWithFingerprintPossible(currentUser)) {
                CancellationSignal cancellationSignal = this.mFingerprintCancelSignal;
                if (cancellationSignal != null) {
                    cancellationSignal.cancel();
                }
                this.mFingerprintCancelSignal = new CancellationSignal();
                this.mFpm.authenticate((FingerprintManager.CryptoObject) null, this.mFingerprintCancelSignal, 0, this.mAuthenticationCallback, (Handler) null, currentUser);
                setFingerprintRunningState(1);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleLocaleChanged() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
            }
        }
    }

    public boolean isUnlockWithFingerprintPossible(int i) {
        if (!this.mFingerprintPossibleMap.containsKey(Integer.valueOf(i)) || this.mFingerprintPossibleMap.get(Integer.valueOf(i)) == null || !this.mKeyguardShowing) {
            FingerprintManager fingerprintManager = this.mFpm;
            this.mFingerprintPossibleMap.put(Integer.valueOf(i), Boolean.valueOf(fingerprintManager != null && fingerprintManager.isHardwareDetected() && this.mFpm.getEnrolledFingerprints(i).size() > 0));
        }
        if (!this.mFingerprintPossibleMap.get(Integer.valueOf(i)).booleanValue() || isFingerprintDisabled(i)) {
            return false;
        }
        return true;
    }

    private void stopListeningForFingerprint() {
        Log.v("KeyguardUpdateMonitor", "stopListeningForFingerprint()");
        if (this.mFingerprintRunningState == 1) {
            CancellationSignal cancellationSignal = this.mFingerprintCancelSignal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
            }
            this.mFingerprintCancelSignal = null;
            setFingerprintRunningState(2);
        }
        if (this.mFingerprintRunningState == 3) {
            setFingerprintRunningState(2);
        }
    }

    private void watchForDeviceProvisioning() {
        this.mDeviceProvisionedObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                super.onChange(z);
                KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.this;
                boolean unused = keyguardUpdateMonitor.mDeviceProvisioned = MiuiKeyguardUtils.isDeviceProvisionedInSettingsDb(keyguardUpdateMonitor.mContext);
                if (KeyguardUpdateMonitor.this.mDeviceProvisioned) {
                    MiuiKeyguardUtils.setUserAuthenticatedSinceBoot();
                    KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(308);
                }
                Log.d("KeyguardUpdateMonitor", "DEVICE_PROVISIONED state = " + KeyguardUpdateMonitor.this.mDeviceProvisioned);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, this.mDeviceProvisionedObserver);
        boolean isDeviceProvisionedInSettingsDb = MiuiKeyguardUtils.isDeviceProvisionedInSettingsDb(this.mContext);
        if (isDeviceProvisionedInSettingsDb != this.mDeviceProvisioned) {
            this.mDeviceProvisioned = isDeviceProvisionedInSettingsDb;
            if (this.mDeviceProvisioned) {
                this.mHandler.sendEmptyMessage(308);
            }
        }
    }

    public boolean hasLockscreenWallpaper() {
        return this.mHasLockscreenWallpaper;
    }

    /* access modifiers changed from: protected */
    public void handleDevicePolicyManagerStateChanged() {
        this.mDpmFingerprintDisable.clear();
        this.mFingerprintPossibleMap.clear();
        updateFingerprintListeningState();
        for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(size).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDevicePolicyManagerStateChanged();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitching(int i, IRemoteCallback iRemoteCallback) {
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserSwitching(i);
            }
        }
        try {
            iRemoteCallback.sendResult((Bundle) null);
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitchComplete(int i) {
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserSwitchComplete(i);
            }
        }
    }

    public void dispatchBootCompleted() {
        this.mHandler.sendEmptyMessage(313);
    }

    /* access modifiers changed from: protected */
    public void handleBootCompleted() {
        if (!this.mBootCompleted) {
            this.mBootCompleted = true;
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onBootCompleted();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleDeviceProvisioned() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDeviceProvisioned();
            }
        }
        if (this.mDeviceProvisionedObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mDeviceProvisionedObserver);
            this.mDeviceProvisionedObserver = null;
        }
    }

    /* access modifiers changed from: protected */
    public void handlePhoneStateChanged(String str) {
        Log.d("KeyguardUpdateMonitor", "handlePhoneStateChanged(" + str + ")");
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(str)) {
            this.mPhoneState = 0;
        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(str)) {
            this.mPhoneState = 2;
        } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(str)) {
            this.mPhoneState = 1;
        }
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onPhoneStateChanged(this.mPhoneState);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleRingerModeChange(int i) {
        Log.d("KeyguardUpdateMonitor", "handleRingerModeChange(" + i + ")");
        this.mRingMode = i;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRingerModeChanged(i);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleTimeUpdate() {
        Log.d("KeyguardUpdateMonitor", "handleTimeUpdate");
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTimeChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleBatteryUpdate(BatteryStatus batteryStatus) {
        Log.d("KeyguardUpdateMonitor", "handleBatteryUpdate");
        this.mBatteryStatus = batteryStatus;
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRefreshBatteryInfo(batteryStatus);
            }
        }
    }

    public void onBatteryStatusChange(BatteryStatus batteryStatus) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(302, batteryStatus));
    }

    public String getChargingState() {
        return this.mBatteryStatus.getChargingState();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0071  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0080  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00af  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00cb  */
    /* JADX WARNING: Removed duplicated region for block: B:52:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleSimStateChange(int r7, int r8, com.android.internal.telephony.IccCardConstants.State r9) {
        /*
            r6 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "handleSimStateChange(subId="
            r0.append(r1)
            r0.append(r7)
            java.lang.String r1 = ", slotId="
            r0.append(r1)
            r0.append(r8)
            java.lang.String r1 = ", state="
            r0.append(r1)
            r0.append(r9)
            java.lang.String r1 = ")"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "KeyguardUpdateMonitor"
            android.util.Log.d(r1, r0)
            boolean r0 = android.telephony.SubscriptionManager.isValidSubscriptionId(r7)
            r2 = 0
            r3 = 1
            if (r0 != 0) goto L_0x0062
            java.lang.String r0 = "invalid subId in handleSimStateChange()"
            android.util.Log.w(r1, r0)
            com.android.internal.telephony.IccCardConstants$State r0 = com.android.internal.telephony.IccCardConstants.State.ABSENT
            if (r9 != r0) goto L_0x005d
            java.util.HashMap<java.lang.Integer, com.android.keyguard.KeyguardUpdateMonitor$SimData> r0 = r6.mSimDatas
            java.util.Collection r0 = r0.values()
            java.util.Iterator r0 = r0.iterator()
        L_0x0046:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x005b
            java.lang.Object r1 = r0.next()
            com.android.keyguard.KeyguardUpdateMonitor$SimData r1 = (com.android.keyguard.KeyguardUpdateMonitor.SimData) r1
            int r4 = r1.slotId
            if (r4 != r8) goto L_0x0046
            com.android.internal.telephony.IccCardConstants$State r4 = com.android.internal.telephony.IccCardConstants.State.ABSENT
            r1.simState = r4
            goto L_0x0046
        L_0x005b:
            r0 = r3
            goto L_0x0063
        L_0x005d:
            boolean r0 = com.android.keyguard.MiuiKeyguardUtils.IS_MTK_BUILD
            if (r0 != 0) goto L_0x0062
            return
        L_0x0062:
            r0 = r2
        L_0x0063:
            java.util.HashMap<java.lang.Integer, com.android.keyguard.KeyguardUpdateMonitor$SimData> r1 = r6.mSimDatas
            java.lang.Integer r4 = java.lang.Integer.valueOf(r8)
            java.lang.Object r1 = r1.get(r4)
            com.android.keyguard.KeyguardUpdateMonitor$SimData r1 = (com.android.keyguard.KeyguardUpdateMonitor.SimData) r1
            if (r1 != 0) goto L_0x0080
            com.android.keyguard.KeyguardUpdateMonitor$SimData r1 = new com.android.keyguard.KeyguardUpdateMonitor$SimData
            r1.<init>(r9, r8, r7)
            java.util.HashMap<java.lang.Integer, com.android.keyguard.KeyguardUpdateMonitor$SimData> r4 = r6.mSimDatas
            java.lang.Integer r5 = java.lang.Integer.valueOf(r8)
            r4.put(r5, r1)
            goto L_0x009f
        L_0x0080:
            com.android.internal.telephony.IccCardConstants$State r4 = r1.simState
            if (r4 == r9) goto L_0x008e
            boolean r4 = r6.isEarlyReportSimUnlocked(r9, r4, r8)
            if (r4 != 0) goto L_0x008e
            r1.simState = r9
            r4 = r3
            goto L_0x008f
        L_0x008e:
            r4 = r2
        L_0x008f:
            if (r4 != 0) goto L_0x009b
            int r4 = r1.subId
            if (r4 != r7) goto L_0x009b
            int r4 = r1.slotId
            if (r4 == r8) goto L_0x009a
            goto L_0x009b
        L_0x009a:
            r3 = r2
        L_0x009b:
            r1.subId = r7
            r1.slotId = r8
        L_0x009f:
            if (r3 != 0) goto L_0x00a3
            if (r0 == 0) goto L_0x00c5
        L_0x00a3:
            com.android.internal.telephony.IccCardConstants$State r0 = com.android.internal.telephony.IccCardConstants.State.UNKNOWN
            if (r9 == r0) goto L_0x00c5
        L_0x00a7:
            java.util.ArrayList<java.lang.ref.WeakReference<com.android.keyguard.KeyguardUpdateMonitorCallback>> r0 = r6.mCallbacks
            int r0 = r0.size()
            if (r2 >= r0) goto L_0x00c5
            java.util.ArrayList<java.lang.ref.WeakReference<com.android.keyguard.KeyguardUpdateMonitorCallback>> r0 = r6.mCallbacks
            java.lang.Object r0 = r0.get(r2)
            java.lang.ref.WeakReference r0 = (java.lang.ref.WeakReference) r0
            java.lang.Object r0 = r0.get()
            com.android.keyguard.KeyguardUpdateMonitorCallback r0 = (com.android.keyguard.KeyguardUpdateMonitorCallback) r0
            if (r0 == 0) goto L_0x00c2
            r0.onSimStateChanged(r7, r8, r9)
        L_0x00c2:
            int r2 = r2 + 1
            goto L_0x00a7
        L_0x00c5:
            com.android.systemui.doze.AodHost r7 = com.android.systemui.Dependency.getHost()
            if (r7 == 0) goto L_0x00d6
            com.android.systemui.doze.AodHost r7 = com.android.systemui.Dependency.getHost()
            boolean r6 = r6.isSimPinSecure()
            r7.onSimPinSecureChanged(r6)
        L_0x00d6:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardUpdateMonitor.handleSimStateChange(int, int, com.android.internal.telephony.IccCardConstants$State):void");
    }

    /* access modifiers changed from: private */
    public void handleServiceStateChange(int i, ServiceState serviceState) {
        Log.d("KeyguardUpdateMonitor", "handleServiceStateChange(subId=" + i + ", serviceState=" + serviceState);
        if (!SubscriptionManager.isValidSubscriptionId(i)) {
            Log.w("KeyguardUpdateMonitor", "invalid subId in handleServiceStateChange()");
            return;
        }
        this.mServiceStates.put(Integer.valueOf(i), serviceState);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
                keyguardUpdateMonitorCallback.onServiceStateChanged(i, serviceState);
            }
        }
    }

    public void onKeyguardVisibilityChanged(boolean z) {
        Log.d("KeyguardUpdateMonitor", "onKeyguardVisibilityChanged(" + z + ")");
        this.mKeyguardIsVisible = z;
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardVisibilityChangedRaw(z);
            }
        }
        updateFingerprintListeningState();
    }

    /* access modifiers changed from: private */
    public void handleKeyguardReset() {
        Log.d("KeyguardUpdateMonitor", "handleKeyguardReset");
        updateFingerprintListeningState();
        this.mNeedsSlowUnlockTransition = resolveNeedsSlowUnlockTransition();
    }

    private boolean resolveNeedsSlowUnlockTransition() {
        if (isUserUnlocked()) {
            return false;
        }
        ComponentInfo componentInfo = ResolveInfoCompat.getComponentInfo(this.mContext.getPackageManager().resolveActivity(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME"), 0));
        return FALLBACK_HOME_COMPONENT.equals(new ComponentName(componentInfo.packageName, componentInfo.name));
    }

    public boolean isUserUnlocked() {
        return UserManagerCompat.isUserUnlocked(this.mUserManager, getCurrentUser());
    }

    /* access modifiers changed from: private */
    public void handleKeyguardBouncerChanged(int i) {
        Log.d("KeyguardUpdateMonitor", "handleKeyguardBouncerChanged(" + i + ")");
        boolean z = true;
        if (i != 1) {
            z = false;
        }
        this.mBouncer = z;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardBouncerChanged(z);
            }
        }
        updateFingerprintListeningState();
    }

    /* access modifiers changed from: private */
    public void handleReportEmergencyCallAction() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onEmergencyCallAction();
            }
        }
    }

    public void removeCallback(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        Log.v("KeyguardUpdateMonitor", "*** unregister callback for " + keyguardUpdateMonitorCallback);
        for (int size = this.mCallbacks.size() + -1; size >= 0; size--) {
            if (this.mCallbacks.get(size).get() == keyguardUpdateMonitorCallback) {
                this.mCallbacks.remove(size);
            }
        }
    }

    public void registerCallback(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        Log.v("KeyguardUpdateMonitor", "*** register callback for " + keyguardUpdateMonitorCallback);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            if (this.mCallbacks.get(i).get() == keyguardUpdateMonitorCallback) {
                Log.e("KeyguardUpdateMonitor", "Object tried to add another callback", new Exception("Called by"));
                return;
            }
        }
        this.mCallbacks.add(new WeakReference(keyguardUpdateMonitorCallback));
        removeCallback((KeyguardUpdateMonitorCallback) null);
        sendUpdates(keyguardUpdateMonitorCallback);
    }

    public boolean isSwitchingUser() {
        return this.mSwitchingUser;
    }

    public void setSwitchingUser(boolean z) {
        this.mSwitchingUser = z;
        updateFingerprintListeningState();
        if (this.mSwitchingUser) {
            FaceUnlockManager.getInstance().stopFaceUnlock();
        }
    }

    private void sendUpdates(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        keyguardUpdateMonitorCallback.onRefreshBatteryInfo(this.mBatteryStatus);
        keyguardUpdateMonitorCallback.onTimeChanged();
        keyguardUpdateMonitorCallback.onRingerModeChanged(this.mRingMode);
        keyguardUpdateMonitorCallback.onPhoneStateChanged(this.mPhoneState);
        keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
        keyguardUpdateMonitorCallback.onAirplaneModeChanged();
        keyguardUpdateMonitorCallback.onClockVisibilityChanged();
        PhoneSignalController phoneSignalController = this.mPhoneSignalController;
        keyguardUpdateMonitorCallback.onPhoneSignalChanged(phoneSignalController != null ? phoneSignalController.isSignalAvailable() : false);
        keyguardUpdateMonitorCallback.onLockWallpaperProviderChanged();
        keyguardUpdateMonitorCallback.onSuperSavePowerChanged(this.mIsSuperSaveModePower);
        keyguardUpdateMonitorCallback.onKeyguardOccludedChanged(this.mKeyguardOccluded);
        keyguardUpdateMonitorCallback.onKeyguardShowingChanged(this.mKeyguardShowing);
        for (Map.Entry<Integer, SimData> value : this.mSimDatas.entrySet()) {
            SimData simData = (SimData) value.getValue();
            IccCardConstants.State state = simData.simState;
            if (state != IccCardConstants.State.UNKNOWN) {
                keyguardUpdateMonitorCallback.onSimStateChanged(simData.subId, simData.slotId, state);
            }
        }
    }

    public void sendKeyguardReset() {
        this.mHandler.obtainMessage(312).sendToTarget();
    }

    public void sendKeyguardBouncerChanged(boolean z) {
        Log.d("KeyguardUpdateMonitor", "sendKeyguardBouncerChanged(" + z + ")");
        Message obtainMessage = this.mHandler.obtainMessage(322);
        obtainMessage.arg1 = z ? 1 : 0;
        obtainMessage.sendToTarget();
    }

    public void reportSimUnlocked(int i) {
        Log.v("KeyguardUpdateMonitor", "reportSimUnlocked(subId=" + i + ")");
        int slotIndex = SubscriptionManagerCompat.getSlotIndex(i);
        handleSimStateChange(i, slotIndex, IccCardConstants.State.READY);
        setSimStateEarlyReady(slotIndex, true);
    }

    public void setSimStateEarlyReady(int i, boolean z) {
        this.mSimStateEarlyReadyStatus.put(Integer.valueOf(i), Boolean.valueOf(z));
    }

    private boolean isSimStateEarlyReady(int i) {
        return this.mSimStateEarlyReadyStatus.get(Integer.valueOf(i)).booleanValue();
    }

    public void reportEmergencyCallAction(boolean z) {
        if (!z) {
            this.mHandler.obtainMessage(318).sendToTarget();
        } else {
            handleReportEmergencyCallAction();
        }
    }

    public boolean isDeviceProvisioned() {
        return this.mDeviceProvisioned;
    }

    public void clearFailedUnlockAttempts() {
        this.mFailedAttempts.delete(sCurrentUser);
    }

    public int getFailedUnlockAttempts(int i) {
        return this.mFailedAttempts.get(i, 0);
    }

    public void reportFailedStrongAuthUnlockAttempt(int i) {
        this.mFailedAttempts.put(i, getFailedUnlockAttempts(i) + 1);
    }

    public void clearFingerprintRecognized() {
        this.mUserFingerprintAuthenticated.clear();
        this.mUserFaceAuthenticated.clear();
        this.mUserBleAuthenticated.clear();
    }

    public boolean isSimPinVoiceSecure() {
        return isSimPinSecure();
    }

    public boolean isSimPinSecure() {
        for (SubscriptionInfo simSlotIndex : getSubscriptionInfo(false)) {
            if (isSimPinSecure(getSimState(simSlotIndex.getSimSlotIndex()))) {
                return true;
            }
        }
        return false;
    }

    public IccCardConstants.State getSimState(int i) {
        if (this.mSimDatas.containsKey(Integer.valueOf(i))) {
            return this.mSimDatas.get(Integer.valueOf(i)).simState;
        }
        return IccCardConstants.State.UNKNOWN;
    }

    public boolean isOOS() {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        boolean z = true;
        for (int i = 0; i < phoneCount; i++) {
            int[] subId = SubscriptionManager.getSubId(i);
            if (subId != null && subId.length >= 1) {
                Log.d("KeyguardUpdateMonitor", "slot id:" + i + " subId:" + subId[0]);
                ServiceState serviceState = this.mServiceStates.get(Integer.valueOf(subId[0]));
                if (serviceState != null) {
                    if (serviceState.isEmergencyOnly()) {
                        z = false;
                    }
                    if (!(serviceState.getVoiceRegState() == 1 || serviceState.getVoiceRegState() == 3)) {
                        z = false;
                    }
                    Log.d("KeyguardUpdateMonitor", "is emergency: " + serviceState.isEmergencyOnly());
                    Log.d("KeyguardUpdateMonitor", "voice state: " + serviceState.getVoiceRegState());
                } else {
                    Log.d("KeyguardUpdateMonitor", "state is NULL");
                }
            }
        }
        Log.d("KeyguardUpdateMonitor", "is Emergency supported: " + z);
        return z;
    }

    public void setFingerprintMode(int i) {
        if (this.mFingerprintMode != i) {
            this.mFingerprintMode = i;
            if (!isFingerprintUnlock()) {
                updateFingerprintListeningState();
            }
        }
    }

    public boolean isFingerprintUnlock() {
        int i = this.mFingerprintMode;
        return i == 5 || i == 1 || i == 2 || i == 6;
    }

    public boolean isFingerprintWakeUnlock() {
        int i = this.mFingerprintMode;
        return i == 1 || i == 2;
    }

    public void setFaceUnlockMode(int i) {
        this.mFaceUnlockMode = i;
    }

    public boolean isFaceUnlock() {
        int i = this.mFaceUnlockMode;
        return i == 5 || i == 6;
    }

    private boolean refreshSimState(int i, int i2) {
        IccCardConstants.State state;
        boolean z;
        int simState = TelephonyManager.from(this.mContext).getSimState(i2);
        try {
            state = IccCardConstants.State.intToState(simState);
        } catch (IllegalArgumentException unused) {
            Log.w("KeyguardUpdateMonitor", "Unknown sim state: " + simState);
            state = IccCardConstants.State.UNKNOWN;
        }
        SimData simData = this.mSimDatas.get(Integer.valueOf(i2));
        if (simData == null) {
            this.mSimDatas.put(Integer.valueOf(i2), new SimData(state, i2, i));
            return true;
        }
        IccCardConstants.State state2 = simData.simState;
        if (state2 == state || isEarlyReportSimUnlocked(state, state2, i2)) {
            z = false;
        } else {
            simData.simState = state;
            z = true;
        }
        if (z || simData.subId != i) {
            return true;
        }
        return false;
    }

    private boolean isEarlyReportSimUnlocked(IccCardConstants.State state, IccCardConstants.State state2, int i) {
        return (state == IccCardConstants.State.PIN_REQUIRED || state == IccCardConstants.State.PUK_REQUIRED) && state2 == IccCardConstants.State.READY && isSimStateEarlyReady(i);
    }

    public static boolean isSimPinSecure(IccCardConstants.State state) {
        return state == IccCardConstants.State.PIN_REQUIRED || state == IccCardConstants.State.PUK_REQUIRED || state == IccCardConstants.State.PERM_DISABLED;
    }

    public void dispatchStartedWakingUp() {
        synchronized (this) {
            this.mDeviceInteractive = true;
        }
        this.mHandler.sendEmptyMessage(319);
    }

    public void dispatchStartedWakingUpWithReason(String str) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(336, str));
    }

    public void dispatchStartedGoingToSleep(int i) {
        synchronized (this) {
            this.mDeviceInteractive = false;
        }
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(321, i, 0));
    }

    public void dispatchFinishedGoingToSleep(int i) {
        synchronized (this) {
            this.mDeviceInteractive = false;
        }
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(320, i, 0));
    }

    public void dispatchScreenTurnedOn() {
        synchronized (this) {
            this.mScreenOn = true;
        }
        this.mHandler.sendEmptyMessage(331);
    }

    public void dispatchScreenTurnedOff() {
        synchronized (this) {
            this.mScreenOn = false;
        }
        this.mHandler.sendEmptyMessage(332);
    }

    public void dispatchDreamingStarted() {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(333, 1, 0));
    }

    public void dispatchDreamingStopped() {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(333, 0, 0));
    }

    public boolean isDeviceInteractive() {
        return this.mDeviceInteractive;
    }

    public int getNextSubIdForState(IccCardConstants.State state) {
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(false);
        int i = -1;
        int i2 = Integer.MAX_VALUE;
        for (int i3 = 0; i3 < subscriptionInfo.size(); i3++) {
            int subscriptionId = subscriptionInfo.get(i3).getSubscriptionId();
            int slotIndex = SubscriptionManagerCompat.getSlotIndex(subscriptionId);
            if (state == getSimState(slotIndex) && i2 > slotIndex) {
                i = subscriptionId;
                i2 = slotIndex;
            }
        }
        return i;
    }

    public SubscriptionInfo getSubscriptionInfoForSubId(int i) {
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(false);
        for (int i2 = 0; i2 < subscriptionInfo.size(); i2++) {
            SubscriptionInfo subscriptionInfo2 = subscriptionInfo.get(i2);
            if (i == subscriptionInfo2.getSubscriptionId()) {
                return subscriptionInfo2;
            }
        }
        return null;
    }

    public void setBLEUnlockState(MiuiBleUnlockHelper.BLEUnlockState bLEUnlockState) {
        this.mBLEUnlockState = bLEUnlockState;
        if (bLEUnlockState == MiuiBleUnlockHelper.BLEUnlockState.SUCCEED) {
            this.mUserBleAuthenticated.put(getCurrentUser(), true);
            Intent intent = new Intent("miui_keyguard_ble_unlock_succeed");
            intent.setPackage(this.mContext.getPackageName());
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        }
    }

    public boolean isBleUnlockSuccess() {
        return this.mBLEUnlockState == MiuiBleUnlockHelper.BLEUnlockState.SUCCEED;
    }

    public void resetAllFingerprintLockout() {
        this.mIsFingerprintPermanentlyLockout = false;
        this.mIsFingerprintTemporarilyLockout = false;
    }

    public boolean isFingerprintPermanentlyLockout() {
        return this.mIsFingerprintPermanentlyLockout;
    }

    public boolean isFingerprintTemporarilyLockout() {
        return this.mIsFingerprintTemporarilyLockout;
    }

    public boolean isPsensorDisabled() {
        return this.mIsPsensorDisabled;
    }

    public void setKeyguardViewMediator(KeyguardViewMediator keyguardViewMediator) {
        this.mKeyguardMediator = keyguardViewMediator;
    }

    public void updateShowingState(boolean z) {
        Message obtainMessage = this.mHandler.obtainMessage(335);
        obtainMessage.arg1 = z ? 1 : 0;
        obtainMessage.sendToTarget();
    }

    /* access modifiers changed from: private */
    public void handleShowingStateChange(int i) {
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                boolean z = true;
                if (i != 1) {
                    z = false;
                }
                keyguardUpdateMonitorCallback.updateShowingStatus(z);
            }
        }
    }

    public boolean isSupportLockScreenMagazineLeft() {
        return this.mIsSupportLockScreenMagazineLeft;
    }

    public boolean isSupportLockScreenMagazineLeftOverlay() {
        return this.mIsSupportLockScreenMagazineLeftOverlay;
    }

    public void setSupportLockScreenMagazineOverlay(boolean z) {
        this.mIsSupportLockScreenMagazineLeftOverlay = z;
    }

    public boolean isLockScreenLeftOverlayAvailable() {
        return this.mLockScreenLeftOverlayAvailable;
    }

    public void setLockScreenLeftOverlayAvailable(boolean z) {
        this.mLockScreenLeftOverlayAvailable = z;
    }

    public void setSupportLockScreenMagazineLeft(boolean z) {
        this.mIsSupportLockScreenMagazineLeft = z;
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onLockScreenMagazineStatusChanged();
            }
        }
    }

    public void handleBottomAreaButtonClicked(boolean z) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBottomAreaButtonClicked(z);
            }
        }
    }

    public void handleLockScreenMagazinePreViewVisibilityChanged(boolean z) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onLockScreenMagazinePreViewVisibilityChanged(z);
            }
        }
    }

    public void handleLockWallpaperProviderChanged() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onLockWallpaperProviderChanged();
            }
        }
    }

    public int getPhoneState() {
        return this.mPhoneState;
    }

    public boolean isShowingChargeAnimationWindow() {
        return this.mChargeAnimationWindowShowing;
    }

    public void setShowingChargeAnimationWindow(boolean z) {
        if (this.mChargeAnimationWindowShowing != z) {
            this.mChargeAnimationWindowShowing = z;
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                MiuiGxzwManager.getInstance().updateGxzwState();
            }
            FaceUnlockManager.getInstance().setShowingChargeAnimationWindow(z);
        }
    }

    private void handleKeyguardOccludedChanged(boolean z) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardOccludedChanged(z);
            }
        }
    }

    private void handleKeyguardShowingChanged(boolean z) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardShowingChanged(z);
            }
        }
    }

    public boolean isAodUsingSuperWallpaper() {
        return this.mAodEnable && this.mAodUsingSuperWallpaperStyle;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardUpdateMonitor state:");
        printWriter.println("  SIM States:");
        for (SimData simData : this.mSimDatas.values()) {
            printWriter.println("    " + simData.toString());
        }
        printWriter.println("  Service states:");
        for (Integer intValue : this.mServiceStates.keySet()) {
            int intValue2 = intValue.intValue();
            printWriter.println("    " + intValue2 + "=" + this.mServiceStates.get(Integer.valueOf(intValue2)));
        }
        FingerprintManager fingerprintManager = this.mFpm;
        if (fingerprintManager != null && fingerprintManager.isHardwareDetected()) {
            int currentUser = ActivityManager.getCurrentUser();
            int strongAuthForUser = this.mStrongAuthTracker.getStrongAuthForUser(currentUser);
            printWriter.println("  Fingerprint state (user=" + currentUser + ")");
            StringBuilder sb = new StringBuilder();
            sb.append("    allowed=");
            sb.append(isUnlockingWithFingerprintAllowed());
            printWriter.println(sb.toString());
            printWriter.println("    auth'd=" + this.mUserFingerprintAuthenticated.get(currentUser));
            printWriter.println("    authSinceBoot=" + getStrongAuthTracker().hasUserAuthenticatedSinceBoot());
            printWriter.println("    disabled(DPM)=" + isFingerprintDisabled(currentUser));
            printWriter.println("    possible=" + isUnlockWithFingerprintPossible(currentUser));
            printWriter.println("    strongAuthFlags=" + Integer.toHexString(strongAuthForUser));
            printWriter.println("    trustManaged=" + getUserTrustIsManaged(currentUser));
            printWriter.println("    fingerprintMode=" + this.mFingerprintMode);
            printWriter.println("    fingerprintRunningState=" + this.mFingerprintRunningState);
        }
        printWriter.println("    supportFaceUnlock=" + MiuiFaceUnlockUtils.isSupportFaceUnlock(this.mContext));
        printWriter.println("    hasEnrolledFaces=" + MiuiFaceUnlockUtils.hasEnrolledFaces(this.mContext));
    }
}
