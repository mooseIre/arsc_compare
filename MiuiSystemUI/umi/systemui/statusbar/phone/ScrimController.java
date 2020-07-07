package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.android.systemui.statusbar.stack.ViewState;
import com.android.systemui.util.ColorUtils;

public class ScrimController implements ViewTreeObserver.OnPreDrawListener, OnHeadsUpChangedListener {
    public static final Interpolator KEYGUARD_FADE_OUT_INTERPOLATOR = new PathInterpolator(0.0f, 0.0f, 0.7f, 1.0f);
    public static final Interpolator KEYGUARD_FADE_OUT_INTERPOLATOR_LOCKED = new PathInterpolator(0.3f, 0.0f, 0.8f, 1.0f);
    protected boolean mAnimateChange;
    private boolean mAnimateKeyguardFadingOut;
    private long mAnimationDelay;
    /* access modifiers changed from: private */
    public boolean mAodWaitUnlocking;
    protected boolean mBouncerIsKeyguard = false;
    protected boolean mBouncerShowing;
    private float mCurrentBehindAlpha = -1.0f;
    private float mCurrentHeadsUpAlpha = -1.0f;
    private float mCurrentInFrontAlpha = -1.0f;
    private boolean mDarkenWhileDragging;
    private boolean mDontAnimateBouncerChanges;
    private float mDozeBehindAlpha;
    private float mDozeInFrontAlpha;
    private boolean mDozing;
    private View mDraggedHeadsUpView;
    protected long mDurationOverride = -1;
    private boolean mForceHideScrims;
    private float mFraction;
    private final View mHeadsUpScrim;
    private final Interpolator mInterpolator = new DecelerateInterpolator();
    /* access modifiers changed from: private */
    public ValueAnimator mKeyguardFadeoutAnimation;
    /* access modifiers changed from: private */
    public boolean mKeyguardFadingOutInProgress;
    protected boolean mKeyguardShowing;
    /* access modifiers changed from: private */
    public final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final LightBarController mLightBarController;
    /* access modifiers changed from: private */
    public Runnable mOnAnimationFinished;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (ScrimController.this.mAodWaitUnlocking && ScrimController.this.mKeyguardUpdateMonitor.isDeviceInteractive()) {
                ScrimController.this.setAodWaitUnlocking(false);
            }
        }
    };
    private int mPinnedHeadsUpCount;
    protected final ScrimView mScrimBehind;
    protected float mScrimBehindAlpha;
    protected float mScrimBehindAlphaKeyguard = 0.45f;
    protected float mScrimBehindAlphaUnlocking = 0.2f;
    private final ScrimView mScrimInFront;
    private boolean mSkipFirstFrame;
    private boolean mSupportAmbientMode;
    private float mTopHeadsUpDragAmount;
    private boolean mTracking;
    private final UnlockMethodCache mUnlockMethodCache;
    private boolean mUpdatePending;
    private boolean mWakeAndUnlocking;

    public void onHeadsUpPinnedModeChanged(boolean z) {
    }

    public void onHeadsUpStateChanged(NotificationData.Entry entry, boolean z) {
    }

    public void setCurrentUser(int i) {
    }

    public ScrimController(LightBarController lightBarController, ScrimView scrimView, ScrimView scrimView2, View view) {
        this.mScrimBehind = scrimView;
        this.mScrimInFront = scrimView2;
        this.mHeadsUpScrim = view;
        Context context = scrimView.getContext();
        this.mUnlockMethodCache = UnlockMethodCache.getInstance(context);
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mLightBarController = lightBarController;
        this.mScrimBehindAlpha = context.getResources().getFloat(R.dimen.scrim_behind_alpha);
        updateHeadsUpScrim(false);
        updateScrims();
    }

    public void setKeyguardShowing(boolean z) {
        this.mKeyguardShowing = z;
        scheduleUpdate();
    }

    public void onTrackingStarted() {
        this.mTracking = true;
        this.mDarkenWhileDragging = true ^ this.mUnlockMethodCache.canSkipBouncer();
    }

    public void onExpandingFinished() {
        this.mTracking = false;
    }

    public void setPanelExpansion(float f) {
        if (this.mFraction != f) {
            this.mFraction = f;
            scheduleUpdate();
            if (this.mPinnedHeadsUpCount != 0) {
                updateHeadsUpScrim(false);
            }
            ValueAnimator valueAnimator = this.mKeyguardFadeoutAnimation;
            if (valueAnimator != null && this.mTracking) {
                valueAnimator.cancel();
            }
        }
    }

    public void setBouncerShowing(boolean z) {
        this.mBouncerShowing = z;
        this.mAnimateChange = !this.mTracking && !this.mDontAnimateBouncerChanges;
        scheduleUpdate();
    }

    public void setWakeAndUnlocking() {
        this.mWakeAndUnlocking = true;
        setAodWaitUnlocking(false);
        scheduleUpdate();
    }

    public void resetWakeAndUnlocking() {
        this.mWakeAndUnlocking = false;
    }

    public void setAodWaitUnlocking(boolean z) {
        if (this.mAodWaitUnlocking != z) {
            this.mAodWaitUnlocking = z;
            this.mScrimInFront.setOnClickListener(z ? this.mOnClickListener : null);
            updateScrims();
        }
    }

    public void animateKeyguardFadingOut(long j, long j2, Runnable runnable, boolean z) {
        this.mWakeAndUnlocking = false;
        this.mAnimateKeyguardFadingOut = true;
        this.mDurationOverride = j2;
        this.mAnimationDelay = j;
        this.mAnimateChange = true;
        this.mSkipFirstFrame = z;
        this.mOnAnimationFinished = runnable;
        if (!this.mKeyguardUpdateMonitor.needsSlowUnlockTransition()) {
            scheduleUpdate();
            onPreDraw();
            return;
        }
        this.mScrimInFront.postOnAnimationDelayed(new Runnable() {
            public void run() {
                ScrimController.this.scheduleUpdate();
            }
        }, 16);
    }

    public void abortKeyguardFadingOut() {
        if (this.mAnimateKeyguardFadingOut) {
            endAnimateKeyguardFadingOut(true);
        }
    }

    public void animateGoingToFullShade(long j, long j2) {
        this.mDurationOverride = j2;
        this.mAnimationDelay = j;
        this.mAnimateChange = true;
        scheduleUpdate();
    }

    public void setDozing(boolean z) {
        if (this.mDozing != z) {
            this.mDozing = z;
            scheduleUpdate();
        }
    }

    public void setDozeInFrontAlpha(float f) {
        this.mDozeInFrontAlpha = f;
        updateScrimColor(this.mScrimInFront);
    }

    public void setDozeBehindAlpha(float f) {
        this.mDozeBehindAlpha = f;
        updateScrimColor(this.mScrimBehind);
    }

    public float getDozeBehindAlpha() {
        return this.mDozeBehindAlpha;
    }

    public float getDozeInFrontAlpha() {
        return this.mDozeInFrontAlpha;
    }

    private float getScrimInFrontAlpha() {
        boolean needsSlowUnlockTransition = this.mKeyguardUpdateMonitor.needsSlowUnlockTransition();
        return 0.35f;
    }

    /* access modifiers changed from: protected */
    public void scheduleUpdate() {
        if (!this.mUpdatePending) {
            this.mScrimBehind.invalidate();
            this.mScrimBehind.getViewTreeObserver().addOnPreDrawListener(this);
            this.mUpdatePending = true;
        }
    }

    /* access modifiers changed from: protected */
    public void updateScrims() {
        if (this.mAnimateKeyguardFadingOut || this.mForceHideScrims) {
            setScrimInFrontColor(0.0f);
            setScrimBehindColor(0.0f);
        } else if (this.mAodWaitUnlocking) {
            setScrimInFrontColor(1.0f);
            setScrimBehindColor(0.0f);
        } else if (this.mWakeAndUnlocking) {
            if (this.mDozing) {
                setScrimInFrontColor(0.0f);
                setScrimBehindColor(1.0f);
            } else {
                setScrimInFrontColor(1.0f);
                setScrimBehindColor(0.0f);
            }
        } else if (this.mKeyguardShowing || this.mBouncerShowing) {
            updateScrimKeyguard();
        } else {
            updateScrimNormal();
            setScrimInFrontColor(0.0f);
        }
        this.mAnimateChange = false;
    }

    private void updateScrimKeyguard() {
        if (this.mTracking && this.mDarkenWhileDragging) {
            float max = Math.max(0.0f, Math.min(this.mFraction, 1.0f));
            float pow = (float) Math.pow((double) max, 0.800000011920929d);
            setScrimInFrontColor(((float) Math.pow((double) (1.0f - max), 0.800000011920929d)) * getScrimInFrontAlpha());
            setScrimBehindColor(pow * this.mScrimBehindAlphaKeyguard);
        } else if (this.mBouncerShowing && !this.mBouncerIsKeyguard) {
            setScrimInFrontColor(getScrimInFrontAlpha());
            updateScrimNormal();
        } else if (this.mBouncerShowing) {
            setScrimInFrontColor(0.0f);
            setScrimBehindColor(this.mScrimBehindAlpha);
        } else {
            float max2 = Math.max(0.0f, Math.min(this.mFraction, 1.0f));
            setScrimInFrontColor(0.0f);
            float f = this.mScrimBehindAlphaKeyguard;
            float f2 = this.mScrimBehindAlphaUnlocking;
            setScrimBehindColor((max2 * (f - f2)) + f2);
        }
    }

    private void updateScrimNormal() {
        float f = (this.mFraction * 1.2f) - 0.2f;
        if (f <= 0.0f) {
            setScrimBehindColor(0.0f);
        } else {
            setScrimBehindColor(((float) (1.0d - ((1.0d - Math.cos(Math.pow((double) (1.0f - f), 2.0d) * 3.141590118408203d)) * 0.5d))) * this.mScrimBehindAlpha);
        }
    }

    private void setScrimBehindColor(float f) {
        setScrimColor(this.mScrimBehind, f);
    }

    private void setScrimInFrontColor(float f) {
        setScrimColor(this.mScrimInFront, f);
        if (f == 0.0f) {
            this.mScrimInFront.setClickable(false);
        } else {
            this.mScrimInFront.setClickable(!this.mDozing);
        }
    }

    private void setScrimColor(View view, float f) {
        updateScrim(this.mAnimateChange, view, f, getCurrentScrimAlpha(view));
    }

    /* access modifiers changed from: protected */
    public float getDozeAlpha(View view) {
        return view == this.mScrimBehind ? this.mDozeBehindAlpha : this.mDozeInFrontAlpha;
    }

    /* access modifiers changed from: protected */
    public float getCurrentScrimAlpha(View view) {
        if (this.mSupportAmbientMode) {
            return 0.0f;
        }
        if (view == this.mScrimBehind) {
            return this.mCurrentBehindAlpha;
        }
        if (view == this.mScrimInFront) {
            return this.mCurrentInFrontAlpha;
        }
        return this.mCurrentHeadsUpAlpha;
    }

    /* access modifiers changed from: private */
    public void setCurrentScrimAlpha(View view, float f) {
        if (view == this.mScrimBehind) {
            this.mCurrentBehindAlpha = f;
            this.mLightBarController.setScrimAlpha(f);
        } else if (view == this.mScrimInFront) {
            this.mCurrentInFrontAlpha = f;
        } else {
            this.mCurrentHeadsUpAlpha = Math.max(0.0f, Math.min(1.0f, f));
        }
    }

    /* access modifiers changed from: protected */
    public void updateScrimColor(View view) {
        float currentScrimAlpha = getCurrentScrimAlpha(view);
        if (view instanceof ScrimView) {
            float max = Math.max(0.0f, Math.min(1.0f, 1.0f - ((1.0f - currentScrimAlpha) * (1.0f - getDozeAlpha(view)))));
            ScrimView scrimView = (ScrimView) view;
            scrimView.setScrimColor(ColorUtils.setAlphaComponent(scrimView.getScrimColor(), (int) (max * 255.0f)));
            return;
        }
        view.setAlpha(currentScrimAlpha);
    }

    private void startScrimAnimation(final View view, float f) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{getCurrentScrimAlpha(view), f});
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ScrimController.this.setCurrentScrimAlpha(view, ((Float) valueAnimator.getAnimatedValue()).floatValue());
                ScrimController.this.updateScrimColor(view);
            }
        });
        ofFloat.setInterpolator(getInterpolator());
        ofFloat.setStartDelay(this.mAnimationDelay);
        long j = this.mDurationOverride;
        if (j == -1) {
            j = 220;
        }
        ofFloat.setDuration(j);
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (ScrimController.this.mOnAnimationFinished != null) {
                    ScrimController.this.mOnAnimationFinished.run();
                    Runnable unused = ScrimController.this.mOnAnimationFinished = null;
                }
                if (ScrimController.this.mKeyguardFadingOutInProgress) {
                    ValueAnimator unused2 = ScrimController.this.mKeyguardFadeoutAnimation = null;
                    boolean unused3 = ScrimController.this.mKeyguardFadingOutInProgress = false;
                }
                view.setTag(R.id.scrim, (Object) null);
                view.setTag(R.id.scrim_target, (Object) null);
            }
        });
        ofFloat.start();
        if (this.mAnimateKeyguardFadingOut) {
            this.mKeyguardFadingOutInProgress = true;
            this.mKeyguardFadeoutAnimation = ofFloat;
        }
        if (this.mSkipFirstFrame) {
            ofFloat.setCurrentPlayTime(16);
        }
        view.setTag(R.id.scrim, ofFloat);
        view.setTag(R.id.scrim_target, Float.valueOf(f));
    }

    /* access modifiers changed from: protected */
    public Interpolator getInterpolator() {
        if (this.mAnimateKeyguardFadingOut && this.mKeyguardUpdateMonitor.needsSlowUnlockTransition()) {
            return KEYGUARD_FADE_OUT_INTERPOLATOR_LOCKED;
        }
        if (this.mAnimateKeyguardFadingOut) {
            return KEYGUARD_FADE_OUT_INTERPOLATOR;
        }
        return this.mInterpolator;
    }

    public boolean onPreDraw() {
        this.mScrimBehind.getViewTreeObserver().removeOnPreDrawListener(this);
        this.mUpdatePending = false;
        if (this.mDontAnimateBouncerChanges) {
            this.mDontAnimateBouncerChanges = false;
        }
        updateScrims();
        this.mDurationOverride = -1;
        this.mAnimationDelay = 0;
        this.mSkipFirstFrame = false;
        endAnimateKeyguardFadingOut(false);
        return true;
    }

    private void endAnimateKeyguardFadingOut(boolean z) {
        this.mAnimateKeyguardFadingOut = false;
        if (z || (!isAnimating(this.mScrimInFront) && !isAnimating(this.mScrimBehind))) {
            Runnable runnable = this.mOnAnimationFinished;
            if (runnable != null) {
                runnable.run();
                this.mOnAnimationFinished = null;
            }
            this.mKeyguardFadingOutInProgress = false;
        }
    }

    private boolean isAnimating(View view) {
        return view.getTag(R.id.scrim) != null;
    }

    public void setDrawBehindAsSrc(boolean z) {
        this.mScrimBehind.setDrawAsSrc(z);
    }

    public void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
        this.mPinnedHeadsUpCount++;
        updateHeadsUpScrim(true);
    }

    public void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
        this.mPinnedHeadsUpCount--;
        if (expandableNotificationRow == this.mDraggedHeadsUpView) {
            this.mDraggedHeadsUpView = null;
            this.mTopHeadsUpDragAmount = 0.0f;
        }
        updateHeadsUpScrim(true);
    }

    private void updateHeadsUpScrim(boolean z) {
        updateScrim(z, this.mHeadsUpScrim, calculateHeadsUpAlpha(), this.mCurrentHeadsUpAlpha);
    }

    private void updateScrim(boolean z, View view, float f, float f2) {
        if (!this.mKeyguardFadingOutInProgress || this.mKeyguardFadeoutAnimation.getCurrentPlayTime() == 0) {
            ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(view, R.id.scrim);
            float f3 = -1.0f;
            if (valueAnimator != null) {
                if (z || f == f2) {
                    valueAnimator.cancel();
                } else {
                    f3 = ((Float) ViewState.getChildTag(view, R.id.scrim_alpha_end)).floatValue();
                }
            }
            if (f != f2 && f != f3) {
                if (z) {
                    startScrimAnimation(view, f);
                    view.setTag(R.id.scrim_alpha_start, Float.valueOf(f2));
                    view.setTag(R.id.scrim_alpha_end, Float.valueOf(f));
                } else if (valueAnimator != null) {
                    float floatValue = ((Float) ViewState.getChildTag(view, R.id.scrim_alpha_start)).floatValue();
                    float floatValue2 = ((Float) ViewState.getChildTag(view, R.id.scrim_alpha_end)).floatValue();
                    PropertyValuesHolder[] values = valueAnimator.getValues();
                    float max = Math.max(0.0f, Math.min(1.0f, floatValue + (f - floatValue2)));
                    values[0].setFloatValues(new float[]{max, f});
                    view.setTag(R.id.scrim_alpha_start, Float.valueOf(max));
                    view.setTag(R.id.scrim_alpha_end, Float.valueOf(f));
                    valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
                } else {
                    setCurrentScrimAlpha(view, f);
                    updateScrimColor(view);
                }
            }
        }
    }

    private float calculateHeadsUpAlpha() {
        int i = this.mPinnedHeadsUpCount;
        return (i >= 2 ? 1.0f : i == 0 ? 0.0f : 1.0f - this.mTopHeadsUpDragAmount) * Math.max(1.0f - this.mFraction, 0.0f);
    }

    public void forceHideScrims(boolean z) {
        this.mForceHideScrims = z;
        this.mAnimateChange = false;
        scheduleUpdate();
    }

    public void dontAnimateBouncerChangesUntilNextFrame() {
        this.mDontAnimateBouncerChanges = true;
    }

    public void setExcludedBackgroundArea(Rect rect) {
        this.mScrimBehind.setExcludedArea(rect);
    }

    public int getScrimBehindColor() {
        return this.mScrimBehind.getScrimColorWithAlpha();
    }

    public void setScrimBehindChangeRunnable(Runnable runnable) {
        this.mScrimBehind.setChangeRunnable(runnable);
    }

    public void onDensityOrFontScaleChanged() {
        ViewGroup.LayoutParams layoutParams = this.mHeadsUpScrim.getLayoutParams();
        layoutParams.height = this.mHeadsUpScrim.getResources().getDimensionPixelSize(R.dimen.heads_up_scrim_height);
        this.mHeadsUpScrim.setLayoutParams(layoutParams);
    }

    public void setWallpaperSupportsAmbientMode(boolean z) {
        this.mSupportAmbientMode = z;
        updateScrims();
    }
}
