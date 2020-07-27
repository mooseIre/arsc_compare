package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.android.systemui.plugins.statusbar.phone.NavBarButtonProvider;

public class OpaLayout extends FrameLayout implements NavBarButtonProvider.ButtonInterface {
    public void abortCurrentGesture() {
    }

    public void setCarMode(boolean z) {
    }

    public void setDarkIntensity(float f) {
    }

    public void setImageDrawable(Drawable drawable) {
    }

    public void setVertical(boolean z) {
    }

    public OpaLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }
}
