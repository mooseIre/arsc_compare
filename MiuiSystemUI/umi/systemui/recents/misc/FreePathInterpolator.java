package com.android.systemui.recents.misc;

import android.graphics.Path;
import android.view.animation.BaseInterpolator;

public class FreePathInterpolator extends BaseInterpolator {
    private float mArcLength;
    private float[] mX;
    private float[] mY;

    public float getArcLength() {
        return 2.0f;
    }

    public FreePathInterpolator(Path path) {
        initPath(path);
    }

    private void initPath(Path path) {
        float[] approximate = path.approximate(0.002f);
        int length = approximate.length / 3;
        this.mX = new float[length];
        this.mY = new float[length];
        float f = 0.0f;
        this.mArcLength = 0.0f;
        float f2 = 0.0f;
        int i = 0;
        int i2 = 0;
        float f3 = 0.0f;
        while (i < length) {
            int i3 = i2 + 1;
            float f4 = approximate[i2];
            int i4 = i3 + 1;
            float f5 = approximate[i3];
            int i5 = i4 + 1;
            float f6 = approximate[i4];
            if (f4 == f && f5 != f3) {
                throw new IllegalArgumentException("The Path cannot have discontinuity in the X axis.");
            } else if (f5 >= f3) {
                this.mX[i] = f5;
                this.mY[i] = f6;
                this.mArcLength = (float) (((double) this.mArcLength) + Math.hypot((double) (f5 - f3), (double) (f6 - f2)));
                i++;
                f = f4;
                f3 = f5;
                f2 = f6;
                i2 = i5;
            } else {
                throw new IllegalArgumentException("The Path cannot loop back on itself.");
            }
        }
    }

    public float getInterpolation(float f) {
        float[] fArr = this.mX;
        int length = fArr.length - 1;
        int i = 0;
        if (f <= fArr[0]) {
            return this.mY[0];
        }
        if (f >= fArr[length]) {
            return this.mY[length];
        }
        while (length - i > 1) {
            int i2 = (i + length) / 2;
            if (f < this.mX[i2]) {
                length = i2;
            } else {
                i = i2;
            }
        }
        float[] fArr2 = this.mX;
        float f2 = fArr2[length] - fArr2[i];
        if (f2 == 0.0f) {
            return this.mY[i];
        }
        float[] fArr3 = this.mY;
        float f3 = fArr3[i];
        return f3 + (((f - fArr2[i]) / f2) * (fArr3[length] - f3));
    }

    public float getX(float f) {
        int length = this.mY.length - 1;
        if (f <= 0.0f) {
            return this.mX[length];
        }
        int i = 0;
        if (f >= 1.0f) {
            return this.mX[0];
        }
        while (length - i > 1) {
            int i2 = (i + length) / 2;
            if (f < this.mY[i2]) {
                i = i2;
            } else {
                length = i2;
            }
        }
        float[] fArr = this.mY;
        float f2 = fArr[length] - fArr[i];
        if (f2 == 0.0f) {
            return this.mX[i];
        }
        float[] fArr2 = this.mX;
        float f3 = fArr2[i];
        return f3 + (((f - fArr[i]) / f2) * (fArr2[length] - f3));
    }
}
