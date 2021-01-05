package com.android.systemui.statusbar.policy;

import android.content.Intent;

public interface LocationController extends CallbackController<LocationChangeCallback> {

    public interface LocationChangeCallback {
        void onLocationActiveChanged(boolean z) {
        }

        void onLocationSettingsChanged(boolean z) {
        }

        void onLocationStatusChanged(Intent intent) {
        }
    }

    boolean isLocationActive();

    boolean isLocationEnabled();

    boolean setLocationEnabled(boolean z);
}
