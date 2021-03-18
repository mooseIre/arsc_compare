package com.android.systemui.fsgesture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class WidthAdaptiveView extends View {
    public WidthAdaptiveView(Context context) {
        this(context, null);
    }

    public WidthAdaptiveView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WidthAdaptiveView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public WidthAdaptiveView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public void draw(Canvas canvas) {
        Drawable background = getBackground();
        if (background != null) {
            canvas.save();
            background.setBounds(0, 0, getWidth(), (int) (((float) getWidth()) * ((((float) background.getIntrinsicHeight()) * 1.0f) / ((float) background.getIntrinsicWidth()))));
            background.draw(canvas);
            canvas.restore();
        }
    }
}
