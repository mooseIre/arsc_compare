package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MiuiNotificationSwipeCallback.kt */
public final class MiuiNotificationSwipeCallbackKt {
    /* access modifiers changed from: private */
    public static final boolean isPersistentNotificationRow(View view) {
        if (view instanceof ExpandableNotificationRow) {
            NotificationEntry entry = ((ExpandableNotificationRow) view).getEntry();
            Intrinsics.checkExpressionValueIsNotNull(entry, "view.entry");
            ExpandedNotification sbn = entry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "view.entry.sbn");
            if (sbn.isPersistent()) {
                return true;
            }
        }
        return false;
    }
}
