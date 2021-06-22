package com.android.keyguard.negative;

import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;

/* compiled from: MiuiQuickConnectController.kt */
public final class MiuiQuickConnectController$mKeyguardUpdateMonitorCallback$1 extends MiuiKeyguardUpdateMonitorCallback {
    final /* synthetic */ MiuiQuickConnectController this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiQuickConnectController$mKeyguardUpdateMonitorCallback$1(MiuiQuickConnectController miuiQuickConnectController) {
        this.this$0 = miuiQuickConnectController;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onUserUnlocked() {
        this.this$0.initXMYZLRes();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onDeviceProvisioned() {
        this.this$0.initXMYZLRes();
    }
}
