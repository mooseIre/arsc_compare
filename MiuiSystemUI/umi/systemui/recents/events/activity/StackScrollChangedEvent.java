package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class StackScrollChangedEvent extends RecentsEventBus.Event {
    public int mTranslationY;

    public StackScrollChangedEvent(int i) {
        this.mTranslationY = i;
    }
}
