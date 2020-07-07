package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.model.Task;

public class ExitRecentsWindowFirstAnimationFrameEvent extends RecentsEventBus.Event {
    public final Task launchTask;

    public ExitRecentsWindowFirstAnimationFrameEvent(Task task) {
        this.launchTask = task;
    }
}
