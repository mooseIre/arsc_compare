package com.android.systemui.miui.policy;

import android.os.Bundle;
import com.android.systemui.miui.policy.UsbPortPolicy;
import com.android.systemui.plugins.R;

public class UsbPortRecoveredActivity extends SimpleAlertDialogActivity {
    private UsbPortPolicy.UsbReceiver mReceiver = new UsbPortPolicy.UsbReceiver() {
        /* access modifiers changed from: protected */
        public void onUsbOverheatedStateChanged(boolean z) {
        }

        /* access modifiers changed from: protected */
        public void onUsbShortCircuitChanged(boolean z) {
            if (z) {
                UsbPortRecoveredActivity.this.dismissDialog();
            }
        }
    };

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        getIntent().putExtra("message", getResources().getString(R.string.usb_port_short_circuit_recovered_content));
        super.onCreate(bundle);
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
