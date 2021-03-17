package com.android.systemui.statusbar.policy;

import com.android.systemui.Dumpable;

public interface SecurityController extends CallbackController<SecurityControllerCallback>, Dumpable {

    public interface SecurityControllerCallback {
        void onStateChanged();
    }

    CharSequence getDeviceOwnerOrganizationName();

    String getPrimaryVpnName();

    CharSequence getWorkProfileOrganizationName();

    String getWorkProfileVpnName();

    boolean hasCACertInCurrentUser();

    boolean hasCACertInWorkProfile();

    boolean hasWorkProfile();

    boolean isDeviceManaged();

    boolean isNetworkLoggingEnabled();

    boolean isProfileOwnerOfOrganizationOwnedDevice();

    boolean isSilentVpnPackage();

    boolean isVpnBranded();

    boolean isVpnEnabled();
}
