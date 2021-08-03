package com.android.systemui.controlcenter.phone.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controlcenter.phone.ControlPanelContentView;
import com.android.systemui.controlcenter.qs.tileview.QCBrightnessMirrorController;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.statusbar.policy.MiuiBrightnessController;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlCenterBrightnessView.kt */
public final class ControlCenterBrightnessView extends LinearLayout {
    @NotNull
    private AutoBrightnessView autoBrightness;
    private final MiuiBrightnessController brightnessController;
    private QCBrightnessMirrorController brightnessMirrorController;
    @NotNull
    private QCToggleSliderView brightnessView;
    private boolean listening;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ControlCenterBrightnessView(@NotNull Context context, @Nullable AttributeSet attributeSet, @NotNull BroadcastDispatcher broadcastDispatcher, @NotNull MiuiBrightnessController miuiBrightnessController) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(miuiBrightnessController, "brightnessController");
        this.brightnessController = miuiBrightnessController;
    }

    @NotNull
    public final AutoBrightnessView getAutoBrightness() {
        AutoBrightnessView autoBrightnessView = this.autoBrightness;
        if (autoBrightnessView != null) {
            return autoBrightnessView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("autoBrightness");
        throw null;
    }

    @NotNull
    public final QCToggleSliderView getBrightnessView() {
        QCToggleSliderView qCToggleSliderView = this.brightnessView;
        if (qCToggleSliderView != null) {
            return qCToggleSliderView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("brightnessView");
        throw null;
    }

    public final void setListening(boolean z) {
        if (z != this.listening) {
            this.listening = z;
            AutoBrightnessView autoBrightnessView = this.autoBrightness;
            if (autoBrightnessView != null) {
                autoBrightnessView.handleSetListening(z);
                MiuiBrightnessController miuiBrightnessController = this.brightnessController;
                if (this.listening) {
                    miuiBrightnessController.checkRestrictionAndSetEnabled();
                    miuiBrightnessController.registerCallbacks();
                    return;
                }
                miuiBrightnessController.unregisterCallbacks();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("autoBrightness");
            throw null;
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        View requireViewById = requireViewById(C0015R$id.auto_brightness);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById(R.id.auto_brightness)");
        this.autoBrightness = (AutoBrightnessView) requireViewById;
        View requireViewById2 = requireViewById(C0015R$id.qs_brightness);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById(R.id.qs_brightness)");
        QCToggleSliderView qCToggleSliderView = (QCToggleSliderView) requireViewById2;
        this.brightnessView = qCToggleSliderView;
        MiuiBrightnessController miuiBrightnessController = this.brightnessController;
        if (qCToggleSliderView != null) {
            miuiBrightnessController.setToggleSlider(qCToggleSliderView);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("brightnessView");
            throw null;
        }
    }

    public final void setControlPanelContentView(@NotNull ControlPanelContentView controlPanelContentView) {
        Intrinsics.checkParameterIsNotNull(controlPanelContentView, "panelView");
        QCBrightnessMirrorController qCBrightnessMirrorController = new QCBrightnessMirrorController(controlPanelContentView);
        this.brightnessMirrorController = qCBrightnessMirrorController;
        QCToggleSliderView qCToggleSliderView = this.brightnessView;
        if (qCToggleSliderView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("brightnessView");
            throw null;
        } else if (qCBrightnessMirrorController != null) {
            View findViewById = qCBrightnessMirrorController.getMirror().findViewById(C0015R$id.brightness_slider);
            if (findViewById != null) {
                qCToggleSliderView.setMirror((QCToggleSliderView) findViewById);
                QCToggleSliderView qCToggleSliderView2 = this.brightnessView;
                if (qCToggleSliderView2 != null) {
                    QCBrightnessMirrorController qCBrightnessMirrorController2 = this.brightnessMirrorController;
                    if (qCBrightnessMirrorController2 != null) {
                        qCToggleSliderView2.setMirrorController(qCBrightnessMirrorController2);
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("brightnessMirrorController");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("brightnessView");
                    throw null;
                }
            } else {
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controlcenter.phone.widget.QCToggleSliderView");
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("brightnessMirrorController");
            throw null;
        }
    }

    public final void updateResources() {
        AutoBrightnessView autoBrightnessView = this.autoBrightness;
        if (autoBrightnessView != null) {
            autoBrightnessView.updateResources();
            QCToggleSliderView qCToggleSliderView = this.brightnessView;
            if (qCToggleSliderView != null) {
                qCToggleSliderView.updateResources();
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("brightnessView");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("autoBrightness");
            throw null;
        }
    }

    public final void setHost(@Nullable QSTileHost qSTileHost) {
        AutoBrightnessView autoBrightnessView = this.autoBrightness;
        if (autoBrightnessView != null) {
            autoBrightnessView.setHost(qSTileHost);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("autoBrightness");
            throw null;
        }
    }

    public final void onUserSwitched(int i) {
        AutoBrightnessView autoBrightnessView = this.autoBrightness;
        if (autoBrightnessView != null) {
            autoBrightnessView.onUserSwitched(i);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("autoBrightness");
            throw null;
        }
    }

    public final boolean isMirrorShowing() {
        QCBrightnessMirrorController qCBrightnessMirrorController = this.brightnessMirrorController;
        if (qCBrightnessMirrorController != null) {
            return qCBrightnessMirrorController.isMirrorShowing();
        }
        Intrinsics.throwUninitializedPropertyAccessException("brightnessMirrorController");
        throw null;
    }
}
