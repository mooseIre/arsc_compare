package com.android.systemui.stackdivider;

import android.graphics.Rect;

public class DockedDividerUtils {
    public static int calculateMiddlePosition(boolean z, Rect rect, int i, int i2, int i3) {
        int i4;
        int i5 = z ? rect.top : rect.left;
        if (z) {
            i4 = i2 - rect.bottom;
        } else {
            i4 = i - rect.right;
        }
        return (i5 + ((i4 - i5) / 2)) - (i3 / 2);
    }
}
