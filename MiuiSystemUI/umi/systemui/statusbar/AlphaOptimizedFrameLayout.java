package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class AlphaOptimizedFrameLayout extends FrameLayout {
    public boolean hasOverlappingRendering() {
        return false;
    }

    public AlphaOptimizedFrameLayout(Context context) {
        super(context);
    }

    public AlphaOptimizedFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public AlphaOptimizedFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public AlphaOptimizedFrameLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }
}
