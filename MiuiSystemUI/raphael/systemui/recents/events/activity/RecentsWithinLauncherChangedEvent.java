package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class RecentsWithinLauncherChangedEvent extends RecentsEventBus.Event {
    public final boolean mRecentsWithinLauncher;

    public RecentsWithinLauncherChangedEvent(boolean z) {
        this.mRecentsWithinLauncher = z;
    }
}
