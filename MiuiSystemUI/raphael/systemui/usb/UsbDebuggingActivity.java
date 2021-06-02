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
import miuix.appcompat.app.AlertDialog;
import miuix.appcompat.app.AppCompatActivity;

public class UsbDebuggingActivity extends AppCompatActivity {
    private AlertDialog mCheckBoxDialog;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    private String mKey;
    private DialogInterface.OnClickListener onClickListener;
    private DialogInterface.OnDismissListener onDismissListener;

    @Override // androidx.activity.ComponentActivity, miuix.appcompat.app.AppCompatActivity, androidx.core.app.ComponentActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getAppCompatActionBar() != null) {
            getAppCompatActionBar().hide();
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
        builder.setTitle(getString(C0021R$string.usb_debugging_title));
        builder.setMessage(getString(C0021R$string.usb_debugging_message, new Object[]{stringExtra}));
        builder.setCheckBox(true, getString(C0021R$string.usb_debugging_always));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(17039370), this.onClickListener);
        builder.setNegativeButton(getString(17039360), this.onClickListener);
        builder.setOnDismissListener(this.onDismissListener);
        AlertDialog create = builder.create();
        this.mCheckBoxDialog = create;
        create.show();
    }

    private class UsbDisconnectedReceiver extends BroadcastReceiver {
        private final AppCompatActivity mActivity;

        public UsbDisconnectedReceiver(UsbDebuggingActivity usbDebuggingActivity, AppCompatActivity appCompatActivity) {
            this.mActivity = appCompatActivity;
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.hardware.usb.action.USB_STATE".equals(intent.getAction()) && !intent.getBooleanExtra("connected", false)) {
                this.mActivity.finish();
            }
        }
    }

    @Override // androidx.fragment.app.FragmentActivity
    public void onStart() {
        super.onStart();
        registerReceiver(this.mDisconnectedReceiver, new IntentFilter("android.hardware.usb.action.USB_STATE"));
    }

    /* access modifiers changed from: protected */
    @Override // miuix.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity
    public void onStop() {
        UsbDisconnectedReceiver usbDisconnectedReceiver = this.mDisconnectedReceiver;
        if (usbDisconnectedReceiver != null) {
            unregisterReceiver(usbDisconnectedReceiver);
        }
        super.onStop();
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
