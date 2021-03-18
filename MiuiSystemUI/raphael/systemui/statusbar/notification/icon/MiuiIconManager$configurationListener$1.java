package com.android.systemui.statusbar.notification.icon;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.ConfigurationController;
import java.util.Collection;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MiuiIconManager.kt */
public final class MiuiIconManager$configurationListener$1 implements ConfigurationController.ConfigurationListener {
    final /* synthetic */ MiuiIconManager this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiIconManager$configurationListener$1(MiuiIconManager miuiIconManager) {
        this.this$0 = miuiIconManager;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onMiuiThemeChanged(boolean z) {
        Collection<NotificationEntry> allNotifs = this.this$0.notifCollection.getAllNotifs();
        Intrinsics.checkExpressionValueIsNotNull(allNotifs, "notifCollection.allNotifs");
        for (T t : allNotifs) {
            MiuiIconManager miuiIconManager = this.this$0;
            Intrinsics.checkExpressionValueIsNotNull(t, "it");
            miuiIconManager.updateIconsSafe(t);
        }
    }
}
