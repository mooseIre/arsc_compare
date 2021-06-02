package com.android.systemui.controls.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.service.controls.actions.ControlAction;
import android.widget.EditText;
import com.android.systemui.C0015R$id;
import kotlin.jvm.functions.Function0;

/* compiled from: ChallengeDialogs.kt */
final class ChallengeDialogs$createPinDialog$$inlined$apply$lambda$1 implements DialogInterface.OnClickListener {
    final /* synthetic */ ControlViewHolder $cvh$inlined;
    final /* synthetic */ ControlAction $lastAction$inlined;

    ChallengeDialogs$createPinDialog$$inlined$apply$lambda$1(String str, ControlViewHolder controlViewHolder, ControlAction controlAction, Function0 function0) {
        this.$cvh$inlined = controlViewHolder;
        this.$lastAction$inlined = controlAction;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        if (dialogInterface instanceof Dialog) {
            Dialog dialog = (Dialog) dialogInterface;
            dialog.requireViewById(C0015R$id.controls_pin_input);
            this.$cvh$inlined.action(ChallengeDialogs.access$addChallengeValue(ChallengeDialogs.INSTANCE, this.$lastAction$inlined, ((EditText) dialog.requireViewById(C0015R$id.controls_pin_input)).getText().toString()));
            dialogInterface.dismiss();
        }
    }
}
