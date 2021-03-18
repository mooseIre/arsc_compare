package com.android.systemui.statusbar.notification.policy;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.statusbar.notification.policy.-$$Lambda$NotificationAlertController$cnPeo-2J1MJMxlulxtwNq2qNkNE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationAlertController$cnPeo2J1MJMxlulxtwNq2qNkNE implements Consumer {
    public static final /* synthetic */ $$Lambda$NotificationAlertController$cnPeo2J1MJMxlulxtwNq2qNkNE INSTANCE = new $$Lambda$NotificationAlertController$cnPeo2J1MJMxlulxtwNq2qNkNE();

    private /* synthetic */ $$Lambda$NotificationAlertController$cnPeo2J1MJMxlulxtwNq2qNkNE() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((NotificationEntry) obj).getSbn().setHasShownAfterUnlock(true);
    }
}
