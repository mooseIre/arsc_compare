package com.android.systemui.statusbar.notification.collection;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: NotificationRankingManager.kt */
public final /* synthetic */ class NotificationRankingManager$filterAndSortLocked$filtered$1 extends FunctionReference implements Function1<NotificationEntry, Boolean> {
    NotificationRankingManager$filterAndSortLocked$filtered$1(NotificationRankingManager notificationRankingManager) {
        super(1, notificationRankingManager);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "filter";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(NotificationRankingManager.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "filter(Lcom/android/systemui/statusbar/notification/collection/NotificationEntry;)Z";
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(NotificationEntry notificationEntry) {
        return Boolean.valueOf(invoke(notificationEntry));
    }

    public final boolean invoke(@Nullable NotificationEntry notificationEntry) {
        return ((NotificationRankingManager) this.receiver).filter(notificationEntry);
    }
}
