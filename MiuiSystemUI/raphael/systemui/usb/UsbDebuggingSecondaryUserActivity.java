package com.android.systemui.usb;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.debug.IAdbManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.systemui.C0021R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;

public class UsbDebuggingSecondaryUserActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private final BroadcastDispatcher mBroadcastDispatcher;
    private UsbDisconnectedReceiver mDisconnectedReceiver;

    public UsbDebuggingSecondaryUserActivity(BroadcastDispatcher broadcastDispatcher) {
        this.mBroadcastDispatcher = broadcastDispatcher;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: com.android.systemui.usb.UsbDebuggingSecondaryUserActivity */
    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) {
        UsbDebuggingSecondaryUserActivity.super.onCreate(bundle);
        if (SystemProperties.getInt("service.adb.tcp.port", 0) == 0) {
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver(this, this);
        }
        AlertController.AlertParams alertParams = ((AlertActivity) this).mAlertParams;
        alertParams.mTitle = getString(C0021R$string.usb_debugging_secondary_user_title);
        alertParams.mMessage = getString(C0021R$string.usb_debugging_secondary_user_message);
        alertParams.mPositiveButtonText = getString(17039370);
        alertParams.mPositiveButtonListener = this;
        setupAlert();
    }

    private class UsbDisconnectedReceiver extends BroadcastReceiver {
        private final Activity mActivity;

        UsbDisconnectedReceiver(UsbDebuggingSecondaryUserActivity usbDebuggingSecondaryUserActivity, Activity activity) {
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
        if (this.mDisconnectedReceiver != null) {
            this.mBroadcastDispatcher.registerReceiver(this.mDisconnectedReceiver, new IntentFilter("android.hardware.usb.action.USB_STATE"));
        }
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        UsbDisconnectedReceiver usbDisconnectedReceiver = this.mDisconnectedReceiver;
        if (usbDisconnectedReceiver != null) {
            this.mBroadcastDispatcher.unregisterReceiver(usbDisconnectedReceiver);
        }
        try {
            IAdbManager.Stub.asInterface(ServiceManager.getService("adb")).denyDebugging();
        } catch (RemoteException e) {
            Log.e("UsbDebuggingSecondaryUserActivity", "Unable to notify Usb service", e);
        }
        UsbDebuggingSecondaryUserActivity.super.onStop();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        finish();
    }
}
