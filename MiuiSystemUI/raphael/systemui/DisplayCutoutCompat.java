package com.android.systemui;

import android.content.Context;
import android.graphics.Rect;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.View;
import com.android.systemui.statusbar.phone.StatusBar;
import java.util.List;

public class DisplayCutoutCompat {
    public static int getSafeInsetLeft(StatusBar statusBar, DisplayInfo displayInfo) {
        DisplayCutout displayCutout = displayInfo.displayCutout;
        if (displayCutout == null) {
            return 0;
        }
        return displayCutout.getSafeInsetLeft();
    }

    public static int getSafeInsetRight(StatusBar statusBar, DisplayInfo displayInfo) {
        DisplayCutout displayCutout = displayInfo.displayCutout;
        if (displayCutout == null) {
            return 0;
        }
        return displayCutout.getSafeInsetRight();
    }

    public static int getHeight(DisplayInfo displayInfo) {
        DisplayCutout displayCutout = displayInfo.displayCutout;
        if (displayCutout == null) {
            return 0;
        }
        int safeInsetTop = displayCutout.getSafeInsetTop();
        int min = safeInsetTop > 0 ? Math.min(Integer.MAX_VALUE, safeInsetTop) : Integer.MAX_VALUE;
        int safeInsetBottom = displayCutout.getSafeInsetBottom();
        if (safeInsetBottom > 0) {
            min = Math.min(min, safeInsetBottom);
        }
        int safeInsetLeft = displayCutout.getSafeInsetLeft();
        if (safeInsetLeft > 0) {
            min = Math.min(min, safeInsetLeft);
        }
        int safeInsetRight = displayCutout.getSafeInsetRight();
        int min2 = safeInsetRight > 0 ? Math.min(min, safeInsetRight) : min;
        if (min2 == Integer.MAX_VALUE) {
            return 0;
        }
        return min2;
    }

    public static void boundsFromDirection(View view, int i, Rect rect) {
        DisplayCutout displayCutout = view.isAttachedToWindow() ? view.getRootWindowInsets().getDisplayCutout() : null;
        if (displayCutout != null) {
            boundsFromDirection(displayCutout, i, rect);
        }
    }

    public static void boundsFromDirection(DisplayCutout displayCutout, int i, Rect rect) {
        if (i == 3) {
            rect.set(displayCutout.getBoundingRectLeft());
        } else if (i == 5) {
            rect.set(displayCutout.getBoundingRectRight());
        } else if (i == 48) {
            rect.set(displayCutout.getBoundingRectTop());
        } else if (i != 80) {
            rect.setEmpty();
        } else {
            rect.set(displayCutout.getBoundingRectBottom());
        }
    }

    public static boolean hasCutout(View view) {
        return view.getRootWindowInsets().getDisplayCutout() != null;
    }

    public static int getCutoutWidth(DisplayInfo displayInfo, Context context) {
        List<Rect> boundingRects;
        DisplayCutout displayCutout = displayInfo.displayCutout;
        if (displayCutout == null || (boundingRects = displayCutout.getBoundingRects()) == null || boundingRects.isEmpty()) {
            return 0;
        }
        return boundingRects.get(0).width();
    }

    public static boolean isCutoutSymmetrical(DisplayInfo displayInfo, int i) {
        List<Rect> boundingRects;
        DisplayCutout displayCutout = displayInfo.displayCutout;
        if (displayCutout == null || (boundingRects = displayCutout.getBoundingRects()) == null || boundingRects.isEmpty()) {
            return true;
        }
        int i2 = i / 2;
        if (boundingRects.get(0).left >= i2 || i2 >= boundingRects.get(0).right) {
            return false;
        }
        return true;
    }

    public static boolean isCutoutLeftTop(DisplayInfo displayInfo, int i) {
        List<Rect> boundingRects;
        DisplayCutout displayCutout = displayInfo.displayCutout;
        if (displayCutout != null && (boundingRects = displayCutout.getBoundingRects()) != null && !boundingRects.isEmpty() && boundingRects.get(0).right < i / 2) {
            return true;
        }
        return false;
    }
}
