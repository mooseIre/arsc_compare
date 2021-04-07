package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.Predicate;

/* renamed from: com.android.systemui.statusbar.notification.-$$Lambda$NotificationEntryManager$XDKdt5lsVwxQcyNGVC2kL83OA_U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationEntryManager$XDKdt5lsVwxQcyNGVC2kL83OA_U implements Predicate {
    public static final /* synthetic */ $$Lambda$NotificationEntryManager$XDKdt5lsVwxQcyNGVC2kL83OA_U INSTANCE = new $$Lambda$NotificationEntryManager$XDKdt5lsVwxQcyNGVC2kL83OA_U();

    private /* synthetic */ $$Lambda$NotificationEntryManager$XDKdt5lsVwxQcyNGVC2kL83OA_U() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((NotificationEntry) obj).getSbn().isImportant();
    }
}
