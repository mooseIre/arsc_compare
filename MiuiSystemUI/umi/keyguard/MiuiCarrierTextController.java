package com.android.keyguard;

import android.content.Context;
import android.os.Handler;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.IccCardConstants;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.MCCUtils;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.policy.CarrierObserver;
import com.android.systemui.statusbar.policy.CustomCarrierObserver;
import com.android.systemui.statusbar.policy.MobileSignalController;
import com.android.systemui.statusbar.policy.NetworkController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import miui.os.Build;

public class MiuiCarrierTextController implements CustomCarrierObserver.Callback, CarrierObserver.Callback, NetworkController.EmergencyListener, NetworkController.SignalCallback {
    protected ArrayList<CarrierTextListener> listeners = new ArrayList<>();
    protected boolean mAirplane;
    protected final KeyguardUpdateMonitorCallback mCallback = new KeyguardUpdateMonitorCallback() {
        /* class com.android.keyguard.MiuiCarrierTextController.AnonymousClass3 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onRefreshCarrierInfo() {
            MiuiCarrierTextController miuiCarrierTextController = MiuiCarrierTextController.this;
            miuiCarrierTextController.mMainHandler.post(miuiCarrierTextController.updateCarrierTextRunnable);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTelephonyCapable(boolean z) {
            MiuiCarrierTextController miuiCarrierTextController = MiuiCarrierTextController.this;
            miuiCarrierTextController.mMainHandler.post(miuiCarrierTextController.updateCarrierTextRunnable);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onSimStateChanged(int i, final int i2, final int i3) {
            MiuiCarrierTextController.this.mMainHandler.post(new Runnable() {
                /* class com.android.keyguard.MiuiCarrierTextController.AnonymousClass3.AnonymousClass1 */

                public void run() {
                    int i = i2;
                    if (i >= 0) {
                        MiuiCarrierTextController miuiCarrierTextController = MiuiCarrierTextController.this;
                        if (i <= miuiCarrierTextController.mPhoneCount) {
                            miuiCarrierTextController.mSimError[i] = miuiCarrierTextController.isSimErrorByIccState(IccCardConstants.State.intToState(i3));
                            MiuiCarrierTextController.this.updateCarrierTextRunnable.run();
                        }
                    }
                }
            });
        }
    };
    protected String[] mCarrier;
    protected CarrierObserver mCarrierObserver;
    protected Context mContext;
    protected String mCurrentCarrier;
    protected String[] mCustomCarrier;
    protected CustomCarrierObserver mCustomCarrierObserver;
    protected boolean mEmergencyOnly;
    protected KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    protected Handler mMainHandler;
    protected String[] mMiuiMobileTypeName;
    protected TelephonyManager mPhone;
    protected final int mPhoneCount;
    protected boolean[] mSimError;
    protected SubscriptionManager mSubscriptionManager;
    protected boolean[] mVowifi;
    protected final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() {
        /* class com.android.keyguard.MiuiCarrierTextController.AnonymousClass1 */

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedWakingUp() {
            MiuiCarrierTextController.this.mMainHandler.post(new Runnable() {
                /* class com.android.keyguard.MiuiCarrierTextController.AnonymousClass1.AnonymousClass1 */

                public void run() {
                    MiuiCarrierTextController.this.fireStartedWakingUp();
                }
            });
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedGoingToSleep() {
            MiuiCarrierTextController.this.mMainHandler.post(new Runnable() {
                /* class com.android.keyguard.MiuiCarrierTextController.AnonymousClass1.AnonymousClass2 */

                public void run() {
                    MiuiCarrierTextController.this.fireFinishedGoingToSleep();
                }
            });
        }
    };
    protected Runnable updateCarrierTextRunnable = new Runnable() {
        /* class com.android.keyguard.MiuiCarrierTextController.AnonymousClass2 */

        public void run() {
            MiuiCarrierTextController.this.updateCarrierText();
        }
    };

    public interface CarrierTextListener {
        void onCarrierTextChanged(String str);

        default void onFinishedGoingToSleep() {
        }

        default void onStartedWakingUp() {
        }
    }

    public MiuiCarrierTextController(Context context, Handler handler, Handler handler2) {
        this.mContext = context;
        this.mMainHandler = handler;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mPhone = telephonyManager;
        int activeModemCount = telephonyManager.getActiveModemCount();
        this.mPhoneCount = activeModemCount;
        this.mMiuiMobileTypeName = new String[activeModemCount];
        this.mSimError = new boolean[activeModemCount];
        this.mVowifi = new boolean[activeModemCount];
        this.mSubscriptionManager = SubscriptionManager.from(context);
        this.mCustomCarrierObserver = (CustomCarrierObserver) Dependency.get(CustomCarrierObserver.class);
        this.mCarrierObserver = (CarrierObserver) Dependency.get(CarrierObserver.class);
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        NetworkController networkController = (NetworkController) Dependency.get(NetworkController.class);
        networkController.addCallback((NetworkController.SignalCallback) this);
        networkController.addEmergencyListener(this);
        this.mKeyguardUpdateMonitor.registerCallback(this.mCallback);
        this.mCustomCarrierObserver.addCallback(this);
        this.mCarrierObserver.addCallback(this);
        ((WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class)).addObserver(this.mWakefulnessObserver);
    }

    @Override // com.android.systemui.statusbar.policy.CustomCarrierObserver.Callback
    public void onCustomCarrierChanged(String[] strArr) {
        this.mCustomCarrier = (String[]) Arrays.copyOf(strArr, strArr.length);
        this.updateCarrierTextRunnable.run();
    }

    @Override // com.android.systemui.statusbar.policy.CarrierObserver.Callback
    public void onCarrierChanged(String[] strArr) {
        this.mCarrier = (String[]) Arrays.copyOf(strArr, strArr.length);
        this.updateCarrierTextRunnable.run();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4, MobileSignalController.MiuiMobileState miuiMobileState) {
        if (isCustomizationTest()) {
            int i5 = miuiMobileState.slotId;
            if (i5 >= 0) {
                String[] strArr = this.mMiuiMobileTypeName;
                if (i5 < strArr.length) {
                    strArr[i5] = miuiMobileState.showName;
                }
            }
            this.updateCarrierTextRunnable.run();
        }
    }

    /* access modifiers changed from: protected */
    public void updateCarrierText() {
        String str;
        String str2;
        String[] strArr = new String[this.mPhoneCount];
        int i = 0;
        while (true) {
            str = "";
            if (i >= this.mPhoneCount) {
                break;
            }
            if (this.mSimError[i] || !this.mPhone.hasIccCard(i)) {
                strArr[i] = str;
            } else {
                strArr[i] = getSimCarrier(i);
                if (!TextUtils.isEmpty(strArr[i]) && !TextUtils.isEmpty(this.mMiuiMobileTypeName[i]) && isCustomizationTest()) {
                    strArr[i] = strArr[i] + this.mMiuiMobileTypeName[i];
                }
            }
            i++;
        }
        dealCarrierNameForDisableCard(strArr);
        if (!this.mAirplane || !dealCarrierNameForAirplane(strArr)) {
            boolean z = true;
            for (int i2 = 0; i2 < this.mPhoneCount; i2++) {
                if (!TextUtils.isEmpty(strArr[i2])) {
                    if (z) {
                        str = strArr[i2];
                        z = false;
                    } else {
                        str = str + " | " + strArr[i2];
                    }
                }
            }
            str2 = str;
        } else {
            str2 = getAirplaneModeMessage();
        }
        if (this.mEmergencyOnly) {
            str2 = this.mContext.getResources().getString(C0021R$string.lock_screen_no_sim_card_emergency_only);
        } else if (TextUtils.isEmpty(str2)) {
            str2 = this.mContext.getResources().getString(C0021R$string.lock_screen_no_sim_card_no_service);
        }
        Log.d("MiuiCarrierTextController", "updateCarrierText: " + str2);
        fireCarrierTextChanged(str2);
    }

    private void dealCarrierNameForDisableCard(String[] strArr) {
        int simSlotIndex;
        if (strArr != null) {
            for (SubscriptionInfo subscriptionInfo : this.mSubscriptionManager.getCompleteActiveSubscriptionInfoList()) {
                if (!subscriptionInfo.areUiccApplicationsEnabled() && (simSlotIndex = subscriptionInfo.getSimSlotIndex()) >= 0 && simSlotIndex < strArr.length) {
                    strArr[simSlotIndex] = "";
                }
            }
        }
    }

    private boolean dealCarrierNameForAirplane(String[] strArr) {
        int i;
        if (strArr == null) {
            return true;
        }
        List completeActiveSubscriptionInfoList = this.mSubscriptionManager.getCompleteActiveSubscriptionInfoList();
        boolean[] zArr = new boolean[this.mPhoneCount];
        Iterator it = completeActiveSubscriptionInfoList.iterator();
        boolean z = true;
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            SubscriptionInfo subscriptionInfo = (SubscriptionInfo) it.next();
            int simSlotIndex = subscriptionInfo.getSimSlotIndex();
            if (simSlotIndex >= 0 && simSlotIndex < this.mPhoneCount && this.mVowifi[simSlotIndex] && !TextUtils.isEmpty(strArr[simSlotIndex])) {
                if (MCCUtils.isShowSpnWhenAirplaneOn(this.mContext, this.mPhone.getSimOperatorNumericForPhone(simSlotIndex)) || MCCUtils.isShowSpnByGidWhenAirplaneOn(this.mContext, this.mPhone.getSimOperatorNumericForPhone(simSlotIndex), this.mPhone.getGroupIdLevel1(subscriptionInfo.getSubscriptionId()))) {
                    zArr[simSlotIndex] = true;
                    z = false;
                }
            }
        }
        if (!z) {
            for (i = 0; i < this.mPhoneCount; i++) {
                if (!zArr[i]) {
                    strArr[i] = "";
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public String getAirplaneModeMessage() {
        return this.mContext.getString(C0021R$string.lock_screen_carrier_airplane_mode_on);
    }

    /* access modifiers changed from: protected */
    public String getSimCarrier(int i) {
        String[] strArr = this.mCustomCarrier;
        if (strArr != null && i >= 0 && i < strArr.length && !TextUtils.isEmpty(strArr[i])) {
            return this.mCustomCarrier[i];
        }
        String[] strArr2 = this.mCarrier;
        return (strArr2 == null || i < 0 || i >= strArr2.length || TextUtils.isEmpty(strArr2[i])) ? "" : this.mCarrier[i];
    }

    public void setVowifi(int i, boolean z) {
        boolean[] zArr = this.mVowifi;
        if (zArr != null && i >= 0 && i < zArr.length) {
            zArr[i] = z;
        }
        this.updateCarrierTextRunnable.run();
    }

    public void addCallback(CarrierTextListener carrierTextListener) {
        if (carrierTextListener != null) {
            if (!this.listeners.contains(carrierTextListener)) {
                this.listeners.add(carrierTextListener);
            }
            carrierTextListener.onCarrierTextChanged(this.mCurrentCarrier);
        }
    }

    public void removeCallback(CarrierTextListener carrierTextListener) {
        this.listeners.remove(carrierTextListener);
    }

    public void fireCarrierTextChanged(String str) {
        if (!TextUtils.equals(this.mCurrentCarrier, str)) {
            this.mCurrentCarrier = str;
            int size = this.listeners.size();
            for (int i = 0; i < size; i++) {
                this.listeners.get(i).onCarrierTextChanged(str);
            }
        }
    }

    public void fireStartedWakingUp() {
        int size = this.listeners.size();
        for (int i = 0; i < size; i++) {
            this.listeners.get(i).onStartedWakingUp();
        }
    }

    public void fireFinishedGoingToSleep() {
        int size = this.listeners.size();
        for (int i = 0; i < size; i++) {
            this.listeners.get(i).onFinishedGoingToSleep();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSimErrorByIccState(IccCardConstants.State state) {
        if (state == null) {
            return false;
        }
        if (!this.mKeyguardUpdateMonitor.isDeviceProvisioned() && (state == IccCardConstants.State.ABSENT || state == IccCardConstants.State.PERM_DISABLED)) {
            state = IccCardConstants.State.NETWORK_LOCKED;
        }
        if (state == IccCardConstants.State.READY) {
            return false;
        }
        return true;
    }

    private boolean isCustomizationTest() {
        return Build.IS_CM_CUSTOMIZATION_TEST || Build.IS_CU_CUSTOMIZATION_TEST || Build.IS_CT_CUSTOMIZATION_TEST;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        final boolean z = iconState.visible;
        this.mMainHandler.post(new Runnable() {
            /* class com.android.keyguard.MiuiCarrierTextController.AnonymousClass4 */

            public void run() {
                MiuiCarrierTextController miuiCarrierTextController = MiuiCarrierTextController.this;
                miuiCarrierTextController.mAirplane = z;
                miuiCarrierTextController.updateCarrierTextRunnable.run();
            }
        });
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.EmergencyListener
    public void setEmergencyCallsOnly(final boolean z) {
        this.mMainHandler.post(new Runnable() {
            /* class com.android.keyguard.MiuiCarrierTextController.AnonymousClass5 */

            public void run() {
                MiuiCarrierTextController miuiCarrierTextController = MiuiCarrierTextController.this;
                miuiCarrierTextController.mEmergencyOnly = z;
                miuiCarrierTextController.updateCarrierTextRunnable.run();
            }
        });
    }
}
