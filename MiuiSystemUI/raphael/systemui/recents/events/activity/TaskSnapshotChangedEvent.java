package com.android.systemui.recents.events.activity;

import android.graphics.Bitmap;
import com.android.systemui.proxy.ActivityManager$TaskThumbnailInfo;
import com.android.systemui.recents.events.RecentsEventBus;

public class TaskSnapshotChangedEvent extends RecentsEventBus.AnimatedEvent {
    public final boolean isDeterminedWhetherBlur;
    public final Bitmap snapshot;
    public final int taskId;
    public final ActivityManager$TaskThumbnailInfo taskThumbnailInfo;

    public TaskSnapshotChangedEvent(int i, Bitmap bitmap, ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo, boolean z) {
        this.taskId = i;
        this.snapshot = bitmap;
        this.taskThumbnailInfo = activityManager$TaskThumbnailInfo;
        this.isDeterminedWhetherBlur = z;
    }
}
