package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class FsGestureEnterRecentsZoomEvent extends RecentsEventBus.Event {
    public final Runnable mAnimEndRunnable;
    public final long mTimeOffset;

    public FsGestureEnterRecentsZoomEvent(long j, Runnable runnable) {
        this.mTimeOffset = j;
        this.mAnimEndRunnable = runnable;
    }
}
