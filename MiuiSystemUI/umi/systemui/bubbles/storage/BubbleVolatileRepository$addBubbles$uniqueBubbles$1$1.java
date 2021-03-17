package com.android.systemui.bubbles.storage;

import java.util.function.Predicate;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: BubbleVolatileRepository.kt */
public final class BubbleVolatileRepository$addBubbles$uniqueBubbles$1$1<T> implements Predicate<BubbleEntity> {
    final /* synthetic */ BubbleEntity $b;

    BubbleVolatileRepository$addBubbles$uniqueBubbles$1$1(BubbleEntity bubbleEntity) {
        this.$b = bubbleEntity;
    }

    public final boolean test(@NotNull BubbleEntity bubbleEntity) {
        Intrinsics.checkParameterIsNotNull(bubbleEntity, "e");
        return Intrinsics.areEqual(this.$b.getKey(), bubbleEntity.getKey());
    }
}
