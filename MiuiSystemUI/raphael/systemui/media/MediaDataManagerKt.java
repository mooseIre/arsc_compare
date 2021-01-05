package com.android.systemui.media;

import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.session.MediaSession;
import android.service.notification.StatusBarNotification;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MediaDataManager.kt */
public final class MediaDataManagerKt {
    /* access modifiers changed from: private */
    public static final String[] ART_URIS = {"android.media.metadata.ALBUM_ART_URI", "android.media.metadata.ART_URI", "android.media.metadata.DISPLAY_ICON_URI"};
    /* access modifiers changed from: private */
    public static final MediaData LOADING = new MediaData(-1, false, 0, (String) null, (Drawable) null, (CharSequence) null, (CharSequence) null, (Icon) null, CollectionsKt__CollectionsKt.emptyList(), CollectionsKt__CollectionsKt.emptyList(), "INVALID", (MediaSession.Token) null, (PendingIntent) null, (MediaDeviceData) null, true, (Runnable) null, false, (String) null, false, 458752, (DefaultConstructorMarker) null);

    public static final boolean isMediaNotification(@NotNull StatusBarNotification statusBarNotification) {
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
