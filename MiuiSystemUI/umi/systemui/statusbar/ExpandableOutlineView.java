package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;

public abstract class ExpandableOutlineView extends ExpandableView {
    private boolean mCustomOutline;
    private int mNotificationBgRadius;
    /* access modifiers changed from: private */
    public float mOutlineAlpha = -1.0f;
    private final Rect mOutlineRect = new Rect();
    ViewOutlineProvider mProvider = new ViewOutlineProvider() {
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(ExpandableOutlineView.this.getOutlineRect(), (float) ExpandableOutlineView.this.getOutlineRadius());
            outline.setAlpha(ExpandableOutlineView.this.mOutlineAlpha);
        }
    };
    private final Rect mTempOutlineRect = new Rect();

    /* access modifiers changed from: protected */
    public boolean needsOutline() {
        return false;
    }

    public ExpandableOutlineView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mNotificationBgRadius = NotificationUtil.getOutlineRadius(context);
        setOutlineProvider(this.mProvider);
        setClipToOutline(true);
    }

    public void setActualHeight(int i, boolean z) {
        super.setActualHeight(i, z);
        invalidateOutline();
    }

    public void setClipTopAmount(int i) {
        super.setClipTopAmount(i);
        invalidateOutline();
    }

    public void setClipBottomAmount(int i) {
        super.setClipBottomAmount(i);
        invalidateOutline();
    }

    /* access modifiers changed from: protected */
    public void setOutlineAlpha(float f) {
        if (f != this.mOutlineAlpha) {
            this.mOutlineAlpha = f;
            invalidateOutline();
        }
    }

    public float getOutlineAlpha() {
        return this.mOutlineAlpha;
    }

    /* access modifiers changed from: protected */
    public void setOutlineRect(RectF rectF) {
        if (rectF != null) {
            setOutlineRect(rectF.left, rectF.top, rectF.right, rectF.bottom);
            return;
        }
        this.mCustomOutline = false;
        invalidateOutline();
    }

    public int getOutlineTranslation() {
        return this.mCustomOutline ? this.mOutlineRect.left : (int) getTranslation();
    }

    public void updateOutline() {
        setOutlineProvider(needsOutline() ? this.mProvider : null);
    }

    /* access modifiers changed from: protected */
    public void setOutlineRect(float f, float f2, float f3, float f4) {
        this.mCustomOutline = true;
        setClipToOutline(true);
        this.mOutlineRect.set((int) f, (int) f2, (int) f3, (int) f4);
        Rect rect = this.mOutlineRect;
        rect.bottom = (int) Math.max(f2, (float) rect.bottom);
        Rect rect2 = this.mOutlineRect;
        rect2.right = (int) Math.max(f, (float) rect2.right);
        invalidateOutline();
    }

    /* access modifiers changed from: private */
    public Rect getOutlineRect() {
        if (this.mCustomOutline) {
            return this.mOutlineRect;
        }
        int translation = (int) getTranslation();
        int max = Math.max(getActualHeight() - this.mClipBottomAmount, this.mClipTopAmount);
        this.mTempOutlineRect.set(translation, this.mClipTopAmount, getWidth() + translation, max);
        return this.mTempOutlineRect;
    }

    /* access modifiers changed from: protected */
    public int getOutlineRadius() {
        return this.mNotificationBgRadius;
    }
}
