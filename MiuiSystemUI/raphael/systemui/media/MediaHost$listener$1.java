package com.android.systemui.media;

import com.android.systemui.media.MediaDataManager;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaHost.kt */
public final class MediaHost$listener$1 implements MediaDataManager.Listener {
    final /* synthetic */ MediaHost this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MediaHost$listener$1(MediaHost mediaHost) {
        this.this$0 = mediaHost;
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(mediaData, "data");
        this.this$0.updateViewVisibility();
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataRemoved(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        this.this$0.updateViewVisibility();
    }
}
