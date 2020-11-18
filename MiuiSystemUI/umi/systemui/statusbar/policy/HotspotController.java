package com.android.systemui.statusbar.policy;

import com.android.systemui.Dumpable;

public interface HotspotController extends CallbackController<Callback>, Dumpable {

    public interface Callback {
        void onHotspotAvailabilityChanged(boolean z) {
        }

        void onHotspotChanged(boolean z);
    }

    boolean isHotspotEnabled();

    boolean isHotspotReady();

    boolean isHotspotSupported();

    boolean isHotspotTransient();

    void setHotspotEnabled(boolean z);
}
