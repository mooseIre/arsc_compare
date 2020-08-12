package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class FsGestureRecentsModeMoveEvent extends RecentsEventBus.Event {
    public float mScale;

    public FsGestureRecentsModeMoveEvent(float f) {
        this.mScale = f;
    }
}
