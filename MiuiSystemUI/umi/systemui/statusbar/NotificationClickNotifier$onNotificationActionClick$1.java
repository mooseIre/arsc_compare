package com.android.systemui.statusbar;

/* compiled from: NotificationClickNotifier.kt */
final class NotificationClickNotifier$onNotificationActionClick$1 implements Runnable {
    final /* synthetic */ String $key;
    final /* synthetic */ NotificationClickNotifier this$0;

    NotificationClickNotifier$onNotificationActionClick$1(NotificationClickNotifier notificationClickNotifier, String str) {
        this.this$0 = notificationClickNotifier;
        this.$key = str;
    }

    public final void run() {
        this.this$0.notifyListenersAboutInteraction(this.$key);
    }
}
