package com.android.keyguard.injector;

import com.android.keyguard.KeyguardMoveHelper;
import com.android.systemui.keyguard.WakefulnessLifecycle;

/* compiled from: KeyguardPanelViewInjector.kt */
public final class KeyguardPanelViewInjector$mWakeObserver$1 implements WakefulnessLifecycle.Observer {
    final /* synthetic */ KeyguardPanelViewInjector this$0;

    KeyguardPanelViewInjector$mWakeObserver$1(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        this.this$0 = keyguardPanelViewInjector;
    }

    public void onStartedWakingUp() {
        KeyguardMoveHelper access$getMKeyguardMoveHelper$p = this.this$0.mKeyguardMoveHelper;
        if (access$getMKeyguardMoveHelper$p != null) {
            access$getMKeyguardMoveHelper$p.onStartedWakingUp();
        }
        KeyguardPanelViewInjector.access$getMLockScreenMagazineController$p(this.this$0).onStartedWakingUp();
        KeyguardPanelViewInjector.access$getMIndicationController$p(this.this$0).onStartedWakingUp();
    }

    public void onFinishedGoingToSleep() {
        KeyguardMoveHelper access$getMKeyguardMoveHelper$p = this.this$0.mKeyguardMoveHelper;
        if (access$getMKeyguardMoveHelper$p != null) {
            access$getMKeyguardMoveHelper$p.onFinishedGoingToSleep();
        }
        KeyguardPanelViewInjector.access$getMLockScreenMagazineController$p(this.this$0).onFinishedGoingToSleep();
        KeyguardPanelViewInjector.access$getMIndicationController$p(this.this$0).onFinishedGoingToSleep();
    }
}
