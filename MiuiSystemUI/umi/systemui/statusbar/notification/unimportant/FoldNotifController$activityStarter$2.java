package com.android.systemui.statusbar.notification.unimportant;

import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* access modifiers changed from: package-private */
/* compiled from: FoldNotifController.kt */
public final class FoldNotifController$activityStarter$2 extends Lambda implements Function0<ActivityStarter> {
    public static final FoldNotifController$activityStarter$2 INSTANCE = new FoldNotifController$activityStarter$2();

    FoldNotifController$activityStarter$2() {
        super(0);
    }

    @Override // kotlin.jvm.functions.Function0
    public final ActivityStarter invoke() {
        return (ActivityStarter) Dependency.get(ActivityStarter.class);
    }
}
