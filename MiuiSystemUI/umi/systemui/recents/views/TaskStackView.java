package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.AnimFirstTaskViewAlphaEvent;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ClickTaskViewToLaunchTaskEvent;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.EnterRecentsTaskStackAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.HideStackActionButtonEvent;
import com.android.systemui.recents.events.activity.IterateRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchNextTaskRequestEvent;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.activity.LaunchTaskStartedEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.PackagesChangedEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.activity.RotationChangedEvent;
import com.android.systemui.recents.events.activity.ShowStackActionButtonEvent;
import com.android.systemui.recents.events.activity.StackScrollChangedEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.DismissTaskViewEvent;
import com.android.systemui.recents.events.ui.RecentsGrowingEvent;
import com.android.systemui.recents.events.ui.TaskViewDismissedEvent;
import com.android.systemui.recents.events.ui.UpdateFreeformTaskViewVisibilityEvent;
import com.android.systemui.recents.events.ui.UserInteractionEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartInitializeDropTargetsEvent;
import com.android.systemui.recents.events.ui.focus.DismissFocusedTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusNextTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusPreviousTaskViewEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import com.android.systemui.recents.misc.SpringAnimationUtils;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm;
import com.android.systemui.recents.views.TaskStackViewScroller;
import com.android.systemui.recents.views.TaskView;
import com.android.systemui.recents.views.ViewPool;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class TaskStackView extends FrameLayout implements TaskStack.TaskStackCallbacks, TaskView.TaskViewCallbacks, TaskStackViewScroller.TaskStackViewScrollerCallbacks, TaskStackLayoutAlgorithm.TaskStackLayoutAlgorithmCallbacks, ViewPool.ViewPoolConsumer<TaskView, Task> {
    private static boolean sIsChangingConfigurations = false;
    /* access modifiers changed from: private */
    public TaskStackAnimationHelper mAnimationHelper;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mAwaitingFirstLayout = true;
    private ArrayList<TaskViewTransform> mCurrentTaskTransforms = new ArrayList<>();
    private AnimationProps mDeferredTaskViewLayoutAnimation = null;
    /* access modifiers changed from: private */
    public boolean mDeleteAllTasksAnimating;
    private Runnable mDeleteAllTasksAnimationRunnable;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mDisplayOrientation = 0;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mDisplayRect = new Rect();
    private int mDividerSize;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mEnterAnimationComplete = false;
    /* access modifiers changed from: private */
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "focused_task_")
    public Task mFocusedTask;
    private GradientDrawable mFreeformWorkspaceBackground;
    private ObjectAnimator mFreeformWorkspaceBackgroundAnimator;
    private DropTarget mFreeformWorkspaceDropTarget;
    private ArraySet<Task.TaskKey> mIgnoreTasks = new ArraySet<>();
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mInMeasureLayout = false;
    private LayoutInflater mInflater;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mInitialState = 1;
    public boolean mIsMultiStateChanging;
    private boolean mIsShowingMenu;
    private boolean mKeepAlphaWhenRelayout;
    private int mLastHeight;
    private int mLastWidth;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "layout_")
    TaskStackLayoutAlgorithm mLayoutAlgorithm;
    private FrameLayout mMaskWithMenu;
    private ValueAnimator.AnimatorUpdateListener mRequestUpdateClippingListener;
    private boolean mResetToInitialStateWhenResized;
    @ViewDebug.ExportedProperty(category = "recents")
    boolean mScreenPinningEnabled;
    private TaskStackLayoutAlgorithm mStableLayoutAlgorithm;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mStableStackBounds = new Rect();
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mStableWindowRect = new Rect();
    /* access modifiers changed from: private */
    public TaskStack mStack = new TaskStack();
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mStackBounds = new Rect();
    private DropTarget mStackDropTarget;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mStackReloaded = false;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "scroller_")
    private TaskStackViewScroller mStackScroller;
    private int mStartTimerIndicatorDuration;
    /* access modifiers changed from: private */
    public boolean mTaskEnterAnimationComplete;
    private ArrayList<TaskView> mTaskViews = new ArrayList<>();
    /* access modifiers changed from: private */
    @ViewDebug.ExportedProperty(category = "recents")
    public boolean mTaskViewsClipDirty = true;
    private int[] mTmpIntPair;
    private Rect mTmpRect = new Rect();
    private ArrayMap<Task.TaskKey, TaskView> mTmpTaskViewMap = new ArrayMap<>();
    private List<TaskView> mTmpTaskViews = new ArrayList();
    @ViewDebug.ExportedProperty(category = "recents")
    boolean mTouchExplorationEnabled;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "touch_")
    private TaskStackViewTouchHandler mTouchHandler;
    /* access modifiers changed from: private */
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "doze_")
    public DozeTrigger mUIDozeTrigger;
    private ViewPool<TaskView, Task> mViewPool;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mWindowRect = new Rect();

    private void clipTaskViews() {
    }

    public static void setIsChangingConfigurations(boolean z) {
        sIsChangingConfigurations = z;
    }

    public TaskStackView(Context context) {
        super(context);
        new TaskViewTransform();
        this.mTmpIntPair = new int[2];
        this.mIsMultiStateChanging = false;
        this.mKeepAlphaWhenRelayout = false;
        this.mTaskEnterAnimationComplete = false;
        this.mDeleteAllTasksAnimating = false;
        this.mRequestUpdateClippingListener = new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (!TaskStackView.this.mTaskViewsClipDirty) {
                    boolean unused = TaskStackView.this.mTaskViewsClipDirty = true;
                    TaskStackView.this.invalidate();
                }
            }
        };
        this.mFreeformWorkspaceDropTarget = new DropTarget() {
            public boolean acceptsDrop(int i, int i2, int i3, int i4, boolean z) {
                if (!z) {
                    return TaskStackView.this.mLayoutAlgorithm.mFreeformRect.contains(i, i2);
                }
                return false;
            }
        };
        this.mStackDropTarget = new DropTarget() {
            public boolean acceptsDrop(int i, int i2, int i3, int i4, boolean z) {
                if (!z) {
                    return TaskStackView.this.mLayoutAlgorithm.mStackRect.contains(i, i2);
                }
                return false;
            }
        };
        this.mIsShowingMenu = false;
        SystemServicesProxy systemServices = Recents.getSystemServices();
        Resources resources = context.getResources();
        this.mStack.setCallbacks(this);
        this.mViewPool = new ViewPool<>(context, this);
        this.mInflater = LayoutInflater.from(context);
        this.mLayoutAlgorithm = new TaskStackLayoutAlgorithm(context, this);
        this.mStableLayoutAlgorithm = new TaskStackLayoutAlgorithm(context, (TaskStackLayoutAlgorithm.TaskStackLayoutAlgorithmCallbacks) null);
        this.mStackScroller = new TaskStackViewScroller(context, this, this.mLayoutAlgorithm);
        this.mTouchHandler = new TaskStackViewTouchHandler(context, this, this.mStackScroller);
        this.mAnimationHelper = new TaskStackAnimationHelper(context, this);
        resources.getDimensionPixelSize(R.dimen.recents_task_view_rounded_corners_radius);
        this.mDividerSize = systemServices.getDockedDividerSize(context);
        this.mDisplayOrientation = Utilities.getAppConfiguration(this.mContext).orientation;
        this.mDisplayRect = systemServices.getDisplayRect();
        this.mUIDozeTrigger = new DozeTrigger(getResources().getInteger(R.integer.recents_task_bar_dismiss_delay_seconds), new Runnable() {
            public void run() {
                List<TaskView> taskViews = TaskStackView.this.getTaskViews();
                int size = taskViews.size();
                for (int i = 0; i < size; i++) {
                    taskViews.get(i).startNoUserInteractionAnimation();
                }
            }
        });
        setImportantForAccessibility(1);
        GradientDrawable gradientDrawable = (GradientDrawable) getContext().getDrawable(R.drawable.recents_freeform_workspace_bg);
        this.mFreeformWorkspaceBackground = gradientDrawable;
        gradientDrawable.setCallback(this);
        if (systemServices.hasFreeformWorkspaceSupport()) {
            this.mFreeformWorkspaceBackground.setColor(getContext().getColor(R.color.recents_freeform_workspace_bg_color));
        }
        FrameLayout frameLayout = new FrameLayout(context);
        this.mMaskWithMenu = frameLayout;
        addView(frameLayout, -1, -1);
        setClipChildren(false);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        readSystemFlags();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: package-private */
    public void onReload(boolean z) {
        if (!z) {
            resetFocusedTask(getFocusedTask());
        }
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(getTaskViews());
        arrayList.addAll(this.mViewPool.getViews());
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            ((TaskView) arrayList.get(size)).onReload(z);
        }
        readSystemFlags();
        this.mTaskViewsClipDirty = true;
        this.mEnterAnimationComplete = false;
        this.mUIDozeTrigger.stopDozing();
        if (z) {
            animateFreeformWorkspaceBackgroundAlpha(this.mLayoutAlgorithm.getStackState().freeformBackgroundAlpha, new AnimationProps(150, Interpolators.FAST_OUT_SLOW_IN));
        } else {
            this.mStackScroller.reset();
            this.mStableLayoutAlgorithm.reset();
            this.mLayoutAlgorithm.reset();
        }
        this.mStackReloaded = true;
        this.mAwaitingFirstLayout = true;
        this.mInitialState = 1;
        this.mTaskEnterAnimationComplete = false;
        this.mIgnoreTasks.clear();
        requestLayout();
    }

    public void setTasks(TaskStack taskStack, boolean z) {
        this.mStack.setTasks(getContext(), taskStack.computeAllTasksList(), z && this.mLayoutAlgorithm.isInitialized());
    }

    public TaskStack getStack() {
        return this.mStack;
    }

    public void updateToInitialState() {
        this.mStackScroller.setStackScrollToInitialState();
        this.mLayoutAlgorithm.setTaskOverridesForInitialState(this.mStack, false);
    }

    /* access modifiers changed from: package-private */
    public void updateTaskViewsList() {
        this.mTaskViews.clear();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof TaskView) {
                this.mTaskViews.add((TaskView) childAt);
            }
        }
    }

    public List<TaskView> getTaskViews() {
        return this.mTaskViews;
    }

    public TaskView getFrontMostTaskView(boolean z) {
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            Task task = taskView.getTask();
            if (!z || !task.isFreeformTask()) {
                return taskView;
            }
        }
        return null;
    }

    public TaskView getChildViewForTask(Task task) {
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            if (taskView.getTask() == task) {
                return taskView;
            }
        }
        return null;
    }

    public TaskStackLayoutAlgorithm getStackAlgorithm() {
        return this.mLayoutAlgorithm;
    }

    public TaskStackViewTouchHandler getTouchHandler() {
        return this.mTouchHandler;
    }

    /* access modifiers changed from: package-private */
    public void addIgnoreTask(Task task) {
        this.mIgnoreTasks.add(task.key);
    }

    /* access modifiers changed from: package-private */
    public void removeIgnoreTask(Task task) {
        this.mIgnoreTasks.remove(task.key);
    }

    /* access modifiers changed from: package-private */
    public boolean isIgnoredTask(Task task) {
        return this.mIgnoreTasks.contains(task.key);
    }

    /* access modifiers changed from: package-private */
    public int[] computeVisibleTaskTransforms(ArrayList<TaskViewTransform> arrayList, ArrayList<Task> arrayList2, float f, float f2, ArraySet<Task.TaskKey> arraySet, boolean z) {
        boolean z2;
        ArrayList<TaskViewTransform> arrayList3 = arrayList;
        ArrayList<Task> arrayList4 = arrayList2;
        int size = arrayList2.size();
        int[] iArr = this.mTmpIntPair;
        iArr[0] = -1;
        iArr[1] = -1;
        boolean z3 = Float.compare(f, f2) != 0;
        Utilities.matchTaskListSize(arrayList4, arrayList3);
        int i = size - 1;
        TaskViewTransform taskViewTransform = null;
        TaskViewTransform taskViewTransform2 = null;
        TaskViewTransform taskViewTransform3 = null;
        while (i >= 0) {
            Task task = arrayList4.get(i);
            TaskStackLayoutAlgorithm taskStackLayoutAlgorithm = this.mLayoutAlgorithm;
            TaskViewTransform taskViewTransform4 = arrayList3.get(i);
            TaskViewTransform taskViewTransform5 = taskViewTransform4;
            Task task2 = task;
            taskStackLayoutAlgorithm.getStackTransform(task, f, taskViewTransform4, taskViewTransform, z);
            if (!z3 || taskViewTransform5.visible) {
                float f3 = f2;
            } else {
                TaskStackLayoutAlgorithm taskStackLayoutAlgorithm2 = this.mLayoutAlgorithm;
                TaskViewTransform taskViewTransform6 = new TaskViewTransform();
                taskStackLayoutAlgorithm2.getStackTransform(task2, f2, taskViewTransform6, taskViewTransform2);
                if (taskViewTransform6.visible) {
                    taskViewTransform5.copyFrom(taskViewTransform6);
                }
                taskViewTransform3 = taskViewTransform6;
            }
            if (!arraySet.contains(task2.key) && !task2.isFreeformTask()) {
                if (taskViewTransform5.visible) {
                    if (iArr[0] < 0) {
                        iArr[0] = i;
                    }
                    z2 = true;
                    iArr[1] = i;
                } else {
                    z2 = true;
                }
                taskViewTransform = taskViewTransform5;
                taskViewTransform2 = taskViewTransform3;
            } else {
                z2 = true;
            }
            i--;
            boolean z4 = z2;
        }
        return iArr;
    }

    /* access modifiers changed from: package-private */
    public void bindVisibleTaskViews(float f) {
        bindVisibleTaskViews(f, false);
    }

    /* access modifiers changed from: package-private */
    public void bindVisibleTaskViews(float f, boolean z) {
        int i;
        ArrayList<Task> stackTasks = this.mStack.getStackTasks();
        int[] computeVisibleTaskTransforms = computeVisibleTaskTransforms(this.mCurrentTaskTransforms, stackTasks, this.mStackScroller.getStackScroll(), f, this.mIgnoreTasks, z);
        this.mTmpTaskViewMap.clear();
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size() - 1;
        int i2 = -1;
        while (true) {
            TaskViewTransform taskViewTransform = null;
            if (size < 0) {
                break;
            }
            TaskView taskView = taskViews.get(size);
            Task task = taskView.getTask();
            if (!this.mIgnoreTasks.contains(task.key)) {
                int indexOfStackTask = this.mStack.indexOfStackTask(task);
                if (indexOfStackTask != -1 && this.mCurrentTaskTransforms.size() > 0) {
                    taskViewTransform = this.mCurrentTaskTransforms.get(indexOfStackTask);
                }
                if (task.isFreeformTask() || (taskViewTransform != null && taskViewTransform.visible)) {
                    this.mTmpTaskViewMap.put(task.key, taskView);
                } else {
                    if (this.mTouchExplorationEnabled && Utilities.isDescendentAccessibilityFocused(taskView)) {
                        resetFocusedTask(task);
                        i2 = indexOfStackTask;
                    }
                    this.mViewPool.returnViewToPool(taskView);
                }
            }
            size--;
        }
        for (int size2 = stackTasks.size() - 1; size2 >= 0; size2--) {
            Task task2 = stackTasks.get(size2);
            TaskViewTransform taskViewTransform2 = this.mCurrentTaskTransforms.size() > 0 ? this.mCurrentTaskTransforms.get(size2) : null;
            if (!this.mIgnoreTasks.contains(task2.key) && (task2.isFreeformTask() || taskViewTransform2.visible)) {
                TaskView taskView2 = this.mTmpTaskViewMap.get(task2.key);
                if (taskView2 == null) {
                    TaskView pickUpViewFromPool = this.mViewPool.pickUpViewFromPool(task2, task2);
                    if (task2.isFreeformTask()) {
                        updateTaskViewToTransform(pickUpViewFromPool, taskViewTransform2, AnimationProps.IMMEDIATE);
                    } else {
                        TaskViewTransform taskViewTransform3 = new TaskViewTransform();
                        float f2 = (float) size2;
                        this.mLayoutAlgorithm.getStackTransform(f2, f2, this.mTouchHandler.getOldStackScroll(), 0, taskViewTransform3, (TaskViewTransform) null, true, true);
                        taskViewTransform3.visible = true;
                        updateTaskViewToTransform(pickUpViewFromPool, taskViewTransform3, AnimationProps.IMMEDIATE);
                    }
                } else {
                    int findTaskViewInsertIndex = findTaskViewInsertIndex(task2, this.mStack.indexOfStackTask(task2));
                    if (findTaskViewInsertIndex != getTaskViews().indexOf(taskView2)) {
                        if (taskView2 == findFocus()) {
                            clearChildFocus(taskView2);
                        }
                        detachViewFromParent(taskView2);
                        attachViewToParent(taskView2, findTaskViewInsertIndex, taskView2.getLayoutParams());
                        updateTaskViewsList();
                    }
                }
            }
        }
        if (i2 != -1) {
            if (i2 < computeVisibleTaskTransforms[1]) {
                i = computeVisibleTaskTransforms[1];
            } else {
                i = computeVisibleTaskTransforms[0];
            }
            setFocusedTask(i, false, true);
            TaskView childViewForTask = getChildViewForTask(this.mFocusedTask);
            if (childViewForTask != null) {
                childViewForTask.requestAccessibilityFocus();
            }
        }
    }

    public void relayoutTaskViews(AnimationProps animationProps) {
        relayoutTaskViews(animationProps, (ArrayMap<Task, AnimationProps>) null, false);
    }

    private void relayoutTaskViews(AnimationProps animationProps, ArrayMap<Task, AnimationProps> arrayMap, boolean z) {
        cancelDeferredTaskViewLayoutAnimation();
        bindVisibleTaskViews(this.mStackScroller.getStackScroll(), z);
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            Task task = taskView.getTask();
            int indexOfStackTask = this.mStack.indexOfStackTask(task);
            if (indexOfStackTask != -1 && indexOfStackTask < this.mCurrentTaskTransforms.size()) {
                TaskViewTransform taskViewTransform = this.mCurrentTaskTransforms.get(indexOfStackTask);
                if (!this.mIgnoreTasks.contains(task.key)) {
                    if (task.isLaunchTarget && Recents.getConfiguration().getLaunchState().launchedViaFsGesture && this.mKeepAlphaWhenRelayout) {
                        taskViewTransform.alpha = taskView.getAlpha();
                    }
                    if (arrayMap != null && arrayMap.containsKey(task)) {
                        animationProps = arrayMap.get(task);
                    }
                    updateTaskViewToTransform(taskView, taskViewTransform, animationProps);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void relayoutTaskViewsOnNextFrame(AnimationProps animationProps) {
        this.mDeferredTaskViewLayoutAnimation = animationProps;
        invalidate();
    }

    public void updateTaskViewToTransform(TaskView taskView, TaskViewTransform taskViewTransform, AnimationProps animationProps) {
        if (!taskView.isAnimatingTo(taskViewTransform)) {
            taskView.cancelTransformAnimation();
            taskView.updateViewPropertiesToTaskTransform(taskViewTransform, animationProps, this.mRequestUpdateClippingListener);
        }
    }

    public void getCurrentTaskTransforms(ArrayList<Task> arrayList, ArrayList<TaskViewTransform> arrayList2) {
        Utilities.matchTaskListSize(arrayList, arrayList2);
        int focusState = this.mLayoutAlgorithm.getFocusState();
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            Task task = arrayList.get(size);
            TaskViewTransform taskViewTransform = arrayList2.get(size);
            TaskView childViewForTask = getChildViewForTask(task);
            if (childViewForTask != null) {
                taskViewTransform.fillIn(childViewForTask);
            } else {
                this.mLayoutAlgorithm.getStackTransform(task, this.mStackScroller.getStackScroll(), focusState, taskViewTransform, (TaskViewTransform) null, true, false);
            }
            taskViewTransform.visible = true;
        }
    }

    public void getLayoutTaskTransforms(float f, int i, ArrayList<Task> arrayList, boolean z, ArrayList<TaskViewTransform> arrayList2) {
        ArrayList<Task> arrayList3 = arrayList;
        ArrayList<TaskViewTransform> arrayList4 = arrayList2;
        Utilities.matchTaskListSize(arrayList3, arrayList4);
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            TaskViewTransform taskViewTransform = arrayList4.get(size);
            this.mLayoutAlgorithm.getStackTransform(arrayList3.get(size), f, i, taskViewTransform, (TaskViewTransform) null, true, z);
            taskViewTransform.visible = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelDeferredTaskViewLayoutAnimation() {
        this.mDeferredTaskViewLayoutAnimation = null;
    }

    /* access modifiers changed from: package-private */
    public void cancelAllTaskViewAnimations() {
        List<TaskView> taskViews = getTaskViews();
        for (int size = taskViews.size() - 1; size >= 0; size--) {
            TaskView taskView = taskViews.get(size);
            if (!this.mIgnoreTasks.contains(taskView.getTask().key)) {
                taskView.cancelTransformAnimation();
            }
        }
    }

    public void updateLayoutAlgorithm(boolean z) {
        this.mLayoutAlgorithm.update(this.mStack, this.mIgnoreTasks);
        if (Recents.getSystemServices().hasFreeformWorkspaceSupport()) {
            this.mTmpRect.set(this.mLayoutAlgorithm.mFreeformRect);
            this.mFreeformWorkspaceBackground.setBounds(this.mTmpRect);
        }
        if (z) {
            this.mStackScroller.boundScroll();
        }
    }

    private void updateLayoutToStableBounds() {
        if (this.mLayoutAlgorithm.setSystemInsets(this.mStableLayoutAlgorithm.mSystemInsets) || !this.mWindowRect.equals(this.mStableWindowRect) || !this.mStackBounds.equals(this.mStableStackBounds)) {
            this.mWindowRect.set(this.mStableWindowRect);
            this.mStackBounds.set(this.mStableStackBounds);
            this.mLayoutAlgorithm.initialize(this.mDisplayRect, this.mWindowRect, this.mStackBounds, TaskStackLayoutAlgorithm.StackState.getStackStateForStack(this.mStack));
            updateLayoutAlgorithm(true);
        }
    }

    public TaskStackViewScroller getScroller() {
        return this.mStackScroller;
    }

    /* access modifiers changed from: private */
    public boolean setFocusedTask(int i, boolean z, boolean z2) {
        return setFocusedTask(i, z, z2, 0);
    }

    private boolean setFocusedTask(int i, boolean z, boolean z2, int i2) {
        TaskView childViewForTask;
        int clamp = this.mStack.getTaskCount() > 0 ? Utilities.clamp(i, 0, this.mStack.getTaskCount() - 1) : -1;
        Task task = clamp != -1 ? this.mStack.getStackTasks().get(clamp) : null;
        Task task2 = this.mFocusedTask;
        if (task2 != null) {
            if (i2 > 0 && (childViewForTask = getChildViewForTask(task2)) != null) {
                childViewForTask.getHeaderView().cancelFocusTimerIndicator();
            }
            resetFocusedTask(this.mFocusedTask);
        }
        this.mFocusedTask = task;
        if (task == null) {
            return false;
        }
        if (i2 > 0) {
            TaskView childViewForTask2 = getChildViewForTask(task);
            if (childViewForTask2 != null) {
                childViewForTask2.getHeaderView().startFocusTimerIndicator(i2);
            } else {
                this.mStartTimerIndicatorDuration = i2;
            }
        }
        if (z) {
            if (!this.mEnterAnimationComplete) {
                cancelAllTaskViewAnimations();
            }
            this.mLayoutAlgorithm.clearUnfocusedTaskOverrides();
            return this.mAnimationHelper.startScrollToFocusedTaskAnimation(task, z2);
        }
        TaskView childViewForTask3 = getChildViewForTask(task);
        if (childViewForTask3 == null) {
            return false;
        }
        childViewForTask3.setFocusedState(true, z2);
        return false;
    }

    public void setRelativeFocusedTask(boolean z, boolean z2, boolean z3) {
        setRelativeFocusedTask(z, z2, z3, false, 0);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0046, code lost:
        if (r3.get(r6).isFreeformTask() == false) goto L_0x009e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00a7 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:47:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setRelativeFocusedTask(boolean r6, boolean r7, boolean r8, boolean r9, int r10) {
        /*
            r5 = this;
            com.android.systemui.recents.model.Task r8 = r5.getFocusedTask()
            com.android.systemui.recents.model.TaskStack r0 = r5.mStack
            int r0 = r0.indexOfStackTask(r8)
            r1 = -1
            r2 = 1
            if (r8 == 0) goto L_0x005a
            if (r7 == 0) goto L_0x0049
            com.android.systemui.recents.model.TaskStack r3 = r5.mStack
            java.util.ArrayList r3 = r3.getStackTasks()
            boolean r8 = r8.isFreeformTask()
            if (r8 == 0) goto L_0x002e
            com.android.systemui.recents.views.TaskView r6 = r5.getFrontMostTaskView(r7)
            if (r6 == 0) goto L_0x009f
            com.android.systemui.recents.model.TaskStack r7 = r5.mStack
            com.android.systemui.recents.model.Task r6 = r6.getTask()
            int r0 = r7.indexOfStackTask(r6)
            goto L_0x009f
        L_0x002e:
            if (r6 == 0) goto L_0x0032
            r6 = r1
            goto L_0x0033
        L_0x0032:
            r6 = r2
        L_0x0033:
            int r6 = r6 + r0
            if (r6 < 0) goto L_0x009f
            int r7 = r3.size()
            if (r6 >= r7) goto L_0x009f
            java.lang.Object r7 = r3.get(r6)
            com.android.systemui.recents.model.Task r7 = (com.android.systemui.recents.model.Task) r7
            boolean r7 = r7.isFreeformTask()
            if (r7 != 0) goto L_0x009f
            goto L_0x009e
        L_0x0049:
            com.android.systemui.recents.model.TaskStack r7 = r5.mStack
            int r7 = r7.getTaskCount()
            if (r7 <= 0) goto L_0x009f
            if (r6 == 0) goto L_0x0055
            r6 = r1
            goto L_0x0056
        L_0x0055:
            r6 = r2
        L_0x0056:
            int r0 = r0 + r6
            int r0 = r0 + r7
            int r0 = r0 % r7
            goto L_0x009f
        L_0x005a:
            com.android.systemui.recents.views.TaskStackViewScroller r7 = r5.mStackScroller
            float r7 = r7.getStackScroll()
            com.android.systemui.recents.model.TaskStack r8 = r5.mStack
            java.util.ArrayList r8 = r8.getStackTasks()
            int r0 = r8.size()
            if (r6 == 0) goto L_0x0085
            int r0 = r0 - r2
        L_0x006d:
            if (r0 < 0) goto L_0x009f
            com.android.systemui.recents.views.TaskStackLayoutAlgorithm r6 = r5.mLayoutAlgorithm
            java.lang.Object r3 = r8.get(r0)
            com.android.systemui.recents.model.Task r3 = (com.android.systemui.recents.model.Task) r3
            float r6 = r6.getStackScrollForTask(r3)
            int r6 = java.lang.Float.compare(r6, r7)
            if (r6 > 0) goto L_0x0082
            goto L_0x009f
        L_0x0082:
            int r0 = r0 + -1
            goto L_0x006d
        L_0x0085:
            r6 = 0
        L_0x0086:
            if (r6 >= r0) goto L_0x009e
            com.android.systemui.recents.views.TaskStackLayoutAlgorithm r3 = r5.mLayoutAlgorithm
            java.lang.Object r4 = r8.get(r6)
            com.android.systemui.recents.model.Task r4 = (com.android.systemui.recents.model.Task) r4
            float r3 = r3.getStackScrollForTask(r4)
            int r3 = java.lang.Float.compare(r3, r7)
            if (r3 < 0) goto L_0x009b
            goto L_0x009e
        L_0x009b:
            int r6 = r6 + 1
            goto L_0x0086
        L_0x009e:
            r0 = r6
        L_0x009f:
            if (r0 == r1) goto L_0x00b6
            boolean r5 = r5.setFocusedTask(r0, r2, r2, r10)
            if (r5 == 0) goto L_0x00b6
            if (r9 == 0) goto L_0x00b6
            com.android.systemui.recents.events.RecentsEventBus r5 = com.android.systemui.recents.events.RecentsEventBus.getDefault()
            com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent r6 = new com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent
            r7 = 0
            r6.<init>(r7)
            r5.send(r6)
        L_0x00b6:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.recents.views.TaskStackView.setRelativeFocusedTask(boolean, boolean, boolean, boolean, int):void");
    }

    /* access modifiers changed from: package-private */
    public void resetFocusedTask(Task task) {
        TaskView childViewForTask;
        if (!(task == null || (childViewForTask = getChildViewForTask(task)) == null)) {
            childViewForTask.setFocusedState(false, false);
        }
        this.mFocusedTask = null;
    }

    /* access modifiers changed from: package-private */
    public Task getFocusedTask() {
        return this.mFocusedTask;
    }

    /* access modifiers changed from: package-private */
    public Task getAccessibilityFocusedTask() {
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            if (Utilities.isDescendentAccessibilityFocused(taskView)) {
                return taskView.getTask();
            }
        }
        TaskView frontMostTaskView = getFrontMostTaskView(true);
        if (frontMostTaskView != null) {
            return frontMostTaskView.getTask();
        }
        return null;
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size();
        if (size > 0) {
            TaskView taskView = taskViews.get(size - 1);
            accessibilityEvent.setFromIndex(this.mStack.indexOfStackTask(taskViews.get(0).getTask()));
            accessibilityEvent.setToIndex(this.mStack.indexOfStackTask(taskView.getTask()));
            accessibilityEvent.setContentDescription(taskView.getTask().title);
        }
        accessibilityEvent.setItemCount(this.mStack.getTaskCount());
        float height = (float) this.mLayoutAlgorithm.mStackRect.height();
        accessibilityEvent.setScrollY((int) (this.mStackScroller.getStackScroll() * height));
        accessibilityEvent.setMaxScrollY((int) (this.mLayoutAlgorithm.mMaxScrollP * height));
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (getTaskViews().size() > 1) {
            Task accessibilityFocusedTask = getAccessibilityFocusedTask();
            accessibilityNodeInfo.setScrollable(true);
            int indexOfStackTask = this.mStack.indexOfStackTask(accessibilityFocusedTask);
            if (indexOfStackTask > 0) {
                accessibilityNodeInfo.addAction(8192);
            }
            if (indexOfStackTask >= 0 && indexOfStackTask < this.mStack.getTaskCount() - 1) {
                accessibilityNodeInfo.addAction(4096);
            }
        }
    }

    public CharSequence getAccessibilityClassName() {
        return ScrollView.class.getName();
    }

    public boolean performAccessibilityAction(int i, Bundle bundle) {
        if (super.performAccessibilityAction(i, bundle)) {
            return true;
        }
        int indexOfStackTask = this.mStack.indexOfStackTask(getAccessibilityFocusedTask());
        if (indexOfStackTask >= 0 && indexOfStackTask < this.mStack.getTaskCount()) {
            if (i == 4096) {
                setFocusedTask(indexOfStackTask + 1, true, true, 0);
                return true;
            } else if (i == 8192) {
                setFocusedTask(indexOfStackTask - 1, true, true, 0);
                return true;
            }
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return this.mTouchHandler.onInterceptTouchEvent(motionEvent);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.mTouchHandler.onTouchEvent(motionEvent);
    }

    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        return this.mTouchHandler.onGenericMotionEvent(motionEvent);
    }

    public void computeScroll() {
        if (this.mStackScroller.computeScroll()) {
            sendAccessibilityEvent(4096);
        }
        AnimationProps animationProps = this.mDeferredTaskViewLayoutAnimation;
        if (animationProps != null) {
            relayoutTaskViews(animationProps);
            this.mTaskViewsClipDirty = true;
            this.mDeferredTaskViewLayoutAnimation = null;
        }
        RecentsEventBus.getDefault().send(new StackScrollChangedEvent((int) ((-this.mStackScroller.getStackScroll()) * ((float) this.mLayoutAlgorithm.mTaskRect.height()))));
        if (this.mTaskViewsClipDirty) {
            clipTaskViews();
        }
    }

    public TaskStackLayoutAlgorithm.VisibilityReport computeStackVisibilityReport() {
        return this.mLayoutAlgorithm.computeStackVisibilityReport(this.mStack.getStackTasks());
    }

    public void setSystemInsets(Rect rect) {
        if (this.mLayoutAlgorithm.setSystemInsets(rect) || (this.mStableLayoutAlgorithm.setSystemInsets(rect) | false)) {
            requestLayout();
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        boolean z = true;
        this.mInMeasureLayout = true;
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        TaskStackLayoutAlgorithm taskStackLayoutAlgorithm = this.mLayoutAlgorithm;
        Rect rect = this.mDisplayRect;
        Rect rect2 = new Rect(0, 0, size, size2);
        Rect rect3 = this.mLayoutAlgorithm.mSystemInsets;
        taskStackLayoutAlgorithm.getTaskStackBounds(rect, rect2, rect3.top, rect3.left, rect3.right, this.mTmpRect);
        if (!this.mTmpRect.equals(this.mStableStackBounds) && !this.mIsMultiStateChanging) {
            this.mStableStackBounds.set(this.mTmpRect);
            this.mStackBounds.set(this.mTmpRect);
            this.mStableWindowRect.set(0, 0, size, size2);
            this.mWindowRect.set(0, 0, size, size2);
        }
        this.mStableLayoutAlgorithm.initialize(this.mDisplayRect, this.mStableWindowRect, this.mStableStackBounds, TaskStackLayoutAlgorithm.StackState.getStackStateForStack(this.mStack));
        this.mLayoutAlgorithm.initialize(this.mDisplayRect, this.mWindowRect, this.mStackBounds, TaskStackLayoutAlgorithm.StackState.getStackStateForStack(this.mStack));
        updateLayoutAlgorithm(false);
        if ((size == this.mLastWidth && size2 == this.mLastHeight) || !this.mResetToInitialStateWhenResized) {
            z = false;
        }
        if (this.mAwaitingFirstLayout || this.mInitialState != 0 || z) {
            if (this.mInitialState != 2 || z) {
                updateToInitialState();
                this.mResetToInitialStateWhenResized = false;
            }
            if (!this.mAwaitingFirstLayout) {
                this.mInitialState = 0;
            }
        }
        bindVisibleTaskViews(this.mStackScroller.getStackScroll(), false);
        this.mTmpTaskViews.clear();
        this.mTmpTaskViews.addAll(getTaskViews());
        this.mTmpTaskViews.addAll(this.mViewPool.getViews());
        int size3 = this.mTmpTaskViews.size();
        for (int i3 = 0; i3 < size3; i3++) {
            measureTaskView(this.mTmpTaskViews.get(i3));
        }
        measureMaskView(size, size2);
        setMeasuredDimension(size, size2);
        this.mLastWidth = size;
        this.mLastHeight = size2;
        this.mInMeasureLayout = false;
    }

    private void measureTaskView(TaskView taskView) {
        Rect rect = new Rect();
        if (taskView.getBackground() != null) {
            taskView.getBackground().getPadding(rect);
        }
        this.mTmpRect.set(this.mStableLayoutAlgorithm.mTaskRect);
        this.mTmpRect.union(this.mLayoutAlgorithm.mTaskRect);
        taskView.measure(View.MeasureSpec.makeMeasureSpec(this.mTmpRect.width() + rect.left + rect.right, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mTmpRect.height() + rect.top + rect.bottom, 1073741824));
    }

    private void measureMaskView(int i, int i2) {
        if (this.mMaskWithMenu.getVisibility() != 8) {
            measureChild(this.mMaskWithMenu, View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(i2, 1073741824));
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (!this.mDeleteAllTasksAnimating) {
            this.mTmpTaskViews.clear();
            this.mTmpTaskViews.addAll(getTaskViews());
            this.mTmpTaskViews.addAll(this.mViewPool.getViews());
            int size = this.mTmpTaskViews.size();
            for (int i5 = 0; i5 < size; i5++) {
                layoutTaskView(z, this.mTmpTaskViews.get(i5));
                this.mTmpTaskViews.get(i5).getViewBounds().reset();
            }
            layoutMaskView();
            if (z && this.mStackScroller.isScrollOutOfBounds()) {
                this.mStackScroller.boundScroll();
            }
            relayoutTaskViews(AnimationProps.IMMEDIATE);
            clipTaskViews();
            if (this.mAwaitingFirstLayout) {
                this.mInitialState = 0;
                onFirstLayout();
                if (this.mStackReloaded) {
                    this.mAwaitingFirstLayout = false;
                    tryStartEnterAnimation();
                }
            }
        }
    }

    private void layoutTaskView(boolean z, TaskView taskView) {
        Task task = taskView.getTask();
        if (task != null && this.mIgnoreTasks.contains(task.key)) {
            return;
        }
        if (z) {
            Rect rect = new Rect();
            if (taskView.getBackground() != null) {
                taskView.getBackground().getPadding(rect);
            }
            this.mTmpRect.set(this.mStableLayoutAlgorithm.mTaskRect);
            this.mTmpRect.union(this.mLayoutAlgorithm.mTaskRect);
            taskView.cancelTransformAnimation();
            Rect rect2 = this.mTmpRect;
            taskView.layout(rect2.left - rect.left, rect2.top - rect.top, rect2.right + rect.right, rect2.bottom + rect.bottom);
            return;
        }
        taskView.layout(taskView.getLeft(), taskView.getTop(), taskView.getRight(), taskView.getBottom());
    }

    private void layoutMaskView() {
        if (this.mMaskWithMenu.getVisibility() != 8) {
            this.mMaskWithMenu.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
        }
    }

    /* access modifiers changed from: package-private */
    public void onFirstLayout() {
        this.mAnimationHelper.prepareForEnterAnimation();
        animateFreeformWorkspaceBackgroundAlpha(this.mLayoutAlgorithm.getStackState().freeformBackgroundAlpha, new AnimationProps(150, Interpolators.FAST_OUT_SLOW_IN));
        int initialFocusTaskIndex = Recents.getConfiguration().getLaunchState().getInitialFocusTaskIndex(this.mStack.getTaskCount());
        if (initialFocusTaskIndex != -1) {
            setFocusedTask(initialFocusTaskIndex, false, false);
        }
        if (this.mStackScroller.getStackScroll() >= 0.3f || this.mStack.getTaskCount() <= 0) {
            RecentsEventBus.getDefault().send(new HideStackActionButtonEvent());
        } else {
            RecentsEventBus.getDefault().send(new ShowStackActionButtonEvent(false));
        }
    }

    public boolean isTouchPointInView(float f, float f2, TaskView taskView) {
        this.mTmpRect.set(taskView.getLeft(), taskView.getTop(), taskView.getRight(), taskView.getBottom());
        this.mTmpRect.offset((int) taskView.getTranslationX(), (int) taskView.getTranslationY());
        return this.mTmpRect.contains((int) f, (int) f2);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (Recents.getSystemServices().hasFreeformWorkspaceSupport() && this.mFreeformWorkspaceBackground.getAlpha() > 0) {
            this.mFreeformWorkspaceBackground.draw(canvas);
        }
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable drawable) {
        if (drawable == this.mFreeformWorkspaceBackground) {
            return true;
        }
        return super.verifyDrawable(drawable);
    }

    public boolean launchFreeformTasks() {
        Task task;
        ArrayList<Task> freeformTasks = this.mStack.getFreeformTasks();
        if (freeformTasks.isEmpty() || (task = freeformTasks.get(freeformTasks.size() - 1)) == null || !task.isFreeformTask()) {
            return false;
        }
        RecentsEventBus.getDefault().send(new LaunchTaskEvent(getChildViewForTask(task), task, (Rect) null, -1, false));
        return true;
    }

    public void onStackTaskAdded(TaskStack taskStack, Task task) {
        AnimationProps animationProps;
        updateLayoutAlgorithm(true);
        if (this.mAwaitingFirstLayout) {
            animationProps = AnimationProps.IMMEDIATE;
        } else {
            animationProps = new AnimationProps(200, Interpolators.FAST_OUT_SLOW_IN);
        }
        relayoutTaskViews(animationProps);
    }

    public void onStackTaskRemoved(TaskStack taskStack, Task task, Task task2, AnimationProps animationProps, boolean z, boolean z2) {
        TaskView childViewForTask;
        if (this.mFocusedTask == task) {
            resetFocusedTask(task);
        }
        TaskView childViewForTask2 = getChildViewForTask(task);
        if (childViewForTask2 != null) {
            this.mViewPool.returnViewToPool(childViewForTask2);
        }
        removeIgnoreTask(task);
        if (animationProps != null) {
            updateLayoutAlgorithm(true);
            relayoutTaskViews(animationProps);
        }
        if (!(!this.mScreenPinningEnabled || task2 == null || (childViewForTask = getChildViewForTask(task2)) == null)) {
            childViewForTask.showActionButton(true, 200);
        }
        if (this.mStack.getTaskCount() == 0 && z2) {
            RecentsEventBus.getDefault().send(new AllTaskViewsDismissedEvent(Recents.getSystemServices().hasDockedTask() ? R.string.recents_empty_message_multi_window : R.string.recents_empty_message, true, z));
        }
    }

    public void onStackTasksRemoved(TaskStack taskStack) {
        if (taskStack.getTaskCount() == 0) {
            resetFocusedTask(getFocusedTask());
        }
        ArrayList arrayList = new ArrayList();
        for (TaskView next : getTaskViews()) {
            if (next.getTask() == null || !next.getTask().isProtected()) {
                arrayList.add(next);
            }
        }
        boolean z = true;
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            this.mViewPool.returnViewToPool((TaskView) arrayList.get(size));
        }
        this.mIgnoreTasks.clear();
        RecentsEventBus recentsEventBus = RecentsEventBus.getDefault();
        int i = Recents.getSystemServices().hasDockedTask() ? R.string.recents_empty_message_multi_window : R.string.recents_empty_message_dismissed_all;
        if (taskStack.getTaskCount() != 0) {
            z = false;
        }
        recentsEventBus.send(new AllTaskViewsDismissedEvent(i, z));
    }

    public void onStackTasksUpdated(TaskStack taskStack) {
        updateLayoutAlgorithm(false);
        relayoutTaskViews(AnimationProps.IMMEDIATE);
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            bindTaskView(taskView, taskView.getTask());
        }
    }

    public TaskView createView(Context context) {
        return (TaskView) this.mInflater.inflate(R.layout.recents_task_view, this, false);
    }

    public void onReturnViewToPool(TaskView taskView) {
        unbindTaskView(taskView, taskView.getTask());
        taskView.clearAccessibilityFocus();
        taskView.resetViewProperties();
        taskView.setFocusedState(false, false);
        taskView.setClipViewInStack(false);
        if (this.mScreenPinningEnabled) {
            taskView.hideActionButton(false, 0, false, (Animator.AnimatorListener) null);
        }
        if (taskView == findFocus()) {
            clearChildFocus(taskView);
        }
        try {
            detachViewFromParent(taskView);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e("TaskStackView", e.getMessage());
        }
        updateTaskViewsList();
    }

    public void onPickUpViewFromPool(TaskView taskView, Task task, boolean z) {
        int findTaskViewInsertIndex = findTaskViewInsertIndex(task, this.mStack.indexOfStackTask(task));
        if (!z) {
            attachViewToParent(taskView, findTaskViewInsertIndex, taskView.getLayoutParams());
        } else if (this.mInMeasureLayout) {
            addView(taskView, findTaskViewInsertIndex);
        } else {
            ViewGroup.LayoutParams layoutParams = taskView.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = generateDefaultLayoutParams();
            }
            addViewInLayout(taskView, findTaskViewInsertIndex, layoutParams, true);
            measureTaskView(taskView);
            layoutTaskView(true, taskView);
        }
        updateTaskViewsList();
        bindTaskView(taskView, task);
        if (this.mUIDozeTrigger.isAsleep()) {
            taskView.setNoUserInteractionState();
        }
        taskView.setCallbacks(this);
        taskView.setTouchEnabled(true);
        taskView.setClipViewInStack(true);
        taskView.setImportantForAccessibility(0);
        if (this.mFocusedTask == task) {
            taskView.setFocusedState(true, false);
            if (this.mStartTimerIndicatorDuration > 0) {
                taskView.getHeaderView().startFocusTimerIndicator(this.mStartTimerIndicatorDuration);
                this.mStartTimerIndicatorDuration = 0;
            }
        }
        if (this.mScreenPinningEnabled && taskView.getTask() == this.mStack.getStackFrontMostTask(false)) {
            taskView.showActionButton(false, 0);
        }
    }

    public boolean hasPreferredData(TaskView taskView, Task task) {
        return taskView.getTask() == task;
    }

    private void bindTaskView(TaskView taskView, Task task) {
        taskView.onTaskBound(task, this.mTouchExplorationEnabled, this.mDisplayOrientation, this.mDisplayRect);
        Recents.getTaskLoader().loadTaskData(task);
    }

    private void unbindTaskView(TaskView taskView, Task task) {
        Recents.getTaskLoader().unloadTaskData(task);
    }

    public void onTaskViewClipStateChanged(TaskView taskView) {
        if (!this.mTaskViewsClipDirty) {
            this.mTaskViewsClipDirty = true;
            invalidate();
        }
    }

    public void onFocusStateChanged(int i, int i2) {
        if (this.mDeferredTaskViewLayoutAnimation == null) {
            this.mUIDozeTrigger.poke();
            relayoutTaskViewsOnNextFrame(AnimationProps.IMMEDIATE);
        }
    }

    public void onStackScrollChanged(float f, float f2, AnimationProps animationProps) {
        this.mUIDozeTrigger.poke();
        if (animationProps != null) {
            relayoutTaskViewsOnNextFrame(animationProps);
        }
        if (!this.mEnterAnimationComplete) {
            return;
        }
        if (f > 0.3f && f2 <= 0.3f && this.mStack.getTaskCount() > 0) {
            RecentsEventBus.getDefault().send(new ShowStackActionButtonEvent(true));
        } else if (f < 0.3f && f2 >= 0.3f) {
            RecentsEventBus.getDefault().send(new HideStackActionButtonEvent());
        }
    }

    public final void onBusEvent(PackagesChangedEvent packagesChangedEvent) {
        ArraySet<ComponentName> computeComponentsRemoved = this.mStack.computeComponentsRemoved(packagesChangedEvent.packageName, packagesChangedEvent.userId);
        ArrayList<Task> stackTasks = this.mStack.getStackTasks();
        for (int size = stackTasks.size() - 1; size >= 0; size--) {
            Task task = stackTasks.get(size);
            if (computeComponentsRemoved.contains(task.key.getComponent())) {
                TaskView childViewForTask = getChildViewForTask(task);
                if (childViewForTask != null) {
                    childViewForTask.dismissTask();
                } else {
                    this.mStack.removeTask(task, AnimationProps.IMMEDIATE, false);
                }
            }
        }
    }

    public final void onBusEvent(LaunchTaskEvent launchTaskEvent) {
        this.mUIDozeTrigger.stopDozing();
    }

    public final void onBusEvent(LaunchNextTaskRequestEvent launchNextTaskRequestEvent) {
        int i;
        TaskView childViewForTask = getChildViewForTask(this.mStack.getLaunchTarget());
        if (childViewForTask != null) {
            childViewForTask.onLaunchNextTask();
        }
        TaskStack taskStack = this.mStack;
        int indexOfStackTask = taskStack.indexOfStackTask(taskStack.getLaunchTarget());
        if (indexOfStackTask != -1) {
            i = Utilities.clamp(indexOfStackTask + 1, 0, this.mStack.getTaskCount() - 1);
        } else {
            i = Math.min(this.mStack.getTaskCount() - 1, 0);
        }
        if (this.mStack.getTaskCount() == 0) {
            RecentsEventBus.getDefault().send(new HideRecentsEvent(false, true, false));
        } else if (i != -1) {
            cancelAllTaskViewAnimations();
            final Task task = this.mStack.getStackTasks().get(i);
            float abs = Math.abs(0.0f - this.mStackScroller.getStackScroll());
            if (getChildViewForTask(task) == null || abs > 0.35f) {
                this.mStackScroller.animateScroll(0.0f, (int) ((abs * 32.0f) + 216.0f), new Runnable() {
                    public void run() {
                        RecentsEventBus.getDefault().send(new LaunchTaskEvent(TaskStackView.this.getChildViewForTask(task), task, (Rect) null, -1, false));
                    }
                });
            } else {
                RecentsEventBus.getDefault().send(new LaunchTaskEvent(getChildViewForTask(task), task, (Rect) null, -1, false));
            }
            MetricsLogger.action(getContext(), 318, task.key.getComponent().toString());
        }
    }

    public final void onBusEvent(LaunchTaskStartedEvent launchTaskStartedEvent) {
        this.mAnimationHelper.startLaunchTaskAnimation(launchTaskStartedEvent.taskView, launchTaskStartedEvent.screenPinningRequested, launchTaskStartedEvent.getAnimationTrigger());
    }

    public final void onBusEvent(DismissRecentsToHomeAnimationStarted dismissRecentsToHomeAnimationStarted) {
        this.mTouchHandler.cancelNonDismissTaskAnimations();
        this.mStackScroller.stopScroller();
        this.mStackScroller.stopBoundScrollAnimation();
        cancelDeferredTaskViewLayoutAnimation();
        this.mAnimationHelper.startExitToHomeAnimation(dismissRecentsToHomeAnimationStarted.animated, dismissRecentsToHomeAnimationStarted.getAnimationTrigger());
        animateFreeformWorkspaceBackgroundAlpha(0, new AnimationProps(350, Interpolators.FAST_OUT_SLOW_IN));
    }

    public final void onBusEvent(DismissFocusedTaskViewEvent dismissFocusedTaskViewEvent) {
        Task task = this.mFocusedTask;
        if (task != null) {
            TaskView childViewForTask = getChildViewForTask(task);
            if (childViewForTask != null) {
                childViewForTask.dismissTask();
            }
            resetFocusedTask(this.mFocusedTask);
        }
    }

    public final void onBusEvent(DismissTaskViewEvent dismissTaskViewEvent) {
        this.mAnimationHelper.startDeleteTaskAnimation(dismissTaskViewEvent.taskView, dismissTaskViewEvent.getAnimationTrigger());
    }

    public final void onBusEvent(final DismissAllTaskViewsEvent dismissAllTaskViewsEvent) {
        this.mStackScroller.stopScroller();
        ArrayList arrayList = new ArrayList(this.mStack.getStackTasks());
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            Task task = (Task) arrayList.get(size);
            if (!task.isProtected()) {
                RecentsEventBus.getDefault().send(new DeleteTaskDataEvent(task, true));
            }
        }
        Slog.d("TaskStackView", "removeAllTask, cleanByRecents=true");
        AnonymousClass6 r0 = new Runnable() {
            int count = 0;

            public void run() {
                int i;
                if (TaskStackView.this.mTaskEnterAnimationComplete || (i = this.count) >= 35) {
                    TaskStackView.this.mAnimationHelper.startDeleteAllTasksAnimation(TaskStackView.this.getTaskViews(), dismissAllTaskViewsEvent.getAnimationTrigger());
                    boolean unused = TaskStackView.this.mDeleteAllTasksAnimating = true;
                    dismissAllTaskViewsEvent.addPostAnimationCallback(new Runnable() {
                        public void run() {
                            TaskStackView taskStackView = TaskStackView.this;
                            taskStackView.announceForAccessibility(taskStackView.getContext().getString(R.string.accessibility_recents_all_items_dismissed));
                            TaskStackView.this.mStack.removeAllTasks();
                            boolean unused = TaskStackView.this.mDeleteAllTasksAnimating = false;
                            MetricsLogger.action(TaskStackView.this.getContext(), 357);
                        }
                    });
                    return;
                }
                this.count = i + 1;
                TaskStackView.this.postDelayed(this, 15);
            }
        };
        this.mDeleteAllTasksAnimationRunnable = r0;
        post(r0);
    }

    public final void onBusEvent(TaskViewDismissedEvent taskViewDismissedEvent) {
        announceForAccessibility(getContext().getString(R.string.accessibility_recents_item_dismissed, new Object[]{taskViewDismissedEvent.task.title}));
        this.mStack.removeTask(taskViewDismissedEvent.task, taskViewDismissedEvent.animation, false);
        RecentsEventBus.getDefault().send(new DeleteTaskDataEvent(taskViewDismissedEvent.task));
        MetricsLogger.action(getContext(), 289, taskViewDismissedEvent.task.key.getComponent().toString());
    }

    public final void onBusEvent(FocusNextTaskViewEvent focusNextTaskViewEvent) {
        this.mStackScroller.stopScroller();
        this.mStackScroller.stopBoundScrollAnimation();
        setRelativeFocusedTask(true, false, true, false, focusNextTaskViewEvent.timerIndicatorDuration);
    }

    public final void onBusEvent(FocusPreviousTaskViewEvent focusPreviousTaskViewEvent) {
        this.mStackScroller.stopScroller();
        this.mStackScroller.stopBoundScrollAnimation();
        setRelativeFocusedTask(false, false, true);
    }

    public final void onBusEvent(UserInteractionEvent userInteractionEvent) {
        Task task;
        TaskView childViewForTask;
        this.mUIDozeTrigger.poke();
        if (Recents.getDebugFlags().isFastToggleRecentsEnabled() && (task = this.mFocusedTask) != null && (childViewForTask = getChildViewForTask(task)) != null) {
            childViewForTask.getHeaderView().cancelFocusTimerIndicator();
        }
    }

    public final void onBusEvent(DragStartEvent dragStartEvent) {
        addIgnoreTask(dragStartEvent.task);
        if (dragStartEvent.task.isFreeformTask()) {
            this.mStackScroller.animateScroll(this.mLayoutAlgorithm.mInitialScrollP, (Runnable) null);
        }
        SpringAnimationUtils.getInstance().startTaskViewDragStartAnim(dragStartEvent.taskView);
    }

    public final void onBusEvent(DragStartInitializeDropTargetsEvent dragStartInitializeDropTargetsEvent) {
        if (Recents.getSystemServices().hasFreeformWorkspaceSupport()) {
            dragStartInitializeDropTargetsEvent.handler.registerDropTargetForCurrentDrag(this.mStackDropTarget);
            dragStartInitializeDropTargetsEvent.handler.registerDropTargetForCurrentDrag(this.mFreeformWorkspaceDropTarget);
        }
    }

    public final void onBusEvent(DragDropTargetChangedEvent dragDropTargetChangedEvent) {
        AnimationProps animationProps = new AnimationProps(250, Interpolators.FAST_OUT_SLOW_IN);
        DropTarget dropTarget = dragDropTargetChangedEvent.dropTarget;
        boolean z = true;
        if (dropTarget instanceof TaskStack.DockState) {
            Rect rect = new Rect(this.mStableLayoutAlgorithm.mSystemInsets);
            int measuredHeight = getMeasuredHeight() - rect.bottom;
            rect.set(rect.left, rect.top, rect.right, 0);
            this.mStackBounds.set(((TaskStack.DockState) dropTarget).getDockedTaskStackBounds(this.mDisplayRect, getMeasuredWidth(), measuredHeight, this.mDividerSize, rect, this.mLayoutAlgorithm, getResources(), this.mWindowRect));
            TaskStackLayoutAlgorithm taskStackLayoutAlgorithm = this.mLayoutAlgorithm;
            taskStackLayoutAlgorithm.mDropToDockState = true;
            taskStackLayoutAlgorithm.setSystemInsets(rect);
            this.mLayoutAlgorithm.initialize(this.mDisplayRect, this.mWindowRect, this.mStackBounds, TaskStackLayoutAlgorithm.StackState.getStackStateForStack(this.mStack));
            updateLayoutAlgorithm(true);
        } else {
            removeIgnoreTask(dragDropTargetChangedEvent.task);
            updateLayoutToStableBounds();
            addIgnoreTask(dragDropTargetChangedEvent.task);
            z = false;
        }
        relayoutTaskViews(animationProps, (ArrayMap<Task, AnimationProps>) null, z);
        this.mLayoutAlgorithm.mDropToDockState = false;
    }

    public final void onBusEvent(final DragEndEvent dragEndEvent) {
        if (dragEndEvent.dropTarget instanceof TaskStack.DockState) {
            this.mLayoutAlgorithm.clearUnfocusedTaskOverrides();
            return;
        }
        boolean isFreeformTask = dragEndEvent.task.isFreeformTask();
        if ((!isFreeformTask && dragEndEvent.dropTarget == this.mFreeformWorkspaceDropTarget) || (isFreeformTask && dragEndEvent.dropTarget == this.mStackDropTarget)) {
            DropTarget dropTarget = dragEndEvent.dropTarget;
            if (dropTarget == this.mFreeformWorkspaceDropTarget) {
                this.mStack.moveTaskToStack(dragEndEvent.task, 2);
            } else if (dropTarget == this.mStackDropTarget) {
                this.mStack.moveTaskToStack(dragEndEvent.task, 1);
            }
            updateLayoutAlgorithm(true);
            dragEndEvent.addPostAnimationCallback(new Runnable() {
                public void run() {
                    SystemServicesProxy systemServices = Recents.getSystemServices();
                    Task.TaskKey taskKey = dragEndEvent.task.key;
                    systemServices.moveTaskToStack(taskKey.id, taskKey.stackId);
                }
            });
        }
        removeIgnoreTask(dragEndEvent.task);
        SpringAnimationUtils.getInstance().startTaskViewDragEndAnim(dragEndEvent.taskView);
    }

    public final void onBusEvent(DragEndCancelledEvent dragEndCancelledEvent) {
        removeIgnoreTask(dragEndCancelledEvent.task);
        updateLayoutToStableBounds();
        Utilities.setViewFrameFromTranslation(dragEndCancelledEvent.taskView);
        new ArrayMap().put(dragEndCancelledEvent.task, new AnimationProps(250, Interpolators.FAST_OUT_SLOW_IN, dragEndCancelledEvent.getAnimationTrigger().decrementOnAnimationEnd()));
        relayoutTaskViews(new AnimationProps(250, Interpolators.FAST_OUT_SLOW_IN));
        dragEndCancelledEvent.getAnimationTrigger().increment();
    }

    public final void onBusEvent(IterateRecentsEvent iterateRecentsEvent) {
        if (!this.mEnterAnimationComplete) {
            RecentsEventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent((Task) null));
        }
    }

    public final void onBusEvent(EnterRecentsWindowAnimationCompletedEvent enterRecentsWindowAnimationCompletedEvent) {
        this.mEnterAnimationComplete = true;
        tryStartEnterAnimation();
    }

    private void tryStartEnterAnimation() {
        if (this.mStackReloaded && !this.mAwaitingFirstLayout) {
            if (this.mEnterAnimationComplete || sIsChangingConfigurations) {
                if (this.mStack.getTaskCount() > 0) {
                    ReferenceCountedTrigger referenceCountedTrigger = new ReferenceCountedTrigger();
                    referenceCountedTrigger.addLastDecrementRunnable(new Runnable() {
                        public void run() {
                            TaskStackView.this.mUIDozeTrigger.startDozing();
                            if (TaskStackView.this.mFocusedTask != null) {
                                RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
                                TaskStackView taskStackView = TaskStackView.this;
                                boolean unused = taskStackView.setFocusedTask(taskStackView.mStack.indexOfStackTask(TaskStackView.this.mFocusedTask), false, launchState.launchedWithAltTab);
                                TaskStackView taskStackView2 = TaskStackView.this;
                                TaskView childViewForTask = taskStackView2.getChildViewForTask(taskStackView2.mFocusedTask);
                                if (TaskStackView.this.mTouchExplorationEnabled && childViewForTask != null) {
                                    childViewForTask.requestAccessibilityFocus();
                                }
                            }
                            boolean unused2 = TaskStackView.this.mTaskEnterAnimationComplete = true;
                            RecentsEventBus.getDefault().send(new EnterRecentsTaskStackAnimationCompletedEvent());
                        }
                    });
                    this.mAnimationHelper.startEnterAnimation(referenceCountedTrigger);
                }
                this.mStackReloaded = false;
            }
        }
    }

    public final void onBusEvent(UpdateFreeformTaskViewVisibilityEvent updateFreeformTaskViewVisibilityEvent) {
        List<TaskView> taskViews = getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            if (taskView.getTask().isFreeformTask()) {
                taskView.setVisibility(updateFreeformTaskViewVisibilityEvent.visible ? 0 : 4);
            }
        }
    }

    public final void onBusEvent(final MultiWindowStateChangedEvent multiWindowStateChangedEvent) {
        this.mIsMultiStateChanging = false;
        if (multiWindowStateChangedEvent.inMultiWindow || !multiWindowStateChangedEvent.showDeferredAnimation) {
            setTasks(multiWindowStateChangedEvent.stack, true);
            return;
        }
        Recents.getConfiguration().getLaunchState().reset();
        multiWindowStateChangedEvent.getAnimationTrigger().increment();
        post(new Runnable() {
            public void run() {
                TaskStackAnimationHelper access$200 = TaskStackView.this.mAnimationHelper;
                MultiWindowStateChangedEvent multiWindowStateChangedEvent = multiWindowStateChangedEvent;
                access$200.startNewStackScrollAnimation(multiWindowStateChangedEvent.stack, multiWindowStateChangedEvent.getAnimationTrigger());
                multiWindowStateChangedEvent.getAnimationTrigger().decrement();
            }
        });
    }

    public final void onBusEvent(ConfigurationChangedEvent configurationChangedEvent) {
        if (configurationChangedEvent.fromDeviceOrientationChange) {
            this.mDisplayOrientation = Utilities.getAppConfiguration(this.mContext).orientation;
            this.mDisplayRect = Recents.getSystemServices().getDisplayRect();
            this.mStackScroller.stopScroller();
        }
        reloadOnConfigurationChange();
        if (!configurationChangedEvent.fromMultiWindow) {
            this.mTmpTaskViews.clear();
            this.mTmpTaskViews.addAll(getTaskViews());
            this.mTmpTaskViews.addAll(this.mViewPool.getViews());
            int size = this.mTmpTaskViews.size();
            for (int i = 0; i < size; i++) {
                this.mTmpTaskViews.get(i).onConfigurationChanged();
            }
        }
        if (configurationChangedEvent.fromMultiWindow) {
            this.mInitialState = 2;
            requestLayout();
        } else if (configurationChangedEvent.fromDeviceOrientationChange) {
            this.mInitialState = 1;
            requestLayout();
        }
    }

    public final void onBusEvent(RecentsGrowingEvent recentsGrowingEvent) {
        this.mResetToInitialStateWhenResized = true;
    }

    public final void onBusEvent(RecentsVisibilityChangedEvent recentsVisibilityChangedEvent) {
        if (recentsVisibilityChangedEvent.visible) {
            updateLayoutToStableBounds();
            return;
        }
        ArrayList arrayList = new ArrayList(getTaskViews());
        for (int i = 0; i < arrayList.size(); i++) {
            this.mViewPool.returnViewToPool((TaskView) arrayList.get(i));
        }
    }

    public final void onBusEvent(ClickTaskViewToLaunchTaskEvent clickTaskViewToLaunchTaskEvent) {
        RecentsPushEventHelper.sendSwitchAppEvent("clickToSwitch", this.mStack.indexOfStackTask(clickTaskViewToLaunchTaskEvent.task));
        this.mTouchHandler.setAllowHideRecentsFromBackgroundTap(false);
    }

    public final void onBusEvent(AnimFirstTaskViewAlphaEvent animFirstTaskViewAlphaEvent) {
        TaskView frontMostTaskView = getFrontMostTaskView(true);
        if (frontMostTaskView != null && frontMostTaskView.getHeaderView() != null && frontMostTaskView.getThumbnailView() != null) {
            frontMostTaskView.changeChildrenAlpha(animFirstTaskViewAlphaEvent.mWithAnim, animFirstTaskViewAlphaEvent.mAlpha);
            this.mKeepAlphaWhenRelayout = animFirstTaskViewAlphaEvent.mKeepAlphaWhenRelayout;
        }
    }

    public final void onBusEvent(RotationChangedEvent rotationChangedEvent) {
        this.mLayoutAlgorithm.updatePaddingOfNotch(rotationChangedEvent.rotation);
        this.mStableLayoutAlgorithm.updatePaddingOfNotch(rotationChangedEvent.rotation);
    }

    public final void onBusEvent(RecentsActivityStartingEvent recentsActivityStartingEvent) {
        this.mTouchHandler.setAllowHideRecentsFromBackgroundTap(true);
    }

    public void reloadOnConfigurationChange() {
        this.mStableLayoutAlgorithm.reloadOnConfigurationChange(getContext());
        this.mLayoutAlgorithm.reloadOnConfigurationChange(getContext());
    }

    private void animateFreeformWorkspaceBackgroundAlpha(int i, AnimationProps animationProps) {
        if (this.mFreeformWorkspaceBackground.getAlpha() != i) {
            Utilities.cancelAnimationWithoutCallbacks(this.mFreeformWorkspaceBackgroundAnimator);
            GradientDrawable gradientDrawable = this.mFreeformWorkspaceBackground;
            ObjectAnimator ofInt = ObjectAnimator.ofInt(gradientDrawable, Utilities.DRAWABLE_ALPHA, new int[]{gradientDrawable.getAlpha(), i});
            this.mFreeformWorkspaceBackgroundAnimator = ofInt;
            ofInt.setStartDelay(animationProps.getDuration(4));
            this.mFreeformWorkspaceBackgroundAnimator.setDuration(animationProps.getDuration(4));
            this.mFreeformWorkspaceBackgroundAnimator.setInterpolator(animationProps.getInterpolator(4));
            this.mFreeformWorkspaceBackgroundAnimator.start();
        }
    }

    private int findTaskViewInsertIndex(Task task, int i) {
        if (i != -1) {
            List<TaskView> taskViews = getTaskViews();
            int size = taskViews.size();
            boolean z = false;
            for (int i2 = 0; i2 < size; i2++) {
                Task task2 = taskViews.get(i2).getTask();
                if (task2 == task) {
                    z = true;
                } else if (i < this.mStack.indexOfStackTask(task2)) {
                    return z ? i2 - 1 : i2;
                }
            }
        }
        return -1;
    }

    private void readSystemFlags() {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        this.mTouchExplorationEnabled = systemServices.isTouchExplorationEnabled();
        this.mScreenPinningEnabled = systemServices.getSystemSetting(getContext(), "lock_to_app_enabled") != 0;
    }

    public void dump(String str, PrintWriter printWriter) {
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        String str7 = str + "  ";
        String hexString = Integer.toHexString(System.identityHashCode(this));
        printWriter.print(str);
        printWriter.print("TaskStackView");
        printWriter.print(" hasDefRelayout=");
        String str8 = "Y";
        printWriter.print(this.mDeferredTaskViewLayoutAnimation != null ? str8 : "N");
        printWriter.print(" clipDirty=");
        if (this.mTaskViewsClipDirty) {
            str2 = str8;
        } else {
            str2 = "N";
        }
        printWriter.print(str2);
        printWriter.print(" awaitingFirstLayout=");
        if (this.mAwaitingFirstLayout) {
            str3 = str8;
        } else {
            str3 = "N";
        }
        printWriter.print(str3);
        printWriter.print(" initialState=");
        printWriter.print(this.mInitialState);
        printWriter.print(" inMeasureLayout=");
        if (this.mInMeasureLayout) {
            str4 = str8;
        } else {
            str4 = "N";
        }
        printWriter.print(str4);
        printWriter.print(" enterAnimCompleted=");
        if (this.mEnterAnimationComplete) {
            str5 = str8;
        } else {
            str5 = "N";
        }
        printWriter.print(str5);
        printWriter.print(" touchExplorationOn=");
        if (this.mTouchExplorationEnabled) {
            str6 = str8;
        } else {
            str6 = "N";
        }
        printWriter.print(str6);
        printWriter.print(" screenPinningOn=");
        if (!this.mScreenPinningEnabled) {
            str8 = "N";
        }
        printWriter.print(str8);
        printWriter.print(" numIgnoreTasks=");
        printWriter.print(this.mIgnoreTasks.size());
        printWriter.print(" numViewPool=");
        printWriter.print(this.mViewPool.getViews().size());
        printWriter.print(" stableStackBounds=");
        printWriter.print(Utilities.dumpRect(this.mStableStackBounds));
        printWriter.print(" stackBounds=");
        printWriter.print(Utilities.dumpRect(this.mStackBounds));
        printWriter.print(" stableWindow=");
        printWriter.print(Utilities.dumpRect(this.mStableWindowRect));
        printWriter.print(" window=");
        printWriter.print(Utilities.dumpRect(this.mWindowRect));
        printWriter.print(" display=");
        printWriter.print(Utilities.dumpRect(this.mDisplayRect));
        printWriter.print(" orientation=");
        printWriter.print(this.mDisplayOrientation);
        printWriter.print(" [0x");
        printWriter.print(hexString);
        printWriter.print("]");
        printWriter.println();
        if (this.mFocusedTask != null) {
            printWriter.print(str7);
            printWriter.print("Focused task: ");
            this.mFocusedTask.dump("", printWriter);
        }
        this.mLayoutAlgorithm.dump(str7, printWriter);
        this.mStackScroller.dump(str7, printWriter);
    }

    public void setIsShowingMenu(boolean z) {
        this.mIsShowingMenu = z;
    }

    public boolean isShowingMenu() {
        return this.mIsShowingMenu;
    }

    public FrameLayout getMask() {
        return this.mMaskWithMenu;
    }
}
