package com.android.systemui.statusbar.policy;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.statusbar.policy.NetworkController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import miui.telephony.SubscriptionInfo;

public class CallbackHandler extends Handler implements NetworkController.EmergencyListener, NetworkController.CarrierNameListener, NetworkController.SignalCallback, NetworkController.MobileTypeListener {
    private final ArrayList<NetworkController.CarrierNameListener> mCarrierNameListeners = new ArrayList<>();
    private final ArrayList<NetworkController.EmergencyListener> mEmergencyListeners = new ArrayList<>();
    private final ArrayList<NetworkController.MobileTypeListener> mMobileTypeListeners = new ArrayList<>();
    /* access modifiers changed from: private */
    public final ArrayList<NetworkController.SignalCallback> mSignalCallbacks = new ArrayList<>();

    public CallbackHandler() {
        super(Looper.getMainLooper());
    }

    @VisibleForTesting
    CallbackHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case 0:
                Iterator<NetworkController.EmergencyListener> it = this.mEmergencyListeners.iterator();
                while (it.hasNext()) {
                    it.next().setEmergencyCallsOnly(message.arg1 != 0);
                }
                return;
            case 1:
                Iterator<NetworkController.SignalCallback> it2 = this.mSignalCallbacks.iterator();
                while (it2.hasNext()) {
                    it2.next().setSubs((List) message.obj);
                }
                return;
            case 2:
                Iterator<NetworkController.SignalCallback> it3 = this.mSignalCallbacks.iterator();
                while (it3.hasNext()) {
                    it3.next().setNoSims(message.arg1 != 0);
                }
                return;
            case 3:
                Iterator<NetworkController.SignalCallback> it4 = this.mSignalCallbacks.iterator();
                while (it4.hasNext()) {
                    it4.next().setEthernetIndicators((NetworkController.IconState) message.obj);
                }
                return;
            case 4:
                Iterator<NetworkController.SignalCallback> it5 = this.mSignalCallbacks.iterator();
                while (it5.hasNext()) {
                    it5.next().setIsAirplaneMode((NetworkController.IconState) message.obj);
                }
                return;
            case 5:
                Iterator<NetworkController.SignalCallback> it6 = this.mSignalCallbacks.iterator();
                while (it6.hasNext()) {
                    it6.next().setMobileDataEnabled(message.arg1 != 0);
                }
                return;
            case 6:
                if (message.arg1 != 0) {
                    this.mEmergencyListeners.add((NetworkController.EmergencyListener) message.obj);
                    return;
                } else {
                    this.mEmergencyListeners.remove((NetworkController.EmergencyListener) message.obj);
                    return;
                }
            case 7:
                if (message.arg1 != 0) {
                    this.mSignalCallbacks.add((NetworkController.SignalCallback) message.obj);
                    return;
                } else {
                    this.mSignalCallbacks.remove((NetworkController.SignalCallback) message.obj);
                    return;
                }
            case 8:
                Iterator<NetworkController.SignalCallback> it7 = this.mSignalCallbacks.iterator();
                while (it7.hasNext()) {
                    it7.next().setIsImsRegisted(message.arg1, message.arg2 == 1);
                }
                return;
            case 9:
                Iterator<NetworkController.SignalCallback> it8 = this.mSignalCallbacks.iterator();
                while (it8.hasNext()) {
                    it8.next().setVolteNoService(message.arg1, message.arg2 == 1);
                }
                return;
            case 10:
                Iterator<NetworkController.SignalCallback> it9 = this.mSignalCallbacks.iterator();
                while (it9.hasNext()) {
                    it9.next().setSpeechHd(message.arg1, message.arg2 == 1);
                }
                return;
            case 11:
                Iterator<NetworkController.SignalCallback> it10 = this.mSignalCallbacks.iterator();
                while (it10.hasNext()) {
                    it10.next().setNetworkNameVoice(message.arg1, (String) message.obj);
                }
                return;
            case 12:
                Iterator<NetworkController.SignalCallback> it11 = this.mSignalCallbacks.iterator();
                while (it11.hasNext()) {
                    it11.next().setVowifi(message.arg1, message.arg2 == 1);
                }
                return;
            case 13:
                if (message.arg1 != 0) {
                    this.mCarrierNameListeners.add((NetworkController.CarrierNameListener) message.obj);
                    return;
                } else {
                    this.mCarrierNameListeners.remove((NetworkController.CarrierNameListener) message.obj);
                    return;
                }
            case 14:
                Iterator<NetworkController.CarrierNameListener> it12 = this.mCarrierNameListeners.iterator();
                while (it12.hasNext()) {
                    it12.next().updateCarrierName(message.arg1, (String) message.obj);
                }
                return;
            case 15:
                Iterator<NetworkController.SignalCallback> it13 = this.mSignalCallbacks.iterator();
                while (it13.hasNext()) {
                    it13.next().setIsDefaultDataSim(message.arg1, ((Boolean) message.obj).booleanValue());
                }
                return;
            case 16:
                if (message.arg1 != 0) {
                    this.mMobileTypeListeners.add((NetworkController.MobileTypeListener) message.obj);
                    return;
                } else {
                    this.mMobileTypeListeners.remove((NetworkController.MobileTypeListener) message.obj);
                    return;
                }
            case 17:
                Iterator<NetworkController.MobileTypeListener> it14 = this.mMobileTypeListeners.iterator();
                while (it14.hasNext()) {
                    it14.next().updateMobileTypeName(message.arg1, (String) message.obj);
                }
                return;
            default:
                return;
        }
    }

    public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4) {
        final boolean z5 = z;
        final NetworkController.IconState iconState3 = iconState;
        final NetworkController.IconState iconState4 = iconState2;
        final boolean z6 = z2;
        final boolean z7 = z3;
        final String str2 = str;
        final boolean z8 = z4;
        post(new Runnable() {
            public void run() {
                Iterator it = CallbackHandler.this.mSignalCallbacks.iterator();
                while (it.hasNext()) {
                    ((NetworkController.SignalCallback) it.next()).setWifiIndicators(z5, iconState3, iconState4, z6, z7, str2, z8);
                }
            }
        });
    }

    public void updateWifiGeneration(final boolean z, final int i) {
        post(new Runnable() {
            public void run() {
                Iterator it = CallbackHandler.this.mSignalCallbacks.iterator();
                while (it.hasNext()) {
                    ((NetworkController.SignalCallback) it.next()).updateWifiGeneration(z, i);
                }
            }
        });
    }

    public void setSlaveWifiIndicators(final boolean z, final NetworkController.IconState iconState, final NetworkController.IconState iconState2) {
        post(new Runnable() {
            public void run() {
                Iterator it = CallbackHandler.this.mSignalCallbacks.iterator();
                while (it.hasNext()) {
                    ((NetworkController.SignalCallback) it.next()).setSlaveWifiIndicators(z, iconState, iconState2);
                }
            }
        });
    }

    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, int i4, int i5, String str, String str2, boolean z3, int i6, boolean z4) {
        final NetworkController.IconState iconState3 = iconState;
        final NetworkController.IconState iconState4 = iconState2;
        final int i7 = i;
        final int i8 = i2;
        final boolean z5 = z;
        final boolean z6 = z2;
        final int i9 = i3;
        final int i10 = i4;
        final int i11 = i5;
        final String str3 = str;
        final String str4 = str2;
        final boolean z7 = z3;
        final int i12 = i6;
        AnonymousClass4 r16 = r0;
        final boolean z8 = z4;
        AnonymousClass4 r0 = new Runnable() {
            public void run() {
                for (Iterator it = CallbackHandler.this.mSignalCallbacks.iterator(); it.hasNext(); it = it) {
                    ((NetworkController.SignalCallback) it.next()).setMobileDataIndicators(iconState3, iconState4, i7, i8, z5, z6, i9, i10, i11, str3, str4, z7, i12, z8);
                }
            }
        };
        post(r16);
    }

    public void setSubs(List<SubscriptionInfo> list) {
        obtainMessage(1, list).sendToTarget();
    }

    public void setNoSims(boolean z) {
        obtainMessage(2, z ? 1 : 0, 0).sendToTarget();
    }

    public void setMobileDataEnabled(boolean z) {
        obtainMessage(5, z ? 1 : 0, 0).sendToTarget();
    }

    public void setEmergencyCallsOnly(boolean z) {
        obtainMessage(0, z ? 1 : 0, 0).sendToTarget();
    }

    public void updateCarrierName(int i, String str) {
        obtainMessage(14, i, 0, str).sendToTarget();
    }

    public void updateMobileTypeName(int i, String str) {
        obtainMessage(17, i, 0, str).sendToTarget();
    }

    public void setEthernetIndicators(NetworkController.IconState iconState) {
        obtainMessage(3, iconState).sendToTarget();
    }

    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        obtainMessage(4, iconState).sendToTarget();
    }

    public void setListening(NetworkController.EmergencyListener emergencyListener, boolean z) {
        obtainMessage(6, z ? 1 : 0, 0, emergencyListener).sendToTarget();
    }

    public void setListening(NetworkController.MobileTypeListener mobileTypeListener, boolean z) {
        obtainMessage(16, z ? 1 : 0, 0, mobileTypeListener).sendToTarget();
    }

    public void setListening(NetworkController.SignalCallback signalCallback, boolean z) {
        obtainMessage(7, z ? 1 : 0, 0, signalCallback).sendToTarget();
    }

    public void setIsImsRegisted(int i, boolean z) {
        obtainMessage(8, i, z ? 1 : 0).sendToTarget();
    }

    public void setVolteNoService(int i, boolean z) {
        obtainMessage(9, i, z ? 1 : 0).sendToTarget();
    }

    public void setSpeechHd(int i, boolean z) {
        obtainMessage(10, i, z ? 1 : 0).sendToTarget();
    }

    public void setVowifi(int i, boolean z) {
        obtainMessage(12, i, z ? 1 : 0).sendToTarget();
    }

    public void setNetworkNameVoice(int i, String str) {
        obtainMessage(11, i, 0, str).sendToTarget();
    }

    public void setIsDefaultDataSim(int i, boolean z) {
        obtainMessage(15, i, 0, Boolean.valueOf(z)).sendToTarget();
    }
}
