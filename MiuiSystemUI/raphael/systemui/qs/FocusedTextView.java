package com.android.systemui.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class FocusedTextView extends TextView {
    public boolean canScrollHorizontally(int i) {
        return false;
    }

    public boolean isFocused() {
        return true;
    }

    public FocusedTextView(Context context) {
        super(context);
    }

    public FocusedTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public FocusedTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }
}
