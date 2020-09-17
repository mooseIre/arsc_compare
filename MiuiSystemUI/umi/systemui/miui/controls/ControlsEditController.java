package com.android.systemui.miui.controls;

import android.view.View;
import com.android.systemui.miui.controlcenter.QSControlCenterPanel;
import com.android.systemui.miui.statusbar.phone.ControlPanelContentView;
import com.android.systemui.plugins.miui.controls.ControlsEditCallback;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.listener.TransitionListener;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;

public class ControlsEditController {
    private boolean isShown = false;
    private IStateStyle mAnim;
    public ControlsEditCallback mCallback = new ControlsEditCallback() {
        public void showEditView() {
            ControlsEditController.this.startAnim(true);
        }

        public void hideEditView() {
            ControlsEditController.this.startAnim(false);
        }
    };
    private ControlPanelContentView mControlPanelContentView;
    /* access modifiers changed from: private */
    public View mControlsEditView;
    /* access modifiers changed from: private */
    public ControlsPluginManager mControlsPluginManager;
    private AnimState mHideAnim;
    private IStateStyle mPanelAnim;
    private AnimState mPanelHideAnim;
    private AnimState mPanelShowAnim;
    private QSControlCenterPanel mQSControlCenterPanel;
    private AnimState mShowAnim;

    public ControlsEditController(ControlsPluginManager controlsPluginManager, ControlPanelContentView controlPanelContentView, QSControlCenterPanel qSControlCenterPanel) {
        this.mControlsPluginManager = controlsPluginManager;
        this.mControlPanelContentView = controlPanelContentView;
        this.mQSControlCenterPanel = qSControlCenterPanel;
    }

    public boolean isShown() {
        return this.isShown;
    }

    public void hideEditPanel() {
        startAnim(false);
    }

    public void addControlsEditView() {
        removeControlsEditView();
        View controlsEditView = this.mControlsPluginManager.getControlsEditView(this.mCallback);
        this.mControlsEditView = controlsEditView;
        if (controlsEditView != null) {
            ControlPanelContentView controlPanelContentView = this.mControlPanelContentView;
            controlPanelContentView.addView(controlsEditView, controlPanelContentView.getChildCount());
            initAnim();
        }
    }

    public void removeControlsEditView() {
        View view = this.mControlsEditView;
        if (view != null) {
            this.mControlPanelContentView.removeView(view);
            this.mControlsEditView = null;
        }
        this.mControlsPluginManager.removeControlsEditView();
    }

    private void initAnim() {
        this.mAnim = Folme.useAt(this.mControlsEditView).state();
        AnimState animState = new AnimState("controls_editor_show");
        animState.add(ViewProperty.ALPHA, 1.0f, new long[0]);
        animState.add(ViewProperty.TRANSLATION_Y, 0, new long[0]);
        this.mShowAnim = animState;
        AnimState animState2 = new AnimState("controls_editor_hide");
        animState2.add(ViewProperty.ALPHA, 0.0f, new long[0]);
        animState2.add(ViewProperty.TRANSLATION_Y, 100, new long[0]);
        this.mHideAnim = animState2;
        this.mPanelAnim = Folme.useAt(this.mQSControlCenterPanel).state();
        AnimState animState3 = new AnimState("qs_control_customizer_show_panel");
        animState3.add(ViewProperty.ALPHA, 1.0f, new long[0]);
        this.mPanelShowAnim = animState3;
        AnimState animState4 = new AnimState("qs_control_customizer_hide_panel");
        animState4.add(ViewProperty.ALPHA, 0.0f, new long[0]);
        this.mPanelHideAnim = animState4;
    }

    /* access modifiers changed from: private */
    public void startAnim(boolean z) {
        if (this.mControlsEditView != null) {
            if (z) {
                this.mControlsPluginManager.showControlsEditView();
                this.mPanelAnim.fromTo(this.mPanelShowAnim, this.mPanelHideAnim, new AnimConfig());
                IStateStyle iStateStyle = this.mAnim;
                AnimState animState = this.mHideAnim;
                AnimState animState2 = this.mShowAnim;
                AnimConfig animConfig = new AnimConfig();
                animConfig.setEase(EaseManager.getStyle(-2, 0.8f, 0.5f));
                animConfig.setDelay(0);
                animConfig.addListeners(new TransitionListener() {
                    public void onBegin(Object obj) {
                        super.onBegin(obj);
                        ControlsEditController.this.mControlsEditView.setVisibility(0);
                    }
                });
                iStateStyle.fromTo(animState, animState2, animConfig);
                this.isShown = true;
            } else if (this.isShown) {
                IStateStyle iStateStyle2 = this.mPanelAnim;
                AnimState animState3 = this.mPanelHideAnim;
                AnimState animState4 = this.mPanelShowAnim;
                AnimConfig animConfig2 = new AnimConfig();
                animConfig2.setDelay(60);
                iStateStyle2.fromTo(animState3, animState4, animConfig2);
                IStateStyle iStateStyle3 = this.mAnim;
                AnimState animState5 = this.mShowAnim;
                AnimState animState6 = this.mHideAnim;
                AnimConfig animConfig3 = new AnimConfig();
                animConfig3.addListeners(new TransitionListener() {
                    public void onComplete(Object obj) {
                        super.onComplete(obj);
                        ControlsEditController.this.mControlsEditView.setVisibility(8);
                        ControlsEditController.this.mControlsPluginManager.hideControlsEditView();
                    }
                });
                iStateStyle3.fromTo(animState5, animState6, animConfig3);
                this.isShown = false;
            }
        }
    }
}
