package com.android.systemui.statusbar.notification.collection.notifcollection;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotifEvent.kt */
public final class RankingAppliedEvent extends NotifEvent {
    public RankingAppliedEvent() {
        super(null);
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifEvent
    public void dispatchToListener(@NotNull NotifCollectionListener notifCollectionListener) {
        Intrinsics.checkParameterIsNotNull(notifCollectionListener, "listener");
        notifCollectionListener.onRankingApplied();
    }
}
