package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewOutlineProvider;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.proxy.ActivityManager$TaskThumbnailInfo;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.ClickTaskViewToLaunchTaskEvent;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.ShowTaskMenuEvent;
import com.android.systemui.recents.events.component.UpdateLockStateEvent;
import com.android.systemui.recents.events.ui.DismissTaskViewEvent;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import com.android.systemui.recents.misc.SpringAnimationImpl;
import com.android.systemui.recents.misc.SpringAnimationUtils;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.TaskStackAnimationHelper;
import java.util.ArrayList;

public class TaskView extends FixedSizeFrameLayout implements Task.TaskCallbacks, TaskStackAnimationHelper.Callbacks, View.OnClickListener, View.OnLongClickListener {
    public static final Property<TaskView, Float> DIM_ALPHA = new FloatProperty<TaskView>("dimAlpha") {
        public void setValue(TaskView taskView, float f) {
            taskView.setDimAlpha(f);
        }

        public Float get(TaskView taskView) {
            return Float.valueOf(taskView.getDimAlpha());
        }
    };
    public static final Property<TaskView, Float> DIM_ALPHA_WITHOUT_HEADER = new FloatProperty<TaskView>("dimAlphaWithoutHeader") {
        public void setValue(TaskView taskView, float f) {
            taskView.setDimAlphaWithoutHeader(f);
        }

        public Float get(TaskView taskView) {
            return Float.valueOf(taskView.getDimAlpha());
        }
    };
    public static final Property<TaskView, Float> VIEW_OUTLINE_ALPHA = new FloatProperty<TaskView>("viewOutlineAlpha") {
        public void setValue(TaskView taskView, float f) {
            taskView.getViewBounds().setAlpha(f);
        }

        public Float get(TaskView taskView) {
            return Float.valueOf(taskView.getViewBounds().getAlpha());
        }
    };
    private View mAccessLockView;
    private float mActionButtonTranslationZ;
    /* access modifiers changed from: private */
    public View mActionButtonView;
    private TaskViewCallbacks mCb;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mClipViewInStack;
    @ViewDebug.ExportedProperty(category = "recents")
    private float mDimAlpha;
    private ObjectAnimator mDimAnimator;
    private Toast mDisabledAppToast;
    @ViewDebug.ExportedProperty(category = "recents")
    private Point mDownTouchPos;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "header_")
    TaskViewHeader mHeaderView;
    private View mIncompatibleAppToastView;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mIsDisabledInSafeMode;
    private boolean mIsDragging;
    public boolean mIsScollAnimating;
    private ObjectAnimator mOutlineAnimator;
    private View mScreeningView;
    public SpringAnimationImpl mSpringAnimationImpl;
    private final TaskViewTransform mTargetAnimationTransform;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "task_")
    private Task mTask;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "thumbnail_")
    TaskViewThumbnail mThumbnailView;
    private ArrayList<Animator> mTmpAnimators;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mTouchExplorationEnabled;
    private AnimatorSet mTransformAnimation;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "view_bounds_")
    private AnimateableViewBounds mViewBounds;

    interface TaskViewCallbacks {
        void onTaskViewClipStateChanged(TaskView taskView);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public TaskView(Context context) {
        this(context, (AttributeSet) null);
    }

    public TaskView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TaskView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public TaskView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mClipViewInStack = true;
        this.mTargetAnimationTransform = new TaskViewTransform();
        this.mTmpAnimators = new ArrayList<>();
        this.mDownTouchPos = new Point();
        this.mIsScollAnimating = false;
        Recents.getConfiguration();
        this.mViewBounds = new AnimateableViewBounds(this, context.getResources().getDimensionPixelSize(R.dimen.recents_task_view_shadow_rounded_corners_radius));
        setOutlineProvider(this.mViewBounds);
        setOnLongClickListener(this);
        this.mSpringAnimationImpl = new SpringAnimationImpl(this);
    }

    /* access modifiers changed from: package-private */
    public void setCallbacks(TaskViewCallbacks taskViewCallbacks) {
        this.mCb = taskViewCallbacks;
    }

    /* access modifiers changed from: package-private */
    public void onReload(boolean z) {
        resetNoUserInteractionState();
        if (!z) {
            resetViewProperties();
        }
    }

    public Task getTask() {
        return this.mTask;
    }

    /* access modifiers changed from: package-private */
    public AnimateableViewBounds getViewBounds() {
        return this.mViewBounds;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        this.mHeaderView = (TaskViewHeader) findViewById(R.id.task_view_bar);
        this.mThumbnailView = (TaskViewThumbnail) findViewById(R.id.task_view_thumbnail);
        this.mAccessLockView = findViewById(R.id.task_view_access_lock);
        this.mScreeningView = findViewById(R.id.task_view_screening);
        this.mThumbnailView.updateClipToTaskBar(this.mHeaderView);
        this.mActionButtonView = findViewById(R.id.lock_to_app_fab);
        this.mActionButtonView.setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, TaskView.this.mActionButtonView.getWidth(), TaskView.this.mActionButtonView.getHeight());
                outline.setAlpha(0.35f);
            }
        });
        this.mActionButtonView.setOnClickListener(this);
        this.mActionButtonTranslationZ = this.mActionButtonView.getTranslationZ();
    }

    /* access modifiers changed from: package-private */
    public void onConfigurationChanged() {
        this.mHeaderView.onConfigurationChanged();
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i > 0 && i2 > 0) {
            this.mHeaderView.onTaskViewSizeChanged(i, i2);
            this.mThumbnailView.onTaskViewSizeChanged(i, i2);
            this.mActionButtonView.setTranslationX((float) (i - getMeasuredWidth()));
            this.mActionButtonView.setTranslationY((float) (i2 - getMeasuredHeight()));
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            this.mDownTouchPos.set((int) (motionEvent.getX() * getScaleX()), (int) (motionEvent.getY() * getScaleY()));
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    /* access modifiers changed from: protected */
    public void measureContents(int i, int i2) {
        measureChildren(View.MeasureSpec.makeMeasureSpec((i - this.mPaddingLeft) - this.mPaddingRight, 1073741824), View.MeasureSpec.makeMeasureSpec((i2 - this.mPaddingTop) - this.mPaddingBottom, 1073741824));
        setMeasuredDimension(i, i2);
    }

    /* access modifiers changed from: package-private */
    public void updateViewPropertiesToTaskTransform(TaskViewTransform taskViewTransform, AnimationProps animationProps, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        Recents.getConfiguration();
        cancelTransformAnimation();
        this.mTmpAnimators.clear();
        taskViewTransform.applyToTaskView(this, this.mTmpAnimators, animationProps, false);
        if (animationProps.isImmediate()) {
            if (Float.compare(getDimAlpha(), taskViewTransform.dimAlpha) != 0) {
                setDimAlpha(taskViewTransform.dimAlpha);
            }
            if (Float.compare(this.mViewBounds.getAlpha(), taskViewTransform.viewOutlineAlpha) != 0) {
                this.mViewBounds.setAlpha(taskViewTransform.viewOutlineAlpha);
            }
            if (animationProps.getListener() != null) {
                animationProps.getListener().onAnimationEnd((Animator) null);
            }
            if (animatorUpdateListener != null) {
                animatorUpdateListener.onAnimationUpdate((ValueAnimator) null);
                return;
            }
            return;
        }
        if (Float.compare(getDimAlpha(), taskViewTransform.dimAlpha) != 0) {
            this.mDimAnimator = ObjectAnimator.ofFloat(this, DIM_ALPHA, new float[]{getDimAlpha(), taskViewTransform.dimAlpha});
            ArrayList<Animator> arrayList = this.mTmpAnimators;
            ObjectAnimator objectAnimator = this.mDimAnimator;
            animationProps.apply(6, objectAnimator);
            arrayList.add(objectAnimator);
        }
        if (Float.compare(this.mViewBounds.getAlpha(), taskViewTransform.viewOutlineAlpha) != 0) {
            this.mOutlineAnimator = ObjectAnimator.ofFloat(this, VIEW_OUTLINE_ALPHA, new float[]{this.mViewBounds.getAlpha(), taskViewTransform.viewOutlineAlpha});
            ArrayList<Animator> arrayList2 = this.mTmpAnimators;
            ObjectAnimator objectAnimator2 = this.mOutlineAnimator;
            animationProps.apply(6, objectAnimator2);
            arrayList2.add(objectAnimator2);
        }
        if (animatorUpdateListener != null) {
            ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{0, 1});
            ofInt.addUpdateListener(animatorUpdateListener);
            ArrayList<Animator> arrayList3 = this.mTmpAnimators;
            animationProps.apply(6, ofInt);
            arrayList3.add(ofInt);
        }
        this.mTransformAnimation = animationProps.createAnimator(this.mTmpAnimators);
        this.mTransformAnimation.start();
        this.mTargetAnimationTransform.copyFrom(taskViewTransform);
    }

    /* access modifiers changed from: package-private */
    public void resetViewProperties() {
        SpringAnimationUtils.getInstance().cancelAllSpringAnimation(this.mSpringAnimationImpl);
        cancelTransformAnimation();
        setDimAlpha(0.0f);
        setVisibility(0);
        getViewBounds().reset();
        getHeaderView().reset();
        getThumbnailView().reset();
        TaskViewTransform.reset(this);
        this.mActionButtonView.setScaleX(1.0f);
        this.mActionButtonView.setScaleY(1.0f);
        this.mActionButtonView.setAlpha(0.0f);
        this.mActionButtonView.setTranslationX(0.0f);
        this.mActionButtonView.setTranslationY(0.0f);
        this.mActionButtonView.setTranslationZ(this.mActionButtonTranslationZ);
        View view = this.mIncompatibleAppToastView;
        if (view != null) {
            view.setVisibility(4);
        }
        this.mAccessLockView.setVisibility(8);
        this.mScreeningView.setVisibility(8);
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimatingTo(TaskViewTransform taskViewTransform) {
        AnimatorSet animatorSet = this.mTransformAnimation;
        return animatorSet != null && animatorSet.isStarted() && this.mTargetAnimationTransform.isSame(taskViewTransform);
    }

    public void cancelTransformAnimation() {
        Utilities.cancelAnimationWithoutCallbacks(this.mTransformAnimation);
        Utilities.cancelAnimationWithoutCallbacks(this.mDimAnimator);
        Utilities.cancelAnimationWithoutCallbacks(this.mOutlineAnimator);
    }

    /* access modifiers changed from: package-private */
    public void setTouchEnabled(boolean z) {
        setOnClickListener(z ? this : null);
    }

    /* access modifiers changed from: package-private */
    public void startNoUserInteractionAnimation() {
        this.mHeaderView.startNoUserInteractionAnimation();
    }

    /* access modifiers changed from: package-private */
    public void setNoUserInteractionState() {
        this.mHeaderView.setNoUserInteractionState();
    }

    /* access modifiers changed from: package-private */
    public void resetNoUserInteractionState() {
        this.mHeaderView.resetNoUserInteractionState();
    }

    /* access modifiers changed from: package-private */
    public void dismissTask() {
        RecentsEventBus.getDefault().send(new DismissTaskViewEvent(this));
    }

    /* access modifiers changed from: package-private */
    public void setClipViewInStack(boolean z) {
        if (z != this.mClipViewInStack) {
            this.mClipViewInStack = z;
            TaskViewCallbacks taskViewCallbacks = this.mCb;
            if (taskViewCallbacks != null) {
                taskViewCallbacks.onTaskViewClipStateChanged(this);
            }
        }
    }

    public TaskViewHeader getHeaderView() {
        return this.mHeaderView;
    }

    public TaskViewThumbnail getThumbnailView() {
        return this.mThumbnailView;
    }

    public void setDimAlpha(float f) {
        this.mDimAlpha = f;
        this.mThumbnailView.setDimAlpha(f);
        this.mHeaderView.setDimAlpha(f);
    }

    public void setDimAlphaWithoutHeader(float f) {
        this.mDimAlpha = f;
        this.mThumbnailView.setDimAlpha(f);
    }

    public float getDimAlpha() {
        return this.mDimAlpha;
    }

    public void setFocusedState(boolean z, boolean z2) {
        if (z) {
            if (z2 && !isFocused()) {
                requestFocus();
            }
        } else if (isAccessibilityFocused() && this.mTouchExplorationEnabled) {
            clearAccessibilityFocus();
        }
    }

    public void showActionButton(boolean z, int i) {
        this.mActionButtonView.setVisibility(0);
        if (!z || this.mActionButtonView.getAlpha() >= 1.0f) {
            this.mActionButtonView.setScaleX(1.0f);
            this.mActionButtonView.setScaleY(1.0f);
            this.mActionButtonView.setAlpha(1.0f);
            this.mActionButtonView.setTranslationZ(this.mActionButtonTranslationZ);
            return;
        }
        this.mActionButtonView.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration((long) i).setInterpolator(Interpolators.ALPHA_IN).start();
    }

    public void hideActionButton(boolean z, int i, boolean z2, final Animator.AnimatorListener animatorListener) {
        if (!z || this.mActionButtonView.getAlpha() <= 0.0f) {
            this.mActionButtonView.setAlpha(0.0f);
            this.mActionButtonView.setVisibility(4);
            if (animatorListener != null) {
                animatorListener.onAnimationEnd((Animator) null);
                return;
            }
            return;
        }
        if (z2) {
            this.mActionButtonView.animate().scaleX(0.9f).scaleY(0.9f);
        }
        this.mActionButtonView.animate().alpha(0.0f).setDuration((long) i).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable() {
            public void run() {
                Animator.AnimatorListener animatorListener = animatorListener;
                if (animatorListener != null) {
                    animatorListener.onAnimationEnd((Animator) null);
                }
                TaskView.this.mActionButtonView.setVisibility(4);
            }
        }).start();
    }

    public void updateLockedFlagVisible(boolean z) {
        updateLockedFlagVisible(z, false, 0);
    }

    public void updateLockedFlagVisible(boolean z, boolean z2, long j) {
        String str;
        this.mHeaderView.updateLockedFlagVisible(z, z2, j);
        if (z) {
            str = getContext().getString(R.string.accessibility_recent_task_locked_state);
        } else {
            str = getContext().getString(R.string.accessibility_recent_task_unlocked_state);
        }
        setContentDescription(this.mTask.titleDescription + "," + str);
    }

    public void onPrepareLaunchTargetForEnterAnimation() {
        setDimAlphaWithoutHeader(0.0f);
        this.mActionButtonView.setAlpha(0.0f);
        View view = this.mIncompatibleAppToastView;
        if (view != null && view.getVisibility() == 0) {
            this.mIncompatibleAppToastView.setAlpha(0.0f);
        }
    }

    public void onStartLaunchTargetEnterAnimation(TaskViewTransform taskViewTransform, int i, boolean z, ReferenceCountedTrigger referenceCountedTrigger) {
        Utilities.cancelAnimationWithoutCallbacks(this.mDimAnimator);
        referenceCountedTrigger.increment();
        AnimationProps animationProps = new AnimationProps(i, Interpolators.ALPHA_OUT);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, DIM_ALPHA_WITHOUT_HEADER, new float[]{getDimAlpha(), taskViewTransform.dimAlpha});
        animationProps.apply(7, ofFloat);
        this.mDimAnimator = ofFloat;
        this.mDimAnimator.addListener(referenceCountedTrigger.decrementOnAnimationEnd());
        this.mDimAnimator.start();
        if (z) {
            showActionButton(true, i);
        }
        View view = this.mIncompatibleAppToastView;
        if (view != null && view.getVisibility() == 0) {
            this.mIncompatibleAppToastView.animate().alpha(1.0f).setDuration((long) i).setInterpolator(Interpolators.ALPHA_IN).start();
        }
    }

    public void onLaunchNextTask() {
        View view = this.mIncompatibleAppToastView;
        if (view != null && view.getVisibility() == 0) {
            this.mIncompatibleAppToastView.setAlpha(1.0f);
        }
    }

    public void onStartLaunchTargetLaunchAnimation(int i, boolean z, ReferenceCountedTrigger referenceCountedTrigger) {
        Utilities.cancelAnimationWithoutCallbacks(this.mDimAnimator);
        AnimationProps animationProps = new AnimationProps(i, Interpolators.ALPHA_OUT);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, DIM_ALPHA, new float[]{getDimAlpha(), 0.0f});
        animationProps.apply(7, ofFloat);
        this.mDimAnimator = ofFloat;
        this.mDimAnimator.start();
        referenceCountedTrigger.increment();
        hideActionButton(true, i, !z, referenceCountedTrigger.decrementOnAnimationEnd());
    }

    public void onStartFrontTaskEnterAnimation(boolean z) {
        if (z) {
            showActionButton(false, 0);
        }
    }

    public void onTaskBound(Task task, boolean z, int i, Rect rect) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        this.mTouchExplorationEnabled = z;
        this.mTask = task;
        this.mTask.addCallback(this);
        this.mIsDisabledInSafeMode = !this.mTask.isSystemApp && systemServices.isInSafeMode();
        this.mThumbnailView.bindToTask(this.mTask, this.mIsDisabledInSafeMode, i, rect);
        this.mHeaderView.bindToTask(this.mTask, this.mTouchExplorationEnabled, this.mIsDisabledInSafeMode);
        if (task.isDockable || !systemServices.hasDockedTask()) {
            View view = this.mIncompatibleAppToastView;
            if (view != null) {
                view.setVisibility(4);
            }
        } else {
            if (this.mIncompatibleAppToastView == null) {
                this.mIncompatibleAppToastView = Utilities.findViewStubById((View) this, (int) R.id.incompatible_app_toast_stub).inflate();
                ((TextView) findViewById(16908299)).setText(R.string.recents_incompatible_app_message);
            }
            this.mIncompatibleAppToastView.setVisibility(0);
        }
        int i2 = 8;
        this.mAccessLockView.setVisibility(task.isAccessLocked ? 0 : 8);
        View view2 = this.mScreeningView;
        if (task.key.isScreening) {
            i2 = 0;
        }
        view2.setVisibility(i2);
        updateLockedFlagVisible(this.mTask.isLocked);
    }

    public void onTaskDataLoaded(Task task, ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo) {
        this.mThumbnailView.onTaskDataLoaded(activityManager$TaskThumbnailInfo);
        this.mHeaderView.onTaskDataLoaded();
    }

    public void onTaskDataUnloaded() {
        this.mTask.removeCallback(this);
        this.mThumbnailView.unbindFromTask();
        this.mHeaderView.unbindFromTask(this.mTouchExplorationEnabled);
    }

    public void onTaskStackIdChanged() {
        this.mHeaderView.bindToTask(this.mTask, this.mTouchExplorationEnabled, this.mIsDisabledInSafeMode);
        this.mHeaderView.onTaskDataLoaded();
    }

    public void onClick(View view) {
        boolean z;
        if (this.mIsDisabledInSafeMode) {
            Context context = getContext();
            String string = context.getString(R.string.recents_launch_disabled_message, new Object[]{this.mTask.title});
            Toast toast = this.mDisabledAppToast;
            if (toast != null) {
                toast.cancel();
            }
            this.mDisabledAppToast = Toast.makeText(context, string, 0);
            this.mDisabledAppToast.show();
            return;
        }
        View view2 = this.mActionButtonView;
        if (view == view2) {
            view2.setTranslationZ(0.0f);
            z = true;
        } else {
            z = false;
        }
        RecentsEventBus.getDefault().send(new LaunchTaskEvent(this, this.mTask, (Rect) null, -1, z));
        RecentsEventBus.getDefault().send(new ClickTaskViewToLaunchTaskEvent(this.mTask));
        MetricsLogger.action(view.getContext(), 277, this.mTask.key.getComponent().toString());
    }

    public boolean onLongClick(View view) {
        if (RecentsConfiguration.sCanMultiWindow) {
            return startDrag();
        }
        if (Utilities.isLowMemoryDevices()) {
            RecentsEventBus.getDefault().send(new ShowApplicationInfoEvent(this.mTask));
            return true;
        }
        RecentsEventBus.getDefault().send(new ShowTaskMenuEvent(this));
        return true;
    }

    public boolean startDrag() {
        boolean z;
        if (!this.mIsDragging && waitForDragToEnterMultiWindowMode()) {
            SystemServicesProxy systemServices = Recents.getSystemServices();
            Rect rect = new Rect(this.mViewBounds.mClipBounds);
            if (!rect.isEmpty()) {
                rect.scale(getScaleX());
                Point point = this.mDownTouchPos;
                z = rect.contains(point.x, point.y);
            } else {
                z = this.mDownTouchPos.x <= getWidth() && this.mDownTouchPos.y <= getHeight();
            }
            if (z && !systemServices.hasDockedTask()) {
                setTranslationZ(10.0f);
                setClipViewInStack(false);
                RecentsEventBus.getDefault().register(this, 3);
                RecentsEventBus.getDefault().send(new DragStartEvent(this.mTask, this));
                return true;
            }
        }
        return false;
    }

    public final void onBusEvent(DragStartEvent dragStartEvent) {
        this.mIsDragging = true;
    }

    public final void onBusEvent(DragEndEvent dragEndEvent) {
        this.mIsDragging = false;
        postDelayed(new Runnable() {
            public void run() {
                TaskView.this.setTranslationZ(0.0f);
            }
        }, 250);
        if (!(dragEndEvent.dropTarget instanceof TaskStack.DockState)) {
            dragEndEvent.addPostAnimationCallback(new Runnable() {
                public void run() {
                    TaskView.this.setClipViewInStack(true);
                }
            });
        }
    }

    public final void onBusEvent(DragEndCancelledEvent dragEndCancelledEvent) {
        dragEndCancelledEvent.addPostAnimationCallback(new Runnable() {
            public void run() {
                TaskView.this.setClipViewInStack(true);
            }
        });
    }

    public final void onBusEvent(UpdateLockStateEvent updateLockStateEvent) {
        updateLockedFlagVisible(this.mTask.isLocked);
    }

    public final void onBusEvent(MultiWindowStateChangedEvent multiWindowStateChangedEvent) {
        View view;
        if (!multiWindowStateChangedEvent.inMultiWindow && (view = this.mIncompatibleAppToastView) != null) {
            view.setVisibility(4);
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        RecentsEventBus.getDefault().register(this, 3);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        RecentsEventBus.getDefault().unregister(this);
    }

    public static boolean waitForDragToEnterMultiWindowMode() {
        return RecentsConfiguration.sCanMultiWindow;
    }

    public void setIsScollAnimating(boolean z) {
        this.mIsScollAnimating = z;
    }

    public void changeChildrenAlpha(boolean z, float f) {
        if (z) {
            this.mHeaderView.animate().alpha(f).start();
            this.mThumbnailView.animate().alpha(f).start();
            this.mScreeningView.animate().alpha(f).start();
            this.mAccessLockView.animate().alpha(f).start();
            return;
        }
        this.mHeaderView.animate().cancel();
        this.mThumbnailView.animate().cancel();
        this.mScreeningView.animate().cancel();
        this.mAccessLockView.animate().cancel();
        this.mHeaderView.setAlpha(f);
        this.mThumbnailView.setAlpha(f);
        this.mScreeningView.setAlpha(f);
        this.mAccessLockView.setAlpha(f);
    }

    public SpringAnimationImpl getSpringAnimationImpl() {
        return this.mSpringAnimationImpl;
    }
}
