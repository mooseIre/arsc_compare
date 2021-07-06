package com.android.systemui.statusbar;

import com.android.systemui.plugins.DarkIconDispatcher;

public interface StatusIconDisplayable extends DarkIconDispatcher.DarkReceiver {
    String getSlot();

    int getVisibleState();

    default boolean isIconBlocked() {
        return false;
    }

    boolean isIconVisible();

    default boolean isSignalView() {
        return false;
    }

    default void onDensityOrFontScaleChanged() {
    }

    void setDecorColor(int i);

    default void setDrip(boolean z) {
    }

    default void setMiuiBlocked(boolean z) {
    }

    void setStaticDrawableColor(int i);

    void setVisibleState(int i, boolean z);

    default void setVisibleState(int i) {
        setVisibleState(i, false);
    }
}
