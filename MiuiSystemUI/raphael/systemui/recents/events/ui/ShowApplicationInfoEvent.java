package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.model.Task;

public class ShowApplicationInfoEvent extends RecentsEventBus.Event {
    public final Task task;

    public ShowApplicationInfoEvent(Task task2) {
        this.task = task2;
    }
}
