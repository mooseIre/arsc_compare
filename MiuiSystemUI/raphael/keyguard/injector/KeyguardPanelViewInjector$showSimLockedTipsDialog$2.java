package com.android.keyguard.injector;

import android.view.View;

/* compiled from: KeyguardPanelViewInjector.kt */
final class KeyguardPanelViewInjector$showSimLockedTipsDialog$2 implements View.OnSystemUiVisibilityChangeListener {
    final /* synthetic */ KeyguardPanelViewInjector this$0;

    KeyguardPanelViewInjector$showSimLockedTipsDialog$2(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        this.this$0 = keyguardPanelViewInjector;
    }

    public final void onSystemUiVisibilityChange(int i) {
        if (i == 0) {
            this.this$0.alertDialogDecorViewAddFlag();
        }
    }
}
