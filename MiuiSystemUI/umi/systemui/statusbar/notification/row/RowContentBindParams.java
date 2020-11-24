package com.android.systemui.statusbar.notification.row;

public final class RowContentBindParams {
    private int mContentViews = 3;
    private int mDirtyContentViews = 3;
    private boolean mUseIncreasedHeadsUpHeight;
    private boolean mUseIncreasedHeight;
    private boolean mUseLowPriority;
    private boolean mViewsNeedReinflation;

    public void setUseLowPriority(boolean z) {
        if (this.mUseLowPriority != z) {
            this.mDirtyContentViews |= 3;
        }
        this.mUseLowPriority = z;
    }

    public boolean useLowPriority() {
        return this.mUseLowPriority;
    }

    public void setUseIncreasedCollapsedHeight(boolean z) {
        if (this.mUseIncreasedHeight != z) {
            this.mDirtyContentViews |= 1;
        }
        this.mUseIncreasedHeight = z;
    }

    public boolean useIncreasedHeight() {
        return this.mUseIncreasedHeight;
    }

    public void setUseIncreasedHeadsUpHeight(boolean z) {
        if (this.mUseIncreasedHeadsUpHeight != z) {
            this.mDirtyContentViews |= 4;
        }
        this.mUseIncreasedHeadsUpHeight = z;
    }

    public boolean useIncreasedHeadsUpHeight() {
        return this.mUseIncreasedHeadsUpHeight;
    }

    public void requireContentViews(int i) {
        int i2 = this.mContentViews;
        int i3 = i & (~i2);
        this.mContentViews = i2 | i3;
        this.mDirtyContentViews = i3 | this.mDirtyContentViews;
    }

    public void markContentViewsFreeable(int i) {
        int i2 = this.mContentViews;
        int i3 = ~i;
        this.mContentViews = i2 & i3;
        this.mDirtyContentViews = i3 & this.mDirtyContentViews;
    }

    public int getContentViews() {
        return this.mContentViews;
    }

    public void rebindAllContentViews() {
        this.mDirtyContentViews = this.mContentViews;
    }

    /* access modifiers changed from: package-private */
    public void clearDirtyContentViews() {
        this.mDirtyContentViews = 0;
    }

    public int getDirtyContentViews() {
        return this.mDirtyContentViews;
    }

    public void setNeedsReinflation(boolean z) {
        this.mViewsNeedReinflation = z;
        this.mDirtyContentViews = this.mContentViews | this.mDirtyContentViews;
    }

    public boolean needsReinflation() {
        return this.mViewsNeedReinflation;
    }

    public String toString() {
        return String.format("RowContentBindParams[mContentViews=%x mDirtyContentViews=%x mUseLowPriority=%b mUseIncreasedHeight=%b mUseIncreasedHeadsUpHeight=%b mViewsNeedReinflation=%b]", new Object[]{Integer.valueOf(this.mContentViews), Integer.valueOf(this.mDirtyContentViews), Boolean.valueOf(this.mUseLowPriority), Boolean.valueOf(this.mUseIncreasedHeight), Boolean.valueOf(this.mUseIncreasedHeadsUpHeight), Boolean.valueOf(this.mViewsNeedReinflation)});
    }
}
