package com.android.systemui.recents.events;

import com.android.systemui.recents.events.RecentsEventBus;

public class FsGestureMoveEvent extends RecentsEventBus.Event {
    public float mTouchX;
    public float mTouchY;

    public FsGestureMoveEvent(float f, float f2) {
        this.mTouchX = f;
        this.mTouchY = f2;
    }
}
