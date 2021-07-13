package com.android.systemui.controlcenter.phone;

import miuix.animation.listener.TransitionListener;
import miuix.animation.property.FloatProperty;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlCenterPanelViewController.kt */
public final class ControlCenterPanelViewController$toExpandAnimation$animConfig$1 extends TransitionListener {
    final /* synthetic */ ControlCenterPanelViewController this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ControlCenterPanelViewController$toExpandAnimation$animConfig$1(ControlCenterPanelViewController controlCenterPanelViewController) {
        this.this$0 = controlCenterPanelViewController;
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onUpdate(@Nullable Object obj, @Nullable FloatProperty<?> floatProperty, float f, float f2, boolean z) {
        ControlCenterPanelViewController.access$setAnimatingToCollapse$p(this.this$0, false);
        ControlCenterPanelViewController controlCenterPanelViewController = this.this$0;
        ControlCenterPanelViewController.access$setTransRatio$p(controlCenterPanelViewController, (f - ((float) ControlCenterPanelViewController.access$getTileLayoutMinHeight$p(controlCenterPanelViewController))) / ((float) ControlCenterPanelViewController.access$getExpandThreshold$p(this.this$0)));
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onComplete(@Nullable Object obj) {
        ControlCenterPanelViewController.access$setAnimatingToCollapse$p(this.this$0, false);
        ControlCenterPanelViewController.access$getPanelView$p(this.this$0).getTileLayout().setExpanded(true);
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onCancel(@Nullable Object obj) {
        ControlCenterPanelViewController.access$setAnimatingToCollapse$p(this.this$0, false);
        ControlCenterPanelViewController.access$calculateTransitionValues(this.this$0);
    }
}
