package com.android.systemui.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.settingslib.RestrictedLockUtils;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.R$styleable;
import com.android.systemui.settings.ToggleSlider;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;

public class ToggleSliderView extends RelativeLayout implements ToggleSlider {
    private final CompoundButton.OnCheckedChangeListener mCheckListener;
    /* access modifiers changed from: private */
    public boolean mIgnoreTrackingEvent;
    private TextView mLabel;
    /* access modifiers changed from: private */
    public int mLastTouchAction;
    /* access modifiers changed from: private */
    public ToggleSlider.Listener mListener;
    /* access modifiers changed from: private */
    public ToggleSliderView mMirror;
    /* access modifiers changed from: private */
    public BrightnessMirrorController mMirrorController;
    private final SeekBar.OnSeekBarChangeListener mSeekListener;
    /* access modifiers changed from: private */
    public ToggleSeekBar mSlider;
    /* access modifiers changed from: private */
    public CompoundButton mToggle;
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
        this.mCheckListener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                ToggleSliderView.this.mSlider.setEnabled(!z);
                if (ToggleSliderView.this.mListener != null) {
                    ToggleSlider.Listener access$100 = ToggleSliderView.this.mListener;
                    ToggleSliderView toggleSliderView = ToggleSliderView.this;
                    access$100.onChanged(toggleSliderView, toggleSliderView.mTracking, z, ToggleSliderView.this.mSlider.getProgress(), false);
                }
                if (ToggleSliderView.this.mMirror != null) {
                    ToggleSliderView.this.mMirror.mToggle.setChecked(z);
                }
            }
        };
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (ToggleSliderView.this.mListener != null) {
                    ToggleSlider.Listener access$100 = ToggleSliderView.this.mListener;
                    ToggleSliderView toggleSliderView = ToggleSliderView.this;
                    access$100.onChanged(toggleSliderView, toggleSliderView.mTracking, ToggleSliderView.this.mToggle.isChecked(), i, false);
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
                    ToggleSlider.Listener access$100 = ToggleSliderView.this.mListener;
                    ToggleSliderView toggleSliderView = ToggleSliderView.this;
                    access$100.onChanged(toggleSliderView, toggleSliderView.mTracking, ToggleSliderView.this.mToggle.isChecked(), ToggleSliderView.this.mSlider.getProgress(), false);
                }
                ToggleSliderView.this.mToggle.setChecked(false);
                if (ToggleSliderView.this.mMirrorController != null) {
                    ToggleSliderView.this.mMirrorController.showMirror();
                    ToggleSliderView.this.mMirrorController.setLocation((View) ToggleSliderView.this.getParent());
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (ToggleSliderView.this.mIgnoreTrackingEvent) {
                    boolean unused = ToggleSliderView.this.mIgnoreTrackingEvent = false;
                    Log.w("ToggleSliderView", "ignoring onStopTrackingTouch, maybe tap event");
                    return;
                }
                boolean unused2 = ToggleSliderView.this.mTracking = false;
                if (ToggleSliderView.this.mListener != null) {
                    ToggleSlider.Listener access$100 = ToggleSliderView.this.mListener;
                    ToggleSliderView toggleSliderView = ToggleSliderView.this;
                    access$100.onChanged(toggleSliderView, toggleSliderView.mTracking, ToggleSliderView.this.mToggle.isChecked(), ToggleSliderView.this.mSlider.getProgress(), true);
                    ToggleSliderView.this.mListener.onStop(seekBar.getProgress());
                }
                if (ToggleSliderView.this.mMirrorController != null) {
                    ToggleSliderView.this.mMirrorController.hideMirror();
                }
            }
        };
        View.inflate(context, C0017R$layout.status_bar_toggle_slider, this);
        context.getResources();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.ToggleSliderView, i, 0);
        CompoundButton compoundButton = (CompoundButton) findViewById(C0015R$id.toggle);
        this.mToggle = compoundButton;
        compoundButton.setOnCheckedChangeListener(this.mCheckListener);
        ToggleSeekBar toggleSeekBar = (ToggleSeekBar) findViewById(C0015R$id.slider);
        this.mSlider = toggleSeekBar;
        toggleSeekBar.setOnSeekBarChangeListener(this.mSeekListener);
        TextView textView = (TextView) findViewById(C0015R$id.label);
        this.mLabel = textView;
        textView.setText(obtainStyledAttributes.getString(R$styleable.ToggleSliderView_text));
        this.mSlider.setAccessibilityLabel(getContentDescription().toString());
        obtainStyledAttributes.recycle();
    }

    public void setMirror(ToggleSliderView toggleSliderView) {
        this.mMirror = toggleSliderView;
        if (toggleSliderView != null) {
            toggleSliderView.setChecked(this.mToggle.isChecked());
            this.mMirror.setMax(this.mSlider.getMax());
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

    public void setEnforcedAdmin(RestrictedLockUtils.EnforcedAdmin enforcedAdmin) {
        boolean z = true;
        this.mToggle.setEnabled(enforcedAdmin == null);
        ToggleSeekBar toggleSeekBar = this.mSlider;
        if (enforcedAdmin != null) {
            z = false;
        }
        toggleSeekBar.setEnabled(z);
        this.mSlider.setEnforcedAdmin(enforcedAdmin);
    }

    public void setOnChangedListener(ToggleSlider.Listener listener) {
        this.mListener = listener;
    }

    public void setChecked(boolean z) {
        this.mToggle.setChecked(z);
    }

    public boolean isChecked() {
        return this.mToggle.isChecked();
    }

    public void setMax(int i) {
        this.mSlider.setMax(i);
        ToggleSliderView toggleSliderView = this.mMirror;
        if (toggleSliderView != null) {
            toggleSliderView.setMax(i);
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
            BrightnessMirrorController brightnessMirrorController = this.mMirrorController;
            if (brightnessMirrorController != null) {
                brightnessMirrorController.setLocation(this);
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
