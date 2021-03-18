package com.android.systemui.stackdivider;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.TypedValue;
import android.window.WindowContainerTransaction;
import com.android.systemui.stackdivider.DividerSnapAlgorithm;
import com.android.systemui.wm.DisplayLayout;

public class SplitDisplayLayout {
    Rect mAdjustedPrimary = null;
    Rect mAdjustedSecondary = null;
    Context mContext;
    DisplayLayout mDisplayLayout;
    int mDividerSize;
    int mDividerSizeInactive;
    private DividerSnapAlgorithm mMinimizedSnapAlgorithm = null;
    Rect mPrimary = null;
    boolean mResourcesValid = false;
    Rect mSecondary = null;
    private DividerSnapAlgorithm mSnapAlgorithm = null;
    SplitScreenTaskOrganizer mTiles;

    public SplitDisplayLayout(Context context, DisplayLayout displayLayout, SplitScreenTaskOrganizer splitScreenTaskOrganizer) {
        this.mTiles = splitScreenTaskOrganizer;
        this.mDisplayLayout = displayLayout;
        this.mContext = context;
    }

    /* access modifiers changed from: package-private */
    public void rotateTo(int i) {
        this.mDisplayLayout.rotateTo(this.mContext.getResources(), i);
        Configuration configuration = new Configuration();
        configuration.unset();
        configuration.orientation = this.mDisplayLayout.getOrientation();
        Rect rect = new Rect(0, 0, this.mDisplayLayout.width(), this.mDisplayLayout.height());
        rect.inset(this.mDisplayLayout.nonDecorInsets());
        configuration.windowConfiguration.setAppBounds(rect);
        rect.set(0, 0, this.mDisplayLayout.width(), this.mDisplayLayout.height());
        rect.inset(this.mDisplayLayout.stableInsets());
        configuration.screenWidthDp = (int) (((float) rect.width()) / this.mDisplayLayout.density());
        configuration.screenHeightDp = (int) (((float) rect.height()) / this.mDisplayLayout.density());
        this.mContext = this.mContext.createConfigurationContext(configuration);
        this.mSnapAlgorithm = null;
        this.mMinimizedSnapAlgorithm = null;
        this.mResourcesValid = false;
    }

    private void updateResources() {
        if (!this.mResourcesValid) {
            this.mResourcesValid = true;
            Resources resources = this.mContext.getResources();
            this.mDividerSize = DockedDividerUtils.getDividerSize(resources, DockedDividerUtils.getDividerInsets(resources));
            this.mDividerSizeInactive = (int) TypedValue.applyDimension(1, 4.0f, resources.getDisplayMetrics());
        }
    }

    /* access modifiers changed from: package-private */
    public int getPrimarySplitSide() {
        int navigationBarPosition = this.mDisplayLayout.getNavigationBarPosition(this.mContext.getResources());
        if (navigationBarPosition == 1) {
            return 3;
        }
        if (navigationBarPosition == 2) {
            return 1;
        }
        if (navigationBarPosition != 4) {
            return -1;
        }
        if (SplitDisplayLayoutInjector.canUpdatePrimarySplitSide(this.mContext)) {
            return SplitDisplayLayoutInjector.getPrimarySplitSide(this.mContext, this.mDisplayLayout.isLandscape());
        }
        return this.mDisplayLayout.isLandscape() ? 1 : 2;
    }

    /* access modifiers changed from: package-private */
    public DividerSnapAlgorithm getSnapAlgorithm() {
        if (this.mSnapAlgorithm == null) {
            updateResources();
            this.mSnapAlgorithm = new DividerSnapAlgorithm(this.mContext.getResources(), this.mDisplayLayout.width(), this.mDisplayLayout.height(), this.mDividerSize, !this.mDisplayLayout.isLandscape(), this.mDisplayLayout.stableInsets(), getPrimarySplitSide());
        }
        return this.mSnapAlgorithm;
    }

    /* access modifiers changed from: package-private */
    public DividerSnapAlgorithm getMinimizedSnapAlgorithm(boolean z) {
        if (this.mMinimizedSnapAlgorithm == null) {
            updateResources();
            this.mMinimizedSnapAlgorithm = new DividerSnapAlgorithm(this.mContext.getResources(), this.mDisplayLayout.width(), this.mDisplayLayout.height(), this.mDividerSize, !this.mDisplayLayout.isLandscape(), this.mDisplayLayout.stableInsets(), getPrimarySplitSide(), true, z);
        }
        return this.mMinimizedSnapAlgorithm;
    }

    /* access modifiers changed from: package-private */
    public void resizeSplits(int i) {
        Rect rect = this.mPrimary;
        if (rect == null) {
            rect = new Rect();
        }
        this.mPrimary = rect;
        Rect rect2 = this.mSecondary;
        if (rect2 == null) {
            rect2 = new Rect();
        }
        this.mSecondary = rect2;
        calcSplitBounds(i, this.mPrimary, rect2);
    }

    /* access modifiers changed from: package-private */
    public void resizeSplits(int i, WindowContainerTransaction windowContainerTransaction) {
        resizeSplits(i);
        windowContainerTransaction.setBounds(this.mTiles.mPrimary.token, this.mPrimary);
        windowContainerTransaction.setBounds(this.mTiles.mSecondary.token, this.mSecondary);
        windowContainerTransaction.setSmallestScreenWidthDp(this.mTiles.mPrimary.token, getSmallestWidthDpForBounds(this.mContext, this.mDisplayLayout, this.mPrimary));
        windowContainerTransaction.setSmallestScreenWidthDp(this.mTiles.mSecondary.token, getSmallestWidthDpForBounds(this.mContext, this.mDisplayLayout, this.mSecondary));
    }

