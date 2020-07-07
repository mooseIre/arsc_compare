package com.android.systemui.recents.events.activity;

import android.graphics.Rect;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.TaskView;

public class LaunchTaskEvent extends RecentsEventBus.Event {
    public final boolean screenPinningRequested;
    public final Rect targetTaskBounds;
    public final int targetTaskStack;
    public final Task task;
    public final TaskView taskView;

    public LaunchTaskEvent(TaskView taskView2, Task task2, Rect rect, int i, boolean z) {
        this.taskView = taskView2;
        this.task = task2;
        this.targetTaskBounds = rect;
        this.targetTaskStack = i;
        this.screenPinningRequested = z;
    }
}
