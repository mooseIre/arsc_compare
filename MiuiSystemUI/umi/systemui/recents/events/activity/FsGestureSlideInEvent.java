package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class FsGestureSlideInEvent extends RecentsEventBus.Event {
    public float mPositionX;
    public float mPositionY;

    public FsGestureSlideInEvent() {
        this.mPositionX = 0.0f;
        this.mPositionY = 0.0f;
    }

    public FsGestureSlideInEvent(float f, float f2) {
        this.mPositionX = f;
        this.mPositionY = f;
    }
}
