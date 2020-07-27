package com.android.systemui.statusbar.policy;

public interface DeviceProvisionedController extends CallbackController<DeviceProvisionedListener> {

    public interface DeviceProvisionedListener {
        void onDeviceProvisionedChanged() {
        }

        void onUserSetupChanged() {
        }

        void onUserSwitched() {
        }
    }

    int getCurrentUser();

    boolean isCurrentUserSetup();

    boolean isDeviceProvisioned();

    boolean isUserSetup(int i);
}
