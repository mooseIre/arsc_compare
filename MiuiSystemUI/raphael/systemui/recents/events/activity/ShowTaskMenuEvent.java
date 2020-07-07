package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.views.TaskView;

public class ShowTaskMenuEvent extends RecentsEventBus.Event {
    public final TaskView taskView;

    public ShowTaskMenuEvent(TaskView taskView2) {
        this.taskView = taskView2;
    }
}
