package com.android.systemui.util.animation;

import android.graphics.PointF;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: TransitionLayoutController.kt */
public final class DisappearParameters {
    @NotNull
    private PointF contentTranslationFraction = new PointF(0.0f, 0.8f);
    private float disappearEnd = 1.0f;
    @NotNull
    private PointF disappearSize = new PointF(1.0f, 0.0f);
    private float disappearStart;
    private float fadeStartPosition = 0.9f;
    @NotNull
    private PointF gonePivot = new PointF(0.0f, 1.0f);

    @NotNull
    public final PointF getGonePivot() {
        return this.gonePivot;
    }

    @NotNull
    public final PointF getDisappearSize() {
        return this.disappearSize;
    }

    @NotNull
    public final PointF getContentTranslationFraction() {
        return this.contentTranslationFraction;
    }

    public final float getDisappearStart() {
        return this.disappearStart;
    }

    public final void setDisappearStart(float f) {
        this.disappearStart = f;
    }

    public final float getDisappearEnd() {
        return this.disappearEnd;
    }

    public final void setDisappearEnd(float f) {
        this.disappearEnd = f;
    }

    public final float getFadeStartPosition() {
        return this.fadeStartPosition;
    }

    public final void setFadeStartPosition(float f) {
        this.fadeStartPosition = f;
    }

    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DisappearParameters)) {
            return false;
        }
        DisappearParameters disappearParameters = (DisappearParameters) obj;
        if (this.disappearSize.equals(disappearParameters.disappearSize) && this.gonePivot.equals(disappearParameters.gonePivot) && this.contentTranslationFraction.equals(disappearParameters.contentTranslationFraction) && this.disappearStart == disappearParameters.disappearStart && this.disappearEnd == disappearParameters.disappearEnd && this.fadeStartPosition == disappearParameters.fadeStartPosition) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((((((((this.disappearSize.hashCode() * 31) + this.gonePivot.hashCode()) * 31) + this.contentTranslationFraction.hashCode()) * 31) + Float.hashCode(this.disappearStart)) * 31) + Float.hashCode(this.disappearEnd)) * 31) + Float.hashCode(this.fadeStartPosition);
    }

    @NotNull
    public final DisappearParameters deepCopy() {
        DisappearParameters disappearParameters = new DisappearParameters();
        disappearParameters.disappearSize.set(this.disappearSize);
        disappearParameters.gonePivot.set(this.gonePivot);
        disappearParameters.contentTranslationFraction.set(this.contentTranslationFraction);
        disappearParameters.disappearStart = this.disappearStart;
        disappearParameters.disappearEnd = this.disappearEnd;
        disappearParameters.fadeStartPosition = this.fadeStartPosition;
        return disappearParameters;
    }
}
