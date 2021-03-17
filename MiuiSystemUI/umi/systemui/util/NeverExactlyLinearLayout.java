package com.android.systemui.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import kotlin.Triple;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NeverExactlyLinearLayout.kt */
public final class NeverExactlyLinearLayout extends LinearLayout {
    public NeverExactlyLinearLayout(@NotNull Context context) {
        this(context, null, 0, 6, null);
    }

    public NeverExactlyLinearLayout(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0, 4, null);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ NeverExactlyLinearLayout(Context context, AttributeSet attributeSet, int i, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, (i2 & 2) != 0 ? null : attributeSet, (i2 & 4) != 0 ? 0 : i);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public NeverExactlyLinearLayout(@NotNull Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Intrinsics.checkParameterIsNotNull(context, "context");
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        Triple<Boolean, Integer, Integer> nonExactlyMeasureSpec = getNonExactlyMeasureSpec(i);
        boolean booleanValue = nonExactlyMeasureSpec.component1().booleanValue();
        int intValue = nonExactlyMeasureSpec.component2().intValue();
        int intValue2 = nonExactlyMeasureSpec.component3().intValue();
        Triple<Boolean, Integer, Integer> nonExactlyMeasureSpec2 = getNonExactlyMeasureSpec(i2);
        boolean booleanValue2 = nonExactlyMeasureSpec2.component1().booleanValue();
        int intValue3 = nonExactlyMeasureSpec2.component2().intValue();
        int intValue4 = nonExactlyMeasureSpec2.component3().intValue();
        super.onMeasure(intValue, intValue3);
        if (booleanValue || booleanValue2) {
            if (!booleanValue) {
                intValue2 = getMeasuredWidth();
            }
            if (!booleanValue2) {
                intValue4 = getMeasuredHeight();
            }
            setMeasuredDimension(intValue2, intValue4);
        }
    }

    private final Triple<Boolean, Integer, Integer> getNonExactlyMeasureSpec(int i) {
        boolean z = View.MeasureSpec.getMode(i) == 1073741824;
        int size = View.MeasureSpec.getSize(i);
        if (z) {
            i = View.MeasureSpec.makeMeasureSpec(size, Integer.MIN_VALUE);
        }
        return new Triple<>(Boolean.valueOf(z), Integer.valueOf(i), Integer.valueOf(size));
    }
}
