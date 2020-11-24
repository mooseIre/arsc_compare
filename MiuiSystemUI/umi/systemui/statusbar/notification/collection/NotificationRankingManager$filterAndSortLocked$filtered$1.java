package com.android.systemui.statusbar.notification.collection;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationRankingManager.kt */
final /* synthetic */ class NotificationRankingManager$filterAndSortLocked$filtered$1 extends FunctionReference implements Function1<NotificationEntry, Boolean> {
    NotificationRankingManager$filterAndSortLocked$filtered$1(NotificationRankingManager notificationRankingManager) {
        super(1, notificationRankingManager);
    }

    public final String getName() {
        return "filter";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(NotificationRankingManager.class);
    }

    public final String getSignature() {
        return "filter(Lcom/android/systemui/statusbar/notification/collection/NotificationEntry;)Z";
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        return Boolean.valueOf(invoke((NotificationEntry) obj));
    }

    public final boolean invoke(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "p1");
        return ((NotificationRankingManager) this.receiver).filter(notificationEntry);
    }
}
