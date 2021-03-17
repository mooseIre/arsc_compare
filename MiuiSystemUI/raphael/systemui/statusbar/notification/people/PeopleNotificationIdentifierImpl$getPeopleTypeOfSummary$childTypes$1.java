package com.android.systemui.statusbar.notification.people;

import android.service.notification.NotificationListenerService;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

/* compiled from: PeopleNotificationIdentifier.kt */
final class PeopleNotificationIdentifierImpl$getPeopleTypeOfSummary$childTypes$1 extends Lambda implements Function1<NotificationEntry, Integer> {
    final /* synthetic */ PeopleNotificationIdentifierImpl this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    PeopleNotificationIdentifierImpl$getPeopleTypeOfSummary$childTypes$1(PeopleNotificationIdentifierImpl peopleNotificationIdentifierImpl) {
        super(1);
        this.this$0 = peopleNotificationIdentifierImpl;
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        return Integer.valueOf(invoke((NotificationEntry) obj));
    }

    public final int invoke(NotificationEntry notificationEntry) {
        PeopleNotificationIdentifierImpl peopleNotificationIdentifierImpl = this.this$0;
        Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "it");
        ExpandedNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "it.sbn");
        NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "it.ranking");
        return peopleNotificationIdentifierImpl.getPeopleNotificationType(sbn, ranking);
    }
}
