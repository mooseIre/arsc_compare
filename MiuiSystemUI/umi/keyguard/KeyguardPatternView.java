package com.android.keyguard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import com.android.internal.util.LatencyTracker;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockscreenCredential;
import com.android.keyguard.MiuiLockPatternView;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.settingslib.animation.AppearAnimationCreator;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0019R$plurals;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.miui.systemui.anim.PhysicBasedInterpolator;
import com.miui.systemui.util.HapticFeedBackImpl;
import java.util.List;
import java.util.concurrent.TimeUnit;
import miui.view.animation.SineEaseInOutInterpolator;

public class KeyguardPatternView extends MiuiKeyguardPasswordView implements KeyguardSecurityView, AppearAnimationCreator<MiuiLockPatternView.CellState> {
    /* access modifiers changed from: private */
    public boolean mAppearAnimating;
    private final AppearAnimationUtils mAppearAnimationUtils;
    /* access modifiers changed from: private */
    public Runnable mCancelPatternRunnable;
    private ViewGroup mContainer;
    private CountDownTimer mCountdownTimer;
    /* access modifiers changed from: private */
    public boolean mDisappearAnimatePending;
    private final DisappearAnimationUtils mDisappearAnimationUtils;
    private final DisappearAnimationUtils mDisappearAnimationUtilsLocked;
    /* access modifiers changed from: private */
    public Runnable mDisappearFinishRunnable;
    private int mDisappearYTranslation;
    private long mLastPokeTime;
    private final Rect mLockPatternScreenBounds;
    /* access modifiers changed from: private */
    public MiuiLockPatternView mLockPatternView;
    /* access modifiers changed from: private */
    public AsyncTask<?, ?, ?> mPendingLockCheck;
    private final int mScreenHeight;
    private final Rect mTempRect;
    private final int[] mTmpPosition;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean needsInput() {
        return false;
    }

