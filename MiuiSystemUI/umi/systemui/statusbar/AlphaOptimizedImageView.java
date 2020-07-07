package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AlphaOptimizedImageView extends ImageView {
    public boolean hasOverlappingRendering() {
        return false;
    }

    public AlphaOptimizedImageView(Context context) {
        this(context, (AttributeSet) null);
    }

    public AlphaOptimizedImageView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AlphaOptimizedImageView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public AlphaOptimizedImageView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }
}
