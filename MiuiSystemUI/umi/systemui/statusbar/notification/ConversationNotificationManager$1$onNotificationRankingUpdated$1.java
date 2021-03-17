package com.android.systemui.statusbar.notification;

import android.view.View;
import com.android.systemui.statusbar.notification.row.NotificationContentView;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt__SequencesKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: ConversationNotifications.kt */
final class ConversationNotificationManager$1$onNotificationRankingUpdated$1 extends Lambda implements Function1<NotificationContentView, Sequence<? extends View>> {
    public static final ConversationNotificationManager$1$onNotificationRankingUpdated$1 INSTANCE = new ConversationNotificationManager$1$onNotificationRankingUpdated$1();

    ConversationNotificationManager$1$onNotificationRankingUpdated$1() {
        super(1);
    }

    @NotNull
    public final Sequence<View> invoke(@NotNull NotificationContentView notificationContentView) {
        Intrinsics.checkParameterIsNotNull(notificationContentView, "view");
        return SequencesKt__SequencesKt.sequenceOf(notificationContentView.getContractedChild(), notificationContentView.getExpandedChild(), notificationContentView.getHeadsUpChild());
    }
}
