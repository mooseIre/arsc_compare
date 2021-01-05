package com.android.systemui.bubbles.storage;

import java.util.function.Predicate;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: BubbleVolatileRepository.kt */
final class BubbleVolatileRepository$addBubbles$uniqueBubbles$1$1<T> implements Predicate<BubbleEntity> {
    final /* synthetic */ BubbleEntity $b;

    BubbleVolatileRepository$addBubbles$uniqueBubbles$1$1(BubbleEntity bubbleEntity) {
        this.$b = bubbleEntity;
    }

    public final boolean test(@NotNull BubbleEntity bubbleEntity) {
        Intrinsics.checkParameterIsNotNull(bubbleEntity, "e");
        return Intrinsics.areEqual((Object) this.$b.getKey(), (Object) bubbleEntity.getKey());
    }
}
