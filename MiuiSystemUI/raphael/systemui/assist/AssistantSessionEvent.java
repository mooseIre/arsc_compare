package com.android.systemui.assist;

import com.android.internal.logging.UiEventLogger;

/* JADX INFO: Failed to restore enum class, 'enum' modifier removed */
/* compiled from: AssistantSessionEvent.kt */
public final class AssistantSessionEvent extends Enum<AssistantSessionEvent> implements UiEventLogger.UiEventEnum {
    private static final /* synthetic */ AssistantSessionEvent[] $VALUES;
    public static final AssistantSessionEvent ASSISTANT_SESSION_CLOSE;
    public static final AssistantSessionEvent ASSISTANT_SESSION_INVOCATION_CANCELLED;
    public static final AssistantSessionEvent ASSISTANT_SESSION_UPDATE;
    private final int id;

    public static AssistantSessionEvent valueOf(String str) {
        return (AssistantSessionEvent) Enum.valueOf(AssistantSessionEvent.class, str);
    }

    public static AssistantSessionEvent[] values() {
        return (AssistantSessionEvent[]) $VALUES.clone();
    }

    private AssistantSessionEvent(String str, int i, int i2) {
        this.id = i2;
    }

    static {
        AssistantSessionEvent assistantSessionEvent = new AssistantSessionEvent("ASSISTANT_SESSION_INVOCATION_CANCELLED", 3, 526);
        ASSISTANT_SESSION_INVOCATION_CANCELLED = assistantSessionEvent;
        AssistantSessionEvent assistantSessionEvent2 = new AssistantSessionEvent("ASSISTANT_SESSION_UPDATE", 5, 528);
        ASSISTANT_SESSION_UPDATE = assistantSessionEvent2;
        AssistantSessionEvent assistantSessionEvent3 = new AssistantSessionEvent("ASSISTANT_SESSION_CLOSE", 6, 529);
        ASSISTANT_SESSION_CLOSE = assistantSessionEvent3;
        $VALUES = new AssistantSessionEvent[]{new AssistantSessionEvent("ASSISTANT_SESSION_UNKNOWN", 0, 523), new AssistantSessionEvent("ASSISTANT_SESSION_TIMEOUT_DISMISS", 1, 524), new AssistantSessionEvent("ASSISTANT_SESSION_INVOCATION_START", 2, 525), assistantSessionEvent, new AssistantSessionEvent("ASSISTANT_SESSION_USER_DISMISS", 4, 527), assistantSessionEvent2, assistantSessionEvent3};
    }

    public int getId() {
        return this.id;
    }
}
