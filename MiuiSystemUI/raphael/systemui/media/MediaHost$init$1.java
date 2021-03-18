package com.android.systemui.media;

import android.view.View;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaHost.kt */
public final class MediaHost$init$1 implements View.OnAttachStateChangeListener {
    final /* synthetic */ MediaHost this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MediaHost$init$1(MediaHost mediaHost) {
        this.this$0 = mediaHost;
    }

    public void onViewAttachedToWindow(@Nullable View view) {
        this.this$0.mediaDataFilter.addListener(this.this$0.listener);
        this.this$0.updateViewVisibility();
    }

    public void onViewDetachedFromWindow(@Nullable View view) {
        this.this$0.mediaDataFilter.removeListener(this.this$0.listener);
    }
}
