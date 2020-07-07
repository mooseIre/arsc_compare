package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class FsGestureEnterRecentsZoomEvent extends RecentsEventBus.Event {
    public final Runnable mAnimEndRunnable;

    public FsGestureEnterRecentsZoomEvent(long j, Runnable runnable) {
        this.mAnimEndRunnable = runnable;
    }
}
