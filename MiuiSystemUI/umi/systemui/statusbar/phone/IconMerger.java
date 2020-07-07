package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.plugins.R;

public class IconMerger extends LinearLayout {
    private View mClockView;
    private boolean mEnoughSpace;
    private boolean mForceShowingMore;
    private int mIconWidth;
    private View mIcons;
    /* access modifiers changed from: private */
    public View mMoreView;
    private View mStatusBar;
    private View mStatusIcons;
    private int mWidth = 0;

    public IconMerger(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIconWidth = context.getResources().getDimensionPixelSize(R.dimen.status_bar_icon_size) + (context.getResources().getDimensionPixelSize(R.dimen.status_bar_icon_padding) * 2);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        post(new Runnable() {
            public void run() {
                IconMerger.this.requestLayout();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int i3;
        super.onMeasure(i, i2);
        int measuredWidth = ((((((this.mStatusBar.getMeasuredWidth() - this.mStatusBar.getPaddingStart()) - this.mStatusBar.getPaddingEnd()) - this.mStatusIcons.getMeasuredWidth()) - this.mClockView.getMeasuredWidth()) - this.mMoreView.getMeasuredWidth()) - this.mIcons.getPaddingStart()) - this.mIcons.getPaddingEnd();
        if (measuredWidth > this.mIconWidth * getChildCount()) {
            i3 = this.mIconWidth * getChildCount();
        } else {
            i3 = measuredWidth - (measuredWidth % this.mIconWidth);
        }
        this.mWidth = i3;
        int i4 = 0;
        this.mEnoughSpace = measuredWidth >= 0;
        int i5 = this.mWidth;
        if (i5 >= 0) {
            i4 = i5;
        }
        setMeasuredDimension(i4, getMeasuredHeight());
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        checkOverflow();
    }

    private void checkOverflow() {
        if (this.mMoreView != null) {
            int childCount = getChildCount();
            boolean z = false;
            int i = 0;
            for (int i2 = 0; i2 < childCount; i2++) {
                if (getChildAt(i2).getVisibility() != 8) {
                    i++;
                }
            }
            final boolean z2 = (this.mForceShowingMore || i * this.mIconWidth > this.mWidth) && getVisibility() == 0 && this.mEnoughSpace;
            if (this.mMoreView.getVisibility() == 0) {
                z = true;
            }
            if (z2 != z) {
                post(new Runnable() {
                    public void run() {
                        IconMerger.this.mMoreView.setVisibility(z2 ? 0 : 8);
                    }
                });
            }
        }
    }
}
