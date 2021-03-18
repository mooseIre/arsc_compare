package com.android.systemui.screenshot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ScreenshotSelectorView extends View {
    private final Paint mPaintBackground;
    private final Paint mPaintSelection;
    private Rect mSelectionRect;
    private Point mStartPoint;

    public ScreenshotSelectorView(Context context) {
        this(context, null);
    }

    public ScreenshotSelectorView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Paint paint = new Paint(-16777216);
        this.mPaintBackground = paint;
        paint.setAlpha(160);
        Paint paint2 = new Paint(0);
        this.mPaintSelection = paint2;
        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void startSelection(int i, int i2) {
        this.mStartPoint = new Point(i, i2);
        this.mSelectionRect = new Rect(i, i2, i, i2);
    }

    public void updateSelection(int i, int i2) {
        Rect rect = this.mSelectionRect;
        if (rect != null) {
            rect.left = Math.min(this.mStartPoint.x, i);
            this.mSelectionRect.right = Math.max(this.mStartPoint.x, i);
            this.mSelectionRect.top = Math.min(this.mStartPoint.y, i2);
            this.mSelectionRect.bottom = Math.max(this.mStartPoint.y, i2);
            invalidate();
        }
    }

    public Rect getSelectionRect() {
        return this.mSelectionRect;
    }

    public void stopSelection() {
        this.mStartPoint = null;
        this.mSelectionRect = null;
    }

    public void draw(Canvas canvas) {
        canvas.drawRect((float) ((View) this).mLeft, (float) ((View) this).mTop, (float) ((View) this).mRight, (float) ((View) this).mBottom, this.mPaintBackground);
        Rect rect = this.mSelectionRect;
        if (rect != null) {
            canvas.drawRect(rect, this.mPaintSelection);
        }
    }
}
