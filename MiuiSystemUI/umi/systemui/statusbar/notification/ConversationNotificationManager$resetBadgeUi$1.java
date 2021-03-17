package com.android.systemui.statusbar.notification;

import android.view.View;
import com.android.systemui.statusbar.notification.row.NotificationContentView;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: ConversationNotifications.kt */
public final class ConversationNotificationManager$resetBadgeUi$1 extends Lambda implements Function1<NotificationContentView, Sequence<? extends View>> {
    public static final ConversationNotificationManager$resetBadgeUi$1 INSTANCE = new ConversationNotificationManager$resetBadgeUi$1();

    ConversationNotificationManager$resetBadgeUi$1() {
        super(1);
    }

    @NotNull
    public final Sequence<View> invoke(NotificationContentView notificationContentView) {
        Intrinsics.checkExpressionValueIsNotNull(notificationContentView, "layout");
        View[] allViews = notificationContentView.getAllViews();
        Intrinsics.checkExpressionValueIsNotNull(allViews, "layout.allViews");
        return ArraysKt___ArraysKt.asSequence(allViews);
    }
}
