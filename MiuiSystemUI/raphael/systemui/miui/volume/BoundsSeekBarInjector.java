package com.android.systemui.miui.volume;

import android.view.MotionEvent;
import android.widget.SeekBar;
import com.android.systemui.miui.widget.RelativeSeekBarInjector;

class BoundsSeekBarInjector extends RelativeSeekBarInjector {
    private float mBoundsEnd;
    private float mBoundsStart;
    private SeekBar mSeekBar;
    private float mTouchBoundsEnd;
    private float mTouchBoundsStart;
    private boolean mVertical;

    public BoundsSeekBarInjector(SeekBar seekBar, boolean z) {
        super(seekBar, z);
        this.mSeekBar = seekBar;
        this.mVertical = z;
    }

    public void setBounds(float f, float f2) {
        this.mBoundsStart = f;
        this.mBoundsEnd = f2;
    }

    public void setVertical(boolean z) {
        super.setVertical(z);
        this.mVertical = z;
    }

    public void transformTouchEvent(MotionEvent motionEvent) {
        super.transformTouchEvent(motionEvent);
        if (motionEvent.getAction() == 0) {
            computeTouchOffset();
        }
        if (this.mVertical) {
            motionEvent.offsetLocation(0.0f, Util.constrain(motionEvent.getY(), Math.min(this.mTouchBoundsStart, this.mTouchBoundsEnd), Math.max(this.mTouchBoundsStart, this.mTouchBoundsEnd)) - motionEvent.getY());
        } else {
            motionEvent.offsetLocation(Util.constrain(motionEvent.getX(), Math.min(this.mTouchBoundsStart, this.mTouchBoundsEnd), Math.max(this.mTouchBoundsStart, this.mTouchBoundsEnd)) - motionEvent.getX(), 0.0f);
        }
    }

    private void computeTouchOffset() {
        if (this.mVertical) {
            float height = (float) ((this.mSeekBar.getHeight() - this.mSeekBar.getPaddingTop()) - this.mSeekBar.getPaddingBottom());
            this.mTouchBoundsStart = ((float) this.mSeekBar.getPaddingTop()) + ((1.0f - (this.mBoundsStart / ((float) this.mSeekBar.getMax()))) * height);
            this.mTouchBoundsEnd = ((float) this.mSeekBar.getPaddingTop()) + ((1.0f - (this.mBoundsEnd / ((float) this.mSeekBar.getMax()))) * height);
            return;
        }
        float width = (float) ((this.mSeekBar.getWidth() - this.mSeekBar.getPaddingLeft()) - this.mSeekBar.getPaddingRight());
        this.mTouchBoundsStart = ((float) this.mSeekBar.getPaddingLeft()) + ((this.mBoundsStart / ((float) this.mSeekBar.getMax())) * width);
        this.mTouchBoundsEnd = ((float) this.mSeekBar.getPaddingLeft()) + ((this.mBoundsEnd / ((float) this.mSeekBar.getMax())) * width);
    }
}
