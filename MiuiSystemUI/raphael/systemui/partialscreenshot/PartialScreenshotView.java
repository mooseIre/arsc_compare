package com.android.systemui.partialscreenshot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.android.systemui.partialscreenshot.shape.PartialScreenshotShape;

public class PartialScreenshotView extends View {
    private Paint mPaintBackground;
    private PartialScreenshotShape shape;

    public PartialScreenshotView(Context context) {
        this(context, (AttributeSet) null);
    }

    public PartialScreenshotView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPaintBackground = new Paint();
        this.mPaintBackground.setColor(-16777216);
        this.mPaintBackground.setAlpha(165);
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
            return;
        }
        canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), this.mPaintBackground);
    }
}
