package com.android.systemui.media;

import com.android.systemui.media.MediaDataFilter;

/* compiled from: MediaDataFilter.kt */
final class MediaDataFilter$1$onUserSwitched$1 implements Runnable {
    final /* synthetic */ int $newUserId;
    final /* synthetic */ MediaDataFilter.AnonymousClass1 this$0;

    MediaDataFilter$1$onUserSwitched$1(MediaDataFilter.AnonymousClass1 r1, int i) {
        this.this$0 = r1;
        this.$newUserId = i;
    }

    public final void run() {
        this.this$0.this$0.handleUserSwitched$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(this.$newUserId);
    }
}
