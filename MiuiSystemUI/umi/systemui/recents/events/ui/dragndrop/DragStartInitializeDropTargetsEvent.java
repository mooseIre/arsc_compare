package com.android.systemui.recents.events.ui.dragndrop;

import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.RecentsViewTouchHandler;
import com.android.systemui.recents.views.TaskView;

public class DragStartInitializeDropTargetsEvent extends RecentsEventBus.Event {
    public final RecentsViewTouchHandler handler;
    public final TaskView taskView;

    public DragStartInitializeDropTargetsEvent(Task task, TaskView taskView2, RecentsViewTouchHandler recentsViewTouchHandler) {
        this.taskView = taskView2;
        this.handler = recentsViewTouchHandler;
    }
}
