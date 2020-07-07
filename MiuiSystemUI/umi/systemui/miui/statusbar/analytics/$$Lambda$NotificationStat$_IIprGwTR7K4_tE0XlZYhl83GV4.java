package com.android.systemui.miui.statusbar.analytics;

import com.android.systemui.statusbar.NotificationData;
import java.util.function.Function;

/* renamed from: com.android.systemui.miui.statusbar.analytics.-$$Lambda$NotificationStat$_IIprGwTR7K4_tE0XlZYhl83GV4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationStat$_IIprGwTR7K4_tE0XlZYhl83GV4 implements Function {
    public static final /* synthetic */ $$Lambda$NotificationStat$_IIprGwTR7K4_tE0XlZYhl83GV4 INSTANCE = new $$Lambda$NotificationStat$_IIprGwTR7K4_tE0XlZYhl83GV4();

    private /* synthetic */ $$Lambda$NotificationStat$_IIprGwTR7K4_tE0XlZYhl83GV4() {
    }

    public final Object apply(Object obj) {
        return ((NotificationData.Entry) obj).notification;
    }
}
