package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import com.android.systemui.recents.misc.SpringAnimationUtils;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import java.util.ArrayList;
import java.util.List;
import miui.view.animation.CubicEaseOutInterpolator;
import miui.view.animation.QuinticEaseOutInterpolator;

public class TaskStackAnimationHelper {
    private static final Interpolator DISMISS_ALL_TRANSLATION_INTERPOLATOR = Interpolators.EASE_IN_OUT;
    private static final Interpolator ENTER_FROM_HOME_ALPHA_INTERPOLATOR = Interpolators.LINEAR;
    private static final Interpolator ENTER_FROM_HOME_TRANSLATION_INTERPOLATOR = new QuinticEaseOutInterpolator();
    private static final Interpolator ENTER_WHILE_DOCKING_INTERPOLATOR;
    private static final Interpolator EXIT_TO_HOME_TRANSLATION_INTERPOLATOR = new CubicEaseOutInterpolator();
    /* access modifiers changed from: private */
    public static final Interpolator FOCUS_BEHIND_NEXT_TASK_INTERPOLATOR;
    private static final Interpolator FOCUS_IN_FRONT_NEXT_TASK_INTERPOLATOR = new PathInterpolator(0.0f, 0.0f, 0.0f, 1.0f);
    private static final Interpolator FOCUS_NEXT_TASK_INTERPOLATOR = new PathInterpolator(0.4f, 0.0f, 0.0f, 1.0f);
    /* access modifiers changed from: private */
    public TaskStackView mStackView;
    private ArrayList<TaskViewTransform> mTmpCurrentTaskTransforms = new ArrayList<>();
    private ArrayList<TaskViewTransform> mTmpFinalTaskTransforms = new ArrayList<>();
    private TaskViewTransform mTmpTransform = new TaskViewTransform();

    public interface Callbacks {
    }

    static {
        Interpolator interpolator = Interpolators.LINEAR_OUT_SLOW_IN;
        FOCUS_BEHIND_NEXT_TASK_INTERPOLATOR = interpolator;
        ENTER_WHILE_DOCKING_INTERPOLATOR = interpolator;
    }

    public TaskStackAnimationHelper(Context context, TaskStackView taskStackView) {
        this.mStackView = taskStackView;
    }

