package com.android.systemui.statusbar.notification.icon;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.ConfigurationController;
import java.util.Collection;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MiuiIconManager.kt */
public final class MiuiIconManager$configurationListener$1 implements ConfigurationController.ConfigurationListener {
    final /* synthetic */ MiuiIconManager this$0;

    MiuiIconManager$configurationListener$1(MiuiIconManager miuiIconManager) {
        this.this$0 = miuiIconManager;
    }

    public void onMiuiThemeChanged(boolean z) {
        Collection<NotificationEntry> allNotifs = this.this$0.notifCollection.getAllNotifs();
        Intrinsics.checkExpressionValueIsNotNull(allNotifs, "notifCollection.allNotifs");
        for (NotificationEntry notificationEntry : allNotifs) {
            MiuiIconManager miuiIconManager = this.this$0;
            Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "it");
            miuiIconManager.updateIconsSafe(notificationEntry);
        }
    }
}
