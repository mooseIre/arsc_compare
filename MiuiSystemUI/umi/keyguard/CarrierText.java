package com.android.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.TextView;
import com.android.internal.telephony.IccCardConstants;
import com.android.systemui.Dependency;
import com.android.systemui.MCCUtils;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.DarkIconDispatcherHelper;
import com.android.systemui.statusbar.policy.NetworkController;
import java.util.Iterator;
import java.util.List;
import miui.os.Build;
import miui.telephony.SubscriptionInfo;
import miui.telephony.SubscriptionManager;

public class CarrierText extends TextView implements NetworkController.CarrierNameListener, NetworkController.EmergencyListener, NetworkController.MobileTypeListener, DarkIconDispatcher.DarkReceiver, NetworkController.SignalCallback {
    /* access modifiers changed from: private */
    public boolean mAirplaneModeOn;
    private final BroadcastReceiver mBroadcastReceiver;
    private KeyguardUpdateMonitorCallback mCallback;
    /* access modifiers changed from: private */
    public String[] mCustomCarrier;
    private ContentObserver[] mCustomCarrierObserver;
    private boolean mEmergencyOnly;
    /* access modifiers changed from: private */
    public boolean mForceHide;
    /* access modifiers changed from: private */
    public boolean mHasCustomCarrier;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private int mMaxWidth;
    private String[] mMobileType;
    private TelephonyManager mPhone;
    /* access modifiers changed from: private */
    public final int mPhoneCount;
    /* access modifiers changed from: private */
    public boolean mShowCarrier;
    private ContentObserver mShowCarrierObserver;
    private boolean mShowEmergencyPreferentially;
    private boolean mShowSpnWhenAirplaneOn;
    /* access modifiers changed from: private */
    public int mShowStyle;
    /* access modifiers changed from: private */
    public String[] mSimCarrier;
    /* access modifiers changed from: private */
    public boolean[] mSimErrorState;
    private SubscriptionManager mSubscriptionManager;
    private boolean mSupportNetwork;
    private SparseBooleanArray mVowifiArray;

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        Class cls = NetworkController.class;
        super.onAttachedToWindow();
        this.mShowSpnWhenAirplaneOn = MCCUtils.getResourcesForOperation(this.mContext, "00000", false).getBoolean(R.bool.status_bar_show_spn_when_airplane);
        if (ConnectivityManager.from(this.mContext).isNetworkSupported(0)) {
            this.mSupportNetwork = true;
            KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(this.mContext);
            this.mKeyguardUpdateMonitor = instance;
            instance.registerCallback(this.mCallback);
            initCarrier();
            registerObservers();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
            ((NetworkController) Dependency.get(cls)).addCarrierNameListener(this);
            ((NetworkController) Dependency.get(cls)).addEmergencyListener(this);
            if (isCustomizationTest()) {
                ((NetworkController) Dependency.get(cls)).addMobileTypeListener(this);
            }
            ((NetworkController) Dependency.get(cls)).addCallback(this);
            return;
        }
        this.mSupportNetwork = false;
        this.mKeyguardUpdateMonitor = null;
        setText("");
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        setMeasuredDimension(Math.min(this.mMaxWidth, getMeasuredWidth()), getMeasuredHeight());
    }

    public void setMaxWidth(int i) {
        if (i > 0) {
            this.mMaxWidth = i;
        }
    }

    private void registerObservers() {
        Handler handler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mCustomCarrierObserver[i] = new ContentObserver(handler) {
                public void onChange(boolean z) {
                    super.onChange(z);
                    String str = "";
                    for (int i = 0; i < CarrierText.this.mPhoneCount; i++) {
                        CarrierText.this.mCustomCarrier[i] = MiuiSettings.System.getStringForUser(CarrierText.this.mContext.getContentResolver(), "status_bar_custom_carrier" + i, KeyguardUpdateMonitor.getCurrentUser());
                        if (!TextUtils.isEmpty(CarrierText.this.mCustomCarrier[i])) {
                            str = str + CarrierText.this.mCustomCarrier[i];
                        }
                    }
                    boolean unused = CarrierText.this.mHasCustomCarrier = !TextUtils.isEmpty(str);
                    CarrierText.this.updateCarrier();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_custom_carrier" + i), false, this.mCustomCarrierObserver[i], -1);
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_carrier_under_keyguard"), false, this.mShowCarrierObserver, -1);
        updateCarrier();
    }

    /* access modifiers changed from: private */
    public void initCarrier() {
        boolean z = false;
        if (ConnectivityManager.from(this.mContext).isNetworkSupported(0)) {
            this.mShowCarrier = Settings.System.getIntForUser(this.mContext.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1, KeyguardUpdateMonitor.getCurrentUser()) == 1;
            String str = "";
            for (int i = 0; i < this.mPhoneCount; i++) {
                this.mCustomCarrier[i] = MiuiSettings.System.getStringForUser(this.mContext.getContentResolver(), "status_bar_custom_carrier" + i, -2);
                if (!TextUtils.isEmpty(this.mCustomCarrier[i])) {
                    str = str + this.mCustomCarrier[i];
                }
            }
            this.mHasCustomCarrier = !TextUtils.isEmpty(str);
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1) {
                z = true;
            }
            this.mAirplaneModeOn = z;
            updateCarrier();
        }
    }

    public void unregisterObservers() {
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mCustomCarrierObserver[i]);
        }
        this.mContext.getContentResolver().unregisterContentObserver(this.mShowCarrierObserver);
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
    }

    private boolean isCustomizationTest() {
        return Build.IS_CM_CUSTOMIZATION_TEST || Build.IS_CU_CUSTOMIZATION_TEST || Build.IS_CT_CUSTOMIZATION_TEST;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        Class cls = NetworkController.class;
        super.onDetachedFromWindow();
        if (this.mSupportNetwork) {
            KeyguardUpdateMonitor keyguardUpdateMonitor = this.mKeyguardUpdateMonitor;
            if (keyguardUpdateMonitor != null) {
                keyguardUpdateMonitor.removeCallback(this.mCallback);
            }
            unregisterObservers();
            ((NetworkController) Dependency.get(cls)).removeEmergencyListener(this);
            ((NetworkController) Dependency.get(cls)).removeCarrierNameListener(this);
            if (isCustomizationTest()) {
                ((NetworkController) Dependency.get(cls)).removeMobileTypeListener(this);
            }
            ((NetworkController) Dependency.get(cls)).removeCallback(this);
        }
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this);
    }

    public CarrierText(Context context) {
        this(context, (AttributeSet) null);
    }

    public CarrierText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mShowStyle = 0;
        this.mMaxWidth = Integer.MAX_VALUE;
        this.mVowifiArray = new SparseBooleanArray();
        this.mCallback = new KeyguardUpdateMonitorCallback() {
            public void onRefreshCarrierInfo() {
                CarrierText.this.updateCarrier();
            }

            public void onAirplaneModeChanged() {
                CarrierText carrierText = CarrierText.this;
                boolean z = false;
                if (Settings.Global.getInt(carrierText.mContext.getContentResolver(), "airplane_mode_on", 0) == 1) {
                    z = true;
                }
                boolean unused = carrierText.mAirplaneModeOn = z;
                CarrierText.this.updateCarrier();
            }

            public void onFinishedGoingToSleep(int i) {
                CarrierText.this.setSelected(false);
            }

            public void onStartedWakingUp() {
                CarrierText.this.setSelected(true);
            }

            public void onSimStateChanged(int i, int i2, IccCardConstants.State state) {
                if (i2 < 0) {
                    Log.d("CarrierText", "onSimStateChanged() - slotId invalid: " + i2);
                } else if (CarrierText.this.isSimErrorByIccState(state)) {
                    CarrierText.this.mSimErrorState[i2] = true;
                    CarrierText.this.updateCarrier();
                } else {
                    CarrierText.this.mSimErrorState[i2] = false;
                    CarrierText.this.updateCarrier();
                }
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                    CarrierText.this.initCarrier();
                }
            }
        };
        this.mShowCarrierObserver = new ContentObserver(new Handler((Looper) Dependency.get(Dependency.BG_LOOPER))) {
            public void onChange(boolean z) {
                super.onChange(z);
                CarrierText carrierText = CarrierText.this;
                boolean z2 = true;
                if (Settings.System.getIntForUser(carrierText.mContext.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1, KeyguardUpdateMonitor.getCurrentUser()) != 1) {
                    z2 = false;
                }
                boolean unused = carrierText.mShowCarrier = z2;
                CarrierText.this.updateCarrier();
            }
        };
        this.mShowEmergencyPreferentially = context.getResources().getBoolean(R.bool.show_emergency_carrier_preferentially);
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        this.mPhoneCount = phoneCount;
        this.mSimErrorState = new boolean[phoneCount];
        this.mCustomCarrierObserver = new ContentObserver[phoneCount];
        this.mCustomCarrier = new String[phoneCount];
        this.mSimCarrier = new String[phoneCount];
        this.mMobileType = new String[phoneCount];
    }

    public void onDarkChanged(Rect rect, float f, int i) {
        setTextColor(DarkIconDispatcherHelper.getTint(rect, this, i));
    }

    public void updateCarrierName(int i, String str) {
        if (i < this.mPhoneCount) {
            this.mSimCarrier[i] = str;
        }
        updateCarrier();
    }

    public void setEmergencyCallsOnly(boolean z) {
        if (this.mEmergencyOnly != z) {
            this.mEmergencyOnly = z;
            updateCarrier();
        }
    }

    public void updateMobileTypeName(int i, String str) {
        if (i < this.mPhoneCount) {
            this.mMobileType[i] = str;
            updateCarrier();
        }
    }

    public void updateCarrier() {
        if (ConnectivityManager.from(this.mContext).isNetworkSupported(0)) {
            post(new Runnable() {
                public void run() {
                    String[] strArr = new String[CarrierText.this.mPhoneCount];
                    int i = 0;
                    for (int i2 = 0; i2 < CarrierText.this.mPhoneCount; i2++) {
                        if (!TelephonyManager.getDefault().hasIccCard(i2) || CarrierText.this.mSimErrorState[i2]) {
                            strArr[i2] = "";
                        } else if (!TextUtils.isEmpty(CarrierText.this.mCustomCarrier[i2])) {
                            strArr[i2] = CarrierText.this.mCustomCarrier[i2];
                        } else {
                            strArr[i2] = CarrierText.this.mSimCarrier[i2];
                        }
                    }
                    String access$1200 = CarrierText.this.getCarrierName(strArr);
                    if (!access$1200.equals(CarrierText.this.getText())) {
                        CarrierText.this.setText(access$1200);
                    }
                    if (CarrierText.this.mForceHide) {
                        CarrierText.this.setVisibility(8);
                    } else if (CarrierText.this.mShowStyle == -1) {
                        CarrierText.this.setVisibility(8);
                    } else if (CarrierText.this.mShowStyle == 1) {
                        CarrierText.this.setVisibility(0);
                    } else if (CarrierText.this.mShowStyle == 0) {
                        CarrierText carrierText = CarrierText.this;
                        if (!carrierText.mShowCarrier) {
                            i = 8;
                        }
                        carrierText.setVisibility(i);
                    }
                }
            });
        }
    }

    private boolean showAirplaneMode(String[] strArr) {
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
            if (this.mVowifiArray.get(slotId) && (this.mShowSpnWhenAirplaneOn || MCCUtils.isShowSpnWhenAirplaneOn(this.mContext, this.mPhone.getSimOperatorNumericForPhone(slotId)) || MCCUtils.isShowSpnByGidWhenAirplaneOn(this.mContext, this.mPhone.getSimOperatorNumericForPhone(slotId), this.mPhone.getGroupIdLevel1(subscriptionInfo.getSubscriptionId())))) {
                if (slotId >= 0 && slotId < this.mPhoneCount) {
                    zArr[slotId] = true;
                }
                z = false;
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

    /* access modifiers changed from: private */
    public String getCarrierName(String[] strArr) {
        if (this.mAirplaneModeOn && showAirplaneMode(strArr)) {
            return this.mContext.getResources().getString(R.string.lock_screen_carrier_airplane_mode_on);
        }
        StringBuilder sb = new StringBuilder();
        if (strArr != null) {
            for (int i = 0; i < strArr.length; i++) {
                if (!TextUtils.isEmpty(strArr[i])) {
                    if (sb.length() == 0) {
                        sb.append(strArr[i]);
                    } else {
                        sb.append(" | ");
                        sb.append(strArr[i]);
                    }
                    if (isCustomizationTest()) {
                        sb.append(this.mMobileType[i]);
                    }
                }
            }
        }
        if (this.mShowEmergencyPreferentially) {
            if (sb.length() > 0 && this.mHasCustomCarrier) {
                return sb.toString();
            }
            if (this.mEmergencyOnly) {
                return this.mContext.getResources().getString(R.string.lock_screen_no_sim_card_emergency_only);
            }
            if (sb.length() > 0) {
                return sb.toString();
            }
            return this.mContext.getResources().getString(R.string.lock_screen_no_sim_card_no_service);
        } else if (sb.length() > 0) {
            return sb.toString();
        } else {
            if (this.mEmergencyOnly) {
                return this.mContext.getResources().getString(R.string.lock_screen_no_sim_card_emergency_only);
            }
            return this.mContext.getResources().getString(R.string.lock_screen_no_sim_card_no_service);
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPhone = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mSubscriptionManager = SubscriptionManager.getDefault();
        getResources().getString(17040469);
        setSelected(KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive());
    }

    /* access modifiers changed from: private */
    public boolean isSimErrorByIccState(IccCardConstants.State state) {
        if (state == null) {
            return false;
        }
        if (!KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceProvisioned() && (state == IccCardConstants.State.ABSENT || state == IccCardConstants.State.PERM_DISABLED)) {
            state = IccCardConstants.State.NETWORK_LOCKED;
        }
        if (state == IccCardConstants.State.READY) {
            return false;
        }
        return true;
    }

    public void setShowStyle(int i) {
        this.mShowStyle = i;
        updateCarrier();
    }

    public void forceHide(boolean z) {
        if (this.mForceHide != z) {
            this.mForceHide = z;
            if (z) {
                setVisibility(8);
            } else {
                updateCarrier();
            }
        }
    }

    public void setVowifi(int i, boolean z) {
        this.mVowifiArray.put(i, z);
        updateCarrier();
    }
}
