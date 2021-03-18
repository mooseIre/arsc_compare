package com.android.keyguard.injector;

import android.app.AlertDialog;
import android.content.DialogInterface;
import com.android.keyguard.utils.PhoneUtils;

/* access modifiers changed from: package-private */
/* compiled from: KeyguardPanelViewInjector.kt */
public final class KeyguardPanelViewInjector$showSimLockedTipsDialog$1 implements DialogInterface.OnClickListener {
    final /* synthetic */ KeyguardPanelViewInjector this$0;

    KeyguardPanelViewInjector$showSimLockedTipsDialog$1(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        this.this$0 = keyguardPanelViewInjector;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        PhoneUtils.takeEmergencyCallAction(this.this$0.getMContext());
        AlertDialog alertDialog = this.this$0.mSimLockedTipsDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        this.this$0.mSimLockedTipsDialog = null;
    }
}
