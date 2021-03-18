package com.android.systemui.controls.ui;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.service.controls.Control;
import android.service.controls.templates.ControlTemplate;
import com.android.systemui.C0015R$id;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: TouchBehavior.kt */
public final class TouchBehavior implements Behavior {
    @NotNull
    public Drawable clipLayer;
    @NotNull
    public Control control;
    @NotNull
    public ControlViewHolder cvh;
    private int lastColorOffset;
    private boolean statelessTouch;
    @NotNull
    public ControlTemplate template;

    @NotNull
    public final ControlTemplate getTemplate() {
        ControlTemplate controlTemplate = this.template;
        if (controlTemplate != null) {
            return controlTemplate;
        }
        Intrinsics.throwUninitializedPropertyAccessException("template");
        throw null;
    }

    @NotNull
    public final Control getControl() {
        Control control2 = this.control;
        if (control2 != null) {
            return control2;
        }
        Intrinsics.throwUninitializedPropertyAccessException("control");
        throw null;
    }

    /* access modifiers changed from: private */
    public final boolean getEnabled() {
        return this.lastColorOffset > 0 || this.statelessTouch;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void initialize(@NotNull ControlViewHolder controlViewHolder) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        this.cvh = controlViewHolder;
        controlViewHolder.getLayout().setOnClickListener(new TouchBehavior$initialize$1(this, controlViewHolder));
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void bind(@NotNull ControlWithState controlWithState, int i) {
        Intrinsics.checkParameterIsNotNull(controlWithState, "cws");
        Control control2 = controlWithState.getControl();
        if (control2 != null) {
            this.control = control2;
            this.lastColorOffset = i;
            ControlViewHolder controlViewHolder = this.cvh;
            if (controlViewHolder == null) {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
                throw null;
            } else if (control2 != null) {
                CharSequence statusText = control2.getStatusText();
                Intrinsics.checkExpressionValueIsNotNull(statusText, "control.getStatusText()");
                int i2 = 0;
                ControlViewHolder.setStatusText$default(controlViewHolder, statusText, false, 2, null);
                Control control3 = this.control;
                if (control3 != null) {
                    ControlTemplate controlTemplate = control3.getControlTemplate();
                    Intrinsics.checkExpressionValueIsNotNull(controlTemplate, "control.getControlTemplate()");
                    this.template = controlTemplate;
                    ControlViewHolder controlViewHolder2 = this.cvh;
                    if (controlViewHolder2 != null) {
                        Drawable background = controlViewHolder2.getLayout().getBackground();
                        if (background != null) {
                            Drawable findDrawableByLayerId = ((LayerDrawable) background).findDrawableByLayerId(C0015R$id.clip_layer);
                            Intrinsics.checkExpressionValueIsNotNull(findDrawableByLayerId, "ld.findDrawableByLayerId(R.id.clip_layer)");
                            this.clipLayer = findDrawableByLayerId;
                            if (findDrawableByLayerId != null) {
                                if (getEnabled()) {
                                    i2 = 10000;
                                }
                                findDrawableByLayerId.setLevel(i2);
                                ControlViewHolder controlViewHolder3 = this.cvh;
                                if (controlViewHolder3 != null) {
                                    ControlViewHolder.applyRenderInfo$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core$default(controlViewHolder3, getEnabled(), i, false, 4, null);
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("cvh");
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
