package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.model.RecentsPackageMonitor;

public class PackagesChangedEvent extends RecentsEventBus.Event {
    public final String packageName;
    public final int userId;

    public PackagesChangedEvent(RecentsPackageMonitor recentsPackageMonitor, String str, int i) {
        this.packageName = str;
        this.userId = i;
    }
}
