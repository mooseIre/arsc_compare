package com.android.systemui.recents.views;

import android.app.ActivityOptions;
import android.app.ActivityOptionsCompat;
import android.content.Context;
import android.content.ContextCompat;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.util.Log;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.View;
import com.android.systemui.SystemUICompat;
import com.android.systemui.recents.BaseRecentsImpl;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ExitRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskStartedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskSucceededEvent;
import com.android.systemui.recents.events.component.ScreenPinningRequestEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecentsTransitionHelper {
    private static final List<AppTransitionAnimationSpec> SPECS_WAITING = new ArrayList();
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public StartScreenPinningRunnableRunnable mStartScreenPinningRunnable = new StartScreenPinningRunnableRunnable();
    private TaskViewTransform mTmpTransform = new TaskViewTransform();

    public interface AnimationSpecComposer {
        List<AppTransitionAnimationSpec> composeSpecs();
    }

    private class StartScreenPinningRunnableRunnable implements Runnable {
        /* access modifiers changed from: private */
        public int taskId;

        private StartScreenPinningRunnableRunnable() {
            this.taskId = -1;
        }

        public void run() {
            RecentsEventBus.getDefault().send(new ScreenPinningRequestEvent(RecentsTransitionHelper.this.mContext, this.taskId));
        }
    }

    public RecentsTransitionHelper(Context context) {
        this.mContext = context.getApplicationContext();
        this.mHandler = new Handler();
    }

    public void launchTaskFromRecents(TaskStack taskStack, final Task task, final TaskStackView taskStackView, TaskView taskView, final boolean z, Rect rect, int i) {
        ActivityOptions.OnAnimationStartedListener onAnimationStartedListener;
        IAppTransitionAnimationSpecsFuture iAppTransitionAnimationSpecsFuture;
        ActivityOptions makeBasic = ActivityOptions.makeBasic();
        if (rect != null) {
            if (rect.isEmpty()) {
                rect = null;
            }
            ActivityOptionsCompat.setOptionsLaunchBounds(makeBasic, rect);
        }
        if (taskView != null) {
            final Rect windowRect = Recents.getSystemServices().getWindowRect();
            iAppTransitionAnimationSpecsFuture = getAppTransitionFuture(new AnimationSpecComposer() {
                public List<AppTransitionAnimationSpec> composeSpecs() {
                    return RecentsTransitionHelper.this.composeAnimationSpecs(task, taskStackView, 0, 0, windowRect);
                }
            }, taskStackView.getHandler());
            onAnimationStartedListener = new ActivityOptions.OnAnimationStartedListener() {
                public void onAnimationStarted() {
                    RecentsEventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(task));
                    RecentsEventBus.getDefault().send(new ExitRecentsWindowFirstAnimationFrameEvent(task));
                    taskStackView.cancelAllTaskViewAnimations();
                    if (z) {
                        int unused = RecentsTransitionHelper.this.mStartScreenPinningRunnable.taskId = task.key.id;
                        RecentsTransitionHelper.this.mHandler.postDelayed(RecentsTransitionHelper.this.mStartScreenPinningRunnable, 350);
                    }
                }
            };
        } else {
            onAnimationStartedListener = new ActivityOptions.OnAnimationStartedListener(this) {
                public void onAnimationStarted() {
                    RecentsEventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(task));
                    RecentsEventBus.getDefault().send(new ExitRecentsWindowFirstAnimationFrameEvent(task));
                    taskStackView.cancelAllTaskViewAnimations();
                }
            };
            iAppTransitionAnimationSpecsFuture = null;
        }
        if (taskView == null) {
            startTaskActivity(taskStack, task, taskView, makeBasic, iAppTransitionAnimationSpecsFuture, onAnimationStartedListener);
            return;
        }
        RecentsEventBus.getDefault().send(new LaunchTaskStartedEvent(taskView, z));
        startTaskActivity(taskStack, task, taskView, makeBasic, iAppTransitionAnimationSpecsFuture, onAnimationStartedListener);
    }

    public IRemoteCallback wrapStartedListener(final ActivityOptions.OnAnimationStartedListener onAnimationStartedListener) {
        if (onAnimationStartedListener == null) {
            return null;
        }
        return new IRemoteCallback.Stub() {
            public void sendResult(Bundle bundle) throws RemoteException {
                RecentsTransitionHelper.this.mHandler.post(new Runnable() {
                    public void run() {
                        onAnimationStartedListener.onAnimationStarted();
                    }
                });
            }
        };
    }

    private void startTaskActivity(TaskStack taskStack, Task task, TaskView taskView, ActivityOptions activityOptions, IAppTransitionAnimationSpecsFuture iAppTransitionAnimationSpecsFuture, ActivityOptions.OnAnimationStartedListener onAnimationStartedListener) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        if (systemServices.startActivityFromRecents(this.mContext, task.key, task.title, activityOptions)) {
            int i = 0;
            int indexOfStackTask = taskStack.indexOfStackTask(task);
            if (indexOfStackTask > -1) {
                i = (taskStack.getTaskCount() - indexOfStackTask) - 1;
            }
            RecentsEventBus.getDefault().send(new LaunchTaskSucceededEvent(i));
        } else {
            if (taskView != null) {
                taskView.dismissTask();
            }
            RecentsEventBus.getDefault().send(new LaunchTaskFailedEvent());
        }
        if (iAppTransitionAnimationSpecsFuture != null) {
            systemServices.overridePendingAppTransitionMultiThumbFuture(iAppTransitionAnimationSpecsFuture, wrapStartedListener(onAnimationStartedListener), true, ContextCompat.getDisplayId(this.mContext));
        }
    }

    public IAppTransitionAnimationSpecsFuture getAppTransitionFuture(final AnimationSpecComposer animationSpecComposer, Handler handler) {
        if (animationSpecComposer == null || handler == null) {
            return null;
        }
        return new AppTransitionAnimationSpecsFuture(this, handler) {
            public List<AppTransitionAnimationSpec> composeSpecs() {
                return animationSpecComposer.composeSpecs();
            }
        }.getFuture();
    }

    public List<AppTransitionAnimationSpec> composeDockAnimationSpec(TaskView taskView, Rect rect) {
        this.mTmpTransform.fillIn(taskView);
        Task task = taskView.getTask();
        return Collections.singletonList(new AppTransitionAnimationSpec(task.key.id, composeTaskBitmap(taskView, this.mTmpTransform), rect));
    }

    public List<AppTransitionAnimationSpec> composeAnimationSpecs(Task task, TaskStackView taskStackView, int i, int i2, Rect rect) {
        TaskView childViewForTask = taskStackView.getChildViewForTask(task);
        TaskStackLayoutAlgorithm stackAlgorithm = taskStackView.getStackAlgorithm();
        Rect rect2 = new Rect();
        stackAlgorithm.getFrontOfStackTransform().rect.round(rect2);
        if (i != 1 && i != 3 && i != 4 && i2 != 4 && i != 0) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        if (childViewForTask == null) {
            arrayList.add(composeOffscreenAnimationSpec(task, rect2));
        } else {
            this.mTmpTransform.fillIn(childViewForTask);
            stackAlgorithm.transformToScreenCoordinates(this.mTmpTransform, rect);
            AppTransitionAnimationSpec composeAnimationSpec = composeAnimationSpec(taskStackView, childViewForTask, this.mTmpTransform, true);
            if (composeAnimationSpec != null) {
                arrayList.add(composeAnimationSpec);
            }
        }
        return arrayList;
    }

    private static AppTransitionAnimationSpec composeOffscreenAnimationSpec(Task task, Rect rect) {
        return new AppTransitionAnimationSpec(task.key.id, (GraphicBuffer) null, rect);
    }

    public static GraphicBuffer composeTaskBitmap(TaskView taskView, TaskViewTransform taskViewTransform) {
        float f = taskViewTransform.scale;
        int width = (int) (taskViewTransform.rect.width() * f);
        int height = (int) (taskViewTransform.rect.height() * f);
        if (width != 0 && height != 0) {
            return drawViewIntoGraphicBuffer(width, height, (View) null, 1.0f, 0);
        }
        Log.e("RecentsTransitionHelper", "Could not compose thumbnail for task: " + taskView.getTask() + " at transform: " + taskViewTransform);
        return drawViewIntoGraphicBuffer(1, 1, (View) null, 1.0f, 16777215);
    }

    private static GraphicBuffer composeHeaderBitmap(TaskView taskView, TaskViewTransform taskViewTransform) {
        float f = taskViewTransform.scale;
        int width = (int) taskViewTransform.rect.width();
        int measuredHeight = (int) (((float) taskView.mHeaderView.getMeasuredHeight()) * f);
        if (width == 0 || measuredHeight == 0) {
            return null;
        }
        return drawViewIntoGraphicBuffer(width, measuredHeight, (View) null, 1.0f, 0);
    }

    public static GraphicBuffer drawViewIntoGraphicBuffer(int i, int i2, View view, float f, int i3) {
        Bitmap drawViewIntoBitmap;
        if (i <= 0 || i2 <= 0 || (drawViewIntoBitmap = SystemUICompat.drawViewIntoBitmap(i, i2, view, f, i3)) == null) {
            return null;
        }
        return drawViewIntoBitmap.createGraphicBufferHandle();
    }

    private static AppTransitionAnimationSpec composeAnimationSpec(TaskStackView taskStackView, TaskView taskView, TaskViewTransform taskViewTransform, boolean z) {
        GraphicBuffer graphicBuffer = null;
        if (z) {
            GraphicBuffer composeHeaderBitmap = composeHeaderBitmap(taskView, taskViewTransform);
            if (composeHeaderBitmap == null) {
                return null;
            }
            graphicBuffer = composeHeaderBitmap;
        }
        Rect rect = new Rect();
        taskViewTransform.rect.round(rect);
        rect.top += BaseRecentsImpl.mTaskBarHeight;
        return new AppTransitionAnimationSpec(taskView.getTask().key.id, graphicBuffer, rect);
    }
}
