package com.android.systemui.statusbar.policy;

public interface DeviceProvisionedController extends CallbackController<DeviceProvisionedListener> {
    int getCurrentUser();

    boolean isDeviceProvisioned();

    boolean isUserSetup(int i);

    default boolean isCurrentUserSetup() {
        return isUserSetup(getCurrentUser());
    }

    public interface DeviceProvisionedListener {
        default void onDeviceProvisionedChanged() {
        }

        default void onUserSetupChanged() {
        }

        default void onUserSwitched() {
            onUserSetupChanged();
        }
    }
}
