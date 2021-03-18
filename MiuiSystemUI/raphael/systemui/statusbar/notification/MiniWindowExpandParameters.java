package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiniWindowExpandParameters.kt */
public final class MiniWindowExpandParameters extends ActivityLaunchAnimator.ExpandAnimationParameters {
    private float alpha;
    private float backgroundAlpha;
    private int startHeight;

    public final void setIconAlpha(float f) {
    }

    public final void setStartWidth(int i) {
    }

    public final float getAlpha() {
        return this.alpha;
    }

    public final void setAlpha(float f) {
        this.alpha = f;
    }

    public final float getBackgroundAlpha() {
        return this.backgroundAlpha;
    }

    public final void setBackgroundAlpha(float f) {
        this.backgroundAlpha = f;
    }

    public final int getStartHeight() {
        return this.startHeight;
    }

    public final void setStartHeight(int i) {
        this.startHeight = i;
    }

    public final void setStartPosition(@Nullable int[] iArr) {
        this.startPosition = iArr;
    }

    public final int getLeft() {
        return this.left;
    }

    public final void setLeft(int i) {
        this.left = i;
    }

    public final int getRight() {
        return this.right;
    }

    public final void setRight(int i) {
        this.right = i;
    }

    public final void setStartTranslationZ(float f) {
        this.startTranslationZ = f;
    }

    public final void setStartClipTopAmount(int i) {
        this.startClipTopAmount = i;
    }

    public final void setTop(int i) {
        this.top = i;
    }

    public final void setBottom(int i) {
        this.bottom = i;
    }
}
