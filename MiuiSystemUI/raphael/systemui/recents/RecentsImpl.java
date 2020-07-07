package com.android.systemui.recents;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import com.android.systemui.SystemUICompat;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.EnterRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.RecentsTransitionHelper;
import com.android.systemui.recents.views.TaskStackView;
import com.android.systemui.recents.views.TaskViewTransform;

public class RecentsImpl extends BaseRecentsImpl {
    public RecentsImpl(Context context) {
        super(context);
    }

    /* access modifiers changed from: package-private */
    public ActivityOptions getThumbnailTransitionActivityOptions(ActivityManager.RunningTaskInfo runningTaskInfo, TaskStackView taskStackView, Rect rect) {
        Task task = new Task();
        TaskViewTransform thumbnailTransitionTransform = getThumbnailTransitionTransform(taskStackView, task, rect);
        GraphicBuffer drawThumbnailTransitionBitmap = drawThumbnailTransitionBitmap(task, thumbnailTransitionTransform);
        RectF rectF = thumbnailTransitionTransform.rect;
        rectF.top += (float) BaseRecentsImpl.mTaskBarHeight;
        return ActivityOptions.makeThumbnailAspectScaleDownAnimation(this.mDummyStackView, SystemUICompat.createHardwareBitmapFromGraphicBuffer(drawThumbnailTransitionBitmap), (int) rectF.left, (int) rectF.top, (int) rectF.width(), (int) rectF.height(), this.mHandler, new ActivityOptions.OnAnimationStartedListener() {
            public void onAnimationStarted() {
                RecentsEventBus.getDefault().post(new EnterRecentsWindowFirstAnimationFrameEvent());
            }
        });
    }

    private GraphicBuffer drawThumbnailTransitionBitmap(Task task, TaskViewTransform taskViewTransform) {
        GraphicBuffer drawViewIntoGraphicBuffer;
        SystemServicesProxy systemServices = Recents.getSystemServices();
        if (taskViewTransform == null || task.key == null) {
            return null;
        }
        synchronized (this.mHeaderBarLock) {
            if (!task.isSystemApp) {
                boolean isInSafeMode = systemServices.isInSafeMode();
            }
            int width = (int) taskViewTransform.rect.width();
            this.mHeaderBar.onTaskViewSizeChanged(width, (int) taskViewTransform.rect.height());
            drawViewIntoGraphicBuffer = RecentsTransitionHelper.drawViewIntoGraphicBuffer(width, BaseRecentsImpl.mTaskBarHeight, (View) null, 1.0f, 0);
        }
        return drawViewIntoGraphicBuffer;
    }
}
