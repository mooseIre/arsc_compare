package com.android.systemui.util;

import android.graphics.Rect;
import kotlin.Lazy;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;
import kotlin.reflect.KProperty;

/* access modifiers changed from: package-private */
/* compiled from: FloatingContentCoordinator.kt */
public final class FloatingContentCoordinator$Companion$findAreaForContentVertically$positionAboveInBounds$2 extends Lambda implements Function0<Boolean> {
    final /* synthetic */ Rect $allowedBounds;
    final /* synthetic */ Lazy $newContentBoundsAbove;
    final /* synthetic */ KProperty $newContentBoundsAbove$metadata;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    FloatingContentCoordinator$Companion$findAreaForContentVertically$positionAboveInBounds$2(Rect rect, Lazy lazy, KProperty kProperty) {
        super(0);
        this.$allowedBounds = rect;
        this.$newContentBoundsAbove = lazy;
        this.$newContentBoundsAbove$metadata = kProperty;
    }

    /* Return type fixed from 'boolean' to match base method */
    @Override // kotlin.jvm.functions.Function0
    public final Boolean invoke() {
        return this.$allowedBounds.contains((Rect) this.$newContentBoundsAbove.getValue());
    }
}
