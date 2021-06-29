package com.android.systemui.statusbar.notification;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import com.miui.systemui.SettingsManager;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationEntryManager.kt */
public final class MiuiNotificationEntryManager$notifListener$1 implements NotificationListener.NotificationHandler {
    final /* synthetic */ MiuiNotificationEntryManager this$0;

    @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
    public void onNotificationsInitialized() {
    }

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiNotificationEntryManager$notifListener$1(MiuiNotificationEntryManager miuiNotificationEntryManager) {
        this.this$0 = miuiNotificationEntryManager;
    }

    @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
    public void onNotificationPosted(@NotNull StatusBarNotification statusBarNotification, @NotNull NotificationListenerService.RankingMap rankingMap) {
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        Intrinsics.checkParameterIsNotNull(rankingMap, "rankingMap");
        boolean z = true;
        if (!FoldManager.Companion.shouldSuppressFold() && ((SettingsManager) Dependency.get(SettingsManager.class)).getNotifFold()) {
            NotificationUtil.setFold(statusBarNotification, FoldManager.Companion.isSbnFold$default(FoldManager.Companion, statusBarNotification, false, 0, 6, null));
        }
        if (!MiuiNotificationEntryManager.access$getActiveUnimportantNotifications$p(this.this$0).containsKey(statusBarNotification.getKey()) && !this.this$0.mActiveNotifications.containsKey(statusBarNotification.getKey())) {
            z = false;
        }
        if (z) {
            this.this$0.updateNotification(statusBarNotification, rankingMap);
        } else {
            this.this$0.addNotification(statusBarNotification, rankingMap);
        }
    }

    @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
    public void onNotificationRemoved(@NotNull StatusBarNotification statusBarNotification, @NotNull NotificationListenerService.RankingMap rankingMap, int i) {
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        Intrinsics.checkParameterIsNotNull(rankingMap, "rankingMap");
        this.this$0.removeNotification(statusBarNotification.getKey(), rankingMap, i);
    }

    @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
    public void onNotificationRankingUpdate(@NotNull NotificationListenerService.RankingMap rankingMap) {
        Intrinsics.checkParameterIsNotNull(rankingMap, "rankingMap");
        this.this$0.updateNotificationRanking(rankingMap);
    }
}
