package com.android.systemui.statusbar.phone;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.Function;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$NotificationIconAreaController$ujxUr-qwlryo8PHBzga56kRshsA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationIconAreaController$ujxUrqwlryo8PHBzga56kRshsA implements Function {
    public static final /* synthetic */ $$Lambda$NotificationIconAreaController$ujxUrqwlryo8PHBzga56kRshsA INSTANCE = new $$Lambda$NotificationIconAreaController$ujxUrqwlryo8PHBzga56kRshsA();

    private /* synthetic */ $$Lambda$NotificationIconAreaController$ujxUrqwlryo8PHBzga56kRshsA() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((NotificationEntry) obj).getIcons().getStatusBarIcon();
    }
}
