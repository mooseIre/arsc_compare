package com.android.systemui.statusbar.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.keyguard.AlphaOptimizedLinearLayout;

public class MiuiStatusBarLimitWidthLinearLayout extends AlphaOptimizedLinearLayout {
    public MiuiStatusBarLimitWidthLinearLayout(Context context) {
        super(context);
    }

    public MiuiStatusBarLimitWidthLinearLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public MiuiStatusBarLimitWidthLinearLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public MiuiStatusBarLimitWidthLinearLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
    }

    /* access modifiers changed from: protected */
    public boolean setFrame(int i, int i2, int i3, int i4) {
        if (i < 0) {
            i = 0;
        }
        return super.setFrame(i, i2, i3, i4);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (getParent() instanceof View) {
            View view = (View) getParent();
            if (view.getWidth() < i3) {
                i3 = view.getWidth();
            }
            if (i <= 0) {
                i = 0;
            }
        }
        super.onLayout(z, i, i2, i3, i4);
    }
}
