package com.android.systemui.statusbar;

import com.android.systemui.plugins.statusbar.StatusBarStateController;

public interface SysuiStatusBarStateController extends StatusBarStateController {
    boolean setIsDozing(boolean z);

    boolean setState(int i);

    public static class RankedListener {
        final StatusBarStateController.StateListener mListener;
        /* access modifiers changed from: package-private */
        public final int mRank;

        RankedListener(StatusBarStateController.StateListener stateListener, int i) {
            this.mListener = stateListener;
            this.mRank = i;
        }
    }
}
