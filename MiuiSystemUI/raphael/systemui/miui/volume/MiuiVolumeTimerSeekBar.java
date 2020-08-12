package com.android.systemui.miui.volume;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.systemui.miui.widget.TimerSeekBar;

public class MiuiVolumeTimerSeekBar extends TimerSeekBar {
    private int mBoundsStart;
    private int mCurrentSegment;
    protected BoundsSeekBarInjector mInjector;
    private TimerSeekBarMotions mMotions;
    private int mTimeRemain;

    interface TimerSeekBarMotions {
        void addCountDownStateReceiver(TextView textView);

        void addTickingTimeReceiver(TextView textView);

        void onSegmentChange(int i, int i2);

        void onTimeUpdate(int i);

        void onTouchDown();

        void onTouchRelease();
    }

    public MiuiVolumeTimerSeekBar(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiVolumeTimerSeekBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiVolumeTimerSeekBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.MiuiVolumeTimerSeekBar, i, 0);
        this.mBoundsStart = obtainStyledAttributes.getInt(R$styleable.MiuiVolumeTimerSeekBar_progressBoundsStart, 0);
        boolean z = obtainStyledAttributes.getBoolean(R$styleable.MiuiVolumeTimerSeekBar_drawTickingTime, true);
        obtainStyledAttributes.recycle();
        this.mMotions = new MiuiVolumeTimerDrawableHelper(this, z);
        this.mInjector = new BoundsSeekBarInjector(this, false);
        this.mInjector.setBounds((float) this.mBoundsStart, (float) getMax());
    }

    /* access modifiers changed from: protected */
    public void transformTouchEvent(MotionEvent motionEvent) {
        this.mInjector.transformTouchEvent(motionEvent);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        transformTouchEvent(motionEvent);
        return super.onTouchEvent(motionEvent);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        super.onStartTrackingTouch(seekBar);
        this.mMotions.onTouchDown();
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        super.onStopTrackingTouch(seekBar);
        this.mMotions.onTouchRelease();
    }

    public void onTimeSet(int i) {
        super.onTimeSet(i);
        this.mTimeRemain = i;
        this.mMotions.onTimeUpdate(i);
    }

    public void onSegmentChange(int i, int i2) {
        super.onSegmentChange(i, i2);
        this.mCurrentSegment = i;
        this.mMotions.onSegmentChange(i, i2);
    }

    public void onTimeUpdate(int i) {
        super.onTimeUpdate(i);
        this.mTimeRemain = i;
        this.mMotions.onTimeUpdate(i);
    }

    private int constrainProgress(int i) {
        return Util.constrain(i, this.mBoundsStart, getMax());
    }

    public synchronized void setProgress(int i) {
        super.setProgress(constrainProgress(i));
    }

    public synchronized void setMax(int i) {
        super.setMax(i);
        if (this.mInjector != null) {
            this.mInjector.setBounds((float) this.mBoundsStart, (float) i);
        }
    }

    public int getRemainTime() {
        return this.mTimeRemain;
    }

    public void addTickingTimeReceiver(TextView textView) {
        this.mMotions.addTickingTimeReceiver(textView);
    }

    public void addCountDownStateReceiver(TextView textView) {
        this.mMotions.addCountDownStateReceiver(textView);
    }
}
