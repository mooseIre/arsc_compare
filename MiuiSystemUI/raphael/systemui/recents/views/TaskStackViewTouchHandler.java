package com.android.systemui.recents.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.SwipeHelperForRecents;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.ui.StackViewScrolledEvent;
import com.android.systemui.recents.events.ui.TaskViewDismissedEvent;
import com.android.systemui.recents.misc.FreePathInterpolator;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.recents.misc.SpringAnimationUtils;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.statusbar.FlingAnimationUtils;
import java.util.ArrayList;
import java.util.List;

class TaskStackViewTouchHandler implements SwipeHelperForRecents.Callback {
    private static final Interpolator OVERSCROLL_INTERP;
    int mActivePointerId = -1;
    TaskView mActiveTaskView = null;
    private boolean mAllowHideRecentsFromBackgroundTap = true;
    Context mContext;
    private ArrayList<TaskViewTransform> mCurrentTaskTransforms = new ArrayList<>();
    private ArrayList<Task> mCurrentTasks = new ArrayList<>();
    float mDownScrollP;
    int mDownX;
    int mDownY;
    private ArrayList<TaskViewTransform> mFinalTaskTransforms = new ArrayList<>();
    FlingAnimationUtils mFlingAnimUtils;
    boolean mInterceptedBySwipeHelper;
    private boolean mIsCancelAnimations = false;
    @ViewDebug.ExportedProperty(category = "recents")
    boolean mIsScrolling;
    float mLastScrollP;
    int mLastY;
    int mMaximumVelocity;
    int mMinimumVelocity;
    private float mOldStackScroll;
    int mOverscrollSize;
    int mRecentsTaskLockDistance;
    /* access modifiers changed from: private */
    public TaskView mSameTopPositionTaskView;
    ValueAnimator mScrollFlingAnimator;
    int mScrollTouchSlop;
    TaskStackViewScroller mScroller;
    private final StackViewScrolledEvent mStackViewScrolledEvent = new StackViewScrolledEvent();
    TaskStackView mSv;
    SwipeHelperForRecents mSwipeHelper;
    /* access modifiers changed from: private */
    public ArrayMap<View, Object> mSwipeHelperAnimations = new ArrayMap<>();
    /* access modifiers changed from: private */
    public float mTargetStackScroll;
    private TaskViewTransform mTmpTransform = new TaskViewTransform();
    VelocityTracker mVelocityTracker;
    final int mWindowTouchSlop;

    public float getFalsingThresholdFactor() {
        return 0.0f;
    }

    public boolean isAntiFalsingNeeded() {
        return false;
    }

    static {
        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        path.cubicTo(0.2f, 0.175f, 0.25f, 0.3f, 1.0f, 0.3f);
        OVERSCROLL_INTERP = new FreePathInterpolator(path);
    }

    public TaskStackViewTouchHandler(Context context, TaskStackView taskStackView, TaskStackViewScroller taskStackViewScroller) {
        Resources resources = context.getResources();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mContext = context;
        this.mSv = taskStackView;
        this.mScroller = taskStackViewScroller;
        this.mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mScrollTouchSlop = resources.getDimensionPixelSize(R.dimen.recents_view_configuration_touch_slop);
        this.mWindowTouchSlop = viewConfiguration.getScaledWindowTouchSlop();
        this.mFlingAnimUtils = new FlingAnimationUtils(context, 0.2f);
        this.mOverscrollSize = resources.getDimensionPixelSize(R.dimen.recents_fling_overscroll_distance);
        this.mRecentsTaskLockDistance = resources.getDimensionPixelSize(R.dimen.recents_task_lock_distance);
        resources.getDimensionPixelSize(R.dimen.recents_lock_view_swipe_top_margin);
        resources.getDimensionPixelSize(R.dimen.recents_lock_view_swipe_height);
        this.mSwipeHelper = new SwipeHelperForRecents(0, this, context) {
            /* access modifiers changed from: protected */
            public float getUnscaledEscapeVelocity() {
                return 800.0f;
            }

            /* access modifiers changed from: protected */
            public float getSize(View view) {
                return TaskStackViewTouchHandler.this.getScaledDismissSize();
            }

            /* access modifiers changed from: protected */
            public void prepareDismissAnimation(View view, Object obj) {
                TaskStackViewTouchHandler.this.mSwipeHelperAnimations.put(view, obj);
            }

            /* access modifiers changed from: protected */
            public void prepareSnapBackAnimation(View view, Object obj) {
                TaskStackViewTouchHandler.this.mSwipeHelperAnimations.put(view, obj);
            }

            /* access modifiers changed from: protected */
            public void onMoveUpdate(View view, float f, float f2) {
                if (TaskStackViewTouchHandler.this.mSameTopPositionTaskView != null) {
                    int width = TaskStackViewTouchHandler.this.mSameTopPositionTaskView.getWidth();
                    boolean z = TaskStackViewTouchHandler.this.mSameTopPositionTaskView.getLeft() < TaskStackViewTouchHandler.this.mSv.getWidth() - TaskStackViewTouchHandler.this.mSameTopPositionTaskView.getRight();
                    if ((z && f < 0.0f) || (!z && f > 0.0f)) {
                        TaskStackViewTouchHandler.this.mSameTopPositionTaskView.getHeaderView().setAlpha(((-1.0f / (((float) width) * 0.5f)) * Math.abs(f)) + 1.0f);
                    }
                }
            }
        };
        this.mSwipeHelper.setDisableHardwareLayers(true);
    }

