package com.android.systemui.qs;

import com.android.internal.logging.UiEventLogger;

/* JADX INFO: Failed to restore enum class, 'enum' modifier removed */
/* compiled from: QSEvents.kt */
public final class QSEditEvent extends Enum<QSEditEvent> implements UiEventLogger.UiEventEnum {
    private static final /* synthetic */ QSEditEvent[] $VALUES;
    public static final QSEditEvent QS_EDIT_ADD;
    public static final QSEditEvent QS_EDIT_CLOSED;
    public static final QSEditEvent QS_EDIT_MOVE;
    public static final QSEditEvent QS_EDIT_OPEN;
    public static final QSEditEvent QS_EDIT_REMOVE;
    private final int _id;

    public static QSEditEvent valueOf(String str) {
        return (QSEditEvent) Enum.valueOf(QSEditEvent.class, str);
    }

    public static QSEditEvent[] values() {
        return (QSEditEvent[]) $VALUES.clone();
    }

    private QSEditEvent(String str, int i, int i2) {
        this._id = i2;
    }

    static {
        QSEditEvent qSEditEvent = new QSEditEvent("QS_EDIT_REMOVE", 0, 210);
        QS_EDIT_REMOVE = qSEditEvent;
        QSEditEvent qSEditEvent2 = new QSEditEvent("QS_EDIT_ADD", 1, 211);
        QS_EDIT_ADD = qSEditEvent2;
        QSEditEvent qSEditEvent3 = new QSEditEvent("QS_EDIT_MOVE", 2, 212);
        QS_EDIT_MOVE = qSEditEvent3;
        QSEditEvent qSEditEvent4 = new QSEditEvent("QS_EDIT_OPEN", 3, 213);
        QS_EDIT_OPEN = qSEditEvent4;
        QSEditEvent qSEditEvent5 = new QSEditEvent("QS_EDIT_CLOSED", 4, 214);
        QS_EDIT_CLOSED = qSEditEvent5;
        $VALUES = new QSEditEvent[]{qSEditEvent, qSEditEvent2, qSEditEvent3, qSEditEvent4, qSEditEvent5, new QSEditEvent("QS_EDIT_RESET", 5, 215)};
    }

    public int getId() {
        return this._id;
    }
}
