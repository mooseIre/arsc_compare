package com.android.systemui.controls.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import com.android.systemui.C0015R$id;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: ChallengeDialogs.kt */
public final class ChallengeDialogs$createPinDialog$$inlined$apply$lambda$3 implements DialogInterface.OnShowListener {
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
        challengeDialogs.setInputType(editText, checkBox.isChecked());
        ((CheckBox) this.$this_apply.requireViewById(C0015R$id.controls_pin_use_alpha)).setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.controls.ui.ChallengeDialogs$createPinDialog$$inlined$apply$lambda$3.AnonymousClass1 */

            /*  JADX ERROR: JadxRuntimeException in pass: InlineMethods
                jadx.core.utils.exceptions.JadxRuntimeException: Failed to process method for inline: com.android.systemui.controls.ui.ChallengeDialogs.access$setInputType(com.android.systemui.controls.ui.ChallengeDialogs, android.widget.EditText, boolean):void
                	at jadx.core.dex.visitors.InlineMethods.processInvokeInsn(InlineMethods.java:68)
                	at jadx.core.dex.visitors.InlineMethods.visit(InlineMethods.java:43)
                Caused by: java.lang.ArrayIndexOutOfBoundsException: arraycopy: last source index 3 out of bounds for object array[2]
                	at java.base/java.lang.System.arraycopy(Native Method)
                	at java.base/java.util.ArrayList.shiftTailOverGap(ArrayList.java:778)
                	at java.base/java.util.ArrayList.removeIf(ArrayList.java:1721)
                	at java.base/java.util.ArrayList.removeIf(ArrayList.java:1690)
                	at jadx.core.dex.instructions.args.SSAVar.removeUse(SSAVar.java:125)
                	at jadx.core.utils.InsnRemover.unbindArgUsage(InsnRemover.java:151)
                	at jadx.core.dex.nodes.InsnNode.replaceArg(InsnNode.java:136)
                	at jadx.core.dex.visitors.MarkMethodsForInline.addInlineAttr(MarkMethodsForInline.java:103)
                	at jadx.core.dex.visitors.MarkMethodsForInline.inlineMth(MarkMethodsForInline.java:88)
                	at jadx.core.dex.visitors.MarkMethodsForInline.process(MarkMethodsForInline.java:58)
                	at jadx.core.dex.visitors.InlineMethods.processInvokeInsn(InlineMethods.java:57)
                	... 1 more
                */
            public final void onClick(android.view.View r3) {
                /*
                    r2 = this;
                    com.android.systemui.controls.ui.ChallengeDialogs r3 = com.android.systemui.controls.ui.ChallengeDialogs.INSTANCE
                    android.widget.EditText r0 = r4
                    java.lang.String r1 = "editText"
                    kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r0, r1)
                    android.widget.CheckBox r2 = r0
                    boolean r2 = r2.isChecked()
                    com.android.systemui.controls.ui.ChallengeDialogs.access$setInputType(r3, r0, r2)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.ui.ChallengeDialogs$createPinDialog$$inlined$apply$lambda$3.AnonymousClass1.onClick(android.view.View):void");
            }
        });
        editText.requestFocus();
    }
}
