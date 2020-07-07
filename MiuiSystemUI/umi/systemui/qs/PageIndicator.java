package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.plugins.R;

public class PageIndicator extends ViewGroup {
    private final int mPageDotSize;
    private final int mPageDotSpace;
    private int mPosition = -1;

    private float getAlpha(boolean z) {
        return z ? 0.7f : 0.2f;
    }

    public PageIndicator(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Resources resources = this.mContext.getResources();
        this.mPageDotSize = (int) resources.getDimension(R.dimen.qs_page_indicator_dot_size);
        this.mPageDotSpace = (int) resources.getDimension(R.dimen.qs_page_indicator_dot_space);
    }

    public void setNumPages(int i) {
        setVisibility(i > 1 ? 0 : 4);
        while (i < getChildCount()) {
            removeViewAt(getChildCount() - 1);
        }
        while (i > getChildCount()) {
            ImageView imageView = new ImageView(this.mContext);
            imageView.setImageResource(R.drawable.qs_page_indicator_dot);
            addView(imageView);
        }
        setIndex(this.mPosition >> 1);
    }

    public void setLocation(float f) {
        int i = (int) f;
        setContentDescription(getContext().getString(R.string.accessibility_quick_settings_page, new Object[]{Integer.valueOf(i + 1), Integer.valueOf(getChildCount())}));
        int i2 = this.mPosition;
        if (i == i2) {
            return;
        }
        if (i2 <= i || f - ((float) i) <= 0.0f) {
            setPosition(i);
        }
    }

    private void setPosition(int i) {
        if (!isVisibleToUser() || Math.abs(this.mPosition - i) != 1) {
            setIndex(i);
        } else {
            animate(this.mPosition, i);
        }
        this.mPosition = i;
    }

    private void setIndex(int i) {
        int childCount = getChildCount();
        int i2 = 0;
        while (i2 < childCount) {
            ImageView imageView = (ImageView) getChildAt(i2);
            imageView.setImageResource(R.drawable.qs_page_indicator_dot);
            imageView.setAlpha(getAlpha(i2 == i));
            i2++;
        }
    }

    private void animate(int i, int i2) {
        setIndex(i2);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int childCount = getChildCount();
        if (childCount == 0) {
            super.onMeasure(i, i2);
            return;
        }
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mPageDotSize, 1073741824);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mPageDotSize, 1073741824);
        for (int i3 = 0; i3 < childCount; i3++) {
            getChildAt(i3).measure(makeMeasureSpec, makeMeasureSpec2);
        }
        int i4 = this.mPageDotSize;
        int i5 = this.mPageDotSpace;
        setMeasuredDimension(((i4 + i5) * childCount) - i5, i4);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int childCount = getChildCount();
        if (childCount != 0) {
            for (int i5 = 0; i5 < childCount; i5++) {
                int i6 = (this.mPageDotSize + this.mPageDotSpace) * i5;
                View childAt = getChildAt(i5);
                int i7 = this.mPageDotSize;
                childAt.layout(i6, 0, i7 + i6, i7);
            }
        }
    }
}
