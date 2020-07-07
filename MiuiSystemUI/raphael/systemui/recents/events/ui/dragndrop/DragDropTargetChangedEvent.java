package com.android.systemui.recents.events.ui.dragndrop;

import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.DropTarget;

public class DragDropTargetChangedEvent extends RecentsEventBus.AnimatedEvent {
    public final DropTarget dropTarget;
    public final Task task;

    public DragDropTargetChangedEvent(Task task2, DropTarget dropTarget2) {
        this.task = task2;
        this.dropTarget = dropTarget2;
    }
}
