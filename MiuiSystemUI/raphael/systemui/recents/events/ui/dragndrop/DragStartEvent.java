package com.android.systemui.recents.events.ui.dragndrop;

import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.TaskView;

public class DragStartEvent extends RecentsEventBus.Event {
    public final Task task;
    public final TaskView taskView;

    public DragStartEvent(Task task2, TaskView taskView2) {
        this.task = task2;
        this.taskView = taskView2;
    }
}
