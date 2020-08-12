package com.android.systemui.partialscreenshot;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BlurImageView extends ImageView {
    public boolean hasOverlappingRendering() {
        return false;
    }

    public BlurImageView(Context context) {
        super(context);
    }

    public BlurImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public BlurImageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public BlurImageView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }
}
