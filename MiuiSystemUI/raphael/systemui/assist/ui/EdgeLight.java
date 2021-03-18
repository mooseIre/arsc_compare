package com.android.systemui.assist.ui;

import android.util.Log;

public final class EdgeLight {
    private int mColor;
    private float mLength;
    private float mStart;

    public EdgeLight(int i, float f, float f2) {
        this.mColor = i;
        this.mStart = f;
        this.mLength = f2;
    }

    public int getColor() {
        return this.mColor;
    }

    public boolean setColor(int i) {
        boolean z = this.mColor != i;
        this.mColor = i;
        return z;
    }

    public float getLength() {
        return this.mLength;
    }

    public void setEndpoints(float f, float f2) {
        if (f > f2) {
            Log.e("EdgeLight", String.format("Endpoint must be >= start (add 1 if necessary). Got [%f, %f]", Float.valueOf(f), Float.valueOf(f2)));
            return;
        }
        this.mStart = f;
        this.mLength = f2 - f;
    }

    public float getStart() {
        return this.mStart;
    }
}
