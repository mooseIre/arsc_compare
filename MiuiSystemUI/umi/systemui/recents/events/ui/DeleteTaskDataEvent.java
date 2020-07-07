package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.model.Task;

public class DeleteTaskDataEvent extends RecentsEventBus.Event {
    public boolean remainProcess = false;
    public final Task task;

    public DeleteTaskDataEvent(Task task2) {
        this.task = task2;
    }

    public DeleteTaskDataEvent(Task task2, boolean z) {
        this.task = task2;
        this.remainProcess = z;
    }
}
