package com.android.systemui.bubbles;

import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.systemui.bubbles.BubbleLogger;

public class BubbleLoggerImpl extends UiEventLoggerImpl implements BubbleLogger {
    @Override // com.android.systemui.bubbles.BubbleLogger
    public void log(Bubble bubble, UiEventLogger.UiEventEnum uiEventEnum) {
        if (bubble.getInstanceId() != null) {
            logWithInstanceId(uiEventEnum, bubble.getAppUid(), bubble.getPackageName(), bubble.getInstanceId());
        }
    }

    @Override // com.android.systemui.bubbles.BubbleLogger
    public void logOverflowRemove(Bubble bubble, int i) {
        if (i == 5) {
            log(bubble, BubbleLogger.Event.BUBBLE_OVERFLOW_REMOVE_CANCEL);
        } else if (i == 9) {
            log(bubble, BubbleLogger.Event.BUBBLE_OVERFLOW_REMOVE_GROUP_CANCEL);
        } else if (i == 7) {
            log(bubble, BubbleLogger.Event.BUBBLE_OVERFLOW_REMOVE_NO_LONGER_BUBBLE);
        } else if (i == 4) {
            log(bubble, BubbleLogger.Event.BUBBLE_OVERFLOW_REMOVE_BLOCKED);
        }
    }

    @Override // com.android.systemui.bubbles.BubbleLogger
    public void logOverflowAdd(Bubble bubble, int i) {
        if (i == 2) {
            log(bubble, BubbleLogger.Event.BUBBLE_OVERFLOW_ADD_AGED);
        } else if (i == 1) {
            log(bubble, BubbleLogger.Event.BUBBLE_OVERFLOW_ADD_USER_GESTURE);
        }
    }
}
