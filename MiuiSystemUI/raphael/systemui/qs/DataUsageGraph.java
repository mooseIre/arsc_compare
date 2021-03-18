package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.android.settingslib.Utils;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;

public class DataUsageGraph extends View {
    private long mLimitLevel;
    private final int mMarkerWidth;
    private long mMaxLevel;
    private final int mOverlimitColor;
    private final Paint mTmpPaint = new Paint();
    private final RectF mTmpRect = new RectF();
    private final int mTrackColor;
    private final int mUsageColor;
    private long mUsageLevel;
    private final int mWarningColor;
    private long mWarningLevel;

    public DataUsageGraph(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Resources resources = context.getResources();
        this.mTrackColor = Utils.getColorStateListDefaultColor(context, C0011R$color.data_usage_graph_track);
        this.mWarningColor = Utils.getColorStateListDefaultColor(context, C0011R$color.data_usage_graph_warning);
        this.mUsageColor = Utils.getColorAccentDefaultColor(context);
        this.mOverlimitColor = Utils.getColorErrorDefaultColor(context);
        this.mMarkerWidth = resources.getDimensionPixelSize(C0012R$dimen.data_usage_graph_marker_width);
    }

    public void setLevels(long j, long j2, long j3) {
        this.mLimitLevel = Math.max(0L, j);
        this.mWarningLevel = Math.max(0L, j2);
        this.mUsageLevel = Math.max(0L, j3);
        this.mMaxLevel = Math.max(Math.max(Math.max(this.mLimitLevel, this.mWarningLevel), this.mUsageLevel), 1L);
        postInvalidate();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rectF = this.mTmpRect;
        Paint paint = this.mTmpPaint;
        int width = getWidth();
        int height = getHeight();
        long j = this.mLimitLevel;
        boolean z = j > 0 && this.mUsageLevel > j;
        float f = (float) width;
        long j2 = this.mMaxLevel;
        float f2 = (((float) this.mUsageLevel) / ((float) j2)) * f;
        if (z) {
            int i = this.mMarkerWidth;
            f2 = Math.min(Math.max(((((float) this.mLimitLevel) / ((float) j2)) * f) - ((float) (i / 2)), (float) i), (float) (width - (this.mMarkerWidth * 2)));
            rectF.set(((float) this.mMarkerWidth) + f2, 0.0f, f, (float) height);
            paint.setColor(this.mOverlimitColor);
            canvas.drawRect(rectF, paint);
        } else {
            rectF.set(0.0f, 0.0f, f, (float) height);
            paint.setColor(this.mTrackColor);
            canvas.drawRect(rectF, paint);
        }
        float f3 = (float) height;
        rectF.set(0.0f, 0.0f, f2, f3);
        paint.setColor(this.mUsageColor);
        canvas.drawRect(rectF, paint);
        float min = Math.min(Math.max((f * (((float) this.mWarningLevel) / ((float) this.mMaxLevel))) - ((float) (this.mMarkerWidth / 2)), 0.0f), (float) (width - this.mMarkerWidth));
        rectF.set(min, 0.0f, ((float) this.mMarkerWidth) + min, f3);
        paint.setColor(this.mWarningColor);
        canvas.drawRect(rectF, paint);
    }
}
