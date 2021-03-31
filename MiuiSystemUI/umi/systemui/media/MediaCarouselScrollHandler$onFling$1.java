package com.android.systemui.media;

/* access modifiers changed from: package-private */
/* compiled from: MediaCarouselScrollHandler.kt */
public final class MediaCarouselScrollHandler$onFling$1 implements Runnable {
    final /* synthetic */ MediaCarouselScrollHandler this$0;

    MediaCarouselScrollHandler$onFling$1(MediaCarouselScrollHandler mediaCarouselScrollHandler) {
        this.this$0 = mediaCarouselScrollHandler;
    }

    public final void run() {
        this.this$0.dismissCallback.invoke();
    }
}
