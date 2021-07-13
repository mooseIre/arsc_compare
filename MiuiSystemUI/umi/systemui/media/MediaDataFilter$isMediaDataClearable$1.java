package com.android.systemui.media;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: MediaDataFilter.kt */
public final class MediaDataFilter$isMediaDataClearable$1 extends Lambda implements Function1<String, NotificationEntry> {
    final /* synthetic */ MediaDataFilter this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MediaDataFilter$isMediaDataClearable$1(MediaDataFilter mediaDataFilter) {
        super(1);
        this.this$0 = mediaDataFilter;
    }

    public final NotificationEntry invoke(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "it");
        return this.this$0.entryManager.getActiveNotificationUnfiltered(str);
    }
}