    /* access modifiers changed from: package-private */
    public void initOrResetVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    /* access modifiers changed from: package-private */
    public void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.mSv.isShowingMenu()) {
            return true;
        }
        this.mInterceptedBySwipeHelper = this.mSwipeHelper.onInterceptTouchEvent(motionEvent);
        if (this.mInterceptedBySwipeHelper) {
            return true;
        }
        return handleTouchEvent(motionEvent);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mInterceptedBySwipeHelper && this.mSwipeHelper.onTouchEvent(motionEvent)) {
            return true;
        }
        handleTouchEvent(motionEvent);
        return true;
    }

    public boolean cancelNonDismissTaskAnimations() {
        Utilities.cancelAnimationWithoutCallbacks(this.mScrollFlingAnimator);
        boolean z = true;
        if (!this.mSwipeHelperAnimations.isEmpty()) {
            List<TaskView> taskViews = this.mSv.getTaskViews();
            for (int size = taskViews.size() - 1; size >= 0; size--) {
                TaskView taskView = taskViews.get(size);
                if (!this.mSv.isIgnoredTask(taskView.getTask())) {
                    taskView.cancelTransformAnimation();
                    this.mSv.getStackAlgorithm().addUnfocusedTaskOverride(taskView, this.mTargetStackScroll);
                }
            }
            this.mSv.getStackAlgorithm().setFocusState(0);
            this.mSv.getScroller().setStackScroll(this.mTargetStackScroll, (AnimationProps) null);
            this.mSwipeHelperAnimations.clear();
        } else {
            z = false;
        }
        this.mActiveTaskView = null;
        return z;
    }

    private boolean handleTouchEvent(MotionEvent motionEvent) {
        int i = 1;
        if (this.mSv.isShowingMenu()) {
            return true;
        }
        initVelocityTrackerIfNotExists();
        TaskStackLayoutAlgorithm taskStackLayoutAlgorithm = this.mSv.mLayoutAlgorithm;
        int action = motionEvent.getAction() & 255;
        if (action == 0) {
            this.mScroller.stopScroller();
            this.mScroller.stopBoundScrollAnimation();
            this.mScroller.resetDeltaScroll();
            if (cancelNonDismissTaskAnimations()) {
                this.mIsCancelAnimations = true;
            }
            this.mSv.cancelDeferredTaskViewLayoutAnimation();
            this.mDownX = (int) motionEvent.getX();
            this.mDownY = (int) motionEvent.getY();
            this.mLastY = this.mDownY;
            this.mDownScrollP = this.mScroller.getStackScroll();
            this.mActivePointerId = motionEvent.getPointerId(0);
            this.mActiveTaskView = findViewAtPoint(this.mDownX, this.mDownY);
            SpringAnimationUtils.getInstance().startTaskViewTouchDownAnim(this.mActiveTaskView);
            initOrResetVelocityTracker();
            this.mVelocityTracker.addMovement(motionEvent);
        } else if (action == 1) {
            this.mVelocityTracker.addMovement(motionEvent);
            this.mVelocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
            int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
            if (findPointerIndex < 0 || findPointerIndex >= motionEvent.getPointerCount()) {
                this.mActivePointerId = motionEvent.getPointerId(0);
                findPointerIndex = 0;
            }
            int y = (int) motionEvent.getY(findPointerIndex);
            int yVelocity = (int) this.mVelocityTracker.getYVelocity(this.mActivePointerId);
            if (this.mIsScrolling) {
                if (this.mScroller.isScrollOutOfBounds()) {
                    this.mScroller.animateBoundScroll(yVelocity);
                } else {
                    this.mScroller.fling(this.mDownScrollP, this.mDownY, y, yVelocity, (int) ((float) (this.mDownY + taskStackLayoutAlgorithm.getXForDeltaP(this.mDownScrollP, taskStackLayoutAlgorithm.mMaxScrollP))), (int) ((float) (this.mDownY + taskStackLayoutAlgorithm.getXForDeltaP(this.mDownScrollP, taskStackLayoutAlgorithm.mMinScrollP))), this.mOverscrollSize);
                    this.mSv.invalidate();
                }
                TaskStackView taskStackView = this.mSv;
                if (!taskStackView.mTouchExplorationEnabled) {
                    taskStackView.resetFocusedTask(taskStackView.getFocusedTask());
                }
                TaskView taskView = this.mActiveTaskView;
                if (taskView != null) {
                    taskView.setIsScollAnimating(false);
                }
            } else if (this.mActiveTaskView == null) {
                maybeHideRecentsFromBackgroundTap((int) motionEvent.getX(), (int) motionEvent.getY());
            } else {
                SpringAnimationUtils.getInstance().startTaskViewTouchMoveOrUpAnim(this.mActiveTaskView);
            }
            this.mIsCancelAnimations = false;
            this.mActivePointerId = -1;
            this.mIsScrolling = false;
            recycleVelocityTracker();
        } else if (action == 2) {
            int findPointerIndex2 = motionEvent.findPointerIndex(this.mActivePointerId);
            if (findPointerIndex2 < 0 || findPointerIndex2 >= motionEvent.getPointerCount()) {
                this.mActivePointerId = motionEvent.getPointerId(0);
                findPointerIndex2 = 0;
            }
            int y2 = (int) motionEvent.getY(findPointerIndex2);
            int x = (int) motionEvent.getX(findPointerIndex2);
            if (!this.mIsScrolling) {
                int abs = Math.abs(y2 - this.mDownY);
                int abs2 = Math.abs(x - this.mDownX);
                if (Math.abs(y2 - this.mDownY) > this.mScrollTouchSlop && abs > abs2) {
                    this.mIsScrolling = true;
                    float stackScroll = this.mScroller.getStackScroll();
                    List<TaskView> taskViews = this.mSv.getTaskViews();
                    for (int size = taskViews.size() - 1; size >= 0; size--) {
                        taskStackLayoutAlgorithm.addUnfocusedTaskOverride(taskViews.get(size).getTask(), stackScroll);
                    }
                    taskStackLayoutAlgorithm.setFocusState(0);
                    ViewParent parent = this.mSv.getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    MetricsLogger.action(this.mSv.getContext(), 287);
                    SpringAnimationUtils.getInstance().startTaskViewTouchMoveOrUpAnim(this.mActiveTaskView);
                    TaskView taskView2 = this.mActiveTaskView;
                    if (taskView2 != null) {
                        taskView2.setIsScollAnimating(true);
                    }
                }
            }
            if (this.mIsScrolling) {
                float deltaPForX = taskStackLayoutAlgorithm.getDeltaPForX(this.mDownY, y2);
                float f = taskStackLayoutAlgorithm.mMinScrollP;
                float f2 = taskStackLayoutAlgorithm.mMaxScrollP;
                float f3 = this.mDownScrollP + deltaPForX;
                if (f3 < f || f3 > f2) {
                    float clamp = Utilities.clamp(f3, f, f2);
                    float f4 = f3 - clamp;
                    f3 = (Math.signum(f4) * OVERSCROLL_INTERP.getInterpolation(Math.abs(f4) / 2.3333333f) * 2.3333333f) + clamp;
                }
                float f5 = -this.mScroller.mExitRecentOverscrollThreshold;
                if (this.mLastScrollP > f5 && f3 < f5 && y2 - this.mLastY > 0) {
                    this.mSv.performHapticFeedback(1);
                    SpringAnimationUtils.getInstance().startDragExitRecentsAnim(this.mSv);
                }
                if (this.mLastScrollP < f5 && f3 > f5 && y2 - this.mLastY < 0) {
                    SpringAnimationUtils.getInstance().startCancelDragExitRecentsAnim(this.mSv);
                }
                float f6 = this.mDownScrollP;
                this.mDownScrollP = f6 + this.mScroller.setDeltaStackScroll(f6, f3 - f6);
                this.mStackViewScrolledEvent.updateY(y2 - this.mLastY);
                RecentsEventBus.getDefault().send(this.mStackViewScrolledEvent);
                this.mLastScrollP = f3;
            }
            this.mLastY = y2;
            this.mVelocityTracker.addMovement(motionEvent);
        } else if (action == 3) {
            this.mIsCancelAnimations = false;
            this.mActivePointerId = -1;
            this.mIsScrolling = false;
            SpringAnimationUtils.getInstance().startTaskViewTouchMoveOrUpAnim(this.mActiveTaskView);
            recycleVelocityTracker();
        } else if (action == 5) {
            int actionIndex = motionEvent.getActionIndex();
            this.mActivePointerId = motionEvent.getPointerId(actionIndex);
            this.mDownX = (int) motionEvent.getX(actionIndex);
            this.mDownY = (int) motionEvent.getY(actionIndex);
            this.mLastY = this.mDownY;
            this.mDownScrollP = this.mScroller.getStackScroll();
            this.mScroller.resetDeltaScroll();
            this.mVelocityTracker.addMovement(motionEvent);
        } else if (action == 6) {
            int actionIndex2 = motionEvent.getActionIndex();
            if (motionEvent.getPointerId(actionIndex2) == this.mActivePointerId) {
                if (actionIndex2 != 0) {
                    i = 0;
                }
                this.mActivePointerId = motionEvent.getPointerId(i);
                this.mDownX = (int) motionEvent.getX(actionIndex2);
                this.mDownY = (int) motionEvent.getY(actionIndex2);
                this.mLastY = this.mDownY;
                this.mDownScrollP = this.mScroller.getStackScroll();
            }
            this.mVelocityTracker.addMovement(motionEvent);
        }
        return this.mIsScrolling;
    }

    public void setAllowHideRecentsFromBackgroundTap(boolean z) {
        this.mAllowHideRecentsFromBackgroundTap = z;
    }

    /* access modifiers changed from: package-private */
    public void maybeHideRecentsFromBackgroundTap(int i, int i2) {
        int abs = Math.abs(this.mDownX - i);
        int abs2 = Math.abs(this.mDownY - i2);
        int i3 = this.mScrollTouchSlop;
        if (abs > i3 || abs2 > i3) {
            if (this.mIsCancelAnimations) {
                this.mSv.requestLayout();
            }
        } else if (!this.mAllowHideRecentsFromBackgroundTap) {
            Log.w("TaskStackViewTouchHandler", "mAllowHideRecentsFromBackgroundTap == false");
        } else {
            if (Recents.getSystemServices().hasFreeformWorkspaceSupport()) {
                TaskStackView taskStackView = this.mSv;
                Rect rect = taskStackView.mLayoutAlgorithm.mFreeformRect;
                if (rect.top <= i2 && i2 <= rect.bottom && taskStackView.launchFreeformTasks()) {
                    return;
                }
            }
            RecentsEventBus.getDefault().send(new HideRecentsEvent(false, true, false));
            RecentsPushEventHelper.sendHideRecentsEvent("clickEmptySpace");
        }
    }

    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        if ((motionEvent.getSource() & 2) != 2 || (motionEvent.getAction() & 255) != 8) {
            return false;
        }
        if (motionEvent.getAxisValue(9) > 0.0f) {
            this.mSv.setRelativeFocusedTask(true, true, false);
        } else {
            this.mSv.setRelativeFocusedTask(false, true, false);
        }
        return true;
    }

    public View getChildAtPosition(MotionEvent motionEvent) {
        TaskView findViewAtPoint = findViewAtPoint((int) motionEvent.getX(), (int) motionEvent.getY());
        if (findViewAtPoint != null) {
            return findViewAtPoint;
        }
        return null;
    }

    public boolean canChildBeDismissed(View view) {
        return !this.mSwipeHelperAnimations.containsKey(view) && this.mSv.getStack().indexOfStackTask(((TaskView) view).getTask()) != -1 && view.getTranslationY() <= 0.0f;
    }

    public void onBeginManualDrag(TaskView taskView) {
        this.mActiveTaskView = taskView;
        this.mSwipeHelperAnimations.put(taskView, (Object) null);
        onBeginDrag(taskView);
    }

    public void onBeginDrag(View view) {
        TaskView taskView = (TaskView) view;
        taskView.getViewBounds().reset();
        taskView.setTranslationZ(10.0f);
        SpringAnimationUtils.getInstance().startTaskViewSwipeAnim(taskView);
        taskView.getHeaderView().startDismissTaskAnim();
        this.mSameTopPositionTaskView = findSameTopPositionTaskView(taskView);
        taskView.setClipViewInStack(false);
        taskView.setTouchEnabled(false);
        ViewParent parent = this.mSv.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
    }

    public void onDragEnd(View view) {
        if (view instanceof TaskView) {
            TaskView taskView = this.mSameTopPositionTaskView;
            if (!(taskView == null || taskView.getHeaderView().getAlpha() == 1.0f)) {
                this.mSameTopPositionTaskView.getHeaderView().animate().alpha(1.0f).setDuration(150).start();
            }
            this.mSv.addIgnoreTask(((TaskView) view).getTask());
            this.mCurrentTasks = new ArrayList<>(this.mSv.getStack().getStackTasks());
            TaskStackViewScroller scroller = this.mSv.getScroller();
            this.mSv.getCurrentTaskTransforms(this.mCurrentTasks, this.mCurrentTaskTransforms);
            this.mSv.updateLayoutAlgorithm(false);
            this.mOldStackScroll = scroller.getStackScroll();
            this.mSv.bindVisibleTaskViews(this.mOldStackScroll, true);
            this.mSv.getLayoutTaskTransforms(this.mOldStackScroll, 0, this.mCurrentTasks, true, this.mFinalTaskTransforms);
            this.mTargetStackScroll = Math.min(this.mOldStackScroll, this.mSv.getStackAlgorithm().mMaxScrollP);
        }
    }

    public float getOldStackScroll() {
        return this.mOldStackScroll;
    }

    public boolean updateSwipeProgress(View view, boolean z, float f) {
        if (this.mActiveTaskView != view && !this.mSwipeHelperAnimations.containsKey(view)) {
            return true;
        }
        updateTaskViewTransforms(Interpolators.FAST_OUT_SLOW_IN.getInterpolation(Math.abs(f)));
        return true;
    }

    public void onChildDismissed(View view) {
        TaskView taskView = (TaskView) view;
        taskView.getHeaderView().resetViewState();
        taskView.setClipViewInStack(true);
        taskView.setTouchEnabled(true);
        RecentsEventBus.getDefault().send(new TaskViewDismissedEvent(taskView.getTask(), taskView, this.mSwipeHelperAnimations.containsKey(view) ? new AnimationProps(200, Interpolators.FAST_OUT_SLOW_IN) : null));
        if (this.mSwipeHelperAnimations.containsKey(view)) {
            this.mSv.postDelayed(new Runnable() {
                public void run() {
                    TaskStackViewTouchHandler.this.mSv.getScroller().animateScroll(TaskStackViewTouchHandler.this.mTargetStackScroll, (Runnable) null);
                }
            }, 200);
            this.mSv.getStackAlgorithm().setFocusState(0);
            this.mSv.getStackAlgorithm().clearUnfocusedTaskOverrides();
            this.mSwipeHelperAnimations.remove(view);
        }
        MetricsLogger.histogram(taskView.getContext(), "overview_task_dismissed_source", 1);
        String packageName = taskView.getTask().key.getComponent().getPackageName();
        ArrayList<Task> arrayList = this.mCurrentTasks;
        RecentsPushEventHelper.sendRemoveTaskEvent(packageName, arrayList != null ? arrayList.indexOf(taskView.getTask()) : -1);
    }

    public void onChildSnappedBack(View view, float f) {
        TaskView taskView = (TaskView) view;
        taskView.setClipViewInStack(true);
        taskView.setTouchEnabled(true);
        taskView.setTranslationZ(0.0f);
        this.mSv.removeIgnoreTask(taskView.getTask());
        this.mSv.updateLayoutAlgorithm(false);
        this.mSv.relayoutTaskViews(AnimationProps.IMMEDIATE);
        this.mSwipeHelperAnimations.remove(view);
    }

    public void onDragCancelled(View view) {
        TaskView taskView = (TaskView) view;
        SpringAnimationUtils.getInstance().startTaskViewSwipeCancelAnim(taskView);
        taskView.getHeaderView().startResetTaskAnim();
    }

    public boolean checkToBeginDrag(View view) {
        return !((TaskView) view).startDrag();
    }

    private void updateTaskViewTransforms(float f) {
        int indexOf;
        List<TaskView> taskViews = this.mSv.getTaskViews();
        int size = taskViews.size();
        for (int i = 0; i < size; i++) {
            TaskView taskView = taskViews.get(i);
            Task task = taskView.getTask();
            if (!this.mSv.isIgnoredTask(task) && (indexOf = this.mCurrentTasks.indexOf(task)) != -1) {
                if (indexOf < 0 || indexOf >= this.mCurrentTaskTransforms.size() || indexOf >= this.mFinalTaskTransforms.size()) {
                    Log.w("TaskStackViewTouchHandler", "updateTaskViewTransforms error, taskIndex = " + indexOf + ",  mCurrentTaskTransforms.size() = " + this.mCurrentTaskTransforms.size() + ",  mCurrentTaskTransforms.size() = " + this.mFinalTaskTransforms.size());
                } else {
                    TaskViewTransform taskViewTransform = this.mCurrentTaskTransforms.get(indexOf);
                    TaskViewTransform taskViewTransform2 = this.mFinalTaskTransforms.get(indexOf);
                    this.mTmpTransform.copyFrom(taskViewTransform);
                    this.mTmpTransform.rect.set(Utilities.RECTF_EVALUATOR.evaluate(f, taskViewTransform.rect, taskViewTransform2.rect));
                    TaskViewTransform taskViewTransform3 = this.mTmpTransform;
                    float f2 = taskViewTransform.dimAlpha;
                    taskViewTransform3.dimAlpha = f2 + ((taskViewTransform2.dimAlpha - f2) * f);
                    float f3 = taskViewTransform.viewOutlineAlpha;
                    taskViewTransform3.viewOutlineAlpha = f3 + ((taskViewTransform2.viewOutlineAlpha - f3) * f);
                    float f4 = taskViewTransform.translationZ;
                    taskViewTransform3.translationZ = f4 + ((taskViewTransform2.translationZ - f4) * f);
                    this.mSv.updateTaskViewToTransform(taskView, taskViewTransform3, AnimationProps.IMMEDIATE);
                }
            }
        }
    }

    private TaskView findViewAtPoint(int i, int i2) {
        ArrayList<Task> stackTasks = this.mSv.getStack().getStackTasks();
        for (int size = stackTasks.size() - 1; size >= 0; size--) {
            TaskView childViewForTask = this.mSv.getChildViewForTask(stackTasks.get(size));
            if (childViewForTask != null && childViewForTask.getVisibility() == 0 && this.mSv.isTouchPointInView((float) i, (float) i2, childViewForTask)) {
                return childViewForTask;
            }
        }
        return null;
    }

    private TaskView findSameTopPositionTaskView(TaskView taskView) {
        ArrayList<Task> stackTasks = this.mSv.getStack().getStackTasks();
        for (int size = stackTasks.size() - 1; size >= 0; size--) {
            TaskView childViewForTask = this.mSv.getChildViewForTask(stackTasks.get(size));
            if (childViewForTask != null && childViewForTask.getVisibility() == 0 && childViewForTask != taskView && childViewForTask.getTop() == taskView.getTop()) {
                return childViewForTask;
            }
        }
        return null;
    }

    public float getScaledDismissSize() {
        return ((float) Math.max(this.mSv.getWidth(), this.mSv.getHeight())) * 1.0f;
    }
}
