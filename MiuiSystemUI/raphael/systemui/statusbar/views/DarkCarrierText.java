package com.android.systemui.statusbar.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import com.android.keyguard.CarrierText;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;

public class DarkCarrierText extends CarrierText implements DarkIconDispatcher.DarkReceiver {
    public DarkCarrierText(Context context) {
        super(context);
    }

    public DarkCarrierText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.CarrierText
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.CarrierText
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i, int i2, int i3, boolean z) {
        if (!DarkIconDispatcher.isInArea(rect, this)) {
            setTextColor(i2);
        } else if (z) {
            setTextColor(i);
        } else {
            if (f > 0.0f) {
                i2 = i3;
            }
            setTextColor(i2);
        }
    }
}
