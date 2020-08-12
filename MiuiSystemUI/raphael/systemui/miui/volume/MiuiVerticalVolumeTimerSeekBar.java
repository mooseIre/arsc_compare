package com.android.systemui.miui.volume;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MiuiVerticalVolumeTimerSeekBar extends MiuiVolumeTimerSeekBar {
    public MiuiVerticalVolumeTimerSeekBar(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiVerticalVolumeTimerSeekBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiVerticalVolumeTimerSeekBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setLayoutDirection(0);
        this.mInjector.setVertical(true);
    }

    /* access modifiers changed from: protected */
    public synchronized void onDraw(Canvas canvas) {
        drawProgress(canvas);
    }

    private void drawProgress(Canvas canvas) {
        Drawable progressDrawable = getProgressDrawable();
        if (progressDrawable != null) {
            canvas.save();
            canvas.rotate(-90.0f, (float) (getWidth() / 2), (float) (getHeight() / 2));
            int width = getWidth();
            int height = getHeight();
            int i = height - width;
            int i2 = (height + width) / 2;
            progressDrawable.setBounds(((-i) / 2) + getPaddingBottom(), (i / 2) + getPaddingLeft(), i2 - getPaddingTop(), i2 - getPaddingRight());
            progressDrawable.draw(canvas);
            canvas.restore();
        }
    }

    /* access modifiers changed from: protected */
    public void transformTouchEvent(MotionEvent motionEvent) {
        super.transformTouchEvent(motionEvent);
        motionEvent.setLocation(((((((float) getHeight()) - motionEvent.getY()) - ((float) getPaddingBottom())) / ((float) ((getHeight() - getPaddingTop()) - getPaddingBottom()))) * ((float) ((getWidth() - getPaddingLeft()) - getPaddingRight()))) + ((float) getPaddingLeft()), motionEvent.getY());
    }
}
