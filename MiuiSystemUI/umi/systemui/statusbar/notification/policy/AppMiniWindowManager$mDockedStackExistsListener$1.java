package com.android.systemui.statusbar.notification.policy;

import java.util.function.Consumer;

/* access modifiers changed from: package-private */
/* compiled from: AppMiniWindowManager.kt */
public final class AppMiniWindowManager$mDockedStackExistsListener$1<T> implements Consumer<Boolean> {
    final /* synthetic */ AppMiniWindowManager this$0;

    AppMiniWindowManager$mDockedStackExistsListener$1(AppMiniWindowManager appMiniWindowManager) {
        this.this$0 = appMiniWindowManager;
    }

    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // java.util.function.Consumer
    public /* bridge */ /* synthetic */ void accept(Boolean bool) {
        accept(bool.booleanValue());
    }

    public final void accept(boolean z) {
        this.this$0.mInDockedStackMode = z;
    }
}
