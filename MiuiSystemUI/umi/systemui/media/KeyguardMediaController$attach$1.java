package com.android.systemui.media;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* compiled from: KeyguardMediaController.kt */
final class KeyguardMediaController$attach$1 extends Lambda implements Function1<Boolean, Unit> {
    final /* synthetic */ KeyguardMediaController this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    KeyguardMediaController$attach$1(KeyguardMediaController keyguardMediaController) {
        super(1);
        this.this$0 = keyguardMediaController;
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        invoke(((Boolean) obj).booleanValue());
        return Unit.INSTANCE;
    }

    public final void invoke(boolean z) {
        this.this$0.updateVisibility();
    }
}
