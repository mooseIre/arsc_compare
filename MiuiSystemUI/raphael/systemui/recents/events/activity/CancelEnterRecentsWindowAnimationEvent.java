package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.model.Task;

public class CancelEnterRecentsWindowAnimationEvent extends RecentsEventBus.Event {
    public final Task launchTask;

    public CancelEnterRecentsWindowAnimationEvent(Task task) {
        this.launchTask = task;
    }
}
