package com.android.systemui.statusbar.notification;

import android.view.ViewTreeObserver;

/* access modifiers changed from: package-private */
/* compiled from: NotificationPanelNavigationBarCoordinator.kt */
public final class NotificationPanelNavigationBarCoordinator$onGlobalLayoutListener$1 implements ViewTreeObserver.OnGlobalLayoutListener {
    final /* synthetic */ NotificationPanelNavigationBarCoordinator this$0;

    NotificationPanelNavigationBarCoordinator$onGlobalLayoutListener$1(NotificationPanelNavigationBarCoordinator notificationPanelNavigationBarCoordinator) {
        this.this$0 = notificationPanelNavigationBarCoordinator;
    }

    public final void onGlobalLayout() {
        NotificationPanelNavigationBarCoordinator.access$getBarView$p(this.this$0).getLocationOnScreen(this.this$0.getLocation());
    }
}
