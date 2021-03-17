package com.android.systemui.usb;

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
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import miui.app.Activity;
import miui.app.AlertDialog;

public class UsbDebuggingActivity extends Activity {
    private AlertDialog mCheckBoxDialog;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    private String mKey;
    private DialogInterface.OnClickListener onClickListener;
    private DialogInterface.OnDismissListener onDismissListener;

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: com.android.systemui.usb.UsbDebuggingActivity */
    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) {
        UsbDebuggingActivity.super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        getWindow().getDecorView().setAlpha(0.0f);
        if (SystemProperties.getInt("service.adb.tcp.port", 0) == 0) {
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver(this, this);
        }
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("fingerprints");
        String stringExtra2 = intent.getStringExtra("key");
        this.mKey = stringExtra2;
        if (stringExtra == null || stringExtra2 == null) {
            finish();
            return;
        }
        this.onClickListener = new DialogInterface.OnClickListener() {
            /* class com.android.systemui.usb.UsbDebuggingActivity.AnonymousClass1 */

            public void onClick(DialogInterface dialogInterface, int i) {
                boolean z = true;
                boolean z2 = i == -1;
                if (!z2 || !UsbDebuggingActivity.this.mCheckBoxDialog.isChecked()) {
                    z = false;
                }
                if (z2) {
                    try {
                        UsbDebuggingActivity.allowDebugging(z, UsbDebuggingActivity.this.mKey);
                    } catch (Exception e) {
                        Log.e("UsbDebuggingActivity", "Unable to notify Usb service", e);
                    }
                } else {
                    UsbDebuggingActivity.denyDebugging();
                }
                UsbDebuggingActivity.this.finish();
            }
        };
        this.onDismissListener = new DialogInterface.OnDismissListener() {
            /* class com.android.systemui.usb.UsbDebuggingActivity.AnonymousClass2 */

            public void onDismiss(DialogInterface dialogInterface) {
                UsbDebuggingActivity.this.finish();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this, C0022R$style.Theme_Dialog_Alert);
        builder.setTitle(getString(C0021R$string.usb_debugging_title)).setMessage(getString(C0021R$string.usb_debugging_message, new Object[]{stringExtra})).setCheckBox(true, getString(C0021R$string.usb_debugging_always)).setCancelable(true).setPositiveButton(getString(17039370), this.onClickListener).setNegativeButton(getString(17039360), this.onClickListener).setOnDismissListener(this.onDismissListener);
        AlertDialog create = builder.create();
        this.mCheckBoxDialog = create;
        create.show();
    }

    private class UsbDisconnectedReceiver extends BroadcastReceiver {
        private final Activity mActivity;

        public UsbDisconnectedReceiver(UsbDebuggingActivity usbDebuggingActivity, Activity activity) {
            this.mActivity = activity;
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.hardware.usb.action.USB_STATE".equals(intent.getAction()) && !intent.getBooleanExtra("connected", false)) {
                this.mActivity.finish();
            }
        }
    }

    public void onStart() {
        UsbDebuggingActivity.super.onStart();
        registerReceiver(this.mDisconnectedReceiver, new IntentFilter("android.hardware.usb.action.USB_STATE"));
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        UsbDisconnectedReceiver usbDisconnectedReceiver = this.mDisconnectedReceiver;
        if (usbDisconnectedReceiver != null) {
            unregisterReceiver(usbDisconnectedReceiver);
        }
        UsbDebuggingActivity.super.onStop();
    }

    public static IAdbManager getService() {
        return IAdbManager.Stub.asInterface(ServiceManager.getService("adb"));
    }

    public static void allowDebugging(boolean z, String str) throws RemoteException {
        getService().allowDebugging(z, str);
    }

    public static void denyDebugging() throws RemoteException {
        getService().denyDebugging();
    }
}
