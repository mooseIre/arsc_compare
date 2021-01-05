package com.android.systemui.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class AutoMarqueeTextView extends TextView {
    private boolean mAggregatedVisible = false;

    public AutoMarqueeTextView(Context context) {
        super(context);
    }

    public AutoMarqueeTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public AutoMarqueeTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public AutoMarqueeTextView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        onVisibilityAggregated(isVisibleToUser());
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setSelected(true);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setSelected(false);
    }

    public void onVisibilityAggregated(boolean z) {
        super.onVisibilityAggregated(z);
        if (z != this.mAggregatedVisible) {
            this.mAggregatedVisible = z;
            if (z) {
                setEllipsize(TextUtils.TruncateAt.MARQUEE);
            } else {
                setEllipsize(TextUtils.TruncateAt.END);
            }
        }
    }
}
