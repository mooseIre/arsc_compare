package com.android.systemui.statusbar.policy;

import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;

/* compiled from: MiuiHeadsUpPolicy.kt */
public final class MiuiHeadsUpPolicy$mKeyguardUpdateMonitorCallback$1 extends MiuiKeyguardUpdateMonitorCallback {
    final /* synthetic */ MiuiHeadsUpPolicy this$0;

    MiuiHeadsUpPolicy$mKeyguardUpdateMonitorCallback$1(MiuiHeadsUpPolicy miuiHeadsUpPolicy) {
        this.this$0 = miuiHeadsUpPolicy;
    }

    public void onKeyguardShowingChanged(boolean z) {
        if (z) {
            this.this$0.headsUpManagerPhone.clearSnoozePackages();
        }
    }
}
