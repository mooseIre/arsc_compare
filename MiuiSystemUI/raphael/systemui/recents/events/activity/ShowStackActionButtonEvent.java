package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class ShowStackActionButtonEvent extends RecentsEventBus.Event {
    public final boolean translate;

    public ShowStackActionButtonEvent(boolean z) {
        this.translate = z;
    }
}
