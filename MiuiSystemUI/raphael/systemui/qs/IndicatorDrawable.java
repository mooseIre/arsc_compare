package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: IndicatorDrawable.kt */
public final class IndicatorDrawable extends Drawable {
    private final int mCaretHeight;
    private final Paint mCaretPaint = new Paint();
    private float mCaretProgress;
    private final int mCaretWidth;
    private final Path mPath = new Path();
    private final Paint mShadowPaint = new Paint();

    public int getOpacity() {
        return -3;
    }

    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    public IndicatorDrawable(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Resources resources = context.getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(C0012R$dimen.qs_panel_expand_indicator_stroke_width);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(C0012R$dimen.qs_panel_expand_indicator_shadow_spread);
        this.mCaretPaint.setColor(context.getColor(C0011R$color.qs_panel_expand_indicator_color));
        this.mCaretPaint.setAntiAlias(true);
        float f = (float) dimensionPixelSize;
        this.mCaretPaint.setStrokeWidth(((float) dimensionPixelSize2) + f);
        this.mCaretPaint.setStyle(Paint.Style.STROKE);
        this.mCaretPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mCaretPaint.setStrokeJoin(Paint.Join.MITER);
        this.mShadowPaint.setColor(context.getColor(C0011R$color.qs_tile_divider));
        this.mShadowPaint.setAntiAlias(true);
        this.mShadowPaint.setStrokeWidth(f + ((float) (dimensionPixelSize2 * 2)));
        this.mShadowPaint.setStyle(Paint.Style.STROKE);
        this.mShadowPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mShadowPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mCaretWidth = resources.getDimensionPixelSize(C0012R$dimen.qs_panel_expand_indicator_width);
        this.mCaretHeight = resources.getDimensionPixelSize(C0012R$dimen.qs_panel_expand_indicator_height);
    }

    public int getIntrinsicWidth() {
        return this.mCaretWidth;
    }

    public int getIntrinsicHeight() {
        return this.mCaretHeight;
    }

    public void draw(@NotNull Canvas canvas) {
        Intrinsics.checkParameterIsNotNull(canvas, "canvas");
        if (Float.compare((float) this.mCaretPaint.getAlpha(), 0.0f) != 0) {
            float width = ((float) getBounds().width()) - this.mShadowPaint.getStrokeWidth();
            float height = ((float) getBounds().height()) - this.mShadowPaint.getStrokeWidth();
            float f = (float) 2;
            float strokeWidth = ((float) getBounds().left) + (this.mShadowPaint.getStrokeWidth() / f);
            float strokeWidth2 = ((float) getBounds().top) + ((height - (this.mShadowPaint.getStrokeWidth() / ((float) 3))) / f);
            float f2 = height / ((float) 4);
            this.mPath.reset();
            float f3 = (float) 1;
            this.mPath.moveTo(strokeWidth, ((f3 - getNormalizedCaretProgress()) * f2) + strokeWidth2);
            this.mPath.lineTo((width / f) + strokeWidth, (getNormalizedCaretProgress() * f2) + strokeWidth2);
            this.mPath.lineTo(strokeWidth + width, strokeWidth2 + (f2 * (f3 - getNormalizedCaretProgress())));
            canvas.drawPath(this.mPath, this.mCaretPaint);
        }
    }

    public final void setCaretProgress(float f) {
        this.mCaretProgress = f;
        invalidateSelf();
    }

    public final float getNormalizedCaretProgress() {
        return (this.mCaretProgress - -1.0f) / 2.0f;
    }

    public void setAlpha(int i) {
        this.mCaretPaint.setAlpha(i);
        this.mShadowPaint.setAlpha(i);
        invalidateSelf();
    }
}
