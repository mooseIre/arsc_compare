package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import java.util.ArrayList;

public class ReverseLinearLayout extends LinearLayout {
    private boolean mIsAlternativeOrder;
    private boolean mIsLayoutReverse;

    public ReverseLinearLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        updateOrder();
    }

    public void addView(View view) {
        reversParams(view.getLayoutParams());
        if (this.mIsLayoutReverse) {
            super.addView(view, 0);
        } else {
            super.addView(view);
        }
    }

    public void addView(View view, ViewGroup.LayoutParams layoutParams) {
        reversParams(layoutParams);
        if (this.mIsLayoutReverse) {
            super.addView(view, 0, layoutParams);
        } else {
            super.addView(view, layoutParams);
        }
    }

    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        updateOrder();
    }

    private void updateOrder() {
        boolean z = (getLayoutDirection() == 1) ^ this.mIsAlternativeOrder;
        if (this.mIsLayoutReverse != z) {
            int childCount = getChildCount();
            ArrayList arrayList = new ArrayList(childCount);
            for (int i = 0; i < childCount; i++) {
                arrayList.add(getChildAt(i));
            }
            removeAllViews();
            for (int i2 = childCount - 1; i2 >= 0; i2--) {
                super.addView((View) arrayList.get(i2));
            }
            this.mIsLayoutReverse = z;
        }
    }

    private void reversParams(ViewGroup.LayoutParams layoutParams) {
        if (layoutParams != null) {
            int i = layoutParams.width;
            layoutParams.width = layoutParams.height;
            layoutParams.height = i;
        }
    }
}
