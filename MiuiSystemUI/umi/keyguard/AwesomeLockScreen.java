package com.android.keyguard;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.biometrics.BiometricSourceType;
import android.media.AudioManager;
import android.provider.Settings;
import android.security.MiuiLockPatternUtils;
import android.util.Log;
import android.util.Slog;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.AwesomeLockScreenImp.AwesomeLockScreenView;
import com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot;
import com.android.keyguard.HeiHeiGestureView;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.charge.ChargeUtils;
import com.android.keyguard.charge.MiuiBatteryStatus;
import com.android.keyguard.faceunlock.FaceUnlockCallback;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.faceunlock.MiuiFaceUnlockUtils;
import com.android.keyguard.fod.MiuiGxzwCallback;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.PanelBar;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import miui.maml.data.Variables;
import miui.maml.util.Utils;

public class AwesomeLockScreen extends FrameLayout implements LockScreenRoot.LockscreenCallback {
    /* access modifiers changed from: private */
    public static RootHolder mRootHolder = new RootHolder();
    private static int mThemeChanged;
    private static long sStartTime;
    static boolean sSuppressNextLockSound;
    private static long sTotalWakenTime;
    private AudioManager mAudioManager;
    private FaceUnlockCallback mFaceUnlockCallback;
    /* access modifiers changed from: private */
    public boolean mInitSuccessful;
    private boolean mIsFocus;
    private boolean mIsInteractive;
    private boolean mIsPaused;
    /* access modifiers changed from: private */
    public boolean mKeyguardBouncerShowing;
    private LockPatternUtils mLockPatternUtils;
    private AwesomeLockScreenView mLockscreenView;
    private MiuiGxzwCallback mMiuiGxzwCallback;
    private NotificationPanelViewController mPanelViewController;
    private int mPasswordMode;
    private StatusBar mStatusBar;
    private StatusBarStateController mStatusBarStateController;
    private KeyguardUpdateMonitor mUpdateMonitor;
    MiuiKeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private long mWakeStartTime;

    private void updateStatusBarColormode() {
    }

    public boolean isSecure() {
        return false;
    }

    public AwesomeLockScreen(Context context, StatusBar statusBar, StatusBarStateController statusBarStateController, NotificationPanelViewController notificationPanelViewController, PanelBar panelBar, KeyguardStateController keyguardStateController) {
        this(context);
        this.mStatusBar = statusBar;
        this.mStatusBarStateController = statusBarStateController;
        this.mPanelViewController = notificationPanelViewController;
        AwesomeLockScreenView awesomeLockScreenView = this.mLockscreenView;
        if (awesomeLockScreenView != null) {
            awesomeLockScreenView.setPanelView(notificationPanelViewController);
        }
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
    }

    /* JADX WARNING: type inference failed for: r3v10, types: [com.android.keyguard.AwesomeLockScreenImp.AwesomeLockScreenView, android.view.View] */
    AwesomeLockScreen(Context context) {
        super(context);
        int i = 0;
        this.mIsPaused = false;
        this.mIsFocus = true;
        this.mKeyguardBouncerShowing = false;
        this.mUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
            public void onRefreshBatteryInfo(MiuiBatteryStatus miuiBatteryStatus) {
                super.onRefreshBatteryInfo(miuiBatteryStatus);
                Log.d("AwesomeLockScreen", "onRefreshBatteryInfo: isBatteryLow = " + miuiBatteryStatus.isBatteryLow() + " isPluggedIn = " + miuiBatteryStatus.isPluggedIn() + " level = " + miuiBatteryStatus.getLevel());
                if (AwesomeLockScreen.this.mInitSuccessful) {
                    AwesomeLockScreen.mRootHolder.getRoot().onRefreshBatteryInfo(miuiBatteryStatus);
                }
            }

            public void onKeyguardBouncerChanged(boolean z) {
                boolean unused = AwesomeLockScreen.this.mKeyguardBouncerShowing = z;
                AwesomeLockScreen.this.updatePauseResumeStatus();
            }

            public void onBiometricHelp(int i, String str, BiometricSourceType biometricSourceType) {
                if (biometricSourceType != BiometricSourceType.FACE) {
                    return;
                }
                if (i == 3) {
                    Log.i("AwesomeLockScreen", "onFaceAuthTimeOut");
                    Utils.putVariableNumber("face_detect_state_msg", AwesomeLockScreen.mRootHolder.getContext().mVariables, 3.0d);
                    return;
                }
                Log.i("AwesomeLockScreen", "onFaceAuthHelp");
                Utils.putVariableNumber("face_detect_help_msg", AwesomeLockScreen.mRootHolder.getContext().mVariables, (double) i);
            }

            public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
                if (biometricSourceType == BiometricSourceType.FACE) {
                    Log.i("AwesomeLockScreen", "onFaceAuthenticated");
                    Utils.putVariableNumber("face_detect_state_msg", AwesomeLockScreen.mRootHolder.getContext().mVariables, 2.0d);
                }
            }

