package com.android.systemui.recents.events.activity;

import android.graphics.Rect;
import com.android.systemui.recents.events.RecentsEventBus;

public class DockedTopTaskEvent extends RecentsEventBus.Event {
    public int dragMode;
    public Rect initialRect;

    public DockedTopTaskEvent(int i, Rect rect) {
        this.dragMode = i;
        this.initialRect = rect;
    }
}
