package com.android.keyguard;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;
import com.miui.systemui.drawable.NumPadRippleDrawable;
import miuix.animation.Folme;

public class AlphaOptimizedImageButton extends ImageButton {
    public boolean hasOverlappingRendering() {
        return false;
    }

    public AlphaOptimizedImageButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Drawable background = getBackground();
        if (background instanceof NumPadRippleDrawable) {
            Folme.clean(((NumPadRippleDrawable) background).getNumPadAnimTarget());
        }
    }
}
