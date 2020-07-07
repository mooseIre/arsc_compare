package com.android.systemui.partialscreenshot;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import com.android.systemui.partialscreenshot.shape.PartialScreenshotShape;

public class PartialScreenshotView extends View {
    private PartialScreenshotShape shape;

    public PartialScreenshotView(Context context) {
        this(context, (AttributeSet) null);
    }

    public PartialScreenshotView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setProduct(PartialScreenshotShape partialScreenshotShape) {
        this.shape = partialScreenshotShape;
        invalidate();
    }

    public void clear() {
        this.shape = null;
        invalidate();
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        PartialScreenshotShape partialScreenshotShape = this.shape;
        if (partialScreenshotShape != null) {
            partialScreenshotShape.draw(canvas);
        }
    }
}
