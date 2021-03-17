package com.android.keyguard.injector;

/* compiled from: KeyguardViewMediatorInjector.kt */
final class KeyguardViewMediatorInjector$keyguardGoingAway$1 implements Runnable {
    final /* synthetic */ KeyguardViewMediatorInjector this$0;

    KeyguardViewMediatorInjector$keyguardGoingAway$1(KeyguardViewMediatorInjector keyguardViewMediatorInjector) {
        this.this$0 = keyguardViewMediatorInjector;
    }

    public final void run() {
        this.this$0.doKeyguardGoingAway();
    }
}
