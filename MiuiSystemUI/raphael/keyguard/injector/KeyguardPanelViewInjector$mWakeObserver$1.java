package com.android.keyguard.injector;

import com.android.keyguard.KeyguardMoveHelper;
import com.android.systemui.keyguard.WakefulnessLifecycle;

/* compiled from: KeyguardPanelViewInjector.kt */
public final class KeyguardPanelViewInjector$mWakeObserver$1 implements WakefulnessLifecycle.Observer {
    final /* synthetic */ KeyguardPanelViewInjector this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    KeyguardPanelViewInjector$mWakeObserver$1(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        this.this$0 = keyguardPanelViewInjector;
    }

    @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
    public void onStartedWakingUp() {
        KeyguardMoveHelper keyguardMoveHelper = this.this$0.mKeyguardMoveHelper;
        if (keyguardMoveHelper != null) {
            keyguardMoveHelper.onStartedWakingUp();
        }
        KeyguardPanelViewInjector.access$getMLockScreenMagazineController$p(this.this$0).onStartedWakingUp();
        KeyguardPanelViewInjector.access$getMIndicationController$p(this.this$0).onStartedWakingUp();
    }

    @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
    public void onStartedGoingToSleep() {
        KeyguardPanelViewInjector.access$getMLockScreenMagazineController$p(this.this$0).onStartedGoingToSleep();
    }

    @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
    public void onFinishedGoingToSleep() {
        KeyguardMoveHelper keyguardMoveHelper = this.this$0.mKeyguardMoveHelper;
        if (keyguardMoveHelper != null) {
            keyguardMoveHelper.onFinishedGoingToSleep();
        }
        KeyguardPanelViewInjector.access$getMLockScreenMagazineController$p(this.this$0).onFinishedGoingToSleep();
        KeyguardPanelViewInjector.access$getMIndicationController$p(this.this$0).onFinishedGoingToSleep();
    }
}
