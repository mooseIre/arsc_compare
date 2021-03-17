package com.android.systemui.controlcenter.policy;

import android.content.DialogInterface;

/* compiled from: SlaveWifiHelper.kt */
final class SlaveWifiHelper$showAlertDialog$1 implements DialogInterface.OnClickListener {
    public static final SlaveWifiHelper$showAlertDialog$1 INSTANCE = new SlaveWifiHelper$showAlertDialog$1();

    SlaveWifiHelper$showAlertDialog$1() {
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
    }
}
