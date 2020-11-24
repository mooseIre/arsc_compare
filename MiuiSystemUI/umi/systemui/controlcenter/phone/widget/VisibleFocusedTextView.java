package com.android.systemui.controlcenter.phone.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class VisibleFocusedTextView extends TextView {
    public boolean canScrollHorizontally(int i) {
        return false;
    }

    public VisibleFocusedTextView(Context context) {
        super(context);
    }

    public VisibleFocusedTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public VisibleFocusedTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public boolean isFocused() {
        return getVisibility() == 0 && getAlpha() == 1.0f;
    }
}
