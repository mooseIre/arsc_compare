package com.android.systemui.recents.events.ui.dragndrop;

import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.DropTarget;
import com.android.systemui.recents.views.TaskView;

public class DragEndEvent extends RecentsEventBus.AnimatedEvent {
    public final DropTarget dropTarget;
    public final Task task;
    public final TaskView taskView;

    public DragEndEvent(Task task2, TaskView taskView2, DropTarget dropTarget2) {
        this.task = task2;
        this.taskView = taskView2;
        this.dropTarget = dropTarget2;
    }
}
