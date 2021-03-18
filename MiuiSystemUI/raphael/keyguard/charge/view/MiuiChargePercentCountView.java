package com.android.keyguard.charge.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import androidx.constraintlayout.widget.R$styleable;
import com.android.keyguard.charge.ChargeUtils;
import com.android.keyguard.charge.MiuiChargeManager;
import com.android.systemui.C0010R$bool;
import com.android.systemui.Dependency;
import java.util.Locale;
import miui.maml.animation.interpolater.CubicEaseOutInterpolater;
import miui.maml.animation.interpolater.QuartEaseOutInterpolater;

public class MiuiChargePercentCountView extends LinearLayout {
    private ChargeLevelAnimationListener mChargeLevelAnimationListener;
    private int mChargeNumberTranslateInit;
    private int mChargeNumberTranslateSmall;
    private int mChargeSpeed;
    private AnimatorSet mContentSwitchAnimator;
    private Interpolator mCubicInterpolator;
    private int mCurrentProgress;
    private NumberDrawView mIntegerTv;
    private boolean mIsFoldChargeVideo;
    private int mLargeTextSizePx;
    private int mPercentTextSizePx;
    private Interpolator mQuartOutInterpolator;
    private Point mScreenSize;
    private int mSmallTextSizePx;
    private ValueAnimator mValueAnimator;
    private WindowManager mWindowManager;

    public interface ChargeLevelAnimationListener {
        void onChargeLevelAnimationEnd();
    }

    public MiuiChargePercentCountView(Context context) {
        this(context, null);
    }

