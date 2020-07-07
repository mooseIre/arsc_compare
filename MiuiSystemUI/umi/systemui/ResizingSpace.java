package com.android.systemui;

import android.R;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ResizingSpace extends View {
    private final int mHeight;
    private final int mWidth;

    public void draw(Canvas canvas) {
    }

    public ResizingSpace(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getVisibility() == 0) {
            setVisibility(4);
        }
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ViewGroup_Layout);
        this.mWidth = obtainStyledAttributes.getResourceId(0, 0);
        this.mHeight = obtainStyledAttributes.getResourceId(1, 0);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        boolean z;
        int dimensionPixelOffset;
        int dimensionPixelOffset2;
        super.onConfigurationChanged(configuration);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        boolean z2 = true;
        if (this.mWidth <= 0 || (dimensionPixelOffset2 = getContext().getResources().getDimensionPixelOffset(this.mWidth)) == layoutParams.width) {
            z = false;
        } else {
            layoutParams.width = dimensionPixelOffset2;
            z = true;
        }
        if (this.mHeight <= 0 || (dimensionPixelOffset = getContext().getResources().getDimensionPixelOffset(this.mHeight)) == layoutParams.height) {
            z2 = z;
        } else {
            layoutParams.height = dimensionPixelOffset;
        }
        if (z2) {
            setLayoutParams(layoutParams);
        }
    }

    private static int getDefaultSize2(int i, int i2) {
        int mode = View.MeasureSpec.getMode(i2);
        int size = View.MeasureSpec.getSize(i2);
        if (mode == Integer.MIN_VALUE) {
            return Math.min(i, size);
        }
        if (mode != 1073741824) {
            return i;
        }
        return size;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        setMeasuredDimension(getDefaultSize2(getSuggestedMinimumWidth(), i), getDefaultSize2(getSuggestedMinimumHeight(), i2));
    }
}
