package com.android.systemui.controls.ui;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.service.controls.Control;
import android.service.controls.templates.ControlTemplate;
import android.service.controls.templates.TemperatureControlTemplate;
import android.service.controls.templates.ToggleTemplate;
import android.util.Log;
import com.android.systemui.C0015R$id;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ToggleBehavior.kt */
public final class ToggleBehavior implements Behavior {
    @NotNull
    public Drawable clipLayer;
    @NotNull
    public Control control;
    @NotNull
    public ControlViewHolder cvh;
    @NotNull
    public ToggleTemplate template;

    @NotNull
    public final ToggleTemplate getTemplate() {
        ToggleTemplate toggleTemplate = this.template;
        if (toggleTemplate != null) {
            return toggleTemplate;
        }
        Intrinsics.throwUninitializedPropertyAccessException("template");
        throw null;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void initialize(@NotNull ControlViewHolder controlViewHolder) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        this.cvh = controlViewHolder;
        controlViewHolder.getLayout().setOnClickListener(new ToggleBehavior$initialize$1(this, controlViewHolder));
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void bind(@NotNull ControlWithState controlWithState, int i) {
        ToggleTemplate toggleTemplate;
        Intrinsics.checkParameterIsNotNull(controlWithState, "cws");
        Control control2 = controlWithState.getControl();
        if (control2 != null) {
            this.control = control2;
            ControlViewHolder controlViewHolder = this.cvh;
            if (controlViewHolder == null) {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
                throw null;
            } else if (control2 != null) {
                CharSequence statusText = control2.getStatusText();
                Intrinsics.checkExpressionValueIsNotNull(statusText, "control.getStatusText()");
                ControlViewHolder.setStatusText$default(controlViewHolder, statusText, false, 2, null);
                Control control3 = this.control;
                if (control3 != null) {
                    TemperatureControlTemplate controlTemplate = control3.getControlTemplate();
                    if (controlTemplate instanceof ToggleTemplate) {
                        toggleTemplate = (ToggleTemplate) controlTemplate;
                    } else if (controlTemplate instanceof TemperatureControlTemplate) {
                        ControlTemplate template2 = controlTemplate.getTemplate();
                        if (template2 != null) {
                            toggleTemplate = (ToggleTemplate) template2;
                        } else {
                            throw new TypeCastException("null cannot be cast to non-null type android.service.controls.templates.ToggleTemplate");
                        }
                    } else {
                        Log.e("ControlsUiController", "Unsupported template type: " + controlTemplate);
                        return;
                    }
                    this.template = toggleTemplate;
                    ControlViewHolder controlViewHolder2 = this.cvh;
                    if (controlViewHolder2 != null) {
                        Drawable background = controlViewHolder2.getLayout().getBackground();
                        if (background != null) {
                            Drawable findDrawableByLayerId = ((LayerDrawable) background).findDrawableByLayerId(C0015R$id.clip_layer);
                            Intrinsics.checkExpressionValueIsNotNull(findDrawableByLayerId, "ld.findDrawableByLayerId(R.id.clip_layer)");
                            this.clipLayer = findDrawableByLayerId;
                            if (findDrawableByLayerId != null) {
                                findDrawableByLayerId.setLevel(10000);
                                ToggleTemplate toggleTemplate2 = this.template;
                                if (toggleTemplate2 != null) {
                                    boolean isChecked = toggleTemplate2.isChecked();
                                    ControlViewHolder controlViewHolder3 = this.cvh;
                                    if (controlViewHolder3 != null) {
                                        ControlViewHolder.applyRenderInfo$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core$default(controlViewHolder3, isChecked, i, false, 4, null);
                                    } else {
                                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                                        throw null;
                                    }
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("template");
                                    throw null;
                                }
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
                                throw null;
                            }
                        } else {
                            throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.LayerDrawable");
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("control");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("control");
                throw null;
            }
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }
}
