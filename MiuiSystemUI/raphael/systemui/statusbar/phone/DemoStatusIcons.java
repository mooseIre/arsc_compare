package com.android.systemui.statusbar.phone;

import com.android.systemui.DemoMode;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.StatusIconDisplayable;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;

public class DemoStatusIcons extends StatusIconContainer implements DemoMode, DarkIconDispatcher.DarkReceiver {
    public abstract void addDemoWifiView(StatusBarSignalPolicy.WifiIconState wifiIconState);

    public abstract void addMobileView(StatusBarSignalPolicy.MobileIconState mobileIconState);

    public abstract void onRemoveIcon(StatusIconDisplayable statusIconDisplayable);

    public abstract void updateMobileState(StatusBarSignalPolicy.MobileIconState mobileIconState);

    public abstract void updateWifiState(StatusBarSignalPolicy.WifiIconState wifiIconState);
}
