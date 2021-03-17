package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.telephony.SubscriptionInfo;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.wifi.AccessPoint;
import com.android.systemui.DemoMode;
import com.android.systemui.statusbar.policy.MobileSignalController;
import java.util.List;

public interface NetworkController extends CallbackController<SignalCallback>, DemoMode {

    public interface AccessPointController {

        public interface AccessPointCallback {
            void onAccessPointsChanged(List<AccessPoint> list);

            void onSettingsActivityTriggered(Intent intent);
        }

        void addAccessPointCallback(AccessPointCallback accessPointCallback);

        boolean canConfigWifi();

        boolean connect(AccessPoint accessPoint);

        int getIcon(AccessPoint accessPoint);

        void removeAccessPointCallback(AccessPointCallback accessPointCallback);

        void scanForAccessPoints();
    }

    public interface EmergencyListener {
        void setEmergencyCallsOnly(boolean z);
    }

    void addCallback(SignalCallback signalCallback);

    void addEmergencyListener(EmergencyListener emergencyListener);

    AccessPointController getAccessPointController();

    DataSaverController getDataSaverController();

    DataUsageController getMobileDataController();

    String getMobileDataNetworkName();

    int getNumberSubscriptions();

    boolean hasEmergencyCryptKeeperText();

    boolean hasMobileDataFeature();

    boolean isRadioOn();

    void removeCallback(SignalCallback signalCallback);

    void setWifiEnabled(boolean z);

    public interface SignalCallback {
        default void setEthernetIndicators(IconState iconState) {
        }

        default void setIsAirplaneMode(IconState iconState) {
        }

        default void setIsDefaultDataSim(int i, boolean z) {
        }

        default void setMobileDataEnabled(boolean z) {
        }

        default void setMobileDataIndicators(IconState iconState, IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4) {
        }

        default void setNoSims(boolean z, boolean z2) {
        }

        default void setSubs(List<SubscriptionInfo> list) {
        }

        default void setWifiIndicators(boolean z, IconState iconState, IconState iconState2, boolean z2, boolean z3, String str, boolean z4, String str2) {
        }

        default void setWifiIndicators(boolean z, IconState iconState, IconState iconState2, boolean z2, boolean z3, int i, String str, boolean z4, String str2, boolean z5) {
            setWifiIndicators(z, iconState, iconState2, z2, z3, str, z4, str2);
        }

        default void setMobileDataIndicators(IconState iconState, IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4, MobileSignalController.MiuiMobileState miuiMobileState) {
            setMobileDataIndicators(iconState, iconState2, i, i2, z, z2, i3, charSequence, charSequence2, charSequence3, z3, i4, z4);
        }
    }

    public static class IconState {
        public final String contentDescription;
        public final int icon;
        public final boolean visible;

        public IconState(boolean z, int i, String str) {
            this.visible = z;
            this.icon = i;
            this.contentDescription = str;
        }

        public IconState(boolean z, int i, int i2, Context context) {
            this(z, i, context.getString(i2));
        }
    }
}
