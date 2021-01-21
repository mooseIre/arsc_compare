package com.android.systemui.statusbar.notification.policy;

import java.util.function.Consumer;

/* compiled from: AppMiniWindowManager.kt */
final class AppMiniWindowManager$mDockedStackExistsListener$1<T> implements Consumer<Boolean> {
    final /* synthetic */ AppMiniWindowManager this$0;

    AppMiniWindowManager$mDockedStackExistsListener$1(AppMiniWindowManager appMiniWindowManager) {
        this.this$0 = appMiniWindowManager;
    }

    public /* bridge */ /* synthetic */ void accept(Object obj) {
        accept(((Boolean) obj).booleanValue());
    }

    public final void accept(boolean z) {
        this.this$0.mInDockedStackMode = z;
    }
}
