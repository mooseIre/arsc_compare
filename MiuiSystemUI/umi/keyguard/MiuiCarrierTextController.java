package com.android.keyguard;

import android.content.Context;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.IccCardConstants;
import com.android.systemui.C0018R$string;
import com.android.systemui.Dependency;
import com.android.systemui.MCCUtils;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.policy.CarrierObserver;
import com.android.systemui.statusbar.policy.CustomCarrierObserver;
import com.android.systemui.statusbar.policy.NetworkController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import miui.telephony.SubscriptionInfo;
import miui.telephony.SubscriptionManager;

public class MiuiCarrierTextController implements CustomCarrierObserver.Callback, CarrierObserver.Callback, NetworkController.EmergencyListener, NetworkController.SignalCallback {
    protected ArrayList<CarrierTextListener> listeners = new ArrayList<>();
    protected boolean mAirplane;
    protected final KeyguardUpdateMonitorCallback mCallback = new KeyguardUpdateMonitorCallback() {
        public void onRefreshCarrierInfo() {
            MiuiCarrierTextController miuiCarrierTextController = MiuiCarrierTextController.this;
            miuiCarrierTextController.mMainHandler.post(miuiCarrierTextController.updateCarrierTextRunnable);
        }

        public void onTelephonyCapable(boolean z) {
            MiuiCarrierTextController miuiCarrierTextController = MiuiCarrierTextController.this;
            miuiCarrierTextController.mMainHandler.post(miuiCarrierTextController.updateCarrierTextRunnable);
        }

        public void onSimStateChanged(int i, final int i2, final int i3) {
            MiuiCarrierTextController.this.mMainHandler.post(new Runnable() {
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
    protected TelephonyManager mPhone;
    protected final int mPhoneCount;
    protected boolean[] mSimError;
    protected SubscriptionManager mSubscriptionManager;
    protected boolean[] mVowifi;
    protected final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() {
        public void onStartedWakingUp() {
            MiuiCarrierTextController.this.mMainHandler.post(new Runnable() {
                public void run() {
                    MiuiCarrierTextController.this.fireStartedWakingUp();
                }
            });
        }

        public void onFinishedGoingToSleep() {
            MiuiCarrierTextController.this.mMainHandler.post(new Runnable() {
                public void run() {
                    MiuiCarrierTextController.this.fireFinishedGoingToSleep();
                }
            });
        }
    };
    protected Runnable updateCarrierTextRunnable = new Runnable() {
        public void run() {
            MiuiCarrierTextController.this.updateCarrierText();
        }
    };

    public interface CarrierTextListener {
        void onCarrierTextChanged(String str);

        void onFinishedGoingToSleep() {
        }

        void onStartedWakingUp() {
        }
    }

    public MiuiCarrierTextController(Context context, Handler handler, Handler handler2) {
        this.mContext = context;
        this.mMainHandler = handler;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mPhone = telephonyManager;
        this.mPhoneCount = telephonyManager.getActiveModemCount();
        Log.d("MiuiCarrierTextController", "MiuiCarrierTextController: " + this.mPhoneCount);
        int i = this.mPhoneCount;
        this.mSimError = new boolean[i];
        this.mVowifi = new boolean[i];
        this.mSubscriptionManager = SubscriptionManager.getDefault();
        this.mCustomCarrierObserver = (CustomCarrierObserver) Dependency.get(CustomCarrierObserver.class);
        this.mCarrierObserver = (CarrierObserver) Dependency.get(CarrierObserver.class);
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        NetworkController networkController = (NetworkController) Dependency.get(NetworkController.class);
        networkController.addCallback(this);
        networkController.addEmergencyListener(this);
        this.mKeyguardUpdateMonitor.registerCallback(this.mCallback);
        this.mCustomCarrierObserver.addCallback(this);
        this.mCarrierObserver.addCallback(this);
        ((WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class)).addObserver(this.mWakefulnessObserver);
    }

    public void onCustomCarrierChanged(String[] strArr) {
        this.mCustomCarrier = (String[]) Arrays.copyOf(strArr, strArr.length);
        this.updateCarrierTextRunnable.run();
    }

    public void onCarrierChanged(String[] strArr) {
        this.mCarrier = (String[]) Arrays.copyOf(strArr, strArr.length);
        this.updateCarrierTextRunnable.run();
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
            }
            i++;
        }
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
            str2 = this.mContext.getResources().getString(C0018R$string.lock_screen_no_sim_card_emergency_only);
        } else if (TextUtils.isEmpty(str2)) {
            str2 = this.mContext.getResources().getString(C0018R$string.lock_screen_no_sim_card_no_service);
        }
        Log.d("MiuiCarrierTextController", "updateCarrierText: " + str2);
        fireCarrierTextChanged(str2);
    }

    private boolean dealCarrierNameForAirplane(String[] strArr) {
        int i;
        List activeSubscriptionInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList == null) {
            Log.e(CarrierText.class.getSimpleName(), " subscriptions is null");
            return true;
        }
        boolean[] zArr = new boolean[this.mPhoneCount];
        Iterator it = activeSubscriptionInfoList.iterator();
        boolean z = true;
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            SubscriptionInfo subscriptionInfo = (SubscriptionInfo) it.next();
            int slotId = subscriptionInfo.getSlotId();
            if (slotId >= 0 && slotId < this.mPhoneCount && this.mVowifi[slotId]) {
                if (MCCUtils.isShowSpnWhenAirplaneOn(this.mContext, this.mPhone.getSimOperatorNumericForPhone(slotId)) || MCCUtils.isShowSpnByGidWhenAirplaneOn(this.mContext, this.mPhone.getSimOperatorNumericForPhone(slotId), this.mPhone.getGroupIdLevel1(subscriptionInfo.getSubscriptionId()))) {
                    zArr[slotId] = true;
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
        return this.mContext.getString(C0018R$string.lock_screen_carrier_airplane_mode_on);
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
        this.mCurrentCarrier = str;
        int size = this.listeners.size();
        for (int i = 0; i < size; i++) {
            this.listeners.get(i).onCarrierTextChanged(str);
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
    public boolean isSimErrorByIccState(IccCardConstants.State state) {
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

    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        final boolean z = iconState.visible;
        this.mMainHandler.post(new Runnable() {
            public void run() {
                MiuiCarrierTextController miuiCarrierTextController = MiuiCarrierTextController.this;
                miuiCarrierTextController.mAirplane = z;
                miuiCarrierTextController.updateCarrierTextRunnable.run();
            }
        });
    }

    public void setEmergencyCallsOnly(final boolean z) {
        Log.d("MiuiCarrierTextController", "setEmergencyCallsOnly: " + z);
        this.mMainHandler.post(new Runnable() {
            public void run() {
                MiuiCarrierTextController miuiCarrierTextController = MiuiCarrierTextController.this;
                miuiCarrierTextController.mEmergencyOnly = z;
                miuiCarrierTextController.updateCarrierTextRunnable.run();
            }
        });
    }
}
