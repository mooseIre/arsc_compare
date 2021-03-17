package com.android.systemui.assist;

import com.android.internal.logging.UiEventLogger;

/* compiled from: AssistantSessionEvent.kt */
public enum AssistantSessionEvent implements UiEventLogger.UiEventEnum {
    ASSISTANT_SESSION_INVOCATION_CANCELLED(526),
    ASSISTANT_SESSION_UPDATE(528),
    ASSISTANT_SESSION_CLOSE(529);
    
    private final int id;

    private AssistantSessionEvent(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }
}
