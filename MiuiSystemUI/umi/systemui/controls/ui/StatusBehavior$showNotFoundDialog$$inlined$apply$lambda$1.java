package com.android.systemui.controls.ui;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.service.controls.Control;

/* access modifiers changed from: package-private */
/* compiled from: StatusBehavior.kt */
public final class StatusBehavior$showNotFoundDialog$$inlined$apply$lambda$1 implements DialogInterface.OnClickListener {
    final /* synthetic */ ControlViewHolder $cvh$inlined;
    final /* synthetic */ ControlWithState $cws$inlined;
    final /* synthetic */ AlertDialog.Builder $this_apply;

    StatusBehavior$showNotFoundDialog$$inlined$apply$lambda$1(AlertDialog.Builder builder, ControlViewHolder controlViewHolder, CharSequence charSequence, ControlWithState controlWithState) {
        this.$this_apply = builder;
        this.$cvh$inlined = controlViewHolder;
        this.$cws$inlined = controlWithState;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        PendingIntent appIntent;
        try {
            Control control = this.$cws$inlined.getControl();
            if (!(control == null || (appIntent = control.getAppIntent()) == null)) {
                appIntent.send();
            }
            this.$this_apply.getContext().sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        } catch (PendingIntent.CanceledException unused) {
            this.$cvh$inlined.setErrorStatus();
        }
        dialogInterface.dismiss();
    }
}
