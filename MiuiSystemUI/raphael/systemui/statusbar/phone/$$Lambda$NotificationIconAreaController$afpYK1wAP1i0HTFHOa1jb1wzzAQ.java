package com.android.systemui.statusbar.phone;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.Function;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$NotificationIconAreaController$afpYK1wAP1i0HTFHOa1jb1wzzAQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationIconAreaController$afpYK1wAP1i0HTFHOa1jb1wzzAQ implements Function {
    public static final /* synthetic */ $$Lambda$NotificationIconAreaController$afpYK1wAP1i0HTFHOa1jb1wzzAQ INSTANCE = new $$Lambda$NotificationIconAreaController$afpYK1wAP1i0HTFHOa1jb1wzzAQ();

    private /* synthetic */ $$Lambda$NotificationIconAreaController$afpYK1wAP1i0HTFHOa1jb1wzzAQ() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((NotificationEntry) obj).getIcons().getShelfIcon();
    }
}
