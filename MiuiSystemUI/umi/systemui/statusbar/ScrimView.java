package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.graphics.ColorUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.internal.colorextraction.drawable.ScrimDrawable;

public class ScrimView extends View {
    private Runnable mChangeRunnable;
    private PorterDuffColorFilter mColorFilter;
    private final ColorExtractor.GradientColors mColors;
    private Drawable mDrawable;
    private int mTintColor;
    private float mViewAlpha;

    /* access modifiers changed from: protected */
    public boolean canReceivePointerEvents() {
        return false;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public ScrimView(Context context) {
        this(context, null);
    }

    public ScrimView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ScrimView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public ScrimView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mViewAlpha = 1.0f;
        ScrimDrawable scrimDrawable = new ScrimDrawable();
        this.mDrawable = scrimDrawable;
        scrimDrawable.setCallback(this);
        this.mColors = new ColorExtractor.GradientColors();
        updateColorWithTint(false);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (this.mDrawable.getAlpha() > 0) {
            this.mDrawable.draw(canvas);
        }
    }

    public void invalidateDrawable(Drawable drawable) {
        super.invalidateDrawable(drawable);
        if (drawable == this.mDrawable) {
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (z) {
            this.mDrawable.setBounds(i, i2, i3, i4);
            invalidate();
        }
    }

    public void setColors(ColorExtractor.GradientColors gradientColors, boolean z) {
        if (gradientColors == null) {
            throw new IllegalArgumentException("Colors cannot be null");
        } else if (!this.mColors.equals(gradientColors)) {
            this.mColors.set(gradientColors);
            updateColorWithTint(z);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Drawable getDrawable() {
        return this.mDrawable;
    }

    public ColorExtractor.GradientColors getColors() {
        return this.mColors;
    }

    public void setTint(int i) {
        setTint(i, false);
    }

    public void setTint(int i, boolean z) {
        if (this.mTintColor != i) {
            this.mTintColor = i;
            updateColorWithTint(z);
        }
    }

    private void updateColorWithTint(boolean z) {
        PorterDuff.Mode mode;
        ScrimDrawable scrimDrawable = this.mDrawable;
        if (scrimDrawable instanceof ScrimDrawable) {
            scrimDrawable.setColor(ColorUtils.blendARGB(this.mColors.getMainColor(), this.mTintColor, ((float) Color.alpha(this.mTintColor)) / 255.0f), z);
        } else {
            if (Color.alpha(this.mTintColor) != 0) {
                PorterDuffColorFilter porterDuffColorFilter = this.mColorFilter;
                if (porterDuffColorFilter == null) {
                    mode = PorterDuff.Mode.SRC_OVER;
                } else {
                    mode = porterDuffColorFilter.getMode();
                }
                PorterDuffColorFilter porterDuffColorFilter2 = this.mColorFilter;
                if (porterDuffColorFilter2 == null || porterDuffColorFilter2.getColor() != this.mTintColor) {
                    this.mColorFilter = new PorterDuffColorFilter(this.mTintColor, mode);
                }
            } else {
                this.mColorFilter = null;
            }
            this.mDrawable.setColorFilter(this.mColorFilter);
            this.mDrawable.invalidateSelf();
        }
        Runnable runnable = this.mChangeRunnable;
        if (runnable != null) {
            runnable.run();
        }
    }

    public int getTint() {
        return this.mTintColor;
    }

    public void setViewAlpha(float f) {
        if (Float.isNaN(f)) {
            throw new IllegalArgumentException("alpha cannot be NaN: " + f);
        } else if (f != this.mViewAlpha) {
            this.mViewAlpha = f;
            this.mDrawable.setAlpha((int) (f * 255.0f));
            Runnable runnable = this.mChangeRunnable;
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    public float getViewAlpha() {
        return this.mViewAlpha;
    }

    public void setChangeRunnable(Runnable runnable) {
        this.mChangeRunnable = runnable;
    }
}
