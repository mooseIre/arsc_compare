package com.android.systemui.media;

import androidx.dynamicanimation.animation.FloatPropertyCompat;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MediaCarouselScrollHandler.kt */
public final class MediaCarouselScrollHandler$Companion$CONTENT_TRANSLATION$1 extends FloatPropertyCompat<MediaCarouselScrollHandler> {
    MediaCarouselScrollHandler$Companion$CONTENT_TRANSLATION$1(String str) {
        super(str);
    }

    public float getValue(@NotNull MediaCarouselScrollHandler mediaCarouselScrollHandler) {
        Intrinsics.checkParameterIsNotNull(mediaCarouselScrollHandler, "handler");
        return mediaCarouselScrollHandler.getContentTranslation();
    }

    public void setValue(@NotNull MediaCarouselScrollHandler mediaCarouselScrollHandler, float f) {
        Intrinsics.checkParameterIsNotNull(mediaCarouselScrollHandler, "handler");
        mediaCarouselScrollHandler.setContentTranslation(f);
    }
}
