package com.android.systemui.controls.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import com.android.systemui.C0015R$id;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ChallengeDialogs.kt */
final class ChallengeDialogs$createPinDialog$$inlined$apply$lambda$3 implements DialogInterface.OnShowListener {
    final /* synthetic */ int $instructions$inlined;
    final /* synthetic */ AlertDialog $this_apply;
    final /* synthetic */ boolean $useAlphaNumeric$inlined;

    ChallengeDialogs$createPinDialog$$inlined$apply$lambda$3(AlertDialog alertDialog, int i, boolean z) {
        this.$this_apply = alertDialog;
        this.$instructions$inlined = i;
        this.$useAlphaNumeric$inlined = z;
    }

    public final void onShow(DialogInterface dialogInterface) {
        final EditText editText = (EditText) this.$this_apply.requireViewById(C0015R$id.controls_pin_input);
        editText.setHint(this.$instructions$inlined);
        final CheckBox checkBox = (CheckBox) this.$this_apply.requireViewById(C0015R$id.controls_pin_use_alpha);
        checkBox.setChecked(this.$useAlphaNumeric$inlined);
        ChallengeDialogs challengeDialogs = ChallengeDialogs.INSTANCE;
        Intrinsics.checkExpressionValueIsNotNull(editText, "editText");
        ChallengeDialogs.access$setInputType(challengeDialogs, editText, checkBox.isChecked());
        ((CheckBox) this.$this_apply.requireViewById(C0015R$id.controls_pin_use_alpha)).setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.controls.ui.ChallengeDialogs$createPinDialog$$inlined$apply$lambda$3.AnonymousClass1 */

            public final void onClick(View view) {
                ChallengeDialogs challengeDialogs = ChallengeDialogs.INSTANCE;
                EditText editText = editText;
                Intrinsics.checkExpressionValueIsNotNull(editText, "editText");
                ChallengeDialogs.access$setInputType(challengeDialogs, editText, checkBox.isChecked());
            }
        });
        editText.requestFocus();
    }
}
