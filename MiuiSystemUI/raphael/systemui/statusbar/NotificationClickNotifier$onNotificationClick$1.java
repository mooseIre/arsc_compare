package com.android.systemui.statusbar;

/* access modifiers changed from: package-private */
/* compiled from: NotificationClickNotifier.kt */
public final class NotificationClickNotifier$onNotificationClick$1 implements Runnable {
    final /* synthetic */ String $key;
    final /* synthetic */ NotificationClickNotifier this$0;

    NotificationClickNotifier$onNotificationClick$1(NotificationClickNotifier notificationClickNotifier, String str) {
        this.this$0 = notificationClickNotifier;
        this.$key = str;
    }

    public final void run() {
        this.this$0.notifyListenersAboutInteraction(this.$key);
    }
}
