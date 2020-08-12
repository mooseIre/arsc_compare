package com.android.systemui.recents.events.component;

import android.content.Context;
import com.android.systemui.recents.events.RecentsEventBus;

public class RecentsVisibilityChangedEvent extends RecentsEventBus.Event {
    public final Context applicationContext;
    public final boolean visible;

    public RecentsVisibilityChangedEvent(Context context, boolean z) {
        this.applicationContext = context.getApplicationContext();
        this.visible = z;
    }
}
