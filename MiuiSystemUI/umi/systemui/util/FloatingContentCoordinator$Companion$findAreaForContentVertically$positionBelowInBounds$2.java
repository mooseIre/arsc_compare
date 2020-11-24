package com.android.systemui.util;

import android.graphics.Rect;
import kotlin.Lazy;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;
import kotlin.reflect.KProperty;

/* compiled from: FloatingContentCoordinator.kt */
final class FloatingContentCoordinator$Companion$findAreaForContentVertically$positionBelowInBounds$2 extends Lambda implements Function0<Boolean> {
    final /* synthetic */ Rect $allowedBounds;
    final /* synthetic */ Lazy $newContentBoundsBelow;
    final /* synthetic */ KProperty $newContentBoundsBelow$metadata;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    FloatingContentCoordinator$Companion$findAreaForContentVertically$positionBelowInBounds$2(Rect rect, Lazy lazy, KProperty kProperty) {
        super(0);
        this.$allowedBounds = rect;
        this.$newContentBoundsBelow = lazy;
        this.$newContentBoundsBelow$metadata = kProperty;
    }

    public final boolean invoke() {
        return this.$allowedBounds.contains((Rect) this.$newContentBoundsBelow.getValue());
    }
}
