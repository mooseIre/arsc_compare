package com.android.systemui.controls.ui;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.service.controls.Control;
import android.service.controls.templates.ControlTemplate;
import android.service.controls.templates.TemperatureControlTemplate;
import com.android.systemui.C0015R$id;
import com.android.systemui.controls.ui.ControlViewHolder;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: TemperatureControlBehavior.kt */
public final class TemperatureControlBehavior implements Behavior {
    @NotNull
    public Drawable clipLayer;
    @NotNull
    public Control control;
    @NotNull
    public ControlViewHolder cvh;
    @Nullable
    private Behavior subBehavior;

    @NotNull
    public final Control getControl() {
        Control control2 = this.control;
        if (control2 != null) {
            return control2;
        }
        Intrinsics.throwUninitializedPropertyAccessException("control");
        throw null;
    }

    @NotNull
    public final ControlViewHolder getCvh() {
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder != null) {
            return controlViewHolder;
        }
        Intrinsics.throwUninitializedPropertyAccessException("cvh");
        throw null;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void initialize(@NotNull ControlViewHolder controlViewHolder) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        this.cvh = controlViewHolder;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void bind(@NotNull ControlWithState controlWithState, int i) {
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
                int i2 = 0;
                ControlViewHolder.setStatusText$default(controlViewHolder, statusText, false, 2, null);
                ControlViewHolder controlViewHolder2 = this.cvh;
                if (controlViewHolder2 != null) {
                    Drawable background = controlViewHolder2.getLayout().getBackground();
                    if (background != null) {
                        Drawable findDrawableByLayerId = ((LayerDrawable) background).findDrawableByLayerId(C0015R$id.clip_layer);
                        Intrinsics.checkExpressionValueIsNotNull(findDrawableByLayerId, "ld.findDrawableByLayerId(R.id.clip_layer)");
                        this.clipLayer = findDrawableByLayerId;
                        Control control3 = this.control;
                        if (control3 != null) {
                            TemperatureControlTemplate controlTemplate = control3.getControlTemplate();
                            if (controlTemplate != null) {
                                TemperatureControlTemplate temperatureControlTemplate = controlTemplate;
                                int currentActiveMode = temperatureControlTemplate.getCurrentActiveMode();
                                ControlTemplate template = temperatureControlTemplate.getTemplate();
                                if (Intrinsics.areEqual(template, ControlTemplate.getNoTemplateObject()) || Intrinsics.areEqual(template, ControlTemplate.getErrorTemplate())) {
                                    boolean z = (currentActiveMode == 0 || currentActiveMode == 1) ? false : true;
                                    Drawable drawable = this.clipLayer;
                                    if (drawable != null) {
                                        if (z) {
                                            i2 = 10000;
                                        }
                                        drawable.setLevel(i2);
                                        ControlViewHolder controlViewHolder3 = this.cvh;
                                        if (controlViewHolder3 != null) {
                                            ControlViewHolder.applyRenderInfo$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core$default(controlViewHolder3, z, currentActiveMode, false, 4, null);
                                            ControlViewHolder controlViewHolder4 = this.cvh;
                                            if (controlViewHolder4 != null) {
                                                controlViewHolder4.getLayout().setOnClickListener(new TemperatureControlBehavior$bind$1(this, temperatureControlTemplate));
                                            } else {
                                                Intrinsics.throwUninitializedPropertyAccessException("cvh");
                                                throw null;
                                            }
                                        } else {
                                            Intrinsics.throwUninitializedPropertyAccessException("cvh");
                                            throw null;
                                        }
                                    } else {
                                        Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
                                        throw null;
                                    }
                                } else {
                                    ControlViewHolder controlViewHolder5 = this.cvh;
                                    if (controlViewHolder5 != null) {
                                        Behavior behavior = this.subBehavior;
                                        ControlViewHolder.Companion companion = ControlViewHolder.Companion;
                                        Control control4 = this.control;
                                        if (control4 != null) {
                                            int status = control4.getStatus();
                                            Intrinsics.checkExpressionValueIsNotNull(template, "subTemplate");
                                            Control control5 = this.control;
                                            if (control5 != null) {
                                                this.subBehavior = controlViewHolder5.bindBehavior(behavior, companion.findBehaviorClass(status, template, control5.getDeviceType()), currentActiveMode);
                                            } else {
                                                Intrinsics.throwUninitializedPropertyAccessException("control");
                                                throw null;
                                            }
                                        } else {
                                            Intrinsics.throwUninitializedPropertyAccessException("control");
                                            throw null;
                                        }
                                    } else {
                                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                                        throw null;
                                    }
                                }
                            } else {
                                throw new TypeCastException("null cannot be cast to non-null type android.service.controls.templates.TemperatureControlTemplate");
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("control");
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
            Intrinsics.throwNpe();
            throw null;
        }
    }
}
