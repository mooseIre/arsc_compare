package com.android.systemui.miui.policy;

import android.content.DialogInterface;
import android.os.Bundle;
import com.android.systemui.miui.policy.UsbPortPolicy;
import com.android.systemui.plugins.R;
import miui.app.AlertDialog;

public class UsbPortShortCircuitActivity extends SimpleAlertDialogActivity {
    private UsbPortPolicy.UsbReceiver mReceiver = new UsbPortPolicy.UsbReceiver() {
        /* access modifiers changed from: protected */
        public void onUsbOverheatedStateChanged(boolean z) {
        }

        /* access modifiers changed from: protected */
        public void onUsbShortCircuitChanged(boolean z) {
            if (!z) {
                UsbPortShortCircuitActivity.this.dismissDialog();
            }
        }
    };

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        getWindow().addFlags(2621440);
        super.onCreate(bundle);
    }

    /* access modifiers changed from: protected */
    public AlertDialog.Builder createDialogBuilder() {
        AlertDialog.Builder createDialogBuilder = super.createDialogBuilder();
        createDialogBuilder.setTitle((int) R.string.usb_port_short_circuit_title);
        createDialogBuilder.setView(R.layout.usb_port_short_circuit_dialog);
        createDialogBuilder.setCancelable(false);
        createDialogBuilder.setPositiveButton((int) R.string.usb_port_ok, (DialogInterface.OnClickListener) null);
        return createDialogBuilder;
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        this.mReceiver.register(this, 1000);
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        this.mReceiver.unregister(this);
        super.onStop();
    }
}
