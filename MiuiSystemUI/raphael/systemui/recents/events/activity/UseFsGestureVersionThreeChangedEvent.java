package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class UseFsGestureVersionThreeChangedEvent extends RecentsEventBus.Event {
    public final boolean mUseFsGestureVersionThree;

    public UseFsGestureVersionThreeChangedEvent(boolean z) {
        this.mUseFsGestureVersionThree = z;
    }
}
