package com.android.keyguard.fod;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.biometrics.BiometricSourceType;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiDozeServiceHost;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.fod.MiuiGxzwQuickOpenView;
import com.android.keyguard.fod.MiuiGxzwSensor;
import com.android.keyguard.fod.MiuiGxzwTransparentTimer;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.C0018R$string;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.phone.StatusBar;

class MiuiGxzwIconView extends GxzwNoRotateFrameLayout implements View.OnTouchListener, DisplayManager.DisplayListener, MiuiGxzwQuickOpenView.QuickViewListener, MiuiGxzwSensor.MiuiGxzwSensorListener, MiuiGxzwTransparentTimer.TransparentTimerListener {
    private CollectGxzwListener mCollectGxzwListener;
    private int mCurrentNonUIMode;
    private boolean mDeviceMoving = false;
    private DisplayManager mDisplayManager;
    private int mDisplayState = 2;
    private boolean mDozeShowIconTimeout = false;
    /* access modifiers changed from: private */
    public boolean mDozing = false;
    /* access modifiers changed from: private */
    public boolean mFingerprintLockout = false;
    private Runnable mGotoUnlockRunnable = new Runnable() {
        public void run() {
            if (MiuiGxzwIconView.this.mDozing) {
                boolean unused = MiuiGxzwIconView.this.mPendingShowBouncer = true;
                MiuiGxzwIconView.this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:GOTO_UNLOCK");
                return;
            }
            MiuiGxzwIconView.this.showBouncer();
        }
    };
    private boolean mGxzwIconTransparent = true;
    private MiuiGxzwHightlightContainer mHighlightView;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
        public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
            super.onBiometricAuthFailed(biometricSourceType);
            if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                Log.i("MiuiGxzwViewIcon", "onFingerprintAuthFailed");
                MiuiGxzwIconView miuiGxzwIconView = MiuiGxzwIconView.this;
                if (miuiGxzwIconView.mKeyguardAuthen) {
                    miuiGxzwIconView.mMiuiGxzwAnimView.cancelAnimFeedback(MiuiGxzwIconView.this.mContext);
                }
                MiuiGxzwIconView miuiGxzwIconView2 = MiuiGxzwIconView.this;
                if (miuiGxzwIconView2.mKeyguardAuthen && miuiGxzwIconView2.mTouchDown && !((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).shouldListenForFingerprintWhenUnlocked()) {
                    MiuiGxzwIconView.this.mMiuiGxzwAnimView.startFalseAnim();
                }
                MiuiGxzwIconView.this.mMiuiGxzwAnimView.performFailFeedback();
            }
        }

        public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
            super.onBiometricAuthenticated(i, biometricSourceType, z);
            if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                MiuiGxzwIconView.this.mMiuiGxzwAnimView.performSuccessFeedback();
            }
        }

        public void onBiometricError(int i, String str, BiometricSourceType biometricSourceType) {
            super.onBiometricError(i, str, biometricSourceType);
            Log.i("MiuiGxzwViewIcon", "onFingerprintError: msgId = " + i + ", errString = " + str);
            if (biometricSourceType != BiometricSourceType.FINGERPRINT) {
                return;
            }
            if (i == 7 || i == 9) {
                boolean unused = MiuiGxzwIconView.this.mFingerprintLockout = true;
            }
        }

        public void onBiometricRunningStateChanged(boolean z, BiometricSourceType biometricSourceType) {
            super.onBiometricRunningStateChanged(z, biometricSourceType);
            if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                Log.i("MiuiGxzwViewIcon", "onFingerprintRunningStateChanged: running = " + z);
                if (z) {
                    boolean unused = MiuiGxzwIconView.this.mFingerprintLockout = false;
                }
            }
        }
    };
    private WindowManager.LayoutParams mLayoutParams;
    /* access modifiers changed from: private */
    public MiuiGxzwAnimView mMiuiGxzwAnimView;
    private MiuiGxzwQuickOpenView mMiuiGxzwQuickOpenView;
    private MiuiGxzwSensor mMiuiGxzwSensor;
    private MiuiGxzwTouchHelper mMiuiGxzwTouchHelper;
    private MiuiGxzwTransparentTimer mMiuiGxzwTransparentTimer;
    private boolean mPendingShow;
    /* access modifiers changed from: private */
    public boolean mPendingShowBouncer;
    private boolean mPendingShowLightIcon;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public boolean mTouchDown = false;
    protected final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() {
        public void onStartedWakingUp() {
            if (MiuiGxzwIconView.this.mPendingShowBouncer) {
                MiuiGxzwIconView.this.showBouncer();
                boolean unused = MiuiGxzwIconView.this.mPendingShowBouncer = false;
            }
        }

        public void onStartedGoingToSleep() {
            boolean unused = MiuiGxzwIconView.this.mPendingShowBouncer = false;
        }

        public void onFinishedGoingToSleep() {
            Log.i("MiuiGxzwViewIcon", "onFinishedGoingToSleep");
            MiuiGxzwIconView.this.updateDozeScreenState();
        }
    };

    interface CollectGxzwListener {
        void onCollectStateChange(boolean z);

        void onIconStateChange(boolean z);
    }

    public void onDisplayAdded(int i) {
    }

    public void onDisplayRemoved(int i) {
    }

    public MiuiGxzwIconView(Context context) {
        super(context);
        Log.d("MiuiGxzwViewIcon", "MiuiGxzwIconView");
        initView();
    }

    /* access modifiers changed from: protected */
    public WindowManager.LayoutParams generateLayoutParams() {
        return this.mLayoutParams;
    }

    private void initView() {
        this.mHighlightView = new MiuiGxzwHightlightContainer(getContext());
        this.mMiuiGxzwAnimView = new MiuiGxzwAnimView(getContext());
        this.mMiuiGxzwQuickOpenView = new MiuiGxzwQuickOpenView(getContext());
        this.mMiuiGxzwSensor = new MiuiGxzwSensor(getContext());
        this.mPowerManager = (PowerManager) getContext().getSystemService("power");
        setSystemUiVisibility(4864);
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mDisplayManager = (DisplayManager) getContext().getSystemService("display");
        this.mMiuiGxzwTransparentTimer = new MiuiGxzwTransparentTimer(getContext());
        this.mMiuiGxzwTouchHelper = new MiuiGxzwTouchHelper(this, this.mMiuiGxzwQuickOpenView);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(this.mRegion.width(), this.mRegion.height(), 2018, 25167368, -2);
        this.mLayoutParams = layoutParams;
        layoutParams.layoutInDisplayCutoutMode = 1;
        layoutParams.privateFlags |= MiuiGxzwUtils.PRIVATE_FLAG_IS_HBM_OVERLAY;
        layoutParams.gravity = 51;
        layoutParams.setTitle("gxzw_touch");
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mMiuiGxzwQuickOpenView.setQuickViewListener(this);
        setOnTouchListener(this);
        this.mKeyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        ((WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class)).addObserver(this.mWakefulnessObserver);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mMiuiGxzwQuickOpenView.setQuickViewListener((MiuiGxzwQuickOpenView.QuickViewListener) null);
        setOnTouchListener((View.OnTouchListener) null);
        this.mKeyguardUpdateMonitor.removeCallback(this.mKeyguardUpdateMonitorCallback);
        ((WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class)).removeObserver(this.mWakefulnessObserver);
    }

    public void show(boolean z) {
        if (!this.mShowing) {
            Log.d("MiuiGxzwViewIcon", "show");
            if (this.mMiuiGxzwQuickOpenView.isShow()) {
                this.mPendingShow = true;
                this.mPendingShowLightIcon = z;
                return;
            }
            super.show();
            this.mMiuiGxzwAnimView.show(z);
            if (!this.mKeyguardAuthen || !MiuiGxzwQuickOpenUtil.isQuickOpenEnable(getContext())) {
                this.mLayoutParams.screenOrientation = -1;
            } else {
                this.mLayoutParams.screenOrientation = 5;
            }
            if (this.mDozing) {
                this.mMiuiGxzwSensor.registerDozeSensor(this);
                scheduleSetIconTransparen();
                this.mMiuiGxzwTransparentTimer.onResume();
                if (!MiuiGxzwUtils.isFodAodShowEnable(getContext())) {
                    dismissFingerpirntIcon();
                }
            } else {
                setGxzwIconOpaque();
            }
            addViewToWindow();
            if (isAttachedToWindow()) {
                this.mWindowManager.updateViewLayout(this, this.mLayoutParams);
            }
            setVisibility(0);
            resetState();
            this.mDisplayState = this.mDisplayManager.getDisplay(0).getState();
            this.mDisplayManager.registerDisplayListener(this, this.mHandler);
            this.mHighlightView.show();
        }
    }

    public void dismiss() {
        this.mPendingShow = false;
        this.mPendingShowLightIcon = false;
        if (this.mShowing) {
            super.dismiss();
            Log.d("MiuiGxzwViewIcon", "dismiss");
            if (this.mDozing) {
                setGxzwIconOpaque();
                this.mMiuiGxzwSensor.unregisterDozeSensor();
                unscheduleSetIconTransparen();
                this.mMiuiGxzwTransparentTimer.onPause();
            }
            if (!this.mTouchDown || ((!MiuiGxzwManager.getInstance().isUnlockByGxzw() && !((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).shouldListenForFingerprintWhenUnlocked()) || !MiuiGxzwQuickOpenUtil.isQuickOpenEnable(getContext()))) {
                this.mMiuiGxzwQuickOpenView.dismiss();
            } else {
                this.mMiuiGxzwQuickOpenView.show(MiuiGxzwManager.getInstance().getGxzwAuthFingerprintID());
            }
            this.mMiuiGxzwAnimView.dismiss(this.mTouchDown);
            this.mMiuiGxzwTouchHelper.onTouchUp(false);
            if (!this.mMiuiGxzwQuickOpenView.isShow()) {
                removeIconView();
            }
            resetState();
            this.mDisplayManager.unregisterDisplayListener(this);
            this.mHighlightView.dismiss();
        }
    }

    private void removeIconView() {
        removeViewFromWindow();
        setVisibility(8);
    }

    public void startDozing() {
        Log.d("MiuiGxzwViewIcon", "startDozing");
        this.mMiuiGxzwAnimView.startDozing();
        this.mDozing = true;
        if (this.mShowing) {
            this.mMiuiGxzwSensor.registerDozeSensor(this);
            scheduleSetIconTransparen();
            this.mMiuiGxzwTransparentTimer.onResume();
        }
        if (!MiuiGxzwUtils.isFodAodShowEnable(getContext())) {
            dismissFingerpirntIcon();
        }
        resetState();
    }

    public void stopDozing() {
        Log.d("MiuiGxzwViewIcon", "stopDozing");
        this.mMiuiGxzwAnimView.stopDozing();
        this.mDozing = false;
        if (this.mShowing) {
            setGxzwIconOpaque();
            this.mMiuiGxzwSensor.unregisterDozeSensor();
            unscheduleSetIconTransparen();
            this.mMiuiGxzwTransparentTimer.onPause();
        }
        resetState();
    }

    public void setCollectGxzwListener(CollectGxzwListener collectGxzwListener) {
        this.mCollectGxzwListener = collectGxzwListener;
    }

    public void onScreenTurnedOn() {
        Log.d("MiuiGxzwViewIcon", "onScreenTurnedOn");
    }

    public void onStartedGoingToSleep() {
        Log.d("MiuiGxzwViewIcon", "onStartedGoingToSleep");
        this.mMiuiGxzwQuickOpenView.dismiss();
        this.mMiuiGxzwQuickOpenView.resetFingerID();
    }

    public void onKeyguardAuthen(boolean z) {
        super.onKeyguardAuthen(z);
        this.mHighlightView.onKeyguardAuthen(z);
        this.mMiuiGxzwAnimView.onKeyguardAuthen(z);
    }

    /* access modifiers changed from: protected */
    public Rect caculateRegion() {
        int i;
        int i2 = 0;
        if (AccessibilityManager.getInstance(getContext()).isTouchExplorationEnabled() || MiuiGxzwUtils.isLargeFod()) {
            i = 0;
        } else {
            int i3 = (int) (((float) MiuiGxzwUtils.GXZW_ICON_HEIGHT) * 0.2f);
            i2 = (int) (((float) MiuiGxzwUtils.GXZW_ICON_WIDTH) * 0.2f);
            i = i3;
        }
        return new Rect(MiuiGxzwUtils.GXZW_ICON_X - i2, MiuiGxzwUtils.GXZW_ICON_Y - i, MiuiGxzwUtils.GXZW_ICON_X + MiuiGxzwUtils.GXZW_ICON_WIDTH + i2, MiuiGxzwUtils.GXZW_ICON_Y + MiuiGxzwUtils.GXZW_ICON_HEIGHT + i);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mHighlightView.onConfigurationChanged(configuration);
    }

    public void setEnrolling(boolean z) {
        this.mMiuiGxzwAnimView.setEnrolling(z);
    }

    public void updateHightlightBackground() {
        this.mHighlightView.updateViewBackground();
    }

    public void refreshIcon() {
        this.mMiuiGxzwAnimView.drawFingerprintIcon(this.mDozing);
    }

    public void disableLockScreenFodAnim(boolean z) {
        this.mMiuiGxzwAnimView.disableLockScreenFodAnim(z);
    }

    public void dismissGxzwIconView(boolean z) {
        int i = 8;
        setVisibility(z ? 8 : 0);
        this.mHighlightView.setVisibility(z ? 8 : 0);
        MiuiGxzwAnimView miuiGxzwAnimView = this.mMiuiGxzwAnimView;
        if (!z) {
            i = 0;
        }
        miuiGxzwAnimView.setVisibilityAnim(i);
        this.mMiuiGxzwAnimView.stopAnim();
    }

    public boolean isDozing() {
        return this.mDozing;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        return this.mMiuiGxzwTouchHelper.onTouch(motionEvent);
    }

    /* access modifiers changed from: package-private */
    public void setCanvasInfo(float f, float f2, float f3, float f4, float f5) {
        if (MiuiGxzwUtils.isLargeFod()) {
            this.mHighlightView.setTouchCenter(f, f2);
            this.mHighlightView.setOvalInfo(f3, f4, f5);
            this.mMiuiGxzwAnimView.setTranslate(((int) f) - (this.mRegion.width() / 2), ((int) f2) - (this.mRegion.height() / 2));
        }
    }

    public boolean onHoverEvent(MotionEvent motionEvent) {
        if (!AccessibilityManager.getInstance(getContext()).isTouchExplorationEnabled()) {
            return super.onHoverEvent(motionEvent);
        }
        int action = motionEvent.getAction();
        if (action == 7) {
            motionEvent.setAction(2);
        } else if (action == 9) {
            motionEvent.setAction(0);
            setTalkbackDescription(getContext().getString(C0018R$string.gxzw_area));
        } else if (action == 10) {
            motionEvent.setAction(1);
        }
        onTouch(this, motionEvent);
        return true;
    }

    private void setTalkbackDescription(String str) {
        setContentDescription(str);
        announceForAccessibility(str);
    }

    /* access modifiers changed from: package-private */
    public void onTouchDown() {
        if (!this.mTouchDown) {
            Log.i("MiuiGxzwViewIcon", "onTouchDown");
            turnOnAodIfScreenOff();
            setGxzwIconOpaque();
            this.mMiuiGxzwAnimView.setCollecting(true);
            if (!MiuiGxzwManager.getInstance().isHbmAlwaysOnWhenDoze()) {
                ((MiuiDozeServiceHost) Dependency.get(MiuiDozeServiceHost.class)).fireFingerprintPressed(true);
            }
            if (((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).shouldListenForFingerprintWhenUnlocked()) {
                this.mHandler.post(this.mGotoUnlockRunnable);
            } else if (this.mFingerprintLockout || this.mKeyguardUpdateMonitor.userNeedsStrongAuth()) {
                this.mHandler.postDelayed(this.mGotoUnlockRunnable, 400);
            }
            this.mTouchDown = true;
            this.mHighlightView.setHightlightOpaque();
            if (this.mDozing) {
                MiuiGxzwManager.getInstance().requestDrawWackLock(300);
            }
            CollectGxzwListener collectGxzwListener = this.mCollectGxzwListener;
            if (collectGxzwListener != null) {
                collectGxzwListener.onCollectStateChange(true);
            }
            if (this.mKeyguardAuthen) {
                this.mMiuiGxzwAnimView.startRecognizingAnim();
            }
            if (this.mDozing) {
                unscheduleSetIconTransparen();
            }
            if (this.mKeyguardAuthen) {
                MiuiGxzwManager.getInstance().notifyGxzwTouchDown();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onTouchUp(boolean z) {
        if (this.mTouchDown) {
            Log.i("MiuiGxzwViewIcon", "onTouchUp");
            this.mMiuiGxzwAnimView.setCollecting(false);
            this.mHandler.removeCallbacks(this.mGotoUnlockRunnable);
            CollectGxzwListener collectGxzwListener = this.mCollectGxzwListener;
            if (collectGxzwListener != null) {
                collectGxzwListener.onCollectStateChange(false);
            }
            this.mTouchDown = false;
            if (!MiuiGxzwManager.getInstance().isHbmAlwaysOnWhenDoze()) {
                ((MiuiDozeServiceHost) Dependency.get(MiuiDozeServiceHost.class)).fireFingerprintPressed(false);
            }
            setHightlightTransparen();
            if (this.mDozing) {
                MiuiGxzwManager.getInstance().requestDrawWackLock(300);
            }
            if (z && this.mKeyguardAuthen) {
                this.mMiuiGxzwAnimView.startBackAnim();
            } else if (!this.mKeyguardAuthen || !MiuiGxzwManager.getInstance().isUnlockByGxzw()) {
                this.mMiuiGxzwAnimView.stopAnim();
            }
            this.mMiuiGxzwAnimView.stopTip();
            if (this.mDozing) {
                scheduleSetIconTransparen();
            }
            if (this.mKeyguardAuthen) {
                MiuiGxzwManager.getInstance().notifyGxzwTouchUp();
            }
        }
    }

    private void scheduleSetIconTransparen() {
        Log.i("MiuiGxzwViewIcon", "scheduleSetIconTransparen");
        this.mMiuiGxzwTransparentTimer.schedule(this);
        this.mDozeShowIconTimeout = false;
    }

    private void unscheduleSetIconTransparen() {
        Log.i("MiuiGxzwViewIcon", "unscheduleSetIconTransparen");
        this.mMiuiGxzwTransparentTimer.cancel();
    }

    private void dismissFingerpirntIcon() {
        setGxzwIconTransparent();
        unscheduleSetIconTransparen();
    }

    private void showFingerprintIcon() {
        if (this.mDozing) {
            setGxzwIconOpaque();
            scheduleSetIconTransparen();
            updateDozeScreenState();
        }
    }

    private void setGxzwIconTransparent() {
        if (!this.mGxzwIconTransparent) {
            Slog.i("MiuiGxzwViewIcon", "setGxzwIconTransparent");
            this.mMiuiGxzwAnimView.setGxzwTransparent(true);
            if (this.mDozing) {
                MiuiGxzwManager.getInstance().requestDrawWackLock(300);
            }
            CollectGxzwListener collectGxzwListener = this.mCollectGxzwListener;
            if (collectGxzwListener != null) {
                collectGxzwListener.onIconStateChange(true);
            }
            this.mGxzwIconTransparent = true;
            ((MiuiDozeServiceHost) Dependency.get(MiuiDozeServiceHost.class)).onGxzwIconChanged(this.mGxzwIconTransparent);
            updateDozeScreenState();
        }
    }

    private void setGxzwIconOpaque() {
        if (this.mGxzwIconTransparent) {
            Slog.i("MiuiGxzwViewIcon", "setGxzwIconOpaque");
            this.mMiuiGxzwAnimView.setGxzwTransparent(false);
            if (this.mDozing) {
                MiuiGxzwManager.getInstance().requestDrawWackLock(300);
            }
            CollectGxzwListener collectGxzwListener = this.mCollectGxzwListener;
            if (collectGxzwListener != null) {
                collectGxzwListener.onIconStateChange(false);
            }
            this.mGxzwIconTransparent = false;
            ((MiuiDozeServiceHost) Dependency.get(MiuiDozeServiceHost.class)).onGxzwIconChanged(this.mGxzwIconTransparent);
        }
    }

    private void turnOffScreenIfInAod() {
        int state = this.mDisplayManager.getDisplay(0).getState();
        if (this.mDozing && state != 1) {
            Slog.i("MiuiGxzwViewIcon", "turnOffScreen");
            ((MiuiDozeServiceHost) Dependency.get(MiuiDozeServiceHost.class)).fireAodState(false);
        }
    }

    private void turnOnAodIfScreenOff() {
        int state = this.mDisplayManager.getDisplay(0).getState();
        if (this.mDozing && state == 1) {
            Slog.i("MiuiGxzwViewIcon", "turnOnScreen");
            ((MiuiDozeServiceHost) Dependency.get(MiuiDozeServiceHost.class)).fireAodState(true);
        }
    }

    /* access modifiers changed from: private */
    public void showBouncer() {
        ((StatusBar) Dependency.get(StatusBar.class)).showBouncerIfKeyguard();
    }

    public void onDeviceMove() {
        Slog.i("MiuiGxzwViewIcon", "detect device move");
        if (this.mCurrentNonUIMode == 0) {
            showFingerprintIcon();
        }
        this.mDeviceMoving = true;
    }

    public void onDeviceStable() {
        Slog.i("MiuiGxzwViewIcon", "detect device stable");
        this.mDeviceMoving = false;
        if (this.mDozeShowIconTimeout && this.mDozing) {
            if (this.mTouchDown) {
                scheduleSetIconTransparen();
            } else {
                dismissFingerpirntIcon();
            }
        }
    }

    public void onDevicePutUp() {
        Slog.i("MiuiGxzwViewIcon", "detect device put up");
        if (this.mCurrentNonUIMode == 0) {
            showFingerprintIcon();
        }
    }

    public void onEnterNonUI(int i) {
        if (this.mCurrentNonUIMode != i) {
            Slog.i("MiuiGxzwViewIcon", "enter nonui mode");
            setNonUIMode(i);
            dismissFingerpirntIcon();
        }
    }

    public void onEixtNonUI(int i) {
        if (this.mCurrentNonUIMode != i) {
            Slog.i("MiuiGxzwViewIcon", "exit nonui mode");
            setNonUIMode(i);
            if (this.mDeviceMoving) {
                showFingerprintIcon();
            }
        }
    }

    public void setHightlightTransparen() {
        this.mHighlightView.setHightlightTransparen();
    }

    public void onKeycodeGoto() {
        Slog.i("MiuiGxzwViewIcon", "onKeycodeGoto");
        if (MiuiGxzwUtils.isFodAodShowEnable(getContext()) && this.mCurrentNonUIMode == 0) {
            showFingerprintIcon();
        }
    }

    public void onTransparentTimeout() {
        if (this.mDozing) {
            this.mDozeShowIconTimeout = true;
            if (this.mTouchDown) {
                scheduleSetIconTransparen();
            } else if (!this.mDeviceMoving) {
                dismissFingerpirntIcon();
            }
        }
    }

    public void onDisplayChanged(int i) {
        if (i == 0) {
            int state = this.mDisplayManager.getDisplay(i).getState();
            int i2 = this.mDisplayState;
            if (this.mKeyguardAuthen && this.mShowing && this.mDozing) {
                boolean z = false;
                boolean z2 = (state == 3 || state == 4) && i2 == 1 && this.mGxzwIconTransparent;
                if ((i2 == 3 || i2 == 4) && state == 1 && !this.mGxzwIconTransparent) {
                    z = true;
                }
                if (z2 || z) {
                    updateDozeScreenState();
                }
            }
            this.mDisplayState = state;
        }
    }

    /* access modifiers changed from: private */
    public void updateDozeScreenState() {
        if (!MiuiKeyguardUtils.isInvertColorsEnable(this.mContext)) {
            if (this.mGxzwIconTransparent) {
                turnOffScreenIfInAod();
            } else {
                turnOnAodIfScreenOff();
            }
        }
    }

    public void onDismiss() {
        if (this.mPendingShow) {
            show(this.mPendingShowLightIcon);
            this.mPendingShow = false;
            this.mPendingShowLightIcon = false;
            return;
        }
        removeIconView();
    }

    public void onShow() {
        if (AccessibilityManager.getInstance(getContext()).isTouchExplorationEnabled()) {
            removeIconView();
        }
    }

    private void resetState() {
        this.mDeviceMoving = false;
        setNonUIMode(0);
        this.mDozeShowIconTimeout = false;
    }

    private void setNonUIMode(int i) {
        if (this.mCurrentNonUIMode != i) {
            this.mCurrentNonUIMode = i;
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable(i) {
                public final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    MiuiGxzwUtils.setTouchMode(17, this.f$0);
                }
            });
        }
    }
}
