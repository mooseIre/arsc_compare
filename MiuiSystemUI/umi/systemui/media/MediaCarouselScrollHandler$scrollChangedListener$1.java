package com.android.systemui.media;

import android.view.View;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaCarouselScrollHandler.kt */
public final class MediaCarouselScrollHandler$scrollChangedListener$1 implements View.OnScrollChangeListener {
    final /* synthetic */ MediaCarouselScrollHandler this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MediaCarouselScrollHandler$scrollChangedListener$1(MediaCarouselScrollHandler mediaCarouselScrollHandler) {
        this.this$0 = mediaCarouselScrollHandler;
    }

    public void onScrollChange(@Nullable View view, int i, int i2, int i3, int i4) {
        if (this.this$0.getPlayerWidthPlusPadding() != 0) {
            int relativeScrollX = this.this$0.getScrollView().getRelativeScrollX();
            MediaCarouselScrollHandler mediaCarouselScrollHandler = this.this$0;
            mediaCarouselScrollHandler.onMediaScrollingChanged(relativeScrollX / mediaCarouselScrollHandler.getPlayerWidthPlusPadding(), relativeScrollX % this.this$0.getPlayerWidthPlusPadding());
        }
    }
}