    public void prepareForEnterAnimation() {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        Resources resources = this.mStackView.getResources();
        Resources resources2 = this.mStackView.getContext().getApplicationContext().getResources();
        TaskStackLayoutAlgorithm stackAlgorithm = this.mStackView.getStackAlgorithm();
        TaskStackViewScroller scroller = this.mStackView.getScroller();
        TaskStack stack = this.mStackView.getStack();
        Task launchTarget = stack.getLaunchTarget();
        if (stack.getTaskCount() != 0) {
            int height = stackAlgorithm.mStackRect.height() - stackAlgorithm.mPaddingTop;
            int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.recents_task_stack_animation_launched_while_docking_offset);
            boolean z = true;
            boolean z2 = resources2.getConfiguration().orientation == 2;
            List<TaskView> taskViews = this.mStackView.getTaskViews();
            int size = taskViews.size() - 1;
            while (size >= 0) {
                TaskView taskView = taskViews.get(size);
                Task task = taskView.getTask();
                boolean z3 = (launchTarget == null || !launchTarget.isFreeformTask() || !task.isFreeformTask()) ? false : z;
                stackAlgorithm.getStackTransform(task, scroller.getStackScroll(), this.mTmpTransform, (TaskViewTransform) null);
                if (z3) {
                    taskView.setVisibility(4);
                } else if (launchState.launchedViaFsGesture) {
                    this.mStackView.updateTaskViewToTransform(taskView, this.mTmpTransform, AnimationProps.IMMEDIATE);
                } else if (!launchState.launchedFromApp || launchState.launchedViaDockGesture) {
                    if (launchState.launchedFromHome) {
                        taskView.setTranslationY((float) height);
                        taskView.setAlpha(0.0f);
                    } else if (launchState.launchedViaDockGesture) {
                        this.mTmpTransform.rect.offset(0.0f, (float) (z2 ? dimensionPixelSize : (int) (((float) height) * 0.9f)));
                        TaskViewTransform taskViewTransform = this.mTmpTransform;
                        taskViewTransform.alpha = 0.0f;
                        this.mStackView.updateTaskViewToTransform(taskView, taskViewTransform, AnimationProps.IMMEDIATE);
                    }
                } else if (task.isLaunchTarget) {
                    taskView.onPrepareLaunchTargetForEnterAnimation();
                }
                size--;
                z = true;
            }
        }
    }

    public void startEnterAnimation(final ReferenceCountedTrigger referenceCountedTrigger) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        Resources resources = this.mStackView.getResources();
        Resources resources2 = this.mStackView.getContext().getApplicationContext().getResources();
        TaskStackLayoutAlgorithm stackAlgorithm = this.mStackView.getStackAlgorithm();
        TaskStackViewScroller scroller = this.mStackView.getScroller();
        TaskStack stack = this.mStackView.getStack();
        stack.getLaunchTarget();
        if (stack.getTaskCount() != 0) {
            int integer = resources.getInteger(R.integer.recents_task_enter_from_app_duration);
            resources.getInteger(R.integer.recents_task_enter_from_affiliated_app_duration);
            int integer2 = resources2.getInteger(R.integer.long_press_dock_anim_duration);
            List<TaskView> taskViews = this.mStackView.getTaskViews();
            int size = taskViews.size() - 1;
            for (int i = size; i >= 0; i--) {
                TaskView taskView = taskViews.get(i);
                Task task = taskView.getTask();
                stackAlgorithm.getStackTransform(task, scroller.getStackScroll(), this.mTmpTransform, (TaskViewTransform) null);
                if (!launchState.launchedFromApp || launchState.launchedViaDockGesture) {
                    if (launchState.launchedFromHome) {
                        referenceCountedTrigger.increment();
                        SpringAnimationUtils.getInstance().startHomeToRecentsAnim(taskView, new Runnable() {
                            public void run() {
                                referenceCountedTrigger.decrement();
                            }
                        });
                        if (i == size) {
                            taskView.onStartFrontTaskEnterAnimation(this.mStackView.mScreenPinningEnabled);
                        }
                    } else if (launchState.launchedViaDockGesture) {
                        AnimationProps animationProps = new AnimationProps();
                        animationProps.setDuration(6, (i * 50) + integer2);
                        animationProps.setInterpolator(6, ENTER_WHILE_DOCKING_INTERPOLATOR);
                        animationProps.setStartDelay(6, 48);
                        animationProps.setListener(referenceCountedTrigger.decrementOnAnimationEnd());
                        referenceCountedTrigger.increment();
                        this.mStackView.updateTaskViewToTransform(taskView, this.mTmpTransform, animationProps);
                    }
                } else if (task.isLaunchTarget) {
                    taskView.onStartLaunchTargetEnterAnimation(this.mTmpTransform, integer, this.mStackView.mScreenPinningEnabled, referenceCountedTrigger);
                }
            }
        }
    }

    public void startExitToHomeAnimation(boolean z, ReferenceCountedTrigger referenceCountedTrigger) {
        AnimationProps animationProps;
        TaskStackLayoutAlgorithm stackAlgorithm = this.mStackView.getStackAlgorithm();
        if (this.mStackView.getStack().getTaskCount() != 0) {
            stackAlgorithm.mStackRect.height();
            List<TaskView> taskViews = this.mStackView.getTaskViews();
            int size = taskViews.size();
            for (int i = 0; i < size; i++) {
                TaskView taskView = taskViews.get(i);
                if (!this.mStackView.isIgnoredTask(taskView.getTask())) {
                    if (z) {
                        Math.min(5, i);
                        animationProps = new AnimationProps();
                        animationProps.setDuration(6, 350);
                        animationProps.setInterpolator(6, EXIT_TO_HOME_TRANSLATION_INTERPOLATOR);
                        animationProps.setDuration(4, 350);
                        animationProps.setInterpolator(4, EXIT_TO_HOME_TRANSLATION_INTERPOLATOR);
                        animationProps.setListener(referenceCountedTrigger.decrementOnAnimationEnd());
                        referenceCountedTrigger.increment();
                    } else {
                        animationProps = AnimationProps.IMMEDIATE;
                    }
                    this.mTmpTransform.fillIn(taskView);
                    TaskViewTransform taskViewTransform = this.mTmpTransform;
                    taskViewTransform.alpha = 0.0f;
                    this.mStackView.updateTaskViewToTransform(taskView, taskViewTransform, animationProps);
                }
            }
        }
    }

    public void startLaunchTaskAnimation(TaskView taskView, boolean z, ReferenceCountedTrigger referenceCountedTrigger) {
        int integer = this.mStackView.getResources().getInteger(R.integer.recents_task_exit_to_app_duration);
        taskView.getTask();
        List<TaskView> taskViews = this.mStackView.getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            final TaskView taskView2 = taskViews.get(i);
            taskView2.getTask();
            if (taskView2 == taskView) {
                taskView2.setClipViewInStack(false);
                referenceCountedTrigger.addLastDecrementRunnable(new Runnable() {
                    public void run() {
                        taskView2.setClipViewInStack(true);
                    }
                });
                taskView2.onStartLaunchTargetLaunchAnimation(integer, z, referenceCountedTrigger);
            }
        }
    }

    public void startDeleteTaskAnimation(final TaskView taskView, final ReferenceCountedTrigger referenceCountedTrigger) {
        final TaskStackViewTouchHandler touchHandler = this.mStackView.getTouchHandler();
        touchHandler.onBeginManualDrag(taskView);
        referenceCountedTrigger.increment();
        referenceCountedTrigger.addLastDecrementRunnable(new Runnable() {
            public void run() {
                touchHandler.onChildDismissed(taskView);
            }
        });
        final float scaledDismissSize = touchHandler.getScaledDismissSize();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        ofFloat.setDuration(400);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                taskView.setTranslationX(scaledDismissSize * floatValue);
                touchHandler.updateSwipeProgress(taskView, true, floatValue);
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                referenceCountedTrigger.decrement();
            }
        });
        ofFloat.start();
    }

    public void startDeleteAllTasksAnimation(List<TaskView> list, final ReferenceCountedTrigger referenceCountedTrigger) {
        if (Constants.IS_SUPPORT_LINEAR_MOTOR_VIBRATE) {
            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performExtHapticFeedback(90);
        }
        for (int size = list.size() - 1; size >= 0; size--) {
            final TaskView taskView = list.get(size);
            int i = size * 50;
            taskView.setClipViewInStack(false);
            if (!taskView.getTask().isProtected()) {
                AnimationProps animationProps = new AnimationProps(i, 150, DISMISS_ALL_TRANSLATION_INTERPOLATOR, new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        referenceCountedTrigger.decrement();
                        taskView.setClipViewInStack(true);
                    }
                });
                referenceCountedTrigger.increment();
                this.mTmpTransform.fillIn(taskView);
                if (this.mTmpTransform.rect.centerX() <= ((float) this.mStackView.getWidth()) / 2.0f) {
                    this.mTmpTransform.rect.offset((float) (-taskView.getRight()), 0.0f);
                } else {
                    this.mTmpTransform.rect.offset((float) (this.mStackView.getWidth() - taskView.getLeft()), 0.0f);
                }
                this.mStackView.updateTaskViewToTransform(taskView, this.mTmpTransform, animationProps);
            }
        }
    }

    public boolean startScrollToFocusedTaskAnimation(Task task, boolean z) {
        Interpolator interpolator;
        int i;
        Task task2 = task;
        TaskStackLayoutAlgorithm stackAlgorithm = this.mStackView.getStackAlgorithm();
        TaskStackViewScroller scroller = this.mStackView.getScroller();
        TaskStack stack = this.mStackView.getStack();
        float stackScroll = scroller.getStackScroll();
        final float boundedStackScroll = scroller.getBoundedStackScroll(stackAlgorithm.getTrueStackScrollForTask(task2));
        boolean z2 = boundedStackScroll > stackScroll;
        boolean z3 = Float.compare(boundedStackScroll, stackScroll) != 0;
        int size = this.mStackView.getTaskViews().size();
        ArrayList<Task> stackTasks = stack.getStackTasks();
        this.mStackView.getCurrentTaskTransforms(stackTasks, this.mTmpCurrentTaskTransforms);
        this.mStackView.bindVisibleTaskViews(boundedStackScroll);
        stackAlgorithm.setFocusState(1);
        scroller.setStackScroll(boundedStackScroll, (AnimationProps) null);
        this.mStackView.cancelDeferredTaskViewLayoutAnimation();
        int i2 = size;
        this.mStackView.getLayoutTaskTransforms(boundedStackScroll, stackAlgorithm.getFocusState(), stackTasks, true, this.mTmpFinalTaskTransforms);
        TaskView childViewForTask = this.mStackView.getChildViewForTask(task2);
        if (childViewForTask == null) {
            Log.e("TaskStackAnimationHelper", "b/27389156 null-task-view prebind:" + i2 + " postbind:" + this.mStackView.getTaskViews().size() + " prescroll:" + stackScroll + " postscroll: " + boundedStackScroll);
            return false;
        }
        childViewForTask.setFocusedState(true, z);
        ReferenceCountedTrigger referenceCountedTrigger = new ReferenceCountedTrigger();
        referenceCountedTrigger.addLastDecrementRunnable(new Runnable() {
            public void run() {
                TaskStackAnimationHelper.this.mStackView.bindVisibleTaskViews(boundedStackScroll);
            }
        });
        List<TaskView> taskViews = this.mStackView.getTaskViews();
        int size2 = taskViews.size();
        int indexOf = taskViews.indexOf(childViewForTask);
        for (int i3 = 0; i3 < size2; i3++) {
            TaskView taskView = taskViews.get(i3);
            Task task3 = taskView.getTask();
            if (!this.mStackView.isIgnoredTask(task3)) {
                int indexOf2 = stackTasks.indexOf(task3);
                TaskViewTransform taskViewTransform = this.mTmpFinalTaskTransforms.get(indexOf2);
                this.mStackView.updateTaskViewToTransform(taskView, this.mTmpCurrentTaskTransforms.get(indexOf2), AnimationProps.IMMEDIATE);
                if (z2) {
                    i = calculateStaggeredAnimDuration(i3);
                    interpolator = FOCUS_BEHIND_NEXT_TASK_INTERPOLATOR;
                } else if (i3 < indexOf) {
                    i = (((indexOf - i3) - 1) * 50) + 150;
                    interpolator = FOCUS_BEHIND_NEXT_TASK_INTERPOLATOR;
                } else if (i3 > indexOf) {
                    i = Math.max(100, 150 - (((i3 - indexOf) - 1) * 50));
                    interpolator = FOCUS_IN_FRONT_NEXT_TASK_INTERPOLATOR;
                } else {
                    i = 200;
                    interpolator = FOCUS_NEXT_TASK_INTERPOLATOR;
                }
                AnimationProps animationProps = new AnimationProps();
                animationProps.setDuration(6, i);
                animationProps.setInterpolator(6, interpolator);
                animationProps.setListener(referenceCountedTrigger.decrementOnAnimationEnd());
                referenceCountedTrigger.increment();
                this.mStackView.updateTaskViewToTransform(taskView, taskViewTransform, animationProps);
            }
        }
        return z3;
    }

    public void startNewStackScrollAnimation(TaskStack taskStack, ReferenceCountedTrigger referenceCountedTrigger) {
        TaskStackLayoutAlgorithm stackAlgorithm = this.mStackView.getStackAlgorithm();
        TaskStackViewScroller scroller = this.mStackView.getScroller();
        ArrayList<Task> stackTasks = taskStack.getStackTasks();
        this.mStackView.getCurrentTaskTransforms(stackTasks, this.mTmpCurrentTaskTransforms);
        this.mStackView.setTasks(taskStack, false);
        this.mStackView.updateLayoutAlgorithm(false);
        final float f = stackAlgorithm.mInitialScrollP;
        this.mStackView.bindVisibleTaskViews(f);
        stackAlgorithm.setFocusState(0);
        stackAlgorithm.setTaskOverridesForInitialState(taskStack, true);
        scroller.setStackScroll(f);
        this.mStackView.cancelDeferredTaskViewLayoutAnimation();
        this.mStackView.getLayoutTaskTransforms(f, stackAlgorithm.getFocusState(), stackTasks, false, this.mTmpFinalTaskTransforms);
        Task stackFrontMostTask = taskStack.getStackFrontMostTask(false);
        final TaskView childViewForTask = this.mStackView.getChildViewForTask(stackFrontMostTask);
        final TaskViewTransform taskViewTransform = this.mTmpFinalTaskTransforms.get(stackTasks.indexOf(stackFrontMostTask));
        if (childViewForTask != null) {
            this.mStackView.updateTaskViewToTransform(childViewForTask, stackAlgorithm.getFrontOfStackTransform(), AnimationProps.IMMEDIATE);
        }
        referenceCountedTrigger.addLastDecrementRunnable(new Runnable() {
            public void run() {
                TaskStackAnimationHelper.this.mStackView.bindVisibleTaskViews(f);
                if (childViewForTask != null) {
                    TaskStackAnimationHelper.this.mStackView.updateTaskViewToTransform(childViewForTask, taskViewTransform, new AnimationProps(75, 250, TaskStackAnimationHelper.FOCUS_BEHIND_NEXT_TASK_INTERPOLATOR));
                }
            }
        });
        List<TaskView> taskViews = this.mStackView.getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            Task task = taskView.getTask();
            if (!this.mStackView.isIgnoredTask(task) && (task != stackFrontMostTask || childViewForTask == null)) {
                int indexOf = stackTasks.indexOf(task);
                this.mStackView.updateTaskViewToTransform(taskView, this.mTmpCurrentTaskTransforms.get(indexOf), AnimationProps.IMMEDIATE);
                int calculateStaggeredAnimDuration = calculateStaggeredAnimDuration(i);
                Interpolator interpolator = FOCUS_BEHIND_NEXT_TASK_INTERPOLATOR;
                AnimationProps animationProps = new AnimationProps();
                animationProps.setDuration(6, calculateStaggeredAnimDuration);
                animationProps.setInterpolator(6, interpolator);
                animationProps.setListener(referenceCountedTrigger.decrementOnAnimationEnd());
                referenceCountedTrigger.increment();
                this.mStackView.updateTaskViewToTransform(taskView, this.mTmpFinalTaskTransforms.get(indexOf), animationProps);
            }
        }
    }

    private int calculateStaggeredAnimDuration(int i) {
        return Math.max(100, ((i - 1) * 50) + 100);
    }
}
