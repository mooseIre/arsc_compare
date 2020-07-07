package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import com.android.systemui.plugins.R;

public class CaretDrawable extends Drawable {
    private final int mCaretHeight;
    private Paint mCaretPaint = new Paint();
    private float mCaretProgress = 0.0f;
    private final int mCaretWidth;
    private Path mPath = new Path();
    private Paint mShadowPaint = new Paint();

    public int getOpacity() {
        return -3;
    }

    public void setColorFilter(ColorFilter colorFilter) {
    }

    public CaretDrawable(Context context) {
        Resources resources = context.getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.qs_panel_expand_indicator_stroke_width);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(R.dimen.qs_panel_expand_indicator_shadow_spread);
        this.mCaretPaint.setColor(resources.getColor(R.color.qs_panel_expand_indicator_color));
        this.mCaretPaint.setAntiAlias(true);
        this.mCaretPaint.setStrokeWidth((float) (dimensionPixelSize + dimensionPixelSize2));
        this.mCaretPaint.setStyle(Paint.Style.STROKE);
        this.mCaretPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mCaretPaint.setStrokeJoin(Paint.Join.MITER);
        this.mShadowPaint.setColor(resources.getColor(R.color.qs_tile_divider));
        this.mShadowPaint.setAntiAlias(true);
        this.mShadowPaint.setStrokeWidth((float) (dimensionPixelSize + (dimensionPixelSize2 * 2)));
        this.mShadowPaint.setStyle(Paint.Style.STROKE);
        this.mShadowPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mShadowPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mCaretWidth = resources.getDimensionPixelSize(R.dimen.qs_panel_expand_indicator_width);
        this.mCaretHeight = resources.getDimensionPixelSize(R.dimen.qs_panel_expand_indicator_height);
    }

    public int getIntrinsicWidth() {
        return this.mCaretWidth;
    }

    public int getIntrinsicHeight() {
        return this.mCaretHeight;
    }

    public void draw(Canvas canvas) {
        if (Float.compare((float) this.mCaretPaint.getAlpha(), 0.0f) != 0) {
            float width = ((float) getBounds().width()) - this.mShadowPaint.getStrokeWidth();
            float height = ((float) getBounds().height()) - this.mShadowPaint.getStrokeWidth();
            float strokeWidth = ((float) getBounds().left) + (this.mShadowPaint.getStrokeWidth() / 2.0f);
            float strokeWidth2 = ((float) getBounds().top) + ((height - (this.mShadowPaint.getStrokeWidth() / 3.0f)) / 2.0f);
            float f = height / 4.0f;
            this.mPath.reset();
            this.mPath.moveTo(strokeWidth, ((1.0f - getNormalizedCaretProgress()) * f) + strokeWidth2);
            this.mPath.lineTo((width / 2.0f) + strokeWidth, (getNormalizedCaretProgress() * f) + strokeWidth2);
            this.mPath.lineTo(strokeWidth + width, strokeWidth2 + (f * (1.0f - getNormalizedCaretProgress())));
            canvas.drawPath(this.mPath, this.mCaretPaint);
        }
    }

    public void setCaretProgress(float f) {
        this.mCaretProgress = f;
        invalidateSelf();
    }

    public float getNormalizedCaretProgress() {
        return (this.mCaretProgress - -1.0f) / 2.0f;
    }

    public void setAlpha(int i) {
        this.mCaretPaint.setAlpha(i);
        this.mShadowPaint.setAlpha(i);
        invalidateSelf();
    }
}
