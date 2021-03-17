package com.android.systemui.media;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* access modifiers changed from: package-private */
/* compiled from: MediaHierarchyManager.kt */
public final class MediaHierarchyManager$register$1 extends Lambda implements Function1<Boolean, Unit> {
    final /* synthetic */ MediaHierarchyManager this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MediaHierarchyManager$register$1(MediaHierarchyManager mediaHierarchyManager) {
        super(1);
        this.this$0 = mediaHierarchyManager;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Boolean bool) {
        invoke(bool.booleanValue());
        return Unit.INSTANCE;
    }

    public final void invoke(boolean z) {
        this.this$0.updateDesiredLocation(true);
    }
}
