package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.RecentsEventBus;

public class AllTaskViewsDismissedEvent extends RecentsEventBus.Event {
    public final boolean mEmpty;
    public final boolean mFromDockGesture;
    public final int msgResId;

    public AllTaskViewsDismissedEvent(int i, boolean z, boolean z2) {
        this.msgResId = i;
        this.mEmpty = z;
        this.mFromDockGesture = z2;
    }

    public AllTaskViewsDismissedEvent(int i, boolean z) {
        this.msgResId = i;
        this.mEmpty = z;
        this.mFromDockGesture = false;
    }
}
