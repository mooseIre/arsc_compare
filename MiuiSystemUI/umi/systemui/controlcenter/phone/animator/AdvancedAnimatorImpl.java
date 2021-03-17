package com.android.systemui.controlcenter.phone.animator;

import android.view.View;
import android.widget.ImageView;
import com.android.systemui.C0015R$id;
import com.android.systemui.controlcenter.phone.ControlCenterPanelView;
import com.android.systemui.controlcenter.phone.ControlCenterPanelViewController;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.utils.ControlCenterUtils;
import com.android.systemui.controlcenter.utils.FolmeAnimState;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;
import org.jetbrains.annotations.NotNull;

/* compiled from: AdvancedAnimatorImpl.kt */
public final class AdvancedAnimatorImpl extends ControlCenterPanelAnimator {
    private final ControlPanelController ccController;
    private final ControlCenterPanelViewController controller;
    private int footerPanelBaseIndex;
    private final ArrayList<View> mTransViews = new ArrayList<>();
    private final ArrayList<View> mViews = new ArrayList<>();
    private int overFlingLines;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public AdvancedAnimatorImpl(@NotNull ControlCenterPanelView controlCenterPanelView, @NotNull ControlCenterPanelViewController controlCenterPanelViewController, @NotNull ControlPanelController controlPanelController) {
        super(controlCenterPanelView);
        Intrinsics.checkParameterIsNotNull(controlCenterPanelView, "panelView");
        Intrinsics.checkParameterIsNotNull(controlCenterPanelViewController, "controller");
        Intrinsics.checkParameterIsNotNull(controlPanelController, "ccController");
        this.controller = controlCenterPanelViewController;
        this.ccController = controlPanelController;
    }

    @Override // com.android.systemui.controlcenter.phone.animator.ControlCenterPanelAnimator
    public void onFinishInflate() {
        super.onFinishInflate();
        AnimState animState = new AnimState("control_panel_show");
        animState.add(ViewProperty.ALPHA, 1.0f, new long[0]);
        animState.add(ViewProperty.SCALE_X, 1.0f, new long[0]);
        animState.add(ViewProperty.SCALE_Y, 1.0f, new long[0]);
        Intrinsics.checkExpressionValueIsNotNull(animState, "AnimState(\"control_panel…ViewProperty.SCALE_Y, 1f)");
        AnimState animState2 = new AnimState("control_panel_hide");
        animState2.add(ViewProperty.ALPHA, 0.0f, new long[0]);
        animState2.add(ViewProperty.SCALE_X, 0.8f, new long[0]);
        animState2.add(ViewProperty.SCALE_Y, 0.8f, new long[0]);
        Intrinsics.checkExpressionValueIsNotNull(animState2, "AnimState(\"control_panel…ewProperty.SCALE_Y, 0.8f)");
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(EaseManager.getStyle(-2, 300.0f, 0.99f, 0.6666f));
        Intrinsics.checkExpressionValueIsNotNull(animConfig, "AnimConfig().setEase(Eas…Y, 300f, 0.99f, 0.6666f))");
    }

