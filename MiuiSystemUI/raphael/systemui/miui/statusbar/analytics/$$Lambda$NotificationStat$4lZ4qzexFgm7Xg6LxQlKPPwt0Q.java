package com.android.systemui.miui.statusbar.analytics;

import com.android.systemui.statusbar.NotificationData;
import java.util.function.Predicate;

/* renamed from: com.android.systemui.miui.statusbar.analytics.-$$Lambda$NotificationStat$4lZ4qzexFgm7Xg6LxQlKPPwt-0Q  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationStat$4lZ4qzexFgm7Xg6LxQlKPPwt0Q implements Predicate {
    public static final /* synthetic */ $$Lambda$NotificationStat$4lZ4qzexFgm7Xg6LxQlKPPwt0Q INSTANCE = new $$Lambda$NotificationStat$4lZ4qzexFgm7Xg6LxQlKPPwt0Q();

    private /* synthetic */ $$Lambda$NotificationStat$4lZ4qzexFgm7Xg6LxQlKPPwt0Q() {
    }

    public final boolean test(Object obj) {
        return NotificationStat.lambda$handleNotiVisibleEvent$3((NotificationData.Entry) obj);
    }
}