            public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
                if (biometricSourceType == BiometricSourceType.FACE) {
                    Log.i("AwesomeLockScreen", "onFaceAuthFailed");
                    Utils.putVariableNumber("face_detect_state_msg", AwesomeLockScreen.mRootHolder.getContext().mVariables, 4.0d);
                }
            }
        };
        this.mFaceUnlockCallback = new FaceUnlockCallback(this) {
            public void onFaceAuthLocked() {
                Log.i("AwesomeLockScreen", "onFaceAuthLocked");
                Utils.putVariableNumber("face_detect_state_msg", AwesomeLockScreen.mRootHolder.getContext().mVariables, 5.0d);
            }

            public void onFaceEnableChange(boolean z, boolean z2) {
                Log.i("AwesomeLockScreen", "onFaceEnableChange enable=" + z + ";stay=" + z2);
                double d = 1.0d;
                Utils.putVariableNumber("face_enable", AwesomeLockScreen.mRootHolder.getContext().mVariables, z ? 1.0d : 0.0d);
                Variables variables = AwesomeLockScreen.mRootHolder.getContext().mVariables;
                if (!z2) {
                    d = 0.0d;
                }
                Utils.putVariableNumber("face_unlock_success_stay_screen_enable", variables, d);
            }
        };
        this.mMiuiGxzwCallback = new MiuiGxzwCallback() {
            public void onGxzwEnableChange(boolean z) {
                Log.i("AwesomeLockScreen", "onGxzwEnableChange: enable = " + z);
                Utils.putVariableNumber("fod_enable", AwesomeLockScreen.mRootHolder.getContext().mVariables, z ? 1.0d : 0.0d);
                if (z) {
                    Rect fodPosition = MiuiGxzwManager.getFodPosition(AwesomeLockScreen.this.getContext());
                    Utils.putVariableNumber("fod_x", AwesomeLockScreen.mRootHolder.getContext().mVariables, (double) fodPosition.left);
                    Utils.putVariableNumber("fod_y", AwesomeLockScreen.mRootHolder.getContext().mVariables, (double) fodPosition.top);
                    Utils.putVariableNumber("fod_width", AwesomeLockScreen.mRootHolder.getContext().mVariables, (double) fodPosition.width());
                    Utils.putVariableNumber("fod_height", AwesomeLockScreen.mRootHolder.getContext().mVariables, (double) fodPosition.height());
                }
            }

            public void onGxzwTouchDown() {
                Log.i("AwesomeLockScreen", "onGxzwTouchDown");
                Utils.putVariableNumber("fod_state_msg", AwesomeLockScreen.mRootHolder.getContext().mVariables, 1.0d);
            }

            public void onGxzwTouchUp() {
                Log.i("AwesomeLockScreen", "onGxzwTouchUp");
                Utils.putVariableNumber("fod_state_msg", AwesomeLockScreen.mRootHolder.getContext().mVariables, 2.0d);
            }

            public void onGxzwAuthFailed() {
                Log.i("AwesomeLockScreen", "onGxzwAuthFailed");
                Utils.putVariableNumber("fod_state_msg", AwesomeLockScreen.mRootHolder.getContext().mVariables, 3.0d);
            }

            public void onGxzwAuthSucceeded() {
                Log.i("AwesomeLockScreen", "onGxzwAuthSucceeded");
                Utils.putVariableNumber("fod_state_msg", AwesomeLockScreen.mRootHolder.getContext().mVariables, 4.0d);
            }
        };
        this.mLockPatternUtils = new LockPatternUtils(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        int i2 = context.getResources().getConfiguration().extraConfig.themeChanged;
        if (i2 > mThemeChanged) {
            clearCache();
            mThemeChanged = i2;
        }
        if (!mRootHolder.init(this.mContext, this)) {
            AnalyticsHelper.getInstance(this.mContext).record("awesome_lockscreen_init_failed");
            Slog.e("AwesomeLockScreen", "fail to init RootHolder");
            return;
        }
        Utils.putVariableString("owner_info", mRootHolder.getContext().mVariables, this.mLockPatternUtils.isOwnerInfoEnabled(KeyguardUpdateMonitor.getCurrentUser()) ? new MiuiLockPatternUtils(context).getOwnerInfo() : null);
        HeiHeiGestureView heiHeiGestureView = new HeiHeiGestureView(this.mContext);
        heiHeiGestureView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        heiHeiGestureView.setOnTriggerListener(new HeiHeiGestureView.OnTriggerListener() {
            public void onTrigger() {
                AwesomeLockScreen.sSuppressNextLockSound = true;
                AwesomeLockScreen.this.collapsePanel();
            }
        });
        addView(heiHeiGestureView);
        this.mPasswordMode = getPasswordMode();
        mRootHolder.getContext().mVariables.put("__password_mode", (double) this.mPasswordMode);
        mRootHolder.getRoot().setLockscreenCallback(this);
        AwesomeLockScreenView createView = mRootHolder.createView(this.mContext);
        this.mLockscreenView = createView;
        if (createView != null) {
            heiHeiGestureView.addView(this.mLockscreenView, new FrameLayout.LayoutParams(-1, -1));
            this.mInitSuccessful = true;
        }
        if (sStartTime == 0) {
            sStartTime = System.currentTimeMillis() / 1000;
        }
        this.mWakeStartTime = System.currentTimeMillis() / 1000;
        updatePauseResumeStatus();
        mRootHolder.getRoot().setBgColor(this.mPasswordMode != 0 ? -16777216 : i);
    }

    public void setIsInteractive(boolean z) {
        this.mIsInteractive = z;
        updatePauseResumeStatus();
    }

    public void rebindView() {
        if (this.mInitSuccessful) {
            mRootHolder.getRoot().setLockscreenCallback(this);
            this.mLockscreenView.rebindRoot();
        }
    }

    public static void clearCache() {
        mRootHolder.clear();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mUpdateMonitor.registerCallback(this.mUpdateMonitorCallback);
        updateStatusBarColormode();
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().registerCallback(this.mMiuiGxzwCallback);
        }
        ((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).registerFaceUnlockCallback(this.mFaceUnlockCallback);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        this.mUpdateMonitor.removeCallback(this.mUpdateMonitorCallback);
        cleanUp();
        disableLockScreenFodAnim(false);
        disableLockScreenFod(false);
        disableLockScreenFaceUnlockAnim(false);
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().removeCallback(this.mMiuiGxzwCallback);
        }
        ((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).removeFaceUnlockCallback(this.mFaceUnlockCallback);
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public void updatePauseResumeStatus() {
        if (this.mInitSuccessful) {
            if (!this.mIsFocus || !this.mIsInteractive || (!(this.mStatusBarStateController.getState() == 1 || this.mStatusBarStateController.getState() == 2) || this.mKeyguardBouncerShowing)) {
                onPause();
            } else {
                onResume(false);
            }
        }
    }

    private void onPause() {
        if (!this.mIsPaused) {
            Log.d("AwesomeLockScreen", "onPause");
            this.mIsPaused = true;
            this.mLockscreenView.pause();
            mRootHolder.getRoot().onCommand("pause");
            sTotalWakenTime += (System.currentTimeMillis() / 1000) - this.mWakeStartTime;
        }
    }

    private void onResume(boolean z) {
        if (this.mIsPaused) {
            Log.d("AwesomeLockScreen", "onResume");
            this.mIsPaused = false;
            this.mLockscreenView.resume();
            mRootHolder.getRoot().onCommand("resume");
            this.mWakeStartTime = System.currentTimeMillis() / 1000;
            updateStatusBarColormode();
        }
    }

    public void cleanUp() {
        mRootHolder.cleanUp(this);
    }

    public void unlocked(Intent intent, int i) {
        sendLockscreenIntentTypeAnalytics(intent);
        postDelayed(new Runnable() {
            public void run() {
                try {
                    AwesomeLockScreen.this.collapsePanel();
                } catch (ActivityNotFoundException e) {
                    Log.e("AwesomeLockScreen", e.toString());
                    e.printStackTrace();
                }
            }
        }, (long) i);
        if (MiuiFaceUnlockUtils.isSupportLiftingCamera(this.mContext)) {
            this.mUpdateMonitor.requestFaceAuth(1);
        }
        Log.d("AwesomeLockScreen", String.format("lockscreen awake time: [%d sec] in time range: [%d sec]", new Object[]{Long.valueOf(sTotalWakenTime), Long.valueOf((System.currentTimeMillis() / 1000) - sStartTime)}));
    }

    public boolean unlockVerify(String str, int i) {
        this.mPasswordMode = getPasswordMode();
        mRootHolder.getRoot().getVariables().put("__password_mode", (double) this.mPasswordMode);
        int i2 = this.mPasswordMode;
        if (i2 != 0 && i2 != -1) {
            return false;
        }
        collapsePanel();
        return true;
    }

    /* access modifiers changed from: private */
    public void collapsePanel() {
        this.mPanelViewController.collapse(false, 1.0f);
    }

    private void sendLockscreenIntentTypeAnalytics(Intent intent) {
        String str;
        Intent intent2 = new Intent("miui.intent.action.TRACK_EVENT");
        intent2.putExtra("eventId", "lockscreen_intent_type");
        if (intent == null) {
            str = "";
        } else {
            str = intent.toString();
        }
        intent2.putExtra("eventObj", str);
    }

    public int getPasswordMode() {
        int keyguardStoredPasswordQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(KeyguardUpdateMonitor.getCurrentUser());
        if (keyguardStoredPasswordQuality == 0) {
            return 0;
        }
        return (keyguardStoredPasswordQuality == 131072 || keyguardStoredPasswordQuality == 196608) ? 1 : 10;
    }

    public void pokeWakelock() {
        this.mStatusBar.userActivity();
    }

    public void haptic(int i) {
        performHapticFeedback(1);
    }

    public boolean isSoundEnable() {
        boolean z = Settings.System.getIntForUser(this.mContext.getContentResolver(), "lockscreen_sounds_enabled", 1, KeyguardUpdateMonitor.getCurrentUser()) != 0;
        if (this.mAudioManager.getRingerMode() != 2 || !z) {
            return false;
        }
        return true;
    }

    public void disableLockScreenFod(boolean z) {
        MiuiGxzwManager.getInstance().disableLockScreenFod(z);
    }

    public void disableLockScreenFodAnim(boolean z) {
        MiuiGxzwManager.getInstance().disableLockScreenFodAnim(z);
    }

    public void disableChargeAnimation(boolean z) {
        ChargeUtils.disableChargeAnimation(z);
    }

    public void startLockScreenFaceUnlock() {
        this.mUpdateMonitor.requestFaceAuth();
    }

    public void stopLockScreenFaceUnlock() {
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).cancelFaceAuth();
    }

    public void disableLockScreenFaceUnlockAnim(boolean z) {
        ((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).disableLockScreenFaceUnlockAnim(z);
    }

    public void cleanUpView() {
        if (this.mInitSuccessful) {
            this.mLockscreenView.finishRoot();
            this.mLockscreenView.cleanUp(true);
        }
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        this.mIsFocus = z;
        updatePauseResumeStatus();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mInitSuccessful) {
            return super.onTouchEvent(motionEvent);
        }
        collapsePanel();
        return true;
    }

    public void updateQsExpandHeight(float f) {
        Utils.putVariableNumber("qs_height", mRootHolder.getContext().mVariables, (double) f);
    }
}
