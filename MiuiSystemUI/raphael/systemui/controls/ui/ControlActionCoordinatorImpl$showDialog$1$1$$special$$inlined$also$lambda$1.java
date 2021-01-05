package com.android.systemui.controls.ui;

import android.content.DialogInterface;
import com.android.systemui.controls.ui.ControlActionCoordinatorImpl$showDialog$1;

/* compiled from: ControlActionCoordinatorImpl.kt */
final class ControlActionCoordinatorImpl$showDialog$1$1$$special$$inlined$also$lambda$1 implements DialogInterface.OnDismissListener {
    final /* synthetic */ ControlActionCoordinatorImpl$showDialog$1.AnonymousClass1 this$0;

    ControlActionCoordinatorImpl$showDialog$1$1$$special$$inlined$also$lambda$1(ControlActionCoordinatorImpl$showDialog$1.AnonymousClass1 r1) {
        this.this$0 = r1;
    }

    public final void onDismiss(DialogInterface dialogInterface) {
        this.this$0.this$0.this$0.dialog = null;
    }
}
