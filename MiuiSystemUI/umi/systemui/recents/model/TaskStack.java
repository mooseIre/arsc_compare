package com.android.systemui.recents.model;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.IntProperty;
import android.util.SparseArray;
import android.view.animation.Interpolator;
import com.android.internal.policy.DockedDividerUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.AnimationProps;
import com.android.systemui.recents.views.DropTarget;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskStack {
    private Comparator<Task> FREEFORM_COMPARATOR = new Comparator<Task>(this) {
        public int compare(Task task, Task task2) {
            if (task.isFreeformTask() && !task2.isFreeformTask()) {
                return 1;
            }
            if (!task2.isFreeformTask() || task.isFreeformTask()) {
                return Long.compare((long) task.temporarySortIndexInStack, (long) task2.temporarySortIndexInStack);
            }
            return -1;
        }
    };
    TaskStackCallbacks mCb;
    ArrayList<Task> mRawTaskList = new ArrayList<>();
    FilteredTaskList mStackTaskList;

    public interface TaskStackCallbacks {
        void onStackTaskAdded(TaskStack taskStack, Task task);

        void onStackTaskRemoved(TaskStack taskStack, Task task, Task task2, AnimationProps animationProps, boolean z, boolean z2);

        void onStackTasksRemoved(TaskStack taskStack);

        void onStackTasksUpdated(TaskStack taskStack);
    }

    public static class DockState implements DropTarget {
        public static final DockState LEFT = new DockState(1, 0, 80, 0, 1, new RectF(0.0f, 0.0f, 0.125f, 1.0f), new RectF(0.0f, 0.0f, 0.125f, 1.0f), new RectF(0.0f, 0.0f, 0.5f, 1.0f));
        public static final DockState NONE = new DockState(-1, -1, 80, 255, 0, (RectF) null, (RectF) null, (RectF) null);
        public static final DockState TOP = new DockState(2, 0, 80, 0, 0, new RectF(0.0f, 0.0f, 1.0f, 0.125f), new RectF(0.0f, 0.0f, 1.0f, 0.125f), new RectF(0.0f, 0.0f, 1.0f, 0.5f));
        public static final DockState TOP_FORCE_BLACK = new DockState(2, 0, 80, 0, 0, new RectF(0.0f, 0.0f, 1.0f, 0.16f), new RectF(0.0f, 0.0f, 1.0f, 0.16f), new RectF(0.0f, 0.0f, 1.0f, 0.5f));
        public final int createMode;
        private final RectF dockArea;
        public final int dockSide;
        private final RectF expandedTouchDockArea;
        private final RectF touchArea;
        public final ViewState viewState;

        static {
            new DockState(3, 1, 80, 0, 1, new RectF(0.875f, 0.0f, 1.0f, 1.0f), new RectF(0.875f, 0.0f, 1.0f, 1.0f), new RectF(0.5f, 0.0f, 1.0f, 1.0f));
            new DockState(4, 1, 80, 0, 0, new RectF(0.0f, 0.875f, 1.0f, 1.0f), new RectF(0.0f, 0.875f, 1.0f, 1.0f), new RectF(0.0f, 0.5f, 1.0f, 1.0f));
        }

        public boolean acceptsDrop(int i, int i2, int i3, int i4, boolean z) {
            if (z) {
                return areaContainsPoint(this.expandedTouchDockArea, i3, i4, (float) i, (float) i2);
            }
            return areaContainsPoint(this.touchArea, i3, i4, (float) i, (float) i2);
        }

        public static class ViewState {
            private static final IntProperty<ViewState> HINT_ALPHA = new IntProperty<ViewState>("drawableAlpha") {
                public void setValue(ViewState viewState, int i) {
                    int unused = viewState.mHintTextAlpha = i;
                    viewState.dockAreaOverlay.invalidateSelf();
                }

                public Integer get(ViewState viewState) {
                    return Integer.valueOf(viewState.mHintTextAlpha);
                }
            };
            public final int dockAreaAlpha;
            public final ColorDrawable dockAreaOverlay;
            public final int hintTextAlpha;
            public final int hintTextOrientation;
            private AnimatorSet mDockAreaOverlayAnimator;
            private String mHintText;
            /* access modifiers changed from: private */
            public int mHintTextAlpha;
            private TextPaint mHintTextPaint;
            private final int mHintTextResId;
            private int mStatusBarHeight;
            private int mTextHintWidth;
            private Rect mTmpRect;

            private ViewState(int i, int i2, int i3, int i4) {
                this.mHintTextAlpha = 0;
                this.mTmpRect = new Rect();
                this.dockAreaAlpha = i;
                ColorDrawable colorDrawable = new ColorDrawable(-1);
                this.dockAreaOverlay = colorDrawable;
                colorDrawable.setAlpha(0);
                this.hintTextAlpha = i2;
                this.hintTextOrientation = i3;
                this.mHintTextResId = i4;
                TextPaint textPaint = new TextPaint(1);
                this.mHintTextPaint = textPaint;
                textPaint.setColor(-1);
                this.mHintTextPaint.setShadowLayer(3.0f, 2.0f, 2.0f, -16777216);
            }

            public void update(Context context) {
                Resources resources = context.getResources();
                this.dockAreaOverlay.setColor(resources.getColor(R.color.recents_dock_area_overlay));
                this.dockAreaOverlay.setAlpha(0);
                this.mHintTextPaint.setColor(resources.getColor(R.color.recents_dock_area_text_color));
                this.mHintTextPaint.setShadowLayer(3.0f, 2.0f, 2.0f, resources.getColor(R.color.recents_dock_area_text_shadow_color));
                this.mHintText = context.getString(this.mHintTextResId);
                this.mHintTextPaint.setTextSize((float) resources.getDimensionPixelSize(R.dimen.recents_drag_hint_text_size));
                TextPaint textPaint = this.mHintTextPaint;
                String str = this.mHintText;
                textPaint.getTextBounds(str, 0, str.length(), this.mTmpRect);
                this.mStatusBarHeight = resources.getDimensionPixelSize(R.dimen.status_bar_height);
                this.mTextHintWidth = resources.getDimensionPixelSize(R.dimen.dock_area_overlay_hint_text_margin);
            }

            public void draw(Canvas canvas) {
                if (this.dockAreaOverlay.getAlpha() > 0) {
                    this.dockAreaOverlay.draw(canvas);
                }
                if (this.mHintTextAlpha > 0) {
                    Rect bounds = this.dockAreaOverlay.getBounds();
                    int height = (this.hintTextOrientation == 1 ? bounds.height() : bounds.width()) - this.mTextHintWidth;
                    String str = this.mHintText;
                    StaticLayout build = StaticLayout.Builder.obtain(str, 0, str.length(), this.mHintTextPaint, height).setAlignment(Layout.Alignment.ALIGN_CENTER).build();
                    int width = bounds.left + ((bounds.width() - build.getWidth()) / 2);
                    int height2 = bounds.top + ((bounds.height() - build.getHeight()) / 2);
                    this.mHintTextPaint.setAlpha(this.mHintTextAlpha);
                    canvas.save();
                    if (this.hintTextOrientation == 1) {
                        canvas.rotate(-90.0f, (float) bounds.centerX(), (float) bounds.centerY());
                    } else if (RecentsActivity.isForceBlack()) {
                        height2 += this.mStatusBarHeight / 2;
                    }
                    canvas.translate((float) width, (float) height2);
                    build.draw(canvas);
                    canvas.restore();
                }
            }

            public void startAnimation(Rect rect, int i, int i2, int i3, Interpolator interpolator, boolean z, boolean z2) {
                Interpolator interpolator2;
                AnimatorSet animatorSet = this.mDockAreaOverlayAnimator;
                if (animatorSet != null) {
                    animatorSet.cancel();
                }
                ArrayList arrayList = new ArrayList();
                if (this.dockAreaOverlay.getAlpha() != i) {
                    if (z) {
                        ColorDrawable colorDrawable = this.dockAreaOverlay;
                        ObjectAnimator ofInt = ObjectAnimator.ofInt(colorDrawable, Utilities.DRAWABLE_ALPHA, new int[]{colorDrawable.getAlpha(), i});
                        ofInt.setDuration((long) i3);
                        ofInt.setInterpolator(interpolator);
                        arrayList.add(ofInt);
                    } else {
                        this.dockAreaOverlay.setAlpha(i);
                    }
                }
                int i4 = this.mHintTextAlpha;
                if (i4 != i2) {
                    if (z) {
                        ObjectAnimator ofInt2 = ObjectAnimator.ofInt(this, HINT_ALPHA, new int[]{i4, i2});
                        ofInt2.setDuration(150);
                        if (i2 > this.mHintTextAlpha) {
                            interpolator2 = Interpolators.ALPHA_IN;
                        } else {
                            interpolator2 = Interpolators.ALPHA_OUT;
                        }
                        ofInt2.setInterpolator(interpolator2);
                        arrayList.add(ofInt2);
                    } else {
                        this.mHintTextAlpha = i2;
                        this.dockAreaOverlay.invalidateSelf();
                    }
                }
                if (rect != null && !this.dockAreaOverlay.getBounds().equals(rect)) {
                    if (z2) {
                        PropertyValuesHolder ofObject = PropertyValuesHolder.ofObject(Utilities.DRAWABLE_RECT, Utilities.RECT_EVALUATOR, new Rect[]{new Rect(this.dockAreaOverlay.getBounds()), rect});
                        ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(this.dockAreaOverlay, new PropertyValuesHolder[]{ofObject});
                        ofPropertyValuesHolder.setDuration((long) i3);
                        ofPropertyValuesHolder.setInterpolator(interpolator);
                        arrayList.add(ofPropertyValuesHolder);
                    } else {
                        this.dockAreaOverlay.setBounds(rect);
                    }
                }
                if (!arrayList.isEmpty()) {
                    AnimatorSet animatorSet2 = new AnimatorSet();
                    this.mDockAreaOverlayAnimator = animatorSet2;
                    animatorSet2.playTogether(arrayList);
                    this.mDockAreaOverlayAnimator.start();
                }
            }
        }

        DockState(int i, int i2, int i3, int i4, int i5, RectF rectF, RectF rectF2, RectF rectF3) {
            this.dockSide = i;
            this.createMode = i2;
            this.viewState = new ViewState(i3, i4, i5, R.string.recents_drag_hint_message);
            this.dockArea = rectF2;
            this.touchArea = rectF;
            this.expandedTouchDockArea = rectF3;
        }

        public void update(Context context) {
            this.viewState.update(context);
        }

        public boolean areaContainsPoint(RectF rectF, int i, int i2, float f, float f2) {
            float f3 = (float) i;
            float f4 = (float) i2;
            return f >= ((float) ((int) (rectF.left * f3))) && f2 >= ((float) ((int) (rectF.top * f4))) && f <= ((float) ((int) (rectF.right * f3))) && f2 <= ((float) ((int) (rectF.bottom * f4)));
        }

        public Rect getPreDockedBounds(int i, int i2) {
            RectF rectF = this.dockArea;
            float f = (float) i;
            float f2 = (float) i2;
            return new Rect((int) (rectF.left * f), (int) (rectF.top * f2), (int) (rectF.right * f), (int) (rectF.bottom * f2));
        }

        public Rect getDockedBounds(int i, int i2, int i3, Rect rect, Resources resources) {
            boolean z = true;
            if (resources.getConfiguration().orientation != 1) {
                z = false;
            }
            int calculateMiddlePosition = DockedDividerUtils.calculateMiddlePosition(z, rect, i, i2, i3);
            Rect rect2 = new Rect();
            DockedDividerUtils.calculateBoundsForPosition(calculateMiddlePosition, this.dockSide, rect2, i, i2, i3);
            return rect2;
        }

        public Rect getDockedTaskStackBounds(Rect rect, int i, int i2, int i3, Rect rect2, TaskStackLayoutAlgorithm taskStackLayoutAlgorithm, Resources resources, Rect rect3) {
            int i4;
            int i5;
            int i6;
            Rect rect4 = rect2;
            int i7 = 0;
            boolean z = true;
            if (resources.getConfiguration().orientation == 1) {
                i6 = i;
                i5 = i2;
                i4 = i3;
            } else {
                i6 = i;
                i5 = i2;
                i4 = i3;
                z = false;
            }
            DockedDividerUtils.calculateBoundsForPosition(DockedDividerUtils.calculateMiddlePosition(z, rect4, i6, i5, i4), DockedDividerUtils.invertDockSide(this.dockSide), rect3, i, i2, i3);
            Rect rect5 = new Rect();
            if (this.dockArea.bottom >= 1.0f) {
                i7 = rect4.top;
            }
            taskStackLayoutAlgorithm.getTaskStackBounds(rect, rect3, i7, rect4.left, rect4.right, rect5);
            return rect5;
        }
    }

    public TaskStack() {
        FilteredTaskList filteredTaskList = new FilteredTaskList();
        this.mStackTaskList = filteredTaskList;
        filteredTaskList.setFilter(new TaskFilter(this) {
            public boolean acceptTask(SparseArray<Task> sparseArray, Task task, int i) {
                return task.isStackTask;
            }
        });
    }

    public void setCallbacks(TaskStackCallbacks taskStackCallbacks) {
        this.mCb = taskStackCallbacks;
    }

    public void moveTaskToStack(Task task, int i) {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int size = tasks.size();
        if (!task.isFreeformTask() && i == 2) {
            this.mStackTaskList.moveTaskToStack(task, size, i);
        } else if (task.isFreeformTask() && i == 1) {
            int i2 = 0;
            int i3 = size - 1;
            while (true) {
                if (i3 < 0) {
                    break;
                } else if (!tasks.get(i3).isFreeformTask()) {
                    i2 = i3 + 1;
                    break;
                } else {
                    i3--;
                }
            }
            this.mStackTaskList.moveTaskToStack(task, i2, i);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeTaskImpl(FilteredTaskList filteredTaskList, Task task) {
        filteredTaskList.remove(task);
    }

    public void removeTask(Task task, AnimationProps animationProps, boolean z) {
        if (this.mStackTaskList.contains(task)) {
            removeTaskImpl(this.mStackTaskList, task);
            Task stackFrontMostTask = getStackFrontMostTask(false);
            TaskStackCallbacks taskStackCallbacks = this.mCb;
            if (taskStackCallbacks != null) {
                taskStackCallbacks.onStackTaskRemoved(this, task, stackFrontMostTask, animationProps, z, true);
            }
        }
        this.mRawTaskList.remove(task);
    }

    public void removeAllTasks() {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        for (int size = tasks.size() - 1; size >= 0; size--) {
            Task task = tasks.get(size);
            if (!task.isProtected()) {
                removeTaskImpl(this.mStackTaskList, task);
                this.mRawTaskList.remove(task);
            }
        }
        TaskStackCallbacks taskStackCallbacks = this.mCb;
        if (taskStackCallbacks != null) {
            taskStackCallbacks.onStackTasksRemoved(this);
        }
    }

    public void setTasks(Context context, List<Task> list, boolean z) {
        List<Task> list2 = list;
        ArrayMap<Task.TaskKey, Task> createTaskKeyMapFromList = createTaskKeyMapFromList(this.mRawTaskList);
        ArrayMap<Task.TaskKey, Task> createTaskKeyMapFromList2 = createTaskKeyMapFromList(list2);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ArrayList<Task> arrayList3 = new ArrayList<>();
        boolean z2 = this.mCb == null ? false : z;
        for (int size = this.mRawTaskList.size() - 1; size >= 0; size--) {
            Task task = this.mRawTaskList.get(size);
            if (!createTaskKeyMapFromList2.containsKey(task.key) && z2) {
                arrayList2.add(task);
            }
        }
        int size2 = list.size();
        for (int i = 0; i < size2; i++) {
            Task task2 = list2.get(i);
            Task task3 = createTaskKeyMapFromList.get(task2.key);
            if (task3 == null && z2) {
                arrayList.add(task2);
            } else if (task3 != null) {
                task3.copyFrom(task2);
                task2 = task3;
            }
            arrayList3.add(task2);
        }
        for (int size3 = arrayList3.size() - 1; size3 >= 0; size3--) {
            arrayList3.get(size3).temporarySortIndexInStack = size3;
        }
        Collections.sort(arrayList3, this.FREEFORM_COMPARATOR);
        this.mStackTaskList.set(arrayList3);
        this.mRawTaskList = arrayList3;
        int size4 = arrayList2.size();
        Task stackFrontMostTask = getStackFrontMostTask(false);
        for (int i2 = 0; i2 < size4; i2++) {
            this.mCb.onStackTaskRemoved(this, (Task) arrayList2.get(i2), stackFrontMostTask, AnimationProps.IMMEDIATE, false, false);
        }
        int size5 = arrayList.size();
        for (int i3 = 0; i3 < size5; i3++) {
            this.mCb.onStackTaskAdded(this, (Task) arrayList.get(i3));
        }
        if (z2) {
            this.mCb.onStackTasksUpdated(this);
        }
    }

    public Task getStackFrontMostTask(boolean z) {
        return getStackFirstTask(z);
    }

    public Task getStackFirstTask(boolean z) {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        if (tasks.isEmpty()) {
            return null;
        }
        for (int i = 0; i <= tasks.size() - 1; i++) {
            Task task = tasks.get(i);
            if (!task.isFreeformTask() || z) {
                return task;
            }
        }
        return null;
    }

    public ArrayList<Task.TaskKey> getTaskKeys() {
        ArrayList<Task.TaskKey> arrayList = new ArrayList<>();
        ArrayList<Task> computeAllTasksList = computeAllTasksList();
        int size = computeAllTasksList.size();
        for (int i = 0; i < size; i++) {
            arrayList.add(computeAllTasksList.get(i).key);
        }
        return arrayList;
    }

    public ArrayList<Task> getStackTasks() {
        return this.mStackTaskList.getTasks();
    }

    public ArrayList<Task> getFreeformTasks() {
        ArrayList<Task> arrayList = new ArrayList<>();
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int size = tasks.size();
        for (int i = 0; i < size; i++) {
            Task task = tasks.get(i);
            if (task.isFreeformTask()) {
                arrayList.add(task);
            }
        }
        return arrayList;
    }

    public ArrayList<Task> computeAllTasksList() {
        ArrayList<Task> arrayList = new ArrayList<>();
        arrayList.addAll(this.mStackTaskList.getTasks());
        return arrayList;
    }

    public int getTaskCount() {
        return this.mStackTaskList.size();
    }

    public int getStackTaskCount() {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int size = tasks.size();
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            if (!tasks.get(i2).isFreeformTask()) {
                i++;
            }
        }
        return i;
    }

    public int getFreeformTaskCount() {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int size = tasks.size();
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            if (tasks.get(i2).isFreeformTask()) {
                i++;
            }
        }
        return i;
    }

    public Task getLaunchTarget() {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int size = tasks.size();
        for (int i = 0; i < size; i++) {
            Task task = tasks.get(i);
            if (task.isLaunchTarget) {
                return task;
            }
        }
        return null;
    }

    public int indexOfStackTask(Task task) {
        return this.mStackTaskList.indexOf(task);
    }

    public ArraySet<ComponentName> computeComponentsRemoved(String str, int i) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        ArraySet arraySet = new ArraySet();
        ArraySet<ComponentName> arraySet2 = new ArraySet<>();
        ArrayList<Task.TaskKey> taskKeys = getTaskKeys();
        int size = taskKeys.size();
        for (int i2 = 0; i2 < size; i2++) {
            Task.TaskKey taskKey = taskKeys.get(i2);
            if (taskKey.userId == i) {
                ComponentName component = taskKey.getComponent();
                if (component.getPackageName().equals(str) && !arraySet.contains(component)) {
                    if (systemServices.getActivityInfo(component, i) != null) {
                        arraySet.add(component);
                    } else {
                        arraySet2.add(component);
                    }
                }
            }
        }
        return arraySet2;
    }

    public String toString() {
        String str = "Stack Tasks (" + this.mStackTaskList.size() + "):\n";
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            str = str + "    " + tasks.get(i).toString() + "\n";
        }
        return str;
    }

    private ArrayMap<Task.TaskKey, Task> createTaskKeyMapFromList(List<Task> list) {
        ArrayMap<Task.TaskKey, Task> arrayMap = new ArrayMap<>(list.size());
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Task task = list.get(i);
            arrayMap.put(task.key, task);
        }
        return arrayMap;
    }

    public void dump(String str, PrintWriter printWriter) {
        String str2 = str + "  ";
        printWriter.print(str);
        printWriter.print("TaskStack");
        printWriter.print(" numStackTasks=");
        printWriter.print(this.mStackTaskList.size());
        printWriter.println();
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int size = tasks.size();
        for (int i = 0; i < size; i++) {
            tasks.get(i).dump(str2, printWriter);
        }
    }
}
