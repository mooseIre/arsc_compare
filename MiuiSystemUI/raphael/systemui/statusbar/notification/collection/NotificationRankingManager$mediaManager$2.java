package com.android.systemui.statusbar.notification.collection;

import com.android.systemui.statusbar.NotificationMediaManager;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: NotificationRankingManager.kt */
final class NotificationRankingManager$mediaManager$2 extends Lambda implements Function0<NotificationMediaManager> {
    final /* synthetic */ NotificationRankingManager this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    NotificationRankingManager$mediaManager$2(NotificationRankingManager notificationRankingManager) {
        super(0);
        this.this$0 = notificationRankingManager;
    }

    public final NotificationMediaManager invoke() {
        return (NotificationMediaManager) this.this$0.mediaManagerLazy.get();
    }
}
