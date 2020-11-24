package com.android.systemui.statusbar.notification.icon;

import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;

/* compiled from: IconManager.kt */
final class IconManager$createIcons$1 implements StatusBarIconView.OnVisibilityChangedListener {
    final /* synthetic */ NotificationEntry $entry;

    IconManager$createIcons$1(NotificationEntry notificationEntry) {
        this.$entry = notificationEntry;
    }

    public final void onVisibilityChanged(int i) {
        this.$entry.setShelfIconVisible(i == 0);
    }
}
