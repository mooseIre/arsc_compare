package com.android.systemui.stackdivider;

import android.content.res.Resources;
import android.graphics.Rect;
import com.android.systemui.C0012R$dimen;

public class DockedDividerUtils {
    public static int invertDockSide(int i) {
        if (i == 1) {
            return 3;
        }
        if (i == 2) {
            return 4;
        }
        if (i != 3) {
            return i != 4 ? -1 : 2;
        }
        return 1;
    }

    public static void calculateBoundsForPosition(int i, int i2, Rect rect, int i3, int i4, int i5) {
        boolean z = false;
        rect.set(0, 0, i3, i4);
        if (i2 == 1) {
            rect.right = i;
        } else if (i2 == 2) {
            rect.bottom = i;
        } else if (i2 == 3) {
            rect.left = i + i5;
        } else if (i2 == 4) {
            rect.top = i + i5;
        }
        if (i2 == 1 || i2 == 2) {
            z = true;
        }
        sanitizeStackBounds(rect, z);
    }

    public static void sanitizeStackBounds(Rect rect, boolean z) {
        if (z) {
            int i = rect.left;
            int i2 = rect.right;
            if (i >= i2) {
                rect.left = i2 - 1;
            }
            int i3 = rect.top;
            int i4 = rect.bottom;
            if (i3 >= i4) {
                rect.top = i4 - 1;
                return;
            }
            return;
        }
        int i5 = rect.right;
        int i6 = rect.left;
        if (i5 <= i6) {
            rect.right = i6 + 1;
        }
        int i7 = rect.bottom;
        int i8 = rect.top;
        if (i7 <= i8) {
            rect.bottom = i8 + 1;
        }
    }

    public static int calculatePositionForBounds(Rect rect, int i, int i2) {
        int i3;
        if (i == 1) {
            return rect.right;
        }
        if (i == 2) {
            return rect.bottom;
        }
        if (i == 3) {
            i3 = rect.left;
        } else if (i != 4) {
            return 0;
        } else {
            i3 = rect.top;
        }
        return i3 - i2;
    }

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

    public static int getDividerInsets(Resources resources) {
        return resources.getDimensionPixelSize(C0012R$dimen.docked_stack_divider_insets);
    }

    public static int getDividerSize(Resources resources, int i) {
        return resources.getDimensionPixelSize(C0012R$dimen.docked_stack_divider_thickness) - (i * 2);
    }
}
