package com.android.keyguard.injector;

/* compiled from: KeyguardViewMediatorInjector.kt */
final class KeyguardViewMediatorInjector$mShowPasswordScreenReceiver$1$onReceive$1 implements Runnable {
    final /* synthetic */ KeyguardViewMediatorInjector$mShowPasswordScreenReceiver$1 this$0;

    KeyguardViewMediatorInjector$mShowPasswordScreenReceiver$1$onReceive$1(KeyguardViewMediatorInjector$mShowPasswordScreenReceiver$1 keyguardViewMediatorInjector$mShowPasswordScreenReceiver$1) {
        this.this$0 = keyguardViewMediatorInjector$mShowPasswordScreenReceiver$1;
    }

    public final void run() {
        this.this$0.this$0.getMStatusBarKeyguardViewManager().showBouncer(true);
    }
}
