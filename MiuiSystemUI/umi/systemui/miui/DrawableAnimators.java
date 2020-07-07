package com.android.systemui.miui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawableCompat;
import android.os.Build;
import android.util.Log;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;

public class DrawableAnimators {
    private static Interpolator DECELERATE = Interpolators.DECELERATE_QUART;

    public static Animator fade(Drawable drawable, boolean z) {
        int[] iArr = new int[1];
        iArr[0] = z ? 255 : 0;
        ObjectAnimator ofInt = ObjectAnimator.ofInt(drawable, "alpha", iArr);
        ofInt.setDuration(300);
        ofInt.setInterpolator(DECELERATE);
        ofInt.setAutoCancel(true);
        ofInt.start();
        return ofInt;
    }

    public static Animator updateCornerRadii(Context context, Drawable drawable, int i) {
        TypedArray obtainTypedArray = context.getResources().obtainTypedArray(i);
        int length = obtainTypedArray.length();
        float[] fArr = new float[length];
        for (int i2 = 0; i2 < length; i2++) {
            fArr[i2] = obtainTypedArray.getDimension(i2, 0.0f);
        }
        obtainTypedArray.recycle();
        return updateCornerRadii(drawable, fArr);
    }

    public static Animator updateCornerRadii(Drawable drawable, float[] fArr) {
        if (!(drawable instanceof GradientDrawable)) {
            Log.e("DrawableAnimatorHelper", "cornerRadii change cannot be applied to " + drawable);
            return null;
        }
        ObjectAnimator ofObject = ObjectAnimator.ofObject(drawable, "cornerRadii", new CornerRadiiTypeEvaluator(drawable), new Object[]{fArr});
        ofObject.setDuration(300);
        ofObject.setInterpolator(DECELERATE);
        ofObject.setAutoCancel(true);
        ofObject.start();
        return ofObject;
    }

    private static class CornerRadiiTypeEvaluator implements TypeEvaluator<float[]> {
        private float[] mFallbackStartValue;
        private float[] mResult;

        CornerRadiiTypeEvaluator(Drawable drawable) {
            if (drawable instanceof GradientDrawable) {
                if (Build.VERSION.SDK_INT < 24) {
                    float[] cornerRadii = GradientDrawableCompat.getCornerRadii((GradientDrawable) drawable);
                    this.mFallbackStartValue = cornerRadii;
                    if (cornerRadii != null) {
                        return;
                    }
                }
                this.mFallbackStartValue = getFallBackArray(drawable);
            }
        }

        private float[] getFallBackArray(Drawable drawable) {
            float cornerRadius = GradientDrawableCompat.getCornerRadius((GradientDrawable) drawable);
            return new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius};
        }

        public float[] evaluate(float f, float[] fArr, float[] fArr2) {
            if (fArr == null) {
                fArr = this.mFallbackStartValue;
            }
            if (this.mResult == null) {
                this.mResult = new float[fArr.length];
            }
            int i = 0;
            while (true) {
                float[] fArr3 = this.mResult;
                if (i >= fArr3.length) {
                    return fArr3;
                }
                fArr3[i] = fArr[i] + ((fArr2[i] - fArr[i]) * f);
                i++;
            }
        }
    }
}
