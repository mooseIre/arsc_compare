package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AlphaImageView extends ImageView {
    public boolean hasOverlappingRendering() {
        return false;
    }

    public AlphaImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }
}
