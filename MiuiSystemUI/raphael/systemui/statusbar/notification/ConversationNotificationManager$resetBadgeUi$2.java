package com.android.systemui.statusbar.notification;

import android.view.View;
import com.android.internal.widget.ConversationLayout;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: ConversationNotifications.kt */
public final class ConversationNotificationManager$resetBadgeUi$2 extends Lambda implements Function1<View, ConversationLayout> {
    public static final ConversationNotificationManager$resetBadgeUi$2 INSTANCE = new ConversationNotificationManager$resetBadgeUi$2();

    ConversationNotificationManager$resetBadgeUi$2() {
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
