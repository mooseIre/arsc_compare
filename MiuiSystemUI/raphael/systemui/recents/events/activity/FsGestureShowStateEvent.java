package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class FsGestureShowStateEvent extends RecentsEventBus.Event {
    public boolean isEnter;
    public String typeFrom;

    public FsGestureShowStateEvent(boolean z) {
        this.isEnter = z;
        this.typeFrom = "typefrom_demo";
    }

    public FsGestureShowStateEvent(boolean z, String str) {
        this.isEnter = z;
        this.typeFrom = str;
    }
}
