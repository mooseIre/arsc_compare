package com.android.systemui.controlcenter.phone.animator;

import com.android.systemui.controlcenter.phone.ControlCenterPanelView;
import kotlin.jvm.internal.Intrinsics;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlCenterPanelAnimator.kt */
public abstract class ControlCenterPanelAnimator {
    @NotNull
    public AnimConfig animConfig;
    @NotNull
    public IStateStyle panelAnim;
    @NotNull
    public AnimState panelHideAnim;
    @NotNull
    public AnimState panelShowAnim;
    @NotNull
    private final ControlCenterPanelView panelView;

    public abstract void animateShowPanel(boolean z);

    public void notifyOrientationChanged() {
    }

    public void notifyTileChanged() {
    }

    public ControlCenterPanelAnimator(@NotNull ControlCenterPanelView controlCenterPanelView) {
        Intrinsics.checkParameterIsNotNull(controlCenterPanelView, "panelView");
        this.panelView = controlCenterPanelView;
    }

    @NotNull
    public final ControlCenterPanelView getPanelView() {
        return this.panelView;
    }

    @NotNull
    public IStateStyle getPanelAnim() {
        IStateStyle iStateStyle = this.panelAnim;
        if (iStateStyle != null) {
            return iStateStyle;
        }
        Intrinsics.throwUninitializedPropertyAccessException("panelAnim");
        throw null;
    }

    public void setPanelAnim(@NotNull IStateStyle iStateStyle) {
        Intrinsics.checkParameterIsNotNull(iStateStyle, "<set-?>");
        this.panelAnim = iStateStyle;
    }

    @NotNull
    public AnimState getPanelShowAnim() {
        AnimState animState = this.panelShowAnim;
        if (animState != null) {
            return animState;
        }
        Intrinsics.throwUninitializedPropertyAccessException("panelShowAnim");
        throw null;
    }

    public void setPanelShowAnim(@NotNull AnimState animState) {
        Intrinsics.checkParameterIsNotNull(animState, "<set-?>");
        this.panelShowAnim = animState;
    }

    @NotNull
    public AnimState getPanelHideAnim() {
        AnimState animState = this.panelHideAnim;
        if (animState != null) {
            return animState;
        }
        Intrinsics.throwUninitializedPropertyAccessException("panelHideAnim");
        throw null;
    }

    public void setPanelHideAnim(@NotNull AnimState animState) {
        Intrinsics.checkParameterIsNotNull(animState, "<set-?>");
        this.panelHideAnim = animState;
    }

    @NotNull
    public AnimConfig getAnimConfig() {
        AnimConfig animConfig2 = this.animConfig;
        if (animConfig2 != null) {
            return animConfig2;
        }
        Intrinsics.throwUninitializedPropertyAccessException("animConfig");
        throw null;
    }

    public void setAnimConfig(@NotNull AnimConfig animConfig2) {
        Intrinsics.checkParameterIsNotNull(animConfig2, "<set-?>");
        this.animConfig = animConfig2;
    }

    public void onFinishInflate() {
        IStateStyle state = Folme.useAt(this.panelView).state();
        Intrinsics.checkExpressionValueIsNotNull(state, "Folme.useAt(panelView).state()");
        setPanelAnim(state);
        AnimState animState = new AnimState("control_center_detail_show");
        animState.add(ViewProperty.ALPHA, 1.0f, new long[0]);
        Intrinsics.checkExpressionValueIsNotNull(animState, "AnimState(\"control_cente…d(ViewProperty.ALPHA, 1f)");
        setPanelShowAnim(animState);
        AnimState animState2 = new AnimState("control_center_detail_hide");
        animState2.add(ViewProperty.ALPHA, 0.0f, new long[0]);
        Intrinsics.checkExpressionValueIsNotNull(animState2, "AnimState(\"control_cente…d(ViewProperty.ALPHA, 0f)");
        setPanelHideAnim(animState2);
        AnimConfig animConfig2 = new AnimConfig();
        animConfig2.setEase(EaseManager.getStyle(0, 300.0f, 0.99f, 0.6666f));
        animConfig2.addListeners(new ControlCenterPanelAnimator$onFinishInflate$1(this));
        Intrinsics.checkExpressionValueIsNotNull(animConfig2, "AnimConfig().setEase(Eas…     }\n                })");
        setAnimConfig(animConfig2);
    }

    public void animateShowPanelWithoutScale(boolean z) {
        getPanelAnim().cancel();
        getPanelAnim().to(z ? getPanelShowAnim() : getPanelHideAnim(), getAnimConfig());
    }

    public void updateOverExpandHeight(float f) {
        boolean z = f == 0.0f;
        this.panelView.getCcContainer().setClipChildren(z);
        this.panelView.getContentContainer().setClipToPadding(z);
    }
}
