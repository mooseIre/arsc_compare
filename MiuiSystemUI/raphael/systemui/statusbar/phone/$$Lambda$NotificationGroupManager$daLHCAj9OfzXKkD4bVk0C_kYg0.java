package com.android.systemui.statusbar.phone;

import com.android.systemui.statusbar.NotificationData;
import java.util.function.Predicate;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$NotificationGroupManager$daLHCA-j9OfzXKkD4bVk0C_kYg0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationGroupManager$daLHCAj9OfzXKkD4bVk0C_kYg0 implements Predicate {
    public static final /* synthetic */ $$Lambda$NotificationGroupManager$daLHCAj9OfzXKkD4bVk0C_kYg0 INSTANCE = new $$Lambda$NotificationGroupManager$daLHCAj9OfzXKkD4bVk0C_kYg0();

    private /* synthetic */ $$Lambda$NotificationGroupManager$daLHCAj9OfzXKkD4bVk0C_kYg0() {
    }

    public final boolean test(Object obj) {
        return NotificationGroupManager.lambda$hasMediaOrCustomChildren$0((NotificationData.Entry) obj);
    }
}
