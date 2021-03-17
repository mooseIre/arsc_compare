package com.android.systemui.fsgesture;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0012R$dimen;

public class ScrollerLayout extends ViewGroup {
    private int mHorizontalGap;

    public ScrollerLayout(Context context) {
        this(context, null);
    }

    public ScrollerLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ScrollerLayout(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public ScrollerLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mHorizontalGap = getResources().getDimensionPixelSize(C0012R$dimen.quick_switch_demo_app_gap);
    }

    public int getHorizontalGap() {
        return this.mHorizontalGap;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            measureChild(getChildAt(i3), i, i2);
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (z) {
            int childCount = getChildCount();
            for (int i5 = 0; i5 < childCount; i5++) {
                View childAt = getChildAt(i5);
                int measuredWidth = (i5 - 1) * (childAt.getMeasuredWidth() + this.mHorizontalGap);
                childAt.layout(measuredWidth, 0, childAt.getMeasuredWidth() + measuredWidth, childAt.getMeasuredHeight());
            }
        }
    }
}
