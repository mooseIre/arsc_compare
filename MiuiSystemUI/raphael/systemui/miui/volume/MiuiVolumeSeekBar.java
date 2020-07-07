package com.android.systemui.miui.volume;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.systemui.miui.widget.RelativeSeekBarInjector;
import miui.widget.VerticalSeekBar;

public class MiuiVolumeSeekBar extends VerticalSeekBar {
    private RelativeSeekBarInjector mInjector = new RelativeSeekBarInjector(this, true);

    public boolean hasOverlappingRendering() {
        return false;
    }

    public MiuiVolumeSeekBar(Context context) {
        super(context);
    }

    public MiuiVolumeSeekBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public MiuiVolumeSeekBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        this.mInjector.transformTouchEvent(motionEvent);
        return super.onTouchEvent(motionEvent);
    }
}
