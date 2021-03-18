package com.android.systemui.statusbar;

import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationInteractionTracker.kt */
public final class NotificationInteractionTracker implements NotifCollectionListener, NotificationInteractionListener {
    private final NotificationClickNotifier clicker;
    private final NotificationEntryManager entryManager;
    private final Map<String, Boolean> interactions = new LinkedHashMap();

    public NotificationInteractionTracker(@NotNull NotificationClickNotifier notificationClickNotifier, @NotNull NotificationEntryManager notificationEntryManager) {
        Intrinsics.checkParameterIsNotNull(notificationClickNotifier, "clicker");
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "entryManager");
        this.clicker = notificationClickNotifier;
        this.entryManager = notificationEntryManager;
        this.clicker.addNotificationInteractionListener(this);
        this.entryManager.addCollectionListener(this);
    }

    public final boolean hasUserInteractedWith(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Boolean bool = this.interactions.get(str);
        if (bool != null) {
            return bool.booleanValue();
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
    public void onEntryAdded(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        Map<String, Boolean> map = this.interactions;
        String key = notificationEntry.getKey();
        Intrinsics.checkExpressionValueIsNotNull(key, "entry.key");
        map.put(key, Boolean.FALSE);
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
    public void onEntryCleanUp(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        this.interactions.remove(notificationEntry.getKey());
    }

    @Override // com.android.systemui.statusbar.NotificationInteractionListener
    public void onNotificationInteraction(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        this.interactions.put(str, Boolean.TRUE);
    }
}
