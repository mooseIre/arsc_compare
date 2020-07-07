package com.android.systemui.miui.statusbar.analytics;

public class Analytics$CancelAllNotiEvent extends Analytics$Event {
    public MODE clearAllMode;
    public int isSlideNotificationBar;
    public int notificationsCount;

    public enum MODE {
        CLEAR_ALL,
        CLEAR_FOLDED
    }

    public Analytics$CancelAllNotiEvent() {
        super("notification_cancel_all");
    }
}
