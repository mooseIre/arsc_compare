package com.android.systemui.statusbar;

import com.android.systemui.plugins.DarkIconDispatcher;

public interface StatusIconDisplayable extends DarkIconDispatcher.DarkReceiver {
    String getSlot();

    int getVisibleState();

    default boolean isIconBlocked() {
        return false;
    }

    boolean isIconVisible();

    void setVisibleState(int i, boolean z);

    default void setVisibleState(int i) {
        setVisibleState(i, false);
    }
}
