package com.android.systemui.qs;

import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import org.jetbrains.annotations.NotNull;

/* compiled from: QSEvents.kt */
public final class QSEvents {
    public static final QSEvents INSTANCE = new QSEvents();
    @NotNull
    private static UiEventLogger qsUiEventsLogger = new UiEventLoggerImpl();

    private QSEvents() {
    }

    @NotNull
    public final UiEventLogger getQsUiEventsLogger() {
        return qsUiEventsLogger;
    }
}
