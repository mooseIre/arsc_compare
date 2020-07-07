package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.view.View;
import com.miui.systemui.renderlayer.MiRenderInfo;
import com.miui.systemui.renderlayer.RenderLayerManager;

public class NotificationBackgroundView extends View implements MiRenderInfo {
    private int mActualHeight;
    private Drawable mBackground;
    private int mClipBottomAmount;
    private int mClipTopAmount;
    private boolean mIsShowHeadsUpBackground = false;
    private boolean mRenderRegistered = false;
    private int mTintColor;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public NotificationBackgroundView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (maybeUpdateDrawableBounds()) {
            this.mBackground.draw(canvas);
        }
    }

    private boolean maybeUpdateDrawableBounds() {
        int i = this.mClipTopAmount;
        int i2 = this.mActualHeight - this.mClipBottomAmount;
        Drawable drawable = this.mBackground;
        if (drawable == null || i2 <= i) {
            return false;
        }
        drawable.setBounds(0, i, getWidth(), i2);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable drawable) {
        return super.verifyDrawable(drawable) || drawable == this.mBackground;
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        drawableStateChanged(this.mBackground);
    }

    private void drawableStateChanged(Drawable drawable) {
        if (drawable != null && drawable.isStateful()) {
            drawable.setState(getDrawableState());
        }
    }

    public void drawableHotspotChanged(float f, float f2) {
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            drawable.setHotspot(f, f2);
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        int i = getResources().getConfiguration().uiMode & 48;
    }

    public void onConfigurationChanged(Configuration configuration) {
        int i = configuration.uiMode & 48;
    }

    public void setCustomBackground(Drawable drawable) {
        Drawable drawable2 = this.mBackground;
        if (drawable2 != drawable) {
            if (drawable2 != null) {
                drawable2.setCallback((Drawable.Callback) null);
                unscheduleDrawable(this.mBackground);
            }
            this.mBackground = drawable;
            if (drawable != null) {
                drawable.mutate();
                this.mBackground.setCallback(this);
                setTint(this.mTintColor);
                this.mBackground.getConstantState().newDrawable().mutate();
            }
            Drawable drawable3 = this.mBackground;
            if (drawable3 instanceof RippleDrawable) {
                ((RippleDrawable) drawable3).setForceSoftware(true);
            }
            invalidate();
        }
    }

    public void setCustomBackground(int i) {
        setCustomBackground(this.mContext.getDrawable(i));
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
        this.mActualHeight = i;
        invalidate();
    }

    public void setClipTopAmount(int i) {
        this.mClipTopAmount = i;
        invalidate();
    }

    public void setClipBottomAmount(int i) {
        this.mClipBottomAmount = i;
        invalidate();
    }

    public void setState(int[] iArr) {
        this.mBackground.setState(iArr);
    }

    public void setRippleColor(int i) {
        Drawable drawable = this.mBackground;
        if (drawable instanceof RippleDrawable) {
            ((RippleDrawable) drawable).setColor(ColorStateList.valueOf(i));
        }
    }

    public void setDrawableAlpha(int i) {
        this.mBackground.setAlpha(i);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        handleRenderInfo();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handleRenderInfo();
    }

    public void setShowHeadsUp(boolean z) {
        this.mIsShowHeadsUpBackground = z;
        handleRenderInfo();
    }

    private void handleRenderInfo() {
        boolean z = this.mIsShowHeadsUpBackground && isAttachedToWindow();
        if (z != this.mRenderRegistered) {
            this.mRenderRegistered = z;
            if (z) {
                RenderLayerManager.getInstance().register(this);
            } else {
                RenderLayerManager.getInstance().unregister(this);
            }
        }
    }
}
