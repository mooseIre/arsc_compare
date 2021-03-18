package com.android.systemui.controls.ui;

import android.content.DialogInterface;

/* access modifiers changed from: package-private */
/* compiled from: StatusBehavior.kt */
public final class StatusBehavior$showNotFoundDialog$builder$1$2 implements DialogInterface.OnClickListener {
    public static final StatusBehavior$showNotFoundDialog$builder$1$2 INSTANCE = new StatusBehavior$showNotFoundDialog$builder$1$2();

    StatusBehavior$showNotFoundDialog$builder$1$2() {
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.cancel();
    }
}
