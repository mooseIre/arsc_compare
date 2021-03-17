package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* compiled from: NotificationStackScrollLayoutExt.kt */
final class NotificationStackScrollLayoutExtKt$generateHeadsUpChildrenPositionAnimation$1 extends Lambda implements Function1<View, Boolean> {
    public static final NotificationStackScrollLayoutExtKt$generateHeadsUpChildrenPositionAnimation$1 INSTANCE = new NotificationStackScrollLayoutExtKt$generateHeadsUpChildrenPositionAnimation$1();

    NotificationStackScrollLayoutExtKt$generateHeadsUpChildrenPositionAnimation$1() {
        super(1);
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        return Boolean.valueOf(invoke((View) obj));
    }

    public final boolean invoke(View view) {
        return (view instanceof ExpandableNotificationRow) && ((ExpandableNotificationRow) view).isPinned();
    }
}
