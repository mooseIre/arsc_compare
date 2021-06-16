package com.android.systemui.statusbar.phone;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.Function;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$NotificationIconAreaController$00FvnUlAy0_w9h_-AZ6H93V6ggw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationIconAreaController$00FvnUlAy0_w9h_AZ6H93V6ggw implements Function {
    public static final /* synthetic */ $$Lambda$NotificationIconAreaController$00FvnUlAy0_w9h_AZ6H93V6ggw INSTANCE = new $$Lambda$NotificationIconAreaController$00FvnUlAy0_w9h_AZ6H93V6ggw();

    private /* synthetic */ $$Lambda$NotificationIconAreaController$00FvnUlAy0_w9h_AZ6H93V6ggw() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((NotificationEntry) obj).getIcons().getStatusBarIcon();
    }
}
