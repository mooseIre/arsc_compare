package com.android.systemui.media;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: MediaHost.kt */
final class MediaHost$init$3 extends Lambda implements Function0<Unit> {
    final /* synthetic */ int $location;
    final /* synthetic */ MediaHost this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MediaHost$init$3(MediaHost mediaHost, int i) {
        super(0);
        this.this$0 = mediaHost;
        this.$location = i;
    }

    public final void invoke() {
        this.this$0.mediaHostStatesManager.updateHostState(this.$location, this.this$0.state);
    }
}
