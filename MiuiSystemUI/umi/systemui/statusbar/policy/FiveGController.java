package com.android.systemui.statusbar.policy;

import com.android.systemui.statusbar.policy.SignalController;
import java.io.PrintWriter;

public interface FiveGController extends CallbackController<FiveGStateChangeCallback> {

    public interface FiveGStateChangeCallback {
        int getSlot();

        void onSignalStrengthChanged(int i, SignalController.IconGroup iconGroup);
    }

    void dump(PrintWriter printWriter);

    int getFiveGDrawable(int i) {
        return 0;
    }

    boolean isConnectedOnSaMode(int i) {
        return false;
    }

    boolean isFiveGBearerAllocated(int i);

    boolean isFiveGConnect(int i, int i2);
}
