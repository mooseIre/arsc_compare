package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.wifi.AccessPoint;
import com.android.systemui.DemoMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import miui.telephony.SubscriptionInfo;

public interface NetworkController extends CallbackController<SignalCallback>, DemoMode {

    public interface AccessPointController {

        public interface AccessPointCallback {
            void onAccessPointsChanged(List<AccessPoint> list);

            void onConnectionStart(AccessPoint accessPoint);

            void onSettingsActivityTriggered(Intent intent);
        }

        void addAccessPointCallback(AccessPointCallback accessPointCallback);

        boolean canConfigWifi();

        boolean connect(AccessPoint accessPoint);

        int getIcon(AccessPoint accessPoint);

        void removeAccessPointCallback(AccessPointCallback accessPointCallback);

        void scanForAccessPoints();

        void updateVerboseLoggingLevel();
    }

    public interface CarrierNameListener {
        void updateCarrierName(int i, String str);
    }

    public interface EmergencyListener {
        void setEmergencyCallsOnly(boolean z);
    }

    public interface MobileTypeListener {
        void updateMobileTypeName(int i, String str);
    }

    public interface SignalCallback {
        void setEthernetIndicators(IconState iconState) {
        }

        void setIsAirplaneMode(IconState iconState) {
        }

        void setIsDefaultDataSim(int i, boolean z) {
        }

        void setIsImsRegisted(int i, boolean z) {
        }

        void setMobileDataEnabled(boolean z) {
        }

        void setMobileDataIndicators(IconState iconState, IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, int i4, int i5, String str, String str2, boolean z3, int i6, boolean z4) {
        }

        void setNetworkNameVoice(int i, String str) {
        }

        void setNoSims(boolean z) {
        }

        void setSlaveWifiIndicators(boolean z, IconState iconState, IconState iconState2) {
        }

        void setSpeechHd(int i, boolean z) {
        }

        void setSubs(List<SubscriptionInfo> list) {
        }

        void setVolteNoService(int i, boolean z) {
        }

        void setVowifi(int i, boolean z) {
        }

        void setWifiIndicators(boolean z, IconState iconState, IconState iconState2, boolean z2, boolean z3, String str, boolean z4) {
        }

        void updateWifiGeneration(boolean z, int i) {
        }
    }

    void addCallback(SignalCallback signalCallback);

    void addCarrierNameListener(CarrierNameListener carrierNameListener);

    void addEmergencyListener(EmergencyListener emergencyListener);

    void addMobileTypeListener(MobileTypeListener mobileTypeListener);

    AccessPointController getAccessPointController();

    DataSaverController getDataSaverController();

    DataUsageController getMobileDataController();

    Resources getResourcesForOperator(int i);

    SignalState getSignalState();

    boolean hasEmergencyCryptKeeperText();

    boolean hasMobileDataFeature();

    boolean hasVoiceCallingFeature();

    boolean hideVolteForOperation(int i);

    boolean hideVowifiForOperation(int i);

    boolean isMobileDataSupported();

    boolean isMobileTypeShownWhenWifiOn(int i);

    boolean isRadioOn();

    void removeCallback(SignalCallback signalCallback);

    void removeCarrierNameListener(CarrierNameListener carrierNameListener);

    void removeEmergencyListener(EmergencyListener emergencyListener);

    void removeMobileTypeListener(MobileTypeListener mobileTypeListener);

    void setWifiEnabled(boolean z);

    public static class IconState {
        public final String contentDescription;
        public final int icon;
        public final int iconOverlay;
        public final boolean visible;

        public IconState(boolean z, int i, int i2, String str) {
            this.visible = z;
            this.icon = i;
            this.iconOverlay = i2;
            this.contentDescription = str;
        }

        public IconState(boolean z, int i, String str) {
            this(z, i, -1, str);
        }

        public IconState(boolean z, int i, int i2, Context context) {
            this(z, i, context.getString(i2));
        }
    }

    public static class SignalState {
        public Map<Integer, Boolean> imsMap = new HashMap();
        public Map<Integer, Boolean> speedHdMap = new HashMap();
        public Map<Integer, Boolean> vowifiMap = new HashMap();

        public void updateMap(ArrayList<Integer> arrayList, Map<Integer, Boolean> map) {
            if (arrayList != null && map != null) {
                Iterator<Map.Entry<Integer, Boolean>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    if (!arrayList.contains(it.next().getKey())) {
                        it.remove();
                    }
                }
            }
        }
    }
}
