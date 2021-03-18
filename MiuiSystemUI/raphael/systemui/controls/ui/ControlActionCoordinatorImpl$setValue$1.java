package com.android.systemui.controls.ui;

import android.service.controls.actions.FloatAction;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: ControlActionCoordinatorImpl.kt */
final class ControlActionCoordinatorImpl$setValue$1 extends Lambda implements Function0<Unit> {
    final /* synthetic */ ControlViewHolder $cvh;
    final /* synthetic */ float $newValue;
    final /* synthetic */ String $templateId;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ControlActionCoordinatorImpl$setValue$1(ControlViewHolder controlViewHolder, String str, float f) {
        super(0);
        this.$cvh = controlViewHolder;
        this.$templateId = str;
        this.$newValue = f;
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        this.$cvh.action(new FloatAction(this.$templateId, this.$newValue));
    }
}