    public KeyguardPatternView(Context context) {
        this(context, (AttributeSet) null);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public KeyguardPatternView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTmpPosition = new int[2];
        this.mTempRect = new Rect();
        this.mLockPatternScreenBounds = new Rect();
        this.mCountdownTimer = null;
        this.mLastPokeTime = -7000;
        this.mCancelPatternRunnable = new Runnable() {
            public void run() {
                KeyguardPatternView.this.mLockPatternView.clearPattern();
            }
        };
        this.mAppearAnimationUtils = new AppearAnimationUtils(context, 220, 1.5f, 2.0f, AnimationUtils.loadInterpolator(this.mContext, 17563662));
        this.mDisappearAnimationUtils = new DisappearAnimationUtils(context, 125, 1.2f, 0.6f, AnimationUtils.loadInterpolator(this.mContext, 17563663));
        this.mDisappearAnimationUtilsLocked = new DisappearAnimationUtils(context, 187, 1.2f, 0.6f, AnimationUtils.loadInterpolator(this.mContext, 17563663));
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(C0012R$dimen.miui_disappear_y_translation);
        this.mScreenHeight = context.getResources().getConfiguration().screenHeightDp;
    }

    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        this.mCallback = keyguardSecurityCallback;
    }

    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mContainer = (ViewGroup) findViewById(C0015R$id.container);
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils == null) {
            lockPatternUtils = new LockPatternUtils(this.mContext);
        }
        this.mLockPatternUtils = lockPatternUtils;
        MiuiLockPatternView miuiLockPatternView = (MiuiLockPatternView) findViewById(C0015R$id.lockPatternView);
        this.mLockPatternView = miuiLockPatternView;
        miuiLockPatternView.setSaveEnabled(false);
        this.mLockPatternView.setOnPatternListener(new UnlockPatternListener());
        this.mLockPatternView.setInStealthMode(!this.mLockPatternUtils.isVisiblePatternEnabled(KeyguardUpdateMonitor.getCurrentUser()));
        this.mLockPatternView.setTactileFeedbackEnabled(this.mLockPatternUtils.isTactileFeedbackEnabled());
        setPositionForFod();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        long elapsedRealtime = SystemClock.elapsedRealtime() - this.mLastPokeTime;
        if (onTouchEvent && elapsedRealtime > 6900) {
            this.mLastPokeTime = SystemClock.elapsedRealtime();
        }
        boolean z = false;
        this.mTempRect.set(0, 0, 0, 0);
        offsetRectIntoDescendantCoords(this.mLockPatternView, this.mTempRect);
        Rect rect = this.mTempRect;
        motionEvent.offsetLocation((float) rect.left, (float) rect.top);
        if (this.mLockPatternView.dispatchTouchEvent(motionEvent) || onTouchEvent) {
            z = true;
        }
        Rect rect2 = this.mTempRect;
        motionEvent.offsetLocation((float) (-rect2.left), (float) (-rect2.top));
        return z;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mLockPatternView.getLocationOnScreen(this.mTmpPosition);
        Rect rect = this.mLockPatternScreenBounds;
        int[] iArr = this.mTmpPosition;
        rect.set(iArr[0] - 40, iArr[1] - 40, iArr[0] + this.mLockPatternView.getWidth() + 40, this.mTmpPosition[1] + this.mLockPatternView.getHeight() + 40);
    }

    public void reset() {
        this.mLockPatternView.setInStealthMode(!this.mLockPatternUtils.isVisiblePatternEnabled(KeyguardUpdateMonitor.getCurrentUser()));
        this.mLockPatternView.enableInput();
        this.mLockPatternView.setEnabled(true);
        this.mLockPatternView.clearPattern();
        long lockoutAttemptDeadline = this.mLockPatternUtils.getLockoutAttemptDeadline(KeyguardUpdateMonitor.getCurrentUser());
        if (lockoutAttemptDeadline != 0) {
            handleAttemptLockout(lockoutAttemptDeadline);
        }
    }

    public boolean disallowInterceptTouch(MotionEvent motionEvent) {
        return !this.mLockPatternView.isEmpty() || this.mLockPatternScreenBounds.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    }

    private class UnlockPatternListener implements MiuiLockPatternView.OnPatternListener {
        public void onPatternCleared() {
        }

        private UnlockPatternListener() {
        }

        public void onPatternStart() {
            KeyguardPatternView.this.mLockPatternView.removeCallbacks(KeyguardPatternView.this.mCancelPatternRunnable);
        }

        public void onPatternCellAdded(List<LockPatternView.Cell> list) {
            KeyguardPatternView.this.mCallback.userActivity();
        }

        public void onPatternDetected(List<LockPatternView.Cell> list) {
            KeyguardPatternView.this.mLockPatternView.disableInput();
            int i = 0;
            if (KeyguardPatternView.this.mPendingLockCheck != null) {
                KeyguardPatternView.this.mPendingLockCheck.cancel(false);
            }
            final int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (list.size() < 4) {
                KeyguardPatternView.this.mLockPatternView.enableInput();
                onPatternChecked(currentUser, false, 0, false);
                return;
            }
            if (currentUser == 0) {
                UserSwitcherController userSwitcherController = (UserSwitcherController) Dependency.get(UserSwitcherController.class);
                i = UserSwitcherController.getSecondUser();
            }
            final int i2 = i;
            if (LatencyTracker.isEnabled(KeyguardPatternView.this.mContext)) {
                LatencyTracker.getInstance(KeyguardPatternView.this.mContext).onActionStart(3);
                LatencyTracker.getInstance(KeyguardPatternView.this.mContext).onActionStart(4);
            }
            final long currentTimeMillis = System.currentTimeMillis();
            KeyguardPatternView keyguardPatternView = KeyguardPatternView.this;
            AsyncTask unused = keyguardPatternView.mPendingLockCheck = MiuiLockPatternChecker.checkCredentialForUsers(keyguardPatternView.mLockPatternUtils, LockscreenCredential.createPattern(list), currentUser, i2, KeyguardPatternView.this.mContext, new OnCheckForUsersCallback() {
                public void onEarlyMatched() {
                    Slog.i("miui_keyguard_password", "pattern unlock duration " + (System.currentTimeMillis() - currentTimeMillis));
                    UnlockPatternListener.this.handleUserCheckMatched(currentUser);
                }

                public void onChecked(boolean z, int i, int i2) {
                    if (LatencyTracker.isEnabled(KeyguardPatternView.this.mContext)) {
                        LatencyTracker.getInstance(KeyguardPatternView.this.mContext).onActionEnd(4);
                    }
                    KeyguardPatternView.this.mLockPatternView.enableInput();
                    AsyncTask unused = KeyguardPatternView.this.mPendingLockCheck = null;
                    if (!z) {
                        UnlockPatternListener.this.onPatternChecked(i, false, i2, true);
                    }
                }
            }, new OnCheckForUsersCallback() {
                public void onChecked(boolean z, int i, int i2) {
                }

                public void onEarlyMatched() {
                    Slog.i("miui_keyguard_password", "pattern unlock duration other user" + (System.currentTimeMillis() - currentTimeMillis));
                    UnlockPatternListener.this.handleUserCheckMatched(i2);
                }
            });
            if (list.size() > 2) {
                KeyguardPatternView.this.mCallback.userActivity();
            }
        }

        /* access modifiers changed from: private */
        public void handleUserCheckMatched(int i) {
            if (LatencyTracker.isEnabled(KeyguardPatternView.this.mContext)) {
                LatencyTracker.getInstance(KeyguardPatternView.this.mContext).onActionEnd(3);
            }
            onPatternChecked(i, true, 0, true);
        }

        /* access modifiers changed from: private */
        public void onPatternChecked(int i, boolean z, int i2, boolean z2) {
            if (!z) {
                KeyguardPatternView.this.mLockPatternView.setDisplayMode(MiuiLockPatternView.DisplayMode.WRONG);
                if (z2) {
                    KeyguardPatternView.this.mCallback.reportUnlockAttempt(i, false, i2);
                    if (i2 > 0) {
                        KeyguardPatternView.this.mKeyguardUpdateMonitor.cancelFaceAuth();
                        KeyguardPatternView.this.handleAttemptLockout(KeyguardPatternView.this.mLockPatternUtils.setLockoutAttemptDeadline(i, i2));
                    }
                }
                ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).extHapticFeedback(76, true, 150);
                if (i2 == 0) {
                    KeyguardPatternView.this.mLockPatternView.postDelayed(KeyguardPatternView.this.mCancelPatternRunnable, 1500);
                }
            } else if (KeyguardPatternView.this.allowUnlock(i)) {
                KeyguardPatternView.this.switchUser(i);
                KeyguardPatternView.this.mCallback.reportUnlockAttempt(i, true, 0);
                KeyguardPatternView.this.mLockPatternView.setDisplayMode(MiuiLockPatternView.DisplayMode.CORRECT);
                KeyguardPatternView.this.mCallback.dismiss(true, i);
            } else {
                return;
            }
            ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setKeyguardUnlockWay("pw", z);
        }
    }

    /* access modifiers changed from: protected */
    public void handleWrongPassword() {
        this.mLockPatternView.setDisplayMode(MiuiLockPatternView.DisplayMode.WRONG);
        this.mVibrator.vibrate(150);
        this.mLockPatternView.postDelayed(this.mCancelPatternRunnable, 1500);
    }

    /* access modifiers changed from: private */
    public void handleAttemptLockout(long j) {
        this.mLockPatternView.clearPattern();
        this.mLockPatternView.setEnabled(false);
        long elapsedRealtime = j - SystemClock.elapsedRealtime();
        this.mCallback.handleAttemptLockout(elapsedRealtime);
        this.mCountdownTimer = new CountDownTimer(((long) Math.ceil(((double) elapsedRealtime) / 1000.0d)) * 1000, 1000) {
            public void onTick(long j) {
            }

            public void onFinish() {
                KeyguardPatternView.this.mLockPatternView.setEnabled(true);
            }
        }.start();
    }

    public void onPause() {
        CountDownTimer countDownTimer = this.mCountdownTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            this.mCountdownTimer = null;
        }
        AsyncTask<?, ?, ?> asyncTask = this.mPendingLockCheck;
        if (asyncTask != null) {
            asyncTask.cancel(false);
            this.mPendingLockCheck = null;
        }
    }

    public void onResume(int i) {
        reset();
    }

    public void showPromptReason(int i) {
        if (i != 0) {
            String promptReasonString = getPromptReasonString(i);
            if (!TextUtils.isEmpty(promptReasonString)) {
                this.mKeyguardBouncerMessageView.showMessage(this.mContext.getResources().getString(C0021R$string.input_password_hint_text), promptReasonString);
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getPromptReasonString(int i) {
        Resources resources = this.mContext.getResources();
        if (i == 0) {
            return "";
        }
        if (i == 1) {
            return resources.getString(C0021R$string.input_password_after_boot_msg);
        }
        if (i == 2) {
            long requiredStrongAuthTimeout = getRequiredStrongAuthTimeout();
            return resources.getQuantityString(C0019R$plurals.input_pattern_after_timeout_msg, (int) TimeUnit.MILLISECONDS.toHours(requiredStrongAuthTimeout), new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toHours(requiredStrongAuthTimeout))});
        } else if (i == 3) {
            return resources.getString(C0021R$string.kg_prompt_reason_device_admin);
        } else {
            if (i != 4) {
                return resources.getString(C0021R$string.kg_prompt_reason_timeout_pattern);
            }
            return resources.getString(C0021R$string.kg_prompt_reason_user_request);
        }
    }

    public void showMessage(String str, String str2, int i) {
        this.mKeyguardBouncerMessageView.showMessage(str, str2, i);
    }

    public void applyHintAnimation(long j) {
        this.mKeyguardBouncerMessageView.applyHintAnimation(j);
    }

    public void startAppearAnimation() {
        this.mKeyguardBouncerMessageView.setVisibility(0);
        enableClipping(false);
        setAlpha(1.0f);
        this.mAppearAnimating = true;
        this.mDisappearAnimatePending = false;
        setTranslationY((float) (this.mScreenHeight / 2));
        AppearAnimationUtils.startTranslationYAnimation(this, 0, 500, 0.0f, new PhysicBasedInterpolator(0.99f, 0.3f));
        this.mAppearAnimationUtils.startAnimation2d(this.mLockPatternView.getCellStates(), new Runnable() {
            public void run() {
                KeyguardPatternView.this.enableClipping(true);
                boolean unused = KeyguardPatternView.this.mAppearAnimating = false;
                if (KeyguardPatternView.this.mDisappearAnimatePending) {
                    boolean unused2 = KeyguardPatternView.this.mDisappearAnimatePending = false;
                    KeyguardPatternView keyguardPatternView = KeyguardPatternView.this;
                    keyguardPatternView.startDisappearAnimation(keyguardPatternView.mDisappearFinishRunnable);
                }
            }
        }, this);
    }

    public boolean startDisappearAnimation(final Runnable runnable) {
        DisappearAnimationUtils disappearAnimationUtils;
        if (this.mAppearAnimating) {
            this.mDisappearAnimatePending = true;
            this.mDisappearFinishRunnable = runnable;
            return true;
        }
        float f = this.mKeyguardUpdateMonitor.needsSlowUnlockTransition() ? 1.5f : 1.0f;
        this.mLockPatternView.clearPattern();
        enableClipping(false);
        setTranslationY(0.0f);
        AppearAnimationUtils.startTranslationYAnimation(this, 0, (long) (f * 350.0f), (float) this.mDisappearYTranslation, new SineEaseInOutInterpolator());
        if (this.mKeyguardUpdateMonitor.needsSlowUnlockTransition()) {
            disappearAnimationUtils = this.mDisappearAnimationUtilsLocked;
        } else {
            disappearAnimationUtils = this.mDisappearAnimationUtils;
        }
        disappearAnimationUtils.startAnimation2d(this.mLockPatternView.getCellStates(), new Runnable() {
            public void run() {
                Log.d("SecurityPatternView", "startDisappearAnimation finish");
                KeyguardPatternView.this.enableClipping(true);
                Runnable runnable = runnable;
                if (runnable != null) {
                    runnable.run();
                }
            }
        }, this);
        this.mEmergencyButton.setEnabled(false);
        return true;
    }

    /* access modifiers changed from: private */
    public void enableClipping(boolean z) {
        setClipChildren(z);
        setClipToPadding(z);
    }

    public void createAnimation(MiuiLockPatternView.CellState cellState, long j, long j2, float f, boolean z, Interpolator interpolator, Runnable runnable) {
        this.mLockPatternView.startCellStateAnimation(cellState, 1.0f, z ? 1.0f : 0.0f, z ? f : 0.0f, z ? 0.0f : f, z ? 0.0f : 1.0f, 1.0f, j, j2, interpolator, runnable);
        if (runnable != null) {
            this.mAppearAnimationUtils.createAnimation((View) this.mEmergencyCarrierArea, j, j2, f, z, interpolator, (Runnable) null);
        }
    }

    /* access modifiers changed from: protected */
    public void handleConfigurationFontScaleChanged() {
        float dimensionPixelSize = (float) getResources().getDimensionPixelSize(C0012R$dimen.miui_keyguard_view_eca_text_size);
        this.mEmergencyButton.setTextSize(0, dimensionPixelSize);
        this.mBackButton.setTextSize(0, dimensionPixelSize);
    }

    /* access modifiers changed from: protected */
    public void handleConfigurationOrientationChanged() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mKeyguardBouncerMessageView.getLayoutParams();
        layoutParams.topMargin = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_bouncer_message_view_margin_top);
        this.mKeyguardBouncerMessageView.setLayoutParams(layoutParams);
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mContainer.getLayoutParams();
        layoutParams2.bottomMargin = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_pattern_view_container_margin_bottom);
        this.mContainer.setLayoutParams(layoutParams2);
    }

    /* access modifiers changed from: protected */
    public void handleConfigurationSmallWidthChanged() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mContainer.getLayoutParams();
        layoutParams.width = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_pattern_layout_width);
        layoutParams.height = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_pattern_layout_height);
        layoutParams.bottomMargin = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_pattern_view_container_margin_bottom);
        this.mContainer.setLayoutParams(layoutParams);
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mLockPatternView.getLayoutParams();
        layoutParams2.width = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_pattern_view_pattern_view_height_width);
        layoutParams2.height = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_pattern_view_pattern_view_height_width);
        this.mLockPatternView.setLayoutParams(layoutParams2);
    }

    private void setPositionForFod() {
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            Display display = ((DisplayManager) getContext().getSystemService("display")).getDisplay(0);
            Point point = new Point();
            display.getRealSize(point);
            int max = Math.max(point.x, point.y);
            int dimensionPixelOffset = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_pattern_layout_height);
            int dimensionPixelOffset2 = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_pattern_view_pattern_view_height_width);
            int dimensionPixelOffset3 = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_pattern_view_pattern_view_margin_bottom);
            int dimensionPixelOffset4 = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_pattern_view_eca_height);
            int dimensionPixelOffset5 = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_pattern_view_eca_fod_top_margin);
            int i = (dimensionPixelOffset - dimensionPixelOffset2) - dimensionPixelOffset3;
            int i2 = max - (i - (dimensionPixelOffset4 / 2));
            int i3 = max - i;
            Rect fodPosition = MiuiGxzwManager.getFodPosition(getContext());
            int height = fodPosition.top + (fodPosition.height() / 2);
            int dimensionPixelOffset6 = (i3 - fodPosition.bottom) - getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_pattern_view_em_fod_top_margin);
            View findViewById = findViewById(C0015R$id.container);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById.getLayoutParams();
            if (MiuiKeyguardUtils.isGlobalAndFingerprintEnable()) {
                layoutParams.bottomMargin = dimensionPixelOffset6;
                layoutParams.height = dimensionPixelOffset + dimensionPixelOffset5 + ((i2 - height) - dimensionPixelOffset6);
            } else {
                layoutParams.bottomMargin = i2 - height;
                layoutParams.height = dimensionPixelOffset + dimensionPixelOffset5;
            }
            findViewById.setLayoutParams(layoutParams);
            if (MiuiKeyguardUtils.isGlobalAndFingerprintEnable()) {
                LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mEmergencyButton.getLayoutParams();
                layoutParams2.topMargin = ((i2 - height) - dimensionPixelOffset6) + dimensionPixelOffset5;
                layoutParams2.height = dimensionPixelOffset4;
                this.mEmergencyButton.setLayoutParams(layoutParams2);
                LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mBackButton.getLayoutParams();
                layoutParams3.topMargin = dimensionPixelOffset5;
                layoutParams3.height = dimensionPixelOffset4;
                this.mBackButton.setLayoutParams(layoutParams3);
                return;
            }
            View findViewById2 = findViewById(C0015R$id.keyguard_selector_fade_container);
            LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) findViewById2.getLayoutParams();
            layoutParams4.topMargin = dimensionPixelOffset5;
            findViewById2.setLayoutParams(layoutParams4);
        }
    }
}
