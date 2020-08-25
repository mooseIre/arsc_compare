package com.android.systemui.stackdivider;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.view.DisplayInfo;
import com.android.systemui.plugins.R;
import java.util.ArrayList;

public class DividerSnapAlgorithm {
    private final int mDisplayHeight;
    private final int mDisplayWidth;
    private final int mDividerSize;
    private final float mFixedRatio;
    private final Rect mInsets;
    private boolean mIsHorizontalDivision;
    private final int mMinimalSizeResizableTask;
    private final int mSnapMode;
    private final ArrayList<SnapTarget> mTargets;
    private final int mTaskHeightInMinimizedMode;

    public static DividerSnapAlgorithm create(Context context, Rect rect) {
        DisplayInfo displayInfo = new DisplayInfo();
        ((DisplayManager) context.getSystemService(DisplayManager.class)).getDisplay(0).getDisplayInfo(displayInfo);
        return new DividerSnapAlgorithm(context.getResources(), displayInfo.logicalWidth, displayInfo.logicalHeight, context.getResources().getDimensionPixelSize(R.dimen.docked_stack_divider_thickness) - (context.getResources().getDimensionPixelSize(R.dimen.docked_stack_divider_insets) * 2), context.getApplicationContext().getResources().getConfiguration().orientation == 1, rect);
    }

    public DividerSnapAlgorithm(Resources resources, int i, int i2, int i3, boolean z, Rect rect) {
        this(resources, i, i2, i3, z, rect, -1, false);
    }

    public DividerSnapAlgorithm(Resources resources, int i, int i2, int i3, boolean z, Rect rect, int i4, boolean z2) {
        int i5;
        this.mTargets = new ArrayList<>();
        this.mInsets = new Rect();
        float f = resources.getDisplayMetrics().density;
        float f2 = resources.getDisplayMetrics().density;
        this.mDividerSize = i3;
        this.mDisplayWidth = i;
        this.mDisplayHeight = i2;
        this.mIsHorizontalDivision = z;
        this.mInsets.set(rect);
        if (z2) {
            i5 = 3;
        } else {
            i5 = resources.getInteger(R.integer.config_dockedStackDividerSnapMode);
        }
        this.mSnapMode = i5;
        this.mFixedRatio = resources.getFraction(R.fraction.docked_stack_divider_fixed_ratio, 1, 1);
        this.mMinimalSizeResizableTask = resources.getDimensionPixelSize(R.dimen.default_minimal_size_resizable_task);
        this.mTaskHeightInMinimizedMode = resources.getDimensionPixelSize(R.dimen.task_height_of_minimized_mode);
        calculateTargets(z, i4);
        SnapTarget snapTarget = this.mTargets.get(1);
        ArrayList<SnapTarget> arrayList = this.mTargets;
        SnapTarget snapTarget2 = arrayList.get(arrayList.size() - 2);
        SnapTarget snapTarget3 = this.mTargets.get(0);
        ArrayList<SnapTarget> arrayList2 = this.mTargets;
        SnapTarget snapTarget4 = arrayList2.get(arrayList2.size() - 1);
        ArrayList<SnapTarget> arrayList3 = this.mTargets;
        SnapTarget snapTarget5 = arrayList3.get(arrayList3.size() / 2);
    }

    public boolean isSplitScreenFeasible() {
        int i;
        Rect rect = this.mInsets;
        int i2 = rect.top;
        int i3 = this.mIsHorizontalDivision ? rect.bottom : rect.right;
        if (this.mIsHorizontalDivision) {
            i = this.mDisplayHeight;
        } else {
            i = this.mDisplayWidth;
        }
        return (((i - i3) - i2) - this.mDividerSize) / 2 >= this.mMinimalSizeResizableTask;
    }

