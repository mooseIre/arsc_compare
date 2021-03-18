package com.android.systemui.statusbar.notification.collection;

import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: TargetSdkResolver.kt */
public final class TargetSdkResolver$initialize$1 implements NotifCollectionListener {
    final /* synthetic */ TargetSdkResolver this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    TargetSdkResolver$initialize$1(TargetSdkResolver targetSdkResolver) {
        this.this$0 = targetSdkResolver;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
    public void onEntryBind(@NotNull NotificationEntry notificationEntry, @NotNull StatusBarNotification statusBarNotification) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        notificationEntry.targetSdk = this.this$0.resolveNotificationSdk(statusBarNotification);
    }
}
