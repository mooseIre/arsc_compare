package com.android.systemui.statusbar.policy;

import android.graphics.Rect;
import android.view.View;
import com.android.systemui.Dependency;

public abstract class DarkIconDispatcherHelper implements DarkIconDispatcher {
    public static int getTint(Rect rect, View view, int i) {
        if (isInArea(rect, view)) {
            return i;
        }
        return ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).getLightTintColor();
    }

    public static float getDarkIntensity(Rect rect, View view, float f) {
        if (isInArea(rect, view)) {
            return f;
        }
        return 0.0f;
    }

    public static boolean inDarkMode(Rect rect, View view, float f) {
        return getDarkIntensity(rect, view, f) > 0.0f;
    }

    public static boolean isInArea(Rect rect, View view) {
        if (rect.isEmpty()) {
            return true;
        }
        DarkIconDispatcher.sTmpRect.set(rect);
        view.getLocationOnScreen(DarkIconDispatcher.sTmpInt2);
        int i = DarkIconDispatcher.sTmpInt2[0];
        int max = Math.max(0, Math.min(i + view.getWidth(), rect.right) - Math.max(i, rect.left));
        boolean z = rect.top <= 0;
        if (!(max * 2 > view.getWidth()) || !z) {
            return false;
        }
        return true;
    }
}
