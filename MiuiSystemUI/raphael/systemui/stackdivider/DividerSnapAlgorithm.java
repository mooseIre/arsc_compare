package com.android.systemui.stackdivider;

import android.content.res.Resources;
import android.graphics.Rect;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0014R$fraction;
import com.android.systemui.C0016R$integer;
import java.util.ArrayList;

public class DividerSnapAlgorithm {
    private final SnapTarget mDismissEndTarget;
    private final SnapTarget mDismissStartTarget;
    private final int mDisplayHeight;
    private final int mDisplayWidth;
    private final int mDividerSize;
    private final SnapTarget mFirstSplitTarget;
    private final float mFixedRatio;
    private final boolean mFreeSnapMode;
    private final Rect mInsets;
    private boolean mIsHorizontalDivision;
    private final SnapTarget mLastSplitTarget;
    private final SnapTarget mMiddleTarget;
    private final float mMinDismissVelocityPxPerSecond;
    private final float mMinFlingVelocityPxPerSecond;
    private final int mMinimalSizeResizableTask;
    private final int mSnapMode;
    private final ArrayList<SnapTarget> mTargets;
    private final int mTaskHeightInMinimizedMode;
    private boolean mWideScreen;

    public DividerSnapAlgorithm(Resources resources, int i, int i2, int i3, boolean z, Rect rect) {
        this(resources, i, i2, i3, z, rect, -1, false, true);
    }

    public DividerSnapAlgorithm(Resources resources, int i, int i2, int i3, boolean z, Rect rect, int i4) {
        this(resources, i, i2, i3, z, rect, i4, false, true);
    }

