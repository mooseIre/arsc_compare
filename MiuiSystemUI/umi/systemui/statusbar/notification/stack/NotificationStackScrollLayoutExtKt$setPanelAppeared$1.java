package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* access modifiers changed from: package-private */
/* compiled from: NotificationStackScrollLayoutExt.kt */
public final class NotificationStackScrollLayoutExtKt$setPanelAppeared$1 extends Lambda implements Function1<View, Boolean> {
    public static final NotificationStackScrollLayoutExtKt$setPanelAppeared$1 INSTANCE = new NotificationStackScrollLayoutExtKt$setPanelAppeared$1();

    NotificationStackScrollLayoutExtKt$setPanelAppeared$1() {
        super(1);
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(View view) {
        return Boolean.valueOf(invoke(view));
    }

    public final boolean invoke(View view) {
        return view instanceof ExpandableNotificationRow;
    }
}
