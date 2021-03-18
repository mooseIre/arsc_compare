package com.android.systemui.statusbar.policy;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SubscriptionInfo;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.statusbar.policy.MobileSignalController;
import com.android.systemui.statusbar.policy.NetworkController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CallbackHandler extends Handler implements NetworkController.EmergencyListener, NetworkController.SignalCallback {
    private final ArrayList<NetworkController.EmergencyListener> mEmergencyListeners = new ArrayList<>();
    private final ArrayList<NetworkController.SignalCallback> mSignalCallbacks = new ArrayList<>();

    public CallbackHandler() {
        super(Looper.getMainLooper());
    }

    @VisibleForTesting
    CallbackHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message message) {
        int i = message.what;
        if (i != 100) {
            switch (i) {
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
                        it3.next().setNoSims(message.arg1 != 0, message.arg2 != 0);
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
                default:
                    return;
            }
        } else {
            Iterator<NetworkController.SignalCallback> it7 = this.mSignalCallbacks.iterator();
            while (it7.hasNext()) {
                it7.next().setIsDefaultDataSim(message.arg1, ((Boolean) message.obj).booleanValue());
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, int i, String str, boolean z4, String str2, boolean z5) {
        post(new Runnable(z, iconState, iconState2, z2, z3, i, str, z4, str2, z5) {
            /* class com.android.systemui.statusbar.policy.$$Lambda$CallbackHandler$teGtBh4s8e4F0kF88Yhtgulw40 */
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ boolean f$10;
            public final /* synthetic */ NetworkController.IconState f$2;
            public final /* synthetic */ NetworkController.IconState f$3;
            public final /* synthetic */ boolean f$4;
            public final /* synthetic */ boolean f$5;
            public final /* synthetic */ int f$6;
            public final /* synthetic */ String f$7;
            public final /* synthetic */ boolean f$8;
            public final /* synthetic */ String f$9;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
                this.f$9 = r10;
                this.f$10 = r11;
            }

            public final void run() {
                CallbackHandler.this.lambda$setWifiIndicators$0$CallbackHandler(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setWifiIndicators$0 */
    public /* synthetic */ void lambda$setWifiIndicators$0$CallbackHandler(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, int i, String str, boolean z4, String str2, boolean z5) {
        Iterator<NetworkController.SignalCallback> it = this.mSignalCallbacks.iterator();
        while (it.hasNext()) {
            it.next().setWifiIndicators(z, iconState, iconState2, z2, z3, i, str, z4, str2, z5);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4, MobileSignalController.MiuiMobileState miuiMobileState) {
        post(new Runnable(iconState, iconState2, i, i2, z, z2, i3, charSequence, charSequence2, charSequence3, z3, i4, z4, miuiMobileState) {
            /* class com.android.systemui.statusbar.policy.$$Lambda$CallbackHandler$TQfkWJYrezLiwOeHBSSxFOP4ACs */
            public final /* synthetic */ NetworkController.IconState f$1;
            public final /* synthetic */ CharSequence f$10;
            public final /* synthetic */ boolean f$11;
            public final /* synthetic */ int f$12;
            public final /* synthetic */ boolean f$13;
            public final /* synthetic */ MobileSignalController.MiuiMobileState f$14;
            public final /* synthetic */ NetworkController.IconState f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ int f$4;
            public final /* synthetic */ boolean f$5;
            public final /* synthetic */ boolean f$6;
            public final /* synthetic */ int f$7;
            public final /* synthetic */ CharSequence f$8;
            public final /* synthetic */ CharSequence f$9;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
                this.f$9 = r10;
                this.f$10 = r11;
                this.f$11 = r12;
                this.f$12 = r13;
                this.f$13 = r14;
                this.f$14 = r15;
            }

            public final void run() {
                CallbackHandler.this.lambda$setMobileDataIndicators$1$CallbackHandler(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setMobileDataIndicators$1 */
    public /* synthetic */ void lambda$setMobileDataIndicators$1$CallbackHandler(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4, MobileSignalController.MiuiMobileState miuiMobileState) {
        Iterator<NetworkController.SignalCallback> it = this.mSignalCallbacks.iterator();
        while (it.hasNext()) {
            it.next().setMobileDataIndicators(iconState, iconState2, i, i2, z, z2, i3, charSequence, charSequence2, charSequence3, z3, i4, z4, miuiMobileState);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setIsDefaultDataSim(int i, boolean z) {
        obtainMessage(100, i, 0, Boolean.valueOf(z)).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4, String str2) {
        post(new Runnable(z, iconState, iconState2, z2, z3, str, z4, str2) {
            /* class com.android.systemui.statusbar.policy.$$Lambda$CallbackHandler$4ydSKK9FqSMzYAAvkfeyBMRlZs */
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ NetworkController.IconState f$2;
            public final /* synthetic */ NetworkController.IconState f$3;
            public final /* synthetic */ boolean f$4;
            public final /* synthetic */ boolean f$5;
            public final /* synthetic */ String f$6;
            public final /* synthetic */ boolean f$7;
            public final /* synthetic */ String f$8;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
            }

            public final void run() {
                CallbackHandler.this.lambda$setWifiIndicators$2$CallbackHandler(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setWifiIndicators$2 */
    public /* synthetic */ void lambda$setWifiIndicators$2$CallbackHandler(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4, String str2) {
        Iterator<NetworkController.SignalCallback> it = this.mSignalCallbacks.iterator();
        while (it.hasNext()) {
            it.next().setWifiIndicators(z, iconState, iconState2, z2, z3, str, z4, str2);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4) {
        post(new Runnable(iconState, iconState2, i, i2, z, z2, i3, charSequence, charSequence2, charSequence3, z3, i4, z4) {
            /* class com.android.systemui.statusbar.policy.$$Lambda$CallbackHandler$4oXuOzEU1dJJfSIVy69kxPkm6g */
            public final /* synthetic */ NetworkController.IconState f$1;
            public final /* synthetic */ CharSequence f$10;
            public final /* synthetic */ boolean f$11;
            public final /* synthetic */ int f$12;
            public final /* synthetic */ boolean f$13;
            public final /* synthetic */ NetworkController.IconState f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ int f$4;
            public final /* synthetic */ boolean f$5;
            public final /* synthetic */ boolean f$6;
            public final /* synthetic */ int f$7;
            public final /* synthetic */ CharSequence f$8;
            public final /* synthetic */ CharSequence f$9;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
                this.f$9 = r10;
                this.f$10 = r11;
                this.f$11 = r12;
                this.f$12 = r13;
                this.f$13 = r14;
            }

            public final void run() {
                CallbackHandler.this.lambda$setMobileDataIndicators$3$CallbackHandler(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setMobileDataIndicators$3 */
    public /* synthetic */ void lambda$setMobileDataIndicators$3$CallbackHandler(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4) {
        Iterator<NetworkController.SignalCallback> it = this.mSignalCallbacks.iterator();
        while (it.hasNext()) {
            it.next().setMobileDataIndicators(iconState, iconState2, i, i2, z, z2, i3, charSequence, charSequence2, charSequence3, z3, i4, z4);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setSubs(List<SubscriptionInfo> list) {
        obtainMessage(1, list).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setNoSims(boolean z, boolean z2) {
        obtainMessage(2, z ? 1 : 0, z2 ? 1 : 0).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataEnabled(boolean z) {
        obtainMessage(5, z ? 1 : 0, 0).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.EmergencyListener
    public void setEmergencyCallsOnly(boolean z) {
        obtainMessage(0, z ? 1 : 0, 0).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setEthernetIndicators(NetworkController.IconState iconState) {
        obtainMessage(3, iconState).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        obtainMessage(4, iconState).sendToTarget();
    }

    public void setListening(NetworkController.EmergencyListener emergencyListener, boolean z) {
        obtainMessage(6, z ? 1 : 0, 0, emergencyListener).sendToTarget();
    }

    public void setListening(NetworkController.SignalCallback signalCallback, boolean z) {
        obtainMessage(7, z ? 1 : 0, 0, signalCallback).sendToTarget();
    }
}