    public MiuiChargePercentCountView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiChargePercentCountView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCubicInterpolator = new CubicEaseOutInterpolater();
        this.mQuartOutInterpolator = new QuartEaseOutInterpolater();
        this.mIsFoldChargeVideo = false;
        this.mChargeSpeed = 0;
        init(context);
    }

    private void init(Context context) {
        this.mIsFoldChargeVideo = context.getResources().getBoolean(C0010R$bool.config_folding_charge_video);
        setLayoutDirection(0);
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mScreenSize = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        updateSizeForScreenSizeChange();
        Typeface createFromAsset = Typeface.createFromAsset(context.getAssets(), "fonts/Mitype2018-35.otf");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        this.mCurrentProgress = 0;
        setOrientation(0);
        setGravity(81);
        NumberDrawView numberDrawView = new NumberDrawView(context);
        this.mIntegerTv = numberDrawView;
        numberDrawView.setSize(this.mLargeTextSizePx, this.mSmallTextSizePx, this.mPercentTextSizePx);
        this.mIntegerTv.setTextColor(Color.parseColor("#FFFFFF"));
        if (createFromAsset != null) {
            this.mIntegerTv.setTypeface(createFromAsset);
        }
        addView(this.mIntegerTv, layoutParams);
        setProgress(0);
    }

    /* access modifiers changed from: protected */
    public void setTextSize(int i, int i2, int i3) {
        this.mIntegerTv.setSize(i, i2, i3);
    }

    public void setProgress(int i) {
        ValueAnimator valueAnimator;
        ValueAnimator valueAnimator2;
        if (i >= 0 && i <= 100) {
            if (i == 100 || (valueAnimator2 = this.mValueAnimator) == null || !valueAnimator2.isRunning()) {
                if (i == 100 && (valueAnimator = this.mValueAnimator) != null) {
                    valueAnimator.cancel();
                }
                this.mCurrentProgress = i;
                this.mIntegerTv.setLevelText(String.format(Locale.getDefault(), "%d", Integer.valueOf(i)));
            }
        }
    }

    public void startValueAnimation(float f, float f2) {
        if (f >= 0.0f && f < 100.0f) {
            ValueAnimator valueAnimator = this.mValueAnimator;
            if (valueAnimator == null || !valueAnimator.isRunning()) {
                this.mCurrentProgress = (int) f;
                this.mIntegerTv.setLevelText(String.format(Locale.getDefault(), "%1.2f", Float.valueOf(f)));
                ValueAnimator ofFloat = ValueAnimator.ofFloat(f, Math.min(f + f2, 99.99f));
                this.mValueAnimator = ofFloat;
                ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class com.android.keyguard.charge.view.MiuiChargePercentCountView.AnonymousClass1 */

                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int floatValue = (int) ((Float) valueAnimator.getAnimatedValue()).floatValue();
                        if (floatValue != MiuiChargePercentCountView.this.mCurrentProgress) {
                            ((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).updateBattery(floatValue);
                            MiuiChargePercentCountView.this.mCurrentProgress = floatValue;
                        }
                        MiuiChargePercentCountView.this.mIntegerTv.setLevelText(String.format(Locale.getDefault(), "%1.2f", valueAnimator.getAnimatedValue()));
                    }
                });
                this.mValueAnimator.addListener(new Animator.AnimatorListener() {
                    /* class com.android.keyguard.charge.view.MiuiChargePercentCountView.AnonymousClass2 */

                    public void onAnimationCancel(Animator animator) {
                    }

                    public void onAnimationRepeat(Animator animator) {
                    }

                    public void onAnimationStart(Animator animator) {
                        ((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).setIsChargeLevelAnimationRunning(true);
                    }

                    public void onAnimationEnd(Animator animator) {
                        ((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).setIsChargeLevelAnimationRunning(false);
                        if (MiuiChargePercentCountView.this.mChargeLevelAnimationListener != null) {
                            MiuiChargePercentCountView.this.mChargeLevelAnimationListener.onChargeLevelAnimationEnd();
                        }
                    }
                });
                this.mValueAnimator.setInterpolator(new LinearInterpolator());
                this.mValueAnimator.setDuration(10000L);
                this.mValueAnimator.start();
            }
        }
    }

    public void startPercentViewAnimation(boolean z) {
        Log.d("MiuiChargePercentCountView", "startPercentViewAnimation: " + this.mChargeSpeed);
        resetViewState(z);
    }

    public void switchPercentViewAnimation(int i) {
        Log.d("MiuiChargePercentCountView", "switchPercentViewAnimation: " + i);
        this.mChargeSpeed = i;
        startAnimation();
    }

    private void startAnimation() {
        if (ChargeUtils.supportWaveChargeAnimation()) {
            startWaveTextAnimation();
        } else {
            switchAnimation();
        }
    }

    /* access modifiers changed from: protected */
    public void resetViewState(boolean z) {
        Log.d("MiuiChargePercentCountView", "resetViewState: chargeSpeed= " + this.mChargeSpeed + ",clickShow=" + z);
        if (this.mChargeSpeed == 0) {
            setScaleX(1.0f);
            setScaleY(1.0f);
            setTranslationY((float) this.mChargeNumberTranslateInit);
        } else {
            setScaleX(0.85f);
            setScaleY(0.85f);
            setTranslationY((float) this.mChargeNumberTranslateSmall);
        }
        if (ChargeUtils.supportWaveChargeAnimation()) {
            setTextSize(265, R$styleable.Constraint_layout_goneMarginStart, R$styleable.Constraint_layout_goneMarginStart);
            if (z) {
                setTranslationY(-100.0f);
            } else {
                setTranslationY(-80.0f);
            }
        } else {
            setTextSize(this.mLargeTextSizePx, this.mSmallTextSizePx, this.mPercentTextSizePx);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void switchAnimation() {
        float f;
        int i;
        Log.d("MiuiChargePercentCountView", "switchAnimation: chargeSpeed=" + this.mChargeSpeed);
        AnimatorSet animatorSet = this.mContentSwitchAnimator;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        float f2 = 0.85f;
        if (ChargeUtils.supportWaveChargeAnimation()) {
            f2 = 0.75f;
            f = -100.0f;
        } else {
            int i2 = this.mChargeSpeed;
            if (i2 == 0) {
                setScaleX(1.0f);
                setScaleY(1.0f);
                setTranslationY((float) this.mChargeNumberTranslateInit);
                f = (float) this.mChargeNumberTranslateInit;
                f2 = 1.0f;
            } else {
                if (i2 == 1) {
                    setScaleX(1.0f);
                    setScaleY(1.0f);
                    setTranslationY((float) this.mChargeNumberTranslateInit);
                    i = this.mChargeNumberTranslateSmall;
                } else {
                    i = this.mChargeNumberTranslateSmall;
                }
                f = (float) i;
            }
        }
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat(LinearLayout.SCALE_X, getScaleX(), f2), PropertyValuesHolder.ofFloat(LinearLayout.SCALE_Y, getScaleY(), f2), PropertyValuesHolder.ofFloat(LinearLayout.TRANSLATION_Y, getTranslationY(), f)).setDuration(500L);
        AnimatorSet animatorSet2 = new AnimatorSet();
        this.mContentSwitchAnimator = animatorSet2;
        animatorSet2.setInterpolator(this.mCubicInterpolator);
        this.mContentSwitchAnimator.play(duration);
        this.mContentSwitchAnimator.start();
    }

    private void startWaveTextAnimation() {
        Log.d("MiuiChargePercentCountView", "startWaveTextAnimation: chargeSpeed= " + this.mChargeSpeed);
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat(LinearLayout.ALPHA, getAlpha(), 1.0f)).setDuration(800L);
        duration.setInterpolator(this.mQuartOutInterpolator);
        duration.setStartDelay((long) ChargeUtils.getWaveTextDelayTime());
        duration.addListener(new Animator.AnimatorListener() {
            /* class com.android.keyguard.charge.view.MiuiChargePercentCountView.AnonymousClass3 */

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                MiuiChargePercentCountView.this.switchAnimation();
            }
        });
        duration.start();
    }

    public void stopValueAnimation() {
        ValueAnimator valueAnimator = this.mValueAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
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
            this.mIntegerTv.updateSizeForScreenSizeChange(this.mLargeTextSizePx, this.mSmallTextSizePx, this.mPercentTextSizePx);
            requestLayout();
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
        this.mLargeTextSizePx = (int) (188.0f * min);
        this.mSmallTextSizePx = (int) (100.0f * min);
        this.mPercentTextSizePx = (int) (64.0f * min);
        this.mChargeNumberTranslateSmall = (int) (-70.0f * min);
        this.mChargeNumberTranslateInit = (int) (min * -10.0f);
    }
}
