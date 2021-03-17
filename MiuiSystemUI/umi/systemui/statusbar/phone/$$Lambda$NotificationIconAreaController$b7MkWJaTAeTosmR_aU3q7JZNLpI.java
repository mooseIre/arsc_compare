package com.android.systemui.statusbar.phone;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.Function;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$NotificationIconAreaController$b7MkWJaTAeTosmR_aU3q7JZNLpI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationIconAreaController$b7MkWJaTAeTosmR_aU3q7JZNLpI implements Function {
    public static final /* synthetic */ $$Lambda$NotificationIconAreaController$b7MkWJaTAeTosmR_aU3q7JZNLpI INSTANCE = new $$Lambda$NotificationIconAreaController$b7MkWJaTAeTosmR_aU3q7JZNLpI();

    private /* synthetic */ $$Lambda$NotificationIconAreaController$b7MkWJaTAeTosmR_aU3q7JZNLpI() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((NotificationEntry) obj).getIcons().getAodIcon();
    }
}
