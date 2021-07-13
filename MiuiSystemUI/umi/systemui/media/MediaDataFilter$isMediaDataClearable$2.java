package com.android.systemui.media;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: MediaDataFilter.kt */
public final class MediaDataFilter$isMediaDataClearable$2 extends Lambda implements Function1<NotificationEntry, Boolean> {
    public static final MediaDataFilter$isMediaDataClearable$2 INSTANCE = new MediaDataFilter$isMediaDataClearable$2();

    MediaDataFilter$isMediaDataClearable$2() {
        super(1);
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(NotificationEntry notificationEntry) {
        return Boolean.valueOf(invoke(notificationEntry));
    }

    public final boolean invoke(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "it");
        return !notificationEntry.isClearable();
    }
}
