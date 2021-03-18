package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.util.ArrayUtils;
import com.android.systemui.C0010R$bool;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;

public class NotificationBackgroundView extends BaseMiuiNotificationBackgroundView {
    private int mActualHeight;
    private float mActualWidth;
    private Drawable mBackground;
    private int mBackgroundTop;
    private boolean mBottomAmountClips = true;
    private boolean mBottomIsRounded;
    private int mClipBottomAmount;
    private int mClipTopAmount;
    private float[] mCornerRadii = new float[8];
    private float mDistanceToTopRoundness;
    private final boolean mDontModifyCorners = getResources().getBoolean(C0010R$bool.config_clipNotificationsToOutline);
    private int mDrawableAlpha = 255;
    private boolean mExpandAnimationRunning;
    private boolean mFirstInSection;
    private boolean mIsPressedAllowed;
    private boolean mLastInSection;
    private int mTintColor;
    private boolean mTopAmountRounded;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public NotificationBackgroundView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mClipTopAmount + this.mClipBottomAmount < this.mActualHeight - this.mBackgroundTop || this.mExpandAnimationRunning) {
            canvas.save();
            if (!this.mExpandAnimationRunning) {
                canvas.clipRect(0, this.mClipTopAmount, getWidth(), this.mActualHeight - this.mClipBottomAmount);
            }
            draw(canvas, this.mBackground);
            canvas.restore();
        }
    }

    private void draw(Canvas canvas, Drawable drawable) {
        if (drawable != null) {
            int i = this.mBackgroundTop;
            int i2 = this.mActualHeight;
            if (this.mBottomIsRounded && this.mBottomAmountClips && !this.mExpandAnimationRunning && !this.mLastInSection) {
                i2 -= this.mClipBottomAmount;
            }
            int i3 = 0;
            int width = getWidth();
            if (this.mExpandAnimationRunning) {
                float f = this.mActualWidth;
                i3 = (int) ((((float) getWidth()) - f) / 2.0f);
                width = (int) (((float) i3) + f);
            }
            if (this.mTopAmountRounded) {
                int i4 = (int) (((float) this.mClipTopAmount) - this.mDistanceToTopRoundness);
                if (i4 >= 0 || !this.mFirstInSection) {
                    i += i4;
                }
                if (i4 >= 0 && !this.mLastInSection) {
                    i2 += i4;
                }
            }
            drawable.setAlpha(isBlurEnabledAndSupported() ? 10 : 255);
            drawable.setBounds(i3, i, width, i2);
            drawable.draw(canvas);
        }
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable drawable) {
        return super.verifyDrawable(drawable) || drawable == this.mBackground;
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        setState(getDrawableState());
    }

    public void drawableHotspotChanged(float f, float f2) {
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            drawable.setHotspot(f, f2);
        }
    }

    public void setCustomBackground(Drawable drawable) {
        Drawable drawable2 = this.mBackground;
        if (drawable2 != null) {
            drawable2.setCallback(null);
            unscheduleDrawable(this.mBackground);
        }
        this.mBackground = drawable;
        drawable.mutate();
        Drawable drawable3 = this.mBackground;
        if (drawable3 != null) {
            drawable3.setCallback(this);
            setTint(this.mTintColor);
        }
        Drawable drawable4 = this.mBackground;
        if (drawable4 instanceof RippleDrawable) {
            ((RippleDrawable) drawable4).setForceSoftware(true);
        }
        updateBackgroundRadii();
        invalidate();
    }

    public void setCustomBackground(int i) {
        setCustomBackground(((View) this).mContext.getDrawable(i));
    }

    public void setTint(int i) {
        if (i != 0) {
            this.mBackground.setColorFilter(i, PorterDuff.Mode.SRC_ATOP);
        } else {
            this.mBackground.clearColorFilter();
        }
        this.mTintColor = i;
        invalidate();
    }

    public void setActualHeight(int i) {
        if (!this.mExpandAnimationRunning) {
            this.mActualHeight = i;
            invalidate();
        }
    }

    public int getActualHeight() {
        return this.mActualHeight;
    }

    public void setClipTopAmount(int i) {
        this.mClipTopAmount = i;
        invalidate();
    }

    public void setClipBottomAmount(int i) {
        this.mClipBottomAmount = i;
        invalidate();
    }

    public void setDistanceToTopRoundness(float f) {
        if (f != this.mDistanceToTopRoundness) {
            this.mTopAmountRounded = f >= 0.0f;
            this.mDistanceToTopRoundness = f;
            invalidate();
        }
    }

    public void setState(int[] iArr) {
        Drawable drawable = this.mBackground;
        if (drawable != null && drawable.isStateful()) {
            if (!this.mIsPressedAllowed) {
                iArr = ArrayUtils.removeInt(iArr, 16842919);
            }
            this.mBackground.setState(iArr);
        }
    }

    public void setRippleColor(int i) {
        Drawable drawable = this.mBackground;
        if (drawable instanceof RippleDrawable) {
            ((RippleDrawable) drawable).setColor(ColorStateList.valueOf(i));
        }
    }

    public void setDrawableAlpha(int i) {
        this.mDrawableAlpha = i;
        if (!this.mExpandAnimationRunning) {
            this.mBackground.setAlpha(i);
        }
    }

    public void setRoundness(float f, float f2) {
        float[] fArr = this.mCornerRadii;
        if (f != fArr[0] || f2 != fArr[4]) {
            this.mBottomIsRounded = f2 != 0.0f;
            float[] fArr2 = this.mCornerRadii;
            fArr2[0] = f;
            fArr2[1] = f;
            fArr2[2] = f;
            fArr2[3] = f;
            fArr2[4] = f2;
            fArr2[5] = f2;
            fArr2[6] = f2;
            fArr2[7] = f2;
            updateBackgroundRadii();
        }
    }

    public void setBottomAmountClips(boolean z) {
        if (z != this.mBottomAmountClips) {
            this.mBottomAmountClips = z;
            invalidate();
        }
    }

    public void setLastInSection(boolean z) {
        this.mLastInSection = z;
        invalidate();
    }

    public void setFirstInSection(boolean z) {
        this.mFirstInSection = z;
        invalidate();
    }

    private void updateBackgroundRadii() {
        if (!this.mDontModifyCorners) {
            Drawable drawable = this.mBackground;
            if (drawable instanceof LayerDrawable) {
                ((GradientDrawable) ((LayerDrawable) drawable).getDrawable(0)).setCornerRadii(this.mCornerRadii);
            }
        }
    }

    public void setBackgroundTop(int i) {
        this.mBackgroundTop = i;
        invalidate();
    }

    public void setExpandAnimationParams(ActivityLaunchAnimator.ExpandAnimationParameters expandAnimationParameters) {
        this.mActualHeight = expandAnimationParameters.getHeight();
        this.mActualWidth = (float) expandAnimationParameters.getWidth();
        this.mBackground.setAlpha((int) (((float) this.mDrawableAlpha) * (1.0f - Interpolators.ALPHA_IN.getInterpolation(expandAnimationParameters.getProgress(67, 200)))));
        invalidate();
    }

    public void setExpandAnimationRunning(boolean z) {
        this.mExpandAnimationRunning = z;
        Drawable drawable = this.mBackground;
        if (drawable instanceof LayerDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) ((LayerDrawable) drawable).getDrawable(0);
            gradientDrawable.setXfermode(z ? new PorterDuffXfermode(PorterDuff.Mode.SRC) : null);
            gradientDrawable.setAntiAlias(!z);
        }
        if (!this.mExpandAnimationRunning) {
            setDrawableAlpha(this.mDrawableAlpha);
        }
        invalidate();
    }

    public void setPressedAllowed(boolean z) {
        this.mIsPressedAllowed = z;
    }

    @Override // com.miui.blur.sdk.backdrop.ViewBlurDrawInfo, com.miui.blur.sdk.backdrop.BlurDrawInfo
    public void getBlurOutline(Outline outline) {
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            drawable.getOutline(outline);
        }
        if (outline.mMode == 0) {
            outline.setRect(0, 0, getWidth(), Math.max(getHeight(), this.mActualHeight));
        }
    }
}
