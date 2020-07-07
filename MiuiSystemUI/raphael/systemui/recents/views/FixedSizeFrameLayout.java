package com.android.systemui.recents.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class FixedSizeFrameLayout extends FrameLayout {
    private final Rect mLayoutBounds = new Rect();

    public FixedSizeFrameLayout(Context context) {
        super(context);
    }

    public FixedSizeFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public FixedSizeFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public FixedSizeFrameLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    /* access modifiers changed from: protected */
    public final void onMeasure(int i, int i2) {
        measureContents(View.MeasureSpec.getSize(i), View.MeasureSpec.getSize(i2));
    }

    /* access modifiers changed from: protected */
    public final void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.mLayoutBounds.set(i, i2, i3, i4);
        layoutContents(this.mLayoutBounds, z);
    }

    public final void requestLayout() {
        Rect rect = this.mLayoutBounds;
        if (rect == null || rect.isEmpty()) {
            super.requestLayout();
            return;
        }
        measureContents(getMeasuredWidth(), getMeasuredHeight());
        layoutContents(this.mLayoutBounds, false);
    }

    /* access modifiers changed from: protected */
    public void measureContents(int i, int i2) {
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(i, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(i2, Integer.MIN_VALUE));
    }

    /* access modifiers changed from: protected */
    public void layoutContents(Rect rect, boolean z) {
        super.onLayout(z, rect.left, rect.top, rect.right, rect.bottom);
        onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
    }
}
