package com.android.keyguard.injector;

/* access modifiers changed from: package-private */
/* compiled from: KeyguardPanelViewInjector.kt */
public final class KeyguardPanelViewInjector$onViewAttachedToWindow$1 implements Runnable {
    final /* synthetic */ KeyguardPanelViewInjector this$0;

    KeyguardPanelViewInjector$onViewAttachedToWindow$1(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        this.this$0 = keyguardPanelViewInjector;
    }

    public final void run() {
        KeyguardPanelViewInjector.access$getMKeyguardStatusBarView$p(this.this$0).setDarkStyle(KeyguardPanelViewInjector.access$getMWallpaperController$p(this.this$0).isWallpaperColorLight());
    }
}
