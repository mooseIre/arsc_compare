package com.android.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class AlphaOptimizedRelativeLayout extends RelativeLayout {
    public boolean hasOverlappingRendering() {
        return false;
    }

    public AlphaOptimizedRelativeLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }
}
