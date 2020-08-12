package com.android.systemui.assist.ui;

public final class EdgeLight {
    private int mColor;
    private float mLength;
    private float mOffset;

    public EdgeLight(int i, float f, float f2) {
        this.mColor = i;
        this.mOffset = f;
        this.mLength = f2;
    }

    public int getColor() {
        return this.mColor;
    }

    public void setColor(int i) {
        this.mColor = i;
    }

    public float getLength() {
        return this.mLength;
    }

    public void setLength(float f) {
        this.mLength = f;
    }

    public float getOffset() {
        return this.mOffset;
    }

    public void setOffset(float f) {
        this.mOffset = f;
    }
}
