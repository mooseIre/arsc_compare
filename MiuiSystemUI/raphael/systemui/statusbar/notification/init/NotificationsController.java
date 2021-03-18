package com.android.systemui.statusbar.notification.init;

import android.service.notification.StatusBarNotification;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.phone.StatusBar;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationsController.kt */
public interface NotificationsController {
    void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr, boolean z);

    int getActiveNotificationsCount();

    void initialize(@NotNull StatusBar statusBar, @NotNull NotificationPresenter notificationPresenter, @NotNull NotificationListContainer notificationListContainer, @NotNull NotificationActivityStarter notificationActivityStarter, @NotNull NotificationRowBinderImpl.BindRowCallback bindRowCallback);

    void requestNotificationUpdate(@NotNull String str);

    void resetUserExpandedStates();

    void setNotificationSnoozed(@NotNull StatusBarNotification statusBarNotification, int i);

    void setNotificationSnoozed(@NotNull StatusBarNotification statusBarNotification, @NotNull NotificationSwipeActionHelper.SnoozeOption snoozeOption);
}
