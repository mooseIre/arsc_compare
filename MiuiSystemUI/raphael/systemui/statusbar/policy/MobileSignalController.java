package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyDisplayInfo;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.PhoneConstants;
import com.android.settingslib.Utils;
import com.android.settingslib.net.SignalStrengthUtil;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.FiveGControllerImpl;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.SignalController;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import miui.os.Build;
import miui.telephony.TelephonyManager;
import miui.telephony.TelephonyManagerEx;
import miui.util.FeatureParser;

public class MobileSignalController extends SignalController<MobileState, MobileIconGroup> implements FiveGControllerImpl.FiveGStateChangeCallback {
    private static final boolean SUPPORT_CA = FeatureParser.getBoolean("support_ca", false);
    /* access modifiers changed from: private */
    public int mCallState = 0;
    /* access modifiers changed from: private */
    public NetworkControllerImpl.Config mConfig;
    /* access modifiers changed from: private */
    public int mDataState = 0;
    private MobileIconGroup mDefaultIcons;
    private final NetworkControllerImpl.SubscriptionDefaults mDefaults;
    private boolean mEnableVolteForSlot;
    private FiveGControllerImpl mFiveGController;
    @VisibleForTesting
    boolean mInflateSignalStrengths = false;
    private boolean mIsSupportDoubleFiveG;
    private PhoneConstants.DataState mMMSDataState = PhoneConstants.DataState.DISCONNECTED;
    protected String[] mMiuiMobileTypeNameArray;
    protected TelephonyManager mMiuiTelephonyManager;
    private final String mNetworkNameDefault;
    private final String mNetworkNameSeparator;
    final Map<String, MobileIconGroup> mNetworkToIconLookup = new HashMap();
    private final ContentObserver mObserver;
    private final android.telephony.TelephonyManager mPhone;
    protected int mPhoneCount;
    @VisibleForTesting
    final PhoneStateListener mPhoneStateListener;
    /* access modifiers changed from: private */
    public ServiceState mServiceState;
    /* access modifiers changed from: private */
    public SignalStrength mSignalStrength;
    protected int mSlotId;
    final SubscriptionInfo mSubscriptionInfo;
    private boolean mSupportDualVolte;
    /* access modifiers changed from: private */
    public TelephonyDisplayInfo mTelephonyDisplayInfo = new TelephonyDisplayInfo(0, 0);
    private final BroadcastReceiver mVolteSwitchObserver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String str = MobileSignalController.this.mTag;
            Log.d(str, "action=" + intent.getAction());
            if (MobileSignalController.this.mConfig.showVolteIcon) {
                MobileSignalController.this.notifyListeners();
            }
        }
    };

    private boolean isVolteSwitchOn() {
        return false;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MobileSignalController(Context context, NetworkControllerImpl.Config config, boolean z, android.telephony.TelephonyManager telephonyManager, CallbackHandler callbackHandler, NetworkControllerImpl networkControllerImpl, SubscriptionInfo subscriptionInfo, NetworkControllerImpl.SubscriptionDefaults subscriptionDefaults, Looper looper) {
        super("MobileSignalController(" + subscriptionInfo.getSubscriptionId() + ")", context, 0, callbackHandler, networkControllerImpl);
        String str;
        boolean z2 = z;
        Looper looper2 = looper;
        this.mConfig = config;
        this.mPhone = telephonyManager;
        this.mDefaults = subscriptionDefaults;
        this.mSubscriptionInfo = subscriptionInfo;
        this.mFiveGController = (FiveGControllerImpl) Dependency.get(FiveGControllerImpl.class);
        this.mPhoneStateListener = new MobilePhoneStateListener(new Executor(new Handler(looper2)) {
            public final /* synthetic */ Handler f$0;

            {
                this.f$0 = r1;
            }

            public final void execute(Runnable runnable) {
                this.f$0.post(runnable);
            }
        });
        this.mNetworkNameSeparator = getTextIfExists(C0021R$string.status_bar_network_name_separator).toString();
        this.mNetworkNameDefault = getTextIfExists(17040510).toString();
        mapIconSets();
        if (subscriptionInfo.getCarrierName() != null) {
            str = subscriptionInfo.getCarrierName().toString();
        } else {
            str = this.mNetworkNameDefault;
        }
        T t = this.mLastState;
        T t2 = this.mCurrentState;
        ((MobileState) t2).networkName = str;
        ((MobileState) t).networkName = str;
        ((MobileState) t2).networkNameData = str;
        ((MobileState) t).networkNameData = str;
        ((MobileState) t2).enabled = z2;
        ((MobileState) t).enabled = z2;
        MobileIconGroup mobileIconGroup = this.mDefaultIcons;
        ((MobileState) t2).iconGroup = mobileIconGroup;
        ((MobileState) t).iconGroup = mobileIconGroup;
        TelephonyManager telephonyManager2 = TelephonyManager.getDefault();
        this.mMiuiTelephonyManager = telephonyManager2;
        this.mSupportDualVolte = telephonyManager2.isDualVolteSupported();
        int simSlotIndex = subscriptionInfo.getSimSlotIndex();
        this.mSlotId = simSlotIndex;
        boolean isVolteOn = this.mNetworkController.isVolteOn(simSlotIndex);
        ((MobileState) this.mCurrentState).volte = isVolteOn;
        ((MobileState) this.mLastState).volte = isVolteOn;
        boolean isVowifiOn = this.mNetworkController.isVowifiOn(this.mSlotId);
        ((MobileState) this.mCurrentState).vowifi = isVowifiOn;
        ((MobileState) this.mLastState).vowifi = isVowifiOn;
        boolean isSpeechHdOn = this.mNetworkController.isSpeechHdOn(this.mSlotId);
        ((MobileState) this.mCurrentState).speedHd = isSpeechHdOn;
        ((MobileState) this.mLastState).speedHd = isSpeechHdOn;
        updateMiuiConfig();
        updateDataSim();
        this.mObserver = new ContentObserver(new Handler(looper2)) {
            public void onChange(boolean z) {
                MobileSignalController.this.updateTelephony();
            }
        };
        this.mPhoneCount = this.mPhone.getActiveModemCount();
    }

    public void setConfiguration(NetworkControllerImpl.Config config) {
        this.mConfig = config;
        updateMiuiConfig();
        updateInflateSignalStrength();
        mapIconSets();
        updateTelephony();
    }

    public void setAirplaneMode(boolean z) {
        ((MobileState) this.mCurrentState).airplaneMode = z;
        notifyListenersIfNecessary();
    }

    public void setUserSetupComplete(boolean z) {
        ((MobileState) this.mCurrentState).userSetup = z;
        notifyListenersIfNecessary();
    }

    public void updateConnectivity(BitSet bitSet, BitSet bitSet2) {
        boolean z = bitSet2.get(this.mTransportType);
        ((MobileState) this.mCurrentState).isDefault = bitSet.get(this.mTransportType);
        T t = this.mCurrentState;
        ((MobileState) t).inetCondition = (z || !((MobileState) t).isDefault) ? 1 : 0;
        notifyListenersIfNecessary();
    }

    public void setCarrierNetworkChangeMode(boolean z) {
        ((MobileState) this.mCurrentState).carrierNetworkChangeMode = z;
        updateTelephony();
    }

    public void registerListener() {
        this.mFiveGController.addCallback(this);
        this.mPhone.listen(this.mPhoneStateListener, 5308897);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, this.mObserver);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        contentResolver.registerContentObserver(Settings.Global.getUriFor("mobile_data" + this.mSubscriptionInfo.getSubscriptionId()), true, this.mObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("data_roaming"), true, this.mObserver);
        ContentResolver contentResolver2 = this.mContext.getContentResolver();
        contentResolver2.registerContentObserver(Settings.Global.getUriFor("data_roaming" + this.mSubscriptionInfo.getSubscriptionId()), true, this.mObserver);
        this.mContext.registerReceiver(this.mVolteSwitchObserver, new IntentFilter("org.codeaurora.intent.action.ACTION_ENHANCE_4G_SWITCH"));
    }

    public void unregisterListener() {
        ((CallStateControllerImpl) Dependency.get(CallStateControllerImpl.class)).setCallState(this.mSlotId, 0);
        this.mFiveGController.removeCallback(this);
        this.mPhone.listen(this.mPhoneStateListener, 0);
        this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
        this.mContext.unregisterReceiver(this.mVolteSwitchObserver);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x013b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void mapIconSets() {
        /*
            r7 = this;
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            r0.clear()
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            r1 = 5
            java.lang.String r1 = r7.toIconKey(r1)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r2 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            r0.put(r1, r2)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            r1 = 6
            java.lang.String r1 = r7.toIconKey(r1)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r2 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            r0.put(r1, r2)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            r1 = 12
            java.lang.String r1 = r7.toIconKey(r1)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r2 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            r0.put(r1, r2)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            r1 = 14
            java.lang.String r1 = r7.toIconKey(r1)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r2 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            r0.put(r1, r2)
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r0 = r7.mConfig
            boolean r0 = r0.show4gFor3g
            r1 = 3
            if (r0 == 0) goto L_0x004a
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r2 = r7.toIconKey(r1)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r3 = com.android.systemui.statusbar.policy.TelephonyIcons.FOUR_G
            r0.put(r2, r3)
            goto L_0x0055
        L_0x004a:
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r2 = r7.toIconKey(r1)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r3 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            r0.put(r2, r3)
        L_0x0055:
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            r2 = 17
            java.lang.String r2 = r7.toIconKey(r2)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r3 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            r0.put(r2, r3)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            r2 = 20
            java.lang.String r2 = r7.toIconKey(r2)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r3 = com.android.systemui.statusbar.policy.TelephonyIcons.FIVE_G_SA
            r0.put(r2, r3)
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r0 = r7.mConfig
            boolean r0 = r0.showAtLeast3G
            r2 = 7
            r3 = 0
            r4 = 4
            r5 = 2
            if (r0 != 0) goto L_0x00aa
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r3 = r7.toIconKey(r3)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.UNKNOWN
            r0.put(r3, r6)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r3 = r7.toIconKey(r5)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.E
            r0.put(r3, r6)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r3 = r7.toIconKey(r4)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.ONE_X
            r0.put(r3, r6)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r2 = r7.toIconKey(r2)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r3 = com.android.systemui.statusbar.policy.TelephonyIcons.ONE_X
            r0.put(r2, r3)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r0 = com.android.systemui.statusbar.policy.TelephonyIcons.G
            r7.mDefaultIcons = r0
            goto L_0x00da
        L_0x00aa:
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r3 = r7.toIconKey(r3)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            r0.put(r3, r6)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r3 = r7.toIconKey(r5)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            r0.put(r3, r6)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r3 = r7.toIconKey(r4)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            r0.put(r3, r6)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r2 = r7.toIconKey(r2)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r3 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            r0.put(r2, r3)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r0 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            r7.mDefaultIcons = r0
        L_0x00da:
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r0 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r2 = r7.mConfig
            boolean r3 = r2.show4gFor3g
            if (r3 == 0) goto L_0x00e5
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r0 = com.android.systemui.statusbar.policy.TelephonyIcons.FOUR_G
            goto L_0x00ee
        L_0x00e5:
            boolean r2 = r2.hspaDataDistinguishable
            if (r2 == 0) goto L_0x00ee
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r0 = com.android.systemui.statusbar.policy.TelephonyIcons.H
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r2 = com.android.systemui.statusbar.policy.TelephonyIcons.H_PLUS
            goto L_0x00ef
        L_0x00ee:
            r2 = r0
        L_0x00ef:
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r3 = r7.mNetworkToIconLookup
            r6 = 8
            java.lang.String r6 = r7.toIconKey(r6)
            r3.put(r6, r0)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r3 = r7.mNetworkToIconLookup
            r6 = 9
            java.lang.String r6 = r7.toIconKey(r6)
            r3.put(r6, r0)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r3 = r7.mNetworkToIconLookup
            r6 = 10
            java.lang.String r6 = r7.toIconKey(r6)
            r3.put(r6, r0)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            r3 = 15
            java.lang.String r3 = r7.toIconKey(r3)
            r0.put(r3, r2)
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r0 = r7.mConfig
            boolean r0 = r0.show4gForLte
            r2 = 1
            r3 = 13
            if (r0 == 0) goto L_0x013b
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r3 = r7.toIconKey(r3)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.FOUR_G
            r0.put(r3, r6)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r2 = r7.toDisplayIconKey(r2)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r3 = com.android.systemui.statusbar.policy.TelephonyIcons.FOUR_G_PLUS
            r0.put(r2, r3)
            goto L_0x0151
        L_0x013b:
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r3 = r7.toIconKey(r3)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.LTE
            r0.put(r3, r6)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r2 = r7.toDisplayIconKey(r2)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r3 = com.android.systemui.statusbar.policy.TelephonyIcons.LTE_PLUS
            r0.put(r2, r3)
        L_0x0151:
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            r2 = 18
            java.lang.String r2 = r7.toIconKey(r2)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r3 = com.android.systemui.statusbar.policy.TelephonyIcons.WFC
            r0.put(r2, r3)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r2 = r7.toDisplayIconKey(r5)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r3 = com.android.systemui.statusbar.policy.TelephonyIcons.LTE_CA_5G_E
            r0.put(r2, r3)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r1 = r7.toDisplayIconKey(r1)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r2 = com.android.systemui.statusbar.policy.TelephonyIcons.NR_5G
            r0.put(r1, r2)
            java.util.Map<java.lang.String, com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup> r0 = r7.mNetworkToIconLookup
            java.lang.String r7 = r7.toDisplayIconKey(r4)
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r1 = com.android.systemui.statusbar.policy.TelephonyIcons.NR_5G_PLUS
            r0.put(r7, r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.MobileSignalController.mapIconSets():void");
    }

    private String getIconKey() {
        ServiceState serviceState;
        if (this.mTelephonyDisplayInfo.getOverrideNetworkType() != 0) {
            return toDisplayIconKey(this.mTelephonyDisplayInfo.getOverrideNetworkType());
        }
        int networkType = this.mTelephonyDisplayInfo.getNetworkType();
        if (networkType == 0) {
            networkType = getDataNetworkType();
        }
        if (networkType == 13 && (serviceState = this.mServiceState) != null && (serviceState.isUsingCarrierAggregation() || (FeatureParser.getBoolean("support_ca", false) && Build.IS_CT_CUSTOMIZATION_TEST))) {
            networkType = 19;
        }
        if (networkType == 19) {
            return toDisplayIconKey(1);
        }
        return toIconKey(networkType);
    }

    private String toIconKey(int i) {
        return Integer.toString(i);
    }

    private String toDisplayIconKey(int i) {
        if (i == 1) {
            return toIconKey(13) + "_CA";
        } else if (i == 2) {
            return toIconKey(13) + "_CA_Plus";
        } else if (i != 3) {
            return i != 4 ? "unsupported" : "5G_Plus";
        } else {
            return "5G";
        }
    }

    private void updateInflateSignalStrength() {
        this.mInflateSignalStrengths = SignalStrengthUtil.shouldInflateSignalStrength(this.mContext, this.mSubscriptionInfo.getSubscriptionId());
    }

    public int getCurrentIconId() {
        if (Utils.isInService(this.mServiceState)) {
            return TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH[((MobileState) this.mCurrentState).level];
        }
        return C0013R$drawable.stat_sys_signal_null;
    }

    public int getQsCurrentIconId() {
        return getCurrentIconId();
    }

    private int getVolteResId() {
        int voiceNetworkType = getVoiceNetworkType();
        T t = this.mCurrentState;
        if ((((MobileState) t).voiceCapable || ((MobileState) t).videoCapable) && ((MobileState) this.mCurrentState).imsRegistered) {
            return C0013R$drawable.ic_volte;
        }
        if ((this.mTelephonyDisplayInfo.getNetworkType() == 13 || this.mTelephonyDisplayInfo.getNetworkType() == 19) && voiceNetworkType == 0) {
            return C0013R$drawable.ic_volte_no_voice;
        }
        return 0;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v49, resolved type: com.android.systemui.statusbar.policy.NetworkController$IconState} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v4, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v6, resolved type: java.lang.String} */
    /* JADX WARNING: type inference failed for: r10v12, types: [com.android.systemui.statusbar.policy.NetworkController$IconState] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyListeners(com.android.systemui.statusbar.policy.NetworkController.SignalCallback r35) {
        /*
            r34 = this;
            r0 = r34
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r1 = r34.getIcons()
            int r2 = r34.getContentDescription()
            java.lang.CharSequence r2 = r0.getTextIfExists(r2)
            java.lang.String r2 = r2.toString()
            int r3 = r1.mDataContentDescription
            java.lang.CharSequence r13 = r0.getTextIfExists(r3)
            java.lang.String r3 = r13.toString()
            r4 = 0
            android.text.Spanned r3 = android.text.Html.fromHtml(r3, r4)
            java.lang.String r3 = r3.toString()
            T r5 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r5 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r5
            int r5 = r5.inetCondition
            if (r5 != 0) goto L_0x0035
            android.content.Context r3 = r0.mContext
            int r5 = com.android.systemui.C0021R$string.data_connection_no_internet
            java.lang.String r3 = r3.getString(r5)
        L_0x0035:
            r12 = r3
            T r3 = r0.mCurrentState
            r5 = r3
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r5 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r5
            com.android.systemui.statusbar.policy.SignalController$IconGroup r5 = r5.iconGroup
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.DATA_DISABLED
            r7 = 1
            if (r5 == r6) goto L_0x004a
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r3 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r3
            com.android.systemui.statusbar.policy.SignalController$IconGroup r3 = r3.iconGroup
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r5 = com.android.systemui.statusbar.policy.TelephonyIcons.NOT_DEFAULT_DATA
            if (r3 != r5) goto L_0x0054
        L_0x004a:
            T r3 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r3 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r3
            boolean r3 = r3.userSetup
            if (r3 == 0) goto L_0x0054
            r3 = r7
            goto L_0x0055
        L_0x0054:
            r3 = r4
        L_0x0055:
            T r5 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r5 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r5
            boolean r5 = r5.dataConnected
            if (r5 != 0) goto L_0x0062
            if (r3 == 0) goto L_0x0060
            goto L_0x0062
        L_0x0060:
            r5 = r4
            goto L_0x0063
        L_0x0062:
            r5 = r7
        L_0x0063:
            com.android.systemui.statusbar.policy.NetworkController$IconState r6 = new com.android.systemui.statusbar.policy.NetworkController$IconState
            T r8 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r8 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r8
            boolean r8 = r8.enabled
            int r9 = r34.getCurrentIconId()
            r6.<init>(r8, r9, r2)
            T r8 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r8 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r8
            boolean r8 = r8.dataSim
            r9 = 0
            if (r8 == 0) goto L_0x00b4
            if (r5 != 0) goto L_0x0086
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r8 = r0.mConfig
            boolean r8 = r8.alwaysShowDataRatIcon
            if (r8 == 0) goto L_0x0084
            goto L_0x0086
        L_0x0084:
            r8 = r4
            goto L_0x0088
        L_0x0086:
            int r8 = r1.mQsDataType
        L_0x0088:
            com.android.systemui.statusbar.policy.NetworkController$IconState r10 = new com.android.systemui.statusbar.policy.NetworkController$IconState
            T r11 = r0.mCurrentState
            r14 = r11
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r14 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r14
            boolean r14 = r14.enabled
            if (r14 == 0) goto L_0x009b
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r11 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r11
            boolean r11 = r11.isEmergency
            if (r11 != 0) goto L_0x009b
            r11 = r7
            goto L_0x009c
        L_0x009b:
            r11 = r4
        L_0x009c:
            int r14 = r34.getQsCurrentIconId()
            r10.<init>(r11, r14, r2)
            T r2 = r0.mCurrentState
            r11 = r2
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r11 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r11
            boolean r11 = r11.isEmergency
            if (r11 == 0) goto L_0x00ad
            goto L_0x00b1
        L_0x00ad:
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r2 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r2
            java.lang.String r9 = r2.networkName
        L_0x00b1:
            r14 = r9
            r9 = r10
            goto L_0x00b6
        L_0x00b4:
            r8 = r4
            r14 = r9
        L_0x00b6:
            T r2 = r0.mCurrentState
            r10 = r2
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r10 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r10
            boolean r10 = r10.dataConnected
            if (r10 == 0) goto L_0x00ce
            r10 = r2
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r10 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r10
            boolean r10 = r10.carrierNetworkChangeMode
            if (r10 != 0) goto L_0x00ce
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r2 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r2
            boolean r2 = r2.activityIn
            if (r2 == 0) goto L_0x00ce
            r2 = r7
            goto L_0x00cf
        L_0x00ce:
            r2 = r4
        L_0x00cf:
            T r10 = r0.mCurrentState
            r11 = r10
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r11 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r11
            boolean r11 = r11.dataConnected
            if (r11 == 0) goto L_0x00e7
            r11 = r10
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r11 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r11
            boolean r11 = r11.carrierNetworkChangeMode
            if (r11 != 0) goto L_0x00e7
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r10 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r10
            boolean r10 = r10.activityOut
            if (r10 == 0) goto L_0x00e7
            r10 = r7
            goto L_0x00e8
        L_0x00e7:
            r10 = r4
        L_0x00e8:
            T r11 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r11 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r11
            boolean r11 = r11.isDefault
            if (r11 != 0) goto L_0x00f5
            if (r3 == 0) goto L_0x00f3
            goto L_0x00f5
        L_0x00f3:
            r3 = r4
            goto L_0x00f6
        L_0x00f5:
            r3 = r7
        L_0x00f6:
            r3 = r3 & r5
            if (r3 != 0) goto L_0x0106
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r5 = r0.mConfig
            boolean r11 = r5.alwaysShowDataRatIcon
            if (r11 != 0) goto L_0x0106
            boolean r5 = r5.alwaysShowNetworkTypeIcon
            if (r5 == 0) goto L_0x0104
            goto L_0x0106
        L_0x0104:
            r5 = r4
            goto L_0x0108
        L_0x0106:
            int r5 = r1.mDataType
        L_0x0108:
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r11 = r0.mConfig
            boolean r11 = r11.showVolteIcon
            if (r11 == 0) goto L_0x0118
            boolean r11 = r34.isVolteSwitchOn()
            if (r11 == 0) goto L_0x0118
            int r4 = r34.getVolteResId()
        L_0x0118:
            r11 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r4 = r34.getVowifiIconGroup()
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r15 = r0.mConfig
            boolean r15 = r15.showVowifiIcon
            if (r15 == 0) goto L_0x013c
            if (r4 == 0) goto L_0x013c
            int r4 = r4.mDataType
            com.android.systemui.statusbar.policy.NetworkController$IconState r5 = new com.android.systemui.statusbar.policy.NetworkController$IconState
            T r15 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r15 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r15
            boolean r15 = r15.enabled
            if (r15 == 0) goto L_0x0134
            int r15 = r6.icon
            goto L_0x0135
        L_0x0134:
            r15 = -1
        L_0x0135:
            java.lang.String r6 = r6.contentDescription
            r5.<init>(r7, r15, r6)
            r7 = r4
            goto L_0x013e
        L_0x013c:
            r7 = r5
            r5 = r6
        L_0x013e:
            java.lang.String r4 = r0.mTag
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r15 = "notifyListeners mConfig.alwaysShowNetworkTypeIcon="
            r6.append(r15)
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r15 = r0.mConfig
            boolean r15 = r15.alwaysShowNetworkTypeIcon
            r6.append(r15)
            java.lang.String r15 = "  getNetworkType:"
            r6.append(r15)
            android.telephony.TelephonyDisplayInfo r15 = r0.mTelephonyDisplayInfo
            int r15 = r15.getNetworkType()
            r6.append(r15)
            java.lang.String r15 = "/"
            r6.append(r15)
            r16 = r14
            android.telephony.TelephonyDisplayInfo r14 = r0.mTelephonyDisplayInfo
            int r14 = r14.getNetworkType()
            java.lang.String r14 = android.telephony.TelephonyManager.getNetworkTypeName(r14)
            r6.append(r14)
            java.lang.String r14 = " voiceNetType="
            r6.append(r14)
            int r14 = r34.getVoiceNetworkType()
            r6.append(r14)
            r6.append(r15)
            int r14 = r34.getVoiceNetworkType()
            java.lang.String r14 = android.telephony.TelephonyManager.getNetworkTypeName(r14)
            r6.append(r14)
            java.lang.String r14 = " showDataIcon="
            r6.append(r14)
            r6.append(r3)
            java.lang.String r3 = " mConfig.alwaysShowDataRatIcon="
            r6.append(r3)
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r3 = r0.mConfig
            boolean r3 = r3.alwaysShowDataRatIcon
            r6.append(r3)
            java.lang.String r3 = " icons.mDataType="
            r6.append(r3)
            int r3 = r1.mDataType
            r6.append(r3)
            java.lang.String r3 = " mConfig.showVolteIcon="
            r6.append(r3)
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r3 = r0.mConfig
            boolean r3 = r3.showVolteIcon
            r6.append(r3)
            java.lang.String r3 = " isVolteSwitchOn="
            r6.append(r3)
            boolean r3 = r34.isVolteSwitchOn()
            r6.append(r3)
            java.lang.String r3 = " volteIcon="
            r6.append(r3)
            r6.append(r11)
            java.lang.String r3 = " mConfig.showVowifiIcon="
            r6.append(r3)
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r3 = r0.mConfig
            boolean r3 = r3.showVowifiIcon
            r6.append(r3)
            java.lang.String r3 = r6.toString()
            android.util.Log.d(r4, r3)
            com.android.systemui.statusbar.policy.MobileSignalController$MiuiMobileState r3 = new com.android.systemui.statusbar.policy.MobileSignalController$MiuiMobileState
            r17 = r3
            T r4 = r0.mCurrentState
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.airplaneMode
            r18 = r6
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.dataConnected
            r19 = r6
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.volte
            r20 = r6
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.vowifi
            r21 = r6
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.hideVolte
            r22 = r6
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.hideVowifi
            r23 = r6
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.speedHd
            r24 = r6
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.volteNoService
            r25 = r6
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.showDataTypeWhenWifiOn
            r26 = r6
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.showDataTypeDataDisconnected
            r27 = r6
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.showMobileDataTypeInMMS
            r28 = r6
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            java.lang.String r6 = r6.showName
            r29 = r6
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            int r6 = r6.volteResId
            r30 = r6
            r6 = r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            int r6 = r6.vowifiResId
            r31 = r6
            int r6 = r0.mSlotId
            r32 = r6
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r4 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r4
            int r4 = r4.qcom5GDrawbleId
            r33 = r4
            r17.<init>(r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30, r31, r32, r33)
            android.telephony.SubscriptionInfo r4 = r0.mSubscriptionInfo
            int r4 = r4.getSimSlotIndex()
            T r6 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.dataSim
            r14 = r35
            r14.setIsDefaultDataSim(r4, r6)
            boolean r15 = r1.mIsWide
            android.telephony.SubscriptionInfo r1 = r0.mSubscriptionInfo
            int r1 = r1.getSubscriptionId()
            T r0 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r0 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r0
            boolean r0 = r0.roaming
            r4 = r35
            r6 = r9
            r9 = r2
            r14 = r16
            r16 = r1
            r17 = r0
            r18 = r3
            r4.setMobileDataIndicators(r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.MobileSignalController.notifyListeners(com.android.systemui.statusbar.policy.NetworkController$SignalCallback):void");
    }

    /* access modifiers changed from: protected */
    public MobileIconGroup getIcons() {
        T t = this.mCurrentState;
        if (((MobileState) t).qcom5GIconGroup != null) {
            return ((MobileState) t).qcom5GIconGroup;
        }
        return (MobileIconGroup) super.getIcons();
    }

    /* access modifiers changed from: protected */
    public MobileState cleanState() {
        return new MobileState();
    }

    private boolean isCdma() {
        SignalStrength signalStrength = this.mSignalStrength;
        return (signalStrength != null && !signalStrength.isGsm()) || ((MobileState) this.mCurrentState).phoneType == 2;
    }

    public boolean isEmergencyOnly() {
        ServiceState serviceState = this.mServiceState;
        return serviceState != null && serviceState.isEmergencyOnly();
    }

    private boolean isRoaming() {
        if (isCarrierNetworkChangeActive()) {
            return false;
        }
        if (!isCdma() || this.mServiceState == null) {
            ServiceState serviceState = this.mServiceState;
            if (serviceState == null || !serviceState.getRoaming()) {
                return false;
            }
            return true;
        }
        int eriIconMode = this.mPhone.getCdmaEriInformation().getEriIconMode();
        int eriIconIndex = this.mPhone.getCdmaEriInformation().getEriIconIndex();
        if (eriIconIndex < 0 || eriIconIndex == 1) {
            return false;
        }
        if (eriIconMode == 0 || eriIconMode == 1) {
            return true;
        }
        return false;
    }

    private boolean isCarrierNetworkChangeActive() {
        return ((MobileState) this.mCurrentState).carrierNetworkChangeMode;
    }

    public void handleBroadcast(Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.telephony.action.SERVICE_PROVIDERS_UPDATED")) {
            updateNetworkName(intent.getBooleanExtra("android.telephony.extra.SHOW_SPN", false), intent.getStringExtra("android.telephony.extra.SPN"), intent.getStringExtra("android.telephony.extra.DATA_SPN"), intent.getBooleanExtra("android.telephony.extra.SHOW_PLMN", false), intent.getStringExtra("android.telephony.extra.PLMN"));
            notifyListenersIfNecessary();
        } else if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
            updateDataSim();
            notifyListenersIfNecessary();
        } else if (action.equals("android.intent.action.ANY_DATA_STATE")) {
            String stringExtra = intent.getStringExtra("apnType");
            String stringExtra2 = intent.getStringExtra("state");
            if ("mms".equals(stringExtra)) {
                String str = this.mTag;
                Log.d(str, "handleBroadcast MMS connection state=" + stringExtra2);
                this.mMMSDataState = PhoneConstants.DataState.valueOf(stringExtra2);
                updateTelephony();
            }
        } else if (action.equals("miui.intent.action.ACTION_ENHANCED_4G_LTE_MODE_CHANGE_FOR_SLOT1") || action.equals("miui.intent.action.ACTION_ENHANCED_4G_LTE_MODE_CHANGE_FOR_SLOT2")) {
            this.mEnableVolteForSlot = intent.getBooleanExtra("extra_is_enhanced_4g_lte_on", false);
            updateTelephony();
        } else if (action.equals("android.intent.action.RADIO_TECHNOLOGY")) {
            ((MobileState) this.mCurrentState).phoneType = this.mMiuiTelephonyManager.getPhoneTypeForSlot(this.mSlotId);
            updateTelephony();
        }
    }

    /* access modifiers changed from: private */
    public void updateDataSim() {
        int activeDataSubId = this.mDefaults.getActiveDataSubId();
        boolean z = true;
        if (SubscriptionManager.isValidSubscriptionId(activeDataSubId)) {
            MobileState mobileState = (MobileState) this.mCurrentState;
            if (activeDataSubId != this.mSubscriptionInfo.getSubscriptionId()) {
                z = false;
            }
            mobileState.dataSim = z;
            return;
        }
        ((MobileState) this.mCurrentState).dataSim = true;
    }

    /* access modifiers changed from: package-private */
    public void updateNetworkName(boolean z, String str, String str2, boolean z2, String str3) {
        if (SignalController.CHATTY) {
            Log.d("CarrierLabel", "updateNetworkName showSpn=" + z + " spn=" + str + " dataSpn=" + str2 + " showPlmn=" + z2 + " plmn=" + str3);
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        if (z2 && str3 != null) {
            sb.append(str3);
            sb2.append(str3);
        }
        if (z && str != null) {
            if (sb.length() != 0) {
                sb.append(this.mNetworkNameSeparator);
            }
            sb.append(str);
        }
        if (sb.length() != 0) {
            ((MobileState) this.mCurrentState).networkName = sb.toString();
        } else {
            ((MobileState) this.mCurrentState).networkName = this.mNetworkNameDefault;
        }
        if (z && str2 != null) {
            if (sb2.length() != 0) {
                sb2.append(this.mNetworkNameSeparator);
            }
            sb2.append(str2);
        }
        if (sb2.length() != 0) {
            ((MobileState) this.mCurrentState).networkNameData = sb2.toString();
            return;
        }
        ((MobileState) this.mCurrentState).networkNameData = this.mNetworkNameDefault;
    }

    /* access modifiers changed from: private */
    public final void updateTelephony() {
        int rilVoiceRadioTechnology;
        ServiceState serviceState;
        ServiceState serviceState2;
        Log.d(this.mTag, "updateTelephonySignalStrength: hasService=" + Utils.isInService(this.mServiceState) + " ss=" + this.mSignalStrength + " displayInfo=" + this.mTelephonyDisplayInfo);
        checkDefaultData();
        boolean z = true;
        ((MobileState) this.mCurrentState).connected = Utils.isInService(this.mServiceState) && this.mSignalStrength != null;
        updateSignalStrength();
        String iconKey = getIconKey();
        if (this.mNetworkToIconLookup.get(iconKey) != null) {
            ((MobileState) this.mCurrentState).iconGroup = this.mNetworkToIconLookup.get(iconKey);
        } else {
            ((MobileState) this.mCurrentState).iconGroup = this.mDefaultIcons;
        }
        T t = this.mCurrentState;
        ((MobileState) t).dataConnected = ((MobileState) t).connected && (this.mDataState == 2 || (this.mMMSDataState == PhoneConstants.DataState.CONNECTED && ((MobileState) t).showMobileDataTypeInMMS));
        ((MobileState) this.mCurrentState).roaming = isRoaming();
        if (isCarrierNetworkChangeActive()) {
            ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.CARRIER_NETWORK_CHANGE;
        }
        boolean isEmergencyOnly = isEmergencyOnly();
        T t2 = this.mCurrentState;
        if (isEmergencyOnly != ((MobileState) t2).isEmergency) {
            ((MobileState) t2).isEmergency = isEmergencyOnly();
            this.mNetworkController.recalculateEmergency();
        }
        if (((MobileState) this.mCurrentState).networkName.equals(this.mNetworkNameDefault) && (serviceState2 = this.mServiceState) != null && !TextUtils.isEmpty(serviceState2.getOperatorAlphaShort())) {
            ((MobileState) this.mCurrentState).networkName = this.mServiceState.getOperatorAlphaShort();
        }
        if (((MobileState) this.mCurrentState).networkNameData.equals(this.mNetworkNameDefault) && (serviceState = this.mServiceState) != null && ((MobileState) this.mCurrentState).dataSim && !TextUtils.isEmpty(serviceState.getOperatorAlphaShort())) {
            ((MobileState) this.mCurrentState).networkNameData = this.mServiceState.getOperatorAlphaShort();
        }
        ((MobileState) this.mCurrentState).mobileDataEnabled = this.mPhone.isDataEnabled();
        ((MobileState) this.mCurrentState).roamingDataEnabled = this.mPhone.isDataRoamingEnabled();
        int voiceNetworkType = getVoiceNetworkType();
        T t3 = this.mCurrentState;
        if (((MobileState) t3).fiveGConnected) {
            ((MobileState) t3).iconGroup = TelephonyIcons.FIVE_G;
            ((MobileState) t3).miuiDataType = 10;
        } else {
            ((MobileState) t3).miuiDataType = ((MobileIconGroup) ((MobileState) t3).iconGroup).mMiuiDataType;
        }
        T t4 = this.mCurrentState;
        ((MobileState) t4).miuiVoiceType = voiceNetworkType;
        if ((((MobileIconGroup) ((MobileState) t4).iconGroup).mMiuiDataType == 0 && !((MobileState) t4).fiveGConnected) || (isCdma() && !isCallIdle())) {
            T t5 = this.mCurrentState;
            ((MobileState) t5).showType = ((MobileState) t5).miuiVoiceType;
            ((MobileState) t5).showName = transformVoiceTypeToName(((MobileState) t5).showType);
        } else {
            T t6 = this.mCurrentState;
            ((MobileState) t6).showType = ((MobileState) t6).miuiDataType;
            ((MobileState) t6).showName = getMobileTypeName(((MobileState) t6).showType);
        }
        if (((MobileState) this.mCurrentState).CTSim) {
            ServiceState serviceState3 = this.mServiceState;
            boolean z2 = (serviceState3 == null || (rilVoiceRadioTechnology = serviceState3.getRilVoiceRadioTechnology()) == 6 || rilVoiceRadioTechnology == 4 || rilVoiceRadioTechnology == 5) ? false : true;
            T t7 = this.mCurrentState;
            MobileState mobileState = (MobileState) t7;
            if (!z2 || !this.mEnableVolteForSlot || ((MobileState) t7).volte || ((MobileState) t7).airplaneMode) {
                z = false;
            }
            mobileState.volteNoService = z;
        }
        T t8 = this.mCurrentState;
        if (((MobileState) t8).volteNoService && !this.mSupportDualVolte && !((MobileState) t8).dataSim) {
            ((MobileState) t8).volteNoService = false;
        }
        notifyListenersIfNecessary();
    }

    private void checkDefaultData() {
        T t = this.mCurrentState;
        if (((MobileState) t).iconGroup != TelephonyIcons.NOT_DEFAULT_DATA) {
            ((MobileState) t).defaultDataOff = false;
            return;
        }
        ((MobileState) t).defaultDataOff = this.mNetworkController.isDataControllerDisabled();
    }

    /* access modifiers changed from: package-private */
    public void onMobileDataChanged() {
        checkDefaultData();
        notifyListenersIfNecessary();
    }

    /* access modifiers changed from: package-private */
    public boolean isDataDisabled() {
        return !this.mPhone.isDataConnectionAllowed();
    }

    private boolean isCallIdle() {
        return this.mCallState == 0;
    }

    private int getVoiceNetworkType() {
        ServiceState serviceState = this.mServiceState;
        if (serviceState != null) {
            return serviceState.getVoiceNetworkType();
        }
        return 0;
    }

    private int getDataNetworkType() {
        ServiceState serviceState = this.mServiceState;
        if (serviceState != null) {
            return serviceState.getDataNetworkType();
        }
        return 0;
    }

    private int getAlternateLteLevel(SignalStrength signalStrength) {
        int lteDbm = signalStrength.getLteDbm();
        if (lteDbm == Integer.MAX_VALUE) {
            int level = signalStrength.getLevel();
            String str = this.mTag;
            Log.d(str, "getAlternateLteLevel lteRsrp:INVALID  signalStrengthLevel = " + level);
            return level;
        }
        int i = 0;
        if (lteDbm <= -44) {
            if (lteDbm >= -97) {
                i = 4;
            } else if (lteDbm >= -105) {
                i = 3;
            } else if (lteDbm >= -113) {
                i = 2;
            } else if (lteDbm >= -120) {
                i = 1;
            }
        }
        String str2 = this.mTag;
        Log.d(str2, "getAlternateLteLevel lteRsrp:" + lteDbm + " rsrpLevel = " + i);
        return i;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setActivity(int i) {
        boolean z = false;
        ((MobileState) this.mCurrentState).activityIn = i == 3 || i == 1;
        MobileState mobileState = (MobileState) this.mCurrentState;
        if (i == 3 || i == 2) {
            z = true;
        }
        mobileState.activityOut = z;
        notifyListenersIfNecessary();
    }

    public void setVolte(boolean z) {
        ((MobileState) this.mCurrentState).volte = z;
        updateTelephony();
    }

    public void setVowifi(boolean z) {
        ((MobileState) this.mCurrentState).vowifi = z;
        updateTelephony();
    }

    public void setSpeechHd(boolean z) {
        ((MobileState) this.mCurrentState).speedHd = z;
        updateTelephony();
    }

    private boolean isVowifiAvailable() {
        T t = this.mCurrentState;
        return ((MobileState) t).voiceCapable && ((MobileState) t).imsRegistered && getDataNetworkType() == 18;
    }

    private MobileIconGroup getVowifiIconGroup() {
        if (isVowifiAvailable() && !isCallIdle()) {
            return TelephonyIcons.VOWIFI_CALLING;
        }
        if (isVowifiAvailable()) {
            return TelephonyIcons.VOWIFI;
        }
        return null;
    }

    public void dump(PrintWriter printWriter) {
        super.dump(printWriter);
        printWriter.println("  mSubscription=" + this.mSubscriptionInfo + ",");
        printWriter.println("  mServiceState=" + this.mServiceState + ",");
        printWriter.println("  mSignalStrength=" + this.mSignalStrength + ",");
        printWriter.println("  mTelephonyDisplayInfo=" + this.mTelephonyDisplayInfo + ",");
        printWriter.println("  mDataState=" + this.mDataState + ",");
        printWriter.println("  mInflateSignalStrengths=" + this.mInflateSignalStrengths + ",");
        printWriter.println("  isDataDisabled=" + isDataDisabled() + ",");
    }

    public int getSlot() {
        return this.mSubscriptionInfo.getSimSlotIndex();
    }

    public ServiceState getServiceState() {
        return this.mServiceState;
    }

    public void onSignalStrengthChanged(int i, MobileIconGroup mobileIconGroup) {
        update5GConnectState();
        boolean isFiveGConnect = this.mFiveGController.isFiveGConnect(getSlot(), getDataNetworkType());
        if (!isFiveGConnect) {
            ((MobileState) this.mCurrentState).showQcom5GSignalStrength = false;
        } else if (i != 0) {
            T t = this.mCurrentState;
            ((MobileState) t).showQcom5GSignalStrength = true;
            ((MobileState) t).level = i;
        } else if (!this.mFiveGController.isConnectedOnSaMode(getSlot()) || !isCalling(getOtherSlotId(getSlot()))) {
            T t2 = this.mCurrentState;
            ((MobileState) t2).showQcom5GSignalStrength = false;
            ((MobileState) t2).level = i;
        } else {
            ((MobileState) this.mCurrentState).showQcom5GSignalStrength = true;
        }
        if (TelephonyManager.isCustForKrOps()) {
            T t3 = this.mCurrentState;
            ((MobileState) t3).qcom5GIconGroup = mobileIconGroup;
            ((MobileState) t3).qcom5GDrawbleId = this.mFiveGController.getFiveGDrawable(this.mSlotId);
        }
        String str = this.mTag;
        Log.d(str, "is5GConnected = " + isFiveGConnect + ", slotId = " + getSlot() + ", level = " + i + ", current level = " + ((MobileState) this.mCurrentState).level + ", mIsShow5GSignalStrength: " + ((MobileState) this.mCurrentState).showQcom5GSignalStrength);
        updateTelephony();
    }

    public static boolean isCalling(int i) {
        return ((CallStateControllerImpl) Dependency.get(CallStateControllerImpl.class)).getCallState(i) != 0;
    }

    public static int getOtherSlotId(int i) {
        for (int i2 = 0; i2 < TelephonyManagerEx.getDefault().getPhoneCount(); i2++) {
            if (i2 != i) {
                return i2;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public void update5GConnectState() {
        if (FiveGStatus.isNr5G(this.mServiceState) || getDataNetworkType() == 20 || this.mFiveGController.isFiveGConnect(getSlot(), getDataNetworkType())) {
            ((MobileState) this.mCurrentState).fiveGConnected = true;
        } else {
            ((MobileState) this.mCurrentState).fiveGConnected = false;
        }
        String str = this.mTag;
        Log.d(str, "update5GConnectState: " + ((MobileState) this.mCurrentState).fiveGConnected);
    }

    private void updateSignalStrength() {
        int dataNetworkType;
        T t = this.mCurrentState;
        if (!((MobileState) t).showQcom5GSignalStrength) {
            SignalStrength signalStrength = this.mSignalStrength;
            if (!(signalStrength == null || this.mServiceState == null)) {
                ((MobileState) t).level = this.mMiuiTelephonyManager.getMiuiLevel(signalStrength);
                if (this.mConfig.showRsrpSignalLevelforLTE && ((dataNetworkType = this.mServiceState.getDataNetworkType()) == 13 || dataNetworkType == 19)) {
                    ((MobileState) this.mCurrentState).level = getAlternateLteLevel(this.mSignalStrength);
                }
            }
            String str = this.mTag;
            Log.d(str, "NotQcom5G, signal level = " + ((MobileState) this.mCurrentState).level);
        }
    }

    private String transformVoiceTypeToName(int i) {
        if (!Utils.isInService(this.mServiceState)) {
            return "";
        }
        long networkClass = (long) ((int) TelephonyManagerEx.getDefault().getNetworkClass(i));
        if (networkClass == 524288) {
            return getMobileTypeName(10);
        }
        if (networkClass == 397312) {
            if (!Build.IS_CT_CUSTOMIZATION_TEST || !SUPPORT_CA || !isCdma()) {
                return getMobileTypeName(6);
            }
            return getMobileTypeName(7);
        } else if (networkClass == 93108) {
            return getMobileTypeName(3);
        } else {
            if (Build.IS_CM_CUSTOMIZATION_TEST || Build.IS_CU_CUSTOMIZATION_TEST) {
                return getMobileTypeName(1);
            }
            return "";
        }
    }

    class MobilePhoneStateListener extends PhoneStateListener {
        public MobilePhoneStateListener(Executor executor) {
            super(executor);
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            String str;
            String str2 = MobileSignalController.this.mTag;
            StringBuilder sb = new StringBuilder();
            sb.append("onSignalStrengthsChanged signalStrength=");
            sb.append(signalStrength);
            if (signalStrength == null) {
                str = "";
            } else {
                str = " level=" + signalStrength.getLevel();
            }
            sb.append(str);
            Log.d(str2, sb.toString());
            SignalStrength unused = MobileSignalController.this.mSignalStrength = signalStrength;
            MobileSignalController.this.updateTelephony();
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            int i;
            MobileSignalController mobileSignalController = MobileSignalController.this;
            if (mobileSignalController.mPhoneCount != 2 || (i = mobileSignalController.mSlotId) < 0 || i >= 2 || MobileSignalController.getOtherSlotId(i) == -1 || !MobileSignalController.isCalling(MobileSignalController.getOtherSlotId(MobileSignalController.this.mSlotId)) || Utils.isInService(serviceState)) {
                String str = MobileSignalController.this.mTag;
                Log.d(str, "onServiceStateChanged voiceState=" + serviceState);
                ServiceState unused = MobileSignalController.this.mServiceState = serviceState;
                MobileSignalController.this.update5GConnectState();
                MobileSignalController.this.updateTelephony();
            }
        }

        public void onDataConnectionStateChanged(int i, int i2) {
            String str = MobileSignalController.this.mTag;
            Log.d(str, "onDataConnectionStateChanged: state=" + i + " type=" + i2);
            int unused = MobileSignalController.this.mDataState = i;
            MobileSignalController.this.updateTelephony();
        }

        public void onDataActivity(int i) {
            String str = MobileSignalController.this.mTag;
            Log.d(str, "onDataActivity: direction=" + i);
            MobileSignalController.this.setActivity(i);
        }

        public void onCarrierNetworkChange(boolean z) {
            String str = MobileSignalController.this.mTag;
            Log.d(str, "onCarrierNetworkChange: active=" + z);
            MobileSignalController mobileSignalController = MobileSignalController.this;
            ((MobileState) mobileSignalController.mCurrentState).carrierNetworkChangeMode = z;
            mobileSignalController.updateTelephony();
        }

        public void onActiveDataSubscriptionIdChanged(int i) {
            String str = MobileSignalController.this.mTag;
            Log.d(str, "onActiveDataSubscriptionIdChanged: subId=" + i);
            MobileSignalController.this.updateDataSim();
            MobileSignalController.this.updateTelephony();
        }

        public void onDisplayInfoChanged(TelephonyDisplayInfo telephonyDisplayInfo) {
            String str = MobileSignalController.this.mTag;
            Log.d(str, "onDisplayInfoChanged: telephonyDisplayInfo=" + telephonyDisplayInfo);
            TelephonyDisplayInfo unused = MobileSignalController.this.mTelephonyDisplayInfo = telephonyDisplayInfo;
            MobileSignalController.this.updateTelephony();
        }

        public void onCallStateChanged(int i, String str) {
            String str2 = MobileSignalController.this.mTag;
            Log.d(str2, "onCallStateChanged: state=" + i);
            int unused = MobileSignalController.this.mCallState = i;
            ((MobileState) MobileSignalController.this.mCurrentState).callState = i;
            ((CallStateControllerImpl) Dependency.get(CallStateControllerImpl.class)).setCallState(MobileSignalController.this.mSlotId, i);
            MobileSignalController.this.updateTelephony();
        }
    }

    static class MobileIconGroup extends SignalController.IconGroup {
        final int mDataContentDescription;
        final int mDataType;
        final boolean mIsWide;
        final int mMiuiDataType;
        final int mQsDataType;

        public MobileIconGroup(String str, int[][] iArr, int[][] iArr2, int[] iArr3, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8) {
            super(str, iArr, iArr2, iArr3, i, i2, i3, i4, i5);
            this.mDataContentDescription = i6;
            this.mDataType = i7;
            this.mIsWide = z;
            this.mQsDataType = i7;
            this.mMiuiDataType = i8;
        }
    }

    static class MobileState extends SignalController.State {
        public boolean CTSim;
        boolean airplaneMode;
        public int callState;
        boolean carrierNetworkChangeMode;
        boolean dataConnected;
        boolean dataSim;
        boolean defaultDataOff;
        boolean fiveGConnected;
        public boolean hideVolte;
        public boolean hideVowifi;
        boolean imsRegistered;
        boolean isDefault;
        boolean isEmergency;
        public int miuiDataType;
        public int miuiVoiceType;
        boolean mobileDataEnabled;
        String networkName;
        String networkNameData;
        public int phoneType;
        int qcom5GDrawbleId;
        MobileIconGroup qcom5GIconGroup;
        boolean roaming;
        boolean roamingDataEnabled;
        public boolean showDataTypeDataDisconnected;
        public boolean showDataTypeWhenWifiOn;
        public boolean showMobileDataTypeInMMS;
        public String showName;
        boolean showQcom5GSignalStrength;
        public int showType;
        public boolean speedHd;
        boolean userSetup;
        boolean videoCapable;
        boolean voiceCapable;
        public boolean volte;
        public boolean volteNoService;
        public int volteResId;
        public boolean vowifi;
        public int vowifiResId;

        MobileState() {
        }

        public void copyFrom(SignalController.State state) {
            super.copyFrom(state);
            MobileState mobileState = (MobileState) state;
            this.dataSim = mobileState.dataSim;
            this.networkName = mobileState.networkName;
            this.networkNameData = mobileState.networkNameData;
            this.dataConnected = mobileState.dataConnected;
            this.isDefault = mobileState.isDefault;
            this.isEmergency = mobileState.isEmergency;
            this.airplaneMode = mobileState.airplaneMode;
            this.carrierNetworkChangeMode = mobileState.carrierNetworkChangeMode;
            this.userSetup = mobileState.userSetup;
            this.roaming = mobileState.roaming;
            this.defaultDataOff = mobileState.defaultDataOff;
            this.imsRegistered = mobileState.imsRegistered;
            this.voiceCapable = mobileState.voiceCapable;
            this.videoCapable = mobileState.videoCapable;
            this.mobileDataEnabled = mobileState.mobileDataEnabled;
            this.roamingDataEnabled = mobileState.roamingDataEnabled;
            this.fiveGConnected = mobileState.fiveGConnected;
            this.showQcom5GSignalStrength = mobileState.showQcom5GSignalStrength;
            this.qcom5GIconGroup = mobileState.qcom5GIconGroup;
            this.qcom5GDrawbleId = mobileState.qcom5GDrawbleId;
            this.phoneType = mobileState.phoneType;
            this.callState = mobileState.callState;
            this.volte = mobileState.volte;
            this.vowifi = mobileState.vowifi;
            this.volteNoService = mobileState.volteNoService;
            this.speedHd = mobileState.speedHd;
            this.miuiDataType = mobileState.miuiDataType;
            this.miuiVoiceType = mobileState.miuiVoiceType;
            this.showType = mobileState.showType;
            this.showName = mobileState.showName;
            this.CTSim = mobileState.CTSim;
            this.hideVolte = mobileState.hideVolte;
            this.hideVowifi = mobileState.hideVowifi;
            this.volteResId = mobileState.volteResId;
            this.vowifiResId = mobileState.vowifiResId;
            this.showDataTypeWhenWifiOn = mobileState.showDataTypeWhenWifiOn;
            this.showDataTypeDataDisconnected = mobileState.showDataTypeDataDisconnected;
            this.showMobileDataTypeInMMS = mobileState.showMobileDataTypeInMMS;
        }

        /* access modifiers changed from: protected */
        public void toString(StringBuilder sb) {
            super.toString(sb);
            sb.append(',');
            sb.append("dataSim=");
            sb.append(this.dataSim);
            sb.append(',');
            sb.append("networkName=");
            sb.append(this.networkName);
            sb.append(',');
            sb.append("networkNameData=");
            sb.append(this.networkNameData);
            sb.append(',');
            sb.append("dataConnected=");
            sb.append(this.dataConnected);
            sb.append(',');
            sb.append("roaming=");
            sb.append(this.roaming);
            sb.append(',');
            sb.append("isDefault=");
            sb.append(this.isDefault);
            sb.append(',');
            sb.append("isEmergency=");
            sb.append(this.isEmergency);
            sb.append(',');
            sb.append("airplaneMode=");
            sb.append(this.airplaneMode);
            sb.append(',');
            sb.append("carrierNetworkChangeMode=");
            sb.append(this.carrierNetworkChangeMode);
            sb.append(',');
            sb.append("userSetup=");
            sb.append(this.userSetup);
            sb.append(',');
            sb.append("defaultDataOff=");
            sb.append(this.defaultDataOff);
            sb.append("imsRegistered=");
            sb.append(this.imsRegistered);
            sb.append(',');
            sb.append("voiceCapable=");
            sb.append(this.voiceCapable);
            sb.append(',');
            sb.append("videoCapable=");
            sb.append(this.videoCapable);
            sb.append(',');
            sb.append("mobileDataEnabled=");
            sb.append(this.mobileDataEnabled);
            sb.append(',');
            sb.append("roamingDataEnabled=");
            sb.append(this.roamingDataEnabled);
            sb.append(',');
            sb.append("qcom5GConnected=");
            sb.append(this.fiveGConnected);
            sb.append(',');
            sb.append("showQcom5GSignalStrength=");
            sb.append(this.showQcom5GSignalStrength);
            sb.append(',');
            sb.append("qcom5GIconGroup=");
            sb.append(this.qcom5GIconGroup);
            sb.append(',');
            sb.append("qcom5GDrawbleId=");
            sb.append(this.qcom5GDrawbleId);
            sb.append(',');
            sb.append("phoneType=");
            sb.append(this.phoneType);
            sb.append(',');
            sb.append("callState=");
            sb.append(this.callState);
            sb.append(',');
            sb.append("volte=");
            sb.append(this.volte);
            sb.append(',');
            sb.append("vowifi=");
            sb.append(this.vowifi);
            sb.append(',');
            sb.append("volteNoService=");
            sb.append(this.volteNoService);
            sb.append(',');
            sb.append("speedHd=");
            sb.append(this.speedHd);
            sb.append(',');
            sb.append("miuiDataType=");
            sb.append(this.miuiDataType);
            sb.append(',');
            sb.append("miuiVoiceType=");
            sb.append(this.miuiVoiceType);
            sb.append(',');
            sb.append("showType=");
            sb.append(this.showType);
            sb.append(',');
            sb.append("showName=");
            sb.append(this.showName);
            sb.append(',');
            sb.append("CTSim=");
            sb.append(this.CTSim);
            sb.append(',');
            sb.append("hideVolte=");
            sb.append(this.hideVolte);
            sb.append(',');
            sb.append("hideVowifi=");
            sb.append(this.hideVowifi);
            sb.append(',');
            sb.append("volteResId=");
            sb.append(this.volteResId);
            sb.append(',');
            sb.append("vowifiResId=");
            sb.append(this.vowifiResId);
            sb.append(',');
            sb.append("showDataTypeWhenWifiOn=");
            sb.append(this.showDataTypeWhenWifiOn);
            sb.append(',');
            sb.append("showDataTypeDataDisconnected=");
            sb.append(this.showDataTypeDataDisconnected);
            sb.append(',');
            sb.append("showMobileDataTypeInMMS=");
            sb.append(this.showMobileDataTypeInMMS);
        }

        public boolean equals(Object obj) {
            if (super.equals(obj)) {
                MobileState mobileState = (MobileState) obj;
                return Objects.equals(mobileState.networkName, this.networkName) && Objects.equals(mobileState.networkNameData, this.networkNameData) && mobileState.dataSim == this.dataSim && mobileState.dataConnected == this.dataConnected && mobileState.isEmergency == this.isEmergency && mobileState.airplaneMode == this.airplaneMode && mobileState.carrierNetworkChangeMode == this.carrierNetworkChangeMode && mobileState.userSetup == this.userSetup && mobileState.isDefault == this.isDefault && mobileState.roaming == this.roaming && mobileState.defaultDataOff == this.defaultDataOff && mobileState.imsRegistered == this.imsRegistered && mobileState.voiceCapable == this.voiceCapable && mobileState.videoCapable == this.videoCapable && mobileState.mobileDataEnabled == this.mobileDataEnabled && mobileState.roamingDataEnabled == this.roamingDataEnabled && mobileState.fiveGConnected == this.fiveGConnected && mobileState.showQcom5GSignalStrength == this.showQcom5GSignalStrength && mobileState.qcom5GIconGroup == this.qcom5GIconGroup && mobileState.qcom5GDrawbleId == this.qcom5GDrawbleId && mobileState.phoneType == this.phoneType && mobileState.callState == this.callState && mobileState.volte == this.volte && mobileState.vowifi == this.vowifi && mobileState.volteNoService == this.volteNoService && mobileState.speedHd == this.speedHd && mobileState.miuiDataType == this.miuiDataType && mobileState.miuiVoiceType == this.miuiVoiceType && mobileState.showType == this.showType && Objects.equals(mobileState.showName, this.showName) && mobileState.CTSim == this.CTSim && mobileState.hideVolte == this.hideVolte && mobileState.hideVowifi == this.hideVowifi && mobileState.volteResId == this.volteResId && mobileState.vowifiResId == this.vowifiResId && mobileState.showDataTypeWhenWifiOn == this.showDataTypeWhenWifiOn && mobileState.showDataTypeDataDisconnected == this.showDataTypeDataDisconnected && mobileState.showMobileDataTypeInMMS == this.showMobileDataTypeInMMS;
            }
        }
    }

    public static class MiuiMobileState {
        public boolean airplane;
        public boolean dataConnected;
        public boolean hideVolte;
        public boolean hideVowifi;
        public int qcom5GDrawableId;
        public boolean showDataTypeDataDisconnected;
        public boolean showDataTypeWhenWifiOn;
        public boolean showMobileDataTypeInMMS;
        public String showName;
        public int slotId;
        public boolean speedHd;
        public boolean volte;
        public boolean volteNoService;
        public int volteResId;
        public boolean vowifi;
        public int vowifiResId;

        public MiuiMobileState(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8, boolean z9, boolean z10, boolean z11, String str, int i, int i2, int i3, int i4) {
            this.airplane = z;
            this.dataConnected = z2;
            this.volte = z3;
            this.vowifi = z4;
            this.hideVolte = z5;
            this.hideVowifi = z6;
            this.speedHd = z7;
            this.volteNoService = z8;
            this.showDataTypeWhenWifiOn = z9;
            this.showDataTypeDataDisconnected = z10;
            this.showMobileDataTypeInMMS = z11;
            this.showName = str;
            this.volteResId = i;
            this.vowifiResId = i2;
            this.slotId = i3;
            this.qcom5GDrawableId = i4;
        }
    }

    public void updateMiuiConfig() {
        int i;
        String simOperatorNumericForPhone = this.mPhone.getSimOperatorNumericForPhone(this.mSlotId);
        boolean z = true;
        Resources resourcesForOperation = getResourcesForOperation(this.mContext, simOperatorNumericForPhone, true);
        Resources resourcesForOperation2 = getResourcesForOperation(this.mContext, "00000", true);
        ((MobileState) this.mCurrentState).CTSim = isCTSim(simOperatorNumericForPhone);
        ((MobileState) this.mCurrentState).hideVolte = resourcesForOperation.getBoolean(C0010R$bool.status_bar_hide_volte) || resourcesForOperation2.getBoolean(C0010R$bool.status_bar_hide_volte);
        ((MobileState) this.mCurrentState).hideVowifi = resourcesForOperation.getBoolean(C0010R$bool.status_bar_hide_vowifi_mcc_mnc);
        MobileState mobileState = (MobileState) this.mCurrentState;
        if (Build.IS_INTERNATIONAL_BUILD) {
            i = transformVolteDrawableId(resourcesForOperation.getInteger(C0016R$integer.status_bar_volte_drawable_type));
        } else {
            i = C0013R$drawable.stat_sys_signal_hd_big;
        }
        mobileState.volteResId = i;
        ((MobileState) this.mCurrentState).vowifiResId = transformVowifiDrawableId(resourcesForOperation.getInteger(C0016R$integer.status_bar_vowifi_drawable_type), resourcesForOperation.getBoolean(C0010R$bool.status_bar_show_dual_vowifi_icons), this.mSlotId, this.mNetworkController);
        this.mIsSupportDoubleFiveG = TelephonyManagerEx.getDefault().isDualNrSupported();
        ((MobileState) this.mCurrentState).showDataTypeWhenWifiOn = resourcesForOperation.getBoolean(C0010R$bool.status_bar_show_mobile_type_when_wifi_on);
        ((MobileState) this.mCurrentState).showDataTypeDataDisconnected = this.mIsSupportDoubleFiveG || !Build.IS_INTERNATIONAL_BUILD;
        MobileState mobileState2 = (MobileState) this.mCurrentState;
        if (!resourcesForOperation.getBoolean(C0010R$bool.status_bar_show_mobile_type_in_mms) && !resourcesForOperation2.getBoolean(C0010R$bool.status_bar_show_mobile_type_in_mms)) {
            z = false;
        }
        mobileState2.showMobileDataTypeInMMS = z;
        this.mMiuiMobileTypeNameArray = getMiuiMobileTypeNameArray(resourcesForOperation);
    }

    public String getMobileTypeName(int i) {
        String[] strArr = this.mMiuiMobileTypeNameArray;
        return (strArr == null || i < 0 || i >= strArr.length) ? "" : strArr[i];
    }

    public static Resources getResourcesForOperation(Context context, String str, boolean z) {
        if (TextUtils.isEmpty(str)) {
            return context.getResources();
        }
        Configuration configuration = context.getResources().getConfiguration();
        Configuration configuration2 = new Configuration();
        configuration2.setTo(configuration);
        int i = 0;
        int parseInt = Integer.parseInt(str.substring(0, 3));
        if (z) {
            i = Integer.parseInt(str.substring(3));
        }
        configuration2.mcc = parseInt;
        configuration2.mnc = i;
        if (i == 0) {
            configuration2.mnc = 65535;
        }
        return context.createConfigurationContext(configuration2).getResources();
    }

    public static int transformVolteDrawableId(int i) {
        if (i == 1) {
            return C0013R$drawable.stat_sys_signal_volte_4g;
        }
        if (i == 2) {
            return C0013R$drawable.stat_sys_signal_volte_no_frame;
        }
        if (i == 3) {
            return C0013R$drawable.stat_sys_signal_volte_hd_voice;
        }
        if (i != 4) {
            return C0013R$drawable.stat_sys_signal_volte;
        }
        return C0013R$drawable.stat_sys_signal_hd_big;
    }

    public static int transformVowifiDrawableId(int i, boolean z, int i2, NetworkController networkController) {
        if (i == 1) {
            if (z && networkController.getNumberSubscriptions() == 2) {
                if (i2 == 1) {
                    return C0013R$drawable.stat_sys_vowifi_call_2;
                }
                if (i2 == 0) {
                    return C0013R$drawable.stat_sys_vowifi_call_1;
                }
            }
            return C0013R$drawable.stat_sys_vowifi_call;
        } else if (i != 2) {
            return C0013R$drawable.stat_sys_vowifi;
        } else {
            return C0013R$drawable.stat_sys_vowifi_wifi;
        }
    }

    public static String[] getMiuiMobileTypeNameArray(Resources resources) {
        String[] strArr = new String[11];
        int[] intArray = resources.getIntArray(C0008R$array.data_type_name_default_key);
        String[] stringArray = resources.getStringArray(C0008R$array.data_type_name_default_value);
        for (int i = 0; i < intArray.length; i++) {
            strArr[intArray[i]] = stringArray[i];
        }
        if (Build.IS_CM_CUSTOMIZATION_TEST) {
            strArr[1] = "2G";
        }
        int[] intArray2 = resources.getIntArray(C0008R$array.data_type_name_mcc_key);
        String[] stringArray2 = resources.getStringArray(C0008R$array.data_type_name_mcc_value);
        for (int i2 = 0; i2 < intArray2.length; i2++) {
            strArr[intArray2[i2]] = stringArray2[i2];
        }
        int[] intArray3 = resources.getIntArray(C0008R$array.data_type_name_mcc_mnc_key);
        String[] stringArray3 = resources.getStringArray(C0008R$array.data_type_name_mcc_mnc_value);
        for (int i3 = 0; i3 < intArray3.length; i3++) {
            strArr[intArray3[i3]] = stringArray3[i3];
        }
        int[] intArray4 = resources.getIntArray(C0008R$array.data_type_name_cus_reg_key);
        String[] stringArray4 = resources.getStringArray(C0008R$array.data_type_name_cus_reg_value);
        for (int i4 = 0; i4 < intArray4.length; i4++) {
            strArr[intArray4[i4]] = stringArray4[i4];
        }
        return strArr;
    }

    public static boolean isCTSim(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        if (str.equals("46003") || str.equals("46011") || str.equals("46005") || str.equals("45502") || str.equals("45507")) {
            return true;
        }
        return false;
    }
}
