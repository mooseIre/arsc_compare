package com.android.systemui.controlcenter.phone.animator;

import com.android.systemui.controlcenter.phone.ControlCenterPanelView;
import com.android.systemui.controlcenter.phone.ControlCenterPanelViewController;
import com.android.systemui.controlcenter.utils.FolmeAnimState;
import com.miui.systemui.util.MiuiAnimationUtils;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: PrimaryAnimatorImpl.kt */
public final class PrimaryAnimatorImpl extends ControlCenterPanelAnimator {
    private final ControlCenterPanelViewController controller;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public PrimaryAnimatorImpl(@NotNull ControlCenterPanelView controlCenterPanelView, @NotNull ControlCenterPanelViewController controlCenterPanelViewController) {
        super(controlCenterPanelView);
        Intrinsics.checkParameterIsNotNull(controlCenterPanelView, "panelView");
        Intrinsics.checkParameterIsNotNull(controlCenterPanelViewController, "controller");
        this.controller = controlCenterPanelViewController;
    }

    @Override // com.android.systemui.controlcenter.phone.animator.ControlCenterPanelAnimator
    public void animateShowPanel(boolean z) {
        animateShowPanelWithoutScale(z);
    }

    @Override // com.android.systemui.controlcenter.phone.animator.ControlCenterPanelAnimator
    public void updateOverExpandHeight(float f) {
        super.updateOverExpandHeight(f);
        getPanelView().getCcContainer().setClipChildren(true);
        getPanelView().getContentContainer().setClipToPadding(true);
        if (f == 0.0f) {
            getPanelAnim().to(FolmeAnimState.mSpringBackAnim, FolmeAnimState.mSpringBackConfig);
            return;
        }
        getPanelView().setTranslationY(MiuiAnimationUtils.INSTANCE.afterFriction(f, RangesKt___RangesKt.coerceIn(f, 0.0f, (float) this.controller.getScreenHeight())));
    }
}
