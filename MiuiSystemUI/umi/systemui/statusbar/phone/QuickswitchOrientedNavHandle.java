package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import com.android.systemui.C0009R$dimen;

public class QuickswitchOrientedNavHandle extends NavigationHandle {
    private int mDeltaRotation;
    private final RectF mTmpBoundsRectF = new RectF();
    private final int mWidth;

    public QuickswitchOrientedNavHandle(Context context) {
        super(context);
        this.mWidth = context.getResources().getDimensionPixelSize(C0009R$dimen.navigation_home_handle_width);
    }

    /* access modifiers changed from: package-private */
    public void setDeltaRotation(int i) {
        this.mDeltaRotation = i;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        RectF computeHomeHandleBounds = computeHomeHandleBounds();
        int i = this.mRadius;
        canvas.drawRoundRect(computeHomeHandleBounds, (float) i, (float) i, this.mPaint);
    }

    /* access modifiers changed from: package-private */
    public RectF computeHomeHandleBounds() {
        int i;
        int i2;
        int i3;
        int i4;
        int i5 = this.mRadius * 2;
        int i6 = getLocationOnScreen()[1];
        int i7 = this.mDeltaRotation;
        if (i7 == 1) {
            int i8 = this.mBottom;
            i = i8 + i5;
            int i9 = this.mWidth;
            int height = ((getHeight() / 2) - (i9 / 2)) - (i6 / 2);
            int i10 = i8;
            i2 = height;
            i4 = height + i9;
            i3 = i10;
        } else if (i7 != 3) {
            int i11 = this.mRadius * 2;
            i3 = (getWidth() / 2) - (this.mWidth / 2);
            i2 = (getHeight() - this.mBottom) - i11;
            i = (getWidth() / 2) + (this.mWidth / 2);
            i4 = i11 + i2;
        } else {
            i = getWidth() - this.mBottom;
            int i12 = this.mWidth;
            i2 = ((getHeight() / 2) - (i12 / 2)) - (i6 / 2);
            i3 = i - i5;
            i4 = i2 + i12;
        }
        this.mTmpBoundsRectF.set((float) i3, (float) i2, (float) i, (float) i4);
        return this.mTmpBoundsRectF;
    }
}
