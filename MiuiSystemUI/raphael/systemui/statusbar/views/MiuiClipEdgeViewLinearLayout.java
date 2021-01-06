package com.android.systemui.statusbar.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MiuiClipEdgeViewLinearLayout extends LinearLayout {
    private Rect mClipRect = new Rect();

    public MiuiClipEdgeViewLinearLayout(Context context) {
        super(context);
    }

    public MiuiClipEdgeViewLinearLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public MiuiClipEdgeViewLinearLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    /* access modifiers changed from: protected */
    public boolean setFrame(int i, int i2, int i3, int i4) {
        int i5;
        if (getParent() instanceof View) {
            View view = (View) getParent();
            i5 = Math.max(i, Math.max(view.getPaddingLeft(), 0));
            i3 = Math.max(0, Math.min(i3, view.getWidth() - view.getPaddingRight()));
        } else {
            i5 = Math.max(i, 0);
        }
        return super.setFrame(i5, i2, i3, i4);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (getParent() instanceof View) {
            View view = (View) getParent();
            i3 = Math.max(0, Math.min(i3, view.getWidth() - view.getPaddingRight()));
            i = Math.max(i, Math.max(view.getPaddingLeft(), 0));
        }
        super.onLayout(z, i, i2, i3, i4);
        updateClipRect();
    }

    private void updateClipRect() {
        Rect rect = this.mClipRect;
        int i = 0;
        rect.left = 0;
        rect.top = 0;
        rect.bottom = getHeight();
        this.mClipRect.right = 0;
        if (clipEnd()) {
            i = getWidth();
        }
        setClipRectRight(i, this);
    }

    private void setClipRectRight(int i, ViewGroup viewGroup) {
        for (int i2 = 0; i2 < viewGroup.getChildCount(); i2++) {
            View childAt = viewGroup.getChildAt(i2);
            float f = (float) i;
            if (((float) childAt.getLeft()) + childAt.getTranslationX() < f && ((float) childAt.getRight()) + childAt.getTranslationX() > f && childAt.getVisibility() != 8) {
                if (childAt instanceof ViewGroup) {
                    setClipRectRight(i - ((int) (((float) childAt.getLeft()) + childAt.getTranslationX())), (ViewGroup) childAt);
                } else if (clipEnd()) {
                    this.mClipRect.right = getWidth() - (i - ((int) (((float) childAt.getLeft()) + childAt.getTranslationX())));
                    return;
                } else {
                    this.mClipRect.left = ((int) (((float) childAt.getRight()) + childAt.getTranslationX())) - i;
                    this.mClipRect.right = getWidth();
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean clipEnd() {
        return isLayoutRtl();
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        Rect rect = this.mClipRect;
        boolean z = rect.right - rect.left > 0;
        if (z) {
            canvas.save();
            canvas.clipRect(this.mClipRect);
        }
        super.dispatchDraw(canvas);
        if (z) {
            canvas.restore();
        }
    }
}
