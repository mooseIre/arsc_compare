package com.android.systemui.miui.statusbar.analytics;

import com.android.systemui.statusbar.ExpandableNotificationRow;
import java.util.function.Function;

/* renamed from: com.android.systemui.miui.statusbar.analytics.-$$Lambda$NotificationStat$v9xbYIxdp1VCGQLT5IUgWvjzpSY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationStat$v9xbYIxdp1VCGQLT5IUgWvjzpSY implements Function {
    public static final /* synthetic */ $$Lambda$NotificationStat$v9xbYIxdp1VCGQLT5IUgWvjzpSY INSTANCE = new $$Lambda$NotificationStat$v9xbYIxdp1VCGQLT5IUgWvjzpSY();

    private /* synthetic */ $$Lambda$NotificationStat$v9xbYIxdp1VCGQLT5IUgWvjzpSY() {
    }

    public final Object apply(Object obj) {
        return Long.valueOf(((ExpandableNotificationRow) obj).getEntry().notification.getPostTime());
    }
}
