package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.model.Task;

public class ClickTaskViewToLaunchTaskEvent extends RecentsEventBus.Event {
    public final Task task;

    public ClickTaskViewToLaunchTaskEvent(Task task2) {
        this.task = task2;
    }
}
