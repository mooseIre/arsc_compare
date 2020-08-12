package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class DividerMinimizedChangeEvent extends RecentsEventBus.Event {
    public final boolean isMinimized;

    public DividerMinimizedChangeEvent(boolean z) {
        this.isMinimized = z;
    }
}
