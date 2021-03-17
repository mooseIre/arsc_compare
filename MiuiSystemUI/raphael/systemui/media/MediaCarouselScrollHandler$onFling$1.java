package com.android.systemui.media;

/* compiled from: MediaCarouselScrollHandler.kt */
final class MediaCarouselScrollHandler$onFling$1 implements Runnable {
    final /* synthetic */ MediaCarouselScrollHandler this$0;

    MediaCarouselScrollHandler$onFling$1(MediaCarouselScrollHandler mediaCarouselScrollHandler) {
        this.this$0 = mediaCarouselScrollHandler;
    }

    public final void run() {
        this.this$0.dismissCallback.invoke();
    }
}