    private final void updateViews() {
        int i = 5;
        this.overFlingLines = (this.controller.isPortrait() ? 7 : 5) + getPanelView().getTileLayout().getShowLines();
        if (!this.controller.isPortrait()) {
            i = 4;
        } else if (!this.controller.isSuperPowerMode()) {
            i = 6;
        }
        this.footerPanelBaseIndex = i;
        this.mViews.clear();
        this.mTransViews.clear();
        addAnimateView(getPanelView().getHeader().findViewById(C0015R$id.carrier_text), 0);
        addAnimateView(getPanelView().getHeader().findViewById(C0015R$id.system_icon_area), 0);
        if (!this.controller.isSuperPowerMode()) {
            View shortCut = getPanelView().getHeader().getShortCut();
            addAnimateView(shortCut, 1);
            Folme.useAt(shortCut).state().setTo(FolmeAnimState.mPanelShowAnim);
        }
        addAnimateView(getPanelView().getHeader().getBigTime(), 1);
        addAnimateView(getPanelView().getHeader().getDateTime(), 1);
        addAnimateView(getPanelView().getBigTileLayout().getTileView0(), 2);
        addAnimateView(getPanelView().getBigTileLayout().getBigTile1(), 2);
        addAnimateView(getPanelView().getBigTileLayout().getBigTile2(), 3);
        addAnimateView(getPanelView().getBigTileLayout().getBigTile3(), 3);
        addAnimateView(getPanelView().getBrightnessView().getAutoBrightness(), this.footerPanelBaseIndex);
        addAnimateView(getPanelView().getBrightnessView().getBrightnessView(), this.footerPanelBaseIndex);
        addAnimateView(getPanelView().getFooter().getSettingsFooter().getFooterIcon(), this.footerPanelBaseIndex);
        addAnimateView(getPanelView().getFooter().getSettingsFooter().getFooterText(), this.footerPanelBaseIndex);
        if (this.controller.isPortrait() && !this.controller.isSuperPowerMode()) {
            View editTile = getPanelView().getHeader().getEditTile();
            addAnimateView(editTile, 1);
            Folme.useAt(editTile).state().setTo(FolmeAnimState.mPanelShowAnim);
            Folme.useAt(getPanelView().getFooter().getIndicator()).state().setTo(FolmeAnimState.mPanelShowAnim);
        }
        if (this.controller.isPortrait()) {
            addTransAnimateView(getPanelView().getHeader().getPanelHeader(), 0);
            addTransAnimateView(getPanelView().getHeader().getTilesHeader(), 1);
            addTransAnimateView(getPanelView().getBigTileLayout().getTileView0(), 2);
            addTransAnimateView(getPanelView().getBigTileLayout().getBigTile1(), 2);
            addTransAnimateView(getPanelView().getBigTileLayout().getBigTile2(), 3);
            addTransAnimateView(getPanelView().getBigTileLayout().getBigTile3(), 3);
            addTransAnimateView(getPanelView().getBrightnessView().getAutoBrightness(), this.overFlingLines - 3);
            addTransAnimateView(getPanelView().getBrightnessView().getBrightnessView(), this.overFlingLines - 3);
            addTransAnimateView(getPanelView().getFooter().getSettingsFooter().getFooterText(), this.footerPanelBaseIndex);
            addTransAnimateView(getPanelView().getFooter().getSettingsFooter().getFooterIcon(), this.footerPanelBaseIndex);
        } else {
            addTransAnimateView(getPanelView(), 0);
        }
        Iterator<View> it = this.mTransViews.iterator();
        while (it.hasNext()) {
            Folme.useAt(it.next());
        }
    }

    private final void addAnimateView(View view, int i) {
        if (view != null && !this.mViews.contains(view)) {
            view.setTag(C0015R$id.tag_control_center, Integer.valueOf(i));
            this.mViews.add(view);
        }
    }

    private final void addTransAnimateView(View view, int i) {
        if (view != null && !this.mTransViews.contains(view)) {
            view.setTag(C0015R$id.tag_control_center_trans, Integer.valueOf(i));
            this.mTransViews.add(view);
        }
    }

    @Override // com.android.systemui.controlcenter.phone.animator.ControlCenterPanelAnimator
    public void notifyOrientationChanged() {
        getPanelView().getTileLayout().setBaseLineIdx(this.controller.isPortrait() ? 4 : 0);
        updateViews();
    }

    @Override // com.android.systemui.controlcenter.phone.animator.ControlCenterPanelAnimator
    public void animateShowPanelWithoutScale(boolean z) {
        getPanelAnim().cancel();
        getPanelAnim().to(z ? getPanelShowAnim() : getPanelHideAnim(), getAnimConfig());
    }

    @Override // com.android.systemui.controlcenter.phone.animator.ControlCenterPanelAnimator
    public void animateShowPanel(boolean z) {
        Iterator<T> it = this.mViews.iterator();
        while (it.hasNext()) {
            animateShow(it.next(), z);
        }
        View[] visAnimViews = getPanelView().getTileLayout().getVisAnimViews();
        Intrinsics.checkExpressionValueIsNotNull(visAnimViews, "panelView.tileLayout.visAnimViews");
        for (View view : visAnimViews) {
            Intrinsics.checkExpressionValueIsNotNull(view, "it");
            animateShow(view, z);
        }
        animateExtraViewShow(z);
    }

    private final void animateShow(View view, boolean z) {
        IStateStyle state = Folme.useAt(view).state();
        state.cancel();
        state.to(z ? FolmeAnimState.mPanelShowAnim : FolmeAnimState.mPanelHideAnim, FolmeAnimState.mPanelAnimConfig);
    }

