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
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.keyguard.MiuiLockPatternView;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.settingslib.animation.AppearAnimationCreator;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.miui.anim.PhysicBasedInterpolator;
import com.android.systemui.plugins.R;
import java.util.List;
import java.util.concurrent.TimeUnit;
import miui.view.animation.SineEaseInOutInterpolator;

public class KeyguardPatternView extends MiuiKeyguardPasswordView implements KeyguardSecurityView, AppearAnimationCreator<MiuiLockPatternView.CellState> {
    /* access modifiers changed from: private */
    public boolean mAppearAnimating;
    private final AppearAnimationUtils mAppearAnimationUtils;
    /* access modifiers changed from: private */
    public Runnable mCancelPatternRunnable;
    private CountDownTimer mCountdownTimer;
    /* access modifiers changed from: private */
    public boolean mDisappearAnimatePending;
    private final DisappearAnimationUtils mDisappearAnimationUtils;
    private final DisappearAnimationUtils mDisappearAnimationUtilsLocked;
    /* access modifiers changed from: private */
    public Runnable mDisappearFinishRunnable;
    private int mDisappearYTranslation;
    private long mLastPokeTime;
    /* access modifiers changed from: private */
    public MiuiLockPatternView mLockPatternView;
    /* access modifiers changed from: private */
    public AsyncTask<?, ?, ?> mPendingLockCheck;
    private final int mScreenHeight;
    private Rect mTempRect;

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
        this.mCountdownTimer = null;
        this.mLastPokeTime = -7000;
        this.mCancelPatternRunnable = new Runnable() {
            public void run() {
                KeyguardPatternView.this.mLockPatternView.clearPattern();
            }
        };
        this.mTempRect = new Rect();
        this.mAppearAnimationUtils = new AppearAnimationUtils(context, 220, 1.5f, 2.0f, AnimationUtils.loadInterpolator(this.mContext, 17563662));
        this.mDisappearAnimationUtils = new DisappearAnimationUtils(context, 125, 1.2f, 0.6f, AnimationUtils.loadInterpolator(this.mContext, 17563663));
        this.mDisappearAnimationUtilsLocked = new DisappearAnimationUtils(context, 187, 1.2f, 0.6f, AnimationUtils.loadInterpolator(this.mContext, 17563663));
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(R.dimen.miui_disappear_y_translation);
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
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils == null) {
            lockPatternUtils = new LockPatternUtils(this.mContext);
        }
        this.mLockPatternUtils = lockPatternUtils;
        MiuiLockPatternView miuiLockPatternView = (MiuiLockPatternView) findViewById(R.id.lockPatternView);
        this.mLockPatternView = miuiLockPatternView;
        miuiLockPatternView.setSaveEnabled(false);
        this.mLockPatternView.setOnPatternListener(new UnlockPatternListener());
        this.mLockPatternView.setTactileFeedbackEnabled(this.mLockPatternUtils.isTactileFeedbackEnabled());
        setPositionForFod();
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
                i = KeyguardUpdateMonitor.getSecondUser();
            }
            final int i2 = i;
            if (LatencyTracker.isEnabled(KeyguardPatternView.this.mContext)) {
                LatencyTracker.getInstance(KeyguardPatternView.this.mContext).onActionStart(3);
                LatencyTracker.getInstance(KeyguardPatternView.this.mContext).onActionStart(4);
            }
            AnalyticsHelper.getInstance(KeyguardPatternView.this.mContext).trackPageStart("pw_unlock_time");
            final long currentTimeMillis = System.currentTimeMillis();
            KeyguardPatternView keyguardPatternView = KeyguardPatternView.this;
            AsyncTask unused = keyguardPatternView.mPendingLockCheck = LockPatternChecker.checkPatternForUsers(keyguardPatternView.mLockPatternUtils, list, currentUser, i2, keyguardPatternView.mContext, new OnCheckForUsersCallback() {
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
            AnalyticsHelper.getInstance(KeyguardPatternView.this.mContext).trackPageEnd("pw_verify_time");
            onPatternChecked(i, true, 0, true);
        }

        /* access modifiers changed from: private */
        public void onPatternChecked(int i, boolean z, int i2, boolean z2) {
            if (!z) {
                KeyguardPatternView.this.mLockPatternView.setDisplayMode(MiuiLockPatternView.DisplayMode.Wrong);
                if (z2) {
                    KeyguardPatternView.this.mCallback.reportUnlockAttempt(i, false, i2);
                    if (i2 > 0) {
                        FaceUnlockManager.getInstance().stopFaceUnlock();
                        KeyguardPatternView.this.handleAttemptLockout(KeyguardPatternView.this.mLockPatternUtils.setLockoutAttemptDeadline(i, i2));
                    }
                }
                ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).extHapticFeedback(76, true, 150);
                if (i2 == 0) {
                    KeyguardPatternView.this.mLockPatternView.postDelayed(KeyguardPatternView.this.mCancelPatternRunnable, 1500);
                }
                AnalyticsHelper.getInstance(KeyguardPatternView.this.mContext).recordUnlockWay("pw", false);
            } else if (KeyguardPatternView.this.allowUnlock(i)) {
                KeyguardPatternView.this.switchUser(i);
                KeyguardPatternView.this.mCallback.reportUnlockAttempt(i, true, 0);
                KeyguardPatternView.this.mLockPatternView.setDisplayMode(MiuiLockPatternView.DisplayMode.Correct);
                KeyguardPatternView.this.mCallback.dismiss(true, i);
                AnalyticsHelper.getInstance(KeyguardPatternView.this.mContext).recordUnlockWay("pw", true);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleWrongPassword() {
        this.mLockPatternView.setDisplayMode(MiuiLockPatternView.DisplayMode.Wrong);
        this.mVibrator.vibrate(150);
        this.mLockPatternView.postDelayed(this.mCancelPatternRunnable, 1500);
    }

    /* access modifiers changed from: private */
    public void handleAttemptLockout(long j) {
        long elapsedRealtime = j - SystemClock.elapsedRealtime();
        this.mCallback.handleAttemptLockout(elapsedRealtime);
        this.mLockPatternView.clearPattern();
        this.mLockPatternView.setEnabled(false);
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
        dismissFodView();
    }

    public void onResume(int i) {
        reset();
        showFodViewIfNeed();
    }

    public void showPromptReason(int i) {
        if (i != 0) {
            String promptReasonString = getPromptReasonString(i);
            if (!TextUtils.isEmpty(promptReasonString)) {
                this.mKeyguardBouncerMessageView.showMessage(this.mContext.getResources().getString(R.string.input_password_hint_text), promptReasonString);
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
            return resources.getString(R.string.input_password_after_boot_msg);
        }
        if (i == 2) {
            long requiredStrongAuthTimeout = getRequiredStrongAuthTimeout();
            return resources.getQuantityString(R.plurals.input_pattern_after_timeout_msg, (int) TimeUnit.MILLISECONDS.toHours(requiredStrongAuthTimeout), new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toHours(requiredStrongAuthTimeout))});
        } else if (i == 3) {
            return resources.getString(R.string.kg_prompt_reason_device_admin);
        } else {
            if (i != 4) {
                return resources.getString(R.string.kg_prompt_reason_timeout_pattern);
            }
            return resources.getString(R.string.kg_prompt_reason_user_request);
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
        float dimensionPixelSize = (float) getResources().getDimensionPixelSize(R.dimen.miui_keyguard_view_eca_text_size);
        this.mEmergencyButton.setTextSize(0, dimensionPixelSize);
        this.mBackButton.setTextSize(0, dimensionPixelSize);
    }

    /* access modifiers changed from: protected */
    public void handleConfigurationOrientationChanged() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mKeyguardBouncerMessageView.getLayoutParams();
        layoutParams.topMargin = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_bouncer_message_view_margin_top);
        this.mKeyguardBouncerMessageView.setLayoutParams(layoutParams);
    }

    private void setPositionForFod() {
        if (MiuiKeyguardUtils.isGxzwSensor() && MiuiGxzwManager.getInstance().isShowFodWithPassword()) {
            Display display = ((DisplayManager) getContext().getSystemService("display")).getDisplay(0);
            Point point = new Point();
            display.getRealSize(point);
            int max = Math.max(point.x, point.y);
            int dimensionPixelOffset = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_pattern_layout_height);
            int dimensionPixelOffset2 = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_pattern_view_pattern_view_height_width);
            int dimensionPixelOffset3 = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_pattern_view_pattern_view_margin_bottom);
            int dimensionPixelOffset4 = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_pattern_view_eca_height);
            int dimensionPixelOffset5 = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_pattern_view_eca_fod_top_margin);
            int i = max - (((dimensionPixelOffset - dimensionPixelOffset2) - dimensionPixelOffset3) - (dimensionPixelOffset4 / 2));
            Rect fodPosition = MiuiGxzwManager.getFodPosition(getContext());
            int height = fodPosition.top + (fodPosition.height() / 2);
            View findViewById = findViewById(R.id.container);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById.getLayoutParams();
            layoutParams.bottomMargin = i - height;
            layoutParams.height = dimensionPixelOffset + dimensionPixelOffset5;
            findViewById.setLayoutParams(layoutParams);
            View findViewById2 = findViewById(R.id.keyguard_selector_fade_container);
            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) findViewById2.getLayoutParams();
            layoutParams2.topMargin = dimensionPixelOffset5;
            findViewById2.setLayoutParams(layoutParams2);
        }
    }

    /* access modifiers changed from: protected */
    public void showFodViewIfNeed() {
        super.showFodViewIfNeed();
        if (MiuiKeyguardUtils.isGxzwSensor() && !MiuiGxzwManager.getInstance().isShowFodWithPassword()) {
            if (MiuiGxzwManager.getInstance().isShowFodInBouncer() && this.mLockPatternView.getVisibility() == 0) {
                onShowFodView();
                this.mLockPatternView.setVisibility(4);
                this.mLockPatternView.setEnabled(false);
            } else if (!MiuiGxzwManager.getInstance().isShowFodInBouncer()) {
                MiuiGxzwManager.getInstance().setDimissFodInBouncer(true);
                dismissFodView();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dismissFodView() {
        super.dismissFodView();
        if (MiuiKeyguardUtils.isGxzwSensor() && !MiuiGxzwManager.getInstance().isShowFodWithPassword() && this.mLockPatternView.getVisibility() != 0) {
            onDismissFodView();
            this.mLockPatternView.setVisibility(0);
            this.mLockPatternView.setEnabled(true);
        }
    }

    /* access modifiers changed from: protected */
    public void usePassword() {
        super.usePassword();
        startAppearAnimation();
    }
}
