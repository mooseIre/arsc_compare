package com.android.systemui.wirelesscharge;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.systemui.plugins.R;
import miui.app.Activity;
import miui.app.AlertDialog;

public class WirelessChargingWarningActivity extends Activity {
    private DialogInterface.OnClickListener onClickListener;
    private DialogInterface.OnDismissListener onDismissListener;

    public void onCreate(Bundle bundle) {
        String str;
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        getWindow().getDecorView().setAlpha(0.0f);
        int intExtra = getIntent().getIntExtra("plugstatus", -1);
        this.onClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                WirelessChargingWarningActivity.this.finish();
            }
        };
        this.onDismissListener = new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                WirelessChargingWarningActivity.this.finish();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Dialog_Alert);
        if (intExtra == 4) {
            str = getString(R.string.wireless_charging_connected_message);
        } else {
            str = getString(R.string.wireless_charging_low_battery_level_message);
        }
        builder.setMessage((CharSequence) str);
        builder.setCancelable(true);
        builder.setPositiveButton((CharSequence) getString(R.string.wireless_charging_ok), this.onClickListener);
        builder.setOnDismissListener(this.onDismissListener);
        builder.create().show();
        sendUpdateStatusBroadCast();
    }

    private void sendUpdateStatusBroadCast() {
        Intent intent = new Intent("miui.intent.action.ACTION_WIRELESS_CHARGING");
        intent.addFlags(822083584);
        intent.putExtra("miui.intent.extra.WIRELESS_CHARGING", 1);
        getApplicationContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }
}
