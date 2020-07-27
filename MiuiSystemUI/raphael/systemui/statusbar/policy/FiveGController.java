package com.android.systemui.statusbar.policy;

import java.io.PrintWriter;

public interface FiveGController extends CallbackController<FiveGStateChangeCallback> {

    public interface FiveGStateChangeCallback {
    }

    void dump(PrintWriter printWriter);

    int getFiveGDrawable(int i) {
        return 0;
    }

    boolean isFiveGConnect(int i, int i2);
}
