package com.android.systemui.miui.statusbar;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;

public class DarkReceiverLinearLayout extends LinearLayout implements DarkIconDispatcher.DarkReceiver {
    public DarkReceiverLinearLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public DarkReceiverLinearLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void onDarkChanged(Rect rect, float f, int i) {
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            View childAt = getChildAt(i2);
            if (childAt instanceof DarkIconDispatcher.DarkReceiver) {
                ((DarkIconDispatcher.DarkReceiver) childAt).onDarkChanged(rect, f, i);
            }
        }
    }
}
