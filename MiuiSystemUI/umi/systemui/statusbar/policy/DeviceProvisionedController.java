package com.android.systemui.statusbar.policy;

public interface DeviceProvisionedController extends CallbackController<DeviceProvisionedListener> {
    int getCurrentUser();

    boolean isDeviceProvisioned();

    boolean isUserSetup(int i);

    boolean isCurrentUserSetup() {
        return isUserSetup(getCurrentUser());
    }

    public interface DeviceProvisionedListener {
        void onDeviceProvisionedChanged() {
        }

        void onUserSetupChanged() {
        }

        void onUserSwitched() {
            onUserSetupChanged();
        }
    }
}
