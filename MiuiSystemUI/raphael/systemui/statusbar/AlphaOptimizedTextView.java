package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AlphaOptimizedTextView extends TextView {
    public boolean hasOverlappingRendering() {
        return false;
    }

    public AlphaOptimizedTextView(Context context) {
        this(context, (AttributeSet) null);
    }

    public AlphaOptimizedTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AlphaOptimizedTextView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public AlphaOptimizedTextView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }
}
