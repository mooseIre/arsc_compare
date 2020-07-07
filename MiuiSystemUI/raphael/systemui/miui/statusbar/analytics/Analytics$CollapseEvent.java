package com.android.systemui.miui.statusbar.analytics;

public class Analytics$CollapseEvent extends Analytics$Event {
    public MODE collapseMode;
    public ACTION fistNotificationAction;
    public long fistNotificationActionDuration;
    public int isClickQsToggle;
    public int isDeleteNotification;
    public int isQsExpanded;
    public int isSlideBrightnessBar;
    public int isSlideNotificationBar;
    public int notificationVisibleCount;
    public int notificationsCount;
    public long residenceTime;

    public enum ACTION {
        NONE,
        CLICK,
        LONG_PRESS,
        SWIPE_LEFT,
        SWIPE_RIGHT,
        CLEAR_ALL
    }

    public enum MODE {
        COMMAND,
        BACK,
        HOME,
        CLICK_TOGGLE,
        CLICK_NOTIFICATION,
        CLICK_CLEAR_ALL,
        OTHER
    }

    public Analytics$CollapseEvent() {
        super("collapse_notification_bar");
    }
}
