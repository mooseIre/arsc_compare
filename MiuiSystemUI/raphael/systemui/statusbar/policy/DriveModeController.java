package com.android.systemui.statusbar.policy;

import com.android.systemui.Dumpable;

/* compiled from: DriveModeController.kt */
public interface DriveModeController extends CallbackController<DriveModeListener>, Dumpable {

    /* compiled from: DriveModeController.kt */
    public interface DriveModeListener {
        void onDriveModeChanged();
    }

    boolean isDriveModeAvailable();

    boolean isDriveModeEnabled();

    boolean isMiuiLabDriveModeOn();

    void setDriveModeEnabled(boolean z);
}
