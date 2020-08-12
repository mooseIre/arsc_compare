package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

public final class KeyboardShortcutKeysLayout extends ViewGroup {
    private final Context mContext;
    private int mLineHeight;

    public KeyboardShortcutKeysLayout(Context context) {
        super(context);
        this.mContext = context;
    }

    public KeyboardShortcutKeysLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int i3;
        int i4;
        int size = (View.MeasureSpec.getSize(i) - getPaddingLeft()) - getPaddingRight();
        int childCount = getChildCount();
        int size2 = (View.MeasureSpec.getSize(i2) - getPaddingTop()) - getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        if (View.MeasureSpec.getMode(i2) == Integer.MIN_VALUE) {
            i3 = View.MeasureSpec.makeMeasureSpec(size2, Integer.MIN_VALUE);
        } else {
            i3 = View.MeasureSpec.makeMeasureSpec(0, 0);
        }
        int i5 = paddingLeft;
        int i6 = 0;
        for (int i7 = 0; i7 < childCount; i7++) {
            View childAt = getChildAt(i7);
            if (childAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                childAt.measure(View.MeasureSpec.makeMeasureSpec(size, Integer.MIN_VALUE), i3);
                int measuredWidth = childAt.getMeasuredWidth();
                i6 = Math.max(i6, childAt.getMeasuredHeight() + layoutParams.mVerticalSpacing);
                if (i5 + measuredWidth > size) {
                    i5 = getPaddingLeft();
                    paddingTop += i6;
                }
                i5 += measuredWidth + layoutParams.mHorizontalSpacing;
            }
        }
        this.mLineHeight = i6;
        if (View.MeasureSpec.getMode(i2) == 0) {
            size2 = paddingTop + i6;
        } else if (View.MeasureSpec.getMode(i2) == Integer.MIN_VALUE && (i4 = paddingTop + i6) < size2) {
            size2 = i4;
        }
        setMeasuredDimension(size, size2);
    }

    /* access modifiers changed from: protected */
    public LayoutParams generateDefaultLayoutParams() {
        int horizontalVerticalSpacing = getHorizontalVerticalSpacing();
        return new LayoutParams(horizontalVerticalSpacing, horizontalVerticalSpacing);
    }

    /* access modifiers changed from: protected */
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        int horizontalVerticalSpacing = getHorizontalVerticalSpacing();
        return new LayoutParams(horizontalVerticalSpacing, horizontalVerticalSpacing, layoutParams);
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int childCount = getChildCount();
        int i7 = i3 - i;
        if (isRTL()) {
            i5 = i7 - getPaddingRight();
        } else {
            i5 = getPaddingLeft();
        }
        int paddingTop = getPaddingTop();
        int i8 = i5;
        int i9 = 0;
        int i10 = 0;
        for (int i11 = 0; i11 < childCount; i11++) {
            View childAt = getChildAt(i11);
            if (childAt.getVisibility() != 8) {
                int measuredWidth = childAt.getMeasuredWidth();
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                boolean z2 = true;
                if (!isRTL() ? i8 + measuredWidth <= i7 : (i8 - getPaddingLeft()) - measuredWidth >= 0) {
                    z2 = false;
                }
                if (z2) {
                    layoutChildrenOnRow(i9, i11, i7, i8, paddingTop, i10);
                    if (isRTL()) {
                        i6 = i7 - getPaddingRight();
                    } else {
                        i6 = getPaddingLeft();
                    }
                    i8 = i6;
                    paddingTop += this.mLineHeight;
                    i9 = i11;
                }
                if (isRTL()) {
                    i8 = (i8 - measuredWidth) - layoutParams.mHorizontalSpacing;
                } else {
                    i8 = i8 + measuredWidth + layoutParams.mHorizontalSpacing;
                }
                i10 = layoutParams.mHorizontalSpacing;
            }
        }
        if (i9 < childCount) {
            layoutChildrenOnRow(i9, childCount, i7, i8, paddingTop, i10);
        }
    }

    private int getHorizontalVerticalSpacing() {
        return (int) TypedValue.applyDimension(1, 4.0f, getResources().getDisplayMetrics());
    }

    private void layoutChildrenOnRow(int i, int i2, int i3, int i4, int i5, int i6) {
        if (!isRTL()) {
            i4 = ((getPaddingLeft() + i3) - i4) + i6;
        }
        int i7 = i4;
        int i8 = i;
        while (i8 < i2) {
            View childAt = getChildAt(i8);
            int measuredWidth = childAt.getMeasuredWidth();
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if (isRTL() && i8 == i) {
                i7 = (((i3 - i7) - getPaddingRight()) - measuredWidth) - layoutParams.mHorizontalSpacing;
            }
            childAt.layout(i7, i5, i7 + measuredWidth, childAt.getMeasuredHeight() + i5);
            if (isRTL()) {
                i7 -= (i8 < i2 + -1 ? getChildAt(i8 + 1).getMeasuredWidth() : 0) + layoutParams.mHorizontalSpacing;
            } else {
                i7 += measuredWidth + layoutParams.mHorizontalSpacing;
            }
            i8++;
        }
    }

    private boolean isRTL() {
        return this.mContext.getResources().getConfiguration().getLayoutDirection() == 1;
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public final int mHorizontalSpacing;
        public final int mVerticalSpacing;

        public LayoutParams(int i, int i2, ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.mHorizontalSpacing = i;
            this.mVerticalSpacing = i2;
        }

        public LayoutParams(int i, int i2) {
            super(0, 0);
            this.mHorizontalSpacing = i;
            this.mVerticalSpacing = i2;
        }
    }
}
