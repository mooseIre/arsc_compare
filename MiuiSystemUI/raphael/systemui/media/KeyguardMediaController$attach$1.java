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

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Boolean bool) {
        invoke(bool.booleanValue());
        return Unit.INSTANCE;
    }

    public final void invoke(boolean z) {
        KeyguardMediaController.access$updateVisibility(this.this$0);
    }
}
