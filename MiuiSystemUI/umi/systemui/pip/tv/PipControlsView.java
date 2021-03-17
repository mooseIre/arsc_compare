package com.android.systemui.pip.tv;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;

public class PipControlsView extends LinearLayout {
    public PipControlsView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PipControlsView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public PipControlsView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(C0017R$layout.tv_pip_controls, this);
        setOrientation(0);
        setGravity(49);
    }

    /* access modifiers changed from: package-private */
    public PipControlButtonView getFullButtonView() {
        return (PipControlButtonView) findViewById(C0015R$id.full_button);
    }

    /* access modifiers changed from: package-private */
    public PipControlButtonView getCloseButtonView() {
        return (PipControlButtonView) findViewById(C0015R$id.close_button);
    }

    /* access modifiers changed from: package-private */
    public PipControlButtonView getPlayPauseButtonView() {
        return (PipControlButtonView) findViewById(C0015R$id.play_pause_button);
    }
}
