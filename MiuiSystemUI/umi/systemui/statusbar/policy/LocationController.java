package com.android.systemui.statusbar.policy;

import android.content.Intent;

public interface LocationController extends CallbackController<LocationChangeCallback> {

    public interface LocationChangeCallback {
        default void onLocationActiveChanged(boolean z) {
        }

        default void onLocationSettingsChanged(boolean z) {
        }

        default void onLocationStatusChanged(Intent intent) {
        }
    }

    boolean isLocationActive();

    boolean isLocationEnabled();

    boolean setLocationEnabled(boolean z);
}
