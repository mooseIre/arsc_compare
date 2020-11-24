package com.android.systemui.statusbar.notification;

import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public class NotificationChannelHelper {
    public static NotificationChannel createConversationChannelIfNeeded(Context context, INotificationManager iNotificationManager, NotificationEntry notificationEntry, NotificationChannel notificationChannel) {
        if (!TextUtils.isEmpty(notificationChannel.getConversationId())) {
            return notificationChannel;
        }
        String shortcutId = notificationEntry.getSbn().getShortcutId();
        String packageName = notificationEntry.getSbn().getPackageName();
        int uid = notificationEntry.getSbn().getUid();
        if (TextUtils.isEmpty(shortcutId) || TextUtils.isEmpty(packageName) || notificationEntry.getRanking().getShortcutInfo() == null) {
            return notificationChannel;
        }
        try {
            notificationChannel.setName(getName(notificationEntry));
            iNotificationManager.createConversationNotificationChannelForPackage(packageName, uid, notificationEntry.getSbn().getKey(), notificationChannel, shortcutId);
            return iNotificationManager.getConversationNotificationChannel(context.getOpPackageName(), UserHandle.getUserId(uid), packageName, notificationChannel.getId(), false, shortcutId);
        } catch (RemoteException e) {
            Slog.e("NotificationChannelHelper", "Could not create conversation channel", e);
            return notificationChannel;
        }
    }

    private static CharSequence getName(NotificationEntry notificationEntry) {
        if (notificationEntry.getRanking().getShortcutInfo().getLabel() != null) {
            return notificationEntry.getRanking().getShortcutInfo().getLabel().toString();
        }
        Bundle bundle = notificationEntry.getSbn().getNotification().extras;
        CharSequence charSequence = bundle.getCharSequence("android.conversationTitle");
        if (TextUtils.isEmpty(charSequence)) {
            charSequence = bundle.getCharSequence("android.title");
        }
        return TextUtils.isEmpty(charSequence) ? "fallback" : charSequence;
    }
}
