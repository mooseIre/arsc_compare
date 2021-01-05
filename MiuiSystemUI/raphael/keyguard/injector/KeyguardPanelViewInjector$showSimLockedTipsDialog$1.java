package com.android.keyguard.injector;

import android.app.AlertDialog;
import android.content.DialogInterface;
import com.android.keyguard.utils.PhoneUtils;

/* compiled from: KeyguardPanelViewInjector.kt */
final class KeyguardPanelViewInjector$showSimLockedTipsDialog$1 implements DialogInterface.OnClickListener {
    final /* synthetic */ KeyguardPanelViewInjector this$0;

    KeyguardPanelViewInjector$showSimLockedTipsDialog$1(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        this.this$0 = keyguardPanelViewInjector;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        PhoneUtils.takeEmergencyCallAction(this.this$0.getMContext());
        AlertDialog access$getMSimLockedTipsDialog$p = this.this$0.mSimLockedTipsDialog;
        if (access$getMSimLockedTipsDialog$p != null) {
            access$getMSimLockedTipsDialog$p.dismiss();
        }
        this.this$0.mSimLockedTipsDialog = null;
    }
}
