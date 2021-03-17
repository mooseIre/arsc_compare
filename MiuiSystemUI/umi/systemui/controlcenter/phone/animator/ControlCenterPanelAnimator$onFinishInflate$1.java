package com.android.systemui.controlcenter.phone.animator;

import miuix.animation.listener.TransitionListener;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlCenterPanelAnimator.kt */
public final class ControlCenterPanelAnimator$onFinishInflate$1 extends TransitionListener {
    final /* synthetic */ ControlCenterPanelAnimator this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ControlCenterPanelAnimator$onFinishInflate$1(ControlCenterPanelAnimator controlCenterPanelAnimator) {
        this.this$0 = controlCenterPanelAnimator;
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onBegin(@Nullable Object obj) {
        super.onBegin(obj);
        this.this$0.getPanelView().setLayerType(2, null);
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onComplete(@Nullable Object obj) {
        super.onComplete(obj);
        this.this$0.getPanelView().setLayerType(0, null);
    }
}
