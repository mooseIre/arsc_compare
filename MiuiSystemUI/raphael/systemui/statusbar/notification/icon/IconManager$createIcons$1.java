package com.android.systemui.statusbar.notification.icon;

import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;

/* access modifiers changed from: package-private */
/* compiled from: IconManager.kt */
public final class IconManager$createIcons$1 implements StatusBarIconView.OnVisibilityChangedListener {
    final /* synthetic */ NotificationEntry $entry;

    IconManager$createIcons$1(NotificationEntry notificationEntry) {
        this.$entry = notificationEntry;
    }

    @Override // com.android.systemui.statusbar.StatusBarIconView.OnVisibilityChangedListener
    public final void onVisibilityChanged(int i) {
        this.$entry.setShelfIconVisible(i == 0);
    }
}
