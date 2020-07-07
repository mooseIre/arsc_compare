package com.android.systemui.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import com.android.systemui.plugins.R;
import miui.app.Activity;
import miui.app.AlertDialog;

public class UsbDebuggingActivity extends Activity {
    /* access modifiers changed from: private */
    public AlertDialog mCheckBoxDialog;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    /* access modifiers changed from: private */
    public String mKey;
    private DialogInterface.OnClickListener onClickListener;
    private DialogInterface.OnDismissListener onDismissListener;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        getWindow().getDecorView().setAlpha(0.0f);
        if (SystemProperties.getInt("service.adb.tcp.port", 0) == 0) {
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver(this);
        }
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("fingerprints");
        this.mKey = intent.getStringExtra("key");
        if (stringExtra == null || this.mKey == null) {
            finish();
            return;
        }
        this.onClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean z = true;
                boolean z2 = i == -1;
                if (!z2 || !UsbDebuggingActivity.this.mCheckBoxDialog.isChecked()) {
                    z = false;
                }
                if (z2) {
                    try {
                        UsbDebuggingHelper.allowDebugging(z, UsbDebuggingActivity.this.mKey);
                    } catch (Exception e) {
                        Log.e("UsbDebuggingActivity", "Unable to notify Usb service", e);
                    }
                } else {
                    UsbDebuggingHelper.denyDebugging();
                }
                UsbDebuggingActivity.this.finish();
            }
        };
        this.onDismissListener = new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                UsbDebuggingActivity.this.finish();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Dialog_Alert);
        builder.setTitle(getString(R.string.usb_debugging_title));
        builder.setMessage((CharSequence) getString(R.string.usb_debugging_message, new Object[]{stringExtra}));
        builder.setCheckBox(true, getString(R.string.usb_debugging_always));
        builder.setCancelable(true);
        builder.setPositiveButton((CharSequence) getString(17039370), this.onClickListener);
        builder.setNegativeButton((CharSequence) getString(17039360), this.onClickListener);
        builder.setOnDismissListener(this.onDismissListener);
        this.mCheckBoxDialog = builder.create();
        this.mCheckBoxDialog.show();
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
        super.onStart();
        registerReceiver(this.mDisconnectedReceiver, new IntentFilter("android.hardware.usb.action.USB_STATE"));
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        UsbDisconnectedReceiver usbDisconnectedReceiver = this.mDisconnectedReceiver;
        if (usbDisconnectedReceiver != null) {
            unregisterReceiver(usbDisconnectedReceiver);
        }
        super.onStop();
    }
}
