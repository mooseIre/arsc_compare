package com.android.systemui.miui.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.SeekBar;
import com.android.systemui.Interpolators;
import miui.widget.SeekBar;

public class TimerSeekBar extends SeekBar implements SeekBar.OnSeekBarChangeListener {
    private int mCurrentSegmentPoint;
    private int mDeterminedSegmentPoint;
    private boolean mDragging;
    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener;
    private OnTimeUpdateListener mOnTimeUpdateListener;
    private int[] mTimeSegments;

    public interface OnTimeUpdateListener {
        void onSegmentChange(int i, int i2);

        void onTimeSet(int i);

        void onTimeUpdate(int i);
    }

    public TimerSeekBar(Context context) {
        this(context, (AttributeSet) null);
    }

    public TimerSeekBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TimerSeekBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.TimerSeekBar, i, 0);
        int resourceId = obtainStyledAttributes.getResourceId(R$styleable.TimerSeekBar_timeSegments, 0);
        if (resourceId > 0) {
            this.mTimeSegments = getResources().getIntArray(resourceId);
        }
        obtainStyledAttributes.recycle();
        superSetOnSeekBarChangeListener(this);
    }

    public void setOnTimeUpdateListener(OnTimeUpdateListener onTimeUpdateListener) {
        this.mOnTimeUpdateListener = onTimeUpdateListener;
    }

    public void updateRemainTime(int i) {
        if (!this.mDragging) {
            setProgress(timeToProgress(i));
            onTimeUpdate(i);
        }
    }

    private int timeToProgress(int i) {
        int[] iArr = this.mTimeSegments;
        int length = iArr.length;
        int i2 = iArr[length - 1];
        if (i < 0) {
            i = 0;
        } else if (i > i2) {
            i = i2;
        }
        if (i == i2) {
            return getMax();
        }
        for (int length2 = this.mTimeSegments.length - 1; length2 >= 0; length2--) {
            int[] iArr2 = this.mTimeSegments;
            if (iArr2[length2] < i) {
                int i3 = iArr2[length2];
                int i4 = length2 + 1;
                return (int) (((float) (getMax() / length)) * (((float) i4) + (((float) (i - i3)) / ((float) (iArr2[i4] - i3)))));
            }
        }
        return 0;
    }

    private int determineProgressToSegment(int i) {
        int max = getMax() / this.mTimeSegments.length;
        int i2 = i / max;
        return i < ((int) ((((double) i2) + 0.5d) * ((double) max))) ? i2 : i2 + 1;
    }

    private void setCurrentSegment(int i, int i2) {
        if (i != this.mCurrentSegmentPoint || i2 != this.mDeterminedSegmentPoint) {
            this.mCurrentSegmentPoint = i;
            this.mDeterminedSegmentPoint = i2;
            onSegmentChange(this.mCurrentSegmentPoint, this.mDeterminedSegmentPoint);
        }
    }

    public void onProgressChanged(android.widget.SeekBar seekBar, int i, boolean z) {
        setCurrentSegment(i / (getMax() / this.mTimeSegments.length), determineProgressToSegment(i));
        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = this.mOnSeekBarChangeListener;
        if (onSeekBarChangeListener != null) {
            onSeekBarChangeListener.onProgressChanged(seekBar, i, z);
        }
    }

    public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
        this.mDragging = true;
        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = this.mOnSeekBarChangeListener;
        if (onSeekBarChangeListener != null) {
            onSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
        int i = 0;
        this.mDragging = false;
        int i2 = this.mDeterminedSegmentPoint;
        setCurrentSegment(i2, i2);
        animateToProgress((this.mCurrentSegmentPoint * (getMax() / this.mTimeSegments.length)) - 1);
        int i3 = this.mCurrentSegmentPoint;
        if (i3 != 0) {
            i = this.mTimeSegments[i3 - 1];
        }
        onTimeSet(i);
        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = this.mOnSeekBarChangeListener;
        if (onSeekBarChangeListener != null) {
            onSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    private void animateToProgress(int i) {
        ObjectAnimator ofInt = ObjectAnimator.ofInt(this, "progress", new int[]{i});
        ofInt.setDuration(300);
        ofInt.setAutoCancel(true);
        ofInt.setInterpolator(Interpolators.DECELERATE_QUART);
        ofInt.start();
    }

    /* access modifiers changed from: protected */
    public void onTimeSet(int i) {
        OnTimeUpdateListener onTimeUpdateListener = this.mOnTimeUpdateListener;
        if (onTimeUpdateListener != null) {
            onTimeUpdateListener.onTimeSet(i);
        }
    }

    /* access modifiers changed from: protected */
    public void onSegmentChange(int i, int i2) {
        OnTimeUpdateListener onTimeUpdateListener = this.mOnTimeUpdateListener;
        if (onTimeUpdateListener != null) {
            onTimeUpdateListener.onSegmentChange(i, i2);
        }
    }

    /* access modifiers changed from: protected */
    public void onTimeUpdate(int i) {
        OnTimeUpdateListener onTimeUpdateListener = this.mOnTimeUpdateListener;
        if (onTimeUpdateListener != null) {
            onTimeUpdateListener.onTimeUpdate(i);
        }
    }

    private void superSetOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {
        super.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {
        this.mOnSeekBarChangeListener = onSeekBarChangeListener;
    }
}
