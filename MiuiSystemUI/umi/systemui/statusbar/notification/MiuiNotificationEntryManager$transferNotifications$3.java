package com.android.systemui.statusbar.notification;

import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import com.miui.systemui.SettingsManager;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationEntryManager.kt */
final class MiuiNotificationEntryManager$transferNotifications$3 extends Lambda implements Function1<NotificationEntry, Boolean> {
    final /* synthetic */ boolean $foldOverride;
    final /* synthetic */ int $importance;
    final /* synthetic */ boolean $isUnimportant;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiNotificationEntryManager$transferNotifications$3(boolean z, int i, boolean z2) {
        super(1);
        this.$foldOverride = z;
        this.$importance = i;
        this.$isUnimportant = z2;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(NotificationEntry notificationEntry) {
        return Boolean.valueOf(invoke(notificationEntry));
    }

    public final boolean invoke(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "it");
        FoldManager.Companion companion = FoldManager.Companion;
        ExpandedNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "it.sbn");
        boolean isSbnFold = companion.isSbnFold(sbn, this.$foldOverride, this.$importance);
        boolean notifFold = ((SettingsManager) Dependency.get(SettingsManager.class)).getNotifFold();
        if (this.$importance == 0) {
            if (!this.$isUnimportant || !isSbnFold) {
                if (this.$isUnimportant) {
                    return false;
                }
                if (isSbnFold && notifFold) {
                    return false;
                }
            }
        } else if (this.$isUnimportant && !isSbnFold) {
            return false;
        }
        return true;
    }
}
