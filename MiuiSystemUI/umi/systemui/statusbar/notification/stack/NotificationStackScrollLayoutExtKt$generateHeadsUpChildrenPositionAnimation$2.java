package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import kotlin.TypeCastException;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: NotificationStackScrollLayoutExt.kt */
public final class NotificationStackScrollLayoutExtKt$generateHeadsUpChildrenPositionAnimation$2 extends Lambda implements Function1<View, ExpandableView> {
    public static final NotificationStackScrollLayoutExtKt$generateHeadsUpChildrenPositionAnimation$2 INSTANCE = new NotificationStackScrollLayoutExtKt$generateHeadsUpChildrenPositionAnimation$2();

    NotificationStackScrollLayoutExtKt$generateHeadsUpChildrenPositionAnimation$2() {
        super(1);
    }

    @NotNull
    public final ExpandableView invoke(View view) {
        if (view != null) {
            return (ExpandableView) view;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.ExpandableView");
    }
}
