package com.android.systemui.media;

import android.view.MotionEvent;
import com.android.systemui.Gefingerpoken;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaCarouselScrollHandler.kt */
public final class MediaCarouselScrollHandler$touchListener$1 implements Gefingerpoken {
    final /* synthetic */ MediaCarouselScrollHandler this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MediaCarouselScrollHandler$touchListener$1(MediaCarouselScrollHandler mediaCarouselScrollHandler) {
        this.this$0 = mediaCarouselScrollHandler;
    }

    @Override // com.android.systemui.Gefingerpoken
    public boolean onTouchEvent(@Nullable MotionEvent motionEvent) {
        MediaCarouselScrollHandler mediaCarouselScrollHandler = this.this$0;
        if (motionEvent != null) {
            return mediaCarouselScrollHandler.onTouch(motionEvent);
        }
        Intrinsics.throwNpe();
        throw null;
    }

    @Override // com.android.systemui.Gefingerpoken
    public boolean onInterceptTouchEvent(@Nullable MotionEvent motionEvent) {
        MediaCarouselScrollHandler mediaCarouselScrollHandler = this.this$0;
        if (motionEvent != null) {
            return mediaCarouselScrollHandler.onInterceptTouch(motionEvent);
        }
        Intrinsics.throwNpe();
        throw null;
    }
}
