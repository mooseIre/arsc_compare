package com.android.systemui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.View;
import java.util.ArrayList;

public class ViewInvertHelper {
    /* access modifiers changed from: private */
    public final Paint mDarkPaint;
    private final long mFadeDuration;
    private final ColorMatrix mGrayscaleMatrix;
    private final ColorMatrix mMatrix;
    /* access modifiers changed from: private */
    public final ArrayList<View> mTargets;

    public ViewInvertHelper(View view, long j) {
        this(view.getContext(), j);
        addTarget(view);
    }

    public ViewInvertHelper(Context context, long j) {
        this.mDarkPaint = new Paint();
        this.mMatrix = new ColorMatrix();
        this.mGrayscaleMatrix = new ColorMatrix();
        this.mTargets = new ArrayList<>();
        this.mFadeDuration = j;
    }

    public void addTarget(View view) {
        this.mTargets.add(view);
    }

    public void fade(final boolean z, long j) {
        float f = 0.0f;
        float f2 = z ? 0.0f : 1.0f;
        if (z) {
            f = 1.0f;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{f2, f});
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewInvertHelper.this.updateInvertPaint(((Float) valueAnimator.getAnimatedValue()).floatValue());
                for (int i = 0; i < ViewInvertHelper.this.mTargets.size(); i++) {
                    ((View) ViewInvertHelper.this.mTargets.get(i)).setLayerType(2, ViewInvertHelper.this.mDarkPaint);
                }
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (!z) {
                    for (int i = 0; i < ViewInvertHelper.this.mTargets.size(); i++) {
                        ((View) ViewInvertHelper.this.mTargets.get(i)).setLayerType(0, (Paint) null);
                    }
                }
            }
        });
        ofFloat.setDuration(this.mFadeDuration);
        ofFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        ofFloat.setStartDelay(j);
        ofFloat.start();
    }

    public void update(boolean z) {
        if (z) {
            updateInvertPaint(1.0f);
            for (int i = 0; i < this.mTargets.size(); i++) {
                this.mTargets.get(i).setLayerType(2, this.mDarkPaint);
            }
            return;
        }
        for (int i2 = 0; i2 < this.mTargets.size(); i2++) {
            this.mTargets.get(i2).setLayerType(0, (Paint) null);
        }
    }

    /* access modifiers changed from: private */
    public void updateInvertPaint(float f) {
        float f2 = 1.0f - (2.0f * f);
        float f3 = 255.0f * f;
        this.mMatrix.set(new float[]{f2, 0.0f, 0.0f, 0.0f, f3, 0.0f, f2, 0.0f, 0.0f, f3, 0.0f, 0.0f, f2, 0.0f, f3, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
        this.mGrayscaleMatrix.setSaturation(1.0f - f);
        this.mMatrix.preConcat(this.mGrayscaleMatrix);
        this.mDarkPaint.setColorFilter(new ColorMatrixColorFilter(this.mMatrix));
    }

    public void setInverted(boolean z, boolean z2, long j) {
        if (z2) {
            fade(z, j);
        } else {
            update(z);
        }
    }
}
