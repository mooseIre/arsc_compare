package com.android.systemui.doze;

import android.content.Context;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.doze.DozeMachine;

public class DozeAuthRemover implements DozeMachine.Part {
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class));

    public DozeAuthRemover(Context context) {
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void transitionTo(DozeMachine.State state, DozeMachine.State state2) {
        if (state2 == DozeMachine.State.DOZE || state2 == DozeMachine.State.DOZE_AOD) {
            if (this.mKeyguardUpdateMonitor.getUserUnlockedWithBiometric(KeyguardUpdateMonitor.getCurrentUser())) {
                this.mKeyguardUpdateMonitor.clearBiometricRecognized();
            }
        }
    }
}
