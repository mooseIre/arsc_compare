package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.views.TaskView;

public class DismissTaskViewEvent extends RecentsEventBus.AnimatedEvent {
    public final TaskView taskView;

    public DismissTaskViewEvent(TaskView taskView2) {
        this.taskView = taskView2;
    }
}
