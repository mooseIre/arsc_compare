package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class StartSmallWindowEvent extends RecentsEventBus.Event {
    public String mPkgName;

    public StartSmallWindowEvent(String str) {
        this.mPkgName = str;
    }
}
