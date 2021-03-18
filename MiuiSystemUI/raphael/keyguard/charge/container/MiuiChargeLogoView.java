package com.android.keyguard.charge.container;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.charge.ChargeUtils;
import com.android.keyguard.charge.view.MiuiChargeTurboView;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0021R$string;
import miui.maml.animation.interpolater.CubicEaseOutInterpolater;

public class MiuiChargeLogoView extends RelativeLayout {
    private AnimatorSet mAnimatorSet;
    private int mChargeSpeed;
    private int mChargeTipTranslateSmall;
    private MiuiChargeTurboView mChargeTurboView;
    private Interpolator mCubicInterpolator;
    private boolean mIsFoldChargeVideo;
    private Point mScreenSize;
    private int mSpeedTipTextSizePx;
    private TextView mStateTip;
    private int mStateTipAlpha;
    private int mStateTipTranslationY;
    private int mTipTopMargin;
    private int mTurboViewAlpha;
    private int mTurboViewTranslationY;
    private WindowManager mWindowManager;
    private int mWireState;

    public MiuiChargeLogoView(Context context) {
        this(context, null);
    }

    public MiuiChargeLogoView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiChargeLogoView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCubicInterpolator = new CubicEaseOutInterpolater();
        this.mIsFoldChargeVideo = false;
        this.mChargeSpeed = 0;
        this.mWireState = -1;
        init(context);
    }

    private void init(Context context) {
        this.mIsFoldChargeVideo = context.getResources().getBoolean(C0010R$bool.config_folding_charge_video);
        setLayoutDirection(0);
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mScreenSize = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        updateSizeForScreenSizeChange();
        AccessibilityDisableTextView accessibilityDisableTextView = new AccessibilityDisableTextView(context);
        this.mStateTip = accessibilityDisableTextView;
        accessibilityDisableTextView.setTextSize(0, (float) this.mSpeedTipTextSizePx);
        this.mStateTip.setIncludeFontPadding(false);
        this.mStateTip.setTextColor(Color.parseColor("#8CFFFFFF"));
        this.mStateTip.setGravity(17);
        this.mStateTip.setText(getResources().getString(C0021R$string.rapid_charge_mode_tip));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams.addRule(14);
        layoutParams.topMargin = this.mTipTopMargin;
        addView(this.mStateTip, layoutParams);
        this.mChargeTurboView = new MiuiChargeTurboView(context);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams2.addRule(14);
        layoutParams2.topMargin = this.mTipTopMargin;
        this.mChargeTurboView.setVisibility(8);
        this.mChargeTurboView.setViewInitState();
        addView(this.mChargeTurboView, layoutParams2);
    }

    public void startLogoAnimation(boolean z) {
        Log.d("MiuiChargeLogoView", "startLogoAnimation: mChargeSpeed=" + this.mChargeSpeed);
        this.mWireState = ChargeUtils.sBatteryStatus.wireState;
        resetLogoViewState(z);
    }

    public void switchLogoAnimation(int i) {
        Log.d("MiuiChargeLogoView", "switchLogoAnimation: mChargeSpeed=" + i);
        this.mChargeSpeed = i;
        this.mWireState = ChargeUtils.sBatteryStatus.wireState;
        switchChargeLogo();
    }

    private void resetAllProperty() {
        this.mStateTipTranslationY = 0;
        this.mStateTipAlpha = 0;
        this.mTurboViewTranslationY = 0;
        this.mTurboViewAlpha = 0;
    }

    private void switchChargeLogo() {
        Property property = RelativeLayout.ALPHA;
        Property property2 = RelativeLayout.TRANSLATION_Y;
        Log.d("MiuiChargeLogoView", "switchChargeLogo: mChargeSpeed=" + this.mChargeSpeed);
        AnimatorSet animatorSet = this.mAnimatorSet;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        resetAllProperty();
        if (1 != this.mChargeSpeed || ChargeUtils.supportWaveChargeAnimation()) {
            int i = this.mChargeSpeed;
            if (2 == i || 3 == i) {
                this.mTurboViewTranslationY = this.mChargeTipTranslateSmall;
                this.mTurboViewAlpha = 1;
            }
        } else {
            this.mStateTipTranslationY = this.mChargeTipTranslateSmall;
            this.mStateTipAlpha = 1;
        }
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property2, this.mStateTip.getTranslationY(), (float) this.mStateTipTranslationY);
        PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(property, this.mStateTip.getAlpha(), (float) this.mStateTipAlpha);
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this.mStateTip, ofFloat2, ofFloat).setDuration(500L);
        duration.setInterpolator(this.mCubicInterpolator);
        PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(property2, this.mChargeTurboView.getTranslationY(), (float) this.mTurboViewTranslationY);
        PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(property, this.mChargeTurboView.getAlpha(), (float) this.mTurboViewAlpha);
        ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mChargeTurboView, ofFloat4, ofFloat3).setDuration(250L);
        duration2.setInterpolator(this.mCubicInterpolator);
        duration2.addListener(new Animator.AnimatorListener() {
            /* class com.android.keyguard.charge.container.MiuiChargeLogoView.AnonymousClass1 */

            public void onAnimationStart(Animator animator) {
                if (2 == MiuiChargeLogoView.this.mChargeSpeed || 3 == MiuiChargeLogoView.this.mChargeSpeed) {
                    MiuiChargeLogoView.this.mChargeTurboView.setVisibility(8);
                }
            }

            public void onAnimationEnd(Animator animator) {
                if (2 == MiuiChargeLogoView.this.mChargeSpeed || 3 == MiuiChargeLogoView.this.mChargeSpeed) {
                    MiuiChargeLogoView.this.mChargeTurboView.setVisibility(0);
                    if (3 == MiuiChargeLogoView.this.mChargeSpeed) {
                        MiuiChargeLogoView.this.mChargeTurboView.setStrongViewInitState();
                        if (MiuiChargeLogoView.this.mWireState == 10) {
                            MiuiChargeLogoView.this.mChargeTurboView.animationWirelessStrongToShow();
                        } else if (MiuiChargeLogoView.this.mWireState == 11) {
                            MiuiChargeLogoView.this.mChargeTurboView.animationWiredStrongToShow();
                        }
                    } else {
                        MiuiChargeLogoView.this.mChargeTurboView.setViewInitState();
                        MiuiChargeLogoView.this.mChargeTurboView.animationToShow();
                    }
                }
            }

            public void onAnimationCancel(Animator animator) {
                MiuiChargeLogoView.this.mChargeTurboView.setVisibility(8);
            }

            public void onAnimationRepeat(Animator animator) {
                if (2 == MiuiChargeLogoView.this.mChargeSpeed || 3 == MiuiChargeLogoView.this.mChargeSpeed) {
                    MiuiChargeLogoView.this.mChargeTurboView.setVisibility(8);
                }
            }
        });
        AnimatorSet animatorSet2 = new AnimatorSet();
        this.mAnimatorSet = animatorSet2;
        animatorSet2.setStartDelay((long) ChargeUtils.getWaveItemDelayTime());
        this.mAnimatorSet.playTogether(duration, duration2);
        this.mAnimatorSet.start();
    }

    private void resetLogoViewState(boolean z) {
        Log.d("MiuiChargeLogoView", "resetLogoViewState: mChargeSpeed=" + this.mChargeSpeed + ",clickShow=" + z);
        int i = this.mChargeSpeed;
        if (i == 0) {
            this.mStateTip.setAlpha(0.0f);
            this.mStateTip.setTranslationY(0.0f);
            this.mChargeTurboView.setViewInitState();
            this.mChargeTurboView.setVisibility(8);
        } else if (i == 1) {
            if (ChargeUtils.supportWaveChargeAnimation()) {
                this.mStateTip.setAlpha(0.0f);
            } else {
                this.mStateTip.setAlpha(1.0f);
            }
            this.mStateTip.setTranslationY((float) this.mChargeTipTranslateSmall);
            this.mChargeTurboView.setViewInitState();
            this.mChargeTurboView.setVisibility(8);
        } else if (i == 2) {
            this.mStateTip.setAlpha(0.0f);
            this.mStateTip.setTranslationY((float) this.mChargeTipTranslateSmall);
            this.mChargeTurboView.setVisibility(0);
            if (z) {
                this.mChargeTurboView.setViewShowState();
            } else {
                this.mChargeTurboView.setViewInitState();
            }
        } else if (i == 3) {
            this.mStateTip.setAlpha(0.0f);
            this.mStateTip.setTranslationY((float) this.mChargeTipTranslateSmall);
            this.mChargeTurboView.setStrongViewInitState();
            this.mChargeTurboView.setVisibility(0);
            if (z) {
                int i2 = this.mWireState;
                if (i2 == 10) {
                    this.mChargeTurboView.setWirelessStrongViewShowState();
                } else if (i2 == 11) {
                    this.mChargeTurboView.setWiredStrongViewShowState();
                }
            } else {
                this.mChargeTurboView.setStrongViewInitState();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mStateTip.setText(getResources().getString(C0021R$string.rapid_charge_mode_tip));
        checkScreenSize();
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
        }
    }

    private void updateSizeForScreenSizeChange() {
        Point point = this.mScreenSize;
        float f = 1.0f;
        float min = (((float) Math.min(point.x, point.y)) * 1.0f) / 1080.0f;
        if (this.mIsFoldChargeVideo) {
            if (min <= 1.0f) {
                f = min;
            }
            min = f;
        }
        this.mSpeedTipTextSizePx = (int) (34.485f * min);
        this.mTipTopMargin = (int) (90.0f * min);
        this.mChargeTipTranslateSmall = (int) (min * 0.0f);
    }

    private void updateLayoutParamForScreenSizeChange() {
        this.mStateTip.setTextSize(0, (float) this.mSpeedTipTextSizePx);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mStateTip.getLayoutParams();
        layoutParams.topMargin = this.mTipTopMargin;
        this.mStateTip.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mChargeTurboView.getLayoutParams();
        layoutParams2.topMargin = this.mTipTopMargin;
        this.mChargeTurboView.setLayoutParams(layoutParams2);
    }

    /* access modifiers changed from: private */
    public static class AccessibilityDisableTextView extends TextView {
        public AccessibilityDisableTextView(Context context) {
            super(context);
        }
    }
}
