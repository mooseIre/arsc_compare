package com.android.systemui.statusbar.policy;

import java.io.PrintWriter;

public interface FiveGController extends CallbackController<FiveGStateChangeCallback> {

    public interface FiveGStateChangeCallback {
        int getSlot();

        void onSignalStrengthChanged(int i);
    }

    void dump(PrintWriter printWriter);

    boolean isFiveGBearerAllocated(int i);

    boolean isFiveGConnect(int i, int i2);
}
