package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.notification.FakeShadowView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.phone.DoubleTapHelper;

public abstract class ActivatableNotificationView extends ExpandableOutlineView {
    private final AccessibilityManager mAccessibilityManager;
    protected boolean mActivated;
    private float mAnimationTranslationY;
    /* access modifiers changed from: private */
    public float mAppearAnimationFraction = -1.0f;
    private RectF mAppearAnimationRect = new RectF();
    private float mAppearAnimationTranslation;
    private ValueAnimator mAppearAnimator;
    /* access modifiers changed from: private */
    public ValueAnimator mBackgroundColorAnimator;
    protected NotificationBackgroundView mBackgroundDimmed;
    protected NotificationBackgroundView mBackgroundNormal;
    private ValueAnimator.AnimatorUpdateListener mBackgroundVisibilityUpdater = new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            ActivatableNotificationView activatableNotificationView = ActivatableNotificationView.this;
            activatableNotificationView.setNormalBackgroundVisibilityAmount(activatableNotificationView.mBackgroundNormal.getAlpha());
            ActivatableNotificationView activatableNotificationView2 = ActivatableNotificationView.this;
            float unused = activatableNotificationView2.mDimmedBackgroundFadeInAmount = activatableNotificationView2.mBackgroundDimmed.getAlpha();
        }
    };
    private float mBgAlpha = 1.0f;
    protected int mBgTint = 0;
    private Interpolator mCurrentAlphaInterpolator;
    private Interpolator mCurrentAppearInterpolator;
    private int mCurrentBackgroundTint;
    private boolean mDark;
    private boolean mDimmed;
    private int mDimmedAlpha;
    /* access modifiers changed from: private */
    public float mDimmedBackgroundFadeInAmount = -1.0f;
    private final DoubleTapHelper mDoubleTapHelper;
    private boolean mDrawingAppearAnimation;
    private AnimatorListenerAdapter mFadeInEndListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            super.onAnimationEnd(animator);
            ValueAnimator unused = ActivatableNotificationView.this.mFadeInFromDarkAnimator = null;
            float unused2 = ActivatableNotificationView.this.mDimmedBackgroundFadeInAmount = -1.0f;
            ActivatableNotificationView.this.updateBackground();
        }
    };
    /* access modifiers changed from: private */
    public ValueAnimator mFadeInFromDarkAnimator;
    private FakeShadowView mFakeShadow;
    /* access modifiers changed from: private */
    public FalsingManager mFalsingManager;
    private boolean mIsBelowSpeedBump;
    private final int mLowPriorityColor;
    private final int mLowPriorityRippleColor;
    private boolean mNeedsDimming;
    private float mNormalBackgroundVisibilityAmount;
    private final int mNormalColor;
    protected final int mNormalRippleColor;
    private OnActivatedListener mOnActivatedListener;
    private float mOverrideAmount;
    private int mOverrideTint;
    private float mShadowAlpha = 1.0f;
    private boolean mShadowHidden;
    private final Interpolator mSlowOutFastInInterpolator = new PathInterpolator(0.8f, 0.0f, 0.6f, 1.0f);
    private final Interpolator mSlowOutLinearInInterpolator = new PathInterpolator(0.8f, 0.0f, 1.0f, 1.0f);
    /* access modifiers changed from: private */
    public int mStartTint;
    private final Runnable mTapTimeoutRunnable = new Runnable() {
        public void run() {
            ActivatableNotificationView.this.makeInactive(true);
        }
    };
    /* access modifiers changed from: private */
    public int mTargetTint;
    private final int mTintedRippleColor;
    private ValueAnimator.AnimatorUpdateListener mUpdateOutlineListener = new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            ActivatableNotificationView.this.updateOutlineAlpha();
        }
    };
    private boolean mWasActivatedOnDown;

    public interface OnActivatedListener {
        void onActivated(ActivatableNotificationView activatableNotificationView);

        void onActivationReset(ActivatableNotificationView activatableNotificationView);
    }

    /* access modifiers changed from: protected */
    public boolean disallowSingleClick(MotionEvent motionEvent) {
        return false;
    }

    /* access modifiers changed from: protected */
    public abstract View getContentView();

    /* access modifiers changed from: protected */
    public boolean handleSlideBack() {
        return false;
    }

    public boolean isDimmable() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isInteractive() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void onAppearAnimationFinished(boolean z) {
    }

    /* access modifiers changed from: protected */
    public void onBelowSpeedBumpChanged() {
    }

    static {
        new PathInterpolator(0.6f, 0.0f, 0.5f, 1.0f);
        new PathInterpolator(0.0f, 0.0f, 0.5f, 1.0f);
    }

    public ActivatableNotificationView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setClipChildren(false);
        setClipToPadding(false);
        int color = context.getColor(R.color.notification_material_background_color);
        this.mNormalColor = color;
        this.mLowPriorityColor = color;
        this.mTintedRippleColor = context.getColor(R.color.notification_ripple_tinted_color);
        this.mLowPriorityRippleColor = context.getColor(R.color.notification_ripple_color_low_priority);
        this.mNormalRippleColor = context.getColor(R.color.notification_ripple_untinted_color);
        this.mFalsingManager = FalsingManager.getInstance(context);
        this.mAccessibilityManager = AccessibilityManager.getInstance(this.mContext);
        this.mDoubleTapHelper = new DoubleTapHelper(this, new DoubleTapHelper.ActivationListener() {
            public void onActiveChanged(boolean z) {
                if (z) {
                    ActivatableNotificationView.this.makeActive();
                } else {
                    ActivatableNotificationView.this.makeInactive(true);
                }
            }
        }, new DoubleTapHelper.DoubleTapListener() {
            public boolean onDoubleTap() {
                return ActivatableNotificationView.this.performClick();
            }
        }, new DoubleTapHelper.SlideBackListener() {
            public boolean onSlideBack() {
                return ActivatableNotificationView.this.handleSlideBack();
            }
        }, new DoubleTapHelper.DoubleTapLogListener() {
            public void onDoubleTapLog(boolean z, float f, float f2) {
                ActivatableNotificationView.this.mFalsingManager.onNotificationDoubleTap(z, f, f2);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mBackgroundNormal = (NotificationBackgroundView) findViewById(R.id.backgroundNormal);
        FakeShadowView fakeShadowView = (FakeShadowView) findViewById(R.id.fake_shadow);
        this.mFakeShadow = fakeShadowView;
        this.mShadowHidden = fakeShadowView.getVisibility() != 0;
        this.mBackgroundDimmed = (NotificationBackgroundView) findViewById(R.id.backgroundDimmed);
        this.mBackgroundNormal.setCustomBackground((int) R.drawable.notification_material_bg);
        this.mBackgroundDimmed.setCustomBackground((int) R.drawable.notification_material_bg_dim);
        this.mDimmedAlpha = Color.alpha(this.mContext.getColor(R.color.notification_material_background_dimmed_color));
        updateBackground();
        updateBackgroundTint();
        updateOutlineAlpha();
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!this.mNeedsDimming || this.mActivated || motionEvent.getActionMasked() != 0 || !disallowSingleClick(motionEvent) || isTouchExplorationEnabled()) {
            return super.onInterceptTouchEvent(motionEvent);
        }
        if (!isSummaryWithChildren() || !isGroupExpanded()) {
            return true;
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    private boolean isTouchExplorationEnabled() {
        return this.mAccessibilityManager.isTouchExplorationEnabled();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            this.mWasActivatedOnDown = this.mActivated;
        }
        if (this.mNeedsDimming && !this.mActivated && !isTouchExplorationEnabled() && isInteractive()) {
            boolean z = this.mActivated;
            boolean handleTouchEventDimmed = handleTouchEventDimmed(motionEvent);
            if (z && handleTouchEventDimmed && motionEvent.getAction() == 1) {
                removeCallbacks(this.mTapTimeoutRunnable);
            }
            return handleTouchEventDimmed;
        } else if (!isSummaryWithChildren() || !isGroupExpanded()) {
            return super.onTouchEvent(motionEvent);
        } else {
            return true;
        }
    }

    public void drawableHotspotChanged(float f, float f2) {
        if (!this.mDimmed) {
            this.mBackgroundNormal.drawableHotspotChanged(f, f2);
        }
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mDimmed) {
            this.mBackgroundDimmed.setState(getDrawableState());
        } else {
            this.mBackgroundNormal.setState(getDrawableState());
        }
    }

    private boolean handleTouchEventDimmed(MotionEvent motionEvent) {
        if (this.mNeedsDimming && !this.mDimmed) {
            super.onTouchEvent(motionEvent);
        }
        return this.mDoubleTapHelper.onTouchEvent(motionEvent, getActualHeight());
    }

    public boolean performClick() {
        if (this.mWasActivatedOnDown || !this.mNeedsDimming || isTouchExplorationEnabled()) {
            return super.performClick();
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void makeActive() {
        this.mFalsingManager.onNotificationActive();
        startActivateAnimation(false);
        this.mActivated = true;
        OnActivatedListener onActivatedListener = this.mOnActivatedListener;
        if (onActivatedListener != null) {
            onActivatedListener.onActivated(this);
        }
    }

    /* access modifiers changed from: protected */
    public void startActivateAnimation(boolean z) {
        AnimatorSet animatorSet;
        Interpolator interpolator;
        if (isAttachedToWindow() && isDimmable()) {
            if (z) {
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "scaleX", new float[]{1.0f});
                ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this, "scaleY", new float[]{1.0f});
                animatorSet = new AnimatorSet();
                animatorSet.play(ofFloat).with(ofFloat2);
            } else {
                ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(this, "scaleX", new float[]{1.05f});
                ObjectAnimator ofFloat4 = ObjectAnimator.ofFloat(this, "scaleY", new float[]{1.05f});
                animatorSet = new AnimatorSet();
                animatorSet.play(ofFloat3).with(ofFloat4);
            }
            if (!z) {
                interpolator = Interpolators.MIUI_ALPHA_IN;
            } else {
                interpolator = Interpolators.MIUI_ALPHA_OUT;
            }
            animatorSet.setInterpolator(interpolator);
            animatorSet.setDuration(180);
            if (z) {
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        ActivatableNotificationView.this.updateBackground();
                    }
                });
                animatorSet.start();
                return;
            }
            animatorSet.start();
        }
    }

    public void makeInactive(boolean z) {
        if (this.mActivated) {
            this.mActivated = false;
            if (this.mDimmed) {
                if (z) {
                    startActivateAnimation(true);
                } else {
                    updateBackground();
                }
            }
        }
        OnActivatedListener onActivatedListener = this.mOnActivatedListener;
        if (onActivatedListener != null) {
            onActivatedListener.onActivationReset(this);
        }
        removeCallbacks(this.mTapTimeoutRunnable);
    }

    public void setDimmed(boolean z, boolean z2) {
        this.mNeedsDimming = z;
        boolean isDimmable = z & isDimmable();
        if (this.mDimmed != isDimmable) {
            this.mDimmed = isDimmable;
            resetBackgroundAlpha();
            if (z2) {
                fadeDimmedBackground();
            } else {
                updateBackground();
            }
        }
    }

    public void setDark(boolean z, boolean z2, long j) {
        super.setDark(z, z2, j);
        if (this.mDark != z) {
            this.mDark = z;
            updateBackground();
            updateBackgroundTint(false);
            if (!z && z2 && !shouldHideBackground()) {
                fadeInFromDark(j);
            }
            updateOutlineAlpha();
        }
    }

    /* access modifiers changed from: protected */
    public void updateOutlineAlpha() {
        if (this.mDark) {
            setOutlineAlpha(0.0f);
            return;
        }
        float f = ((0.3f * this.mNormalBackgroundVisibilityAmount) + 0.7f) * this.mShadowAlpha;
        ValueAnimator valueAnimator = this.mFadeInFromDarkAnimator;
        if (valueAnimator != null) {
            f *= valueAnimator.getAnimatedFraction();
        }
        setOutlineAlpha(f);
    }

    public void setNormalBackgroundVisibilityAmount(float f) {
        this.mNormalBackgroundVisibilityAmount = f;
        updateOutlineAlpha();
    }

    public void setBelowSpeedBump(boolean z) {
        super.setBelowSpeedBump(z);
        if (z != this.mIsBelowSpeedBump) {
            this.mIsBelowSpeedBump = z;
            updateBackgroundTint();
            onBelowSpeedBumpChanged();
        }
    }

    public boolean isBelowSpeedBump() {
        return this.mIsBelowSpeedBump;
    }

    public void setTintColor(int i) {
        setTintColor(i, false);
    }

    public void setTintColor(int i, boolean z) {
        if (i != this.mBgTint) {
            this.mBgTint = i;
            updateBackgroundTint(z);
        }
    }

    public void setOverrideTintColor(int i, float f) {
        if (this.mDark) {
            i = 0;
            f = 0.0f;
        }
        this.mOverrideTint = i;
        this.mOverrideAmount = f;
        setBackgroundTintColor(calculateBgColor());
        if (isDimmable() || !this.mNeedsDimming) {
            this.mBackgroundNormal.setDrawableAlpha(255);
        } else {
            this.mBackgroundNormal.setDrawableAlpha((int) NotificationUtils.interpolate(255.0f, (float) this.mDimmedAlpha, f));
        }
    }

    /* access modifiers changed from: protected */
    public void updateBackgroundTint() {
        updateBackgroundTint(false);
    }

    private void updateBackgroundTint(boolean z) {
        ValueAnimator valueAnimator = this.mBackgroundColorAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        int rippleColor = getRippleColor();
        this.mBackgroundDimmed.setRippleColor(rippleColor);
        this.mBackgroundNormal.setRippleColor(rippleColor);
        int calculateBgColor = calculateBgColor();
        if (!z) {
            setBackgroundTintColor(calculateBgColor);
            return;
        }
        int i = this.mCurrentBackgroundTint;
        if (calculateBgColor != i) {
            this.mStartTint = i;
            this.mTargetTint = calculateBgColor;
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            this.mBackgroundColorAnimator = ofFloat;
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ActivatableNotificationView.this.setBackgroundTintColor(NotificationUtils.interpolateColors(ActivatableNotificationView.this.mStartTint, ActivatableNotificationView.this.mTargetTint, valueAnimator.getAnimatedFraction()));
                }
            });
            this.mBackgroundColorAnimator.setDuration(360);
            this.mBackgroundColorAnimator.setInterpolator(Interpolators.LINEAR);
            this.mBackgroundColorAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    ValueAnimator unused = ActivatableNotificationView.this.mBackgroundColorAnimator = null;
                }
            });
            this.mBackgroundColorAnimator.start();
        }
    }

    /* access modifiers changed from: private */
    public void setBackgroundTintColor(int i) {
        if (i != this.mCurrentBackgroundTint) {
            this.mCurrentBackgroundTint = i;
            if (i == this.mNormalColor) {
                i = 0;
            }
            this.mBackgroundDimmed.setTint(i);
            this.mBackgroundNormal.setTint(i);
        }
    }

    private void fadeInFromDark(long j) {
        final NotificationBackgroundView notificationBackgroundView = this.mDimmed ? this.mBackgroundDimmed : this.mBackgroundNormal;
        notificationBackgroundView.setAlpha(0.0f);
        this.mBackgroundVisibilityUpdater.onAnimationUpdate((ValueAnimator) null);
        notificationBackgroundView.animate().alpha(1.0f).setDuration(200).setStartDelay(j).setInterpolator(Interpolators.ALPHA_IN).setListener(new AnimatorListenerAdapter() {
            public void onAnimationCancel(Animator animator) {
                notificationBackgroundView.setAlpha(1.0f);
            }
        }).setUpdateListener(this.mBackgroundVisibilityUpdater).start();
        ValueAnimator ofFloat = TimeAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mFadeInFromDarkAnimator = ofFloat;
        ofFloat.setDuration(200);
        this.mFadeInFromDarkAnimator.setStartDelay(j);
        this.mFadeInFromDarkAnimator.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        this.mFadeInFromDarkAnimator.addListener(this.mFadeInEndListener);
        this.mFadeInFromDarkAnimator.addUpdateListener(this.mUpdateOutlineListener);
        this.mFadeInFromDarkAnimator.start();
    }

    private void fadeDimmedBackground() {
        this.mBackgroundDimmed.animate().cancel();
        this.mBackgroundNormal.animate().cancel();
        if (this.mActivated) {
            updateBackground();
        } else if (shouldHideBackground()) {
        } else {
            if (this.mDimmed) {
                this.mBackgroundDimmed.setVisibility(0);
            } else {
                this.mBackgroundNormal.setVisibility(0);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateBackgroundAlpha(float f) {
        if (!isChildInGroup() || !this.mDimmed) {
            f = 1.0f;
        }
        this.mBgAlpha = f;
        float f2 = this.mDimmedBackgroundFadeInAmount;
        if (f2 != -1.0f) {
            this.mBgAlpha = f * f2;
        }
        this.mBackgroundDimmed.setAlpha(this.mBgAlpha);
    }

    /* access modifiers changed from: protected */
    public void resetBackgroundAlpha() {
        updateBackgroundAlpha(0.0f);
    }

    /* access modifiers changed from: protected */
    public void updateBackground() {
        cancelFadeAnimations();
        float f = 1.0f;
        int i = 4;
        if (shouldHideBackground()) {
            this.mBackgroundDimmed.setVisibility(4);
            NotificationBackgroundView notificationBackgroundView = this.mBackgroundNormal;
            if (this.mActivated) {
                i = 0;
            }
            notificationBackgroundView.setVisibility(i);
        } else if (this.mDimmed) {
            boolean z = isGroupExpansionChanging() && isChildInGroup();
            this.mBackgroundDimmed.setVisibility(z ? 4 : 0);
            NotificationBackgroundView notificationBackgroundView2 = this.mBackgroundNormal;
            if (this.mActivated || z) {
                i = 0;
            }
            notificationBackgroundView2.setVisibility(i);
        } else {
            this.mBackgroundDimmed.setVisibility(4);
            this.mBackgroundNormal.setVisibility(0);
            this.mBackgroundNormal.setAlpha(1.0f);
            removeCallbacks(this.mTapTimeoutRunnable);
            makeInactive(false);
        }
        if (this.mBackgroundNormal.getVisibility() != 0) {
            f = 0.0f;
        }
        setNormalBackgroundVisibilityAmount(f);
    }

    /* access modifiers changed from: protected */
    public boolean shouldHideBackground() {
        return this.mDark;
    }

    private void cancelFadeAnimations() {
        this.mBackgroundDimmed.animate().cancel();
        this.mBackgroundNormal.animate().cancel();
        animate().cancel();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        setPivotX((float) (getWidth() / 2));
    }

    public void setActualHeight(int i, boolean z) {
        super.setActualHeight(i, z);
        setPivotY((float) (i / 2));
        this.mBackgroundNormal.setActualHeight(i);
        this.mBackgroundDimmed.setActualHeight(i);
    }

    public void setClipTopAmount(int i) {
        super.setClipTopAmount(i);
        this.mBackgroundNormal.setClipTopAmount(i);
        this.mBackgroundDimmed.setClipTopAmount(i);
    }

    public void setClipBottomAmount(int i) {
        super.setClipBottomAmount(i);
        this.mBackgroundNormal.setClipBottomAmount(i);
        this.mBackgroundDimmed.setClipBottomAmount(i);
    }

    public void performRemoveAnimation(long j, float f, AnimatorListenerAdapter animatorListenerAdapter, Runnable runnable) {
        enableAppearDrawing(true);
        if (this.mDrawingAppearAnimation) {
            startAppearAnimation(false, f, 0, j, animatorListenerAdapter, runnable);
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public void performAddAnimation(long j, long j2, AnimatorListenerAdapter animatorListenerAdapter) {
        enableAppearDrawing(true);
        if (this.mDrawingAppearAnimation) {
            startAppearAnimation(true, -1.0f, j, j2, animatorListenerAdapter, (Runnable) null);
        }
    }

    private void startAppearAnimation(final boolean z, float f, long j, long j2, AnimatorListenerAdapter animatorListenerAdapter, final Runnable runnable) {
        cancelAppearAnimation();
        float actualHeight = f * ((float) getActualHeight());
        this.mAnimationTranslationY = actualHeight;
        float f2 = 1.0f;
        if (this.mAppearAnimationFraction == -1.0f) {
            if (z) {
                this.mAppearAnimationFraction = 0.0f;
                this.mAppearAnimationTranslation = actualHeight;
            } else {
                this.mAppearAnimationFraction = 1.0f;
                this.mAppearAnimationTranslation = 0.0f;
            }
        }
        if (z) {
            this.mCurrentAppearInterpolator = this.mSlowOutFastInInterpolator;
            this.mCurrentAlphaInterpolator = Interpolators.LINEAR_OUT_SLOW_IN;
        } else {
            this.mCurrentAppearInterpolator = Interpolators.FAST_OUT_SLOW_IN;
            this.mCurrentAlphaInterpolator = this.mSlowOutLinearInInterpolator;
            f2 = 0.0f;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mAppearAnimationFraction, f2});
        this.mAppearAnimator = ofFloat;
        ofFloat.setInterpolator(Interpolators.LINEAR);
        this.mAppearAnimator.setDuration((long) (((float) j2) * Math.abs(this.mAppearAnimationFraction - f2)));
        this.mAppearAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float unused = ActivatableNotificationView.this.mAppearAnimationFraction = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                ActivatableNotificationView.this.updateAppearAnimationAlpha();
                ActivatableNotificationView.this.updateAppearRect();
                ActivatableNotificationView.this.invalidate();
            }
        });
        if (j > 0) {
            updateAppearAnimationAlpha();
            updateAppearRect();
            this.mAppearAnimator.setStartDelay(j);
        }
        if (animatorListenerAdapter != null) {
            this.mAppearAnimator.addListener(animatorListenerAdapter);
        }
        this.mAppearAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean mWasCancelled;

            public void onAnimationEnd(Animator animator) {
                Runnable runnable = runnable;
                if (runnable != null) {
                    runnable.run();
                }
                if (!this.mWasCancelled) {
                    ActivatableNotificationView.this.enableAppearDrawing(false);
                    ActivatableNotificationView.this.onAppearAnimationFinished(z);
                }
            }

            public void onAnimationStart(Animator animator) {
                this.mWasCancelled = false;
            }

            public void onAnimationCancel(Animator animator) {
                this.mWasCancelled = true;
            }
        });
        this.mAppearAnimator.start();
    }

    private void cancelAppearAnimation() {
        ValueAnimator valueAnimator = this.mAppearAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.mAppearAnimator = null;
        }
    }

    /* access modifiers changed from: private */
    public void updateAppearRect() {
        float f;
        float f2;
        float f3 = 1.0f - this.mAppearAnimationFraction;
        float interpolation = this.mCurrentAppearInterpolator.getInterpolation(f3) * this.mAnimationTranslationY;
        this.mAppearAnimationTranslation = interpolation;
        float f4 = f3 - 0.0f;
        float width = ((float) getWidth()) * 0.475f * this.mCurrentAppearInterpolator.getInterpolation(Math.min(1.0f, Math.max(0.0f, f4 / 0.8f)));
        float width2 = ((float) getWidth()) - width;
        float interpolation2 = this.mCurrentAppearInterpolator.getInterpolation(Math.max(0.0f, f4 / 1.0f));
        int actualHeight = getActualHeight();
        float f5 = this.mAnimationTranslationY;
        if (f5 > 0.0f) {
            f = (((float) actualHeight) - ((f5 * interpolation2) * 0.1f)) - interpolation;
            f2 = interpolation2 * f;
        } else {
            float f6 = (float) actualHeight;
            float f7 = (((f5 + f6) * interpolation2) * 0.1f) - interpolation;
            f = (f6 * (1.0f - interpolation2)) + (interpolation2 * f7);
            f2 = f7;
        }
        this.mAppearAnimationRect.set(width, f2, width2, f);
        float f8 = this.mAppearAnimationTranslation;
        setOutlineRect(width, f2 + f8, width2, f + f8);
    }

    /* access modifiers changed from: private */
    public void updateAppearAnimationAlpha() {
        float interpolation = this.mCurrentAlphaInterpolator.getInterpolation(Math.min(1.0f, this.mAppearAnimationFraction / 1.0f));
        setAlphaWithLayer(getContentView(), interpolation);
        setAlphaWithLayer(this.mBackgroundNormal, interpolation);
    }

    private void setAlphaWithLayer(View view, float f) {
        if (view.hasOverlappingRendering()) {
            int i = (f == 0.0f || f == 1.0f) ? 0 : 2;
            if (view.getLayerType() != i) {
                view.setLayerType(i, (Paint) null);
            }
        }
        view.setAlpha(f);
    }

    public int calculateBgColor() {
        return calculateBgColor(true, true);
    }

    private int calculateBgColor(boolean z, boolean z2) {
        int i;
        if (z && this.mDark) {
            return getContext().getColor(R.color.notification_material_background_dark_color);
        }
        if (z2 && this.mOverrideTint != 0) {
            return NotificationUtils.interpolateColors(calculateBgColor(z, false), this.mOverrideTint, this.mOverrideAmount);
        }
        if (z && (i = this.mBgTint) != 0) {
            return i;
        }
        if (this.mIsBelowSpeedBump) {
            return this.mLowPriorityColor;
        }
        return this.mNormalColor;
    }

    /* access modifiers changed from: protected */
    public int getRippleColor() {
        if (this.mBgTint != 0) {
            return this.mTintedRippleColor;
        }
        if (this.mIsBelowSpeedBump) {
            return this.mLowPriorityRippleColor;
        }
        return this.mNormalRippleColor;
    }

    /* access modifiers changed from: private */
    public void enableAppearDrawing(boolean z) {
        if (z != this.mDrawingAppearAnimation) {
            this.mDrawingAppearAnimation = z;
            if (!z) {
                setAlphaWithLayer(getContentView(), 1.0f);
                this.mAppearAnimationFraction = -1.0f;
                setOutlineRect((RectF) null);
            }
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        if (this.mDrawingAppearAnimation) {
            canvas.save();
            canvas.translate(0.0f, this.mAppearAnimationTranslation);
        }
        super.dispatchDraw(canvas);
        if (this.mDrawingAppearAnimation) {
            canvas.restore();
        }
    }

    public void setOnActivatedListener(OnActivatedListener onActivatedListener) {
        this.mOnActivatedListener = onActivatedListener;
    }

    public float getShadowAlpha() {
        return this.mShadowAlpha;
    }

    public void setShadowAlpha(float f) {
        if (f != this.mShadowAlpha) {
            this.mShadowAlpha = f;
            updateOutlineAlpha();
        }
    }

    public void setFakeShadowIntensity(float f, float f2, int i, int i2) {
        boolean z = this.mShadowHidden;
        boolean z2 = f == 0.0f;
        this.mShadowHidden = z2;
        if (!z2 || !z) {
            this.mFakeShadow.setFakeShadowTranslationZ(f * (getTranslationZ() + 0.1f), f2, i, i2);
        }
    }

    public int getBackgroundColorWithoutTint() {
        return calculateBgColor(false, false);
    }
}
