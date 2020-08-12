package com.android.systemui.miui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

public class LimitedSizeStyleSavedView extends StyleSavedView {
    private int mMaxHeight = 0;
    private int mMaxWidth = 0;
    private int mMinHeight = 0;
    private int mMinWidth = 0;

    public LimitedSizeStyleSavedView(Context context) {
        super(context);
    }

    public LimitedSizeStyleSavedView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(attributeSet);
    }

    public LimitedSizeStyleSavedView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(attributeSet);
    }

    private void init(AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R$styleable.LimitedSizeStyleSavedView);
            this.mMinWidth = getMinimumWidth();
            this.mMinHeight = getMinimumHeight();
            this.mMaxWidth = obtainStyledAttributes.getDimensionPixelSize(R$styleable.LimitedSizeStyleSavedView_maximumWidth, Integer.MAX_VALUE);
            this.mMaxHeight = obtainStyledAttributes.getDimensionPixelSize(R$styleable.LimitedSizeStyleSavedView_maximumHeight, Integer.MAX_VALUE);
            obtainStyledAttributes.recycle();
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        int i3 = this.mMaxWidth;
        if (i3 > 0 && i3 < size) {
            i = View.MeasureSpec.makeMeasureSpec(this.mMaxWidth, View.MeasureSpec.getMode(i));
        }
        int size2 = View.MeasureSpec.getSize(i2);
        int i4 = this.mMaxHeight;
        if (i4 > 0 && i4 < size2) {
            i2 = View.MeasureSpec.makeMeasureSpec(this.mMaxHeight, View.MeasureSpec.getMode(i2));
        }
        super.onMeasure(i, i2);
    }

    public void setMaxWidth(int i) {
        this.mMaxWidth = i;
        int i2 = this.mMaxWidth;
        int i3 = this.mMinWidth;
        if (i2 < i3) {
            this.mMaxWidth = i3;
        }
    }
}
