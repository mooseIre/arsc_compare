package com.android.systemui.media;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.jvm.internal.Intrinsics;

public final class MediaDataManagerKt {
    private static final String[] ART_URIS = {"android.media.metadata.ALBUM_ART_URI", "android.media.metadata.ART_URI", "android.media.metadata.DISPLAY_ICON_URI"};
    private static final MediaData LOADING = new MediaData(-1, false, 0, null, null, null, null, null, CollectionsKt__CollectionsKt.emptyList(), CollectionsKt__CollectionsKt.emptyList(), "INVALID", null, null, null, true, null, false, null, false, 458752, null);

    public static final boolean isMediaNotification(StatusBarNotification statusBarNotification) {
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        if (!statusBarNotification.getNotification().hasMediaSession()) {
            return false;
        }
        Notification notification = statusBarNotification.getNotification();
        Intrinsics.checkExpressionValueIsNotNull(notification, "sbn.notification");
        Class notificationStyle = notification.getNotificationStyle();
        if (Notification.DecoratedMediaCustomViewStyle.class.equals(notificationStyle) || Notification.MediaStyle.class.equals(notificationStyle)) {
            return true;
        }
        return false;
    }
}
