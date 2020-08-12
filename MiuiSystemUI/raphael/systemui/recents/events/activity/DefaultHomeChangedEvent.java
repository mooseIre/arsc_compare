package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class DefaultHomeChangedEvent extends RecentsEventBus.Event {
    public final boolean mIsMiuiHome;

    public DefaultHomeChangedEvent(boolean z) {
        this.mIsMiuiHome = z;
    }
}
