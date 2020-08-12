package com.android.systemui.miui.statusbar.analytics;

public class Analytics$ExpandEvent extends Analytics$Event {
    public String currentPage;
    public MODE expandMode;
    public int notificationsCount;

    public enum MODE {
        MANUAL,
        COMMAND
    }

    public Analytics$ExpandEvent() {
        super("expand_notification_bar");
    }
}
