package com.android.keyguard;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.AwesomeLockScreenImp.AwesomeLockScreenView;
import com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot;
import com.android.keyguard.charge.ChargeUtils;
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
import miui.maml.util.Utils;
import org.jetbrains.annotations.Nullable;

public class AwesomeLockScreen extends FrameLayout implements LockScreenRoot.LockscreenCallback {
    private static RootHolder mRootHolder = new RootHolder();
    private static int mThemeChanged;
    private static long sStartTime;
    static boolean sSuppressNextLockSound;
    private static long sTotalWakenTime;
    private AudioManager mAudioManager;
    private FaceUnlockCallback mFaceUnlockCallback;
    private boolean mInitSuccessful;
    private boolean mIsFocus;
    private boolean mIsInteractive;
    private boolean mIsPaused;
    private boolean mKeyguardBouncerShowing;
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

    @Override // com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot.LockscreenCallback
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

    /* JADX DEBUG: Multi-variable search result rejected for r8v4, resolved type: com.android.keyguard.HeiHeiGestureView */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v10, types: [com.android.keyguard.AwesomeLockScreenImp.AwesomeLockScreenView, android.view.View] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    AwesomeLockScreen(android.content.Context r8) {
        /*
        // Method dump skipped, instructions count: 266
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.AwesomeLockScreen.<init>(android.content.Context):void");
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

    @Override // com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot.LockscreenCallback
    public void unlocked(Intent intent, int i) {
        sendLockscreenIntentTypeAnalytics(intent);
        postDelayed(new Runnable() {
            /* class com.android.keyguard.AwesomeLockScreen.AnonymousClass5 */

            public void run() {
                try {
                    AwesomeLockScreen.this.collapsePanel();
                } catch (ActivityNotFoundException e) {
                    Log.e("AwesomeLockScreen", e.toString());
                    e.printStackTrace();
                }
            }
        }, (long) i);
        if (MiuiFaceUnlockUtils.isSupportLiftingCamera(((FrameLayout) this).mContext)) {
            this.mUpdateMonitor.requestFaceAuth(1);
        }
        Log.d("AwesomeLockScreen", String.format("lockscreen awake time: [%d sec] in time range: [%d sec]", Long.valueOf(sTotalWakenTime), Long.valueOf((System.currentTimeMillis() / 1000) - sStartTime)));
    }

    @Override // com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot.LockscreenCallback
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
    /* access modifiers changed from: public */
    private void collapsePanel() {
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

    @Override // com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot.LockscreenCallback
    public int getPasswordMode() {
        int keyguardStoredPasswordQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(KeyguardUpdateMonitor.getCurrentUser());
        if (keyguardStoredPasswordQuality == 0) {
            return 0;
        }
        return (keyguardStoredPasswordQuality == 131072 || keyguardStoredPasswordQuality == 196608) ? 1 : 10;
    }

    @Override // com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot.LockscreenCallback
    public void pokeWakelock() {
        this.mStatusBar.userActivity();
    }

    @Override // com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot.LockscreenCallback
    public void haptic(int i) {
        performHapticFeedback(1);
    }

    @Override // com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot.LockscreenCallback
    public boolean isSoundEnable() {
        boolean z = Settings.System.getIntForUser(((FrameLayout) this).mContext.getContentResolver(), "lockscreen_sounds_enabled", 1, KeyguardUpdateMonitor.getCurrentUser()) != 0;
        if (this.mAudioManager.getRingerMode() != 2 || !z) {
            return false;
        }
        return true;
    }

    @Override // com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot.LockscreenCallback
    public void disableLockScreenFod(boolean z) {
        MiuiGxzwManager.getInstance().disableLockScreenFod(z);
    }

    @Override // com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot.LockscreenCallback
    public void disableLockScreenFodAnim(boolean z) {
        MiuiGxzwManager.getInstance().disableLockScreenFodAnim(z);
    }

    @Override // com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot.LockscreenCallback
    public void disableChargeAnimation(boolean z) {
        ChargeUtils.disableChargeAnimation(z);
    }

    @Override // com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot.LockscreenCallback
    public void startLockScreenFaceUnlock() {
        this.mUpdateMonitor.requestFaceAuth();
    }

    @Override // com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot.LockscreenCallback
    public void stopLockScreenFaceUnlock() {
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).cancelFaceAuth();
    }

    @Override // com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot.LockscreenCallback
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

    public void setStatusBar(@Nullable StatusBar statusBar) {
        this.mStatusBar = statusBar;
    }
}