    /* access modifiers changed from: package-private */
    public void calcSplitBounds(int i, Rect rect, Rect rect2) {
        int primarySplitSide = getPrimarySplitSide();
        DockedDividerUtils.calculateBoundsForPosition(i, primarySplitSide, rect, this.mDisplayLayout.width(), this.mDisplayLayout.height(), this.mDividerSize);
        DockedDividerUtils.calculateBoundsForPosition(i, DockedDividerUtils.invertDockSide(primarySplitSide), rect2, this.mDisplayLayout.width(), this.mDisplayLayout.height(), this.mDividerSize);
    }

    /* access modifiers changed from: package-private */
    public Rect calcResizableMinimizedHomeStackBounds() {
        DividerSnapAlgorithm.SnapTarget middleTarget = getMinimizedSnapAlgorithm(true).getMiddleTarget();
        Rect rect = new Rect();
        DockedDividerUtils.calculateBoundsForPosition(middleTarget.position, DockedDividerUtils.invertDockSide(getPrimarySplitSide()), rect, this.mDisplayLayout.width(), this.mDisplayLayout.height(), this.mDividerSize);
        return rect;
    }

    /* access modifiers changed from: package-private */
    public void updateAdjustedBounds(int i, int i2, int i3) {
        adjustForIME(this.mDisplayLayout, i, i2, i3, this.mDividerSize, this.mDividerSizeInactive, this.mPrimary, this.mSecondary);
    }

    private void adjustForIME(DisplayLayout displayLayout, int i, int i2, int i3, int i4, int i5, Rect rect, Rect rect2) {
        if (this.mAdjustedPrimary == null) {
            this.mAdjustedPrimary = new Rect();
            this.mAdjustedSecondary = new Rect();
        }
        Rect rect3 = new Rect();
        displayLayout.getStableBounds(rect3);
        float f = ((float) (i - i2)) / ((float) (i3 - i2));
        int i6 = rect3.top;
        int i7 = this.mPrimary.bottom;
        int height = displayLayout.height();
        int max = Math.max(0, height - (i + Math.max(0, (i2 - i3) - (i7 - (i6 + ((int) (((float) (i7 - i6)) * 0.3f)))))));
        this.mAdjustedPrimary.set(rect);
        int i8 = -max;
        this.mAdjustedPrimary.offset(0, (i4 - ((int) ((((float) i5) * f) + (((float) i4) * (1.0f - f))))) + i8);
        this.mAdjustedSecondary.set(rect2);
        this.mAdjustedSecondary.offset(0, i8);
    }

    static int getSmallestWidthDpForBounds(Context context, DisplayLayout displayLayout, Rect rect) {
        int dividerSize = DockedDividerUtils.getDividerSize(context.getResources(), DockedDividerUtils.getDividerInsets(context.getResources()));
        Rect rect2 = new Rect();
        Rect rect3 = new Rect();
        Rect rect4 = new Rect(0, 0, displayLayout.width(), displayLayout.height());
        DisplayLayout displayLayout2 = new DisplayLayout();
        int i = Integer.MAX_VALUE;
        for (int i2 = 0; i2 < 4; i2++) {
            displayLayout2.set(displayLayout);
            displayLayout2.rotateTo(context.getResources(), i2);
            DividerSnapAlgorithm initSnapAlgorithmForRotation = initSnapAlgorithmForRotation(context, displayLayout2, dividerSize);
            rect2.set(rect);
            DisplayLayout.rotateBounds(rect2, rect4, i2 - displayLayout.rotation());
            rect3.set(0, 0, displayLayout2.width(), displayLayout2.height());
            int primarySplitSide = getPrimarySplitSide(rect2, rect3, displayLayout2.getOrientation());
            DockedDividerUtils.calculateBoundsForPosition(initSnapAlgorithmForRotation.calculateNonDismissingSnapTarget(DockedDividerUtils.calculatePositionForBounds(rect2, primarySplitSide, dividerSize)).position, primarySplitSide, rect2, displayLayout2.width(), displayLayout2.height(), dividerSize);
            Rect rect5 = new Rect(rect3);
            rect5.inset(displayLayout2.stableInsets());
            rect2.intersect(rect5);
            i = Math.min(rect2.width(), i);
        }
        return (int) (((float) i) / displayLayout.density());
    }

    static DividerSnapAlgorithm initSnapAlgorithmForRotation(Context context, DisplayLayout displayLayout, int i) {
        Configuration configuration = new Configuration();
        configuration.unset();
        configuration.orientation = displayLayout.getOrientation();
        Rect rect = new Rect(0, 0, displayLayout.width(), displayLayout.height());
        rect.inset(displayLayout.nonDecorInsets());
        configuration.windowConfiguration.setAppBounds(rect);
        rect.set(0, 0, displayLayout.width(), displayLayout.height());
        rect.inset(displayLayout.stableInsets());
        configuration.screenWidthDp = (int) (((float) rect.width()) / displayLayout.density());
        configuration.screenHeightDp = (int) (((float) rect.height()) / displayLayout.density());
        return new DividerSnapAlgorithm(context.createConfigurationContext(configuration).getResources(), displayLayout.width(), displayLayout.height(), i, configuration.orientation == 1, displayLayout.stableInsets());
    }

    static int getPrimarySplitSide(Rect rect, Rect rect2, int i) {
        if (i == 1) {
            return (rect2.bottom - rect.bottom) - (rect.top - rect2.top) < 0 ? 4 : 2;
        }
        if (i != 2) {
            return -1;
        }
        if ((rect2.right - rect.right) - (rect.left - rect2.left) < 0) {
            return 3;
        }
        return 1;
    }
}
