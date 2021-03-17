package com.android.systemui.controls.ui;

import android.service.controls.actions.BooleanAction;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: ControlActionCoordinatorImpl.kt */
final class ControlActionCoordinatorImpl$toggle$1 extends Lambda implements Function0<Unit> {
    final /* synthetic */ ControlViewHolder $cvh;
    final /* synthetic */ boolean $isChecked;
    final /* synthetic */ String $templateId;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ControlActionCoordinatorImpl$toggle$1(ControlViewHolder controlViewHolder, String str, boolean z) {
        super(0);
        this.$cvh = controlViewHolder;
        this.$templateId = str;
        this.$isChecked = z;
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        this.$cvh.getLayout().performHapticFeedback(6);
        this.$cvh.action(new BooleanAction(this.$templateId, !this.$isChecked));
    }
}
