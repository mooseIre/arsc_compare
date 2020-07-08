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
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.charge.ChargeUtils;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.plugins.R;
import miui.maml.animation.interpolater.CubicEaseOutInterpolater;
import miui.maml.animation.interpolater.QuartEaseOutInterpolater;

public class WirelessRapidChargeView extends FrameLayout implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
    /* access modifiers changed from: private */
    public IRapidAnimationListener animationListener;
    private Drawable mCarIconDrawable;
    private int mCarIconHeight;
    private int mCarIconWidth;
    private ImageView mCarModeIcon;
    private View mCenterAnchorView;
    private int mChargeNumberTranslateInit;
    private int mChargeNumberTranslateSmall;
    private int mChargeSpeed;
    private int mChargeTipTranslateSmall;
    /* access modifiers changed from: private */
    public ViewGroup mContentContainer;
    private AnimatorSet mContentSwitchAnimator;
    private Interpolator mCubicInterpolator;
    private AnimatorSet mDismissAnimatorSet;
    /* access modifiers changed from: private */
    public String mDismissReason;
    /* access modifiers changed from: private */
    public final Runnable mDismissRunnable;
    /* access modifiers changed from: private */
    public GTChargeAniView mGtChargeAniView;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private int mIconPaddingTop;
    private boolean mInitScreenOn;
    private boolean mIsCarMode;
    /* access modifiers changed from: private */
    public boolean mIsSuperRapidCharge;
    private ImageView mNormalIcon;
    private Drawable mNormalIconDrawable;
    private int mNormalIconHeight;
    private int mNormalIconWidth;
    private OrientationEventListener mOrientationEventListener;
    private PercentCountView mPercentCountView;
    private int mPivotX;
    private Interpolator mQuartOutInterpolator;
    private Point mScreenSize;
    private int mSpaceHeight;
    /* access modifiers changed from: private */
    public boolean mStartingDismissWirelessAlphaAnim;
    private ImageView mSuperRapidIcon;
    private Drawable mSuperRapidIconDrawable;
    private int mSuperRapidIconHeight;
    private int mSuperRapidIconWidth;
    private int mTipTopMargin;
    /* access modifiers changed from: private */
    public ChargeVideoView mVideoView;
    private WindowManager mWindowManager;
    private boolean mWindowShouldAdd;
    private ValueAnimator mZoomAnimator;
    private Runnable timeoutDismissJob;

    public void onAnimationCancel(Animator animator) {
    }

    public void onAnimationRepeat(Animator animator) {
    }

    public void setScreenOn(boolean z) {
    }

    public WirelessRapidChargeView(Context context) {
        this(context, (AttributeSet) null);
    }

    public WirelessRapidChargeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WirelessRapidChargeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCubicInterpolator = new CubicEaseOutInterpolater();
        this.mQuartOutInterpolator = new QuartEaseOutInterpolater();
        this.mHandler = new Handler() {
            public void handleMessage(Message message) {
                int i = message.what;
                if (i == 101) {
                    WirelessRapidChargeView.this.mContentContainer.setVisibility(0);
                } else if (i == 102) {
                    WirelessRapidChargeView.this.disableOrientationSensor();
                }
            }
        };
        this.mIsSuperRapidCharge = false;
        this.mDismissRunnable = new Runnable() {
            public void run() {
                WirelessRapidChargeView.this.mVideoView.stopAnimation();
                WirelessRapidChargeView.this.mVideoView.removeRapidChargeView();
                WirelessRapidChargeView.this.mVideoView.removeChargeView();
                WirelessRapidChargeView.this.setComponentTransparent(true);
                WirelessRapidChargeView.this.removeFromWindow("dismiss");
                if (WirelessRapidChargeView.this.animationListener != null) {
                    WirelessRapidChargeView.this.animationListener.onRapidAnimationDismiss(10, WirelessRapidChargeView.this.mDismissReason);
                }
            }
        };
        this.timeoutDismissJob = new Runnable() {
            public void run() {
                WirelessRapidChargeView.this.startDismiss("dismiss_for_timeout");
            }
        };
        init(context);
    }

    private void init(Context context) {
        this.mIsSuperRapidCharge = false;
        this.mIsCarMode = false;
        this.mNormalIconDrawable = context.getDrawable(R.drawable.charge_animation_normal_charge_icon);
        this.mSuperRapidIconDrawable = context.getDrawable(R.drawable.charge_animation_super_rapid_icon);
        this.mCarIconDrawable = context.getDrawable(R.drawable.charge_animation_car_mode_icon);
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mScreenSize = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        updateSizeForScreenSizeChange();
        setBackgroundColor(Color.argb(242, 0, 0, 0));
        new FrameLayout.LayoutParams(-2, -2).gravity = 81;
        this.mContentContainer = new RelativeLayout(context);
        this.mVideoView = new ChargeVideoView(this.mContext);
        this.mVideoView.setDefaultImage(R.drawable.wireless_charge_video_bg_img);
        this.mVideoView.setChargeUri("android.resource://" + this.mContext.getPackageName() + "/" + R.raw.wireless_charge_video);
        this.mVideoView.setRapidChargeUri("android.resource://" + this.mContext.getPackageName() + "/" + R.raw.wireless_quick_charge_video);
        Point point = this.mScreenSize;
        this.mContentContainer.setTranslationY((float) ((Math.max(point.x, point.y) - 2340) / 2));
        ViewGroup viewGroup = this.mContentContainer;
        ChargeVideoView chargeVideoView = this.mVideoView;
        viewGroup.addView(chargeVideoView, chargeVideoView.getVideoLayoutParams());
        this.mCenterAnchorView = new TextView(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-2, this.mSpaceHeight);
        layoutParams.addRule(13);
        this.mCenterAnchorView.setId(View.generateViewId());
        this.mContentContainer.addView(this.mCenterAnchorView, layoutParams);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams2.addRule(13);
        PercentCountView percentCountView = new PercentCountView(context);
        this.mPercentCountView = percentCountView;
        this.mContentContainer.addView(percentCountView, layoutParams2);
        this.mGtChargeAniView = new GTChargeAniView(context);
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams3.addRule(14);
        layoutParams3.addRule(3, this.mCenterAnchorView.getId());
        layoutParams3.topMargin = this.mTipTopMargin;
        this.mContentContainer.addView(this.mGtChargeAniView, layoutParams3);
        ImageView imageView = new ImageView(context);
        this.mNormalIcon = imageView;
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.mNormalIcon.setImageDrawable(this.mNormalIconDrawable);
        RelativeLayout.LayoutParams layoutParams4 = new RelativeLayout.LayoutParams(this.mNormalIconWidth, this.mNormalIconHeight + this.mIconPaddingTop);
        layoutParams4.addRule(13);
        this.mNormalIcon.setPadding(0, this.mIconPaddingTop, 0, 0);
        this.mNormalIcon.setPivotX((float) this.mPivotX);
        this.mContentContainer.addView(this.mNormalIcon, layoutParams4);
        ImageView imageView2 = new ImageView(context);
        this.mSuperRapidIcon = imageView2;
        imageView2.setScaleType(ImageView.ScaleType.FIT_XY);
        this.mSuperRapidIcon.setImageDrawable(this.mSuperRapidIconDrawable);
        RelativeLayout.LayoutParams layoutParams5 = new RelativeLayout.LayoutParams(this.mSuperRapidIconWidth, this.mSuperRapidIconHeight + this.mIconPaddingTop);
        layoutParams5.addRule(13);
        this.mSuperRapidIcon.setPivotX((float) this.mPivotX);
        this.mSuperRapidIcon.setPadding(0, this.mIconPaddingTop, 0, 0);
        this.mContentContainer.addView(this.mSuperRapidIcon, layoutParams5);
        ImageView imageView3 = new ImageView(context);
        this.mCarModeIcon = imageView3;
        imageView3.setScaleType(ImageView.ScaleType.FIT_XY);
        this.mCarModeIcon.setImageDrawable(this.mCarIconDrawable);
        RelativeLayout.LayoutParams layoutParams6 = new RelativeLayout.LayoutParams(this.mCarIconWidth, this.mCarIconHeight + this.mIconPaddingTop);
        layoutParams6.addRule(13);
        this.mCarModeIcon.setPivotX((float) this.mPivotX);
        this.mCarModeIcon.setPadding(0, this.mIconPaddingTop, 0, 0);
        this.mContentContainer.addView(this.mCarModeIcon, layoutParams6);
        addView(this.mContentContainer, new RelativeLayout.LayoutParams(1080, 2340));
        setComponentTransparent(true);
        this.mOrientationEventListener = new OrientationEventListener(this.mContext) {
            public void onOrientationChanged(int i) {
                Slog.i("WirelessRapidChargeView", "onOrientationChanged: " + i);
                if (i > 45 && i < 135) {
                    WirelessRapidChargeView.this.mContentContainer.setRotation(270.0f);
                } else if (i <= 225 || i >= 315) {
                    WirelessRapidChargeView.this.mContentContainer.setRotation(0.0f);
                } else {
                    WirelessRapidChargeView.this.mContentContainer.setRotation(90.0f);
                }
                WirelessRapidChargeView.this.mContentContainer.setVisibility(0);
                WirelessRapidChargeView.this.disableOrientationSensor();
            }
        };
        setElevation(30.0f);
    }

    public void setChargeState(boolean z, boolean z2) {
        setChargeState(z ? 2 : 0, z2);
    }

    private void setChargeState(int i, boolean z) {
        boolean z2 = true;
        final boolean z3 = i != this.mChargeSpeed;
        final boolean z4 = z != this.mIsCarMode;
        this.mChargeSpeed = i;
        if (i != 2) {
            z2 = false;
        }
        this.mIsSuperRapidCharge = z2;
        this.mIsCarMode = z;
        post(new Runnable() {
            public void run() {
                if (z3 || z4) {
                    WirelessRapidChargeView.this.startContentSwitchAnimation();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void startContentSwitchAnimation() {
        if (isAttachedToWindow()) {
            if (this.mIsCarMode) {
                this.mVideoView.switchToRapidChargeAnim();
                animateToShowCarIcon();
            } else if (this.mIsSuperRapidCharge) {
                this.mVideoView.switchToRapidChargeAnim();
                animateToShowSuperRapidIcon();
            } else {
                this.mVideoView.switchToNormalChargeAnim();
                animateToHideIcon();
            }
        }
    }

    private void animateToShowCarIcon() {
        Property property = FrameLayout.TRANSLATION_Y;
        Property property2 = FrameLayout.ALPHA;
        Property property3 = FrameLayout.SCALE_Y;
        Property property4 = FrameLayout.SCALE_X;
        Log.i("WirelessRapidChargeView", "animateToShowCarIcon: ");
        AnimatorSet animatorSet = this.mContentSwitchAnimator;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this.mPercentCountView, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat(property4, new float[]{this.mPercentCountView.getScaleX(), 0.85f}), PropertyValuesHolder.ofFloat(property3, new float[]{this.mPercentCountView.getScaleY(), 0.85f}), PropertyValuesHolder.ofFloat(property, new float[]{this.mPercentCountView.getTranslationY(), (float) this.mChargeNumberTranslateSmall})}).setDuration(500);
        duration.setInterpolator(this.mCubicInterpolator);
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property, new float[]{this.mGtChargeAniView.getTranslationY(), (float) this.mChargeTipTranslateSmall});
        float[] fArr = new float[2];
        fArr[0] = this.mGtChargeAniView.getAlpha();
        fArr[1] = this.mIsSuperRapidCharge ? 1.0f : 0.0f;
        ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mGtChargeAniView, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat(property2, fArr), ofFloat}).setDuration(250);
        duration2.setInterpolator(this.mCubicInterpolator);
        duration2.addListener(new Animator.AnimatorListener() {
            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
                WirelessRapidChargeView.this.mGtChargeAniView.setVisibility(8);
            }

            public void onAnimationEnd(Animator animator) {
                if (WirelessRapidChargeView.this.mIsSuperRapidCharge) {
                    WirelessRapidChargeView.this.mGtChargeAniView.setViewInitState();
                    WirelessRapidChargeView.this.mGtChargeAniView.setVisibility(0);
                    WirelessRapidChargeView.this.mGtChargeAniView.animationToShow();
                }
            }

            public void onAnimationCancel(Animator animator) {
                WirelessRapidChargeView.this.mGtChargeAniView.setVisibility(8);
            }
        });
        ObjectAnimator duration3 = ObjectAnimator.ofPropertyValuesHolder(this.mSuperRapidIcon, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat(property4, new float[]{this.mSuperRapidIcon.getScaleX(), 0.0f}), PropertyValuesHolder.ofFloat(property3, new float[]{this.mSuperRapidIcon.getScaleY(), 0.0f}), PropertyValuesHolder.ofFloat(property2, new float[]{this.mSuperRapidIcon.getAlpha(), -4.0f})}).setDuration(500);
        duration3.setInterpolator(this.mCubicInterpolator);
        ObjectAnimator duration4 = ObjectAnimator.ofPropertyValuesHolder(this.mNormalIcon, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat(property4, new float[]{this.mNormalIcon.getScaleX(), 0.0f}), PropertyValuesHolder.ofFloat(property3, new float[]{this.mNormalIcon.getScaleY(), 0.0f}), PropertyValuesHolder.ofFloat(property2, new float[]{this.mNormalIcon.getAlpha(), -4.0f})}).setDuration(500);
        ObjectAnimator duration5 = ObjectAnimator.ofPropertyValuesHolder(this.mCarModeIcon, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat(property4, new float[]{this.mCarModeIcon.getScaleX(), 1.0f}), PropertyValuesHolder.ofFloat(property3, new float[]{this.mCarModeIcon.getScaleY(), 1.0f}), PropertyValuesHolder.ofFloat(property2, new float[]{this.mCarModeIcon.getAlpha(), 1.0f})}).setDuration(500);
        duration5.setInterpolator(this.mCubicInterpolator);
        AnimatorSet animatorSet2 = new AnimatorSet();
        this.mContentSwitchAnimator = animatorSet2;
        animatorSet2.playTogether(new Animator[]{duration, duration5, duration3, duration4, duration2});
        this.mContentSwitchAnimator.start();
    }

    private void animateToShowSuperRapidIcon() {
        Property property = FrameLayout.TRANSLATION_Y;
        Property property2 = FrameLayout.ALPHA;
        Property property3 = FrameLayout.SCALE_Y;
        Property property4 = FrameLayout.SCALE_X;
        Log.i("WirelessRapidChargeView", "animateToShowSuperRapidIcon: ");
        AnimatorSet animatorSet = this.mContentSwitchAnimator;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property4, new float[]{this.mPercentCountView.getScaleX(), 0.85f});
        PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mPercentCountView.getScaleY(), 0.85f});
        PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(property, new float[]{this.mPercentCountView.getTranslationY(), (float) this.mChargeNumberTranslateSmall});
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this.mPercentCountView, new PropertyValuesHolder[]{ofFloat, ofFloat2, ofFloat3}).setDuration(500);
        duration.setInterpolator(this.mCubicInterpolator);
        PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(property, new float[]{this.mGtChargeAniView.getTranslationY(), (float) this.mChargeTipTranslateSmall});
        PropertyValuesHolder ofFloat5 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mGtChargeAniView.getAlpha(), 1.0f});
        ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mGtChargeAniView, new PropertyValuesHolder[]{ofFloat5, ofFloat4}).setDuration(250);
        duration2.setInterpolator(this.mCubicInterpolator);
        duration2.addListener(new Animator.AnimatorListener() {
            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
                WirelessRapidChargeView.this.mGtChargeAniView.setVisibility(8);
            }

            public void onAnimationEnd(Animator animator) {
                WirelessRapidChargeView.this.mGtChargeAniView.setViewInitState();
                WirelessRapidChargeView.this.mGtChargeAniView.setVisibility(0);
                WirelessRapidChargeView.this.mGtChargeAniView.animationToShow();
            }

            public void onAnimationCancel(Animator animator) {
                WirelessRapidChargeView.this.mGtChargeAniView.setVisibility(8);
            }
        });
        PropertyValuesHolder ofFloat6 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mSuperRapidIcon.getScaleX(), 1.0f});
        PropertyValuesHolder ofFloat7 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mSuperRapidIcon.getScaleY(), 1.0f});
        PropertyValuesHolder ofFloat8 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mSuperRapidIcon.getAlpha(), 1.0f});
        ObjectAnimator duration3 = ObjectAnimator.ofPropertyValuesHolder(this.mSuperRapidIcon, new PropertyValuesHolder[]{ofFloat6, ofFloat7, ofFloat8}).setDuration(500);
        duration3.setInterpolator(this.mCubicInterpolator);
        duration3.setInterpolator(new OvershootInterpolator(3.0f));
        PropertyValuesHolder ofFloat9 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mNormalIcon.getScaleX(), 0.0f});
        PropertyValuesHolder ofFloat10 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mNormalIcon.getScaleY(), 0.0f});
        PropertyValuesHolder ofFloat11 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mNormalIcon.getAlpha(), -4.0f});
        ObjectAnimator duration4 = ObjectAnimator.ofPropertyValuesHolder(this.mNormalIcon, new PropertyValuesHolder[]{ofFloat9, ofFloat10, ofFloat11}).setDuration(500);
        PropertyValuesHolder ofFloat12 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mCarModeIcon.getScaleX(), 0.0f});
        PropertyValuesHolder ofFloat13 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mCarModeIcon.getScaleY(), 0.0f});
        PropertyValuesHolder ofFloat14 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mCarModeIcon.getAlpha(), -4.0f});
        ObjectAnimator duration5 = ObjectAnimator.ofPropertyValuesHolder(this.mCarModeIcon, new PropertyValuesHolder[]{ofFloat12, ofFloat13, ofFloat14}).setDuration(500);
        duration5.setInterpolator(this.mCubicInterpolator);
        AnimatorSet animatorSet2 = new AnimatorSet();
        this.mContentSwitchAnimator = animatorSet2;
        animatorSet2.playTogether(new Animator[]{duration, duration5, duration3, duration4, duration2});
        this.mContentSwitchAnimator.start();
    }

    private void animateToHideIcon() {
        Property property = FrameLayout.TRANSLATION_Y;
        Property property2 = FrameLayout.ALPHA;
        Property property3 = FrameLayout.SCALE_Y;
        Property property4 = FrameLayout.SCALE_X;
        Log.i("WirelessRapidChargeView", "animateToHideIcon: mIsCarMode " + this.mIsCarMode + " mIsSuperRapidCharge " + this.mIsSuperRapidCharge);
        AnimatorSet animatorSet = this.mContentSwitchAnimator;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property4, new float[]{this.mPercentCountView.getScaleX(), 1.0f});
        PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mPercentCountView.getScaleY(), 1.0f});
        PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(property, new float[]{this.mPercentCountView.getTranslationY(), (float) this.mChargeNumberTranslateInit});
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this.mPercentCountView, new PropertyValuesHolder[]{ofFloat, ofFloat2, ofFloat3}).setDuration(500);
        PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(property, new float[]{this.mGtChargeAniView.getTranslationY(), 0.0f});
        PropertyValuesHolder ofFloat5 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mGtChargeAniView.getAlpha(), 0.0f});
        ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mGtChargeAniView, new PropertyValuesHolder[]{ofFloat5, ofFloat4}).setDuration(500);
        PropertyValuesHolder ofFloat6 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mCarModeIcon.getScaleX(), 0.0f});
        PropertyValuesHolder ofFloat7 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mCarModeIcon.getScaleY(), 0.0f});
        PropertyValuesHolder ofFloat8 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mCarModeIcon.getAlpha(), -4.0f});
        ObjectAnimator duration3 = ObjectAnimator.ofPropertyValuesHolder(this.mCarModeIcon, new PropertyValuesHolder[]{ofFloat6, ofFloat7, ofFloat8}).setDuration(500);
        PropertyValuesHolder ofFloat9 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mSuperRapidIcon.getScaleX(), 0.0f});
        PropertyValuesHolder ofFloat10 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mSuperRapidIcon.getScaleY(), 0.0f});
        PropertyValuesHolder ofFloat11 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mSuperRapidIcon.getAlpha(), -4.0f});
        ObjectAnimator duration4 = ObjectAnimator.ofPropertyValuesHolder(this.mSuperRapidIcon, new PropertyValuesHolder[]{ofFloat9, ofFloat10, ofFloat11}).setDuration(500);
        PropertyValuesHolder ofFloat12 = PropertyValuesHolder.ofFloat(property4, new float[]{this.mNormalIcon.getScaleX(), 1.0f});
        PropertyValuesHolder ofFloat13 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mNormalIcon.getScaleY(), 1.0f});
        PropertyValuesHolder ofFloat14 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mNormalIcon.getAlpha(), 1.0f});
        ObjectAnimator duration5 = ObjectAnimator.ofPropertyValuesHolder(this.mNormalIcon, new PropertyValuesHolder[]{ofFloat12, ofFloat13, ofFloat14}).setDuration(500);
        AnimatorSet animatorSet2 = new AnimatorSet();
        this.mContentSwitchAnimator = animatorSet2;
        animatorSet2.setInterpolator(this.mCubicInterpolator);
        this.mContentSwitchAnimator.playTogether(new Animator[]{duration, duration3, duration4, duration5, duration2});
        this.mContentSwitchAnimator.start();
    }

    public void setProgress(int i) {
        this.mPercentCountView.setProgress(i);
    }

    private void initAnimator() {
        ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{0, 1});
        this.mZoomAnimator = ofInt;
        ofInt.setInterpolator(this.mQuartOutInterpolator);
        this.mZoomAnimator.setDuration(800);
        this.mZoomAnimator.addListener(this);
        this.mZoomAnimator.addUpdateListener(this);
    }

    public void zoomLarge(boolean z) {
        Log.i("WirelessRapidChargeView", "zoomLarge: mInitScreenOn " + z);
        this.mInitScreenOn = z;
        this.mHandler.removeCallbacks(this.mDismissRunnable);
        AnimatorSet animatorSet = this.mDismissAnimatorSet;
        if (animatorSet != null && this.mStartingDismissWirelessAlphaAnim) {
            animatorSet.cancel();
        }
        this.mStartingDismissWirelessAlphaAnim = false;
        addToWindow("zoomLarge: ");
        setComponentTransparent(false);
        setAlpha(this.mInitScreenOn ? 0.0f : 1.0f);
        setViewState();
        setVisibility(0);
        requestFocus();
        if (this.mZoomAnimator == null) {
            initAnimator();
        }
        if (this.mZoomAnimator.isStarted()) {
            this.mZoomAnimator.cancel();
        }
        this.mZoomAnimator.start();
        if (this.mChargeSpeed == 2) {
            this.mVideoView.addRapidChargeView();
        } else {
            this.mVideoView.addChargeView();
        }
        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).extHapticFeedback(75, false, 0);
    }

    private void setViewState() {
        this.mVideoView.removeChargeView();
        this.mVideoView.removeRapidChargeView();
        boolean z = false;
        if (this.mIsSuperRapidCharge) {
            this.mGtChargeAniView.setViewInitState();
            this.mGtChargeAniView.setVisibility(0);
            this.mGtChargeAniView.animationToShow();
        } else {
            this.mGtChargeAniView.setViewInitState();
            this.mGtChargeAniView.setVisibility(8);
        }
        if (this.mIsSuperRapidCharge || this.mIsCarMode) {
            z = true;
        }
        if (z) {
            this.mGtChargeAniView.setTranslationY((float) this.mChargeTipTranslateSmall);
            if (this.mIsCarMode) {
                this.mSuperRapidIcon.setScaleX(0.0f);
                this.mSuperRapidIcon.setScaleY(0.0f);
                this.mCarModeIcon.setScaleX(1.0f);
                this.mCarModeIcon.setScaleY(1.0f);
            } else {
                this.mSuperRapidIcon.setScaleX(1.0f);
                this.mSuperRapidIcon.setScaleY(1.0f);
                this.mCarModeIcon.setScaleX(0.0f);
                this.mCarModeIcon.setScaleY(0.0f);
            }
            this.mNormalIcon.setScaleX(0.0f);
            this.mNormalIcon.setScaleY(0.0f);
            this.mPercentCountView.setScaleX(0.85f);
            this.mPercentCountView.setScaleY(0.85f);
            this.mPercentCountView.setTranslationY((float) this.mChargeNumberTranslateSmall);
            return;
        }
        this.mGtChargeAniView.setTranslationY(0.0f);
        this.mSuperRapidIcon.setScaleX(0.0f);
        this.mSuperRapidIcon.setScaleY(0.0f);
        this.mCarModeIcon.setScaleX(0.0f);
        this.mCarModeIcon.setScaleY(0.0f);
        this.mNormalIcon.setScaleX(1.0f);
        this.mNormalIcon.setScaleY(1.0f);
        this.mPercentCountView.setScaleX(1.0f);
        this.mPercentCountView.setScaleY(1.0f);
        this.mPercentCountView.setTranslationY((float) this.mChargeNumberTranslateInit);
    }

    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        setAlpha(this.mInitScreenOn ? animatedFraction : 1.0f);
        this.mContentContainer.setScaleX(1.0f);
        this.mContentContainer.setScaleY(1.0f);
        this.mVideoView.setScaleX(1.0f);
        this.mVideoView.setScaleY(1.0f);
        this.mVideoView.setAlpha(animatedFraction);
    }

    public void onAnimationStart(Animator animator) {
        IRapidAnimationListener iRapidAnimationListener = this.animationListener;
        if (iRapidAnimationListener != null) {
            iRapidAnimationListener.onRapidAnimationStart(10);
        }
        this.mContentContainer.setVisibility(8);
        OrientationEventListener orientationEventListener = this.mOrientationEventListener;
        if (orientationEventListener != null && orientationEventListener.canDetectOrientation()) {
            Slog.i("WirelessRapidChargeView", "enable orientation sensor");
            this.mOrientationEventListener.enable();
        }
        this.mHandler.sendEmptyMessageDelayed(R.styleable.AppCompatTheme_textAppearanceListItem, 300);
        this.mHandler.sendEmptyMessageDelayed(R.styleable.AppCompatTheme_textAppearanceListItemSecondary, 2000);
        this.mHandler.removeCallbacks(this.timeoutDismissJob);
        this.mHandler.postDelayed(this.timeoutDismissJob, 19400);
    }

    public void onAnimationEnd(Animator animator) {
        this.mContentContainer.setVisibility(0);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mWindowShouldAdd) {
            removeFromWindow("!mWindowShouldAdd");
        } else {
            checkScreenSize();
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mWindowShouldAdd) {
            addToWindow("mWindowShouldAdd");
        }
        this.mHandler.removeCallbacksAndMessages((Object) null);
        disableOrientationSensor();
    }

    /* access modifiers changed from: private */
    public void disableOrientationSensor() {
        this.mHandler.removeMessages(R.styleable.AppCompatTheme_textAppearanceListItemSecondary);
        this.mHandler.removeMessages(R.styleable.AppCompatTheme_textAppearanceListItem);
        if (this.mOrientationEventListener.canDetectOrientation()) {
            this.mOrientationEventListener.disable();
        }
    }

    public void addToWindow(String str) {
        this.mWindowShouldAdd = true;
        if (!isAttachedToWindow() && getParent() == null) {
            try {
                Log.i("WirelessRapidChargeView", "addToWindow: reason " + str);
                setComponentTransparent(true);
                ChargeUtils.getParentView(this.mContext).addView(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeFromWindow(String str) {
        this.mWindowShouldAdd = false;
        if (isAttachedToWindow()) {
            try {
                Log.i("WirelessRapidChargeView", "removeFromWindow: reason " + str);
                ChargeUtils.getParentView(this.mContext).removeView(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startDismiss(String str) {
        String str2 = str;
        Property property = FrameLayout.SCALE_Y;
        Property property2 = FrameLayout.SCALE_X;
        Property property3 = FrameLayout.ALPHA;
        KeyguardUpdateMonitor.getInstance(getContext()).setShowingChargeAnimationWindow(false);
        if (!this.mStartingDismissWirelessAlphaAnim) {
            ValueAnimator valueAnimator = this.mZoomAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            Log.i("WirelessRapidChargeView", "startDismiss: reason: " + str2);
            this.mDismissReason = str2;
            this.mHandler.removeCallbacks(this.timeoutDismissJob);
            this.mHandler.removeCallbacks(this.mDismissRunnable);
            ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat(property3, new float[]{getAlpha(), 0.0f})}).setDuration(600);
            PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property3, new float[]{this.mContentContainer.getAlpha(), 0.0f});
            PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mContentContainer.getScaleX(), 0.0f});
            PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(property, new float[]{this.mContentContainer.getScaleY(), 0.0f});
            ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mContentContainer, new PropertyValuesHolder[]{ofFloat, ofFloat2, ofFloat3}).setDuration(600);
            PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(property3, new float[]{this.mVideoView.getAlpha(), 0.0f});
            PropertyValuesHolder ofFloat5 = PropertyValuesHolder.ofFloat(property2, new float[]{this.mVideoView.getScaleX(), 0.0f});
            PropertyValuesHolder ofFloat6 = PropertyValuesHolder.ofFloat(property, new float[]{this.mVideoView.getScaleY(), 0.0f});
            ObjectAnimator duration3 = ObjectAnimator.ofPropertyValuesHolder(this.mVideoView, new PropertyValuesHolder[]{ofFloat4, ofFloat5, ofFloat6}).setDuration(600);
            AnimatorSet animatorSet = new AnimatorSet();
            this.mDismissAnimatorSet = animatorSet;
            animatorSet.setInterpolator(this.mQuartOutInterpolator);
            this.mDismissAnimatorSet.playTogether(new Animator[]{duration2, duration3});
            this.mDismissAnimatorSet.play(duration2);
            if (!"dismiss_for_timeout".equals(str2)) {
                this.mDismissAnimatorSet.play(duration).with(duration2);
            }
            this.mDismissAnimatorSet.addListener(new Animator.AnimatorListener() {
                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    boolean unused = WirelessRapidChargeView.this.mStartingDismissWirelessAlphaAnim = false;
                    if (WirelessRapidChargeView.this.animationListener != null) {
                        WirelessRapidChargeView.this.animationListener.onRapidAnimationEnd(10, WirelessRapidChargeView.this.mDismissReason);
                    }
                    WirelessRapidChargeView.this.mHandler.post(WirelessRapidChargeView.this.mDismissRunnable);
                }

                public void onAnimationCancel(Animator animator) {
                    boolean unused = WirelessRapidChargeView.this.mStartingDismissWirelessAlphaAnim = false;
                    if (WirelessRapidChargeView.this.animationListener != null) {
                        WirelessRapidChargeView.this.animationListener.onRapidAnimationEnd(10, WirelessRapidChargeView.this.mDismissReason);
                    }
                    WirelessRapidChargeView.this.mHandler.removeCallbacks(WirelessRapidChargeView.this.mDismissRunnable);
                }
            });
            this.mStartingDismissWirelessAlphaAnim = true;
            this.mDismissAnimatorSet.start();
        }
    }

    /* access modifiers changed from: private */
    public void setComponentTransparent(boolean z) {
        if (z) {
            setAlpha(0.0f);
            this.mContentContainer.setAlpha(0.0f);
            return;
        }
        setAlpha(1.0f);
        this.mContentContainer.setAlpha(1.0f);
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
                    WirelessRapidChargeView.this.startContentSwitchAnimation();
                }
            });
        }
    }

    private void updateSizeForScreenSizeChange() {
        Point point = this.mScreenSize;
        float min = (((float) Math.min(point.x, point.y)) * 1.0f) / 1080.0f;
        this.mPivotX = (int) (100.0f * min);
        this.mChargeNumberTranslateSmall = (int) (-70.0f * min);
        this.mChargeNumberTranslateInit = (int) (-10.0f * min);
        this.mChargeTipTranslateSmall = (int) (-50.0f * min);
        this.mSpaceHeight = (int) (16.0f * min);
        this.mTipTopMargin = (int) (70.0f * min);
        this.mIconPaddingTop = (int) (275.0f * min);
        Drawable drawable = this.mNormalIconDrawable;
        if (drawable != null) {
            this.mNormalIconWidth = (int) (((float) drawable.getIntrinsicWidth()) * min);
            this.mNormalIconHeight = (int) (((float) this.mNormalIconDrawable.getIntrinsicHeight()) * min);
        }
        Drawable drawable2 = this.mSuperRapidIconDrawable;
        if (drawable2 != null) {
            this.mSuperRapidIconWidth = (int) (((float) drawable2.getIntrinsicWidth()) * min);
            this.mSuperRapidIconHeight = (int) (((float) this.mSuperRapidIconDrawable.getIntrinsicHeight()) * min);
        }
        Drawable drawable3 = this.mCarIconDrawable;
        if (drawable3 != null) {
            this.mCarIconWidth = (int) (((float) drawable3.getIntrinsicWidth()) * min);
            this.mCarIconHeight = (int) (min * ((float) this.mCarIconDrawable.getIntrinsicHeight()));
        }
    }

    private void updateLayoutParamForScreenSizeChange() {
        this.mCenterAnchorView.getLayoutParams().height = this.mSpaceHeight;
        ((RelativeLayout.LayoutParams) this.mGtChargeAniView.getLayoutParams()).topMargin = this.mTipTopMargin;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mNormalIcon.getLayoutParams();
        layoutParams.width = this.mNormalIconWidth;
        int i = this.mNormalIconHeight;
        int i2 = this.mIconPaddingTop;
        layoutParams.height = i + i2;
        this.mNormalIcon.setPadding(0, i2, 0, 0);
        this.mNormalIcon.setPivotX((float) this.mPivotX);
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mSuperRapidIcon.getLayoutParams();
        layoutParams2.width = this.mSuperRapidIconWidth;
        int i3 = this.mSuperRapidIconHeight;
        int i4 = this.mIconPaddingTop;
        layoutParams2.height = i3 + i4;
        this.mSuperRapidIcon.setPadding(0, i4, 0, 0);
        this.mSuperRapidIcon.setPivotX((float) this.mPivotX);
        RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) this.mCarModeIcon.getLayoutParams();
        layoutParams3.width = this.mCarIconWidth;
        int i5 = this.mCarIconHeight;
        int i6 = this.mIconPaddingTop;
        layoutParams3.height = i5 + i6;
        this.mCarModeIcon.setPadding(0, i6, 0, 0);
        this.mCarModeIcon.setPivotX((float) this.mPivotX);
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        startDismiss("dismiss_for_key_event");
        InputManager.getInstance().injectInputEvent(keyEvent, 0);
        return false;
    }
}
