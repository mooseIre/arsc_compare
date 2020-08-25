package com.android.keyguard.charge.rapid;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.charge.MiuiChargeManager;
import com.android.keyguard.charge.rapid.PercentCountView;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.plugins.R;
import miui.maml.animation.interpolater.CubicEaseOutInterpolater;
import miui.maml.animation.interpolater.QuartEaseOutInterpolater;

public class RapidChargeView extends FrameLayout implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
    /* access modifiers changed from: private */
    public IRapidAnimationListener animationListener;
    protected View mCenterAnchorView;
    private PercentCountView.ChargeLevelAnimationListener mChargeLevelAnimationListener;
    protected int mChargeNumberTranslateInit;
    protected int mChargeNumberTranslateSmall;
    protected int mChargeState;
    protected int mChargeTipTranslateSmall;
    private boolean mClickShowChargeUI;
    protected ViewGroup mContentContainer;
    private AnimatorSet mContentSwitchAnimator;
    private Interpolator mCubicInterpolator;
    protected AnimatorSet mDismissAnimatorSet;
    /* access modifiers changed from: private */
    public String mDismissReason;
    /* access modifiers changed from: private */
    public final Runnable mDismissRunnable;
    protected AnimatorSet mEnterAnimatorSet;
    protected GTChargeAniView mGtChargeAniView;
    /* access modifiers changed from: private */
    public Handler mHandler;
    protected int mIconPaddingTop;
    private boolean mInitScreenOn;
    protected boolean mIsScreenOn;
    protected PercentCountView mPercentCountView;
    private int mPivotX;
    protected Interpolator mQuartOutInterpolator;
    protected ImageView mRapidIcon;
    private Drawable mRapidIconDrawable;
    private int mRapidIconHeight;
    private int mRapidIconWidth;
    protected Point mScreenSize;
    protected int mSpaceHeight;
    protected int mSpeedTipTextSizePx;
    /* access modifiers changed from: private */
    public boolean mStartingDismissWirelessAlphaAnim;
    protected TextView mStateTip;
    private Drawable mStrongSuperRapidIconDrawable;
    protected ImageView mSuperRapidIcon;
    private Drawable mSuperRapidIconDrawable;
    private int mSuperRapidIconHeight;
    private int mSuperRapidIconWidth;
    /* access modifiers changed from: private */
    public Runnable mTimeoutDismissJob;
    protected int mTipTopMargin;
    protected WindowManager mWindowManager;
    protected boolean mWindowShouldAdd;

    /* access modifiers changed from: protected */
    public void addChildView() {
    }

    public void addToWindow(String str) {
    }

    /* access modifiers changed from: protected */
    public void disableTouch(boolean z) {
    }

    /* access modifiers changed from: protected */
    public void hideSystemUI() {
    }

    /* access modifiers changed from: protected */
    public void initAnimator() {
    }

    /* access modifiers changed from: protected */
    public boolean isPercentViewShown() {
        return true;
    }

    public void onAnimationCancel(Animator animator) {
    }

    public void onAnimationEnd(Animator animator) {
    }

    public void onAnimationRepeat(Animator animator) {
    }

    public void removeFromWindow(String str) {
    }

    /* access modifiers changed from: protected */
    public void setComponentTransparent(boolean z) {
    }

    public boolean shouldDismiss() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void showNormalChargeAnim() {
    }

    /* access modifiers changed from: protected */
    public void showRapidChargeAnim() {
    }

    /* access modifiers changed from: protected */
    public void showStrongRapidChargeAnim() {
    }

    /* access modifiers changed from: protected */
    public void stopChildAnimation() {
    }

    /* access modifiers changed from: protected */
    public void zoomLargeOnChildView() {
    }

    public RapidChargeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RapidChargeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mHandler = new Handler();
        this.mCubicInterpolator = new CubicEaseOutInterpolater();
        this.mQuartOutInterpolator = new QuartEaseOutInterpolater();
        this.mChargeLevelAnimationListener = new PercentCountView.ChargeLevelAnimationListener() {
            public void onChargeLevelAnimationEnd() {
                RapidChargeView.this.mHandler.removeCallbacks(RapidChargeView.this.mTimeoutDismissJob);
                RapidChargeView.this.startDismiss("dismiss_for_value_animation_end");
            }
        };
        this.mDismissRunnable = new Runnable() {
            public void run() {
                RapidChargeView.this.stopChildAnimation();
                RapidChargeView.this.mPercentCountView.stopValueAnimation();
                RapidChargeView.this.setComponentTransparent(true);
                RapidChargeView.this.disableTouch(true);
                RapidChargeView.this.dismissView();
                if (RapidChargeView.this.animationListener != null) {
                    RapidChargeView.this.animationListener.onRapidAnimationDismiss(11, RapidChargeView.this.mDismissReason);
                }
            }
        };
        this.mTimeoutDismissJob = new Runnable() {
            public void run() {
                RapidChargeView.this.startDismiss("dismiss_for_timeout");
            }
        };
        init(context);
    }

    /* access modifiers changed from: protected */
    public void init(Context context) {
        this.mRapidIconDrawable = context.getDrawable(R.drawable.charge_animation_rapid_charge_icon);
        this.mSuperRapidIconDrawable = context.getDrawable(R.drawable.charge_animation_super_rapid_charge_icon);
        this.mStrongSuperRapidIconDrawable = context.getDrawable(R.drawable.charge_animation_strong_super_rapid_charge_icon);
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mScreenSize = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        updateSizeForScreenSizeChange();
        this.mChargeState = 0;
        setBackgroundColor(Color.argb(242, 0, 0, 0));
        hideSystemUI();
        this.mContentContainer = new RelativeLayout(context);
        this.mCenterAnchorView = new TextView(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-2, this.mSpaceHeight);
        layoutParams.addRule(13);
        this.mCenterAnchorView.setId(View.generateViewId());
        this.mContentContainer.addView(this.mCenterAnchorView, layoutParams);
        addChildView();
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams2.addRule(13);
        PercentCountView percentCountView = new PercentCountView(context);
        this.mPercentCountView = percentCountView;
        percentCountView.setTranslationY((float) this.mChargeNumberTranslateInit);
        this.mPercentCountView.setChargeLevelAnimationListener(this.mChargeLevelAnimationListener);
        this.mContentContainer.addView(this.mPercentCountView, layoutParams2);
        AccessibilityDisableTextView accessibilityDisableTextView = new AccessibilityDisableTextView(context);
        this.mStateTip = accessibilityDisableTextView;
        accessibilityDisableTextView.setTextSize(0, (float) this.mSpeedTipTextSizePx);
        this.mStateTip.setIncludeFontPadding(false);
        this.mStateTip.setTextColor(Color.parseColor("#8CFFFFFF"));
        this.mStateTip.setGravity(17);
        this.mStateTip.setText(getResources().getString(R.string.rapid_charge_mode_tip));
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams3.addRule(14);
        layoutParams3.addRule(3, this.mCenterAnchorView.getId());
        layoutParams3.topMargin = this.mTipTopMargin;
        this.mContentContainer.addView(this.mStateTip, layoutParams3);
        this.mGtChargeAniView = new GTChargeAniView(context);
        RelativeLayout.LayoutParams layoutParams4 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams4.addRule(14);
        layoutParams4.addRule(3, this.mCenterAnchorView.getId());
        layoutParams4.topMargin = this.mTipTopMargin;
        this.mGtChargeAniView.setVisibility(8);
        this.mGtChargeAniView.setViewInitState();
        this.mContentContainer.addView(this.mGtChargeAniView, layoutParams4);
        ImageView imageView = new ImageView(context);
        this.mRapidIcon = imageView;
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        this.mRapidIcon.setImageDrawable(this.mRapidIconDrawable);
        RelativeLayout.LayoutParams layoutParams5 = new RelativeLayout.LayoutParams(this.mRapidIconWidth, this.mRapidIconHeight + this.mIconPaddingTop);
        layoutParams5.addRule(13);
        this.mRapidIcon.setPadding(0, this.mIconPaddingTop, 0, 0);
        this.mRapidIcon.setPivotX((float) this.mPivotX);
        this.mContentContainer.addView(this.mRapidIcon, layoutParams5);
        ImageView imageView2 = new ImageView(context);
        this.mSuperRapidIcon = imageView2;
        imageView2.setScaleType(ImageView.ScaleType.FIT_CENTER);
        this.mSuperRapidIcon.setImageDrawable(this.mSuperRapidIconDrawable);
        RelativeLayout.LayoutParams layoutParams6 = new RelativeLayout.LayoutParams(this.mSuperRapidIconWidth, this.mSuperRapidIconHeight + this.mIconPaddingTop);
        layoutParams6.addRule(13);
        this.mSuperRapidIcon.setPadding(0, this.mIconPaddingTop, 0, 0);
        this.mSuperRapidIcon.setPivotX((float) this.mPivotX);
        this.mContentContainer.addView(this.mSuperRapidIcon, layoutParams6);
        addView(this.mContentContainer, getContainerLayoutParams());
        setElevation(30.0f);
    }

    /* access modifiers changed from: protected */
    public RelativeLayout.LayoutParams getContainerLayoutParams() {
        return new RelativeLayout.LayoutParams(-1, -1);
    }

    /* access modifiers changed from: protected */
    public void setChargeState(int i) {
        if (i != this.mChargeState) {
            Log.i("RapidChargeView", "setChargeState: " + i);
            this.mChargeState = i;
            post(new Runnable() {
                public void run() {
                    RapidChargeView.this.startContentSwitchAnimation();
                }
            });
        }
    }

    public void setChargeState(boolean z, boolean z2, boolean z3) {
        setChargeState(z3 ? 3 : z2 ? 2 : z ? 1 : 0);
    }

    /* access modifiers changed from: protected */
    public void startContentSwitchAnimation() {
        int i = this.mChargeState;
        if (i == 0) {
            showNormalChargeAnim();
            switchToNormal();
        } else if (i == 1) {
            this.mSuperRapidIcon.setImageDrawable(this.mRapidIconDrawable);
            showRapidChargeAnim();
            switchToRapid();
        } else if (i == 2) {
            this.mSuperRapidIcon.setImageDrawable(this.mSuperRapidIconDrawable);
            showRapidChargeAnim();
            switchToSuperRapid();
        } else if (i == 3) {
            this.mSuperRapidIcon.setImageDrawable(this.mStrongSuperRapidIconDrawable);
            showStrongRapidChargeAnim();
            switchToSuperRapid();
        }
    }

    private void switchToNormal() {
        animateToHideIcon();
    }

    private void switchToRapid() {
        animateToShowRapidIcon();
    }

    private void switchToSuperRapid() {
        animateToShowSuperRapidIcon();
    }

    private void animateToHideIcon() {
        Property property = FrameLayout.TRANSLATION_Y;
        Property property2 = FrameLayout.SCALE_Y;
        Property property3 = FrameLayout.SCALE_X;
        Property property4 = FrameLayout.ALPHA;
        Log.i("RapidChargeView", "animateToHideIcon: ");
        AnimatorSet animatorSet = this.mContentSwitchAnimator;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property3, new float[]{this.mPercentCountView.getScaleX(), 1.0f});
        PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mPercentCountView.getScaleY(), 1.0f});
        PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(property, new float[]{this.mPercentCountView.getTranslationY(), (float) this.mChargeNumberTranslateInit});
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this.mPercentCountView, new PropertyValuesHolder[]{ofFloat, ofFloat2, ofFloat3}).setDuration(500);
        PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(property, new float[]{this.mStateTip.getTranslationY(), 0.0f});
        PropertyValuesHolder ofFloat5 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mStateTip.getAlpha(), 0.0f});
        ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mStateTip, new PropertyValuesHolder[]{ofFloat5, ofFloat4}).setDuration(500);
        PropertyValuesHolder ofFloat6 = PropertyValuesHolder.ofFloat(property, new float[]{this.mGtChargeAniView.getTranslationY(), 0.0f});
        PropertyValuesHolder ofFloat7 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mGtChargeAniView.getAlpha(), 0.0f});
        ObjectAnimator duration3 = ObjectAnimator.ofPropertyValuesHolder(this.mGtChargeAniView, new PropertyValuesHolder[]{ofFloat7, ofFloat6}).setDuration(500);
        PropertyValuesHolder ofFloat8 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mRapidIcon.getScaleX(), 0.0f});
        PropertyValuesHolder ofFloat9 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mRapidIcon.getScaleY(), 0.0f});
        PropertyValuesHolder ofFloat10 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mRapidIcon.getAlpha(), 0.0f});
        ObjectAnimator duration4 = ObjectAnimator.ofPropertyValuesHolder(this.mRapidIcon, new PropertyValuesHolder[]{ofFloat8, ofFloat9, ofFloat10}).setDuration(500);
        PropertyValuesHolder ofFloat11 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mSuperRapidIcon.getScaleX(), 0.0f});
        PropertyValuesHolder ofFloat12 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mSuperRapidIcon.getScaleY(), 0.0f});
        PropertyValuesHolder ofFloat13 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mSuperRapidIcon.getAlpha(), 0.0f});
        ObjectAnimator duration5 = ObjectAnimator.ofPropertyValuesHolder(this.mSuperRapidIcon, new PropertyValuesHolder[]{ofFloat11, ofFloat12, ofFloat13}).setDuration(500);
        AnimatorSet animatorSet2 = new AnimatorSet();
        this.mContentSwitchAnimator = animatorSet2;
        animatorSet2.setInterpolator(this.mCubicInterpolator);
        this.mContentSwitchAnimator.playTogether(new Animator[]{duration, duration2, duration3, duration4, duration5});
        this.mContentSwitchAnimator.start();
    }

    /* access modifiers changed from: protected */
    public void animateToShowRapidIcon() {
        Property property = FrameLayout.TRANSLATION_Y;
        Property property2 = FrameLayout.SCALE_Y;
        Property property3 = FrameLayout.SCALE_X;
        Property property4 = FrameLayout.ALPHA;
        if (isPercentViewShown()) {
            Log.i("RapidChargeView", "animateToShowRapidIcon: ");
            AnimatorSet animatorSet = this.mContentSwitchAnimator;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property3, new float[]{this.mPercentCountView.getScaleX(), 0.85f});
            PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mPercentCountView.getScaleY(), 0.85f});
            PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(property, new float[]{this.mPercentCountView.getTranslationY(), (float) this.mChargeNumberTranslateSmall});
            ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this.mPercentCountView, new PropertyValuesHolder[]{ofFloat, ofFloat2, ofFloat3}).setDuration(500);
            duration.setInterpolator(this.mCubicInterpolator);
            PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(property, new float[]{this.mStateTip.getTranslationY(), (float) this.mChargeTipTranslateSmall});
            PropertyValuesHolder ofFloat5 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mStateTip.getAlpha(), 1.0f});
            ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mStateTip, new PropertyValuesHolder[]{ofFloat5, ofFloat4}).setDuration(500);
            duration2.setInterpolator(this.mCubicInterpolator);
            PropertyValuesHolder ofFloat6 = PropertyValuesHolder.ofFloat(property, new float[]{this.mGtChargeAniView.getTranslationY(), (float) this.mChargeTipTranslateSmall});
            PropertyValuesHolder ofFloat7 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mGtChargeAniView.getAlpha(), 0.0f});
            ObjectAnimator duration3 = ObjectAnimator.ofPropertyValuesHolder(this.mGtChargeAniView, new PropertyValuesHolder[]{ofFloat7, ofFloat6}).setDuration(250);
            duration3.setInterpolator(this.mCubicInterpolator);
            PropertyValuesHolder ofFloat8 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mRapidIcon.getScaleX(), 1.0f});
            PropertyValuesHolder ofFloat9 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mRapidIcon.getScaleY(), 1.0f});
            PropertyValuesHolder ofFloat10 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mRapidIcon.getAlpha(), 1.0f});
            ObjectAnimator duration4 = ObjectAnimator.ofPropertyValuesHolder(this.mRapidIcon, new PropertyValuesHolder[]{ofFloat8, ofFloat9, ofFloat10}).setDuration(500);
            duration4.setInterpolator(this.mCubicInterpolator);
            PropertyValuesHolder ofFloat11 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mSuperRapidIcon.getScaleX(), 0.0f});
            PropertyValuesHolder ofFloat12 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mSuperRapidIcon.getScaleY(), 0.0f});
            PropertyValuesHolder ofFloat13 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mSuperRapidIcon.getAlpha(), 0.0f});
            ObjectAnimator duration5 = ObjectAnimator.ofPropertyValuesHolder(this.mSuperRapidIcon, new PropertyValuesHolder[]{ofFloat11, ofFloat12, ofFloat13}).setDuration(500);
            duration5.setInterpolator(this.mCubicInterpolator);
            duration4.setInterpolator(new OvershootInterpolator(3.0f));
            AnimatorSet animatorSet2 = new AnimatorSet();
            this.mContentSwitchAnimator = animatorSet2;
            animatorSet2.playTogether(new Animator[]{duration, duration2, duration3, duration4, duration5});
            this.mContentSwitchAnimator.start();
        }
    }

    /* access modifiers changed from: protected */
    public void animateToShowSuperRapidIcon() {
        Property property = FrameLayout.TRANSLATION_Y;
        Property property2 = FrameLayout.SCALE_Y;
        Property property3 = FrameLayout.SCALE_X;
        Property property4 = FrameLayout.ALPHA;
        if (isPercentViewShown()) {
            Log.i("RapidChargeView", "animateToShowSuperRapidIcon: ");
            AnimatorSet animatorSet = this.mContentSwitchAnimator;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property3, new float[]{this.mPercentCountView.getScaleX(), 0.85f});
            PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mPercentCountView.getScaleY(), 0.85f});
            PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(property, new float[]{this.mPercentCountView.getTranslationY(), (float) this.mChargeNumberTranslateSmall});
            ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this.mPercentCountView, new PropertyValuesHolder[]{ofFloat, ofFloat2, ofFloat3}).setDuration(500);
            duration.setInterpolator(this.mCubicInterpolator);
            PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(property, new float[]{this.mStateTip.getTranslationY(), (float) this.mChargeTipTranslateSmall});
            PropertyValuesHolder ofFloat5 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mStateTip.getAlpha(), 0.0f});
            ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mStateTip, new PropertyValuesHolder[]{ofFloat5, ofFloat4}).setDuration(500);
            duration2.setInterpolator(this.mCubicInterpolator);
            PropertyValuesHolder ofFloat6 = PropertyValuesHolder.ofFloat(property, new float[]{this.mGtChargeAniView.getTranslationY(), (float) this.mChargeTipTranslateSmall});
            PropertyValuesHolder ofFloat7 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mGtChargeAniView.getAlpha(), 1.0f});
            ObjectAnimator duration3 = ObjectAnimator.ofPropertyValuesHolder(this.mGtChargeAniView, new PropertyValuesHolder[]{ofFloat7, ofFloat6}).setDuration(250);
            duration3.setInterpolator(this.mCubicInterpolator);
            duration3.addListener(new Animator.AnimatorListener() {
                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                    RapidChargeView.this.mGtChargeAniView.setVisibility(8);
                }

                public void onAnimationEnd(Animator animator) {
                    RapidChargeView.this.mGtChargeAniView.setVisibility(0);
                    RapidChargeView rapidChargeView = RapidChargeView.this;
                    if (rapidChargeView.mChargeState == 3) {
                        rapidChargeView.mGtChargeAniView.setStrongViewInitState();
                        RapidChargeView.this.mGtChargeAniView.animationWiredStrongToShow();
                        return;
                    }
                    rapidChargeView.mGtChargeAniView.setViewInitState();
                    RapidChargeView.this.mGtChargeAniView.animationToShow();
                }

                public void onAnimationCancel(Animator animator) {
                    RapidChargeView.this.mGtChargeAniView.setVisibility(8);
                }
            });
            PropertyValuesHolder ofFloat8 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mRapidIcon.getScaleX(), 0.0f});
            PropertyValuesHolder ofFloat9 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mRapidIcon.getScaleY(), 0.0f});
            PropertyValuesHolder ofFloat10 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mRapidIcon.getAlpha(), 0.0f});
            ObjectAnimator duration4 = ObjectAnimator.ofPropertyValuesHolder(this.mRapidIcon, new PropertyValuesHolder[]{ofFloat8, ofFloat9, ofFloat10}).setDuration(500);
            duration4.setInterpolator(this.mCubicInterpolator);
            PropertyValuesHolder ofFloat11 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mSuperRapidIcon.getScaleX(), 1.0f});
            PropertyValuesHolder ofFloat12 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mSuperRapidIcon.getScaleY(), 1.0f});
            PropertyValuesHolder ofFloat13 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mSuperRapidIcon.getAlpha(), 1.0f});
            ObjectAnimator duration5 = ObjectAnimator.ofPropertyValuesHolder(this.mSuperRapidIcon, new PropertyValuesHolder[]{ofFloat11, ofFloat12, ofFloat13}).setDuration(500);
            duration5.setInterpolator(this.mCubicInterpolator);
            duration5.setInterpolator(new OvershootInterpolator(3.0f));
            AnimatorSet animatorSet2 = new AnimatorSet();
            this.mContentSwitchAnimator = animatorSet2;
            animatorSet2.playTogether(new Animator[]{duration, duration2, duration3, duration4, duration5});
            this.mContentSwitchAnimator.start();
        }
    }

    public void setScreenOn(boolean z) {
        this.mIsScreenOn = z;
    }

    public void setProgress(int i) {
        this.mPercentCountView.setProgress(i);
    }

    public void startValueAnimation(float f, float f2) {
        this.mPercentCountView.startValueAnimation(f, f2);
        this.mHandler.removeCallbacks(this.mTimeoutDismissJob);
        this.mHandler.postDelayed(this.mTimeoutDismissJob, 9400);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mWindowShouldAdd) {
            removeFromWindow("!mWindowShouldAdd");
            return;
        }
        this.mStateTip.setText(getResources().getString(R.string.rapid_charge_mode_tip));
        checkScreenSize();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mWindowShouldAdd) {
            addToWindow("mWindowShouldAdd");
        }
        this.mHandler.removeCallbacksAndMessages((Object) null);
    }

    public void zoomLarge(boolean z, boolean z2) {
        Log.i("RapidChargeView", "zoomLarge: mInitScreenOn " + z);
        this.mInitScreenOn = z;
        this.mClickShowChargeUI = z2;
        this.mHandler.removeCallbacks(this.mDismissRunnable);
        AnimatorSet animatorSet = this.mDismissAnimatorSet;
        if (animatorSet != null && this.mStartingDismissWirelessAlphaAnim) {
            animatorSet.cancel();
        }
        this.mStartingDismissWirelessAlphaAnim = false;
        addToWindow("zoomLarge: ");
        hideSystemUI();
        setComponentTransparent(false);
        setAlpha(this.mInitScreenOn ? 0.0f : 1.0f);
        setViewState();
        setVisibility(0);
        requestFocus();
        if (this.mEnterAnimatorSet == null) {
            initAnimator();
        }
        if (this.mEnterAnimatorSet.isStarted()) {
            this.mEnterAnimatorSet.cancel();
        }
        this.mEnterAnimatorSet.start();
        zoomLargeOnChildView();
        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).extHapticFeedback(74, false, 0);
        post(new Runnable() {
            public void run() {
                RapidChargeView.this.disableTouch(false);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setViewState() {
        int i = this.mChargeState;
        if (i == 0) {
            this.mPercentCountView.setScaleX(1.0f);
            this.mPercentCountView.setScaleY(1.0f);
            this.mPercentCountView.setTranslationY((float) this.mChargeNumberTranslateInit);
            this.mStateTip.setAlpha(0.0f);
            this.mStateTip.setTranslationY(0.0f);
            this.mGtChargeAniView.setViewInitState();
            this.mGtChargeAniView.setVisibility(8);
            this.mRapidIcon.setScaleY(0.0f);
            this.mRapidIcon.setScaleX(0.0f);
            this.mRapidIcon.setAlpha(0.0f);
            this.mSuperRapidIcon.setScaleY(0.0f);
            this.mSuperRapidIcon.setScaleX(0.0f);
            this.mSuperRapidIcon.setAlpha(0.0f);
        } else if (i == 1) {
            this.mPercentCountView.setScaleX(0.85f);
            this.mPercentCountView.setScaleY(0.85f);
            this.mPercentCountView.setTranslationY((float) this.mChargeNumberTranslateSmall);
            if (isPercentViewShown()) {
                this.mStateTip.setAlpha(1.0f);
                this.mStateTip.setTranslationY((float) this.mChargeTipTranslateSmall);
                this.mGtChargeAniView.setViewInitState();
                this.mGtChargeAniView.setVisibility(8);
                this.mRapidIcon.setScaleY(1.0f);
                this.mRapidIcon.setScaleX(1.0f);
                this.mRapidIcon.setAlpha(1.0f);
            }
            this.mSuperRapidIcon.setScaleY(0.0f);
            this.mSuperRapidIcon.setScaleX(0.0f);
            this.mSuperRapidIcon.setAlpha(0.0f);
        } else if (i == 2) {
            this.mPercentCountView.setScaleX(0.85f);
            this.mPercentCountView.setScaleY(0.85f);
            this.mPercentCountView.setTranslationY((float) this.mChargeNumberTranslateSmall);
            this.mStateTip.setAlpha(0.0f);
            this.mStateTip.setTranslationY((float) this.mChargeTipTranslateSmall);
            if (isPercentViewShown()) {
                this.mGtChargeAniView.setVisibility(0);
                if (this.mClickShowChargeUI) {
                    this.mGtChargeAniView.setViewShowState();
                } else {
                    this.mGtChargeAniView.setViewInitState();
                    this.mGtChargeAniView.animationToShow();
                }
                this.mSuperRapidIcon.setImageDrawable(this.mSuperRapidIconDrawable);
                this.mSuperRapidIcon.setScaleY(1.0f);
                this.mSuperRapidIcon.setScaleX(1.0f);
                this.mSuperRapidIcon.setAlpha(1.0f);
            }
            this.mRapidIcon.setScaleY(0.0f);
            this.mRapidIcon.setScaleX(0.0f);
            this.mRapidIcon.setAlpha(0.0f);
        } else if (i == 3) {
            this.mPercentCountView.setScaleX(0.85f);
            this.mPercentCountView.setScaleY(0.85f);
            this.mPercentCountView.setTranslationY((float) this.mChargeNumberTranslateSmall);
            this.mStateTip.setAlpha(0.0f);
            this.mStateTip.setTranslationY((float) this.mChargeTipTranslateSmall);
            if (isPercentViewShown()) {
                this.mGtChargeAniView.setStrongViewInitState();
                this.mGtChargeAniView.setVisibility(0);
                if (this.mClickShowChargeUI) {
                    this.mGtChargeAniView.setWiredStrongViewShowState();
                } else {
                    this.mGtChargeAniView.setStrongViewInitState();
                    this.mGtChargeAniView.animationWiredStrongToShow();
                }
                this.mSuperRapidIcon.setImageDrawable(this.mStrongSuperRapidIconDrawable);
                this.mSuperRapidIcon.setScaleY(1.0f);
                this.mSuperRapidIcon.setScaleX(1.0f);
                this.mSuperRapidIcon.setAlpha(1.0f);
            }
            this.mRapidIcon.setScaleY(0.0f);
            this.mRapidIcon.setScaleX(0.0f);
            this.mRapidIcon.setAlpha(0.0f);
        }
    }

    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        if (!this.mInitScreenOn) {
            animatedFraction = 1.0f;
        }
        setAlpha(animatedFraction);
    }

    public void onAnimationStart(Animator animator) {
        IRapidAnimationListener iRapidAnimationListener = this.animationListener;
        if (iRapidAnimationListener != null) {
            iRapidAnimationListener.onRapidAnimationStart(11);
        }
        this.mHandler.removeCallbacks(this.mTimeoutDismissJob);
        this.mHandler.postDelayed(this.mTimeoutDismissJob, (long) (getAnimationDuration() - 600));
    }

    public int getAnimationDuration() {
        return (!((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).isUsbCharging() || KeyguardUpdateMonitor.getInstance(this.mContext).getStrongAuthTracker().hasUserAuthenticatedSinceBoot(KeyguardUpdateMonitor.getCurrentUser())) ? 20000 : 5000;
    }

    public void startDismiss(String str) {
        disableTouch(true);
        if (str != "dismiss_for_timeout") {
            KeyguardUpdateMonitor.getInstance(this.mContext).setShowingChargeAnimationWindow(false);
        }
        if (!this.mStartingDismissWirelessAlphaAnim) {
            AnimatorSet animatorSet = this.mEnterAnimatorSet;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            Log.i("RapidChargeView", "startDismiss: reason: " + str);
            this.mDismissReason = str;
            this.mHandler.removeCallbacks(this.mTimeoutDismissJob);
            this.mHandler.removeCallbacks(this.mDismissRunnable);
            this.mDismissAnimatorSet = new AnimatorSet();
            this.mDismissAnimatorSet.addListener(new Animator.AnimatorListener() {
                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    boolean unused = RapidChargeView.this.mStartingDismissWirelessAlphaAnim = false;
                    if (RapidChargeView.this.animationListener != null) {
                        RapidChargeView.this.animationListener.onRapidAnimationEnd(11, RapidChargeView.this.mDismissReason);
                    }
                    RapidChargeView.this.mHandler.post(RapidChargeView.this.mDismissRunnable);
                }

                public void onAnimationCancel(Animator animator) {
                    boolean unused = RapidChargeView.this.mStartingDismissWirelessAlphaAnim = false;
                    if (RapidChargeView.this.animationListener != null) {
                        RapidChargeView.this.animationListener.onRapidAnimationEnd(11, RapidChargeView.this.mDismissReason);
                    }
                    RapidChargeView.this.mHandler.removeCallbacks(RapidChargeView.this.mDismissRunnable);
                }
            });
            this.mStartingDismissWirelessAlphaAnim = true;
        }
    }

    /* access modifiers changed from: protected */
    public void dismissView() {
        removeFromWindow("dismiss");
    }

    public void setRapidAnimationListener(IRapidAnimationListener iRapidAnimationListener) {
        this.animationListener = iRapidAnimationListener;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        checkScreenSize();
    }

    private void checkScreenSize() {
        Point point = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(point);
        if (!this.mScreenSize.equals(point.x, point.y)) {
            this.mScreenSize.set(point.x, point.y);
            updateSizeForScreenSizeChange();
            updateLayoutParamForScreenSizeChange();
            requestLayout();
            post(new Runnable() {
                public void run() {
                    RapidChargeView.this.startContentSwitchAnimation();
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void updateSizeForScreenSizeChange() {
        Point point = this.mScreenSize;
        float min = (((float) Math.min(point.x, point.y)) * 1.0f) / 1080.0f;
        this.mPivotX = (int) (100.0f * min);
        this.mChargeNumberTranslateSmall = (int) (-70.0f * min);
        this.mChargeNumberTranslateInit = (int) (-10.0f * min);
        this.mChargeTipTranslateSmall = (int) (-50.0f * min);
        this.mSpeedTipTextSizePx = (int) (34.485f * min);
        this.mSpaceHeight = (int) (16.0f * min);
        this.mTipTopMargin = (int) (70.0f * min);
        this.mIconPaddingTop = (int) (275.0f * min);
        Drawable drawable = this.mRapidIconDrawable;
        if (drawable != null) {
            this.mRapidIconWidth = (int) (((float) drawable.getIntrinsicWidth()) * min);
            this.mRapidIconHeight = (int) (((float) this.mRapidIconDrawable.getIntrinsicHeight()) * min);
        }
        Drawable drawable2 = this.mSuperRapidIconDrawable;
        if (drawable2 != null) {
            this.mSuperRapidIconWidth = (int) (((float) drawable2.getIntrinsicWidth()) * min);
            this.mSuperRapidIconHeight = (int) (min * ((float) this.mSuperRapidIconDrawable.getIntrinsicHeight()));
        }
    }

    /* access modifiers changed from: protected */
    public void updateLayoutParamForScreenSizeChange() {
        this.mCenterAnchorView.getLayoutParams().height = this.mSpaceHeight;
        this.mStateTip.setTextSize(0, (float) this.mSpeedTipTextSizePx);
        ((RelativeLayout.LayoutParams) this.mStateTip.getLayoutParams()).topMargin = this.mTipTopMargin;
        ((RelativeLayout.LayoutParams) this.mGtChargeAniView.getLayoutParams()).topMargin = this.mTipTopMargin;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mRapidIcon.getLayoutParams();
        layoutParams.width = this.mRapidIconWidth;
        int i = this.mRapidIconHeight;
        int i2 = this.mIconPaddingTop;
        layoutParams.height = i + i2;
        this.mRapidIcon.setPadding(0, i2, 0, 0);
        this.mRapidIcon.setPivotX((float) this.mPivotX);
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mSuperRapidIcon.getLayoutParams();
        layoutParams2.width = this.mSuperRapidIconWidth;
        int i3 = this.mSuperRapidIconHeight;
        int i4 = this.mIconPaddingTop;
        layoutParams2.height = i3 + i4;
        this.mSuperRapidIcon.setPadding(0, i4, 0, 0);
        this.mSuperRapidIcon.setPivotX((float) this.mPivotX);
    }

    private static class AccessibilityDisableTextView extends TextView {
        public void onPopulateAccessibilityEventInternal(AccessibilityEvent accessibilityEvent) {
        }

        public AccessibilityDisableTextView(Context context) {
            super(context);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        startDismiss("dismiss_for_key_event");
        InputManager.getInstance().injectInputEvent(keyEvent, 0);
        return false;
    }
}
