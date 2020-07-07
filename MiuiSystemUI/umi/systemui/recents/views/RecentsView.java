package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.ContextCompat;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.AppTransitionAnimationSpec;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewPropertyAnimator;
import android.view.WindowInsets;
import android.view.WindowInsetsCompat;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Constants;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.DockedFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.HideStackActionButtonEvent;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.ShowStackActionButtonEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEndedEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.misc.SpringAnimationImpl;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.RecentsTransitionHelper;
import com.android.systemui.stackdivider.WindowManagerProxy;
import com.android.systemui.statusbar.FlingAnimationUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class RecentsView extends FrameLayout {
    private boolean mAwaitingFirstLayout;
    private Drawable mBackgroundScrim;
    private Animator mBackgroundScrimAnimator;
    private float mDefaultScrimAlpha;
    private int mDividerSize;
    private TextView mEmptyView;
    private final FlingAnimationUtils mFlingAnimationUtils;
    private boolean mLastTaskLaunchedWasFreeform;
    private Drawable mRecentBackground;
    private RecentMenuView mRecentMenuView;
    public SpringAnimationImpl mSpringAnimationImpl;
    private TaskStack mStack;
    /* access modifiers changed from: private */
    public TextView mStackActionButton;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mSystemInsets;
    /* access modifiers changed from: private */
    public TaskStackView mTaskStackView;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "touch_")
    private RecentsViewTouchHandler mTouchHandler;
    /* access modifiers changed from: private */
    public RecentsTransitionHelper mTransitionHelper;

    private void hideStackActionButton(int i, boolean z) {
    }

    public final void onBusEvent(HideStackActionButtonEvent hideStackActionButtonEvent) {
    }

    public final void onBusEvent(ShowStackActionButtonEvent showStackActionButtonEvent) {
    }

    public final void onBusEvent(DragEndCancelledEvent dragEndCancelledEvent) {
    }

    public final void onBusEvent(DragStartEvent dragStartEvent) {
    }

    public RecentsView(Context context) {
        this(context, (AttributeSet) null);
    }

    public RecentsView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RecentsView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public RecentsView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mAwaitingFirstLayout = true;
        this.mSystemInsets = new Rect();
        setWillNotDraw(false);
        SystemServicesProxy systemServices = Recents.getSystemServices();
        this.mTransitionHelper = new RecentsTransitionHelper(getContext());
        this.mDividerSize = systemServices.getDockedDividerSize(context);
        this.mTouchHandler = new RecentsViewTouchHandler(this);
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, 0.3f);
        LayoutInflater from = LayoutInflater.from(context);
        TextView textView = (TextView) from.inflate(R.layout.recents_empty, this, false);
        this.mEmptyView = textView;
        textView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                RecentsEventBus.getDefault().send(new HideRecentsEvent(false, true, false));
            }
        });
        addView(this.mEmptyView);
        RecentMenuView recentMenuView = (RecentMenuView) from.inflate(R.layout.recent_menu_view, this, false);
        this.mRecentMenuView = recentMenuView;
        addView(recentMenuView, -1, -1);
        this.mDefaultScrimAlpha = context.getResources().getFloat(R.dimen.recent_background_scrim_alpha);
        this.mBackgroundScrim = new ColorDrawable(Color.argb((int) (this.mDefaultScrimAlpha * 255.0f), 0, 0, 0)).mutate();
        this.mRecentBackground = context.getResources().getDrawable(R.drawable.recent_task_bg, context.getTheme());
        this.mSpringAnimationImpl = new SpringAnimationImpl(this);
    }

    public void onReload(boolean z, boolean z2) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (this.mTaskStackView == null) {
            TaskStackView taskStackView = new TaskStackView(getContext());
            this.mTaskStackView = taskStackView;
            taskStackView.setSystemInsets(this.mSystemInsets);
            addView(this.mTaskStackView);
            this.mRecentMenuView.setTaskStackView(this.mTaskStackView);
            RecentsEventBus.getDefault().register(this.mTaskStackView, 3);
            z = false;
        }
        this.mAwaitingFirstLayout = !z;
        this.mLastTaskLaunchedWasFreeform = false;
        this.mTaskStackView.onReload(z);
        if (launchState.launchedViaFsGesture) {
            this.mBackgroundScrim.setAlpha(255);
        } else if (z) {
            animateBackgroundScrim(1.0f, 200);
        } else if (launchState.launchedViaDockGesture || launchState.launchedFromApp || z2) {
            this.mBackgroundScrim.setAlpha(255);
        } else {
            this.mBackgroundScrim.setAlpha(0);
        }
    }

    public void updateStack(TaskStack taskStack, boolean z) {
        this.mStack = taskStack;
        if (z) {
            this.mTaskStackView.setTasks(taskStack, true);
        }
        if (taskStack.getTaskCount() > 0) {
            hideEmptyView();
        } else {
            showEmptyView(Recents.getSystemServices().hasDockedTask() ? R.string.recents_empty_message_multi_window : R.string.recents_empty_message);
        }
    }

    public TaskStack getStack() {
        return this.mStack;
    }

    public Drawable getBackgroundScrim() {
        Drawable drawable = this.mRecentBackground;
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap.getWidth() == 1 && bitmap.getHeight() == 1 && Color.alpha(bitmap.getPixel(0, 0)) == 0) {
                return this.mBackgroundScrim;
            }
        }
        return this.mRecentBackground;
    }

    public boolean isLastTaskLaunchedFreeform() {
        return this.mLastTaskLaunchedWasFreeform;
    }

    public boolean launchTargetTask(int i) {
        Task launchTarget;
        TaskStackView taskStackView = this.mTaskStackView;
        if (taskStackView == null || (launchTarget = taskStackView.getStack().getLaunchTarget()) == null) {
            return false;
        }
        RecentsEventBus.getDefault().send(new LaunchTaskEvent(this.mTaskStackView.getChildViewForTask(launchTarget), launchTarget, (Rect) null, -1, false));
        if (i == 0) {
            return true;
        }
        MetricsLogger.action(getContext(), i, launchTarget.key.getComponent().toString());
        return true;
    }

    public boolean launchPreviousTask() {
        Task launchTarget;
        TaskStackView taskStackView = this.mTaskStackView;
        if (taskStackView == null || (launchTarget = taskStackView.getStack().getLaunchTarget()) == null) {
            return false;
        }
        RecentsEventBus.getDefault().send(new LaunchTaskEvent(this.mTaskStackView.getChildViewForTask(launchTarget), launchTarget, (Rect) null, -1, false));
        return true;
    }

    public void showEmptyView(int i) {
        this.mEmptyView.setText(i);
        this.mEmptyView.setVisibility(0);
    }

    public void hideEmptyView() {
        this.mEmptyView.setVisibility(4);
        this.mTaskStackView.setVisibility(0);
        this.mTaskStackView.bringToFront();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        RecentsEventBus.getDefault().register(this, 3);
        RecentsEventBus.getDefault().register(this.mTouchHandler, 4);
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        RecentsEventBus.getDefault().unregister(this);
        RecentsEventBus.getDefault().unregister(this.mTouchHandler);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        if (this.mTaskStackView.getVisibility() != 8) {
            this.mTaskStackView.measure(i, i2);
        }
        if (this.mEmptyView.getVisibility() != 8) {
            measureChild(this.mEmptyView, View.MeasureSpec.makeMeasureSpec(size, 1073741824), View.MeasureSpec.makeMeasureSpec(size2, 1073741824));
        }
        if (this.mRecentMenuView.getVisibility() != 8) {
            measureChild(this.mRecentMenuView, View.MeasureSpec.makeMeasureSpec(size, 1073741824), View.MeasureSpec.makeMeasureSpec(size2, 1073741824));
        }
        setMeasuredDimension(size, size2);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.mTaskStackView.getVisibility() != 8) {
            this.mTaskStackView.layout(i, i2, getMeasuredWidth() + i, getMeasuredHeight() + i2);
        }
        if (this.mEmptyView.getVisibility() != 8) {
            Rect rect = this.mSystemInsets;
            int i5 = rect.left + rect.right;
            int i6 = rect.top + rect.bottom;
            int measuredWidth = this.mEmptyView.getMeasuredWidth();
            int measuredHeight = this.mEmptyView.getMeasuredHeight();
            int max = this.mSystemInsets.left + i + (Math.max(0, ((i3 - i) - i5) - measuredWidth) / 2);
            int max2 = this.mSystemInsets.top + i2 + (Math.max(0, ((i4 - i2) - i6) - measuredHeight) / 2);
            this.mEmptyView.layout(max, max2, measuredWidth + max, measuredHeight + max2);
        }
        if (this.mRecentMenuView.getVisibility() != 8) {
            RecentMenuView recentMenuView = this.mRecentMenuView;
            recentMenuView.layout(0, 0, recentMenuView.getMeasuredWidth(), this.mRecentMenuView.getMeasuredHeight());
        }
        if (this.mAwaitingFirstLayout) {
            this.mAwaitingFirstLayout = false;
            if (Recents.getConfiguration().getLaunchState().launchedViaDragGesture) {
                setTranslationY((float) getMeasuredHeight());
            }
        }
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mSystemInsets.set(WindowInsetsCompat.getSystemWindowInsetsAsRect(windowInsets));
        this.mTaskStackView.setSystemInsets(this.mSystemInsets);
        requestLayout();
        return windowInsets;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return this.mTouchHandler.onInterceptTouchEvent(motionEvent);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.mTouchHandler.onTouchEvent(motionEvent);
    }

    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        ArrayList<TaskStack.DockState> visibleDockStates = this.mTouchHandler.getVisibleDockStates();
        for (int size = visibleDockStates.size() - 1; size >= 0; size--) {
            visibleDockStates.get(size).viewState.draw(canvas);
        }
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable drawable) {
        ArrayList<TaskStack.DockState> visibleDockStates = this.mTouchHandler.getVisibleDockStates();
        for (int size = visibleDockStates.size() - 1; size >= 0; size--) {
            if (visibleDockStates.get(size).viewState.dockAreaOverlay == drawable) {
                return true;
            }
        }
        return super.verifyDrawable(drawable);
    }

    public final void onBusEvent(LaunchTaskEvent launchTaskEvent) {
        this.mLastTaskLaunchedWasFreeform = launchTaskEvent.task.isFreeformTask();
        if (Recents.getConfiguration().getLaunchState().launchedViaFsGesture && Recents.getConfiguration().getLaunchState().launchedFromHome) {
            Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, 0.0f, 1.0f);
        }
        this.mTransitionHelper.launchTaskFromRecents(this.mStack, launchTaskEvent.task, this.mTaskStackView, launchTaskEvent.taskView, launchTaskEvent.screenPinningRequested, launchTaskEvent.targetTaskBounds, launchTaskEvent.targetTaskStack);
    }

    public final void onBusEvent(DismissRecentsToHomeAnimationStarted dismissRecentsToHomeAnimationStarted) {
        animateBackgroundScrim(0.0f, 350);
    }

    public final void onBusEvent(DragDropTargetChangedEvent dragDropTargetChangedEvent) {
        DropTarget dropTarget = dragDropTargetChangedEvent.dropTarget;
        if (dropTarget == null || !(dropTarget instanceof TaskStack.DockState)) {
            TaskStack.DockState[] dockStatesForCurrentOrientation = this.mTouchHandler.getDockStatesForCurrentOrientation();
            TaskStack.DockState.ViewState viewState = TaskStack.DockState.NONE.viewState;
            updateVisibleDockRegions(dockStatesForCurrentOrientation, true, viewState.dockAreaAlpha, viewState.hintTextAlpha, true, true);
        } else {
            updateVisibleDockRegions(new TaskStack.DockState[]{(TaskStack.DockState) dropTarget}, false, -1, -1, true, true);
        }
        if (this.mStackActionButton != null) {
            dragDropTargetChangedEvent.addPostAnimationCallback(new Runnable() {
                public void run() {
                    Rect access$000 = RecentsView.this.getStackActionButtonBoundsFromStackLayout();
                    RecentsView.this.mStackActionButton.setLeftTopRightBottom(access$000.left, access$000.top, access$000.right, access$000.bottom);
                }
            });
        }
    }

    public final void onBusEvent(final DragEndEvent dragEndEvent) {
        DropTarget dropTarget = dragEndEvent.dropTarget;
        if (dropTarget instanceof TaskStack.DockState) {
            updateVisibleDockRegions((TaskStack.DockState[]) null, false, -1, -1, false, false);
            RecentsConfiguration.sCanMultiWindow = false;
            Utilities.setViewFrameFromTranslation(dragEndEvent.taskView);
            SystemServicesProxy systemServices = Recents.getSystemServices();
            if (systemServices.startTaskInDockedMode(dragEndEvent.task, ((TaskStack.DockState) dropTarget).createMode, getContext())) {
                this.mTaskStackView.mIsMultiStateChanging = true;
                if (!Utilities.isAndroidNorNewer()) {
                    dragEndEvent.taskView.setVisibility(4);
                }
                AnonymousClass5 r0 = new ActivityOptions.OnAnimationStartedListener() {
                    public void onAnimationStarted() {
                        RecentsEventBus.getDefault().send(new DockedFirstAnimationFrameEvent());
                        RecentsView.this.mTaskStackView.getStack().removeTask(dragEndEvent.task, (AnimationProps) null, true);
                        if (!Utilities.isAndroidNorNewer()) {
                            dragEndEvent.taskView.setVisibility(0);
                        }
                    }
                };
                final Rect taskRect = getTaskRect(dragEndEvent.taskView);
                systemServices.overridePendingAppTransitionMultiThumbFuture(this.mTransitionHelper.getAppTransitionFuture(new RecentsTransitionHelper.AnimationSpecComposer() {
                    public List<AppTransitionAnimationSpec> composeSpecs() {
                        return RecentsView.this.mTransitionHelper.composeDockAnimationSpec(dragEndEvent.taskView, taskRect);
                    }
                }, getHandler()), this.mTransitionHelper.wrapStartedListener(r0), true, ContextCompat.getDisplayId(getContext()));
                MetricsLogger.action(this.mContext, 270, dragEndEvent.task.getTopComponent().flattenToShortString());
            } else {
                RecentsEventBus.getDefault().send(new DragEndCancelledEvent(this.mStack, dragEndEvent.task, dragEndEvent.taskView));
            }
        }
        TextView textView = this.mStackActionButton;
        if (textView != null) {
            textView.animate().alpha(1.0f).setDuration(134).setInterpolator(Interpolators.ALPHA_IN).start();
        }
    }

    private Rect getTaskRect(TaskView taskView) {
        int[] locationOnScreen = taskView.getLocationOnScreen();
        int i = locationOnScreen[0];
        int i2 = locationOnScreen[1];
        return new Rect(i, i2, (int) (((float) i) + (((float) taskView.getWidth()) * taskView.getScaleX())), (int) (((float) i2) + (((float) taskView.getHeight()) * taskView.getScaleY())));
    }

    public final void onBusEvent(DraggingInRecentsEvent draggingInRecentsEvent) {
        if (this.mTaskStackView.getTaskViews().size() > 0) {
            setTranslationY(draggingInRecentsEvent.distanceFromTop - this.mTaskStackView.getTaskViews().get(0).getY());
        }
    }

    public final void onBusEvent(DraggingInRecentsEndedEvent draggingInRecentsEndedEvent) {
        ViewPropertyAnimator animate = animate();
        if (draggingInRecentsEndedEvent.velocity > this.mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            animate.translationY((float) getHeight());
            animate.withEndAction(new Runnable() {
                public void run() {
                    WindowManagerProxy.getInstance().maximizeDockedStack();
                }
            });
            this.mFlingAnimationUtils.apply(animate, getTranslationY(), (float) getHeight(), draggingInRecentsEndedEvent.velocity);
        } else {
            animate.translationY(0.0f);
            animate.setListener((Animator.AnimatorListener) null);
            this.mFlingAnimationUtils.apply(animate, getTranslationY(), 0.0f, draggingInRecentsEndedEvent.velocity);
        }
        animate.start();
    }

    public final void onBusEvent(EnterRecentsWindowAnimationCompletedEvent enterRecentsWindowAnimationCompletedEvent) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (!launchState.launchedViaDockGesture && !launchState.launchedFromApp && !launchState.launchedViaFsGesture && this.mStack.getTaskCount() > 0) {
            animateBackgroundScrim(1.0f, 180);
        }
    }

    public final void onBusEvent(AllTaskViewsDismissedEvent allTaskViewsDismissedEvent) {
        hideStackActionButton(100, true);
    }

    public final void onBusEvent(DismissAllTaskViewsEvent dismissAllTaskViewsEvent) {
        Recents.getSystemServices().hasDockedTask();
    }

    public final void onBusEvent(MultiWindowStateChangedEvent multiWindowStateChangedEvent) {
        updateStack(multiWindowStateChangedEvent.stack, false);
    }

    public final void onBusEvent(ConfigurationChangedEvent configurationChangedEvent) {
        if (configurationChangedEvent.fromDeviceOrientationChange) {
            hideDockRegionsAnim();
            if (RecentsConfiguration.sCanMultiWindow) {
                postDelayed(new Runnable() {
                    public void run() {
                        RecentsView.this.showDockRegionsAnim();
                    }
                }, 100);
            }
            Configuration appConfiguration = Utilities.getAppConfiguration(getContext());
            WallpaperInfo wallpaperInfo = WallpaperManager.getInstance(getContext()).getWallpaperInfo();
            if (appConfiguration.orientation != 2 || wallpaperInfo == null) {
                this.mBackgroundScrim.setColorFilter((ColorFilter) null);
            } else {
                this.mBackgroundScrim.setColorFilter(-16777216, PorterDuff.Mode.SRC);
            }
            this.mRecentMenuView.removeMenu(false);
        }
    }

    private void updateVisibleDockRegions(TaskStack.DockState[] dockStateArr, boolean z, int i, int i2, boolean z2, boolean z3) {
        int i3;
        int i4;
        int i5;
        Rect rect;
        TaskStack.DockState[] dockStateArr2 = dockStateArr;
        ArraySet arraySet = new ArraySet();
        Utilities.arrayToSet(dockStateArr2, arraySet);
        ArrayList<TaskStack.DockState> visibleDockStates = this.mTouchHandler.getVisibleDockStates();
        for (int size = visibleDockStates.size() - 1; size >= 0; size--) {
            TaskStack.DockState dockState = visibleDockStates.get(size);
            TaskStack.DockState.ViewState viewState = dockState.viewState;
            if (dockStateArr2 == null || !arraySet.contains(dockState)) {
                int i6 = i;
                viewState.startAnimation((Rect) null, 0, 0, 250, Interpolators.FAST_OUT_SLOW_IN, z2, z3);
            } else {
                int i7 = i;
                if (i7 != -1) {
                    i3 = i2;
                    i4 = i7;
                } else {
                    i3 = i2;
                    i4 = viewState.dockAreaAlpha;
                }
                if (i3 != -1) {
                    i5 = i3;
                } else {
                    i5 = viewState.hintTextAlpha;
                }
                if (z) {
                    rect = dockState.getPreDockedBounds(getMeasuredWidth(), getMeasuredHeight());
                } else {
                    rect = dockState.getDockedBounds(getMeasuredWidth(), getMeasuredHeight(), this.mDividerSize, this.mSystemInsets, getResources());
                }
                Rect rect2 = rect;
                if (viewState.dockAreaOverlay.getCallback() != this) {
                    viewState.dockAreaOverlay.setCallback(this);
                    viewState.dockAreaOverlay.setBounds(rect2);
                }
                viewState.startAnimation(rect2, i4, i5, 250, Interpolators.FAST_OUT_SLOW_IN, z2, z3);
            }
        }
    }

    private void animateBackgroundScrim(float f, int i) {
        Interpolator interpolator;
        Utilities.cancelAnimationWithoutCallbacks(this.mBackgroundScrimAnimator);
        int alpha = (int) ((((float) this.mBackgroundScrim.getAlpha()) / (this.mDefaultScrimAlpha * 255.0f)) * 255.0f);
        int i2 = (int) (f * 255.0f);
        ObjectAnimator ofInt = ObjectAnimator.ofInt(this.mBackgroundScrim, Utilities.DRAWABLE_ALPHA, new int[]{alpha, i2});
        this.mBackgroundScrimAnimator = ofInt;
        ofInt.setDuration((long) i);
        Animator animator = this.mBackgroundScrimAnimator;
        if (i2 > alpha) {
            interpolator = Interpolators.MIUI_ALPHA_IN;
        } else {
            interpolator = Interpolators.MIUI_ALPHA_OUT;
        }
        animator.setInterpolator(interpolator);
        this.mBackgroundScrimAnimator.start();
    }

    /* access modifiers changed from: private */
    public Rect getStackActionButtonBoundsFromStackLayout() {
        int i;
        int i2;
        Rect rect = new Rect(this.mTaskStackView.mLayoutAlgorithm.mStackActionButtonRect);
        if (isLayoutRtl()) {
            i2 = rect.left;
            i = this.mStackActionButton.getPaddingLeft();
        } else {
            i2 = rect.right + this.mStackActionButton.getPaddingRight();
            i = this.mStackActionButton.getMeasuredWidth();
        }
        int i3 = i2 - i;
        int height = rect.top + ((rect.height() - this.mStackActionButton.getMeasuredHeight()) / 2);
        rect.set(i3, height, this.mStackActionButton.getMeasuredWidth() + i3, this.mStackActionButton.getMeasuredHeight() + height);
        return rect;
    }

    public void showDockRegionsAnim() {
        this.mTouchHandler.setupVisibleDockStates();
        TaskStack.DockState[] dockStatesForCurrentOrientation = this.mTouchHandler.getDockStatesForCurrentOrientation();
        TaskStack.DockState.ViewState viewState = TaskStack.DockState.NONE.viewState;
        updateVisibleDockRegions(dockStatesForCurrentOrientation, true, viewState.dockAreaAlpha, viewState.hintTextAlpha, true, false);
    }

    public void hideDockRegionsAnim() {
        this.mTouchHandler.setupVisibleDockStates();
        updateVisibleDockRegions((TaskStack.DockState[]) null, true, -1, -1, true, false);
    }

    public void dump(String str, PrintWriter printWriter) {
        String str2 = str + "  ";
        String hexString = Integer.toHexString(System.identityHashCode(this));
        printWriter.print(str);
        printWriter.print("RecentsView");
        printWriter.print(" awaitingFirstLayout=");
        printWriter.print(this.mAwaitingFirstLayout ? "Y" : "N");
        printWriter.print(" insets=");
        printWriter.print(Utilities.dumpRect(this.mSystemInsets));
        printWriter.print(" [0x");
        printWriter.print(hexString);
        printWriter.print("]");
        printWriter.println();
        TaskStack taskStack = this.mStack;
        if (taskStack != null) {
            taskStack.dump(str2, printWriter);
        }
        TaskStackView taskStackView = this.mTaskStackView;
        if (taskStackView != null) {
            taskStackView.dump(str2, printWriter);
        }
    }

    public RecentMenuView getMenuView() {
        return this.mRecentMenuView;
    }

    public void updateBlurRatio(float f) {
        try {
            View rootView = getRootView();
            if (rootView != null && (rootView.getLayoutParams() instanceof WindowManager.LayoutParams)) {
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) rootView.getLayoutParams();
                WindowManagerGlobal instance = WindowManagerGlobal.getInstance();
                layoutParams.flags |= 4;
                layoutParams.blurRatio = f;
                instance.updateViewLayout(rootView, layoutParams);
            }
        } catch (Exception e) {
            Log.e("RecentsView", "updateBlurRatio error.", e);
        }
    }

    public float getBlurRatio() {
        try {
            View rootView = getRootView();
            if (rootView == null || !(rootView.getLayoutParams() instanceof WindowManager.LayoutParams)) {
                return 0.0f;
            }
            return ((WindowManager.LayoutParams) rootView.getLayoutParams()).blurRatio;
        } catch (Exception e) {
            Log.e("RecentsView", "getBlurRatio error.", e);
            return 0.0f;
        }
    }

    public int getTaskViewPaddingView() {
        TaskStackLayoutAlgorithm taskStackLayoutAlgorithm = this.mTaskStackView.mLayoutAlgorithm;
        return taskStackLayoutAlgorithm.mPaddingTop + taskStackLayoutAlgorithm.mVerticalGap;
    }

    public List<TaskView> getTaskViews() {
        TaskStackView taskStackView = this.mTaskStackView;
        if (taskStackView != null) {
            return taskStackView.getTaskViews();
        }
        return new ArrayList();
    }

    public void resetProperty() {
        setAlpha(1.0f);
        setTranslationX(0.0f);
        setTranslationY(0.0f);
        setScaleX(1.0f);
        setScaleY(1.0f);
    }

    public void requstLayoutTaskStackView() {
        TaskStackView taskStackView = this.mTaskStackView;
        if (taskStackView != null) {
            taskStackView.requestLayout();
        }
    }

    public void release() {
        if (this.mTaskStackView != null) {
            RecentsEventBus.getDefault().unregister(this.mTaskStackView);
        }
    }

    public SpringAnimationImpl getSpringAnimationImpl() {
        return this.mSpringAnimationImpl;
    }
}
