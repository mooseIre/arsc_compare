package com.android.systemui.statusbar.notification.row;

import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.policy.AppMiniWindowManager;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: MiuiExpandableNotificationRow.kt */
final class MiuiExpandableNotificationRow$mAppMiniWindowManager$2 extends Lambda implements Function0<AppMiniWindowManager> {
    public static final MiuiExpandableNotificationRow$mAppMiniWindowManager$2 INSTANCE = new MiuiExpandableNotificationRow$mAppMiniWindowManager$2();

    MiuiExpandableNotificationRow$mAppMiniWindowManager$2() {
        super(0);
    }

    @Override // kotlin.jvm.functions.Function0
    public final AppMiniWindowManager invoke() {
        return (AppMiniWindowManager) Dependency.get(AppMiniWindowManager.class);
    }
}
