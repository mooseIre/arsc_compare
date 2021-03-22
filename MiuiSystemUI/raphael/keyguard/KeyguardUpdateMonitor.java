package com.android.keyguard;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.UserSwitchObserver;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricSourceType;
import android.hardware.biometrics.CryptoObject;
import android.hardware.biometrics.IBiometricEnabledOnKeyguardCallback;
import android.hardware.face.FaceManager;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.miuiface.BaseMiuiFaceManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import androidx.lifecycle.Observer;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.charge.MiuiBatteryStatus;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.faceunlock.MiuiFaceUnlockUtils;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.MiuiWallpaperClient;
import com.android.settingslib.WirelessUtils;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.util.Assert;
import com.android.systemui.util.RingerModeTracker;
import com.google.android.collect.Lists;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import miui.os.Build;

public class KeyguardUpdateMonitor implements TrustManager.TrustListener, Dumpable {
    public static final boolean CORE_APPS_ONLY;
    private static final boolean DEBUG_FACE = Build.IS_DEBUGGABLE;
    private static final ComponentName FALLBACK_HOME_COMPONENT = new ComponentName("com.android.settings", "com.android.settings.FallbackHome");
    private static int sCurrentUser;
    private int mActiveMobileDataSubscription = -1;
    private boolean mAuthInterruptActive;
    private final Executor mBackgroundExecutor;
    @VisibleForTesting
    MiuiBatteryStatus mBatteryStatus;
    private IBiometricEnabledOnKeyguardCallback mBiometricEnabledCallback = new IBiometricEnabledOnKeyguardCallback.Stub() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass3 */

