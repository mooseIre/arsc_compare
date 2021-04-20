package com.android.systemui.controlcenter.phone;

import miuix.animation.listener.TransitionListener;
import miuix.animation.property.FloatProperty;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlCenterPanelViewController.kt */
public final class ControlCenterPanelViewController$toCollapseAnimation$animConfig$1 extends TransitionListener {
    final /* synthetic */ ControlCenterPanelViewController this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ControlCenterPanelViewController$toCollapseAnimation$animConfig$1(ControlCenterPanelViewController controlCenterPanelViewController) {
        this.this$0 = controlCenterPanelViewController;
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onBegin(@Nullable Object obj) {
        this.this$0.animatingToCollapse = true;
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onUpdate(@Nullable Object obj, @Nullable FloatProperty<?> floatProperty, float f, float f2, boolean z) {
        ControlCenterPanelViewController controlCenterPanelViewController = this.this$0;
        controlCenterPanelViewController.setTransRatio((f - ((float) controlCenterPanelViewController.tileLayoutMinHeight)) / ((float) this.this$0.expandThreshold));
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onComplete(@Nullable Object obj) {
        this.this$0.animatingToCollapse = false;
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onCancel(@Nullable Object obj) {
        this.this$0.animatingToCollapse = false;
        this.this$0.calculateTransitionValues();
    }
}
