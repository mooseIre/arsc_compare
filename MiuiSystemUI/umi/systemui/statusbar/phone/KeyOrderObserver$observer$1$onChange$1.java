package com.android.systemui.statusbar.phone;

/* compiled from: KeyOrderObserver.kt */
final class KeyOrderObserver$observer$1$onChange$1 implements Runnable {
    final /* synthetic */ KeyOrderObserver$observer$1 this$0;

    KeyOrderObserver$observer$1$onChange$1(KeyOrderObserver$observer$1 keyOrderObserver$observer$1) {
        this.this$0 = keyOrderObserver$observer$1;
    }

    public final void run() {
        KeyOrderObserver.access$getKeyOrderCallback$p(this.this$0.this$0).invoke();
    }
}
