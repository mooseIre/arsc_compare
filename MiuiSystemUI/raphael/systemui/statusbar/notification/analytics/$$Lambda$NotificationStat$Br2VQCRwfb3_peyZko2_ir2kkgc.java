package com.android.systemui.statusbar.notification.analytics;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.Function;

/* renamed from: com.android.systemui.statusbar.notification.analytics.-$$Lambda$NotificationStat$Br2VQCRwfb3_peyZko2_ir2kkgc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationStat$Br2VQCRwfb3_peyZko2_ir2kkgc implements Function {
    public static final /* synthetic */ $$Lambda$NotificationStat$Br2VQCRwfb3_peyZko2_ir2kkgc INSTANCE = new $$Lambda$NotificationStat$Br2VQCRwfb3_peyZko2_ir2kkgc();

    private /* synthetic */ $$Lambda$NotificationStat$Br2VQCRwfb3_peyZko2_ir2kkgc() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Long.valueOf(((NotificationEntry) obj).getSbn().getPostTime());
    }
}
