package com.android.keyguard.smartcover;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.systemui.plugins.R;

public class BatteryLevelImageView extends ImageView {
    private int mLevel;
    private int mPadding;

    public BatteryLevelImageView(Context context) {
        this(context, (AttributeSet) null);
    }

    public BatteryLevelImageView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BatteryLevelImageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPadding = getContext().getResources().getDimensionPixelSize(R.dimen.smart_cover_battery_padding);
    }

    public void setBatteryLevel(int i) {
        this.mLevel = i;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.clipRect(this.mPaddingLeft, this.mPadding + ((int) ((((double) (((this.mBottom - this.mTop) - (this.mPadding * 2)) * (100 - this.mLevel))) * 1.0d) / 100.0d)), (this.mRight - this.mLeft) - this.mPaddingRight, (this.mBottom - this.mTop) - this.mPaddingBottom);
        canvas.translate((float) this.mPaddingLeft, (float) this.mPaddingTop);
        getDrawable().draw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
