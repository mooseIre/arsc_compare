package com.android.systemui.statusbar.notification.collection.listbuilder.pluggable;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public abstract class NotifPromoter extends Pluggable<NotifPromoter> {
    public abstract boolean shouldPromoteToTopLevel(NotificationEntry notificationEntry);

    protected NotifPromoter(String str) {
        super(str);
    }
}
