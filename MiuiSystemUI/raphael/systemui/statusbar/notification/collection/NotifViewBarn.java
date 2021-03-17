package com.android.systemui.statusbar.notification.collection;

import android.view.textclassifier.Log;
import com.android.systemui.statusbar.notification.stack.NotificationListItem;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotifViewBarn.kt */
public final class NotifViewBarn {
    private final boolean DEBUG;
    private final Map<String, NotificationListItem> rowMap = new LinkedHashMap();

    @NotNull
    public final NotificationListItem requireView(@NotNull ListEntry listEntry) {
        Intrinsics.checkParameterIsNotNull(listEntry, "forEntry");
        if (this.DEBUG) {
            Log.d("NotifViewBarn", "requireView: " + listEntry + ".key");
        }
        NotificationListItem notificationListItem = this.rowMap.get(listEntry.getKey());
        if (notificationListItem != null) {
            return notificationListItem;
        }
        throw new IllegalStateException("No view has been registered for entry: " + listEntry);
    }

    public final void registerViewForEntry(@NotNull ListEntry listEntry, @NotNull NotificationListItem notificationListItem) {
        Intrinsics.checkParameterIsNotNull(listEntry, "entry");
        Intrinsics.checkParameterIsNotNull(notificationListItem, "view");
        if (this.DEBUG) {
            Log.d("NotifViewBarn", "registerViewForEntry: " + listEntry + ".key");
        }
        Map<String, NotificationListItem> map = this.rowMap;
        String key = listEntry.getKey();
        Intrinsics.checkExpressionValueIsNotNull(key, "entry.key");
        map.put(key, notificationListItem);
    }

    public final void removeViewForEntry(@NotNull ListEntry listEntry) {
        Intrinsics.checkParameterIsNotNull(listEntry, "entry");
        if (this.DEBUG) {
            Log.d("NotifViewBarn", "removeViewForEntry: " + listEntry + ".key");
        }
        this.rowMap.remove(listEntry.getKey());
    }
}
