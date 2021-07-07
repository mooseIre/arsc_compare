package com.android.systemui.statusbar.notification;

import android.service.notification.NotificationListenerService;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MiuiNotificationEntryManager.kt */
final class MiuiNotificationEntryManager$onMiuiThemeChanged$1 implements Runnable {
    final /* synthetic */ MiuiNotificationEntryManager this$0;

    MiuiNotificationEntryManager$onMiuiThemeChanged$1(MiuiNotificationEntryManager miuiNotificationEntryManager) {
        this.this$0 = miuiNotificationEntryManager;
    }

    public final void run() {
        MiuiNotificationEntryManager miuiNotificationEntryManager = this.this$0;
        NotificationListenerService.RankingMap rankingMap = MiuiNotificationEntryManager.access$getRankingManager$p(miuiNotificationEntryManager).getRankingMap();
        if (rankingMap != null) {
            MiuiNotificationEntryManager.updateFoldRankingAndSort$default(miuiNotificationEntryManager, rankingMap, "onMiuiThemeChanged", false, 4, null);
            FoldManager.Companion.checkFoldNotification(MiuiNotificationEntryManager.shouldShow$default(this.this$0, 0, 1, null), this.this$0.getCurrentUser());
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }
}
