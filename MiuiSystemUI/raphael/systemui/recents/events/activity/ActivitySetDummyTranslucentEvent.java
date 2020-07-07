package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class ActivitySetDummyTranslucentEvent extends RecentsEventBus.Event {
    public final boolean mIsTranslucent;

    public ActivitySetDummyTranslucentEvent(boolean z) {
        this.mIsTranslucent = z;
    }
}