    public DividerSnapAlgorithm(Resources resources, int i, int i2, int i3, boolean z, Rect rect, int i4, boolean z2, boolean z3) {
        int i5;
        this.mTargets = new ArrayList<>();
        this.mInsets = new Rect();
        this.mMinFlingVelocityPxPerSecond = resources.getDisplayMetrics().density * 400.0f;
        this.mMinDismissVelocityPxPerSecond = resources.getDisplayMetrics().density * 600.0f;
        this.mDividerSize = i3;
        this.mDisplayWidth = i;
        this.mDisplayHeight = i2;
        if (StackDividerUtils.isWideScreen(resources.getConfiguration())) {
            this.mWideScreen = true;
        }
        this.mIsHorizontalDivision = this.mWideScreen ? false : z;
        this.mInsets.set(rect);
        if (z2) {
            i5 = 3;
        } else {
            i5 = resources.getInteger(C0016R$integer.config_dockedStackDividerSnapMode);
        }
        this.mSnapMode = i5;
        this.mFreeSnapMode = resources.getBoolean(C0010R$bool.config_dockedStackDividerFreeSnapMode);
        this.mFixedRatio = resources.getFraction(C0014R$fraction.docked_stack_divider_fixed_ratio, 1, 1);
        this.mMinimalSizeResizableTask = resources.getDimensionPixelSize(C0012R$dimen.default_minimal_size_resizable_task);
        this.mTaskHeightInMinimizedMode = z3 ? resources.getDimensionPixelSize(C0012R$dimen.task_height_of_minimized_mode) : 0;
        calculateTargets(this.mIsHorizontalDivision, i4);
        this.mFirstSplitTarget = this.mTargets.get(1);
        ArrayList<SnapTarget> arrayList = this.mTargets;
        this.mLastSplitTarget = arrayList.get(arrayList.size() - 2);
        this.mDismissStartTarget = this.mTargets.get(0);
        ArrayList<SnapTarget> arrayList2 = this.mTargets;
        this.mDismissEndTarget = arrayList2.get(arrayList2.size() - 1);
        ArrayList<SnapTarget> arrayList3 = this.mTargets;
        SnapTarget snapTarget = arrayList3.get(arrayList3.size() / 2);
        this.mMiddleTarget = snapTarget;
        snapTarget.isMiddleTarget = true;
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

    public SnapTarget calculateSnapTarget(int i, float f) {
        return calculateSnapTarget(i, f, true);
    }

    public SnapTarget calculateSnapTarget(int i, float f, boolean z) {
        if (i < this.mFirstSplitTarget.position && f < (-this.mMinDismissVelocityPxPerSecond)) {
            return this.mDismissStartTarget;
        }
        if (i > this.mLastSplitTarget.position && f > this.mMinDismissVelocityPxPerSecond) {
            return this.mDismissEndTarget;
        }
        if (Math.abs(f) < this.mMinFlingVelocityPxPerSecond) {
            return snap(i, z);
        }
        if (f < 0.0f) {
            return this.mFirstSplitTarget;
        }
        return this.mLastSplitTarget;
    }

    public SnapTarget calculateNonDismissingSnapTarget(int i) {
        SnapTarget snap = snap(i, false);
        if (snap == this.mDismissStartTarget) {
            return this.mFirstSplitTarget;
        }
        return snap == this.mDismissEndTarget ? this.mLastSplitTarget : snap;
    }

    public float calculateDismissingFraction(int i) {
        if (i < this.mFirstSplitTarget.position) {
            return 1.0f - (((float) (i - getStartInset())) / ((float) (this.mFirstSplitTarget.position - getStartInset())));
        }
        int i2 = this.mLastSplitTarget.position;
        if (i > i2) {
            return ((float) (i - i2)) / ((float) ((this.mDismissEndTarget.position - i2) - this.mDividerSize));
        }
        return 0.0f;
    }

    public SnapTarget getClosestDismissTarget(int i) {
        if (i < this.mFirstSplitTarget.position) {
            return this.mDismissStartTarget;
        }
        if (i > this.mLastSplitTarget.position) {
            return this.mDismissEndTarget;
        }
        SnapTarget snapTarget = this.mDismissStartTarget;
        int i2 = i - snapTarget.position;
        SnapTarget snapTarget2 = this.mDismissEndTarget;
        return i2 < snapTarget2.position - i ? snapTarget : snapTarget2;
    }

    public SnapTarget getFirstSplitTarget() {
        return this.mFirstSplitTarget;
    }

    public SnapTarget getLastSplitTarget() {
        return this.mLastSplitTarget;
    }

    public SnapTarget getDismissStartTarget() {
        return this.mDismissStartTarget;
    }

    public SnapTarget getDismissEndTarget() {
        return this.mDismissEndTarget;
    }

    private int getStartInset() {
        if (this.mIsHorizontalDivision) {
            return this.mInsets.top;
        }
        return this.mInsets.left;
    }

    private int getEndInset() {
        if (this.mIsHorizontalDivision) {
            return this.mInsets.bottom;
        }
        return this.mInsets.right;
    }

    private boolean shouldApplyFreeSnapMode(int i) {
        if (this.mFreeSnapMode && isFirstSplitTargetAvailable() && isLastSplitTargetAvailable() && this.mFirstSplitTarget.position < i && i < this.mLastSplitTarget.position) {
            return true;
        }
        return false;
    }

    private SnapTarget snap(int i, boolean z) {
        if (shouldApplyFreeSnapMode(i)) {
            return new SnapTarget(i, i, 0);
        }
        int i2 = -1;
        float f = Float.MAX_VALUE;
        int size = this.mTargets.size();
        for (int i3 = 0; i3 < size; i3++) {
            SnapTarget snapTarget = this.mTargets.get(i3);
            float abs = (float) Math.abs(i - snapTarget.position);
            if (z) {
                abs /= snapTarget.distanceMultiplier;
            }
            if (abs < f) {
                i2 = i3;
                f = abs;
            }
        }
        return this.mTargets.get(i2);
    }

    private void calculateTargets(boolean z, int i) {
        int i2;
        this.mTargets.clear();
        if (z) {
            i2 = this.mDisplayHeight;
        } else {
            i2 = this.mDisplayWidth;
        }
        Rect rect = this.mInsets;
        int i3 = z ? rect.bottom : rect.right;
        int i4 = -this.mDividerSize;
        if (i == 3) {
            i4 += this.mInsets.left;
        }
        this.mTargets.add(new SnapTarget(i4, i4, 1, 0.35f));
        int i5 = this.mSnapMode;
        if (i5 != 0) {
            if (i5 != 1) {
                if (i5 == 2) {
                    addMiddleTarget(z);
                } else if (i5 == 3) {
                    addMinimizedTarget(z, i);
                }
            } else if (this.mWideScreen) {
                addFixedRatioTargetForWideScreen(z);
            } else {
                addFixedDivisionTargets(z, i2);
            }
        } else if (this.mWideScreen) {
            addRatio16_9TargetsForWideScreen(z, i2);
        } else {
            addRatio16_9Targets(z, i2);
        }
        this.mTargets.add(new SnapTarget(i2 - i3, i2, 2, 0.35f));
    }

    private void addNonDismissingTargets(boolean z, int i, int i2, int i3) {
        maybeAddTarget(i, i - getStartInset());
        addMiddleTarget(z);
        maybeAddTarget(i2, (i3 - getEndInset()) - (this.mDividerSize + i2));
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

    private void addRatio16_9TargetsForWideScreen(boolean z, int i) {
        int i2;
        int i3;
        int i4;
        int i5;
        if (z) {
            int i6 = this.mDisplayHeight;
            Rect rect = this.mInsets;
            i3 = i6 - rect.bottom;
            i2 = rect.top;
        } else {
            int i7 = this.mDisplayWidth;
            Rect rect2 = this.mInsets;
            i3 = i7 - rect2.right;
            i2 = rect2.left;
        }
        int i8 = i3 - i2;
        if (z) {
            int i9 = this.mDisplayHeight;
            Rect rect3 = this.mInsets;
            i5 = i9 - rect3.bottom;
            i4 = rect3.top;
        } else {
            int i10 = this.mDisplayWidth;
            Rect rect4 = this.mInsets;
            i5 = i10 - rect4.right;
            i4 = rect4.left;
        }
        int floor = (int) Math.floor((double) (((float) (((i5 - i4) + 0) - this.mDividerSize)) * 0.64f));
        addNonDismissingTargets(z, (i8 - floor) - this.mDividerSize, floor + 0, i);
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

    private void addFixedRatioTargetForWideScreen(boolean z) {
        int i;
        int i2;
        if (z) {
            int i3 = this.mDisplayHeight;
            Rect rect = this.mInsets;
            i2 = i3 - rect.top;
            i = rect.bottom;
        } else {
            int i4 = this.mDisplayWidth;
            Rect rect2 = this.mInsets;
            i2 = i4 - rect2.right;
            i = rect2.left;
        }
        int ceil = (int) Math.ceil((double) ((((float) (i2 - i)) - ((float) this.mDividerSize)) * 0.64f));
        addMiddleTarget(z);
        this.mTargets.add(new SnapTarget(ceil, ceil, 0));
    }

    private void addMinimizedTarget(boolean z, int i) {
        int i2 = this.mTaskHeightInMinimizedMode;
        int i3 = this.mDividerSize;
        int i4 = i2 + i3;
        if (!z) {
            if (i == 1) {
                i4 += this.mInsets.left;
            } else if (i == 3) {
                i4 = ((this.mDisplayWidth - i4) - this.mInsets.right) - i3;
            }
        }
        this.mTargets.add(new SnapTarget(i4, i4, 0));
    }

    public SnapTarget getMiddleTarget() {
        return this.mMiddleTarget;
    }

    public SnapTarget getNextTarget(SnapTarget snapTarget) {
        int indexOf = this.mTargets.indexOf(snapTarget);
        return (indexOf == -1 || indexOf >= this.mTargets.size() + -1) ? snapTarget : this.mTargets.get(indexOf + 1);
    }

    public SnapTarget getPreviousTarget(SnapTarget snapTarget) {
        int indexOf = this.mTargets.indexOf(snapTarget);
        return (indexOf == -1 || indexOf <= 0) ? snapTarget : this.mTargets.get(indexOf - 1);
    }

    public boolean showMiddleSplitTargetForAccessibility() {
        return this.mTargets.size() + -2 > 1;
    }

    public boolean isFirstSplitTargetAvailable() {
        return this.mFirstSplitTarget != this.mMiddleTarget;
    }

    public boolean isLastSplitTargetAvailable() {
        return this.mLastSplitTarget != this.mMiddleTarget;
    }

    public static class SnapTarget {
        private final float distanceMultiplier;
        public final int flag;
        public boolean isMiddleTarget;
        public final int position;
        public final int taskPosition;

        public SnapTarget(int i, int i2, int i3) {
            this(i, i2, i3, 1.0f);
        }

        public SnapTarget(int i, int i2, int i3, float f) {
            this.position = i;
            this.taskPosition = i2;
            this.flag = i3;
            this.distanceMultiplier = f;
        }

        public String toString() {
            return "position " + this.position + " taskPosition " + this.taskPosition + " distanceMultiplier " + this.distanceMultiplier;
        }
    }
}
