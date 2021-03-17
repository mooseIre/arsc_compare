package com.android.systemui.statusbar.notification.icon;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: IconManager.kt */
public final class IconManager$entryListener$1 implements NotifCollectionListener {
    final /* synthetic */ IconManager this$0;

    IconManager$entryListener$1(IconManager iconManager) {
        this.this$0 = iconManager;
    }

    public void onEntryInit(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        notificationEntry.addOnSensitivityChangedListener(this.this$0.sensitivityListener);
    }

    public void onEntryCleanUp(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        notificationEntry.removeOnSensitivityChangedListener(this.this$0.sensitivityListener);
    }

    public void onRankingApplied() {
        for (NotificationEntry next : this.this$0.notifCollection.getAllNotifs()) {
            IconManager iconManager = this.this$0;
            Intrinsics.checkExpressionValueIsNotNull(next, "entry");
            boolean access$isImportantConversation = iconManager.isImportantConversation(next);
            IconPack icons = next.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons, "entry.icons");
            if (icons.getAreIconsAvailable()) {
                IconPack icons2 = next.getIcons();
                Intrinsics.checkExpressionValueIsNotNull(icons2, "entry.icons");
                if (access$isImportantConversation != icons2.isImportantConversation()) {
                    this.this$0.updateIconsSafe(next);
                }
            }
            IconPack icons3 = next.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons3, "entry.icons");
            icons3.setImportantConversation(access$isImportantConversation);
        }
    }
}
