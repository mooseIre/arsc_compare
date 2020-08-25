package com.android.systemui.miui.statusbar.analytics;

import com.android.systemui.miui.statusbar.ExpandedNotification;
import java.util.function.Function;

/* renamed from: com.android.systemui.miui.statusbar.analytics.-$$Lambda$NotificationStat$Dv6rsKP2EKuIIPOTf3NzGnBvqQ8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationStat$Dv6rsKP2EKuIIPOTf3NzGnBvqQ8 implements Function {
    public static final /* synthetic */ $$Lambda$NotificationStat$Dv6rsKP2EKuIIPOTf3NzGnBvqQ8 INSTANCE = new $$Lambda$NotificationStat$Dv6rsKP2EKuIIPOTf3NzGnBvqQ8();

    private /* synthetic */ $$Lambda$NotificationStat$Dv6rsKP2EKuIIPOTf3NzGnBvqQ8() {
    }

    public final Object apply(Object obj) {
        return Long.valueOf(((ExpandedNotification) obj).getPostTime());
    }
}
