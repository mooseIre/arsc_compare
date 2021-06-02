package com.android.systemui.qs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import java.util.ArrayList;

public class PageIndicator extends ViewGroup {
    private boolean mAnimating;
    private final Runnable mAnimationDone = new Runnable() {
        /* class com.android.systemui.qs.PageIndicator.AnonymousClass1 */

        public void run() {
            PageIndicator.this.mAnimating = false;
            if (PageIndicator.this.mQueuedPositions.size() != 0) {
                PageIndicator pageIndicator = PageIndicator.this;
                pageIndicator.setPosition(((Integer) pageIndicator.mQueuedPositions.remove(0)).intValue());
            }
        }
    };
    private final int mPageDotWidth = ((int) (((float) this.mPageIndicatorWidth) * 0.4f));
    private final int mPageIndicatorHeight = ((int) ((ViewGroup) this).mContext.getResources().getDimension(C0012R$dimen.qs_page_indicator_height));
    private final int mPageIndicatorWidth = ((int) ((ViewGroup) this).mContext.getResources().getDimension(C0012R$dimen.qs_page_indicator_width));
    private int mPosition = -1;
    private final ArrayList<Integer> mQueuedPositions = new ArrayList<>();

    private float getAlpha(boolean z) {
        return z ? 1.0f : 0.42f;
    }

    public PageIndicator(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setNumPages(int i) {
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(new int[]{16843818});
        int color = obtainStyledAttributes.getColor(0, 0);
        obtainStyledAttributes.recycle();
        setNumPages(i, color);
    }

    public void setNumPages(int i, int i2) {
        setVisibility(i > 1 ? 0 : 8);
        if (i != getChildCount()) {
            if (this.mAnimating) {
                Log.w("PageIndicator", "setNumPages during animation");
            }
            while (i < getChildCount()) {
                removeViewAt(getChildCount() - 1);
            }
            while (i > getChildCount()) {
                ImageView imageView = new ImageView(((ViewGroup) this).mContext);
                imageView.setImageResource(C0013R$drawable.minor_a_b);
                imageView.setImageTintList(ColorStateList.valueOf(i2));
                addView(imageView, new ViewGroup.LayoutParams(this.mPageIndicatorWidth, this.mPageIndicatorHeight));
            }
            setIndex(this.mPosition >> 1);
        }
    }

    public void setLocation(float f) {
        int i = (int) f;
        int i2 = 0;
        setContentDescription(getContext().getString(C0021R$string.accessibility_quick_settings_page, Integer.valueOf(i + 1), Integer.valueOf(getChildCount())));
        int i3 = i << 1;
        if (f != ((float) i)) {
            i2 = 1;
        }
        int i4 = i3 | i2;
        int i5 = this.mPosition;
        if (this.mQueuedPositions.size() != 0) {
            ArrayList<Integer> arrayList = this.mQueuedPositions;
            i5 = arrayList.get(arrayList.size() - 1).intValue();
        }
        if (i4 != i5) {
            if (this.mAnimating) {
                this.mQueuedPositions.add(Integer.valueOf(i4));
            } else {
                setPosition(i4);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setPosition(int i) {
        if (!isVisibleToUser() || Math.abs(this.mPosition - i) != 1) {
            setIndex(i >> 1);
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
            imageView.setTranslationX(0.0f);
            imageView.setImageResource(C0013R$drawable.major_a_b);
            imageView.setAlpha(getAlpha(i2 == i));
            i2++;
        }
    }

    private void animate(int i, int i2) {
        int i3 = i >> 1;
        int i4 = i2 >> 1;
        setIndex(i3);
        boolean z = (i & 1) != 0;
        boolean z2 = !z ? i < i2 : i > i2;
        int min = Math.min(i3, i4);
        int max = Math.max(i3, i4);
        if (max == min) {
            max++;
        }
        ImageView imageView = (ImageView) getChildAt(min);
        ImageView imageView2 = (ImageView) getChildAt(max);
        if (imageView != null && imageView2 != null) {
            imageView2.setTranslationX(imageView.getX() - imageView2.getX());
            playAnimation(imageView, getTransition(z, z2, false));
            imageView.setAlpha(getAlpha(false));
            playAnimation(imageView2, getTransition(z, z2, true));
            imageView2.setAlpha(getAlpha(true));
            this.mAnimating = true;
        }
    }

    private void playAnimation(ImageView imageView, int i) {
        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) getContext().getDrawable(i);
        imageView.setImageDrawable(animatedVectorDrawable);
        animatedVectorDrawable.forceAnimationOnUI();
        animatedVectorDrawable.start();
        postDelayed(this.mAnimationDone, 250);
    }

    private int getTransition(boolean z, boolean z2, boolean z3) {
        if (z3) {
            if (z) {
                if (z2) {
                    return C0013R$drawable.major_b_a_animation;
                }
                return C0013R$drawable.major_b_c_animation;
            } else if (z2) {
                return C0013R$drawable.major_a_b_animation;
            } else {
                return C0013R$drawable.major_c_b_animation;
            }
        } else if (z) {
            if (z2) {
                return C0013R$drawable.minor_b_c_animation;
            }
            return C0013R$drawable.minor_b_a_animation;
        } else if (z2) {
            return C0013R$drawable.minor_c_b_animation;
        } else {
            return C0013R$drawable.minor_a_b_animation;
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int childCount = getChildCount();
        if (childCount == 0) {
            super.onMeasure(i, i2);
            return;
        }
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mPageIndicatorWidth, 1073741824);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mPageIndicatorHeight, 1073741824);
        for (int i3 = 0; i3 < childCount; i3++) {
            getChildAt(i3).measure(makeMeasureSpec, makeMeasureSpec2);
        }
        int i4 = this.mPageIndicatorWidth;
        int i5 = this.mPageDotWidth;
        setMeasuredDimension(((i4 - i5) * (childCount - 1)) + i5, this.mPageIndicatorHeight);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int childCount = getChildCount();
        if (childCount != 0) {
            for (int i5 = 0; i5 < childCount; i5++) {
                int i6 = (this.mPageIndicatorWidth - this.mPageDotWidth) * i5;
                getChildAt(i5).layout(i6, 0, this.mPageIndicatorWidth + i6, this.mPageIndicatorHeight);
            }
        }
    }
}
