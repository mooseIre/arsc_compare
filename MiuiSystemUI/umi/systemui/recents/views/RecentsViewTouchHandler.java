package com.android.systemui.recents.views;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import com.android.systemui.recents.BaseRecentsImpl;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.ui.HideIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.ShowIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartInitializeDropTargetsEvent;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.stackdivider.DividerSnapAlgorithm;
import java.util.ArrayList;
import java.util.Iterator;

public class RecentsViewTouchHandler {
    private DividerSnapAlgorithm mDividerSnapAlgorithm;
    @ViewDebug.ExportedProperty(category = "recents")
    private Point mDownPos = new Point();
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mDragRequested;
    private float mDragSlop;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "drag_task")
    private Task mDragTask;
    private ArrayList<DropTarget> mDropTargets = new ArrayList<>();
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mIsDragging;
    private boolean mIsRemovingMenu = false;
    private DropTarget mLastDropTarget;
    private RecentsView mRv;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "drag_task_view_")
    private TaskView mTaskView;
    private ArrayList<TaskStack.DockState> mVisibleDockStates = new ArrayList<>();

    public RecentsViewTouchHandler(RecentsView recentsView) {
        this.mRv = recentsView;
        this.mDragSlop = (float) ViewConfiguration.get(recentsView.getContext()).getScaledTouchSlop();
        updateSnapAlgorithm();
    }

    private void updateSnapAlgorithm() {
        Rect rect = new Rect();
        SystemServicesProxy.getInstance(this.mRv.getContext()).getStableInsets(rect);
        this.mDividerSnapAlgorithm = DividerSnapAlgorithm.create(this.mRv.getContext(), rect);
    }

    public void registerDropTargetForCurrentDrag(DropTarget dropTarget) {
        this.mDropTargets.add(dropTarget);
    }

    public TaskStack.DockState[] getDockStatesForCurrentOrientation() {
        boolean z = this.mRv.getResources().getConfiguration().orientation == 2;
        RecentsConfiguration configuration = Recents.getConfiguration();
        if (z) {
            return configuration.isLargeScreen ? DockRegion.TABLET_LANDSCAPE : DockRegion.PHONE_LANDSCAPE;
        }
        if (configuration.isLargeScreen) {
            return DockRegion.TABLET_PORTRAIT;
        }
        return RecentsActivity.isForceBlack() ? DockRegion.PHONE_PORTRAIT_FORCE_BLACK : DockRegion.PHONE_PORTRAIT;
    }

    public ArrayList<TaskStack.DockState> getVisibleDockStates() {
        return this.mVisibleDockStates;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        handleTouchEvent(motionEvent);
        return this.mDragRequested || this.mIsRemovingMenu || BaseRecentsImpl.sOneKeyCleaning;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        handleTouchEvent(motionEvent);
        return this.mDragRequested;
    }

    public final void onBusEvent(DragStartEvent dragStartEvent) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        this.mRv.getParent().requestDisallowInterceptTouchEvent(true);
        this.mDragRequested = true;
        this.mIsDragging = false;
        this.mDragTask = dragStartEvent.task;
        this.mTaskView = dragStartEvent.taskView;
        this.mDropTargets.clear();
        this.mVisibleDockStates.clear();
        if (!systemServices.hasDockedTask() && this.mDividerSnapAlgorithm.isSplitScreenFeasible()) {
            Recents.logDockAttempt(this.mRv.getContext(), dragStartEvent.task.getTopComponent(), dragStartEvent.task.resizeMode);
            if (!dragStartEvent.task.isDockable) {
                RecentsEventBus.getDefault().send(new ShowIncompatibleAppOverlayEvent());
            } else {
                setupVisibleDockStates();
            }
        }
        RecentsEventBus.getDefault().send(new DragStartInitializeDropTargetsEvent(dragStartEvent.task, dragStartEvent.taskView, this));
    }

    public void setupVisibleDockStates() {
        this.mDropTargets.clear();
        this.mVisibleDockStates.clear();
        for (TaskStack.DockState dockState : getDockStatesForCurrentOrientation()) {
            registerDropTargetForCurrentDrag(dockState);
            dockState.update(this.mRv.getContext());
            this.mVisibleDockStates.add(dockState);
        }
    }

    public final void onBusEvent(DragEndEvent dragEndEvent) {
        Task task = this.mDragTask;
        if (task == null || !task.isDockable) {
            RecentsEventBus.getDefault().send(new HideIncompatibleAppOverlayEvent());
        }
        this.mDragRequested = false;
        this.mDragTask = null;
        this.mTaskView = null;
        this.mLastDropTarget = null;
    }

    public final void onBusEvent(ConfigurationChangedEvent configurationChangedEvent) {
        if (configurationChangedEvent.fromDisplayDensityChange || configurationChangedEvent.fromDeviceOrientationChange) {
            updateSnapAlgorithm();
        }
    }

    public final void onBusEvent(RecentsVisibilityChangedEvent recentsVisibilityChangedEvent) {
        if (!recentsVisibilityChangedEvent.visible && this.mDragRequested) {
            RecentsEventBus.getDefault().send(new DragEndEvent(this.mDragTask, this.mTaskView, (DropTarget) null));
        }
    }

    private void handleTouchEvent(MotionEvent motionEvent) {
        Task task;
        Task.TaskKey taskKey;
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            boolean z = false;
            DropTarget dropTarget = null;
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    Point point = this.mDownPos;
                    int i = point.x;
                    float f = x - ((float) i);
                    int i2 = point.y;
                    float f2 = y - ((float) i2);
                    if (this.mDragRequested) {
                        if (!this.mIsDragging) {
                            if (Math.hypot((double) (x - ((float) i)), (double) (y - ((float) i2))) > ((double) this.mDragSlop)) {
                                z = true;
                            }
                            this.mIsDragging = z;
                        }
                        if (this.mIsDragging) {
                            int measuredWidth = this.mRv.getMeasuredWidth();
                            int measuredHeight = this.mRv.getMeasuredHeight();
                            DropTarget dropTarget2 = this.mLastDropTarget;
                            if (dropTarget2 != null && dropTarget2.acceptsDrop((int) x, (int) y, measuredWidth, measuredHeight, true)) {
                                dropTarget = this.mLastDropTarget;
                            }
                            if (dropTarget == null) {
                                Iterator<DropTarget> it = this.mDropTargets.iterator();
                                while (true) {
                                    if (!it.hasNext()) {
                                        break;
                                    }
                                    DropTarget next = it.next();
                                    int[] iArr = new int[2];
                                    int[] iArr2 = new int[2];
                                    TaskView taskView = this.mTaskView;
                                    if (taskView != null) {
                                        taskView.getLocationOnScreen(iArr);
                                        this.mRv.getLocationOnScreen(iArr2);
                                    }
                                    if (next.acceptsDrop((int) x, this.mTaskView != null ? (iArr[1] - iArr2[1]) + BaseRecentsImpl.mTaskBarHeight : (int) y, measuredWidth, measuredHeight, false)) {
                                        dropTarget = next;
                                        break;
                                    }
                                }
                            }
                            if (this.mLastDropTarget != dropTarget) {
                                this.mLastDropTarget = dropTarget;
                                RecentsEventBus.getDefault().send(new DragDropTargetChangedEvent(this.mDragTask, dropTarget));
                            }
                        }
                        this.mTaskView.setTranslationX(f);
                        this.mTaskView.setTranslationY(f2);
                        return;
                    }
                    return;
                } else if (actionMasked != 3) {
                    return;
                }
            }
            if (this.mDragRequested) {
                if (actionMasked == 3) {
                    z = true;
                }
                if (z) {
                    RecentsEventBus.getDefault().send(new DragDropTargetChangedEvent(this.mDragTask, (DropTarget) null));
                }
                if (!(z || !(this.mLastDropTarget instanceof TaskStack.DockState) || (task = this.mDragTask) == null || (taskKey = task.key) == null || taskKey.getComponent() == null)) {
                    RecentsPushEventHelper.sendEnterMultiWindowEvent("drag", this.mDragTask.key.getComponent().getPackageName());
                }
                RecentsEventBus recentsEventBus = RecentsEventBus.getDefault();
                Task task2 = this.mDragTask;
                TaskView taskView2 = this.mTaskView;
                if (!z) {
                    dropTarget = this.mLastDropTarget;
                }
                recentsEventBus.send(new DragEndEvent(task2, taskView2, dropTarget));
                return;
            }
            return;
        }
        this.mIsRemovingMenu = this.mRv.getMenuView().isShowOrHideAnimRunning();
        this.mDownPos.set((int) motionEvent.getX(), (int) motionEvent.getY());
    }
}
