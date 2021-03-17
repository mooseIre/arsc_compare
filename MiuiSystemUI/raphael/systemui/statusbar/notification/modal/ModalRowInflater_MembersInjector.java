package com.android.systemui.statusbar.notification.modal;

import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.row.NotificationContentInflater;

public final class ModalRowInflater_MembersInjector {
    public static void injectContentInflater(ModalRowInflater modalRowInflater, NotificationContentInflater notificationContentInflater) {
        modalRowInflater.contentInflater = notificationContentInflater;
    }

    public static void injectRemoteInputManager(ModalRowInflater modalRowInflater, NotificationRemoteInputManager notificationRemoteInputManager) {
        modalRowInflater.remoteInputManager = notificationRemoteInputManager;
    }
}
