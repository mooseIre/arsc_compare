package com.android.systemui.fsgesture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundCornerImageView extends ImageView {
    private Path mPath;
    private float mRadius;

    public RoundCornerImageView(Context context) {
        this(context, null);
    }

    public RoundCornerImageView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RoundCornerImageView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public RoundCornerImageView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mRadius = (float) CornerRadiusUtils.getPhoneRadius(context);
        this.mPath = new Path();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        canvas.save();
        this.mPath.reset();
        float f = this.mRadius;
        this.mPath.addRoundRect(0.0f, 0.0f, (float) getMeasuredWidth(), (float) getMeasuredHeight(), f, f, Path.Direction.CW);
        canvas.clipPath(this.mPath);
        super.onDraw(canvas);
        canvas.restore();
    }
}
