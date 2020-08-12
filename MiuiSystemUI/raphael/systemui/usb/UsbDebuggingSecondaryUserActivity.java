package com.android.systemui.usb;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemProperties;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.systemui.plugins.R;

public class UsbDebuggingSecondaryUserActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private UsbDisconnectedReceiver mDisconnectedReceiver;

    /* JADX WARNING: type inference failed for: r1v0, types: [android.content.DialogInterface$OnClickListener, com.android.systemui.usb.UsbDebuggingSecondaryUserActivity, com.android.internal.app.AlertActivity, android.app.Activity] */
    public void onCreate(Bundle bundle) {
        UsbDebuggingSecondaryUserActivity.super.onCreate(bundle);
        if (SystemProperties.getInt("service.adb.tcp.port", 0) == 0) {
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver(this);
        }
        AlertController.AlertParams alertParams = this.mAlertParams;
        alertParams.mTitle = getString(R.string.usb_debugging_secondary_user_title);
        alertParams.mMessage = getString(R.string.usb_debugging_secondary_user_message);
        alertParams.mPositiveButtonText = getString(17039370);
        alertParams.mPositiveButtonListener = this;
        setupAlert();
    }

    private class UsbDisconnectedReceiver extends BroadcastReceiver {
        private final Activity mActivity;

        public UsbDisconnectedReceiver(Activity activity) {
            this.mActivity = activity;
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.hardware.usb.action.USB_STATE".equals(intent.getAction()) && !intent.getBooleanExtra("connected", false)) {
                this.mActivity.finish();
            }
        }
    }

    public void onStart() {
        UsbDebuggingSecondaryUserActivity.super.onStart();
        registerReceiver(this.mDisconnectedReceiver, new IntentFilter("android.hardware.usb.action.USB_STATE"));
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        UsbDisconnectedReceiver usbDisconnectedReceiver = this.mDisconnectedReceiver;
        if (usbDisconnectedReceiver != null) {
            unregisterReceiver(usbDisconnectedReceiver);
        }
        UsbDebuggingSecondaryUserActivity.super.onStop();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        finish();
    }
}