    private final void resetAnimViewState() {
        if (this.ccController.isCCFullyCollapsed()) {
            View[] visAnimViews = getPanelView().getTileLayout().getVisAnimViews();
            Intrinsics.checkExpressionValueIsNotNull(visAnimViews, "panelView.tileLayout.visAnimViews");
            for (View view : visAnimViews) {
                Intrinsics.checkExpressionValueIsNotNull(view, "it");
                resetShowAnim(view);
            }
            Iterator<T> it = this.mViews.iterator();
            while (it.hasNext()) {
                resetShowAnim(it.next());
            }
        }
    }

    private final void resetShowAnim(View view) {
        Folme.useAt(view).state().setTo(FolmeAnimState.mPanelHideAnim);
    }

    @Override // com.android.systemui.controlcenter.phone.animator.ControlCenterPanelAnimator
    public void notifyTileChanged() {
        resetAnimViewState();
    }

    private final void animateExtraViewShow(boolean z) {
        if (!this.controller.isSuperPowerMode()) {
            AnimState animState = new AnimState("control_panel_show");
            animState.add(ViewProperty.ALPHA, 1.0f, new long[0]);
            AnimState animState2 = new AnimState("control_panel_hide");
            animState2.add(ViewProperty.ALPHA, 0.0f, new long[0]);
            AnimConfig animConfig = new AnimConfig();
            animConfig.setEase(EaseManager.getStyle(-2, 0.99f, 0.2f));
            if (z) {
                Folme.useAt(getPanelView().getSmartHomeContainer()).state().fromTo(animState2, animState, animConfig);
            } else {
                Folme.useAt(getPanelView().getSmartHomeContainer()).state().fromTo(animState, animState2, animConfig);
            }
            animateFooterIndicatorShow(z);
        }
    }

    private final void animateFooterIndicatorShow(boolean z) {
        if (this.controller.isPortrait()) {
            AnimState animState = new AnimState("footer_indicator_show");
            animState.add(ViewProperty.ALPHA, 1.0f, new long[0]);
            AnimState animState2 = new AnimState("footer_indicator_show");
            animState2.add(ViewProperty.ALPHA, 0.0f, new long[0]);
            if (z) {
                Folme.useAt(getPanelView().getFooter().getIndicator()).state().fromTo(animState2, animState, getAnimConfig());
                return;
            }
            Folme.useAt(getPanelView().getFooter().getIndicator()).state().to(animState2, getAnimConfig());
        }
    }

    @Override // com.android.systemui.controlcenter.phone.animator.ControlCenterPanelAnimator
    public void updateOverExpandHeight(float f) {
        super.updateOverExpandHeight(f);
        if (this.controller.isPortrait()) {
            if (f == 0.0f) {
                getPanelView().getTileLayout().updateTransHeight(this.mTransViews, f, this.controller.getScreenHeight(), this.overFlingLines);
                Folme.useAt(getPanelView().getSmartHomeContainer()).state().to(FolmeAnimState.mSpringBackAnim, FolmeAnimState.mSpringBackConfig);
                if (!this.controller.isSuperPowerMode()) {
                    Folme.useAt(getPanelView().getFooter().getIndicator()).state().to(FolmeAnimState.mSpringBackAnim, FolmeAnimState.mSpringBackConfig);
                    return;
                }
                return;
            }
            float screenHeight = (float) this.controller.getScreenHeight();
            float f2 = RangesKt___RangesKt.coerceIn(f, 0.0f, screenHeight);
            for (T t : this.mTransViews) {
                Object tag = t.getTag(C0015R$id.tag_control_center_trans);
                if (tag != null) {
                    t.setTranslationY(ControlCenterUtils.getTranslationY(((Integer) tag).intValue(), this.overFlingLines, f2, screenHeight));
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
                }
            }
            ImageView indicator = getPanelView().getFooter().getIndicator();
            int i = this.overFlingLines;
            indicator.setTranslationY(ControlCenterUtils.getTranslationY(i - 1, i, f2, screenHeight));
            int i2 = this.overFlingLines;
            getPanelView().getSmartHomeContainer().setTranslationY(ControlCenterUtils.getTranslationY(i2 - 2, i2, f2, screenHeight));
            getPanelView().getTileLayout().updateTransHeight(null, f, this.controller.getScreenHeight(), this.overFlingLines);
        }
    }
}
