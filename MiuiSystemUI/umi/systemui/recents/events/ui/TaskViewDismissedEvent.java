package com.android.systemui.recents.events.ui;

import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.AnimationProps;
import com.android.systemui.recents.views.TaskView;

public class TaskViewDismissedEvent extends RecentsEventBus.Event {
    public final AnimationProps animation;
    public final Task task;
    public final TaskView taskView;

    public TaskViewDismissedEvent(Task task2, TaskView taskView2, AnimationProps animationProps) {
        this.task = task2;
        this.taskView = taskView2;
        this.animation = animationProps;
    }
}
