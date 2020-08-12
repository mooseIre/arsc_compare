package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class HideRecentsEvent extends RecentsEventBus.Event {
    public final boolean triggeredFromAltTab;
    public final boolean triggeredFromFsGesture;
    public final boolean triggeredFromHomeKey;
    public final boolean triggeredFromScroll;

    public HideRecentsEvent(boolean z, boolean z2, boolean z3) {
        this.triggeredFromAltTab = z;
        this.triggeredFromHomeKey = z2;
        this.triggeredFromFsGesture = z3;
        this.triggeredFromScroll = false;
    }

    public HideRecentsEvent(boolean z, boolean z2, boolean z3, boolean z4) {
        this.triggeredFromAltTab = z;
        this.triggeredFromHomeKey = z2;
        this.triggeredFromFsGesture = z3;
        this.triggeredFromScroll = z4;
    }
}
