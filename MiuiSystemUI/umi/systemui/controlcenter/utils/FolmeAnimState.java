package com.android.systemui.controlcenter.utils;

import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;

public interface FolmeAnimState {
    public static final AnimConfig mPanelAnimConfig;
    public static final AnimState mPanelHideAnim;
    public static final AnimState mPanelShowAnim;
    public static final AnimState mSpringBackAnim;
    public static final AnimConfig mSpringBackConfig;

    static {
        new AnimState("control_center_detail_show").add(ViewProperty.ALPHA, 1.0f, new long[0]);
        new AnimState("control_center_detail_hide").add(ViewProperty.ALPHA, 0.0f, new long[0]);
        new AnimConfig().setEase(EaseManager.getStyle(0, 300.0f, 0.99f, 0.6666f));
        AnimState animState = new AnimState("control_panel_show");
        animState.add(ViewProperty.ALPHA, 1.0f, new long[0]);
        animState.add(ViewProperty.SCALE_X, 1.0f, new long[0]);
        animState.add(ViewProperty.SCALE_Y, 1.0f, new long[0]);
        mPanelShowAnim = animState;
        AnimState animState2 = new AnimState("control_panel_hide");
        animState2.add(ViewProperty.ALPHA, 0.0f, new long[0]);
        animState2.add(ViewProperty.SCALE_X, 0.8f, new long[0]);
        animState2.add(ViewProperty.SCALE_Y, 0.8f, new long[0]);
        mPanelHideAnim = animState2;
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(EaseManager.getStyle(-2, 0.74f, 0.52f));
        mPanelAnimConfig = animConfig;
        AnimState animState3 = new AnimState("control_panel_spring_back");
        animState3.add(ViewProperty.TRANSLATION_Y, 0.0f, new long[0]);
        mSpringBackAnim = animState3;
        AnimConfig animConfig2 = new AnimConfig();
        animConfig2.setEase(EaseManager.getStyle(-2, 0.74f, 0.52f));
        mSpringBackConfig = animConfig2;
    }
}
