package com.android.systemui.statusbar;

import com.android.systemui.plugins.DarkIconDispatcher;

public interface StatusIconDisplayable extends DarkIconDispatcher.DarkReceiver {
    String getSlot();

    int getVisibleState();

    boolean isIconBlocked() {
        return false;
    }

    boolean isIconVisible();

    void setLight(boolean z, int i) {
    }

    void setVisibleState(int i, boolean z);

    void setVisibleState(int i) {
        setVisibleState(i, false);
    }
}
