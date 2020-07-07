package com.android.systemui.wirelesscharge;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import com.android.systemui.plugins.R;
import miui.app.Activity;
import miui.app.AlertDialog;
import miui.util.IWirelessSwitch;

public class WirelessChargingConfirmActivity extends Activity {
    private DialogInterface.OnDismissListener OnFirstDismissListener;
    private DialogInterface.OnDismissListener OnSecondDismissListener;
    /* access modifiers changed from: private */
    public AlertDialog mCheckBoxDialog;
    /* access modifiers changed from: private */
    public Context mContext;
    private DialogInterface.OnClickListener onFirstClickListener;
    private DialogInterface.OnClickListener onSecondClickListener;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mContext = getApplicationContext();
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        getWindow().getDecorView().setAlpha(0.0f);
        this.onFirstClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean z = i == -1;
                Settings.System.putInt(WirelessChargingConfirmActivity.this.mContext.getContentResolver(), "disable_show_tips", (!z || !WirelessChargingConfirmActivity.this.mCheckBoxDialog.isChecked()) ? 0 : 1);
                if (!z) {
                    return;
                }
                if (WirelessChargingConfirmActivity.this.isSaveModeOn()) {
                    WirelessChargingConfirmActivity.this.showSecondConfirmDiaglog();
                    return;
                }
                IWirelessSwitch.getInstance().setWirelessChargingEnabled(true);
                WirelessChargingConfirmActivity.this.sendUpdateStatusBroadCast(0);
                WirelessChargingConfirmActivity.this.finish();
            }
        };
        this.onSecondClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == -1) {
                    IWirelessSwitch.getInstance().setWirelessChargingEnabled(true);
                    WirelessChargingConfirmActivity.this.sendUpdateStatusBroadCast(0);
                } else {
                    WirelessChargingConfirmActivity.this.sendUpdateStatusBroadCast(1);
                }
                WirelessChargingConfirmActivity.this.finish();
            }
        };
        this.OnFirstDismissListener = new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                if (!WirelessChargingConfirmActivity.this.isSaveModeOn()) {
                    WirelessChargingConfirmActivity.this.finish();
                }
            }
        };
        this.OnSecondDismissListener = new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                WirelessChargingConfirmActivity.this.finish();
            }
        };
        if (Settings.System.getInt(this.mContext.getContentResolver(), "disable_show_tips", 0) != 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Dialog_Alert);
            builder.setTitle(getString(R.string.wireless_charging_tips_title));
            builder.setMessage((CharSequence) getString(R.string.wireless_charging_tips_message));
            builder.setCheckBox(false, getString(R.string.wireless_charging_tips_disable));
            builder.setCancelable(false);
            builder.setPositiveButton((CharSequence) getString(R.string.wireless_charging_ok), this.onFirstClickListener);
            builder.setOnDismissListener(this.OnFirstDismissListener);
            this.mCheckBoxDialog = builder.create();
            this.mCheckBoxDialog.show();
        } else if (isSaveModeOn()) {
            showSecondConfirmDiaglog();
        } else {
            IWirelessSwitch.getInstance().setWirelessChargingEnabled(true);
            sendUpdateStatusBroadCast(0);
            finish();
        }
    }

    /* access modifiers changed from: private */
    public boolean isSaveModeOn() {
        return Settings.System.getInt(this.mContext.getContentResolver(), "POWER_SAVE_MODE_OPEN", 0) != 0;
    }

    /* access modifiers changed from: private */
    public void showSecondConfirmDiaglog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Dialog_Alert);
        builder.setMessage((CharSequence) getString(R.string.wireless_charging_saver_message));
        builder.setCancelable(false);
        builder.setPositiveButton((CharSequence) getString(R.string.wireless_charging_saver_ok), this.onSecondClickListener);
        builder.setNegativeButton((CharSequence) getString(R.string.wireless_charging_saver_cancel), this.onSecondClickListener);
        builder.setOnDismissListener(this.OnSecondDismissListener);
        builder.create().show();
    }

    /* access modifiers changed from: private */
    public void sendUpdateStatusBroadCast(int i) {
        Intent intent = new Intent("miui.intent.action.ACTION_WIRELESS_CHARGING");
        intent.addFlags(822083584);
        intent.putExtra("miui.intent.extra.WIRELESS_CHARGING", i);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }
}
