package com.android.systemui.statusbar.notification.mediacontrol;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;

public class SeekBarGradientDrawable extends GradientDrawable {
    private int mHeight = 0;
    private DynamicAnimation.OnAnimationUpdateListener mInvalidateUpdateListener = new DynamicAnimation.OnAnimationUpdateListener() {
        /* class com.android.systemui.statusbar.notification.mediacontrol.SeekBarGradientDrawable.AnonymousClass2 */

        @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationUpdateListener
        public void onAnimationUpdate(DynamicAnimation dynamicAnimation, float f, float f2) {
            SeekBarGradientDrawable.this.invalidateSelf();
        }
    };
    private SpringAnimation mPressedScaleAnim;
    private float mScale = 1.0f;
    private FloatPropertyCompat<SeekBarGradientDrawable> mScaleFloatProperty = new FloatPropertyCompat<SeekBarGradientDrawable>("Scale") {
        /* class com.android.systemui.statusbar.notification.mediacontrol.SeekBarGradientDrawable.AnonymousClass1 */

        public float getValue(SeekBarGradientDrawable seekBarGradientDrawable) {
            return SeekBarGradientDrawable.this.mScale;
        }

        public void setValue(SeekBarGradientDrawable seekBarGradientDrawable, float f) {
            SeekBarGradientDrawable.this.mScale = f;
        }
    };
    private SeekBarGradientState mSeekBarGradientState;
    private SpringAnimation mUnPressedScaleAnim;
    private int mWidth = 0;

    public boolean isStateful() {
        return true;
    }

    public SeekBarGradientDrawable() {
        SeekBarGradientState newSeekBarGradientState = newSeekBarGradientState();
        this.mSeekBarGradientState = newSeekBarGradientState;
        newSeekBarGradientState.setConstantState(super.getConstantState());
        initAnim();
    }

    public SeekBarGradientDrawable(Resources resources, Resources.Theme theme, SeekBarGradientState seekBarGradientState) {
        Drawable drawable;
        if (resources == null) {
            drawable = seekBarGradientState.mParent.newDrawable();
        } else if (theme == null) {
            drawable = seekBarGradientState.mParent.newDrawable(resources);
        } else {
            drawable = seekBarGradientState.mParent.newDrawable(resources, theme);
        }
        seekBarGradientState.mParent = drawable.getConstantState();
        SeekBarGradientState newSeekBarGradientState = newSeekBarGradientState();
        this.mSeekBarGradientState = newSeekBarGradientState;
        newSeekBarGradientState.setConstantState(seekBarGradientState.mParent);
        this.mWidth = drawable.getIntrinsicWidth();
        this.mHeight = drawable.getIntrinsicHeight();
        if (drawable instanceof GradientDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) drawable;
            setCornerRadius(gradientDrawable.getCornerRadius());
            setShape(gradientDrawable.getShape());
            setColor(gradientDrawable.getColor());
        }
        initAnim();
    }

    private void initAnim() {
        SpringAnimation springAnimation = new SpringAnimation(this, this.mScaleFloatProperty, 3.0f);
        this.mPressedScaleAnim = springAnimation;
        springAnimation.getSpring().setStiffness(986.96f);
        this.mPressedScaleAnim.getSpring().setDampingRatio(0.7f);
        this.mPressedScaleAnim.setMinimumVisibleChange(0.002f);
        this.mPressedScaleAnim.addUpdateListener(this.mInvalidateUpdateListener);
        SpringAnimation springAnimation2 = new SpringAnimation(this, this.mScaleFloatProperty, 1.0f);
        this.mUnPressedScaleAnim = springAnimation2;
        springAnimation2.getSpring().setStiffness(986.96f);
        this.mUnPressedScaleAnim.getSpring().setDampingRatio(0.8f);
        this.mUnPressedScaleAnim.setMinimumVisibleChange(0.002f);
        this.mUnPressedScaleAnim.addUpdateListener(this.mInvalidateUpdateListener);
    }

    private SeekBarGradientState newSeekBarGradientState() {
        return new SeekBarGradientState();
    }

    public Drawable.ConstantState getConstantState() {
        return this.mSeekBarGradientState;
    }

    public int getIntrinsicWidth() {
        int i = this.mWidth;
        return i > 0 ? i : super.getIntrinsicWidth();
    }

    public int getIntrinsicHeight() {
        int i = this.mHeight;
        return i > 0 ? i : super.getIntrinsicHeight();
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(int[] iArr) {
        boolean onStateChange = super.onStateChange(iArr);
        boolean z = false;
        for (int i : iArr) {
            if (i == 16842919) {
                z = true;
            }
        }
        if (z) {
            startPressedAnim();
        }
        if (!z) {
            startUnPressedAnim();
        }
        return onStateChange;
    }

    private void startPressedAnim() {
        if (this.mUnPressedScaleAnim.isRunning()) {
            this.mUnPressedScaleAnim.cancel();
        }
        if (!this.mPressedScaleAnim.isRunning()) {
            this.mPressedScaleAnim.start();
        }
    }

    private void startUnPressedAnim() {
        if (this.mPressedScaleAnim.isRunning()) {
            this.mPressedScaleAnim.cancel();
        }
        if (!this.mUnPressedScaleAnim.isRunning()) {
            this.mUnPressedScaleAnim.start();
        }
    }

    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        canvas.save();
        float f = this.mScale;
        canvas.scale(f, f, (float) ((bounds.right + bounds.left) / 2), (float) ((bounds.top + bounds.bottom) / 2));
        super.draw(canvas);
        canvas.restore();
    }

    /* access modifiers changed from: private */
    public static class SeekBarGradientState extends Drawable.ConstantState {
        Drawable.ConstantState mParent;

        private SeekBarGradientState() {
        }

        public void setConstantState(Drawable.ConstantState constantState) {
            this.mParent = constantState;
        }

        public Drawable newDrawable() {
            if (this.mParent == null) {
                return null;
            }
            return newSeekBarGradientDrawable(null, null, this);
        }

        public Drawable newDrawable(Resources resources) {
            if (this.mParent == null) {
                return null;
            }
            return newSeekBarGradientDrawable(resources, null, this);
        }

        public Drawable newDrawable(Resources resources, Resources.Theme theme) {
            if (this.mParent == null) {
                return null;
            }
            return newSeekBarGradientDrawable(resources, theme, this);
        }

        private Drawable newSeekBarGradientDrawable(Resources resources, Resources.Theme theme, SeekBarGradientState seekBarGradientState) {
            return new SeekBarGradientDrawable(resources, theme, seekBarGradientState);
        }

        public int getChangingConfigurations() {
            return this.mParent.getChangingConfigurations();
        }

        public boolean canApplyTheme() {
            return this.mParent.canApplyTheme();
        }
    }
}
