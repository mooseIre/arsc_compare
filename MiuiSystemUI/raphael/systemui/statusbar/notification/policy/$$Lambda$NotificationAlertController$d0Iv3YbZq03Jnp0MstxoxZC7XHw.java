package com.android.systemui.statusbar.notification.policy;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.statusbar.notification.policy.-$$Lambda$NotificationAlertController$d0Iv3YbZq03Jnp0MstxoxZC7XHw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationAlertController$d0Iv3YbZq03Jnp0MstxoxZC7XHw implements Consumer {
    public static final /* synthetic */ $$Lambda$NotificationAlertController$d0Iv3YbZq03Jnp0MstxoxZC7XHw INSTANCE = new $$Lambda$NotificationAlertController$d0Iv3YbZq03Jnp0MstxoxZC7XHw();

    private /* synthetic */ $$Lambda$NotificationAlertController$d0Iv3YbZq03Jnp0MstxoxZC7XHw() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((NotificationEntry) obj).getSbn().setHasShownAfterUnlock(false);
    }
}
