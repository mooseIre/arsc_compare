package com.android.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class AlphaOptimizedImageButton extends ImageButton {
    public boolean hasOverlappingRendering() {
        return false;
    }

    public AlphaOptimizedImageButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }
}
