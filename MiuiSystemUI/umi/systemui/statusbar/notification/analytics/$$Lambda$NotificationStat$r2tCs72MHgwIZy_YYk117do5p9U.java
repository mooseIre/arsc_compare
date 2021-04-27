package com.android.systemui.statusbar.notification.analytics;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.Function;

/* renamed from: com.android.systemui.statusbar.notification.analytics.-$$Lambda$NotificationStat$r2tCs72MHgwIZy_YYk117do5p9U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationStat$r2tCs72MHgwIZy_YYk117do5p9U implements Function {
    public static final /* synthetic */ $$Lambda$NotificationStat$r2tCs72MHgwIZy_YYk117do5p9U INSTANCE = new $$Lambda$NotificationStat$r2tCs72MHgwIZy_YYk117do5p9U();

    private /* synthetic */ $$Lambda$NotificationStat$r2tCs72MHgwIZy_YYk117do5p9U() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((NotificationEntry) obj).getSbn();
    }
}
