package com.android.systemui.controls.ui;

import android.content.Intent;
import android.service.controls.Control;
import android.service.controls.actions.CommandAction;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

/* compiled from: ControlActionCoordinatorImpl.kt */
final class ControlActionCoordinatorImpl$touch$1 extends Lambda implements Function0<Unit> {
    final /* synthetic */ Control $control;
    final /* synthetic */ ControlViewHolder $cvh;
    final /* synthetic */ String $templateId;
    final /* synthetic */ ControlActionCoordinatorImpl this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ControlActionCoordinatorImpl$touch$1(ControlActionCoordinatorImpl controlActionCoordinatorImpl, ControlViewHolder controlViewHolder, Control control, String str) {
        super(0);
        this.this$0 = controlActionCoordinatorImpl;
        this.$cvh = controlViewHolder;
        this.$control = control;
        this.$templateId = str;
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        this.$cvh.getLayout().performHapticFeedback(6);
        if (this.$cvh.usePanel()) {
            ControlActionCoordinatorImpl controlActionCoordinatorImpl = this.this$0;
            ControlViewHolder controlViewHolder = this.$cvh;
            Intent intent = this.$control.getAppIntent().getIntent();
            Intrinsics.checkExpressionValueIsNotNull(intent, "control.getAppIntent().getIntent()");
            controlActionCoordinatorImpl.showDialog(controlViewHolder, intent);
            return;
        }
        this.$cvh.action(new CommandAction(this.$templateId));
    }
}
