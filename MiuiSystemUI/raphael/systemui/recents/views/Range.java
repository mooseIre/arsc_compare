package com.android.systemui.recents.views;

/* compiled from: TaskStackLayoutAlgorithm */
class Range {
    float max;
    float min;
    float origin;
    final float relativeMax;
    final float relativeMin;

    public float getAbsoluteX(float f) {
        return 0.0f;
    }

    public Range(float f, float f2) {
        this.relativeMin = f;
        this.min = f;
        this.relativeMax = f2;
        this.max = f2;
    }

    public void offset(float f) {
        this.origin = f;
        this.min = this.relativeMin + f;
        this.max = f + this.relativeMax;
    }

    public float getNormalizedX(float f) {
        float f2;
        float f3;
        float f4 = this.origin;
        if (f < f4) {
            f2 = (f - f4) * 0.5f;
            f3 = -this.relativeMin;
        } else {
            f2 = (f - f4) * 0.5f;
            f3 = this.relativeMax;
        }
        return (f2 / f3) + 1.5f;
    }

    public boolean isInRange(float f) {
        double d = (double) f;
        return d >= Math.floor((double) this.min) && d <= Math.ceil((double) this.max);
    }
}
