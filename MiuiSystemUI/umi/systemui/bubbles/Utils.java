package com.android.systemui.bubbles;

import android.app.Notification;
import android.content.Context;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import com.android.systemui.plugins.R;

public class Utils {
    public static CharSequence getUpdateMessage(Context context, StatusBarNotification statusBarNotification) {
        Notification notification = statusBarNotification.getNotification();
        Class notificationStyle = notification.getNotificationStyle();
        try {
            if (Notification.BigTextStyle.class.equals(notificationStyle)) {
                CharSequence charSequence = notification.extras.getCharSequence("android.bigText");
                return !TextUtils.isEmpty(charSequence) ? charSequence : notification.extras.getCharSequence("android.text");
            }
            if (Notification.MessagingStyle.class.equals(notificationStyle)) {
                Notification.MessagingStyle.Message findLatestIncomingMessage = Notification.MessagingStyle.findLatestIncomingMessage(Notification.MessagingStyle.Message.getMessagesFromBundleArray((Parcelable[]) notification.extras.get("android.messages")));
                if (findLatestIncomingMessage != null) {
                    CharSequence name = findLatestIncomingMessage.getSenderPerson() != null ? findLatestIncomingMessage.getSenderPerson().getName() : null;
                    if (TextUtils.isEmpty(name)) {
                        return findLatestIncomingMessage.getText();
                    }
                    return context.getResources().getString(R.string.notification_summary_message_format, new Object[]{name, findLatestIncomingMessage.getText()});
                }
            } else if (Notification.InboxStyle.class.equals(notificationStyle)) {
                CharSequence[] charSequenceArray = notification.extras.getCharSequenceArray("android.textLines");
                if (charSequenceArray != null && charSequenceArray.length > 0) {
                    return charSequenceArray[charSequenceArray.length - 1];
                }
            } else if (Notification.MediaStyle.class.equals(notificationStyle)) {
                return null;
            } else {
                return notification.extras.getCharSequence("android.text");
            }
            return null;
        } catch (ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
