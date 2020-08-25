package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class SuperPowerModeChangedEvent extends RecentsEventBus.Event {
    public final boolean mIsSuperPowerMode;

    public SuperPowerModeChangedEvent(boolean z) {
        this.mIsSuperPowerMode = z;
    }
}
