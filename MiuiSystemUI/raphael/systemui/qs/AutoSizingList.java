package com.android.systemui.qs;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import com.android.systemui.R$styleable;

public class AutoSizingList extends LinearLayout {
    private ListAdapter mAdapter;
    private final Runnable mBindChildren = new Runnable() {
        /* class com.android.systemui.qs.AutoSizingList.AnonymousClass1 */

        public void run() {
            AutoSizingList.this.rebindChildren();
        }
    };
    private int mCount;
    private final DataSetObserver mDataObserver = new DataSetObserver() {
        /* class com.android.systemui.qs.AutoSizingList.AnonymousClass2 */

        public void onChanged() {
            if (AutoSizingList.this.mCount > AutoSizingList.this.getDesiredCount()) {
                AutoSizingList autoSizingList = AutoSizingList.this;
                autoSizingList.mCount = autoSizingList.getDesiredCount();
            }
            AutoSizingList.this.postRebindChildren();
        }

        public void onInvalidated() {
            AutoSizingList.this.postRebindChildren();
        }
    };
    private boolean mEnableAutoSizing;
    private final Handler mHandler = new Handler();
    private final int mItemSize;

    public AutoSizingList(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.AutoSizingList);
        this.mItemSize = obtainStyledAttributes.getDimensionPixelSize(R$styleable.AutoSizingList_itemHeight, 0);
        this.mEnableAutoSizing = obtainStyledAttributes.getBoolean(R$styleable.AutoSizingList_enableAutoSizing, true);
        obtainStyledAttributes.recycle();
    }

    public void setAdapter(ListAdapter listAdapter) {
        ListAdapter listAdapter2 = this.mAdapter;
        if (listAdapter2 != null) {
            listAdapter2.unregisterDataSetObserver(this.mDataObserver);
        }
        this.mAdapter = listAdapter;
        if (listAdapter != null) {
            listAdapter.registerDataSetObserver(this.mDataObserver);
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int itemCount;
        int size = View.MeasureSpec.getSize(i2);
        if (!(size == 0 || this.mCount == (itemCount = getItemCount(size)))) {
            postRebindChildren();
            this.mCount = itemCount;
        }
        super.onMeasure(i, i2);
    }

    private int getItemCount(int i) {
        int desiredCount = getDesiredCount();
        return this.mEnableAutoSizing ? Math.min(i / this.mItemSize, desiredCount) : desiredCount;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDesiredCount() {
        ListAdapter listAdapter = this.mAdapter;
        if (listAdapter != null) {
            return listAdapter.getCount();
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postRebindChildren() {
        this.mHandler.post(this.mBindChildren);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void rebindChildren() {
        if (this.mAdapter != null) {
            int i = 0;
            while (i < this.mCount) {
                View childAt = i < getChildCount() ? getChildAt(i) : null;
                View view = this.mAdapter.getView(i, childAt, this);
                if (view != childAt) {
                    if (childAt != null) {
                        removeView(childAt);
                    }
                    addView(view, i);
                }
                i++;
            }
            while (getChildCount() > this.mCount) {
                removeViewAt(getChildCount() - 1);
            }
        }
    }
}
