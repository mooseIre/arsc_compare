package com.android.systemui.controlcenter.phone.animator;

import android.graphics.Paint;
import miuix.animation.listener.TransitionListener;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlCenterPanelAnimator.kt */
public final class ControlCenterPanelAnimator$onFinishInflate$1 extends TransitionListener {
    final /* synthetic */ ControlCenterPanelAnimator this$0;

    ControlCenterPanelAnimator$onFinishInflate$1(ControlCenterPanelAnimator controlCenterPanelAnimator) {
        this.this$0 = controlCenterPanelAnimator;
    }

    public void onBegin(@Nullable Object obj) {
        super.onBegin(obj);
        this.this$0.getPanelView().setLayerType(2, (Paint) null);
    }

    public void onComplete(@Nullable Object obj) {
        super.onComplete(obj);
        this.this$0.getPanelView().setLayerType(0, (Paint) null);
    }
}
