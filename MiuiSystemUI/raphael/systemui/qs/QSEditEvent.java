package com.android.systemui.qs;

import com.android.internal.logging.UiEventLogger;

/* compiled from: QSEvents.kt */
public enum QSEditEvent implements UiEventLogger.UiEventEnum {
    QS_EDIT_REMOVE(210),
    QS_EDIT_ADD(211),
    QS_EDIT_MOVE(212),
    QS_EDIT_OPEN(213),
    QS_EDIT_CLOSED(214);
    
    private final int _id;

    private QSEditEvent(int i) {
        this._id = i;
    }

    public int getId() {
        return this._id;
    }
}
