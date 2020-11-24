package com.android.systemui.media;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* compiled from: MediaHierarchyManager.kt */
final class MediaHierarchyManager$register$1 extends Lambda implements Function1<Boolean, Unit> {
    final /* synthetic */ MediaHierarchyManager this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MediaHierarchyManager$register$1(MediaHierarchyManager mediaHierarchyManager) {
        super(1);
        this.this$0 = mediaHierarchyManager;
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        invoke(((Boolean) obj).booleanValue());
        return Unit.INSTANCE;
    }

    public final void invoke(boolean z) {
        this.this$0.updateDesiredLocation(true);
    }
}
