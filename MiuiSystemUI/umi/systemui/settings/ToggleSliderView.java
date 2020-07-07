package com.android.systemui.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import com.android.systemui.plugins.R;
import com.android.systemui.settings.ToggleSlider;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;

public class ToggleSliderView extends RelativeLayout implements ToggleSlider {
    /* access modifiers changed from: private */
    public boolean mIgnoreTrackingEvent;
    /* access modifiers changed from: private */
    public int mLastTouchAction;
    /* access modifiers changed from: private */
    public ToggleSlider.Listener mListener;
    private ToggleSliderView mMirror;
    /* access modifiers changed from: private */
    public BrightnessMirrorController mMirrorController;
    private final SeekBar.OnSeekBarChangeListener mSeekListener;
    /* access modifiers changed from: private */
    public ToggleSeekBar mSlider;
    /* access modifiers changed from: private */
    public boolean mTracking;

    public ToggleSliderView(Context context) {
        this(context, (AttributeSet) null);
    }

    public ToggleSliderView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ToggleSliderView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (ToggleSliderView.this.mListener != null) {
                    ToggleSlider.Listener access$000 = ToggleSliderView.this.mListener;
                    ToggleSliderView toggleSliderView = ToggleSliderView.this;
                    access$000.onChanged(toggleSliderView, toggleSliderView.mTracking, i, false);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                if (ToggleSliderView.this.mLastTouchAction == 1) {
                    boolean unused = ToggleSliderView.this.mIgnoreTrackingEvent = true;
                    Log.w("ToggleSliderView", "ignoring onStartTrackingTouch, maybe tap event");
                    return;
                }
                boolean unused2 = ToggleSliderView.this.mTracking = true;
                if (ToggleSliderView.this.mListener != null) {
                    ToggleSliderView.this.mListener.onStart(seekBar.getProgress());
                    ToggleSlider.Listener access$000 = ToggleSliderView.this.mListener;
                    ToggleSliderView toggleSliderView = ToggleSliderView.this;
                    access$000.onChanged(toggleSliderView, toggleSliderView.mTracking, ToggleSliderView.this.mSlider.getProgress(), false);
                }
                if (ToggleSliderView.this.mMirrorController != null) {
                    ToggleSliderView.this.mMirrorController.showMirror();
                    ToggleSliderView.this.mMirrorController.setLocation(ToggleSliderView.this);
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (ToggleSliderView.this.mIgnoreTrackingEvent) {
                    boolean unused = ToggleSliderView.this.mIgnoreTrackingEvent = false;
                    Log.w("ToggleSliderView", "ignoring onStopTrackingTouch, maybe tap event");
                    return;
                }
                boolean unused2 = ToggleSliderView.this.mTracking = false;
                ToggleSliderView.this.mSlider.setProgress(seekBar.getProgress());
                if (ToggleSliderView.this.mListener != null) {
                    ToggleSlider.Listener access$000 = ToggleSliderView.this.mListener;
                    ToggleSliderView toggleSliderView = ToggleSliderView.this;
                    access$000.onChanged(toggleSliderView, toggleSliderView.mTracking, ToggleSliderView.this.mSlider.getProgress(), true);
                    ToggleSliderView.this.mListener.onStop(seekBar.getProgress());
                }
                if (ToggleSliderView.this.mMirrorController != null) {
                    ToggleSliderView.this.mMirrorController.hideMirror();
                }
            }
        };
        View.inflate(context, R.layout.status_bar_toggle_slider, this);
        ToggleSeekBar toggleSeekBar = (ToggleSeekBar) findViewById(R.id.slider);
        this.mSlider = toggleSeekBar;
        toggleSeekBar.setOnSeekBarChangeListener(this.mSeekListener);
        this.mSlider.setAccessibilityLabel(getContentDescription().toString());
    }

    public void setMirror(ToggleSliderView toggleSliderView) {
        this.mMirror = toggleSliderView;
        if (toggleSliderView != null) {
            toggleSliderView.setMax(this.mSlider.getMax());
            this.mMirror.setValue(this.mSlider.getProgress());
        }
    }

    public void setMirrorController(BrightnessMirrorController brightnessMirrorController) {
        this.mMirrorController = brightnessMirrorController;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ToggleSlider.Listener listener = this.mListener;
        if (listener != null) {
            listener.onInit(this);
        }
    }

    public void setOnChangedListener(ToggleSlider.Listener listener) {
        this.mListener = listener;
    }

    public void setMax(int i) {
        if (i != this.mSlider.getMax()) {
            this.mSlider.setMax(i);
            ToggleSliderView toggleSliderView = this.mMirror;
            if (toggleSliderView != null) {
                toggleSliderView.setMax(i);
            }
        }
    }

    public void setValue(int i) {
        this.mSlider.setProgress(i);
        ToggleSliderView toggleSliderView = this.mMirror;
        if (toggleSliderView != null) {
            toggleSliderView.setValue(i);
        }
    }

    public int getValue() {
        return this.mSlider.getProgress();
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        this.mLastTouchAction = motionEvent.getActionMasked();
        if (motionEvent.getActionMasked() == 0) {
            this.mIgnoreTrackingEvent = false;
            ToggleSliderView toggleSliderView = this.mMirror;
            if (toggleSliderView != null) {
                toggleSliderView.setValue(this.mSlider.getProgress());
            }
        }
        if (this.mMirror != null) {
            MotionEvent copy = motionEvent.copy();
            this.mMirror.dispatchTouchEvent(copy);
            copy.recycle();
        }
        return super.dispatchTouchEvent(motionEvent);
    }
}
