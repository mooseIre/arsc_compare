package com.android.systemui.fsgesture;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import com.android.systemui.C0017R$layout;

public class FsGestureDemoFinalView extends FrameLayout {
    public FsGestureDemoFinalView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FsGestureDemoFinalView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public FsGestureDemoFinalView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(C0017R$layout.fs_gesture_demo_final_view, this);
    }
}
