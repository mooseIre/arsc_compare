package com.android.systemui.controls.ui;

import android.content.DialogInterface;
import android.service.controls.actions.ControlAction;
import kotlin.jvm.functions.Function0;

/* access modifiers changed from: package-private */
/* compiled from: ChallengeDialogs.kt */
public final class ChallengeDialogs$createConfirmationDialog$$inlined$apply$lambda$1 implements DialogInterface.OnClickListener {
    final /* synthetic */ ControlViewHolder $cvh$inlined;
    final /* synthetic */ ControlAction $lastAction$inlined;

    ChallengeDialogs$createConfirmationDialog$$inlined$apply$lambda$1(ControlViewHolder controlViewHolder, ControlAction controlAction, Function0 function0) {
        this.$cvh$inlined = controlViewHolder;
        this.$lastAction$inlined = controlAction;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        this.$cvh$inlined.action(ChallengeDialogs.access$addChallengeValue(ChallengeDialogs.INSTANCE, this.$lastAction$inlined, "true"));
        dialogInterface.dismiss();
    }
}
