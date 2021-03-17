package com.android.systemui.statusbar.notification.icon;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: IconManager.kt */
public final class IconManager$entryListener$1 implements NotifCollectionListener {
    final /* synthetic */ IconManager this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    IconManager$entryListener$1(IconManager iconManager) {
        this.this$0 = iconManager;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
    public void onEntryInit(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        notificationEntry.addOnSensitivityChangedListener(this.this$0.sensitivityListener);
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
    public void onEntryCleanUp(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        notificationEntry.removeOnSensitivityChangedListener(this.this$0.sensitivityListener);
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
    public void onRankingApplied() {
        for (NotificationEntry notificationEntry : this.this$0.notifCollection.getAllNotifs()) {
            IconManager iconManager = this.this$0;
            Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "entry");
            boolean z = iconManager.isImportantConversation(notificationEntry);
            IconPack icons = notificationEntry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons, "entry.icons");
            if (icons.getAreIconsAvailable()) {
                IconPack icons2 = notificationEntry.getIcons();
                Intrinsics.checkExpressionValueIsNotNull(icons2, "entry.icons");
                if (z != icons2.isImportantConversation()) {
                    this.this$0.updateIconsSafe(notificationEntry);
                }
            }
            IconPack icons3 = notificationEntry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons3, "entry.icons");
            icons3.setImportantConversation(z);
        }
    }
}
