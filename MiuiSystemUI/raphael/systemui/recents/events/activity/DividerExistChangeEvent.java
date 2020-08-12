package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class DividerExistChangeEvent extends RecentsEventBus.Event {
    public final boolean isExist;

    public DividerExistChangeEvent(boolean z) {
        this.isExist = z;
    }
}
