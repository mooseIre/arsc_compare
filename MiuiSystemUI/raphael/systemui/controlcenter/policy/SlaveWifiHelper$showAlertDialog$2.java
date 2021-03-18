package com.android.systemui.controlcenter.policy;

import android.content.Context;
import android.content.DialogInterface;
import androidx.preference.PreferenceManager;
import com.android.settingslib.wifi.AccessPoint;
import com.android.systemui.statusbar.policy.NetworkController;
import kotlin.TypeCastException;
import miui.app.AlertDialog;

/* compiled from: SlaveWifiHelper.kt */
final class SlaveWifiHelper$showAlertDialog$2 implements DialogInterface.OnClickListener {
    final /* synthetic */ NetworkController.AccessPointController $accessPointController;
    final /* synthetic */ AccessPoint $ap;
    final /* synthetic */ Context $context;
    final /* synthetic */ SlaveWifiHelper this$0;

    SlaveWifiHelper$showAlertDialog$2(SlaveWifiHelper slaveWifiHelper, Context context, NetworkController.AccessPointController accessPointController, AccessPoint accessPoint) {
        this.this$0 = slaveWifiHelper;
        this.$context = context;
        this.$accessPointController = accessPointController;
        this.$ap = accessPoint;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        if (dialogInterface != null) {
            if (((AlertDialog) dialogInterface).isChecked()) {
                PreferenceManager.getDefaultSharedPreferences(this.$context).edit().putBoolean("dual_wifi_switching_not_remind", true).commit();
            }
            SlaveWifiHelper.access$getSlaveWifiUtils$p(this.this$0).disconnectSlaveWifi();
            this.$accessPointController.connect(this.$ap);
            dialogInterface.dismiss();
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type miui.app.AlertDialog");
    }
}
