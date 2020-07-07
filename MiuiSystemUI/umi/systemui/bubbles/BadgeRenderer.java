package com.android.systemui.bubbles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import com.android.systemui.plugins.R;

public class BadgeRenderer {
    private final Paint mCirclePaint = new Paint(3);
    private final float mCircleRadius;
    private final float mDotCenterOffset;

    static float getDotRadius(float f) {
        return ((float) ((int) (f * 0.6f))) / 2.0f;
    }

    public BadgeRenderer(Context context) {
        float dotCenterOffset = getDotCenterOffset(context);
        this.mDotCenterOffset = dotCenterOffset;
        this.mCircleRadius = getDotRadius(dotCenterOffset);
    }

    static float getDotCenterOffset(Context context) {
        return ((float) context.getResources().getDimensionPixelSize(R.dimen.individual_bubble_size)) * 0.38f;
    }

    public void draw(Canvas canvas, int i, Rect rect, float f, Point point, boolean z) {
        if (rect == null) {
            Log.e("BadgeRenderer", "Invalid null argument(s) passed in call to draw.");
            return;
        }
        canvas.save();
        canvas.translate(((float) (z ? rect.left : rect.right)) + (z ? this.mDotCenterOffset / 2.0f : -(this.mDotCenterOffset / 2.0f)) + ((float) point.x), (((float) rect.top) + (this.mDotCenterOffset / 2.0f)) - ((float) point.y));
        canvas.scale(f, f);
        this.mCirclePaint.setColor(i);
        canvas.drawCircle(0.0f, 0.0f, this.mCircleRadius, this.mCirclePaint);
        canvas.restore();
    }
}
