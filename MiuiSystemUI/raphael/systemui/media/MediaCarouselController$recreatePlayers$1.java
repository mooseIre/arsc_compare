package com.android.systemui.media;

import java.util.function.BiConsumer;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: MediaCarouselController.kt */
public final class MediaCarouselController$recreatePlayers$1<T, U> implements BiConsumer<String, MediaData> {
    final /* synthetic */ MediaCarouselController this$0;

    MediaCarouselController$recreatePlayers$1(MediaCarouselController mediaCarouselController) {
        this.this$0 = mediaCarouselController;
    }

    public final void accept(@NotNull String str, @NotNull MediaData mediaData) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(mediaData, "data");
        this.this$0.removePlayer(str);
        this.this$0.addOrUpdatePlayer(str, null, mediaData);
    }
}
