package com.android.systemui.statusbar;

import com.android.internal.logging.UiEventLogger;

public enum StatusBarStateEvent implements UiEventLogger.UiEventEnum {
    STATUS_BAR_STATE_UNKNOWN(428),
    STATUS_BAR_STATE_SHADE(429),
    STATUS_BAR_STATE_KEYGUARD(430),
    STATUS_BAR_STATE_SHADE_LOCKED(431),
    STATUS_BAR_STATE_FULLSCREEN_USER_SWITCHER(432);
    
    private int mId;

    private StatusBarStateEvent(int i) {
        this.mId = i;
    }

    public int getId() {
        return this.mId;
    }

    public static StatusBarStateEvent fromState(int i) {
        if (i == 0) {
            return STATUS_BAR_STATE_SHADE;
        }
        if (i == 1) {
            return STATUS_BAR_STATE_KEYGUARD;
        }
        if (i == 2) {
            return STATUS_BAR_STATE_SHADE_LOCKED;
        }
        if (i != 3) {
            return STATUS_BAR_STATE_UNKNOWN;
        }
        return STATUS_BAR_STATE_FULLSCREEN_USER_SWITCHER;
    }
}
