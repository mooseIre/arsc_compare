package com.android.systemui.miui.statusbar.analytics;

import com.android.systemui.statusbar.NotificationData;
import java.util.function.Function;

/* renamed from: com.android.systemui.miui.statusbar.analytics.-$$Lambda$SystemUIStat$4y-QkEBi0C_T1qgydx0AUvJ0g7c  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SystemUIStat$4yQkEBi0C_T1qgydx0AUvJ0g7c implements Function {
    public static final /* synthetic */ $$Lambda$SystemUIStat$4yQkEBi0C_T1qgydx0AUvJ0g7c INSTANCE = new $$Lambda$SystemUIStat$4yQkEBi0C_T1qgydx0AUvJ0g7c();

    private /* synthetic */ $$Lambda$SystemUIStat$4yQkEBi0C_T1qgydx0AUvJ0g7c() {
    }

    public final Object apply(Object obj) {
        return ((NotificationData.Entry) obj).notification;
    }
}
