package com.android.keyguard.charge.rapid;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import com.android.keyguard.charge.MiuiChargeManager;
import com.android.systemui.Dependency;
import java.util.Locale;

public class PercentCountView extends LinearLayout {
    /* access modifiers changed from: private */
    public ChargeLevelAnimationListener mChargeLevelAnimationListener;
    /* access modifiers changed from: private */
    public int mCurrentProgress;
    /* access modifiers changed from: private */
    public NumberDrawView mIntegerTv;
    private int mLargeTextSizePx;
    private int mPercentTextSizePx;
    private Point mScreenSize;
    private int mSmallTextSizePx;
    private ValueAnimator mValueAnimator;
    private WindowManager mWindowManager;

    public interface ChargeLevelAnimationListener {
        void onChargeLevelAnimationEnd();
    }

    public PercentCountView(Context context) {
        this(context, (AttributeSet) null);
    }

    public PercentCountView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PercentCountView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private void init(Context context) {
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
        this.mIntegerTv = new NumberDrawView(context);
        this.mIntegerTv.setSize(this.mLargeTextSizePx, this.mSmallTextSizePx, this.mPercentTextSizePx);
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
                int i2 = this.mCurrentProgress;
                this.mIntegerTv.setLevelText(String.format(Locale.getDefault(), "%d", new Object[]{Integer.valueOf(i2)}));
            }
        }
    }

    public void startValueAnimation(float f, float f2) {
        if (f >= 0.0f && f < 100.0f) {
            ValueAnimator valueAnimator = this.mValueAnimator;
            if (valueAnimator == null || !valueAnimator.isRunning()) {
                this.mCurrentProgress = (int) f;
                this.mIntegerTv.setLevelText(String.format(Locale.getDefault(), "%1.2f", new Object[]{Float.valueOf(f)}));
                this.mValueAnimator = ValueAnimator.ofFloat(new float[]{f, Math.min(f + f2, 99.99f)});
                this.mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int floatValue = (int) ((Float) valueAnimator.getAnimatedValue()).floatValue();
                        if (floatValue != PercentCountView.this.mCurrentProgress) {
                            ((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).updateBattery(floatValue);
                            int unused = PercentCountView.this.mCurrentProgress = floatValue;
                        }
                        PercentCountView.this.mIntegerTv.setLevelText(String.format(Locale.getDefault(), "%1.2f", new Object[]{valueAnimator.getAnimatedValue()}));
                    }
                });
                this.mValueAnimator.addListener(new Animator.AnimatorListener() {
                    public void onAnimationCancel(Animator animator) {
                    }

                    public void onAnimationRepeat(Animator animator) {
                    }

                    public void onAnimationStart(Animator animator) {
                        ((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).setIsChargeLevelAnimationRunning(true);
                    }

                    public void onAnimationEnd(Animator animator) {
                        ((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).setIsChargeLevelAnimationRunning(false);
                        if (PercentCountView.this.mChargeLevelAnimationListener != null) {
                            PercentCountView.this.mChargeLevelAnimationListener.onChargeLevelAnimationEnd();
                        }
                    }
                });
                this.mValueAnimator.setInterpolator(new LinearInterpolator());
                this.mValueAnimator.setDuration(10000);
                this.mValueAnimator.start();
            }
        }
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
            this.mIntegerTv.setSize(this.mLargeTextSizePx, this.mSmallTextSizePx, this.mPercentTextSizePx);
            requestLayout();
        }
    }

    private void updateSizeForScreenSizeChange() {
        Point point = this.mScreenSize;
        float min = (((float) Math.min(point.x, point.y)) * 1.0f) / 1080.0f;
        this.mLargeTextSizePx = (int) (188.0f * min);
        this.mSmallTextSizePx = (int) (100.0f * min);
        this.mPercentTextSizePx = (int) (min * 64.0f);
    }
}
