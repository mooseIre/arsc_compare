package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import java.util.Iterator;

/* compiled from: NotificationWakeUpCoordinator.kt */
final class NotificationWakeUpCoordinator$setStackScroller$1 implements Runnable {
    final /* synthetic */ NotificationWakeUpCoordinator this$0;

    NotificationWakeUpCoordinator$setStackScroller$1(NotificationWakeUpCoordinator notificationWakeUpCoordinator) {
        this.this$0 = notificationWakeUpCoordinator;
    }

    public final void run() {
        boolean isPulseExpanding = this.this$0.isPulseExpanding();
        boolean z = isPulseExpanding != this.this$0.pulseExpanding;
        this.this$0.pulseExpanding = isPulseExpanding;
        Iterator it = this.this$0.wakeUpListeners.iterator();
        while (it.hasNext()) {
            ((NotificationWakeUpCoordinator.WakeUpListener) it.next()).onPulseExpansionChanged(z);
        }
    }
}
