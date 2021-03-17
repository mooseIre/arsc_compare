package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiPageIndicator.kt */
public final class MiuiPageIndicator extends ViewGroup {
    private final int mPageDotSize;
    private final int mPageDotSpace;
    private int mPosition = -1;

    private final float getAlpha(boolean z) {
        return z ? 0.7f : 0.2f;
    }

    public MiuiPageIndicator(@Nullable Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        Context context2 = getContext();
        Intrinsics.checkExpressionValueIsNotNull(context2, "this.context");
        Resources resources = context2.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "this.context.resources");
        this.mPageDotSize = (int) resources.getDimension(C0012R$dimen.qs_page_indicator_dot_size);
        this.mPageDotSpace = (int) resources.getDimension(C0012R$dimen.qs_page_indicator_dot_space);
    }

    public final void setNumPages(int i) {
        if (i != getChildCount()) {
            setVisibility(i > 1 ? 0 : 4);
            while (i < getChildCount()) {
                removeViewAt(getChildCount() - 1);
            }
            while (i > getChildCount()) {
                ImageView imageView = new ImageView(getContext());
                imageView.setImageResource(C0013R$drawable.qs_page_indicator_dot);
                addView(imageView);
            }
            setIndex(this.mPosition >> 1);
        }
    }

    public final void setLocation(float f) {
        int i = (int) f;
        setContentDescription(getContext().getString(C0021R$string.accessibility_quick_settings_page, Integer.valueOf(i + 1), Integer.valueOf(getChildCount())));
        int i2 = this.mPosition;
        if (i == i2) {
            return;
        }
        if (i2 <= i || f - ((float) i) <= 0.0f) {
            setPosition(i);
        }
    }

    private final void setPosition(int i) {
        if (!isVisibleToUser() || Math.abs(this.mPosition - i) != 1) {
            setIndex(i);
        } else {
            animate(this.mPosition, i);
        }
        this.mPosition = i;
    }

    private final void setIndex(int i) {
        int childCount = getChildCount();
        int i2 = 0;
        while (i2 < childCount) {
            View childAt = getChildAt(i2);
            if (childAt != null) {
                ImageView imageView = (ImageView) childAt;
                imageView.setImageResource(C0013R$drawable.qs_page_indicator_dot);
                imageView.setAlpha(getAlpha(i2 == i));
                i2++;
            } else {
                throw new TypeCastException("null cannot be cast to non-null type android.widget.ImageView");
            }
        }
    }

    private final void animate(int i, int i2) {
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
