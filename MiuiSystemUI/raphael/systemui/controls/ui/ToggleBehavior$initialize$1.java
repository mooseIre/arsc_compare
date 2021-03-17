package com.android.systemui.controls.ui;

import android.view.View;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ToggleBehavior.kt */
final class ToggleBehavior$initialize$1 implements View.OnClickListener {
    final /* synthetic */ ControlViewHolder $cvh;
    final /* synthetic */ ToggleBehavior this$0;

    ToggleBehavior$initialize$1(ToggleBehavior toggleBehavior, ControlViewHolder controlViewHolder) {
        this.this$0 = toggleBehavior;
        this.$cvh = controlViewHolder;
    }

    public final void onClick(View view) {
        ControlActionCoordinator controlActionCoordinator = this.$cvh.getControlActionCoordinator();
        ControlViewHolder controlViewHolder = this.$cvh;
        String templateId = this.this$0.getTemplate().getTemplateId();
        Intrinsics.checkExpressionValueIsNotNull(templateId, "template.getTemplateId()");
        controlActionCoordinator.toggle(controlViewHolder, templateId, this.this$0.getTemplate().isChecked());
    }
}
