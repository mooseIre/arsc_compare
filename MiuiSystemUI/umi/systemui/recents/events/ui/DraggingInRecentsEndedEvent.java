package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.RecentsEventBus;

public class DraggingInRecentsEndedEvent extends RecentsEventBus.Event {
    public final float velocity;

    public DraggingInRecentsEndedEvent(float f) {
        this.velocity = f;
    }
}
