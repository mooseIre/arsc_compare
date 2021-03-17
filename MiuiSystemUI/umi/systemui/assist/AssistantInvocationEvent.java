package com.android.systemui.assist;

import com.android.internal.logging.UiEventLogger;
import kotlin.jvm.internal.DefaultConstructorMarker;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: Failed to restore enum class, 'enum' modifier removed */
/* compiled from: AssistantInvocationEvent.kt */
public final class AssistantInvocationEvent extends Enum<AssistantInvocationEvent> implements UiEventLogger.UiEventEnum {
    private static final /* synthetic */ AssistantInvocationEvent[] $VALUES;
    public static final AssistantInvocationEvent ASSISTANT_INVOCATION_HOME_LONG_PRESS;
    public static final AssistantInvocationEvent ASSISTANT_INVOCATION_HOTWORD;
    public static final AssistantInvocationEvent ASSISTANT_INVOCATION_PHYSICAL_GESTURE;
    public static final AssistantInvocationEvent ASSISTANT_INVOCATION_QUICK_SEARCH_BAR;
    public static final AssistantInvocationEvent ASSISTANT_INVOCATION_START_PHYSICAL_GESTURE;
    public static final AssistantInvocationEvent ASSISTANT_INVOCATION_START_TOUCH_GESTURE;
    public static final AssistantInvocationEvent ASSISTANT_INVOCATION_START_UNKNOWN;
    public static final AssistantInvocationEvent ASSISTANT_INVOCATION_TOUCH_GESTURE;
    public static final AssistantInvocationEvent ASSISTANT_INVOCATION_UNKNOWN;
    public static final Companion Companion = new Companion(null);
    private final int id;

    public static AssistantInvocationEvent valueOf(String str) {
        return (AssistantInvocationEvent) Enum.valueOf(AssistantInvocationEvent.class, str);
    }

    public static AssistantInvocationEvent[] values() {
        return (AssistantInvocationEvent[]) $VALUES.clone();
    }

    private AssistantInvocationEvent(String str, int i, int i2) {
        this.id = i2;
    }

    static {
        AssistantInvocationEvent assistantInvocationEvent = new AssistantInvocationEvent("ASSISTANT_INVOCATION_UNKNOWN", 0, 442);
        ASSISTANT_INVOCATION_UNKNOWN = assistantInvocationEvent;
        AssistantInvocationEvent assistantInvocationEvent2 = new AssistantInvocationEvent("ASSISTANT_INVOCATION_TOUCH_GESTURE", 1, 443);
        ASSISTANT_INVOCATION_TOUCH_GESTURE = assistantInvocationEvent2;
        AssistantInvocationEvent assistantInvocationEvent3 = new AssistantInvocationEvent("ASSISTANT_INVOCATION_HOTWORD", 3, 445);
        ASSISTANT_INVOCATION_HOTWORD = assistantInvocationEvent3;
        AssistantInvocationEvent assistantInvocationEvent4 = new AssistantInvocationEvent("ASSISTANT_INVOCATION_QUICK_SEARCH_BAR", 4, 446);
        ASSISTANT_INVOCATION_QUICK_SEARCH_BAR = assistantInvocationEvent4;
        AssistantInvocationEvent assistantInvocationEvent5 = new AssistantInvocationEvent("ASSISTANT_INVOCATION_HOME_LONG_PRESS", 5, 447);
        ASSISTANT_INVOCATION_HOME_LONG_PRESS = assistantInvocationEvent5;
        AssistantInvocationEvent assistantInvocationEvent6 = new AssistantInvocationEvent("ASSISTANT_INVOCATION_PHYSICAL_GESTURE", 6, 448);
        ASSISTANT_INVOCATION_PHYSICAL_GESTURE = assistantInvocationEvent6;
        AssistantInvocationEvent assistantInvocationEvent7 = new AssistantInvocationEvent("ASSISTANT_INVOCATION_START_UNKNOWN", 7, 530);
        ASSISTANT_INVOCATION_START_UNKNOWN = assistantInvocationEvent7;
        AssistantInvocationEvent assistantInvocationEvent8 = new AssistantInvocationEvent("ASSISTANT_INVOCATION_START_TOUCH_GESTURE", 8, 531);
        ASSISTANT_INVOCATION_START_TOUCH_GESTURE = assistantInvocationEvent8;
        AssistantInvocationEvent assistantInvocationEvent9 = new AssistantInvocationEvent("ASSISTANT_INVOCATION_START_PHYSICAL_GESTURE", 9, 532);
        ASSISTANT_INVOCATION_START_PHYSICAL_GESTURE = assistantInvocationEvent9;
        $VALUES = new AssistantInvocationEvent[]{assistantInvocationEvent, assistantInvocationEvent2, new AssistantInvocationEvent("ASSISTANT_INVOCATION_TOUCH_GESTURE_ALT", 2, 444), assistantInvocationEvent3, assistantInvocationEvent4, assistantInvocationEvent5, assistantInvocationEvent6, assistantInvocationEvent7, assistantInvocationEvent8, assistantInvocationEvent9};
    }

    public int getId() {
        return this.id;
    }

    /* compiled from: AssistantInvocationEvent.kt */
    public static final class Companion {
        public final int deviceStateFromLegacyDeviceState(int i) {
            switch (i) {
                case 1:
                    return 1;
                case 2:
                    return 2;
                case 3:
                    return 3;
                case 4:
                    return 4;
                case 5:
                    return 5;
                case 6:
                    return 6;
                case 7:
                    return 7;
                case 8:
                    return 8;
                case 9:
                    return 9;
                case 10:
                    return 10;
                default:
                    return 0;
            }
        }

        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final AssistantInvocationEvent eventFromLegacyInvocationType(int i, boolean z) {
            if (z) {
                if (i == 1) {
                    return AssistantInvocationEvent.ASSISTANT_INVOCATION_TOUCH_GESTURE;
                }
                if (i == 2) {
                    return AssistantInvocationEvent.ASSISTANT_INVOCATION_PHYSICAL_GESTURE;
                }
                if (i == 3) {
                    return AssistantInvocationEvent.ASSISTANT_INVOCATION_HOTWORD;
                }
                if (i == 4) {
                    return AssistantInvocationEvent.ASSISTANT_INVOCATION_QUICK_SEARCH_BAR;
                }
                if (i != 5) {
                    return AssistantInvocationEvent.ASSISTANT_INVOCATION_UNKNOWN;
                }
                return AssistantInvocationEvent.ASSISTANT_INVOCATION_HOME_LONG_PRESS;
            } else if (i == 1) {
                return AssistantInvocationEvent.ASSISTANT_INVOCATION_START_TOUCH_GESTURE;
            } else {
                if (i != 2) {
                    return AssistantInvocationEvent.ASSISTANT_INVOCATION_START_UNKNOWN;
                }
                return AssistantInvocationEvent.ASSISTANT_INVOCATION_START_PHYSICAL_GESTURE;
            }
        }
    }
}
