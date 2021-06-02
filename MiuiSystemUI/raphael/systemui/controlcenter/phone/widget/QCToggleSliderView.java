package com.android.systemui.controlcenter.phone.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.controlcenter.qs.tileview.QCBrightnessMirrorController;
import com.android.systemui.settings.ToggleSeekBar;
import com.android.systemui.settings.ToggleSlider;

public class QCToggleSliderView extends RelativeLayout implements ToggleSlider {
    private boolean mIgnoreTrackingEvent;
    private int mLastTouchAction;
    private ToggleSlider.Listener mListener;
    private ImageView mLow;
    private QCToggleSliderView mMirror;
    private QCBrightnessMirrorController mMirrorController;
    private final SeekBar.OnSeekBarChangeListener mSeekListener;
    private ToggleSeekBar mSlider;
    private boolean mTracking;

    public QCToggleSliderView(Context context) {
        this(context, null);
    }

    public QCToggleSliderView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public QCToggleSliderView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener() {
            /* class com.android.systemui.controlcenter.phone.widget.QCToggleSliderView.AnonymousClass1 */

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (QCToggleSliderView.this.mListener != null) {
                    ToggleSlider.Listener listener = QCToggleSliderView.this.mListener;
                    QCToggleSliderView qCToggleSliderView = QCToggleSliderView.this;
                    listener.onChanged(qCToggleSliderView, qCToggleSliderView.mTracking, QCToggleSliderView.this.mMirror.isChecked(), i, false);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                if (QCToggleSliderView.this.mLastTouchAction == 1) {
                    QCToggleSliderView.this.mIgnoreTrackingEvent = true;
                    Log.w("QCToggleSliderView", "ignoring onStartTrackingTouch, maybe tap event");
                    return;
                }
                QCToggleSliderView.this.mTracking = true;
                if (QCToggleSliderView.this.mListener != null) {
                    QCToggleSliderView.this.mListener.onStart(seekBar.getProgress());
                    ToggleSlider.Listener listener = QCToggleSliderView.this.mListener;
                    QCToggleSliderView qCToggleSliderView = QCToggleSliderView.this;
                    listener.onChanged(qCToggleSliderView, qCToggleSliderView.mTracking, QCToggleSliderView.this.mMirror.isChecked(), QCToggleSliderView.this.mSlider.getProgress(), false);
                }
                if (QCToggleSliderView.this.mMirrorController != null) {
                    QCToggleSliderView.this.mMirrorController.showMirror();
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (QCToggleSliderView.this.mIgnoreTrackingEvent) {
                    QCToggleSliderView.this.mIgnoreTrackingEvent = false;
                    Log.w("QCToggleSliderView", "ignoring onStopTrackingTouch, maybe tap event");
                    return;
                }
                QCToggleSliderView.this.mTracking = false;
                if (QCToggleSliderView.this.mListener != null) {
                    ToggleSlider.Listener listener = QCToggleSliderView.this.mListener;
                    QCToggleSliderView qCToggleSliderView = QCToggleSliderView.this;
                    listener.onChanged(qCToggleSliderView, qCToggleSliderView.mTracking, QCToggleSliderView.this.mMirror.isChecked(), QCToggleSliderView.this.mSlider.getProgress(), true);
                    QCToggleSliderView.this.mListener.onStop(seekBar.getProgress());
                }
                if (QCToggleSliderView.this.mMirrorController != null) {
                    QCToggleSliderView.this.mMirrorController.hideMirror();
                }
            }
        };
        View.inflate(context, C0017R$layout.qs_control_toggle_slider, this);
        this.mSlider = (ToggleSeekBar) findViewById(C0015R$id.slider);
        this.mLow = (ImageView) findViewById(C0015R$id.low);
        this.mSlider.setOnSeekBarChangeListener(this.mSeekListener);
        this.mSlider.setAccessibilityLabel(getContentDescription().toString());
    }

    public void updateResources() {
        this.mSlider.setProgressDrawable(((RelativeLayout) this).mContext.getDrawable(C0013R$drawable.qs_control_brightness_toggle_progress));
        this.mLow.setImageDrawable(((RelativeLayout) this).mContext.getDrawable(C0013R$drawable.qs_control_low_brightness));
        QCToggleSliderView qCToggleSliderView = this.mMirror;
        if (qCToggleSliderView != null) {
            qCToggleSliderView.updateResources();
        }
    }

    public void setMirror(QCToggleSliderView qCToggleSliderView) {
        this.mMirror = qCToggleSliderView;
        setFocusable(false);
        QCToggleSliderView qCToggleSliderView2 = this.mMirror;
        if (qCToggleSliderView2 != null) {
            qCToggleSliderView2.setMax(this.mSlider.getMax());
            this.mMirror.setValue(this.mSlider.getProgress());
        }
    }

    public void setMirrorController(QCBrightnessMirrorController qCBrightnessMirrorController) {
        this.mMirrorController = qCBrightnessMirrorController;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ToggleSlider.Listener listener = this.mListener;
        if (listener != null) {
            listener.onInit(this);
        }
    }

    @Override // com.android.systemui.settings.ToggleSlider
    public void setOnChangedListener(ToggleSlider.Listener listener) {
        this.mListener = listener;
    }

    @Override // com.android.systemui.settings.ToggleSlider
    public void setMax(int i) {
        if (i != this.mSlider.getMax()) {
            this.mSlider.setMax(i);
            QCToggleSliderView qCToggleSliderView = this.mMirror;
            if (qCToggleSliderView != null) {
                qCToggleSliderView.setMax(i);
            }
        }
    }

    @Override // com.android.systemui.settings.ToggleSlider
    public void setValue(int i) {
        this.mSlider.setProgress(i);
        QCToggleSliderView qCToggleSliderView = this.mMirror;
        if (qCToggleSliderView != null) {
            qCToggleSliderView.setValue(i);
        }
    }

    @Override // com.android.systemui.settings.ToggleSlider
    public int getValue() {
        return this.mSlider.getProgress();
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        this.mLastTouchAction = motionEvent.getActionMasked();
        if (motionEvent.getActionMasked() == 0) {
            this.mIgnoreTrackingEvent = false;
            QCToggleSliderView qCToggleSliderView = this.mMirror;
            if (qCToggleSliderView != null) {
                qCToggleSliderView.setValue(this.mSlider.getProgress());
            }
            QCBrightnessMirrorController qCBrightnessMirrorController = this.mMirrorController;
            if (qCBrightnessMirrorController != null) {
                qCBrightnessMirrorController.setLocation(this);
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
