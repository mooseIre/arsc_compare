package com.android.systemui.qs;

import com.android.internal.logging.UiEventLogger;

/* compiled from: QSEvents.kt */
public enum QSUserSwitcherEvent implements UiEventLogger.UiEventEnum {
    QS_USER_SWITCH(424),
    QS_USER_DETAIL_OPEN(425),
    QS_USER_DETAIL_CLOSE(426),
    QS_USER_MORE_SETTINGS(427);
    
    private final int _id;

    private QSUserSwitcherEvent(int i) {
        this._id = i;
    }

    public int getId() {
        return this._id;
    }
}
