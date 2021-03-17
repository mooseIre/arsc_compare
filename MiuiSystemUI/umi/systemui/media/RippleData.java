package com.android.systemui.media;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: LightSourceDrawable.kt */
public final class RippleData {
    private float alpha;
    private float highlight;
    private float maxSize;
    private float minSize;
    private float progress;
    private float x;
    private float y;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RippleData)) {
            return false;
        }
        RippleData rippleData = (RippleData) obj;
        return Float.compare(this.x, rippleData.x) == 0 && Float.compare(this.y, rippleData.y) == 0 && Float.compare(this.alpha, rippleData.alpha) == 0 && Float.compare(this.progress, rippleData.progress) == 0 && Float.compare(this.minSize, rippleData.minSize) == 0 && Float.compare(this.maxSize, rippleData.maxSize) == 0 && Float.compare(this.highlight, rippleData.highlight) == 0;
    }

    public int hashCode() {
        return (((((((((((Float.hashCode(this.x) * 31) + Float.hashCode(this.y)) * 31) + Float.hashCode(this.alpha)) * 31) + Float.hashCode(this.progress)) * 31) + Float.hashCode(this.minSize)) * 31) + Float.hashCode(this.maxSize)) * 31) + Float.hashCode(this.highlight);
    }

    @NotNull
    public String toString() {
        return "RippleData(x=" + this.x + ", y=" + this.y + ", alpha=" + this.alpha + ", progress=" + this.progress + ", minSize=" + this.minSize + ", maxSize=" + this.maxSize + ", highlight=" + this.highlight + ")";
    }

    public RippleData(float f, float f2, float f3, float f4, float f5, float f6, float f7) {
        this.x = f;
        this.y = f2;
        this.alpha = f3;
        this.progress = f4;
        this.minSize = f5;
        this.maxSize = f6;
        this.highlight = f7;
    }

    public final float getX() {
        return this.x;
    }

    public final void setX(float f) {
        this.x = f;
    }

    public final float getY() {
        return this.y;
    }

    public final void setY(float f) {
        this.y = f;
    }

    public final float getAlpha() {
        return this.alpha;
    }

    public final void setAlpha(float f) {
        this.alpha = f;
    }

    public final float getProgress() {
        return this.progress;
    }

    public final void setProgress(float f) {
        this.progress = f;
    }

    public final float getMinSize() {
        return this.minSize;
    }

    public final void setMinSize(float f) {
        this.minSize = f;
    }

    public final float getMaxSize() {
        return this.maxSize;
    }

    public final void setMaxSize(float f) {
        this.maxSize = f;
    }

    public final void setHighlight(float f) {
        this.highlight = f;
    }
}
