package com.android.systemui.qs;

import android.graphics.Path;
import android.view.animation.BaseInterpolator;
import android.view.animation.Interpolator;

public class PathInterpolatorBuilder {
    private float[] mDist;
    private float[] mX;
    private float[] mY;

    public PathInterpolatorBuilder(float f, float f2, float f3, float f4) {
        initCubic(f, f2, f3, f4);
    }

    private void initCubic(float f, float f2, float f3, float f4) {
        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        path.cubicTo(f, f2, f3, f4, 1.0f, 1.0f);
        initPath(path);
    }

    private void initPath(Path path) {
        float[] approximate = path.approximate(0.002f);
        int length = approximate.length / 3;
        float f = 0.0f;
        if (approximate[1] == 0.0f && approximate[2] == 0.0f && approximate[approximate.length - 2] == 1.0f && approximate[approximate.length - 1] == 1.0f) {
            this.mX = new float[length];
            this.mY = new float[length];
            this.mDist = new float[length];
            int i = 0;
            int i2 = 0;
            float f2 = 0.0f;
            while (i < length) {
                int i3 = i2 + 1;
                float f3 = approximate[i2];
                int i4 = i3 + 1;
                float f4 = approximate[i3];
                int i5 = i4 + 1;
                float f5 = approximate[i4];
                if (f3 == f && f4 != f2) {
                    throw new IllegalArgumentException("The Path cannot have discontinuity in the X axis.");
                } else if (f4 >= f2) {
                    float[] fArr = this.mX;
                    fArr[i] = f4;
                    float[] fArr2 = this.mY;
                    fArr2[i] = f5;
                    if (i > 0) {
                        int i6 = i - 1;
                        float f6 = fArr[i] - fArr[i6];
                        float f7 = fArr2[i] - fArr2[i6];
                        float[] fArr3 = this.mDist;
                        fArr3[i] = fArr3[i6] + ((float) Math.sqrt((double) ((f6 * f6) + (f7 * f7))));
                    }
                    i++;
                    f = f3;
                    f2 = f4;
                    i2 = i5;
                } else {
                    throw new IllegalArgumentException("The Path cannot loop back on itself.");
                }
            }
            float[] fArr4 = this.mDist;
            float f8 = fArr4[fArr4.length - 1];
            for (int i7 = 0; i7 < length; i7++) {
                float[] fArr5 = this.mDist;
                fArr5[i7] = fArr5[i7] / f8;
            }
            return;
        }
        throw new IllegalArgumentException("The Path must start at (0,0) and end at (1,1)");
    }

    public Interpolator getXInterpolator() {
        return new PathInterpolator(this.mDist, this.mX);
    }

    public Interpolator getYInterpolator() {
        return new PathInterpolator(this.mDist, this.mY);
    }

    private static class PathInterpolator extends BaseInterpolator {
        private final float[] mX;
        private final float[] mY;

        private PathInterpolator(float[] fArr, float[] fArr2) {
            this.mX = fArr;
            this.mY = fArr2;
        }

        public float getInterpolation(float f) {
            if (f <= 0.0f) {
                return 0.0f;
            }
            if (f >= 1.0f) {
                return 1.0f;
            }
            int i = 0;
            int length = this.mX.length - 1;
            while (length - i > 1) {
                int i2 = (i + length) / 2;
                if (f < this.mX[i2]) {
                    length = i2;
                } else {
                    i = i2;
                }
            }
            float[] fArr = this.mX;
            float f2 = fArr[length] - fArr[i];
            if (f2 == 0.0f) {
                return this.mY[i];
            }
            float[] fArr2 = this.mY;
            float f3 = fArr2[i];
            return f3 + (((f - fArr[i]) / f2) * (fArr2[length] - f3));
        }
    }
}
