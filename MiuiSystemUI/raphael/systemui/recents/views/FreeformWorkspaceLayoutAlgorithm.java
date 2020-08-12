package com.android.systemui.recents.views;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.ArrayMap;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.model.Task;
import java.util.Collections;
import java.util.List;

public class FreeformWorkspaceLayoutAlgorithm {
    private int mTaskPadding;
    private ArrayMap<Task.TaskKey, RectF> mTaskRectMap = new ArrayMap<>();

    public FreeformWorkspaceLayoutAlgorithm(Context context) {
        reloadOnConfigurationChange(context);
    }

    public void reloadOnConfigurationChange(Context context) {
        this.mTaskPadding = context.getResources().getDimensionPixelSize(R.dimen.recents_freeform_layout_task_padding) / 2;
    }

    public void update(List<Task> list, TaskStackLayoutAlgorithm taskStackLayoutAlgorithm) {
        List<Task> list2 = list;
        TaskStackLayoutAlgorithm taskStackLayoutAlgorithm2 = taskStackLayoutAlgorithm;
        Collections.reverse(list);
        this.mTaskRectMap.clear();
        int i = taskStackLayoutAlgorithm2.mNumFreeformTasks;
        if (!list.isEmpty()) {
            float width = (float) taskStackLayoutAlgorithm2.mFreeformRect.width();
            float height = (float) taskStackLayoutAlgorithm2.mFreeformRect.height();
            float f = width / height;
            float[] fArr = new float[i];
            for (int i2 = 0; i2 < i; i2++) {
                Task task = list2.get(i2);
                Rect rect = task.bounds;
                fArr[i2] = Math.min(rect != null ? ((float) rect.width()) / ((float) task.bounds.height()) : f, f);
            }
            float f2 = 0.85f;
            float f3 = 0.0f;
            float f4 = 0.0f;
            int i3 = 1;
            int i4 = 0;
            while (i4 < i) {
                float f5 = fArr[i4] * f2;
                f4 += f5;
                if (f4 > f) {
                    i3++;
                    float f6 = (float) i3;
                    if (f6 * f2 > 1.0f) {
                        f2 = Math.min(f / f4, 1.0f / f6);
                        f4 = 0.0f;
                        i3 = 1;
                        i4 = 0;
                    } else {
                        i4++;
                        f4 = f5;
                    }
                } else {
                    i4++;
                }
                f3 = Math.max(f4, f3);
            }
            float f7 = ((1.0f - (f3 / f)) * width) / 2.0f;
            float f8 = ((1.0f - (((float) i3) * f2)) * height) / 2.0f;
            float f9 = f2 * height;
            float f10 = f7;
            int i5 = 0;
            while (i5 < i) {
                Task task2 = list2.get(i5);
                float f11 = fArr[i5] * f9;
                if (f10 + f11 > width) {
                    f8 += f9;
                    f10 = f7;
                }
                float f12 = f11 + f10;
                RectF rectF = new RectF(f10, f8, f12, f8 + f9);
                int i6 = this.mTaskPadding;
                rectF.inset((float) i6, (float) i6);
                this.mTaskRectMap.put(task2.key, rectF);
                i5++;
                f10 = f12;
            }
        }
    }

    public boolean isTransformAvailable(Task task, TaskStackLayoutAlgorithm taskStackLayoutAlgorithm) {
        if (taskStackLayoutAlgorithm.mNumFreeformTasks == 0 || task == null) {
            return false;
        }
        return this.mTaskRectMap.containsKey(task.key);
    }

    public TaskViewTransform getTransform(Task task, TaskViewTransform taskViewTransform, TaskStackLayoutAlgorithm taskStackLayoutAlgorithm) {
        if (!this.mTaskRectMap.containsKey(task.key)) {
            return null;
        }
        taskViewTransform.scale = 1.0f;
        taskViewTransform.alpha = 1.0f;
        taskViewTransform.translationZ = (float) taskStackLayoutAlgorithm.mMaxTranslationZ;
        taskViewTransform.dimAlpha = 0.0f;
        taskViewTransform.viewOutlineAlpha = 2.0f;
        taskViewTransform.rect.set(this.mTaskRectMap.get(task.key));
        RectF rectF = taskViewTransform.rect;
        Rect rect = taskStackLayoutAlgorithm.mFreeformRect;
        rectF.offset((float) rect.left, (float) rect.top);
        taskViewTransform.visible = true;
        return taskViewTransform;
    }
}
