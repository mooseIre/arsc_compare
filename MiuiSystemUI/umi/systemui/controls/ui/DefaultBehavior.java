package com.android.systemui.controls.ui;

import android.service.controls.Control;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: DefaultBehavior.kt */
public final class DefaultBehavior implements Behavior {
    @NotNull
    public ControlViewHolder cvh;

    @Override // com.android.systemui.controls.ui.Behavior
    public void initialize(@NotNull ControlViewHolder controlViewHolder) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        this.cvh = controlViewHolder;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void bind(@NotNull ControlWithState controlWithState, int i) {
        CharSequence charSequence;
        Intrinsics.checkParameterIsNotNull(controlWithState, "cws");
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder != null) {
            Control control = controlWithState.getControl();
            if (control == null || (charSequence = control.getStatusText()) == null) {
                charSequence = "";
            }
            ControlViewHolder.setStatusText$default(controlViewHolder, charSequence, false, 2, null);
            ControlViewHolder controlViewHolder2 = this.cvh;
            if (controlViewHolder2 != null) {
                ControlViewHolder.applyRenderInfo$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core$default(controlViewHolder2, false, i, false, 4, null);
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
            throw null;
        }
    }
}
