package com.android.systemui.bubbles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.systemui.plugins.R;
import com.android.systemui.util.ColorUtils;

public class BadgedImageView extends ImageView {
    private BadgeRenderer mDotRenderer;
    private float mDotScale;
    private int mIconSize;
    private boolean mOnLeft;
    private boolean mShowUpdateDot;
    private Rect mTempBounds;
    private Point mTempPoint;
    private int mUpdateDotColor;

    public BadgedImageView(Context context) {
        this(context, (AttributeSet) null);
    }

    public BadgedImageView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BadgedImageView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public BadgedImageView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTempBounds = new Rect();
        this.mTempPoint = new Point();
        this.mDotScale = 0.0f;
        this.mIconSize = getResources().getDimensionPixelSize(R.dimen.individual_bubble_size);
        this.mDotRenderer = new BadgeRenderer(getContext());
        context.obtainStyledAttributes(new int[]{16844002}).recycle();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mShowUpdateDot) {
            getDrawingRect(this.mTempBounds);
            this.mTempPoint.set((getWidth() - this.mIconSize) / 2, getPaddingTop());
            this.mDotRenderer.draw(canvas, this.mUpdateDotColor, this.mTempBounds, this.mDotScale, this.mTempPoint, this.mOnLeft);
        }
    }

    public void setDotPosition(boolean z) {
        this.mOnLeft = z;
        invalidate();
    }

    public boolean getDotPosition() {
        return this.mOnLeft;
    }

    public void setShowDot(boolean z) {
        this.mShowUpdateDot = z;
        invalidate();
    }

    public boolean isShowingDot() {
        return this.mShowUpdateDot;
    }

    public void setDotColor(int i) {
        this.mUpdateDotColor = ColorUtils.setAlphaComponent(i, 255);
        invalidate();
    }

    public void setDotScale(float f) {
        this.mDotScale = f;
        invalidate();
    }
}
