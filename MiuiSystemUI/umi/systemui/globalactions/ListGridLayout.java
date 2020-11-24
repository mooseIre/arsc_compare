package com.android.systemui.globalactions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.internal.annotations.VisibleForTesting;

public class ListGridLayout extends LinearLayout {
    private final int[][] mConfigs = {new int[]{0, 0}, new int[]{1, 1}, new int[]{1, 2}, new int[]{1, 3}, new int[]{2, 2}, new int[]{2, 3}, new int[]{2, 3}, new int[]{3, 3}, new int[]{3, 3}, new int[]{3, 3}};
    private int mCurrentCount = 0;
    private int mExpectedCount;
    private boolean mReverseItems;
    private boolean mReverseSublists;
    private boolean mSwapRowsAndColumns;

    public ListGridLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setSwapRowsAndColumns(boolean z) {
        this.mSwapRowsAndColumns = z;
    }

    public void setReverseSublists(boolean z) {
        this.mReverseSublists = z;
    }

    public void setReverseItems(boolean z) {
        this.mReverseItems = z;
    }

    public void removeAllItems() {
        for (int i = 0; i < getChildCount(); i++) {
            ViewGroup sublist = getSublist(i);
            if (sublist != null) {
                sublist.removeAllViews();
                sublist.setVisibility(8);
            }
        }
        this.mCurrentCount = 0;
    }

    public void addItem(View view) {
        ViewGroup parentView = getParentView(this.mCurrentCount, this.mReverseSublists, this.mSwapRowsAndColumns);
        if (this.mReverseItems) {
            parentView.addView(view, 0);
        } else {
            parentView.addView(view);
        }
        parentView.setVisibility(0);
        this.mCurrentCount++;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public ViewGroup getParentView(int i, boolean z, boolean z2) {
        if (getRowCount() == 0 || i < 0) {
            return null;
        }
        return getSublist(getParentViewIndex(Math.min(i, getMaxElementCount() - 1), z, z2));
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public ViewGroup getSublist(int i) {
        return (ViewGroup) getChildAt(i);
    }

    private int reverseSublistIndex(int i) {
        return getChildCount() - (i + 1);
    }

    private int getParentViewIndex(int i, boolean z, boolean z2) {
        int i2;
        int rowCount = getRowCount();
        if (z2) {
            i2 = (int) Math.floor((double) (i / rowCount));
        } else {
            i2 = i % rowCount;
        }
        return z ? reverseSublistIndex(i2) : i2;
    }

    public void setExpectedCount(int i) {
        this.mExpectedCount = i;
    }

    private int getMaxElementCount() {
        return this.mConfigs.length - 1;
    }

    private int[] getConfig() {
        if (this.mExpectedCount < 0) {
            return this.mConfigs[0];
        }
        return this.mConfigs[Math.min(getMaxElementCount(), this.mExpectedCount)];
    }

    public int getRowCount() {
        return getConfig()[0];
    }
}
