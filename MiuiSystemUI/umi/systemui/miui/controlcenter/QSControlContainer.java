package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

public class QSControlContainer extends FrameLayout {
    public QSControlContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void requestLayout() {
        super.requestLayout();
        Log.d("QSControlContainer", "requestLayout:" + isLayoutRequested());
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        Log.d("QSControlContainer", "onlayout:" + isLayoutRequested());
    }
}
