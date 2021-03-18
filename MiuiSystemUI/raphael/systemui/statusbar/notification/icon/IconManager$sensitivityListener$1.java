package com.android.systemui.statusbar.notification.icon;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: IconManager.kt */
public final class IconManager$sensitivityListener$1 implements NotificationEntry.OnSensitivityChangedListener {
    final /* synthetic */ IconManager this$0;

    IconManager$sensitivityListener$1(IconManager iconManager) {
        this.this$0 = iconManager;
    }

    @Override // com.android.systemui.statusbar.notification.collection.NotificationEntry.OnSensitivityChangedListener
    public final void onSensitivityChanged(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        this.this$0.updateIconsSafe(notificationEntry);
    }
}
