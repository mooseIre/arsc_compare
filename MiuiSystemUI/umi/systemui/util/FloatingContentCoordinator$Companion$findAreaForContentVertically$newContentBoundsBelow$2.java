package com.android.systemui.util;

import android.graphics.Rect;
import java.util.List;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;
import kotlin.jvm.internal.Ref$ObjectRef;
import org.jetbrains.annotations.NotNull;

/* compiled from: FloatingContentCoordinator.kt */
final class FloatingContentCoordinator$Companion$findAreaForContentVertically$newContentBoundsBelow$2 extends Lambda implements Function0<Rect> {
    final /* synthetic */ Rect $contentRect;
    final /* synthetic */ Rect $newlyOverlappingRect;
    final /* synthetic */ Ref$ObjectRef $rectsToAvoidBelow;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    FloatingContentCoordinator$Companion$findAreaForContentVertically$newContentBoundsBelow$2(Rect rect, Ref$ObjectRef ref$ObjectRef, Rect rect2) {
        super(0);
        this.$contentRect = rect;
        this.$rectsToAvoidBelow = ref$ObjectRef;
        this.$newlyOverlappingRect = rect2;
    }

    @NotNull
    public final Rect invoke() {
        return FloatingContentCoordinator.Companion.findAreaForContentAboveOrBelow(this.$contentRect, CollectionsKt___CollectionsKt.plus((List) this.$rectsToAvoidBelow.element, this.$newlyOverlappingRect), false);
    }
}
