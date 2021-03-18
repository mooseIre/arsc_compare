package com.android.systemui.statusbar.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.R$styleable;

public class LimitedSizeFrameLayout extends FrameLayout {
    protected int mMaxWidth;

    public LimitedSizeFrameLayout(Context context) {
        this(context, null);
    }

    public LimitedSizeFrameLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public LimitedSizeFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mMaxWidth = -1;
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R$styleable.LimitedSizeFrameLayout, 0, 0);
        try {
            this.mMaxWidth = obtainStyledAttributes.getDimensionPixelSize(R$styleable.LimitedSizeFrameLayout_MiuiMaxWidth, -1);
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int i3;
        if (this.mMaxWidth != -1 && View.MeasureSpec.getMode(i) == Integer.MIN_VALUE && View.MeasureSpec.getSize(i) > (i3 = this.mMaxWidth)) {
            i = View.MeasureSpec.makeMeasureSpec(i3, Integer.MIN_VALUE);
        }
        super.onMeasure(i, i2);
    }
}
