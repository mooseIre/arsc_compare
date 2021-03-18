package com.android.keyguard.injector;

/* access modifiers changed from: package-private */
/* compiled from: KeyguardViewMediatorInjector.kt */
public final class KeyguardViewMediatorInjector$keyguardGoingAway$1 implements Runnable {
    final /* synthetic */ KeyguardViewMediatorInjector this$0;

    KeyguardViewMediatorInjector$keyguardGoingAway$1(KeyguardViewMediatorInjector keyguardViewMediatorInjector) {
        this.this$0 = keyguardViewMediatorInjector;
    }

    public final void run() {
        this.this$0.doKeyguardGoingAway();
    }
}
