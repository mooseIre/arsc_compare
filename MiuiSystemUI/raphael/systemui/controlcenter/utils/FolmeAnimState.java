package com.android.systemui.controlcenter.utils;

import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;

public interface FolmeAnimState {
    public static final AnimConfig mAnimConfig;
    public static final AnimState mHideAnim;
    public static final AnimConfig mPanelAnimConfig;
    public static final AnimState mPanelHideAnim;
    public static final AnimState mPanelShowAnim;
    public static final AnimState mShowAnim;
    public static final AnimState mSpringBackAnim;
    public static final AnimConfig mSpringBackConfig;

    static {
        AnimState animState = new AnimState("control_center_detail_show");
        animState.add(ViewProperty.ALPHA, 1.0f, new long[0]);
        mShowAnim = animState;
        AnimState animState2 = new AnimState("control_center_detail_hide");
        animState2.add(ViewProperty.ALPHA, 0.0f, new long[0]);
        mHideAnim = animState2;
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(EaseManager.getStyle(0, 300.0f, 0.99f, 0.6666f));
        mAnimConfig = animConfig;
        AnimState animState3 = new AnimState("control_panel_show");
        animState3.add(ViewProperty.ALPHA, 1.0f, new long[0]);
        animState3.add(ViewProperty.SCALE_X, 1.0f, new long[0]);
        animState3.add(ViewProperty.SCALE_Y, 1.0f, new long[0]);
        mPanelShowAnim = animState3;
        AnimState animState4 = new AnimState("control_panel_hide");
        animState4.add(ViewProperty.ALPHA, 0.0f, new long[0]);
        animState4.add(ViewProperty.SCALE_X, 0.8f, new long[0]);
        animState4.add(ViewProperty.SCALE_Y, 0.8f, new long[0]);
        mPanelHideAnim = animState4;
        AnimConfig animConfig2 = new AnimConfig();
        animConfig2.setEase(EaseManager.getStyle(-2, 0.74f, 0.52f));
        mPanelAnimConfig = animConfig2;
        AnimState animState5 = new AnimState("control_panel_spring_back");
        animState5.add(ViewProperty.TRANSLATION_Y, 0.0f, new long[0]);
        mSpringBackAnim = animState5;
        AnimConfig animConfig3 = new AnimConfig();
        animConfig3.setEase(EaseManager.getStyle(-2, 0.74f, 0.52f));
        mSpringBackConfig = animConfig3;
    }
}
