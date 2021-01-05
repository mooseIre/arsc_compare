package com.android.systemui.statusbar.notification;

import android.view.View;
import com.android.internal.widget.ConversationLayout;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.Nullable;

/* compiled from: ConversationNotifications.kt */
final class ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$2 extends Lambda implements Function1<View, ConversationLayout> {
    public static final ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$2 INSTANCE = new ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$2();

    ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$2() {
        super(1);
    }

    @Nullable
    public final ConversationLayout invoke(View view) {
        if (!(view instanceof ConversationLayout)) {
            view = null;
        }
        return (ConversationLayout) view;
    }
}
