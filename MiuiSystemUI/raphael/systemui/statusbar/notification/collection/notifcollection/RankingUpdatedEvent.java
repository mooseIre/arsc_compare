package com.android.systemui.statusbar.notification.collection.notifcollection;

import android.service.notification.NotificationListenerService;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotifEvent.kt */
public final class RankingUpdatedEvent extends NotifEvent {
    @NotNull
    private final NotificationListenerService.RankingMap rankingMap;

    public boolean equals(@Nullable Object obj) {
        if (this != obj) {
            return (obj instanceof RankingUpdatedEvent) && Intrinsics.areEqual(this.rankingMap, ((RankingUpdatedEvent) obj).rankingMap);
        }
        return true;
    }

    public int hashCode() {
        NotificationListenerService.RankingMap rankingMap2 = this.rankingMap;
        if (rankingMap2 != null) {
            return rankingMap2.hashCode();
        }
        return 0;
    }

    @NotNull
    public String toString() {
        return "RankingUpdatedEvent(rankingMap=" + this.rankingMap + ")";
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RankingUpdatedEvent(@NotNull NotificationListenerService.RankingMap rankingMap2) {
        super(null);
        Intrinsics.checkParameterIsNotNull(rankingMap2, "rankingMap");
        this.rankingMap = rankingMap2;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifEvent
    public void dispatchToListener(@NotNull NotifCollectionListener notifCollectionListener) {
        Intrinsics.checkParameterIsNotNull(notifCollectionListener, "listener");
        notifCollectionListener.onRankingUpdate(this.rankingMap);
    }
}
