package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.unimportant.FoldManager;

/* access modifiers changed from: package-private */
/* compiled from: MiuiNotificationEntryManager.kt */
public final class MiuiNotificationEntryManager$recheckFoldNotificationDelayed$1 implements Runnable {
    final /* synthetic */ MiuiNotificationEntryManager this$0;

    MiuiNotificationEntryManager$recheckFoldNotificationDelayed$1(MiuiNotificationEntryManager miuiNotificationEntryManager) {
        this.this$0 = miuiNotificationEntryManager;
    }

    public final void run() {
        MiuiNotificationEntryManager miuiNotificationEntryManager = this.this$0;
        MiuiNotificationEntryManager.updateFoldRankingAndSort$default(miuiNotificationEntryManager, MiuiNotificationEntryManager.access$getRankingManager$p(miuiNotificationEntryManager).getRankingMap(), "onMiuiThemeChanged", false, 4, null);
        FoldManager.Companion.checkFoldNotification(MiuiNotificationEntryManager.shouldShow$default(this.this$0, 0, 1, null), this.this$0.getCurrentUser());
    }
}