        public void onChanged(BiometricSourceType biometricSourceType, boolean z, int i) throws RemoteException {
            if (biometricSourceType == BiometricSourceType.FACE) {
                KeyguardUpdateMonitor.this.mFaceSettingEnabledForUser.put(i, z);
                KeyguardUpdateMonitor.this.updateFaceListeningState();
            }
        }
    };
    private BiometricManager mBiometricManager;
    private boolean mBouncer;
    @VisibleForTesting
    protected final BroadcastReceiver mBroadcastAllReceiver = new BroadcastReceiver() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass10 */

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
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(309, Integer.valueOf(getSendingUserId())));
            } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(334, getSendingUserId(), 0));
            } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(340, intent.getIntExtra("android.intent.extra.user_handle", -1), 0));
            } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(341, intent.getIntExtra("android.intent.extra.user_handle", -1), 0));
            } else if ("miui.intent.action.MIUI_REGION_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mUpdateMonitorInjector.onRegionChanged(KeyguardUpdateMonitor.this.mCallbacks);
            }
        }
    };
    private final BroadcastDispatcher mBroadcastDispatcher;
    @VisibleForTesting
    protected final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass9 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("KeyguardUpdateMonitor", "received broadcast " + action);
            if ("android.intent.action.TIME_TICK".equals(action) || "android.intent.action.TIME_SET".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(301);
            } else if ("android.intent.action.TIMEZONE_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(339, intent.getStringExtra("time-zone")));
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                SimData fromIntent = SimData.fromIntent(intent);
                if (!intent.getBooleanExtra("rebroadcastOnUnlock", false)) {
                    Log.v("KeyguardUpdateMonitor", "action " + action + " state: " + intent.getStringExtra("ss") + " slotId: " + fromIntent.slotId + " subid: " + fromIntent.subId);
                    KeyguardUpdateMonitor.this.mHandler.obtainMessage(304, fromIntent.subId, fromIntent.slotId, Integer.valueOf(fromIntent.simState)).sendToTarget();
                } else if (fromIntent.simState == 1) {
                    KeyguardUpdateMonitor.this.mHandler.obtainMessage(338, Boolean.TRUE).sendToTarget();
                }
            } else if ("android.intent.action.PHONE_STATE".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(306, intent.getStringExtra("state")));
            } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(329);
            } else if ("android.intent.action.SERVICE_STATE".equals(action)) {
                ServiceState newFromBundle = ServiceState.newFromBundle(intent.getExtras());
                int intExtra = intent.getIntExtra("android.telephony.extra.SUBSCRIPTION_INDEX", -1);
                Log.v("KeyguardUpdateMonitor", "action " + action + " serviceState=" + newFromBundle + " subId=" + intExtra);
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(330, intExtra, 0, newFromBundle));
            } else if ("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(328);
            } else if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(337);
            }
        }
    };
    private final ArrayList<WeakReference<KeyguardUpdateMonitorCallback>> mCallbacks = Lists.newArrayList();
    private final Runnable mCancelNotReceived = new Runnable(this) {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass1 */

        public void run() {
            Log.w("KeyguardUpdateMonitor", "Cancel not received, transitioning to STOPPED");
        }
    };
    private final Context mContext;
    private boolean mDeviceInteractive;
    private final DevicePolicyManager mDevicePolicyManager;
    private boolean mDeviceProvisioned;
    private ContentObserver mDeviceProvisionedObserver;
    private final IDreamManager mDreamManager;
    BaseMiuiFaceManager.AuthenticationCallback mFaceAuthenticationCallback = new BaseMiuiFaceManager.AuthenticationCallback() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass14 */

        public void onAuthenticationFailed() {
            Slog.d("miui_face", "onAuthenticationFailed");
            KeyguardUpdateMonitor.this.handleFaceAuthFailed();
        }

        public void onAuthenticationSucceeded(FaceManager.AuthenticationResult authenticationResult) {
            Slog.d("miui_face", "onAuthenticationSucceeded result.getUserId()=" + authenticationResult.getUserId());
            Trace.beginSection("KeyguardUpdateMonitor#onAuthenticationSucceeded");
            KeyguardUpdateMonitor.this.handleFaceAuthenticated(authenticationResult.getUserId(), authenticationResult.isStrongBiometric());
            Trace.endSection();
        }

        public void onAuthenticationHelp(int i, CharSequence charSequence) {
            Slog.d("miui_face", "onAuthenticationHelp helpMsgId=" + i + ";helpString=" + ((Object) charSequence));
            KeyguardUpdateMonitor.this.handleFaceHelp(i, TextUtils.isEmpty(charSequence) ? "" : charSequence.toString());
        }

        public void onAuthenticationError(int i, CharSequence charSequence) {
            Slog.d("miui_face", "onAuthenticationError errMsgId=" + i + ";errString=" + ((Object) charSequence));
            KeyguardUpdateMonitor.this.handleFaceError(i, TextUtils.isEmpty(charSequence) ? "" : charSequence.toString());
        }

        public void onAuthenticationAcquired(int i) {
            Slog.d("miui_face", "onAuthenticationAcquired acquireInfo=" + i);
            KeyguardUpdateMonitor.this.handleFaceAcquired(i);
        }
    };
    private CancellationSignal mFaceCancelSignal;
    private ArrayDeque<KeyguardFaceListenModel> mFaceListenModels;
    private final FaceManager.LockoutResetCallback mFaceLockoutResetCallback = new FaceManager.LockoutResetCallback() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass12 */

        public void onLockoutReset() {
            KeyguardUpdateMonitor.this.handleFaceLockoutReset();
        }
    };
    private BaseMiuiFaceManager mFaceManager;
    private int mFaceRunningState = 0;
    private SparseBooleanArray mFaceSettingEnabledForUser = new SparseBooleanArray();
    private FingerprintManager.AuthenticationCallback mFingerprintAuthenticationCallback = new FingerprintManager.AuthenticationCallback() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass13 */

        public void onAuthenticationFailed() {
            KeyguardUpdateMonitor.this.handleFingerprintAuthFailed();
        }

        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult authenticationResult) {
            Trace.beginSection("KeyguardUpdateMonitor#onAuthenticationSucceeded");
            int authUserId = MiuiKeyguardUtils.getAuthUserId(KeyguardUpdateMonitor.this.mContext, authenticationResult.getFingerprint().getBiometricId());
            KeyguardUpdateMonitor.this.mUpdateMonitorInjector.handlePreBiometricAuthenticated(authUserId);
            if (((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock()) {
                DejankUtils.postAfterTraversal(new Runnable(authUserId, authenticationResult) {
                    /* class com.android.keyguard.$$Lambda$KeyguardUpdateMonitor$13$I5ZTgDlr7UTVvoKmj9d4GdQbTbE */
                    public final /* synthetic */ int f$1;
                    public final /* synthetic */ FingerprintManager.AuthenticationResult f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        KeyguardUpdateMonitor.AnonymousClass13.this.lambda$onAuthenticationSucceeded$0$KeyguardUpdateMonitor$13(this.f$1, this.f$2);
                    }
                });
            } else {
                KeyguardUpdateMonitor.this.handleFingerprintAuthenticated(authUserId, authenticationResult.isStrongBiometric());
            }
            Trace.endSection();
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onAuthenticationSucceeded$0 */
        public /* synthetic */ void lambda$onAuthenticationSucceeded$0$KeyguardUpdateMonitor$13(int i, FingerprintManager.AuthenticationResult authenticationResult) {
            KeyguardUpdateMonitor.this.handleFingerprintAuthenticated(i, authenticationResult.isStrongBiometric());
        }

        public void onAuthenticationHelp(int i, CharSequence charSequence) {
            KeyguardUpdateMonitor.this.handleFingerprintHelp(i, charSequence.toString());
        }

        public void onAuthenticationError(int i, CharSequence charSequence) {
            KeyguardUpdateMonitor.this.handleFingerprintError(i, TextUtils.isEmpty(charSequence) ? "" : charSequence.toString());
        }

        public void onAuthenticationAcquired(int i) {
            KeyguardUpdateMonitor.this.handleFingerprintAcquired(i);
        }
    };
    private CancellationSignal mFingerprintCancelSignal;
    private boolean mFingerprintLockedOut;
    private final FingerprintManager.LockoutResetCallback mFingerprintLockoutResetCallback = new FingerprintManager.LockoutResetCallback() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass11 */

        public void onLockoutReset() {
            KeyguardUpdateMonitor.this.handleFingerprintLockoutReset();
        }
    };
    private int mFingerprintRunningState = 0;
    private FingerprintManager mFpm;
    private boolean mGoingToSleep;
    private final Handler mHandler;
    private int mHardwareFaceUnavailableRetryCount = 0;
    private int mHardwareFingerprintUnavailableRetryCount = 0;
    private boolean mHasLockscreenWallpaper;
    private final boolean mIsAutomotive;
    private boolean mIsDreaming;
    private final boolean mIsPrimaryUser;
    private KeyguardBypassController mKeyguardBypassController;
    private boolean mKeyguardGoingAway;
    private boolean mKeyguardIsVisible;
    private boolean mKeyguardOccluded;
    private LockPatternUtils mLockPatternUtils;
    private boolean mLogoutEnabled;
    private MiuiFaceUnlockManager mMiuiFaceUnlockManager;
    private boolean mNeedsSlowUnlockTransition;
    private int mPhoneState;
    @VisibleForTesting
    public PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass4 */

        public void onActiveDataSubscriptionIdChanged(int i) {
            KeyguardUpdateMonitor.this.mActiveMobileDataSubscription = i;
            KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(328);
        }
    };
    private Runnable mRetryFaceAuthentication = new Runnable() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass8 */

        public void run() {
            Log.w("KeyguardUpdateMonitor", "Retrying face after HW unavailable, attempt " + KeyguardUpdateMonitor.this.mHardwareFaceUnavailableRetryCount);
            KeyguardUpdateMonitor.this.updateFaceListeningState();
        }
    };
    private Runnable mRetryFingerprintAuthentication = new Runnable() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass7 */

        public void run() {
            Log.w("KeyguardUpdateMonitor", "Retrying fingerprint after HW unavailable, attempt " + KeyguardUpdateMonitor.this.mHardwareFingerprintUnavailableRetryCount);
            KeyguardUpdateMonitor.this.updateFingerprintListeningState();
        }
    };
    private int mRingMode;
    private final Observer<Integer> mRingerModeObserver = new Observer<Integer>() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass2 */

        public void onChanged(Integer num) {
            KeyguardUpdateMonitor.this.mHandler.obtainMessage(305, num.intValue(), 0).sendToTarget();
        }
    };
    private RingerModeTracker mRingerModeTracker;
    private boolean mScreenOn;
    private Map<Integer, Intent> mSecondaryLockscreenRequirement = new HashMap();
    private boolean mSecureCameraLaunched;
    HashMap<Integer, ServiceState> mServiceStates = new HashMap<>();
    HashMap<Integer, SimData> mSimDatas = new HashMap<>();
    private final StatusBarStateController mStatusBarStateController;
    private StrongAuthTracker mStrongAuthTracker;
    private List<SubscriptionInfo> mSubscriptionInfo;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass5 */

        public void onSubscriptionsChanged() {
            KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(328);
        }
    };
    private SubscriptionManager mSubscriptionManager;
    private boolean mSwitchingUser;
    private final TaskStackChangeListener mTaskStackListener = new TaskStackChangeListener() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass18 */

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskStackChangedBackground() {
            try {
                ActivityManager.StackInfo stackInfo = ActivityTaskManager.getService().getStackInfo(0, 4);
                if (stackInfo != null) {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(335, Boolean.valueOf(stackInfo.visible)));
                }
            } catch (RemoteException e) {
                Log.e("KeyguardUpdateMonitor", "unable to check task stack", e);
            }
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskStackChanged() {
            try {
                List tasks = ActivityTaskManager.getService().getTasks(1);
                if (!tasks.isEmpty() && !TextUtils.isEmpty(((ActivityManager.RunningTaskInfo) tasks.get(0)).topActivity.getPackageName())) {
                    KeyguardUpdateMonitor.this.updateFingerprintListeningState();
                }
            } catch (RemoteException e) {
                Log.e("KeyguardUpdateMonitor", "am.getTasks fail " + e.getStackTrace());
                e.printStackTrace();
            }
        }
    };
    @VisibleForTesting
    protected boolean mTelephonyCapable;
    private TelephonyManager mTelephonyManager;
    private TrustManager mTrustManager;
    private Runnable mUpdateBiometricListeningState = new Runnable() {
        /* class com.android.keyguard.$$Lambda$KeyguardUpdateMonitor$w3Onnt26KGuFqBxQaSJgQd6Y_G4 */

        public final void run() {
            KeyguardUpdateMonitor.this.updateBiometricListeningState();
        }
    };
    private KeyguardUpdateMonitorInjector mUpdateMonitorInjector;
    SparseBooleanArray mUserBleAuthenticated = new SparseBooleanArray();
    @VisibleForTesting
    SparseArray<BiometricAuthenticated> mUserFaceAuthenticated = new SparseArray<>();
    private SparseBooleanArray mUserFaceUnlockRunning = new SparseBooleanArray();
    @VisibleForTesting
    SparseArray<BiometricAuthenticated> mUserFingerprintAuthenticated = new SparseArray<>();
    private SparseBooleanArray mUserHasTrust = new SparseBooleanArray();
    private SparseBooleanArray mUserIsUnlocked = new SparseBooleanArray();
    private UserManager mUserManager;
    private final UserSwitchObserver mUserSwitchObserver = new UserSwitchObserver() {
        /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass16 */

        public void onUserSwitching(int i, IRemoteCallback iRemoteCallback) {
            KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(310, i, 0, iRemoteCallback));
        }

        public void onUserSwitchComplete(int i) throws RemoteException {
            KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(314, i, 0));
        }
    };
    private SparseBooleanArray mUserTrustIsManaged = new SparseBooleanArray();
    private SparseBooleanArray mUserTrustIsUsuallyManaged = new SparseBooleanArray();

    private boolean containsFlag(int i, int i2) {
        return (i & i2) != 0;
    }

    public static boolean isSimPinSecure(int i) {
        return i == 2 || i == 3 || i == 7;
    }

    static {
        try {
            CORE_APPS_ONLY = IPackageManager.Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class BiometricAuthenticated {
        private final boolean mAuthenticated;
        private final boolean mIsStrongBiometric;

        BiometricAuthenticated(boolean z, boolean z2) {
            this.mAuthenticated = z;
            this.mIsStrongBiometric = z2;
        }
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

    public void onTrustChanged(boolean z, int i, int i2) {
        Assert.isMainThread();
        this.mUserHasTrust.put(i, z);
        for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTrustChanged(i);
                if (z && i2 != 0) {
                    keyguardUpdateMonitorCallback.onTrustGrantedWithFlags(i2, i);
                }
            }
        }
    }

    public void onTrustError(CharSequence charSequence) {
        dispatchErrorMessage(charSequence);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSimSubscriptionInfoChanged() {
        Assert.isMainThread();
        Log.v("KeyguardUpdateMonitor", "onSubscriptionInfoChanged()");
        List completeActiveSubscriptionInfoList = this.mSubscriptionManager.getCompleteActiveSubscriptionInfoList();
        if (completeActiveSubscriptionInfoList != null) {
            Iterator it = completeActiveSubscriptionInfoList.iterator();
            while (it.hasNext()) {
                Log.v("KeyguardUpdateMonitor", "SubInfo:" + ((SubscriptionInfo) it.next()));
            }
        } else {
            Log.v("KeyguardUpdateMonitor", "onSubscriptionInfoChanged: list is null");
        }
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(true);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < subscriptionInfo.size(); i++) {
            SubscriptionInfo subscriptionInfo2 = subscriptionInfo.get(i);
            if (refreshSimState(subscriptionInfo2.getSubscriptionId(), subscriptionInfo2.getSimSlotIndex())) {
                arrayList.add(subscriptionInfo2);
            }
        }
        for (int i2 = 0; i2 < arrayList.size(); i2++) {
            SimData simData = this.mSimDatas.get(Integer.valueOf(((SubscriptionInfo) arrayList.get(i2)).getSubscriptionId()));
            for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onSimStateChanged(simData.subId, simData.slotId, simData.simState);
                }
            }
        }
        callbacksRefreshCarrierInfo();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAirplaneModeChanged() {
        callbacksRefreshCarrierInfo();
    }

    private void callbacksRefreshCarrierInfo() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
            }
        }
    }

    public List<SubscriptionInfo> getSubscriptionInfo(boolean z) {
        List list = this.mSubscriptionInfo;
        if (list == null || z) {
            list = this.mSubscriptionManager.getCompleteActiveSubscriptionInfoList();
        }
        if (list == null) {
            this.mSubscriptionInfo = new ArrayList();
        } else {
            this.mSubscriptionInfo = list;
        }
        return new ArrayList(this.mSubscriptionInfo);
    }

    public List<SubscriptionInfo> getFilteredSubscriptionInfo(boolean z) {
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(false);
        if (subscriptionInfo.size() == 2) {
            SubscriptionInfo subscriptionInfo2 = subscriptionInfo.get(0);
            SubscriptionInfo subscriptionInfo3 = subscriptionInfo.get(1);
            if (subscriptionInfo2.getGroupUuid() == null || !subscriptionInfo2.getGroupUuid().equals(subscriptionInfo3.getGroupUuid()) || (!subscriptionInfo2.isOpportunistic() && !subscriptionInfo3.isOpportunistic())) {
                return subscriptionInfo;
            }
            if (CarrierConfigManager.getDefaultConfig().getBoolean("always_show_primary_signal_bar_in_opportunistic_network_boolean")) {
                if (!subscriptionInfo2.isOpportunistic()) {
                    subscriptionInfo2 = subscriptionInfo3;
                }
                subscriptionInfo.remove(subscriptionInfo2);
            } else {
                if (subscriptionInfo2.getSubscriptionId() == this.mActiveMobileDataSubscription) {
                    subscriptionInfo2 = subscriptionInfo3;
                }
                subscriptionInfo.remove(subscriptionInfo2);
            }
        }
        return subscriptionInfo;
    }

    public void onTrustManagedChanged(boolean z, int i) {
        Assert.isMainThread();
        this.mUserTrustIsManaged.put(i, z);
        this.mUserTrustIsUsuallyManaged.put(i, this.mTrustManager.isTrustUsuallyManaged(i));
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTrustManagedChanged(i);
            }
        }
    }

    public void setKeyguardGoingAway(boolean z) {
        this.mKeyguardGoingAway = z;
        updateBiometricListeningState();
        if (!((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock() || !MiuiKeyguardUtils.isTopActivityLauncher(this.mContext)) {
            ((MiuiWallpaperClient) Dependency.get(MiuiWallpaperClient.class)).onKeyguardGoingAway(z, ((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock());
        }
    }

    public void onCameraLaunched() {
        this.mSecureCameraLaunched = true;
        updateBiometricListeningState();
    }

    public boolean isDreaming() {
        return this.mIsDreaming;
    }

    public void awakenFromDream() {
        IDreamManager iDreamManager;
        if (this.mIsDreaming && (iDreamManager = this.mDreamManager) != null) {
            try {
                iDreamManager.awaken();
            } catch (RemoteException unused) {
                Log.e("KeyguardUpdateMonitor", "Unable to awaken from dream");
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void onFingerprintAuthenticated(int i, boolean z) {
        Assert.isMainThread();
        Trace.beginSection("KeyGuardUpdateMonitor#onFingerPrintAuthenticated");
        this.mUserFingerprintAuthenticated.put(i, new BiometricAuthenticated(true, z));
        if (getUserCanSkipBouncer(i)) {
            this.mTrustManager.unlockedByBiometricForUser(i, BiometricSourceType.FINGERPRINT);
        }
        this.mFingerprintCancelSignal = null;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAuthenticated(i, BiometricSourceType.FINGERPRINT, z);
            }
        }
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(336), 500);
        reportSuccessfulBiometricUnlock(z, i);
        Trace.endSection();
    }

    private void reportSuccessfulBiometricUnlock(final boolean z, final int i) {
        this.mBackgroundExecutor.execute(new Runnable() {
            /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass6 */

            public void run() {
                KeyguardUpdateMonitor.this.mLockPatternUtils.reportSuccessfulBiometricUnlock(z, i);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintAuthFailed() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAuthFailed(BiometricSourceType.FINGERPRINT);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintAcquired(int i) {
        Assert.isMainThread();
        if (i == 0) {
            for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onBiometricAcquired(BiometricSourceType.FINGERPRINT);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintAuthenticated(int i, boolean z) {
        Trace.beginSection("KeyGuardUpdateMonitor#handlerFingerPrintAuthenticated");
        try {
            int i2 = ActivityManager.getService().getCurrentUser().id;
            try {
                if (isFingerprintDisabled(i)) {
                    Log.d("KeyguardUpdateMonitor", "Fingerprint disabled by DPM for authUserId: " + i);
                    return;
                }
                onFingerprintAuthenticated(i, z);
                setFingerprintRunningState(0);
                Trace.endSection();
            } finally {
                setFingerprintRunningState(0);
            }
        } catch (RemoteException e) {
            Log.e("KeyguardUpdateMonitor", "Failed to get current user id: ", e);
            setFingerprintRunningState(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintHelp(int i, String str) {
        Assert.isMainThread();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricHelp(i, str, BiometricSourceType.FINGERPRINT);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintError(int i, String str) {
        int i2;
        Assert.isMainThread();
        if (i == 5 && this.mHandler.hasCallbacks(this.mCancelNotReceived)) {
            this.mHandler.removeCallbacks(this.mCancelNotReceived);
        }
        if (i == 5 && this.mFingerprintRunningState == 3) {
            setFingerprintRunningState(0);
            updateFingerprintListeningState();
        } else {
            setFingerprintRunningState(0);
            this.mFingerprintCancelSignal = null;
        }
        if (i == 1 && (i2 = this.mHardwareFingerprintUnavailableRetryCount) < 10) {
            this.mHardwareFingerprintUnavailableRetryCount = i2 + 1;
            this.mHandler.removeCallbacks(this.mRetryFingerprintAuthentication);
            this.mHandler.postDelayed(this.mRetryFingerprintAuthentication, 500);
        }
        if (i == 9) {
            this.mLockPatternUtils.requireStrongAuth(8, getCurrentUser());
        }
        if (i == 7 || i == 9) {
            this.mFingerprintLockedOut = true;
        }
        for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricError(i, str, BiometricSourceType.FINGERPRINT);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintLockoutReset() {
        this.mFingerprintLockedOut = false;
        updateFingerprintListeningState();
        this.mUpdateMonitorInjector.handleFingerprintLockoutReset();
    }

    private void setFingerprintRunningState(int i) {
        boolean z = false;
        boolean z2 = this.mFingerprintRunningState == 1;
        if (i == 1) {
            z = true;
        }
        this.mFingerprintRunningState = i;
        Log.d("KeyguardUpdateMonitor", "fingerprintRunningState: " + this.mFingerprintRunningState);
        if (z2 != z) {
            notifyFingerprintRunningStateChanged();
        }
    }

    private void notifyFingerprintRunningStateChanged() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricRunningStateChanged(isFingerprintDetectionRunning(), BiometricSourceType.FINGERPRINT);
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void onFaceAuthenticated(int i, boolean z) {
        Trace.beginSection("KeyGuardUpdateMonitor#onFaceAuthenticated");
        Assert.isMainThread();
        this.mUserFaceAuthenticated.put(i, new BiometricAuthenticated(true, z));
        if (getUserCanSkipBouncer(i)) {
            this.mTrustManager.unlockedByBiometricForUser(i, BiometricSourceType.FACE);
        }
        this.mFaceCancelSignal = null;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAuthenticated(i, BiometricSourceType.FACE, z);
            }
        }
        reportSuccessfulBiometricUnlock(z, i);
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceAuthFailed() {
        Assert.isMainThread();
        setFaceRunningState(0);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAuthFailed(BiometricSourceType.FACE);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceAcquired(int i) {
        Assert.isMainThread();
        if (i == 0) {
            if (DEBUG_FACE) {
                Log.d("KeyguardUpdateMonitor", "Face acquired");
            }
            for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onBiometricAcquired(BiometricSourceType.FACE);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceAuthenticated(int i, boolean z) {
        Trace.beginSection("KeyGuardUpdateMonitor#handlerFaceAuthenticated");
        try {
            if (this.mGoingToSleep) {
                Log.d("KeyguardUpdateMonitor", "Aborted successful auth because device is going to sleep.");
                return;
            }
            try {
                int i2 = ActivityManager.getService().getCurrentUser().id;
                if (i2 != i) {
                    Log.d("KeyguardUpdateMonitor", "Face authenticated for wrong user: " + i);
                    setFaceRunningState(0);
                } else if (isFaceDisabled(i2)) {
                    Log.d("KeyguardUpdateMonitor", "Face authentication disabled by DPM for userId: " + i2);
                    setFaceRunningState(0);
                } else {
                    if (DEBUG_FACE) {
                        Log.d("KeyguardUpdateMonitor", "Face auth succeeded for user " + i2);
                    }
                    onFaceAuthenticated(i2, z);
                    setFaceRunningState(0);
                    Trace.endSection();
                }
            } catch (RemoteException e) {
                Log.e("KeyguardUpdateMonitor", "Failed to get current user id: ", e);
                setFaceRunningState(0);
            }
        } finally {
            setFaceRunningState(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceHelp(int i, String str) {
        Assert.isMainThread();
        if (DEBUG_FACE) {
            Log.d("KeyguardUpdateMonitor", "Face help received: " + str);
        }
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricHelp(i, MiuiFaceUnlockUtils.getFaceHelpInfo(this.mContext, i), BiometricSourceType.FACE);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceError(int i, String str) {
        int i2;
        Assert.isMainThread();
        if (DEBUG_FACE) {
            Log.d("KeyguardUpdateMonitor", "Face error received: " + str);
        }
        if (i == 5 && this.mHandler.hasCallbacks(this.mCancelNotReceived)) {
            this.mHandler.removeCallbacks(this.mCancelNotReceived);
        }
        if (i == 5 && this.mFaceRunningState == 3) {
            setFaceRunningState(0);
            updateFaceListeningState();
        } else {
            setFaceRunningState(0);
            this.mFaceCancelSignal = null;
        }
        if ((i == 1 || i == 2) && (i2 = this.mHardwareFaceUnavailableRetryCount) < 10) {
            this.mHardwareFaceUnavailableRetryCount = i2 + 1;
            this.mHandler.removeCallbacks(this.mRetryFaceAuthentication);
            this.mHandler.postDelayed(this.mRetryFaceAuthentication, 500);
        }
        if (i == 9) {
            this.mLockPatternUtils.requireStrongAuth(8, getCurrentUser());
        }
        for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricError(i, str, BiometricSourceType.FACE);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceLockoutReset() {
        updateFaceListeningState();
    }

    private void setFaceRunningState(int i) {
        boolean z = false;
        boolean z2 = this.mFaceRunningState == 1;
        if (i == 1) {
            z = true;
        }
        this.mFaceRunningState = i;
        Log.d("KeyguardUpdateMonitor", "faceRunningState: " + this.mFaceRunningState);
        if (z2 != z) {
            notifyFaceRunningStateChanged();
        }
    }

    private void notifyFaceRunningStateChanged() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricRunningStateChanged(isFaceDetectionRunning(), BiometricSourceType.FACE);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceUnlockStateChanged(boolean z, int i) {
        Assert.isMainThread();
        this.mUserFaceUnlockRunning.put(i, z);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFaceUnlockStateChanged(z, i);
            }
        }
    }

    public boolean isFingerprintDetectionRunning() {
        return this.mFingerprintRunningState == 1;
    }

    public boolean isFaceDetectionRunning() {
        return this.mFaceRunningState == 1;
    }

    private boolean isTrustDisabled(int i) {
        return isSimPinSecure();
    }

    private boolean isFingerprintDisabled(int i) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        return !(devicePolicyManager == null || (devicePolicyManager.getKeyguardDisabledFeatures(null, i) & 32) == 0) || isSimPinSecure();
    }

    private boolean isFaceDisabled(int i) {
        return ((Boolean) DejankUtils.whitelistIpcs(new Supplier((DevicePolicyManager) this.mContext.getSystemService("device_policy"), i) {
            /* class com.android.keyguard.$$Lambda$KeyguardUpdateMonitor$N2Cyv6mYvgookTnpPTeaGdzNtxk */
            public final /* synthetic */ DevicePolicyManager f$1;
            public final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return KeyguardUpdateMonitor.this.lambda$isFaceDisabled$0$KeyguardUpdateMonitor(this.f$1, this.f$2);
            }
        })).booleanValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$isFaceDisabled$0 */
    public /* synthetic */ Boolean lambda$isFaceDisabled$0$KeyguardUpdateMonitor(DevicePolicyManager devicePolicyManager, int i) {
        return Boolean.valueOf(!(devicePolicyManager == null || (devicePolicyManager.getKeyguardDisabledFeatures(null, i) & 128) == 0) || isSimPinSecure());
    }

    public boolean getUserCanSkipBouncer(int i) {
        return getUserHasTrust(i) || getUserUnlockedWithBiometric(i) || getUserUnlockedWithBle(i);
    }

    public boolean getUserHasTrust(int i) {
        return !isTrustDisabled(i) && this.mUserHasTrust.get(i);
    }

    public boolean getUserUnlockedWithBiometric(int i) {
        BiometricAuthenticated biometricAuthenticated = this.mUserFingerprintAuthenticated.get(i);
        BiometricAuthenticated biometricAuthenticated2 = this.mUserFaceAuthenticated.get(i);
        return !isTrustDisabled(i) && ((biometricAuthenticated != null && biometricAuthenticated.mAuthenticated && isUnlockingWithBiometricAllowed(biometricAuthenticated.mIsStrongBiometric)) || (biometricAuthenticated2 != null && biometricAuthenticated2.mAuthenticated && isUnlockingWithBiometricAllowed(biometricAuthenticated2.mIsStrongBiometric)));
    }

    public void putUserBleAuthenticated(int i, boolean z) {
        this.mUserBleAuthenticated.put(i, z);
    }

    public boolean getUserBleAuthenticated(int i) {
        return this.mUserBleAuthenticated.get(i);
    }

    public boolean getUserUnlockedWithBle(int i) {
        return !isTrustDisabled(i) && this.mUserBleAuthenticated.get(i);
    }

    public boolean getUserTrustIsManaged(int i) {
        return this.mUserTrustIsManaged.get(i) && !isTrustDisabled(i);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0089 A[LOOP:0: B:16:0x0089->B:21:0x00a4, LOOP_START, PHI: r3 
      PHI: (r3v1 int) = (r3v0 int), (r3v2 int) binds: [B:15:0x0087, B:21:0x00a4] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARNING: Removed duplicated region for block: B:25:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateSecondaryLockscreenRequirement(int r6) {
        /*
        // Method dump skipped, instructions count: 168
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardUpdateMonitor.updateSecondaryLockscreenRequirement(int):void");
    }

    public Intent getSecondaryLockscreenRequirement(int i) {
        return this.mSecondaryLockscreenRequirement.get(Integer.valueOf(i));
    }

    public boolean isTrustUsuallyManaged(int i) {
        Assert.isMainThread();
        return this.mUserTrustIsUsuallyManaged.get(i);
    }

    public boolean isUnlockingWithBiometricAllowed(boolean z) {
        return this.mStrongAuthTracker.isUnlockingWithBiometricAllowed(z);
    }

    public boolean isUserInLockdown(int i) {
        return containsFlag(this.mStrongAuthTracker.getStrongAuthForUser(i), 32);
    }

    public boolean userNeedsStrongAuth() {
        return this.mStrongAuthTracker.getStrongAuthForUser(getCurrentUser()) != 0;
    }

    public boolean needsSlowUnlockTransition() {
        return this.mNeedsSlowUnlockTransition;
    }

    public StrongAuthTracker getStrongAuthTracker() {
        return this.mStrongAuthTracker;
    }

    /* access modifiers changed from: private */
    public void notifyStrongAuthStateChanged(int i) {
        Assert.isMainThread();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStrongAuthStateChanged(i);
            }
        }
    }

    public boolean isScreenOn() {
        return this.mScreenOn;
    }

    private void dispatchErrorMessage(CharSequence charSequence) {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTrustAgentErrorMessage(charSequence);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setAssistantVisible(boolean z) {
        updateBiometricListeningState();
    }

    /* access modifiers changed from: private */
    public static class SimData {
        public int simState;
        public int slotId;
        public int subId;

        SimData(int i, int i2, int i3) {
            this.simState = i;
            this.slotId = i2;
            this.subId = i3;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:20:0x006b, code lost:
            if ("PERM_DISABLED".equals(r9) != false) goto L_0x0038;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x0091, code lost:
            if ("IMSI".equals(r0) == false) goto L_0x0094;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        static com.android.keyguard.KeyguardUpdateMonitor.SimData fromIntent(android.content.Intent r9) {
            /*
            // Method dump skipped, instructions count: 162
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardUpdateMonitor.SimData.fromIntent(android.content.Intent):com.android.keyguard.KeyguardUpdateMonitor$SimData");
        }

        public String toString() {
            return "SimData{state=" + this.simState + ",slotId=" + this.slotId + ",subId=" + this.subId + "}";
        }
    }

    public static class StrongAuthTracker extends LockPatternUtils.StrongAuthTracker {
        private final Consumer<Integer> mStrongAuthRequiredChangedCallback;

        public StrongAuthTracker(Context context, Consumer<Integer> consumer) {
            super(context);
            this.mStrongAuthRequiredChangedCallback = consumer;
        }

        public boolean isUnlockingWithBiometricAllowed(boolean z) {
            return isBiometricAllowedForUser(z, KeyguardUpdateMonitor.getCurrentUser());
        }

        public boolean hasUserAuthenticatedSinceBoot() {
            return hasUserAuthenticatedSinceBoot(KeyguardUpdateMonitor.getCurrentUser());
        }

        public boolean hasOwnerUserAuthenticatedSinceBoot() {
            return hasUserAuthenticatedSinceBoot(0);
        }

        public boolean hasUserAuthenticatedSinceBoot(int i) {
            return (getStrongAuthForUser(i) & 1) == 0;
        }

        public void onStrongAuthRequiredChanged(int i) {
            this.mStrongAuthRequiredChangedCallback.accept(Integer.valueOf(i));
        }
    }

    /* access modifiers changed from: protected */
    public void handleStartedWakingUp() {
        Trace.beginSection("KeyguardUpdateMonitor#handleStartedWakingUp");
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStartedWakingUp();
            }
        }
        Trace.endSection();
    }

    /* access modifiers changed from: protected */
    public void handleStartedWakingUpWithReason(String str) {
        if (this.mMiuiFaceUnlockManager.isWakeupByNotification()) {
            ((NotificationShadeWindowController) Dependency.get(NotificationShadeWindowController.class)).setUserActivityTime(6000);
        }
        updateBiometricListeningState();
        this.mMiuiFaceUnlockManager.setWakeupByNotification(false);
        this.mUpdateMonitorInjector.handleStartedWakingUpWithReason(str);
    }

    /* access modifiers changed from: protected */
    public void handleStartedGoingToSleep(int i) {
        Assert.isMainThread();
        clearBiometricRecognized();
        ((NotificationShadeWindowController) Dependency.get(NotificationShadeWindowController.class)).setUserActivityTime(10000);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStartedGoingToSleep(i);
            }
        }
        this.mGoingToSleep = true;
        updateBiometricListeningState();
    }

    /* access modifiers changed from: protected */
    public void handleFinishedGoingToSleep(int i) {
        Assert.isMainThread();
        this.mGoingToSleep = false;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFinishedGoingToSleep(i);
            }
        }
        updateBiometricListeningState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenTurnedOn() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onScreenTurnedOn();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenTurnedOff() {
        DejankUtils.startDetectingBlockingIpcs("KeyguardUpdateMonitor#handleScreenTurnedOff");
        Assert.isMainThread();
        this.mHardwareFingerprintUnavailableRetryCount = 0;
        this.mHardwareFaceUnavailableRetryCount = 0;
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onScreenTurnedOff();
            }
        }
        DejankUtils.stopDetectingBlockingIpcs("KeyguardUpdateMonitor#handleScreenTurnedOff");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDreamingStateChanged(int i) {
        Assert.isMainThread();
        boolean z = true;
        if (i != 1) {
            z = false;
        }
        this.mIsDreaming = z;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDreamingStateChanged(this.mIsDreaming);
            }
        }
        updateFingerprintListeningState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUserInfoChanged(int i) {
        Assert.isMainThread();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserInfoChanged(i);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUserUnlocked(int i) {
        Assert.isMainThread();
        this.mUserIsUnlocked.put(i, true);
        this.mNeedsSlowUnlockTransition = resolveNeedsSlowUnlockTransition();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserUnlocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUserStopped(int i) {
        Assert.isMainThread();
        this.mUserIsUnlocked.put(i, this.mUserManager.isUserUnlocked(i));
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void handleUserRemoved(int i) {
        Assert.isMainThread();
        this.mUserIsUnlocked.delete(i);
        this.mUserTrustIsUsuallyManaged.delete(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKeyguardGoingAway(boolean z) {
        Assert.isMainThread();
        setKeyguardGoingAway(z);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setStrongAuthTracker(StrongAuthTracker strongAuthTracker) {
        StrongAuthTracker strongAuthTracker2 = this.mStrongAuthTracker;
        if (strongAuthTracker2 != null) {
            this.mLockPatternUtils.unregisterStrongAuthTracker(strongAuthTracker2);
        }
        this.mStrongAuthTracker = strongAuthTracker;
        this.mLockPatternUtils.registerStrongAuthTracker(strongAuthTracker);
    }

    /* access modifiers changed from: private */
    public void registerRingerTracker() {
        this.mRingerModeTracker.getRingerMode().observeForever(this.mRingerModeObserver);
    }

    @VisibleForTesting
    protected KeyguardUpdateMonitor(Context context, Looper looper, BroadcastDispatcher broadcastDispatcher, DumpManager dumpManager, RingerModeTracker ringerModeTracker, Executor executor, StatusBarStateController statusBarStateController, LockPatternUtils lockPatternUtils) {
        this.mContext = context;
        this.mSubscriptionManager = SubscriptionManager.from(context);
        this.mDeviceProvisioned = isDeviceProvisionedInSettingsDb();
        this.mStrongAuthTracker = new StrongAuthTracker(context, new Consumer() {
            /* class com.android.keyguard.$$Lambda$KeyguardUpdateMonitor$GZaxeQabrHzh5b8rORPTQGQVD8 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                KeyguardUpdateMonitor.this.notifyStrongAuthStateChanged(((Integer) obj).intValue());
            }
        });
        this.mBackgroundExecutor = executor;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mRingerModeTracker = ringerModeTracker;
        this.mStatusBarStateController = statusBarStateController;
        this.mLockPatternUtils = lockPatternUtils;
        dumpManager.registerDumpable(KeyguardUpdateMonitor.class.getName(), this);
        this.mHandler = new Handler(looper) {
            /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass15 */

            public void handleMessage(Message message) {
                switch (message.what) {
                    case 301:
                        KeyguardUpdateMonitor.this.handleTimeUpdate();
                        return;
                    case 302:
                        KeyguardUpdateMonitor.this.handleBatteryUpdate((MiuiBatteryStatus) message.obj);
                        return;
                    case 303:
                    case 307:
                    case 311:
                    case 313:
                    case 315:
                    case 316:
                    case 323:
                    case 324:
                    case 325:
                    case 326:
                    default:
                        super.handleMessage(message);
                        return;
                    case 304:
                        KeyguardUpdateMonitor.this.handleSimStateChange(message.arg1, message.arg2, ((Integer) message.obj).intValue());
                        return;
                    case 305:
                        KeyguardUpdateMonitor.this.handleRingerModeChange(message.arg1);
                        return;
                    case 306:
                        KeyguardUpdateMonitor.this.handlePhoneStateChanged((String) message.obj);
                        return;
                    case 308:
                        KeyguardUpdateMonitor.this.handleDeviceProvisioned();
                        return;
                    case 309:
                        KeyguardUpdateMonitor.this.handleDevicePolicyManagerStateChanged(message.arg1);
                        return;
                    case 310:
                        KeyguardUpdateMonitor.this.handleUserSwitching(message.arg1, (IRemoteCallback) message.obj);
                        return;
                    case 312:
                        KeyguardUpdateMonitor.this.handleKeyguardReset();
                        return;
                    case 314:
                        KeyguardUpdateMonitor.this.handleUserSwitchComplete(message.arg1);
                        return;
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
                        KeyguardUpdateMonitor.this.handleUserUnlocked(message.arg1);
                        return;
                    case 335:
                        KeyguardUpdateMonitor.this.setAssistantVisible(((Boolean) message.obj).booleanValue());
                        return;
                    case 336:
                        KeyguardUpdateMonitor.this.updateFingerprintListeningState();
                        return;
                    case 337:
                        KeyguardUpdateMonitor.this.updateLogoutEnabled();
                        return;
                    case 338:
                        KeyguardUpdateMonitor.this.updateTelephonyCapable(((Boolean) message.obj).booleanValue());
                        return;
                    case 339:
                        KeyguardUpdateMonitor.this.handleTimeZoneUpdate((String) message.obj);
                        return;
                    case 340:
                        KeyguardUpdateMonitor.this.handleUserStopped(message.arg1);
                        return;
                    case 341:
                        KeyguardUpdateMonitor.this.handleUserRemoved(message.arg1);
                        return;
                    case 342:
                        KeyguardUpdateMonitor.this.handleKeyguardGoingAway(((Boolean) message.obj).booleanValue());
                        return;
                    case 343:
                        Trace.beginSection("KeyguardUpdateMonitor#handler MSG_STARTED_WAKING_UP");
                        KeyguardUpdateMonitor.this.handleStartedWakingUpWithReason((String) message.obj);
                        Trace.endSection();
                        return;
                }
            }
        };
        if (!this.mDeviceProvisioned) {
            watchForDeviceProvisioning();
        }
        this.mBatteryStatus = new MiuiBatteryStatus(1, 0, 0, 0, 0, -1, 1, -1);
        int i = Settings.Secure.getInt(context.getContentResolver(), "sim_lock_enable", 0);
        this.mUpdateMonitorInjector = (KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        if (i == 0) {
            intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
            intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        }
        intentFilter.addAction("android.intent.action.SERVICE_STATE");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        this.mBroadcastDispatcher.registerReceiverWithHandler(this.mBroadcastReceiver, intentFilter, this.mHandler);
        this.mBackgroundExecutor.execute(new Runnable() {
            /* class com.android.keyguard.$$Lambda$KeyguardUpdateMonitor$Nf7eL_mU0R406vfJ5bZtFnV99Q */

            public final void run() {
                KeyguardUpdateMonitor.this.lambda$new$1$KeyguardUpdateMonitor();
            }
        });
        this.mHandler.post(new Runnable() {
            /* class com.android.keyguard.$$Lambda$KeyguardUpdateMonitor$hhywwBjwxz_vxvVm2yL5SB8SzM */

            public final void run() {
                KeyguardUpdateMonitor.this.registerRingerTracker();
            }
        });
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.USER_INFO_CHANGED");
        intentFilter2.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        intentFilter2.addAction("com.android.facelock.FACE_UNLOCK_STARTED");
        intentFilter2.addAction("com.android.facelock.FACE_UNLOCK_STOPPED");
        intentFilter2.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        intentFilter2.addAction("android.intent.action.USER_UNLOCKED");
        intentFilter2.addAction("android.intent.action.USER_STOPPED");
        intentFilter2.addAction("android.intent.action.USER_REMOVED");
        intentFilter2.addAction("miui.intent.action.MIUI_REGION_CHANGED");
        this.mBroadcastDispatcher.registerReceiverWithHandler(this.mBroadcastAllReceiver, intentFilter2, this.mHandler, UserHandle.ALL);
        if (i == 0) {
            this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
        }
        try {
            ActivityManager.getService().registerUserSwitchObserver(this.mUserSwitchObserver, "KeyguardUpdateMonitor");
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
        this.mTrustManager = (TrustManager) context.getSystemService(TrustManager.class);
        setStrongAuthTracker(this.mStrongAuthTracker);
        this.mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.getService("dreams"));
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.fingerprint")) {
            this.mFpm = (FingerprintManager) context.getSystemService("fingerprint");
        }
        BaseMiuiFaceManager baseMiuiFaceManager = (BaseMiuiFaceManager) context.getSystemService("miui_face");
        this.mFaceManager = baseMiuiFaceManager;
        if (!(this.mFpm == null && baseMiuiFaceManager == null)) {
            BiometricManager biometricManager = (BiometricManager) context.getSystemService(BiometricManager.class);
            this.mBiometricManager = biometricManager;
            biometricManager.registerEnabledOnKeyguardCallback(this.mBiometricEnabledCallback);
        }
        this.mMiuiFaceUnlockManager = (MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class);
        updateFingerprintListeningState();
        FingerprintManager fingerprintManager = this.mFpm;
        if (fingerprintManager != null) {
            fingerprintManager.addLockoutResetCallback(this.mFingerprintLockoutResetCallback);
        }
        BaseMiuiFaceManager baseMiuiFaceManager2 = this.mFaceManager;
        if (baseMiuiFaceManager2 != null) {
            baseMiuiFaceManager2.addLockoutResetCallback(this.mFaceLockoutResetCallback);
        }
        this.mIsAutomotive = isAutomotive();
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
        UserManager userManager = (UserManager) context.getSystemService(UserManager.class);
        this.mUserManager = userManager;
        this.mIsPrimaryUser = userManager.isPrimaryUser();
        int currentUser = ActivityManager.getCurrentUser();
        this.mUserIsUnlocked.put(currentUser, this.mUserManager.isUserUnlocked(currentUser));
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(DevicePolicyManager.class);
        this.mDevicePolicyManager = devicePolicyManager;
        this.mLogoutEnabled = devicePolicyManager.isLogoutEnabled();
        updateSecondaryLockscreenRequirement(currentUser);
        for (UserInfo userInfo : this.mUserManager.getUsers()) {
            SparseBooleanArray sparseBooleanArray = this.mUserTrustIsUsuallyManaged;
            int i2 = userInfo.id;
            sparseBooleanArray.put(i2, this.mTrustManager.isTrustUsuallyManaged(i2));
        }
        updateAirplaneModeState();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mTelephonyManager = telephonyManager;
        if (telephonyManager != null && i == 0) {
            telephonyManager.listen(this.mPhoneStateListener, 4194304);
            for (int i3 = 0; i3 < this.mTelephonyManager.getActiveModemCount(); i3++) {
                int simState = this.mTelephonyManager.getSimState(i3);
                int[] subscriptionIds = this.mSubscriptionManager.getSubscriptionIds(i3);
                if (subscriptionIds != null) {
                    for (int i4 : subscriptionIds) {
                        this.mHandler.obtainMessage(304, i4, i3, Integer.valueOf(simState)).sendToTarget();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$KeyguardUpdateMonitor() {
        Intent registerReceiver;
        int defaultSubscriptionId = SubscriptionManager.getDefaultSubscriptionId();
        ServiceState serviceStateForSubscriber = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).getServiceStateForSubscriber(defaultSubscriptionId);
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(330, defaultSubscriptionId, 0, serviceStateForSubscriber));
        if (this.mBatteryStatus == null && (registerReceiver = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))) != null && this.mBatteryStatus == null) {
            this.mBroadcastReceiver.onReceive(this.mContext, registerReceiver);
        }
    }

    private void updateAirplaneModeState() {
        if (WirelessUtils.isAirplaneModeOn(this.mContext) && !this.mHandler.hasMessages(329)) {
            this.mHandler.sendEmptyMessage(329);
        }
    }

    /* access modifiers changed from: private */
    public void updateBiometricListeningState() {
        updateFingerprintListeningState();
        this.mMiuiFaceUnlockManager.updateFaceDetectTypeForCamera(0);
        updateFaceListeningState();
    }

    public void updateFingerprintListeningState() {
        if (!this.mHandler.hasMessages(336)) {
            this.mHandler.removeCallbacks(this.mRetryFingerprintAuthentication);
            boolean shouldListenForFingerprint = shouldListenForFingerprint();
            int i = this.mFingerprintRunningState;
            boolean z = true;
            if (!(i == 1 || i == 3)) {
                z = false;
            }
            if (z && !shouldListenForFingerprint) {
                stopListeningForFingerprint();
            } else if (!z && shouldListenForFingerprint) {
                startListeningForFingerprint();
            }
        }
    }

    public boolean isUserUnlocked(int i) {
        return this.mUserIsUnlocked.get(i);
    }

    public void onAuthInterruptDetected(boolean z) {
        Log.d("KeyguardUpdateMonitor", "onAuthInterruptDetected(" + z + ")");
        if (this.mAuthInterruptActive != z) {
            this.mAuthInterruptActive = z;
            updateFaceListeningState();
        }
    }

    public void requestFaceAuth() {
        requestFaceAuth(0);
    }

    public void requestFaceAuth(int i) {
        Log.d("miui_face", "requestFaceAuth()");
        this.mMiuiFaceUnlockManager.updateFaceDetectTypeForCamera(i);
        updateFaceListeningState();
    }

    public void cancelFaceAuth() {
        stopListeningForFace();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFaceListeningState() {
        if (!this.mHandler.hasMessages(336)) {
            this.mHandler.removeCallbacks(this.mRetryFaceAuthentication);
            if (isUnlockWithFacePossible(getCurrentUser())) {
                boolean shouldListenForFace = shouldListenForFace();
                if (this.mFaceRunningState == 1 && !shouldListenForFace) {
                    stopListeningForFace();
                } else if (this.mFaceRunningState != 1 && shouldListenForFace) {
                    startListeningForFace();
                }
            }
        }
    }

    public boolean shouldListenForFingerprint() {
        boolean z = (this.mKeyguardIsVisible || !this.mDeviceInteractive || ((this.mBouncer && !this.mKeyguardGoingAway) || ((this.mUpdateMonitorInjector.isKeyguardShowing() && this.mKeyguardOccluded) || this.mGoingToSleep))) && !this.mSwitchingUser && !isFingerprintDisabled(getCurrentUser()) && !this.mUpdateMonitorInjector.isFingerprintUnlock() && MiuiKeyguardUtils.isSystemProcess() && (!this.mUpdateMonitorInjector.isFaceUnlock() || !MiuiKeyguardUtils.isBroadSideFingerprint()) && !((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isSimLocked() && getCurrentUser() != UserSwitcherController.getMaintenanceModeId();
        if (MiuiKeyguardUtils.isGxzwSensor() && MiuiKeyguardUtils.isInvertColorsEnable(this.mContext)) {
            z = z && this.mDeviceInteractive;
        }
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            z = z && this.mMiuiFaceUnlockManager.getHorizontalMoveLeftProgress() == 0.0f;
        }
        if (!z || !this.mKeyguardOccluded || (this.mBouncer && !this.mKeyguardGoingAway)) {
            return z;
        }
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            return !this.mDeviceInteractive;
        }
        return !MiuiKeyguardUtils.isTopActivityMiPay(this.mContext) && !this.mUpdateMonitorInjector.getDisableFingerprintListenState();
    }

    public boolean shouldListenForFace() {
        int currentUser = getCurrentUser();
        int strongAuthForUser = this.mStrongAuthTracker.getStrongAuthForUser(currentUser);
        boolean z = false;
        boolean z2 = containsFlag(strongAuthForUser, 2) || containsFlag(strongAuthForUser, 32);
        boolean z3 = containsFlag(strongAuthForUser, 1) || containsFlag(strongAuthForUser, 16);
        KeyguardBypassController keyguardBypassController = this.mKeyguardBypassController;
        boolean z4 = keyguardBypassController != null && keyguardBypassController.canBypass();
        boolean z5 = !getUserCanSkipBouncer(currentUser) || z4;
        boolean z6 = (!z3 || (z4 && !this.mBouncer)) && !z2;
        boolean isLargeScreen = MiuiKeyguardUtils.isLargeScreen(this.mContext);
        if ((this.mUpdateMonitorInjector.isKeyguardShowing() && this.mDeviceInteractive && !this.mSwitchingUser && !this.mKeyguardGoingAway) && z5 && z6 && ((!this.mMiuiFaceUnlockManager.isWakeupByNotification() || this.mMiuiFaceUnlockManager.isFaceUnlockStartByNotificationScreenOn()) && !this.mUpdateMonitorInjector.isChargeAnimationShowing() && ((!this.mKeyguardOccluded || (this.mBouncer && !MiuiKeyguardUtils.isTopActivityCameraApp(this.mContext))) && !this.mMiuiFaceUnlockManager.isFaceTemporarilyLockout() && getCurrentUser() == 0 && !userNeedsStrongAuth() && !this.mUpdateMonitorInjector.isSimLocked() && !isSimPinSecure() && !isLargeScreen && this.mMiuiFaceUnlockManager.shouldStartFaceDetectForCamera() && this.mMiuiFaceUnlockManager.getHorizontalMoveLeftProgress() == 0.0f))) {
            z = true;
        }
        if (!z) {
            this.mMiuiFaceUnlockManager.printCannotListenFaceLog(z6, this.mKeyguardGoingAway);
        }
        return z;
    }

    public void onLockIconPressed() {
        int currentUser = getCurrentUser();
        this.mUserFaceAuthenticated.put(currentUser, null);
        updateFaceListeningState();
        this.mStrongAuthTracker.onStrongAuthRequiredChanged(currentUser);
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
                CancellationSignal cancellationSignal2 = new CancellationSignal();
                this.mFingerprintCancelSignal = cancellationSignal2;
                this.mFpm.authenticate(null, cancellationSignal2, 0, this.mFingerprintAuthenticationCallback, null, currentUser);
                setFingerprintRunningState(1);
            }
        }
    }

    private void startListeningForFace() {
        if (this.mFaceRunningState == 2) {
            setFaceRunningState(3);
            return;
        }
        Log.v("KeyguardUpdateMonitor", "startListeningForFace()");
        int currentUser = getCurrentUser();
        if (isUnlockWithFacePossible(currentUser)) {
            CancellationSignal cancellationSignal = this.mFaceCancelSignal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
            }
            this.mFaceCancelSignal = new CancellationSignal();
            Slog.v("miui_face", "start face detect");
            this.mFaceManager.authenticate((CryptoObject) null, this.mFaceCancelSignal, 0, this.mFaceAuthenticationCallback, (Handler) null, currentUser);
            setFaceRunningState(1);
            handleFaceHelp(10001, "");
            return;
        }
        this.mMiuiFaceUnlockManager.printUnlockWithFaceImPossibleLog();
    }

    public boolean isUnlockingWithBiometricsPossible(int i) {
        return isUnlockWithFacePossible(i) || isUnlockWithFingerprintPossible(i);
    }

    public boolean isUnlockWithFingerprintPossible(int i) {
        FingerprintManager fingerprintManager = this.mFpm;
        return fingerprintManager != null && fingerprintManager.isHardwareDetected() && !isFingerprintDisabled(i) && this.mFpm.getEnrolledFingerprints(i).size() > 0;
    }

    public boolean isUnlockWithFacePossible(int i) {
        return isFaceAuthEnabledForUser(i) && !isFaceDisabled(i);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$isFaceAuthEnabledForUser$2 */
    public /* synthetic */ Boolean lambda$isFaceAuthEnabledForUser$2$KeyguardUpdateMonitor(int i) {
        BaseMiuiFaceManager baseMiuiFaceManager = this.mFaceManager;
        return Boolean.valueOf(baseMiuiFaceManager != null && baseMiuiFaceManager.isHardwareDetected() && this.mFaceManager.isFaceFeatureEnabled() && this.mFaceManager.hasEnrolledTemplates(i) && this.mMiuiFaceUnlockManager.isFaceUnlockApplyForKeyguard() && this.mIsPrimaryUser);
    }

    public boolean isFaceAuthEnabledForUser(int i) {
        return ((Boolean) DejankUtils.whitelistIpcs(new Supplier(i) {
            /* class com.android.keyguard.$$Lambda$KeyguardUpdateMonitor$L_ayq_delmLzhYZyR4IglXGLtE */
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return KeyguardUpdateMonitor.this.lambda$isFaceAuthEnabledForUser$2$KeyguardUpdateMonitor(this.f$1);
            }
        })).booleanValue();
    }

    private void stopListeningForFingerprint() {
        Log.v("KeyguardUpdateMonitor", "stopListeningForFingerprint()");
        if (this.mFingerprintRunningState == 1) {
            CancellationSignal cancellationSignal = this.mFingerprintCancelSignal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
                this.mFingerprintCancelSignal = null;
                if (!this.mHandler.hasCallbacks(this.mCancelNotReceived)) {
                    this.mHandler.postDelayed(this.mCancelNotReceived, 3000);
                }
            }
            setFingerprintRunningState(2);
        }
        if (this.mFingerprintRunningState == 3) {
            setFingerprintRunningState(2);
        }
    }

    private void stopListeningForFace() {
        Log.v("KeyguardUpdateMonitor", "stopListeningForFace()");
        if (this.mFaceRunningState == 1) {
            CancellationSignal cancellationSignal = this.mFaceCancelSignal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
                this.mFaceCancelSignal = null;
                if (!this.mHandler.hasCallbacks(this.mCancelNotReceived)) {
                    this.mHandler.postDelayed(this.mCancelNotReceived, 3000);
                }
            }
            setFaceRunningState(2);
        }
        if (this.mFaceRunningState == 3) {
            setFaceRunningState(2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDeviceProvisionedInSettingsDb() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    private void watchForDeviceProvisioning() {
        this.mDeviceProvisionedObserver = new ContentObserver(this.mHandler) {
            /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass17 */

            public void onChange(boolean z) {
                super.onChange(z);
                KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.this;
                keyguardUpdateMonitor.mDeviceProvisioned = keyguardUpdateMonitor.isDeviceProvisionedInSettingsDb();
                if (KeyguardUpdateMonitor.this.mDeviceProvisioned) {
                    KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(308);
                }
                Log.d("KeyguardUpdateMonitor", "DEVICE_PROVISIONED state = " + KeyguardUpdateMonitor.this.mDeviceProvisioned);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, this.mDeviceProvisionedObserver);
        boolean isDeviceProvisionedInSettingsDb = isDeviceProvisionedInSettingsDb();
        if (isDeviceProvisionedInSettingsDb != this.mDeviceProvisioned) {
            this.mDeviceProvisioned = isDeviceProvisionedInSettingsDb;
            if (isDeviceProvisionedInSettingsDb) {
                this.mHandler.sendEmptyMessage(308);
            }
        }
    }

    public void setHasLockscreenWallpaper(boolean z) {
        Assert.isMainThread();
        if (z != this.mHasLockscreenWallpaper) {
            this.mHasLockscreenWallpaper = z;
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onHasLockscreenWallpaperChanged(z);
                }
            }
        }
    }

    public boolean hasLockscreenWallpaper() {
        return this.mHasLockscreenWallpaper;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDevicePolicyManagerStateChanged(int i) {
        Assert.isMainThread();
        updateFingerprintListeningState();
        updateSecondaryLockscreenRequirement(i);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDevicePolicyManagerStateChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void handleUserSwitching(int i, IRemoteCallback iRemoteCallback) {
        Assert.isMainThread();
        clearBiometricRecognized();
        this.mUserTrustIsUsuallyManaged.put(i, this.mTrustManager.isTrustUsuallyManaged(i));
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserSwitching(i);
            }
        }
        try {
            iRemoteCallback.sendResult((Bundle) null);
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUserSwitchComplete(int i) {
        Assert.isMainThread();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserSwitchComplete(i);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDeviceProvisioned() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDeviceProvisioned();
            }
        }
        if (this.mDeviceProvisionedObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mDeviceProvisionedObserver);
            this.mDeviceProvisionedObserver = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePhoneStateChanged(String str) {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "handlePhoneStateChanged(" + str + ")");
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(str)) {
            this.mPhoneState = 0;
        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(str)) {
            this.mPhoneState = 2;
        } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(str)) {
            this.mPhoneState = 1;
        }
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onPhoneStateChanged(this.mPhoneState);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRingerModeChange(int i) {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "handleRingerModeChange(" + i + ")");
        this.mRingMode = i;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRingerModeChanged(i);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTimeUpdate() {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "handleTimeUpdate");
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTimeChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTimeZoneUpdate(String str) {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "handleTimeZoneUpdate");
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTimeZoneChanged(TimeZone.getTimeZone(str));
                keyguardUpdateMonitorCallback.onTimeChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBatteryUpdate(MiuiBatteryStatus miuiBatteryStatus) {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "handleBatteryUpdate");
        boolean isBatteryUpdateInteresting = isBatteryUpdateInteresting(this.mBatteryStatus, miuiBatteryStatus);
        this.mBatteryStatus = miuiBatteryStatus;
        if (isBatteryUpdateInteresting) {
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onRefreshBatteryInfo(miuiBatteryStatus);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateTelephonyCapable(boolean z) {
        Assert.isMainThread();
        if (z != this.mTelephonyCapable) {
            this.mTelephonyCapable = z;
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onTelephonyCapable(this.mTelephonyCapable);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0079  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x009b  */
    @com.android.internal.annotations.VisibleForTesting
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleSimStateChange(int r7, int r8, int r9) {
        /*
        // Method dump skipped, instructions count: 178
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardUpdateMonitor.handleSimStateChange(int, int, int):void");
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void handleServiceStateChange(int i, ServiceState serviceState) {
        Log.d("KeyguardUpdateMonitor", "handleServiceStateChange(subId=" + i + ", serviceState=" + serviceState);
        if (!SubscriptionManager.isValidSubscriptionId(i)) {
            Log.w("KeyguardUpdateMonitor", "invalid subId in handleServiceStateChange()");
            return;
        }
        updateTelephonyCapable(true);
        this.mServiceStates.put(Integer.valueOf(i), serviceState);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
                keyguardUpdateMonitorCallback.onServiceStateChanged(i, serviceState);
            }
        }
    }

    public boolean isKeyguardVisible() {
        return this.mKeyguardIsVisible;
    }

    public void onKeyguardVisibilityChanged(boolean z) {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "onKeyguardVisibilityChanged(" + z + ")");
        this.mKeyguardIsVisible = z;
        if (z) {
            this.mSecureCameraLaunched = false;
        }
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardVisibilityChangedRaw(z);
            }
        }
        updateBiometricListeningState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKeyguardReset() {
        Log.d("KeyguardUpdateMonitor", "handleKeyguardReset");
        updateFingerprintListeningState();
        this.mNeedsSlowUnlockTransition = resolveNeedsSlowUnlockTransition();
    }

    private boolean resolveNeedsSlowUnlockTransition() {
        if (isUserUnlocked(getCurrentUser())) {
            return false;
        }
        ResolveInfo resolveActivity = this.mContext.getPackageManager().resolveActivity(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME"), 0);
        if (resolveActivity != null) {
            return FALLBACK_HOME_COMPONENT.equals(resolveActivity.getComponentInfo().getComponentName());
        }
        Log.w("KeyguardUpdateMonitor", "resolveNeedsSlowUnlockTransition: returning false since activity could not be resolved.");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKeyguardBouncerChanged(int i) {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "handleKeyguardBouncerChanged(" + i + ")");
        boolean z = true;
        if (i != 1) {
            z = false;
        }
        this.mBouncer = z;
        if (z) {
            this.mSecureCameraLaunched = false;
        }
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardBouncerChanged(z);
            }
        }
        updateFingerprintListeningState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReportEmergencyCallAction() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onEmergencyCallAction();
            }
        }
    }

    private boolean isBatteryUpdateInteresting(MiuiBatteryStatus miuiBatteryStatus, MiuiBatteryStatus miuiBatteryStatus2) {
        boolean isPluggedIn = miuiBatteryStatus2.isPluggedIn();
        boolean isPluggedIn2 = miuiBatteryStatus.isPluggedIn();
        boolean z = isPluggedIn2 && isPluggedIn && miuiBatteryStatus.status != miuiBatteryStatus2.status;
        if (isPluggedIn2 == isPluggedIn && !z && miuiBatteryStatus.level == miuiBatteryStatus2.level) {
            return (isPluggedIn && miuiBatteryStatus2.maxChargingWattage != miuiBatteryStatus.maxChargingWattage) || miuiBatteryStatus.chargeDeviceType != miuiBatteryStatus2.chargeDeviceType;
        }
        return true;
    }

    private boolean isAutomotive() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.automotive");
    }

    public void removeCallback(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        Assert.isMainThread();
        Log.v("KeyguardUpdateMonitor", "*** unregister callback for " + keyguardUpdateMonitorCallback);
        this.mCallbacks.removeIf(new Predicate() {
            /* class com.android.keyguard.$$Lambda$KeyguardUpdateMonitor$BtuCi3IGxUtdSY1jcMIdW6mFmhs */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return KeyguardUpdateMonitor.lambda$removeCallback$3(KeyguardUpdateMonitorCallback.this, (WeakReference) obj);
            }
        });
    }

    static /* synthetic */ boolean lambda$removeCallback$3(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback, WeakReference weakReference) {
        return weakReference.get() == keyguardUpdateMonitorCallback;
    }

    public void registerCallback(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        Assert.isMainThread();
        Log.v("KeyguardUpdateMonitor", "*** register callback for " + keyguardUpdateMonitorCallback);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            if (this.mCallbacks.get(i).get() == keyguardUpdateMonitorCallback) {
                Log.e("KeyguardUpdateMonitor", "Object tried to add another callback", new Exception("Called by"));
                return;
            }
        }
        this.mCallbacks.add(new WeakReference<>(keyguardUpdateMonitorCallback));
        removeCallback(null);
        sendUpdates(keyguardUpdateMonitorCallback);
    }

    public void setKeyguardBypassController(KeyguardBypassController keyguardBypassController) {
        this.mKeyguardBypassController = keyguardBypassController;
    }

    public boolean isSwitchingUser() {
        return this.mSwitchingUser;
    }

    public void setSwitchingUser(boolean z) {
        this.mSwitchingUser = z;
        this.mHandler.post(this.mUpdateBiometricListeningState);
    }

    private void sendUpdates(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        keyguardUpdateMonitorCallback.onRefreshBatteryInfo(this.mBatteryStatus);
        keyguardUpdateMonitorCallback.onTimeChanged();
        keyguardUpdateMonitorCallback.onRingerModeChanged(this.mRingMode);
        keyguardUpdateMonitorCallback.onPhoneStateChanged(this.mPhoneState);
        keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
        keyguardUpdateMonitorCallback.onClockVisibilityChanged();
        keyguardUpdateMonitorCallback.onKeyguardVisibilityChangedRaw(this.mKeyguardIsVisible);
        keyguardUpdateMonitorCallback.onTelephonyCapable(this.mTelephonyCapable);
        for (Map.Entry<Integer, SimData> entry : this.mSimDatas.entrySet()) {
            SimData value = entry.getValue();
            keyguardUpdateMonitorCallback.onSimStateChanged(value.subId, value.slotId, value.simState);
        }
        this.mUpdateMonitorInjector.sendUpdates(keyguardUpdateMonitorCallback, this.mKeyguardOccluded);
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
        handleSimStateChange(i, getSlotId(i), 5);
    }

    public void reportEmergencyCallAction(boolean z) {
        if (!z) {
            this.mHandler.obtainMessage(318).sendToTarget();
            return;
        }
        Assert.isMainThread();
        handleReportEmergencyCallAction();
    }

    public boolean isDeviceProvisioned() {
        return this.mDeviceProvisioned;
    }

    public ServiceState getServiceState(int i) {
        return this.mServiceStates.get(Integer.valueOf(i));
    }

    public void clearBiometricRecognized() {
        Assert.isMainThread();
        this.mUserFingerprintAuthenticated.clear();
        this.mUserFaceAuthenticated.clear();
        this.mUserBleAuthenticated.clear();
        this.mTrustManager.clearAllBiometricRecognized(BiometricSourceType.FINGERPRINT);
        this.mTrustManager.clearAllBiometricRecognized(BiometricSourceType.FACE);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricsCleared();
            }
        }
    }

    public boolean isSimPinVoiceSecure() {
        return isSimPinSecure();
    }

    public boolean isSimPinSecure() {
        for (SubscriptionInfo subscriptionInfo : getSubscriptionInfo(false)) {
            if (isSimPinSecure(getSimState(subscriptionInfo.getSubscriptionId()))) {
                return true;
            }
        }
        return false;
    }

    public int getSimState(int i) {
        if (this.mSimDatas.containsKey(Integer.valueOf(i))) {
            return this.mSimDatas.get(Integer.valueOf(i)).simState;
        }
        return 0;
    }

    private int getSlotId(int i) {
        if (!this.mSimDatas.containsKey(Integer.valueOf(i))) {
            refreshSimState(i, SubscriptionManager.getSlotIndex(i));
        }
        return this.mSimDatas.get(Integer.valueOf(i)).slotId;
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

    private boolean refreshSimState(int i, int i2) {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        boolean z = false;
        int simState = telephonyManager != null ? telephonyManager.getSimState(i2) : 0;
        SimData simData = this.mSimDatas.get(Integer.valueOf(i));
        if (simData == null) {
            this.mSimDatas.put(Integer.valueOf(i), new SimData(simState, i2, i));
            return true;
        }
        if (!(simData.simState == simState && simData.slotId == i2)) {
            z = true;
        }
        simData.simState = simState;
        simData.slotId = i2;
        return z;
    }

    public void dispatchStartedWakingUp() {
        synchronized (this) {
            this.mDeviceInteractive = true;
        }
        this.mHandler.sendEmptyMessage(319);
    }

    public void dispatchStartedWakingUpWithReason(String str) {
        synchronized (this) {
            this.mDeviceInteractive = true;
        }
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(343, str));
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

    public void dispatchKeyguardGoingAway(boolean z) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(342, Boolean.valueOf(z)));
    }

    public boolean isDeviceInteractive() {
        return this.mDeviceInteractive;
    }

    public int getNextSubIdForState(int i) {
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(false);
        int i2 = -1;
        int i3 = Integer.MAX_VALUE;
        for (int i4 = 0; i4 < subscriptionInfo.size(); i4++) {
            int subscriptionId = subscriptionInfo.get(i4).getSubscriptionId();
            int slotId = getSlotId(subscriptionId);
            if (i == getSimState(subscriptionId) && i3 > slotId) {
                i2 = subscriptionId;
                i3 = slotId;
            }
        }
        return i2;
    }

    public int getUnlockedSubIdForState(int i) {
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(false);
        for (int i2 = 0; i2 < subscriptionInfo.size(); i2++) {
            int subscriptionId = subscriptionInfo.get(i2).getSubscriptionId();
            int slotIndex = SubscriptionManager.getSlotIndex(subscriptionId);
            if (i == getSimState(subscriptionId) && KeyguardViewMediator.getUnlockTrackSimState(slotIndex) != 5) {
                return subscriptionId;
            }
        }
        return -1;
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

    public boolean isLogoutEnabled() {
        return this.mLogoutEnabled;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLogoutEnabled() {
        Assert.isMainThread();
        boolean isLogoutEnabled = this.mDevicePolicyManager.isLogoutEnabled();
        if (this.mLogoutEnabled != isLogoutEnabled) {
            this.mLogoutEnabled = isLogoutEnabled;
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onLogoutEnabledChanged();
                }
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardUpdateMonitor state:");
        printWriter.println("  SIM States:");
        Iterator<SimData> it = this.mSimDatas.values().iterator();
        while (it.hasNext()) {
            printWriter.println("    " + it.next().toString());
        }
        printWriter.println("  Subs:");
        if (this.mSubscriptionInfo != null) {
            for (int i = 0; i < this.mSubscriptionInfo.size(); i++) {
                printWriter.println("    " + this.mSubscriptionInfo.get(i));
            }
        }
        printWriter.println("  Current active data subId=" + this.mActiveMobileDataSubscription);
        printWriter.println("  Service states:");
        for (Integer num : this.mServiceStates.keySet()) {
            int intValue = num.intValue();
            printWriter.println("    " + intValue + "=" + this.mServiceStates.get(Integer.valueOf(intValue)));
        }
        FingerprintManager fingerprintManager = this.mFpm;
        if (fingerprintManager != null && fingerprintManager.isHardwareDetected()) {
            int currentUser = ActivityManager.getCurrentUser();
            int strongAuthForUser = this.mStrongAuthTracker.getStrongAuthForUser(currentUser);
            BiometricAuthenticated biometricAuthenticated = this.mUserFingerprintAuthenticated.get(currentUser);
            printWriter.println("  Fingerprint state (user=" + currentUser + ")");
            StringBuilder sb = new StringBuilder();
            sb.append("    allowed=");
            sb.append(biometricAuthenticated != null && isUnlockingWithBiometricAllowed(biometricAuthenticated.mIsStrongBiometric));
            printWriter.println(sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("    auth'd=");
            sb2.append(biometricAuthenticated != null && biometricAuthenticated.mAuthenticated);
            printWriter.println(sb2.toString());
            printWriter.println("    authSinceBoot=" + getStrongAuthTracker().hasUserAuthenticatedSinceBoot());
            printWriter.println("    disabled(DPM)=" + isFingerprintDisabled(currentUser));
            printWriter.println("    possible=" + isUnlockWithFingerprintPossible(currentUser));
            printWriter.println("    listening: actual=" + this.mFingerprintRunningState + " expected=" + (shouldListenForFingerprint() ? 1 : 0));
            StringBuilder sb3 = new StringBuilder();
            sb3.append("    strongAuthFlags=");
            sb3.append(Integer.toHexString(strongAuthForUser));
            printWriter.println(sb3.toString());
            printWriter.println("    trustManaged=" + getUserTrustIsManaged(currentUser));
        }
        BaseMiuiFaceManager baseMiuiFaceManager = this.mFaceManager;
        if (baseMiuiFaceManager != null && baseMiuiFaceManager.isHardwareDetected()) {
            int currentUser2 = ActivityManager.getCurrentUser();
            int strongAuthForUser2 = this.mStrongAuthTracker.getStrongAuthForUser(currentUser2);
            BiometricAuthenticated biometricAuthenticated2 = this.mUserFaceAuthenticated.get(currentUser2);
            printWriter.println("  Face authentication state (user=" + currentUser2 + ")");
            StringBuilder sb4 = new StringBuilder();
            sb4.append("    allowed=");
            sb4.append(biometricAuthenticated2 != null && isUnlockingWithBiometricAllowed(biometricAuthenticated2.mIsStrongBiometric));
            printWriter.println(sb4.toString());
            StringBuilder sb5 = new StringBuilder();
            sb5.append("    auth'd=");
            sb5.append(biometricAuthenticated2 != null && biometricAuthenticated2.mAuthenticated);
            printWriter.println(sb5.toString());
            printWriter.println("    authSinceBoot=" + getStrongAuthTracker().hasUserAuthenticatedSinceBoot());
            printWriter.println("    disabled(DPM)=" + isFaceDisabled(currentUser2));
            printWriter.println("    possible=" + isUnlockWithFacePossible(currentUser2));
            printWriter.println("    strongAuthFlags=" + Integer.toHexString(strongAuthForUser2));
            printWriter.println("    trustManaged=" + getUserTrustIsManaged(currentUser2));
            printWriter.println("    enabledByUser=" + this.mFaceSettingEnabledForUser.get(currentUser2));
            printWriter.println("    mSecureCameraLaunched=" + this.mSecureCameraLaunched);
        }
        ArrayDeque<KeyguardFaceListenModel> arrayDeque = this.mFaceListenModels;
        if (arrayDeque != null && !arrayDeque.isEmpty()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            printWriter.println("  Face listen results (last 20 calls):");
            Iterator<KeyguardFaceListenModel> it2 = this.mFaceListenModels.iterator();
            while (it2.hasNext()) {
                KeyguardFaceListenModel next = it2.next();
                String format = simpleDateFormat.format(new Date(next.getTimeMillis()));
                printWriter.println("    " + format + " " + next.toString());
            }
        }
        if (this.mIsAutomotive) {
            printWriter.println("  Running on Automotive build");
        }
    }

    public int getPhoneState() {
        return this.mPhoneState;
    }

    public void setKeyguardShowingAndOccluded(boolean z, boolean z2) {
        if (z2 != this.mKeyguardOccluded) {
            this.mKeyguardOccluded = z2;
        }
        this.mUpdateMonitorInjector.setKeyguardShowingAndOccluded(this.mTaskStackListener, z, z2, new MiuiKeyguardUpdateMonitorCallback(this) {
            /* class com.android.keyguard.KeyguardUpdateMonitor.AnonymousClass19 */
        });
    }

    public boolean isBouncerShowing() {
        return this.mBouncer;
    }

    public void onBatteryStatusChange(MiuiBatteryStatus miuiBatteryStatus) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(302, miuiBatteryStatus));
    }

    public int getStatusBarState() {
        return this.mStatusBarStateController.getState();
    }

    public ArrayList<WeakReference<KeyguardUpdateMonitorCallback>> getCallbacks() {
        return this.mCallbacks;
    }

    public boolean isFingerprintTemporarilyLockout() {
        return this.mFingerprintLockedOut;
    }

    public void handleReeFaceLockout() {
        this.mFaceAuthenticationCallback.onAuthenticationError(7, (CharSequence) null);
    }
}
