package com.android.systemui.statusbar.phone;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$Qz8oyL0qAMzuJuwPLHs4cVCa7kg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$StatusBar$Qz8oyL0qAMzuJuwPLHs4cVCa7kg implements Consumer {
    public static final /* synthetic */ $$Lambda$StatusBar$Qz8oyL0qAMzuJuwPLHs4cVCa7kg INSTANCE = new $$Lambda$StatusBar$Qz8oyL0qAMzuJuwPLHs4cVCa7kg();

    private /* synthetic */ $$Lambda$StatusBar$Qz8oyL0qAMzuJuwPLHs4cVCa7kg() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        StatusBar.lambda$maybeEscalateHeadsUp$14((NotificationEntry) obj);
    }
}
