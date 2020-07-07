package com.android.systemui.recents.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.ArraySet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ViewDebug;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import com.android.systemui.Application;
import com.android.systemui.Constants;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.BaseRecentsImpl;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.misc.FreePathInterpolator;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.stackdivider.Divider;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TaskStackLayoutAlgorithm {
    private AccelerateInterpolator mAccelerateInterpolator;
    TaskViewTransform mBackOfStackTransform;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mBaseBottomMargin;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mBaseSideMargin;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mBaseTopMargin;
    private TaskStackLayoutAlgorithmCallbacks mCb;
    Context mContext;
    public boolean mDropToDockState;
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect mFirstTaskRect;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mFocusState;
    private Path mFocusedCurve = linearCurve();
    private FreePathInterpolator mFocusedCurveInterpolator = new FreePathInterpolator(this.mFocusedCurve);
    private Path mFocusedDimCurve = linearCurve();
    private Range mFocusedRange;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mFocusedTopPeekHeight;
    FreeformWorkspaceLayoutAlgorithm mFreeformLayoutAlgorithm;
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect mFreeformRect = new Rect();
    @ViewDebug.ExportedProperty(category = "recents")
    private int mFreeformStackGap;
    TaskViewTransform mFrontOfStackTransform;
    int mHorizontalGap;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mInitialBottomOffset;
    @ViewDebug.ExportedProperty(category = "recents")
    float mInitialScrollP;
    @ViewDebug.ExportedProperty(category = "recents")
    private int mInitialTopOffset;
    private boolean mIsRtlLayout;
    @ViewDebug.ExportedProperty(category = "recents")
    float mMaxScrollP;
    @ViewDebug.ExportedProperty(category = "recents")
    int mMaxTranslationZ;
    private int mMinMargin;
    @ViewDebug.ExportedProperty(category = "recents")
    float mMinScrollP;
    @ViewDebug.ExportedProperty(category = "recents")
    int mNumFreeformTasks;
    @ViewDebug.ExportedProperty(category = "recents")
    int mNumStackTasks;
    int mPaddingBottom;
    int mPaddingLeft;
    int mPaddingRight;
    int mPaddingTop;
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect mStackActionButtonRect = new Rect();
    @ViewDebug.ExportedProperty(category = "recents")
    private int mStackBottomOffset;
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect mStackRect = new Rect();
    private StackState mState = StackState.SPLIT;
    private int mStatusbarHeight;
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect mSystemInsets = new Rect();
    private SparseIntArray mTaskIndexMap;
    private SparseArray<Float> mTaskIndexOverrideMap;
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect mTaskRect = new Rect();
    private int mTaskViewTop;
    private Path mUnfocusedCurve = linearCurve();
    private FreePathInterpolator mUnfocusedCurveInterpolator = new FreePathInterpolator(this.mUnfocusedCurve);
    private Path mUnfocusedDimCurve = linearCurve();
    private Range mUnfocusedRange;
    int mVerticalGap;
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect mWindowRect = new Rect();

    public interface TaskStackLayoutAlgorithmCallbacks {
        void onFocusStateChanged(int i, int i2);
    }

    public static class StackState {
        public static final StackState FREEFORM_ONLY = new StackState(1.0f, 255);
        public static final StackState SPLIT = new StackState(0.5f, 255);
        public static final StackState STACK_ONLY = new StackState(0.0f, 0);
        public final int freeformBackgroundAlpha;
        public final float freeformHeightPct;

        private StackState(float f, int i) {
            this.freeformHeightPct = f;
            this.freeformBackgroundAlpha = i;
        }

        public static StackState getStackStateForStack(TaskStack taskStack) {
            boolean hasFreeformWorkspaceSupport = Recents.getSystemServices().hasFreeformWorkspaceSupport();
            int freeformTaskCount = taskStack.getFreeformTaskCount();
            int stackTaskCount = taskStack.getStackTaskCount();
            if (hasFreeformWorkspaceSupport && stackTaskCount > 0 && freeformTaskCount > 0) {
                return SPLIT;
            }
            if (!hasFreeformWorkspaceSupport || freeformTaskCount <= 0) {
                return STACK_ONLY;
            }
            return FREEFORM_ONLY;
        }

        public void computeRects(Rect rect, Rect rect2, Rect rect3, int i, int i2, int i3) {
            int height = (int) (((float) ((rect3.height() - i) - i3)) * this.freeformHeightPct);
            int max = Math.max(0, height - i2);
            int i4 = rect3.left;
            int i5 = rect3.top;
            rect.set(i4, i5 + i, rect3.right, i5 + i + max);
            rect2.set(rect3.left, rect3.top, rect3.right, rect3.bottom);
            if (height > 0) {
                rect2.top += height;
            } else {
                rect2.top += i;
            }
        }
    }

    public class VisibilityReport {
        public int numVisibleTasks;
        public int numVisibleThumbnails;

        VisibilityReport(TaskStackLayoutAlgorithm taskStackLayoutAlgorithm, int i, int i2) {
            this.numVisibleTasks = i;
            this.numVisibleThumbnails = i2;
        }
    }

    public TaskStackLayoutAlgorithm(Context context, TaskStackLayoutAlgorithmCallbacks taskStackLayoutAlgorithmCallbacks) {
        new FreePathInterpolator(this.mUnfocusedDimCurve);
        new FreePathInterpolator(this.mFocusedDimCurve);
        this.mTaskIndexMap = new SparseIntArray();
        this.mTaskIndexOverrideMap = new SparseArray<>();
        this.mBackOfStackTransform = new TaskViewTransform();
        this.mFrontOfStackTransform = new TaskViewTransform();
        this.mFirstTaskRect = new Rect();
        this.mAccelerateInterpolator = new AccelerateInterpolator();
        this.mPaddingLeft = 0;
        this.mPaddingRight = 0;
        this.mDropToDockState = false;
        Resources resources = context.getResources();
        this.mContext = context;
        this.mCb = taskStackLayoutAlgorithmCallbacks;
        this.mFreeformLayoutAlgorithm = new FreeformWorkspaceLayoutAlgorithm(context);
        this.mMinMargin = resources.getDimensionPixelSize(R.dimen.recents_layout_min_margin);
        this.mBaseTopMargin = getDimensionForDevice(context, R.dimen.recents_layout_top_margin_phone, R.dimen.recents_layout_top_margin_tablet, R.dimen.recents_layout_top_margin_tablet_xlarge);
        this.mBaseSideMargin = getDimensionForDevice(context, R.dimen.recents_layout_side_margin_phone, R.dimen.recents_layout_side_margin_tablet, R.dimen.recents_layout_side_margin_tablet_xlarge);
        this.mBaseBottomMargin = resources.getDimensionPixelSize(R.dimen.recents_layout_bottom_margin);
        this.mFreeformStackGap = resources.getDimensionPixelSize(R.dimen.recents_freeform_layout_bottom_margin);
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        reloadOnConfigurationChange(context);
    }

    public void reloadOnConfigurationChange(Context context) {
        Resources resources = context.getResources();
        this.mFocusedRange = new Range(resources.getFloat(R.integer.recents_layout_focused_range_min), resources.getFloat(R.integer.recents_layout_focused_range_max));
        this.mUnfocusedRange = new Range(resources.getFloat(R.integer.recents_layout_unfocused_range_min), resources.getFloat(R.integer.recents_layout_unfocused_range_max));
        this.mFocusState = getInitialFocusState();
        this.mFocusedTopPeekHeight = resources.getDimensionPixelSize(R.dimen.recents_layout_top_peek_size);
        resources.getDimensionPixelSize(R.dimen.recents_layout_bottom_peek_size);
        resources.getDimensionPixelSize(R.dimen.recents_layout_z_min);
        this.mMaxTranslationZ = resources.getDimensionPixelSize(R.dimen.recents_layout_z_max);
        getDimensionForDevice(context, R.dimen.recents_layout_initial_top_offset_phone_port, R.dimen.recents_layout_initial_top_offset_phone_land, R.dimen.recents_layout_initial_top_offset_tablet, R.dimen.recents_layout_initial_top_offset_tablet, R.dimen.recents_layout_initial_top_offset_tablet, R.dimen.recents_layout_initial_top_offset_tablet);
        getDimensionForDevice(context, R.dimen.recents_layout_initial_bottom_offset_phone_port, R.dimen.recents_layout_initial_bottom_offset_phone_land, R.dimen.recents_layout_initial_bottom_offset_tablet, R.dimen.recents_layout_initial_bottom_offset_tablet, R.dimen.recents_layout_initial_bottom_offset_tablet, R.dimen.recents_layout_initial_bottom_offset_tablet);
        this.mTaskViewTop = getDimensionForDevice(context, R.dimen.recents_task_view_top_port, R.dimen.recents_task_view_top_land, R.dimen.recents_task_view_top_tablet_port, R.dimen.recents_task_view_top_tablet_land, R.dimen.recents_task_view_top_tablet_port, R.dimen.recents_task_view_top_tablet_land);
        this.mFreeformLayoutAlgorithm.reloadOnConfigurationChange(context);
        boolean z = true;
        if (resources.getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        this.mIsRtlLayout = z;
        this.mStatusbarHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
    }

    public void reset() {
        this.mTaskIndexOverrideMap.clear();
        setFocusState(getInitialFocusState());
    }

    public boolean setSystemInsets(Rect rect) {
        boolean z = !this.mSystemInsets.equals(rect);
        if (z) {
            this.mSystemInsets.set(rect);
        }
        return z;
    }

    public void setFocusState(int i) {
        int i2 = this.mFocusState;
        updateFrontBackTransforms();
        TaskStackLayoutAlgorithmCallbacks taskStackLayoutAlgorithmCallbacks = this.mCb;
        if (taskStackLayoutAlgorithmCallbacks != null) {
            taskStackLayoutAlgorithmCallbacks.onFocusStateChanged(i2, i);
        }
    }

    public int getFocusState() {
        return this.mFocusState;
    }

    public void initialize(Rect rect, Rect rect2, Rect rect3, StackState stackState) {
        Rect rect4 = rect2;
        this.mWindowRect = rect4;
        Rect rect5 = new Rect(this.mStackRect);
        Rect rect6 = rect2;
        Rect rect7 = rect;
        int scaleForExtent = getScaleForExtent(rect6, rect7, this.mBaseTopMargin, this.mMinMargin, 1);
        int scaleForExtent2 = getScaleForExtent(rect6, rect7, this.mBaseBottomMargin, this.mMinMargin, 1);
        this.mInitialTopOffset = 0;
        this.mInitialBottomOffset = 0;
        this.mState = stackState;
        int i = this.mSystemInsets.bottom + scaleForExtent2;
        this.mStackBottomOffset = i;
        stackState.computeRects(this.mFreeformRect, this.mStackRect, rect3, scaleForExtent, this.mFreeformStackGap, i);
        Rect rect8 = this.mStackActionButtonRect;
        Rect rect9 = this.mStackRect;
        int i2 = rect9.left;
        int i3 = rect9.top;
        rect8.set(i2, i3 - scaleForExtent, rect9.right, i3 + this.mFocusedTopPeekHeight);
        computeTaskRect(rect3, rect4, rect);
        if (!rect5.equals(this.mStackRect)) {
            updateFrontBackTransforms();
        }
    }

    private void computeTaskRect(Rect rect, Rect rect2, Rect rect3) {
        float f = this.mContext.getResources().getFloat(R.dimen.recents_task_rect_scale);
        RectF rectF = new RectF();
        rectF.set(rect);
        rectF.bottom -= (float) this.mSystemInsets.bottom;
        Utilities.scaleRectAboutCenter(rectF, f);
        Utilities.scaleRectAboutCenter(rectF, 1.0f - ((((float) this.mContext.getResources().getDimensionPixelSize(R.dimen.recents_task_view_padding)) * 1.0f) / rectF.width()));
        rectF.top -= (float) BaseRecentsImpl.mTaskBarHeight;
        rectF.offsetTo(rectF.left, (float) (rect.top + getScaleForExtent(rect, rect3, this.mTaskViewTop, 0, 1)));
        rectF.round(this.mTaskRect);
        this.mFirstTaskRect.set(this.mTaskRect);
        if (isDockedMode()) {
            this.mHorizontalGap = ((this.mStackRect.width() - (this.mTaskRect.width() * 2)) - this.mPaddingRight) / 3;
        } else {
            this.mHorizontalGap = (((this.mStackRect.width() - (this.mTaskRect.width() * 2)) - this.mPaddingLeft) - this.mPaddingRight) / 3;
        }
        this.mVerticalGap = BaseRecentsImpl.mTaskBarHeight;
        if (isLandscapeMode(this.mContext) || isDockedMode()) {
            this.mVerticalGap = (int) (((double) this.mVerticalGap) * 0.8d);
        }
    }

    private void computeTaskViewPadding(int i) {
        float f;
        float f2;
        int i2;
        if (i > 0) {
            if (isDockedMode()) {
                f2 = 0.2f;
                f = 0.15f;
            } else if (isLandscapeMode(this.mContext)) {
                f2 = 0.32f;
                f = 0.5f;
            } else {
                f2 = i <= 2 ? 0.62f : 0.55f;
                f = 0.4f;
            }
            this.mPaddingTop = (int) (((float) this.mTaskRect.height()) * f2);
            if (i <= 2) {
                i2 = 0;
            } else {
                i2 = (int) (((float) this.mTaskRect.height()) * f);
            }
            this.mPaddingBottom = i2;
        }
    }

    /* access modifiers changed from: package-private */
    public void update(TaskStack taskStack, ArraySet<Task.TaskKey> arraySet) {
        Recents.getSystemServices();
        Recents.getConfiguration().getLaunchState();
        this.mTaskIndexMap.clear();
        ArrayList<Task> stackTasks = taskStack.getStackTasks();
        if (stackTasks.isEmpty()) {
            this.mInitialScrollP = 0.0f;
            this.mMaxScrollP = 0.0f;
            this.mMinScrollP = 0.0f;
            this.mNumFreeformTasks = 0;
            this.mNumStackTasks = 0;
            return;
        }
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (int i = 0; i < stackTasks.size(); i++) {
            Task task = stackTasks.get(i);
            if (!arraySet.contains(task.key)) {
                if (task.isFreeformTask()) {
                    arrayList.add(task);
                } else {
                    arrayList2.add(task);
                }
            }
        }
        this.mNumStackTasks = arrayList2.size();
        this.mNumFreeformTasks = arrayList.size();
        int size = arrayList2.size();
        for (int i2 = 0; i2 < size; i2++) {
            this.mTaskIndexMap.put(((Task) arrayList2.get(i2)).key.id, i2);
        }
        if (!arrayList.isEmpty()) {
            this.mFreeformLayoutAlgorithm.update(arrayList, this);
        }
        computeTaskViewPadding(size);
        this.mMinScrollP = 0.0f;
        this.mInitialScrollP = 0.0f;
        int i3 = this.mNumStackTasks;
        if (i3 > 0) {
            Rect rect = this.mTaskRect;
            this.mMaxScrollP = Math.max(0.0f, ((((((float) calculateTaskViewXandY(i3 - 1, this.mTaskRect)[1]) + ((float) rect.top)) + ((float) rect.height())) - ((float) this.mWindowRect.bottom)) + ((float) this.mPaddingBottom)) / ((float) this.mTaskRect.height()));
            return;
        }
        this.mMaxScrollP = 0.0f;
    }

    public void setTaskOverridesForInitialState(TaskStack taskStack, boolean z) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        this.mTaskIndexOverrideMap.clear();
        boolean z2 = launchState.launchedFromHome || launchState.launchedViaDockGesture;
        if (getInitialFocusState() == 0 && this.mNumStackTasks > 1) {
            if (z || (!launchState.launchedWithAltTab && !z2)) {
                float normalizedXFromUnfocusedY = getNormalizedXFromUnfocusedY((float) (this.mSystemInsets.right + this.mInitialBottomOffset), 1);
                float[] fArr = this.mNumStackTasks <= 2 ? new float[]{Math.min(getNormalizedXFromUnfocusedY((float) ((this.mFocusedTopPeekHeight + this.mTaskRect.width()) - this.mMinMargin), 0), normalizedXFromUnfocusedY), getNormalizedXFromUnfocusedY((float) this.mFocusedTopPeekHeight, 0)} : new float[]{normalizedXFromUnfocusedY, getNormalizedXFromUnfocusedY((float) this.mInitialTopOffset, 0)};
                this.mUnfocusedRange.offset(0.0f);
                ArrayList<Task> stackTasks = taskStack.getStackTasks();
                int size = stackTasks.size();
                int i = size - 1;
                while (i >= 0) {
                    int i2 = (size - i) - 1;
                    if (i2 < fArr.length) {
                        this.mTaskIndexOverrideMap.put(stackTasks.get(i).key.id, Float.valueOf(this.mInitialScrollP + this.mUnfocusedRange.getAbsoluteX(fArr[i2])));
                        i--;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    public void addUnfocusedTaskOverride(Task task, float f) {
        if (this.mFocusState != 0) {
            this.mFocusedRange.offset(f);
            this.mUnfocusedRange.offset(f);
            float normalizedX = this.mFocusedRange.getNormalizedX((float) this.mTaskIndexMap.get(task.key.id));
            float x = this.mUnfocusedCurveInterpolator.getX(this.mFocusedCurveInterpolator.getInterpolation(normalizedX));
            float absoluteX = f + this.mUnfocusedRange.getAbsoluteX(x);
            if (Float.compare(normalizedX, x) != 0) {
                this.mTaskIndexOverrideMap.put(task.key.id, Float.valueOf(absoluteX));
            }
        }
    }

    public void addUnfocusedTaskOverride(TaskView taskView, float f) {
        this.mFocusedRange.offset(f);
        this.mUnfocusedRange.offset(f);
        Task task = taskView.getTask();
        float left = (float) (taskView.getLeft() - this.mTaskRect.left);
        float normalizedXFromFocusedY = getNormalizedXFromFocusedY(left, 0);
        float normalizedXFromUnfocusedY = getNormalizedXFromUnfocusedY(left, 0);
        float absoluteX = f + this.mUnfocusedRange.getAbsoluteX(normalizedXFromUnfocusedY);
        if (Float.compare(normalizedXFromFocusedY, normalizedXFromUnfocusedY) != 0) {
            this.mTaskIndexOverrideMap.put(task.key.id, Float.valueOf(absoluteX));
        }
    }

    public void clearUnfocusedTaskOverrides() {
        this.mTaskIndexOverrideMap.clear();
    }

    public float updateFocusStateOnScroll(float f, float f2, float f3) {
        if (f2 == f3) {
            return f2;
        }
        this.mUnfocusedRange.offset(f2);
        this.mTaskIndexOverrideMap.size();
        return f2;
    }

    public int getInitialFocusState() {
        return (Recents.getDebugFlags().isPagingEnabled() || Recents.getConfiguration().getLaunchState().launchedWithAltTab) ? 1 : 0;
    }

    public TaskViewTransform getFrontOfStackTransform() {
        return this.mFrontOfStackTransform;
    }

    public StackState getStackState() {
        return this.mState;
    }

    public boolean isInitialized() {
        return !this.mStackRect.isEmpty();
    }

    public VisibilityReport computeStackVisibilityReport(ArrayList<Task> arrayList) {
        int i;
        int i2;
        ArrayList<Task> arrayList2 = arrayList;
        int i3 = 1;
        if (arrayList.size() <= 1) {
            return new VisibilityReport(this, 1, 1);
        }
        if (this.mNumStackTasks == 0) {
            return new VisibilityReport(this, Math.max(this.mNumFreeformTasks, 1), Math.max(this.mNumFreeformTasks, 1));
        }
        TaskViewTransform taskViewTransform = new TaskViewTransform();
        Range range = ((float) getInitialFocusState()) > 0.0f ? this.mFocusedRange : this.mUnfocusedRange;
        range.offset(this.mInitialScrollP);
        int max = Math.max(this.mNumFreeformTasks, 1);
        int max2 = Math.max(this.mNumFreeformTasks, 1);
        float f = 2.14748365E9f;
        int i4 = 0;
        while (true) {
            if (i4 > arrayList.size() - i3) {
                i = max2;
                break;
            }
            Task task = arrayList2.get(i4);
            if (!task.isFreeformTask()) {
                float stackScrollForTask = getStackScrollForTask(task);
                if (range.isInRange(stackScrollForTask)) {
                    i2 = i4;
                    i = max2;
                    getStackTransform(stackScrollForTask, stackScrollForTask, this.mInitialScrollP, this.mFocusState, taskViewTransform, (TaskViewTransform) null, false, false);
                    float f2 = taskViewTransform.rect.left;
                    if (f2 - f > ((float) this.mTaskRect.width())) {
                        max2 = i + 1;
                        max++;
                        f = f2;
                        i4 = i2 + 1;
                        i3 = 1;
                    } else {
                        for (int i5 = i2; i5 >= 0; i5--) {
                            max++;
                            range.isInRange(getStackScrollForTask(arrayList2.get(i5)));
                        }
                    }
                }
            }
            i2 = i4;
            i4 = i2 + 1;
            i3 = 1;
        }
        return new VisibilityReport(this, max, i);
    }

    public TaskViewTransform getStackTransform(Task task, float f, TaskViewTransform taskViewTransform, TaskViewTransform taskViewTransform2) {
        getStackTransform(task, f, this.mFocusState, taskViewTransform, taskViewTransform2, false, false);
        return taskViewTransform;
    }

    public TaskViewTransform getStackTransform(Task task, float f, TaskViewTransform taskViewTransform, TaskViewTransform taskViewTransform2, boolean z) {
        getStackTransform(task, f, this.mFocusState, taskViewTransform, taskViewTransform2, false, z);
        return taskViewTransform;
    }

    public TaskViewTransform getStackTransform(Task task, float f, int i, TaskViewTransform taskViewTransform, TaskViewTransform taskViewTransform2, boolean z, boolean z2) {
        float f2;
        Task task2 = task;
        TaskViewTransform taskViewTransform3 = taskViewTransform;
        if (this.mFreeformLayoutAlgorithm.isTransformAvailable(task, this)) {
            this.mFreeformLayoutAlgorithm.getTransform(task, taskViewTransform, this);
            return taskViewTransform3;
        }
        int i2 = this.mTaskIndexMap.get(task2.key.id, -1);
        if (task2 == null || i2 == -1) {
            taskViewTransform.reset();
            return taskViewTransform3;
        }
        if (z2) {
            f2 = (float) i2;
        } else {
            f2 = getStackScrollForTask(task);
        }
        getStackTransform(f2, (float) i2, f, i, taskViewTransform, taskViewTransform2, false, z);
        return taskViewTransform3;
    }

    public TaskViewTransform getStackTransformScreenCoordinates(Task task, float f, TaskViewTransform taskViewTransform, TaskViewTransform taskViewTransform2, Rect rect) {
        getStackTransform(task, f, this.mFocusState, taskViewTransform, taskViewTransform2, true, false);
        transformToScreenCoordinates(taskViewTransform, rect);
        return taskViewTransform;
    }

    public TaskViewTransform transformToScreenCoordinates(TaskViewTransform taskViewTransform, Rect rect) {
        if (rect == null) {
            rect = Recents.getSystemServices().getWindowRect();
        }
        taskViewTransform.rect.offset((float) rect.left, (float) rect.top);
        return taskViewTransform;
    }

    public void getStackTransform(float f, float f2, float f3, int i, TaskViewTransform taskViewTransform, TaskViewTransform taskViewTransform2, boolean z, boolean z2) {
        int[] calculateTaskViewXandY = calculateTaskViewXandY((int) f, this.mTaskRect);
        boolean z3 = false;
        int i2 = calculateTaskViewXandY[0];
        int i3 = calculateTaskViewXandY[1];
        if (this.mNumStackTasks == 1) {
            i2 = 0;
        }
        int height = (int) (((float) i3) - (((float) this.mTaskRect.height()) * f3));
        float f4 = 1.0f;
        taskViewTransform.scale = 1.0f;
        if (f3 < 0.0f) {
            f4 = 1.0f - (this.mAccelerateInterpolator.getInterpolation(Math.abs(f3)) / 3.0f);
        }
        taskViewTransform.alpha = f4;
        taskViewTransform.translationZ = 0.0f;
        taskViewTransform.dimAlpha = 0.0f;
        taskViewTransform.viewOutlineAlpha = 0.0f;
        taskViewTransform.rect.set((f != 0.0f || this.mNumStackTasks <= 1) ? this.mTaskRect : this.mFirstTaskRect);
        taskViewTransform.rect.offset((float) i2, (float) height);
        Utilities.scaleRectAboutCenter(taskViewTransform.rect, taskViewTransform.scale);
        RectF rectF = taskViewTransform.rect;
        float f5 = rectF.bottom;
        Rect rect = this.mWindowRect;
        if (f5 > ((float) rect.top) && rectF.top < ((float) rect.bottom)) {
            z3 = true;
        }
        taskViewTransform.visible = z3;
    }

    /* access modifiers changed from: package-private */
    public int[] calculateTaskViewXandY(int i, Rect rect) {
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int[] iArr = new int[2];
        int i9 = this.mPaddingTop;
        if (isDockedMode()) {
            Rect rect2 = this.mStackRect;
            int i10 = rect2.left;
            int i11 = this.mHorizontalGap;
            Rect rect3 = this.mTaskRect;
            i3 = (i10 + i11) - rect3.left;
            i4 = (rect2.right - i11) - rect3.right;
            i2 = this.mPaddingRight;
        } else {
            Rect rect4 = this.mStackRect;
            int i12 = rect4.left;
            int i13 = this.mHorizontalGap;
            Rect rect5 = this.mTaskRect;
            i3 = ((i12 + i13) - rect5.left) + this.mPaddingLeft;
            i4 = (rect4.right - i13) - rect5.right;
            i2 = this.mPaddingRight;
        }
        int i14 = i4 - i2;
        int i15 = i9;
        for (int i16 = 0; i16 < i; i16++) {
            if (i16 == 0) {
                if (!this.mIsRtlLayout) {
                    i6 = i9 + this.mVerticalGap;
                    i5 = this.mFirstTaskRect.height();
                    i9 = i6 + i5;
                } else {
                    i8 = i15 + this.mVerticalGap;
                    i7 = this.mFirstTaskRect.height();
                }
            } else if (i9 > i15) {
                i8 = i15 + this.mVerticalGap;
                i7 = rect.height();
            } else {
                i6 = i9 + this.mVerticalGap;
                i5 = rect.height();
                i9 = i6 + i5;
            }
            i15 = i8 + i7;
        }
        if (i9 > i15 || (this.mIsRtlLayout && i == 0)) {
            iArr[0] = i14;
            iArr[1] = ((i15 + this.mVerticalGap) + this.mWindowRect.top) - rect.top;
        } else {
            iArr[0] = i3;
            iArr[1] = ((i9 + this.mVerticalGap) + this.mWindowRect.top) - rect.top;
        }
        return iArr;
    }

    public Rect getUntransformedTaskViewBounds() {
        return new Rect(this.mTaskRect);
    }

    /* access modifiers changed from: package-private */
    public float getStackScrollForTask(Task task) {
        Float f = this.mTaskIndexOverrideMap.get(task.key.id, (Object) null);
        return (float) this.mTaskIndexMap.get(task.key.id, 0);
    }

    /* access modifiers changed from: package-private */
    public float getTrueStackScrollForTask(Task task) {
        int i = this.mTaskIndexMap.get(task.key.id, 0);
        if (i == 0 || i == 1) {
            return 0.0f;
        }
        Rect rect = this.mTaskRect;
        return (((float) calculateTaskViewXandY(i, this.mTaskRect)[1]) + ((float) rect.top)) / ((float) rect.height());
    }

    public float getDeltaPForX(int i, int i2) {
        return -((((float) (i2 - i)) / ((float) this.mStackRect.height())) * this.mUnfocusedCurveInterpolator.getArcLength());
    }

    public int getXForDeltaP(float f, float f2) {
        return -((int) ((f2 - f) * ((float) this.mStackRect.height()) * (1.0f / this.mUnfocusedCurveInterpolator.getArcLength())));
    }

    public void getTaskStackBounds(Rect rect, Rect rect2, int i, int i2, int i3, Rect rect3) {
        rect3.set(rect2.left + i2, rect2.top + i, rect2.right - i3, rect2.bottom);
        rect3.inset((rect3.width() - (rect3.width() - (getScaleForExtent(rect2, rect, this.mBaseSideMargin, this.mMinMargin, 0) * 2))) / 2, 0);
    }

    public static int getDimensionForDevice(Context context, int i, int i2, int i3) {
        return getDimensionForDevice(context, i, i, i2, i2, i3, i3);
    }

    public static int getDimensionForDevice(Context context, int i, int i2, int i3, int i4, int i5, int i6) {
        RecentsConfiguration configuration = Recents.getConfiguration();
        Resources resources = context.getResources();
        boolean z = Utilities.getAppConfiguration(context).orientation == 2;
        if (configuration.isXLargeScreen) {
            if (z) {
                i5 = i6;
            }
            return resources.getDimensionPixelSize(i5);
        } else if (configuration.isLargeScreen) {
            if (z) {
                i3 = i4;
            }
            return resources.getDimensionPixelSize(i3);
        } else {
            if (z) {
                i = i2;
            }
            return resources.getDimensionPixelSize(i);
        }
    }

    private float getNormalizedXFromUnfocusedY(float f, int i) {
        if (i == 0) {
            f = ((float) this.mStackRect.width()) - f;
        }
        return this.mUnfocusedCurveInterpolator.getX(f / ((float) this.mStackRect.width()));
    }

    private float getNormalizedXFromFocusedY(float f, int i) {
        if (i == 0) {
            f = ((float) this.mStackRect.width()) - f;
        }
        return this.mFocusedCurveInterpolator.getX(f / ((float) this.mStackRect.width()));
    }

    private Path linearCurve() {
        Path path = new Path();
        path.moveTo(0.0f, -1.5f);
        path.lineTo(3.5f, 2.0f);
        return path;
    }

    private int getScaleForExtent(Rect rect, Rect rect2, int i, int i2, int i3) {
        if (i3 == 0) {
            return Math.max(i2, (int) (Utilities.clamp01(((float) rect.width()) / ((float) rect2.width())) * ((float) i)));
        }
        return i3 == 1 ? Math.max(i2, (int) (Utilities.clamp01(((float) rect.height()) / ((float) rect2.height())) * ((float) i))) : i;
    }

    private void updateFrontBackTransforms() {
        if (!this.mStackRect.isEmpty()) {
            float mapRange = Utilities.mapRange((float) this.mFocusState, this.mUnfocusedRange.relativeMin, this.mFocusedRange.relativeMin);
            float mapRange2 = Utilities.mapRange((float) this.mFocusState, this.mUnfocusedRange.relativeMax, this.mFocusedRange.relativeMax);
            getStackTransform(mapRange, mapRange, 1.0f, this.mFocusState, this.mFrontOfStackTransform, (TaskViewTransform) null, true, true);
            getStackTransform(mapRange2, mapRange2, -1.0f, this.mFocusState, this.mBackOfStackTransform, (TaskViewTransform) null, true, true);
            this.mBackOfStackTransform.visible = true;
            this.mFrontOfStackTransform.visible = true;
        }
    }

    public void dump(String str, PrintWriter printWriter) {
        String str2 = str + "  ";
        printWriter.print(str);
        printWriter.print("TaskStackLayoutAlgorithm");
        printWriter.write(" numStackTasks=");
        printWriter.write(this.mNumStackTasks);
        printWriter.println();
        printWriter.print(str2);
        printWriter.print("insets=");
        printWriter.print(Utilities.dumpRect(this.mSystemInsets));
        printWriter.print(" stack=");
        printWriter.print(Utilities.dumpRect(this.mStackRect));
        printWriter.print(" task=");
        printWriter.print(Utilities.dumpRect(this.mTaskRect));
        printWriter.print(" freeform=");
        printWriter.print(Utilities.dumpRect(this.mFreeformRect));
        printWriter.print(" actionButton=");
        printWriter.print(Utilities.dumpRect(this.mStackActionButtonRect));
        printWriter.println();
        printWriter.print(str2);
        printWriter.print("minScroll=");
        printWriter.print(this.mMinScrollP);
        printWriter.print(" maxScroll=");
        printWriter.print(this.mMaxScrollP);
        printWriter.print(" initialScroll=");
        printWriter.print(this.mInitialScrollP);
        printWriter.println();
        printWriter.print(str2);
        printWriter.print("focusState=");
        printWriter.print(this.mFocusState);
        printWriter.println();
        if (this.mTaskIndexOverrideMap.size() > 0) {
            for (int size = this.mTaskIndexOverrideMap.size() - 1; size >= 0; size--) {
                int keyAt = this.mTaskIndexOverrideMap.keyAt(size);
                float floatValue = this.mTaskIndexOverrideMap.get(keyAt, Float.valueOf(0.0f)).floatValue();
                printWriter.print(str2);
                printWriter.print("taskId= ");
                printWriter.print(keyAt);
                printWriter.print(" x= ");
                printWriter.print((float) this.mTaskIndexMap.get(keyAt));
                printWriter.print(" overrideX= ");
                printWriter.print(floatValue);
                printWriter.println();
            }
        }
    }

    public boolean isLandscapeMode(Context context) {
        return Utilities.getAppConfiguration(context).orientation == 2 && !isDockedMode();
    }

    public boolean isDockedMode() {
        boolean z;
        if (Utilities.isAndroidNorNewer()) {
            Divider divider = (Divider) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(Divider.class);
            z = divider != null ? divider.isExists() : false;
        } else {
            z = Recents.getSystemServices().hasDockedTask();
        }
        if (z || this.mDropToDockState) {
            return true;
        }
        return false;
    }

    public void updatePaddingOfNotch(int i) {
        if (Constants.IS_NOTCH && !Utilities.isAndroidPorNewer()) {
            int i2 = 0;
            this.mPaddingLeft = i == 1 ? this.mStatusbarHeight : 0;
            if (i == 3) {
                i2 = this.mStatusbarHeight;
            }
            this.mPaddingRight = i2;
        }
    }
}
