package com.android.systemui.recents.events.activity;

import android.graphics.RectF;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.model.Task;

public class FsGestureLaunchTargetTaskViewRectEvent extends RecentsEventBus.Event {
    public final RectF mRectF;

    public FsGestureLaunchTargetTaskViewRectEvent(RectF rectF, Task task) {
        this.mRectF = rectF;
    }
}
