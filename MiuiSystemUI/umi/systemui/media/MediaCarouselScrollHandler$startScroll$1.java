package com.android.systemui.media;

/* compiled from: MediaCarouselScrollHandler.kt */
final class MediaCarouselScrollHandler$startScroll$1 implements Runnable {
    final /* synthetic */ int $scrollX;
    final /* synthetic */ int $scrollY;
    final /* synthetic */ MediaCarouselScrollHandler this$0;

    MediaCarouselScrollHandler$startScroll$1(MediaCarouselScrollHandler mediaCarouselScrollHandler, int i, int i2) {
        this.this$0 = mediaCarouselScrollHandler;
        this.$scrollX = i;
        this.$scrollY = i2;
    }

    public final void run() {
        this.this$0.getScrollView().smoothScrollTo(this.$scrollX, this.$scrollY);
    }
}
