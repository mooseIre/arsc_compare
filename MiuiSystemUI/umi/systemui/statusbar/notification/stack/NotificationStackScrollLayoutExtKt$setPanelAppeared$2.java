package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import kotlin.TypeCastException;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: NotificationStackScrollLayoutExt.kt */
public final class NotificationStackScrollLayoutExtKt$setPanelAppeared$2 extends Lambda implements Function1<View, ExpandableNotificationRow> {
    public static final NotificationStackScrollLayoutExtKt$setPanelAppeared$2 INSTANCE = new NotificationStackScrollLayoutExtKt$setPanelAppeared$2();

    NotificationStackScrollLayoutExtKt$setPanelAppeared$2() {
        super(1);
    }

    @NotNull
    public final ExpandableNotificationRow invoke(View view) {
        if (view != null) {
            return (ExpandableNotificationRow) view;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.ExpandableNotificationRow");
    }
}
