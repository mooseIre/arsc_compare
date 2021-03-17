package com.android.systemui.statusbar.notification.people;

import android.app.NotificationChannel;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PeopleNotificationIdentifier.kt */
public final class PeopleNotificationIdentifierImpl implements PeopleNotificationIdentifier {
    private final NotificationGroupManager groupManager;
    private final NotificationPersonExtractor personExtractor;

    public PeopleNotificationIdentifierImpl(@NotNull NotificationPersonExtractor notificationPersonExtractor, @NotNull NotificationGroupManager notificationGroupManager) {
        Intrinsics.checkParameterIsNotNull(notificationPersonExtractor, "personExtractor");
        Intrinsics.checkParameterIsNotNull(notificationGroupManager, "groupManager");
        this.personExtractor = notificationPersonExtractor;
        this.groupManager = notificationGroupManager;
    }

    @Override // com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier
    public int getPeopleNotificationType(@NotNull StatusBarNotification statusBarNotification, @NotNull NotificationListenerService.Ranking ranking) {
        int upperBound;
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        Intrinsics.checkParameterIsNotNull(ranking, "ranking");
        int personTypeInfo = getPersonTypeInfo(ranking);
        if (personTypeInfo == 3 || (upperBound = upperBound(personTypeInfo, extractPersonTypeInfo(statusBarNotification))) == 3) {
            return 3;
        }
        return upperBound(upperBound, getPeopleTypeOfSummary(statusBarNotification));
    }

    @Override // com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier
    public int compareTo(int i, int i2) {
        return Intrinsics.compare(i2, i);
    }

    private final int upperBound(int i, int i2) {
        return Math.max(i, i2);
    }

    private final int getPersonTypeInfo(@NotNull NotificationListenerService.Ranking ranking) {
        if (!ranking.isConversation()) {
            return 0;
        }
        if (ranking.getShortcutInfo() == null) {
            return 1;
        }
        NotificationChannel channel = ranking.getChannel();
        return (channel == null || !channel.isImportantConversation()) ? 2 : 3;
    }

    private final int extractPersonTypeInfo(StatusBarNotification statusBarNotification) {
        return this.personExtractor.isPersonNotification(statusBarNotification) ? 1 : 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final int getPeopleTypeOfSummary(android.service.notification.StatusBarNotification r3) {
        /*
            r2 = this;
            com.android.systemui.statusbar.phone.NotificationGroupManager r0 = r2.groupManager
            boolean r0 = r0.isSummaryOfGroup(r3)
            r1 = 0
            if (r0 != 0) goto L_0x000a
            return r1
        L_0x000a:
            com.android.systemui.statusbar.phone.NotificationGroupManager r0 = r2.groupManager
            java.util.ArrayList r3 = r0.getChildren(r3)
            if (r3 == 0) goto L_0x003e
            kotlin.sequences.Sequence r3 = kotlin.collections.CollectionsKt.asSequence(r3)
            if (r3 == 0) goto L_0x003e
            com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifierImpl$getPeopleTypeOfSummary$childTypes$1 r0 = new com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifierImpl$getPeopleTypeOfSummary$childTypes$1
            r0.<init>(r2)
            kotlin.sequences.Sequence r3 = kotlin.sequences.SequencesKt.map(r3, r0)
            if (r3 == 0) goto L_0x003e
            java.util.Iterator r3 = r3.iterator()
        L_0x0027:
            boolean r0 = r3.hasNext()
            if (r0 == 0) goto L_0x003e
            java.lang.Object r0 = r3.next()
            java.lang.Number r0 = (java.lang.Number) r0
            int r0 = r0.intValue()
            int r1 = r2.upperBound(r1, r0)
            r0 = 3
            if (r1 != r0) goto L_0x0027
        L_0x003e:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifierImpl.getPeopleTypeOfSummary(android.service.notification.StatusBarNotification):int");
    }
}
