package com.android.systemui.screenshot;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeTextView extends TextView {
    private boolean isMarqueeEnable = false;

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public MarqueeTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public MarqueeTextView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public void setMarqueeEnable(boolean z) {
        if (this.isMarqueeEnable != z) {
            this.isMarqueeEnable = z;
            if (z) {
                setEllipsize(TextUtils.TruncateAt.MARQUEE);
                setMarqueeRepeatLimit(-1);
            } else {
                setEllipsize(TextUtils.TruncateAt.END);
            }
            onWindowFocusChanged(z);
        }
    }

    public boolean isFocused() {
        return this.isMarqueeEnable;
    }

    /* access modifiers changed from: protected */
    public void onFocusChanged(boolean z, int i, Rect rect) {
        super.onFocusChanged(this.isMarqueeEnable, i, rect);
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(this.isMarqueeEnable);
    }
}
