package com.android.systemui;

import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import org.jetbrains.annotations.Nullable;

/* compiled from: CodeBlueService.kt */
public final class CodeBlueService$addNotifCollectionListener$1 implements NotifCollectionListener {
    final /* synthetic */ CodeBlueService this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    CodeBlueService$addNotifCollectionListener$1(CodeBlueService codeBlueService) {
        this.this$0 = codeBlueService;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
    public void onEntryBind(@Nullable NotificationEntry notificationEntry, @Nullable StatusBarNotification statusBarNotification) {
        ExpandedNotification sbn;
        this.this$0.setLatestNotificationPkgName((notificationEntry == null || (sbn = notificationEntry.getSbn()) == null) ? null : sbn.getPackageName());
    }
}
