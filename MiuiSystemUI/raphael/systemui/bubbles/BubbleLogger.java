package com.android.systemui.bubbles;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.UiEventLogger;

public interface BubbleLogger extends UiEventLogger {
    void log(Bubble bubble, UiEventLogger.UiEventEnum uiEventEnum);

    void logOverflowAdd(Bubble bubble, int i);

    void logOverflowRemove(Bubble bubble, int i);

    @VisibleForTesting
    public enum Event implements UiEventLogger.UiEventEnum {
        BUBBLE_OVERFLOW_ADD_USER_GESTURE(483),
        BUBBLE_OVERFLOW_ADD_AGED(484),
        BUBBLE_OVERFLOW_REMOVE_MAX_REACHED(485),
        BUBBLE_OVERFLOW_REMOVE_CANCEL(486),
        BUBBLE_OVERFLOW_REMOVE_GROUP_CANCEL(487),
        BUBBLE_OVERFLOW_REMOVE_NO_LONGER_BUBBLE(488),
        BUBBLE_OVERFLOW_REMOVE_BACK_TO_STACK(489),
        BUBBLE_OVERFLOW_REMOVE_BLOCKED(490);
        
        private final int mId;

        private Event(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }
}
