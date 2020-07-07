package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class AlphaOptimizedButton extends Button {
    public boolean hasOverlappingRendering() {
        return false;
    }

    public AlphaOptimizedButton(Context context) {
        super(context);
    }

    public AlphaOptimizedButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public AlphaOptimizedButton(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public AlphaOptimizedButton(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }
}