    private void calculateTargets(boolean z, int i) {
        int i2;
        this.mTargets.clear();
        if (z) {
            i2 = this.mDisplayHeight;
        } else {
            i2 = this.mDisplayWidth;
        }
        ArrayList<SnapTarget> arrayList = this.mTargets;
        int i3 = this.mDividerSize;
        arrayList.add(new SnapTarget(-i3, -i3, 1, 0.35f));
        int i4 = this.mSnapMode;
        if (i4 == 0) {
            addRatio16_9Targets(z, i2);
        } else if (i4 == 1) {
            addFixedDivisionTargets(z, i2);
        } else if (i4 == 2) {
            addMiddleTarget(z);
        } else if (i4 == 3) {
            addMinimizedTarget(z, i);
        }
        this.mTargets.add(new SnapTarget(i2 - (z ? this.mInsets.bottom : this.mInsets.right), i2, 2, 0.35f));
    }

    private void addNonDismissingTargets(boolean z, int i, int i2, int i3) {
        maybeAddTarget(i, i - this.mInsets.top);
        addMiddleTarget(z);
        maybeAddTarget(i2, (i3 - this.mInsets.bottom) - (this.mDividerSize + i2));
    }

    private void addFixedDivisionTargets(boolean z, int i) {
        int i2;
        int i3;
        Rect rect = this.mInsets;
        int i4 = z ? rect.top : rect.left;
        if (z) {
            i3 = this.mDisplayHeight;
            i2 = this.mInsets.bottom;
        } else {
            i3 = this.mDisplayWidth;
            i2 = this.mInsets.right;
        }
        int i5 = i3 - i2;
        int i6 = this.mDividerSize;
        int i7 = ((int) (this.mFixedRatio * ((float) (i5 - i4)))) - (i6 / 2);
        addNonDismissingTargets(z, i4 + i7, (i5 - i7) - i6, i);
    }

    private void addRatio16_9Targets(boolean z, int i) {
        int i2;
        int i3;
        int i4;
        int i5;
        Rect rect = this.mInsets;
        int i6 = z ? rect.top : rect.left;
        if (z) {
            i3 = this.mDisplayHeight;
            i2 = this.mInsets.bottom;
        } else {
            i3 = this.mDisplayWidth;
            i2 = this.mInsets.right;
        }
        int i7 = i3 - i2;
        Rect rect2 = this.mInsets;
        int i8 = z ? rect2.left : rect2.top;
        if (z) {
            i5 = this.mDisplayWidth;
            i4 = this.mInsets.right;
        } else {
            i5 = this.mDisplayHeight;
            i4 = this.mInsets.bottom;
        }
        int floor = (int) Math.floor((double) (((float) ((i5 - i4) - i8)) * 0.5625f));
        addNonDismissingTargets(z, i6 + floor, (i7 - floor) - this.mDividerSize, i);
    }

    private void maybeAddTarget(int i, int i2) {
        if (i2 >= this.mMinimalSizeResizableTask) {
            this.mTargets.add(new SnapTarget(i, i, 0));
        }
    }

    private void addMiddleTarget(boolean z) {
        int calculateMiddlePosition = DockedDividerUtils.calculateMiddlePosition(z, this.mInsets, this.mDisplayWidth, this.mDisplayHeight, this.mDividerSize);
        this.mTargets.add(new SnapTarget(calculateMiddlePosition, calculateMiddlePosition, 0));
    }

    private void addMinimizedTarget(boolean z, int i) {
        int i2 = this.mTaskHeightInMinimizedMode;
        Rect rect = this.mInsets;
        int i3 = i2 + rect.top;
        if (!z) {
            if (i == 1) {
                i3 += rect.left;
            } else if (i == 3) {
                i3 = ((this.mDisplayWidth - i3) - rect.right) - this.mDividerSize;
            }
        }
        this.mTargets.add(new SnapTarget(i3, i3, 0));
    }

    public static class SnapTarget {
        private final float distanceMultiplier;
        public int position;
        public int taskPosition;

        public SnapTarget(int i, int i2, int i3) {
            this(i, i2, i3, 1.0f);
        }

        public SnapTarget(int i, int i2, int i3, float f) {
            this.position = i;
            this.taskPosition = i2;
            this.distanceMultiplier = f;
        }

        public String toString() {
            return "position " + this.position + " taskPosition " + this.taskPosition + " distanceMultiplier " + this.distanceMultiplier;
        }
    }
}
