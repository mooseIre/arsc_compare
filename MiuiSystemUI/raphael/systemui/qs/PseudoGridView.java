package com.android.systemui.qs;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.android.systemui.R$styleable;
import java.lang.ref.WeakReference;

public class PseudoGridView extends ViewGroup {
    private int mHorizontalSpacing;
    private int mNumColumns = 3;
    private int mVerticalSpacing;

    public PseudoGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.PseudoGridView);
        int indexCount = obtainStyledAttributes.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = obtainStyledAttributes.getIndex(i);
            if (index == R$styleable.PseudoGridView_numColumns) {
                this.mNumColumns = obtainStyledAttributes.getInt(index, 3);
            } else if (index == R$styleable.PseudoGridView_verticalSpacing) {
                this.mVerticalSpacing = obtainStyledAttributes.getDimensionPixelSize(index, 0);
            } else if (index == R$styleable.PseudoGridView_horizontalSpacing) {
                this.mHorizontalSpacing = obtainStyledAttributes.getDimensionPixelSize(index, 0);
            }
        }
        obtainStyledAttributes.recycle();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        if (View.MeasureSpec.getMode(i) != 0) {
            int size = View.MeasureSpec.getSize(i);
            int i3 = this.mNumColumns;
            int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec((size - ((i3 - 1) * this.mHorizontalSpacing)) / i3, 1073741824);
            int childCount = getChildCount();
            int i4 = this.mNumColumns;
            int i5 = ((childCount + i4) - 1) / i4;
            int i6 = 0;
            for (int i7 = 0; i7 < i5; i7++) {
                int i8 = this.mNumColumns;
                int i9 = i7 * i8;
                int min = Math.min(i8 + i9, childCount);
                int i10 = 0;
                for (int i11 = i9; i11 < min; i11++) {
                    View childAt = getChildAt(i11);
                    childAt.measure(makeMeasureSpec, 0);
                    i10 = Math.max(i10, childAt.getMeasuredHeight());
                }
                int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(i10, 1073741824);
                while (i9 < min) {
                    View childAt2 = getChildAt(i9);
                    if (childAt2.getMeasuredHeight() != i10) {
                        childAt2.measure(makeMeasureSpec, makeMeasureSpec2);
                    }
                    i9++;
                }
                i6 += i10;
                if (i7 > 0) {
                    i6 += this.mVerticalSpacing;
                }
            }
            setMeasuredDimension(size, ViewGroup.resolveSizeAndState(i6, i2, 0));
            return;
        }
        throw new UnsupportedOperationException("Needs a maximum width");
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        boolean isLayoutRtl = isLayoutRtl();
        int childCount = getChildCount();
        int i5 = this.mNumColumns;
        int i6 = ((childCount + i5) - 1) / i5;
        int i7 = 0;
        for (int i8 = 0; i8 < i6; i8++) {
            int width = isLayoutRtl ? getWidth() : 0;
            int i9 = this.mNumColumns;
            int i10 = i8 * i9;
            int min = Math.min(i9 + i10, childCount);
            int i11 = 0;
            while (i10 < min) {
                View childAt = getChildAt(i10);
                int measuredWidth = childAt.getMeasuredWidth();
                int measuredHeight = childAt.getMeasuredHeight();
                if (isLayoutRtl) {
                    width -= measuredWidth;
                }
                childAt.layout(width, i7, width + measuredWidth, i7 + measuredHeight);
                i11 = Math.max(i11, measuredHeight);
                if (isLayoutRtl) {
                    width -= this.mHorizontalSpacing;
                } else {
                    width += measuredWidth + this.mHorizontalSpacing;
                }
                i10++;
            }
            i7 += i11;
            if (i8 > 0) {
                i7 += this.mVerticalSpacing;
            }
        }
    }

    public static class ViewGroupAdapterBridge extends DataSetObserver {
        private final BaseAdapter mAdapter;
        private boolean mReleased = false;
        private final WeakReference<ViewGroup> mViewGroup;

        public static void link(ViewGroup viewGroup, BaseAdapter baseAdapter) {
            new ViewGroupAdapterBridge(viewGroup, baseAdapter);
        }

        private ViewGroupAdapterBridge(ViewGroup viewGroup, BaseAdapter baseAdapter) {
            this.mViewGroup = new WeakReference<>(viewGroup);
            this.mAdapter = baseAdapter;
            baseAdapter.registerDataSetObserver(this);
            refresh();
        }

        private void refresh() {
            if (!this.mReleased) {
                ViewGroup viewGroup = this.mViewGroup.get();
                if (viewGroup == null) {
                    release();
                    return;
                }
                int childCount = viewGroup.getChildCount();
                int count = this.mAdapter.getCount();
                int max = Math.max(childCount, count);
                for (int i = 0; i < max; i++) {
                    if (i < count) {
                        View view = null;
                        if (i < childCount) {
                            view = viewGroup.getChildAt(i);
                        }
                        View view2 = this.mAdapter.getView(i, view, viewGroup);
                        if (view == null) {
                            viewGroup.addView(view2);
                        } else if (view != view2) {
                            viewGroup.removeViewAt(i);
                            viewGroup.addView(view2, i);
                        }
                    } else {
                        viewGroup.removeViewAt(viewGroup.getChildCount() - 1);
                    }
                }
            }
        }

        public void onChanged() {
            refresh();
        }

        public void onInvalidated() {
            release();
        }

        private void release() {
            if (!this.mReleased) {
                this.mReleased = true;
                this.mAdapter.unregisterDataSetObserver(this);
            }
        }
    }
}
