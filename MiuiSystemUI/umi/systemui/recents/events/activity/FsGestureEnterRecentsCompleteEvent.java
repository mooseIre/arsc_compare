package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class FsGestureEnterRecentsCompleteEvent extends RecentsEventBus.Event {
    public final boolean mMoveRecentsToFront;

    public FsGestureEnterRecentsCompleteEvent() {
        this.mMoveRecentsToFront = false;
    }

    public FsGestureEnterRecentsCompleteEvent(boolean z) {
        this.mMoveRecentsToFront = z;
    }
}
