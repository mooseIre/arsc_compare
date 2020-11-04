package com.android.systemui.statusbar.policy;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseDataConnectionState;
import android.telephony.ServiceState;
import android.telephony.ServiceStateCompat;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.CallStateController;
import com.android.systemui.statusbar.NetworkTypeUtils;
import com.android.systemui.statusbar.phone.SignalDrawable;
import com.android.systemui.statusbar.policy.FiveGController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.SignalController;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import miui.telephony.SubscriptionInfo;
import miui.telephony.SubscriptionManager;
import miui.telephony.TelephonyManagerEx;
import miui.util.FeatureParser;

public class MobileSignalController extends SignalController<MobileState, MobileIconGroup> implements FiveGController.FiveGStateChangeCallback {
    private static final boolean SUPPORT_CA = FeatureParser.getBoolean("support_ca", false);
    private NetworkControllerImpl.Config mConfig;
    private MobileIconGroup mDefaultIcons;
    private final NetworkControllerImpl.SubscriptionDefaults mDefaults;
    private boolean mEnableVolteForSlot;
    private FiveGController mFiveGController;
    private boolean mIsCtSim;
    private boolean mIsFirstSimStateChange = true;
    private boolean mIsShowVoiceType;
    private List<String> mMccNncList;
    private NetworkController.MobileTypeListener mMobileTypeListener;
    private final String mNetworkNameDefault;
    final SparseArray<MobileIconGroup> mNetworkToIconLookup;
    private ContentObserver mObserver;
    private final TelephonyManager mPhone;
    @VisibleForTesting
    final PhoneStateListener mPhoneStateListener;
    private Resources mRes;
    /* access modifiers changed from: private */
    public ServiceState mServiceState;
    /* access modifiers changed from: private */
    public SignalStrength mSignalStrength;
    /* access modifiers changed from: private */
    public int mSlotId;
    private int mStyle;
    private int mSubId;
    final SubscriptionInfo mSubscriptionInfo;
    private boolean mSupportDualVolte = miui.telephony.TelephonyManager.getDefault().isDualVolteSupported();

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MobileSignalController(Context context, NetworkControllerImpl.Config config, boolean z, TelephonyManager telephonyManager, CallbackHandler callbackHandler, NetworkControllerImpl networkControllerImpl, SubscriptionInfo subscriptionInfo, NetworkControllerImpl.SubscriptionDefaults subscriptionDefaults, Looper looper) {
        super("MobileSignalController(" + subscriptionInfo.getSubscriptionId() + ")", context, 0, callbackHandler, networkControllerImpl);
        String str;
        NetworkControllerImpl.Config config2 = config;
        boolean z2 = z;
        Looper looper2 = looper;
        "qcom".equals(FeatureParser.getString("vendor"));
        IccCardConstants.State state = IccCardConstants.State.READY;
        this.mStyle = 0;
        this.mFiveGController = (FiveGController) Dependency.get(FiveGController.class);
        this.mNetworkToIconLookup = new SparseArray<>();
        this.mRes = context.getResources();
        this.mConfig = config2;
        this.mPhone = telephonyManager;
        this.mDefaults = subscriptionDefaults;
        this.mSubscriptionInfo = subscriptionInfo;
        this.mSlotId = getSimSlotIndex();
        this.mSubId = this.mSubscriptionInfo.getSubscriptionId();
        this.mPhoneStateListener = new MobilePhoneStateListener(this.mSubId, looper2);
        this.mNetworkNameDefault = getStringIfExists(17040510);
        this.mMccNncList = Arrays.asList(this.mContext.getResources().getStringArray(285343744));
        if (config2.readIconsFromXml) {
            TelephonyIcons.readIconsFromXml(context);
            this.mDefaultIcons = !this.mConfig.showAtLeast3G ? TelephonyIcons.G : TelephonyIcons.THREE_G;
        } else {
            mapIconSets();
        }
        this.mStyle = context.getResources().getInteger(R.integer.status_bar_style);
        if (subscriptionInfo.getDisplayName() != null) {
            str = subscriptionInfo.getDisplayName().toString();
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
        this.mObserver = new ContentObserver(new Handler(looper2)) {
            public void onChange(boolean z) {
                MobileSignalController.this.updateTelephony();
            }
        };
        updateDataSim();
    }

    public SubscriptionInfo getSubscriptionInfo() {
        return this.mSubscriptionInfo;
    }

    public void setConfiguration(NetworkControllerImpl.Config config) {
        this.mConfig = config;
        if (!config.readIconsFromXml) {
            mapIconSets();
        }
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
        this.mPhone.listen(this.mPhoneStateListener, 70113);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, this.mObserver);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        contentResolver.registerContentObserver(Settings.Global.getUriFor("mobile_data" + getSimSlotIndex()), true, this.mObserver);
    }

    public void unregisterListener() {
        this.mFiveGController.removeCallback(this);
        this.mPhone.listen(this.mPhoneStateListener, 0);
        this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
    }

    private void mapIconSets() {
        this.mNetworkToIconLookup.clear();
        this.mNetworkToIconLookup.put(5, TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(6, TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(12, TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(14, TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(3, TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(17, TelephonyIcons.THREE_G);
        if (!this.mConfig.showAtLeast3G) {
            this.mNetworkToIconLookup.put(0, TelephonyIcons.UNKNOWN);
            this.mNetworkToIconLookup.put(2, TelephonyIcons.E);
            this.mNetworkToIconLookup.put(4, TelephonyIcons.ONE_X);
            this.mNetworkToIconLookup.put(7, TelephonyIcons.ONE_X);
            this.mDefaultIcons = TelephonyIcons.G;
        } else {
            this.mNetworkToIconLookup.put(0, TelephonyIcons.THREE_G);
            this.mNetworkToIconLookup.put(2, TelephonyIcons.THREE_G);
            this.mNetworkToIconLookup.put(4, TelephonyIcons.THREE_G);
            this.mNetworkToIconLookup.put(7, TelephonyIcons.THREE_G);
            this.mDefaultIcons = TelephonyIcons.THREE_G;
        }
        MobileIconGroup mobileIconGroup = TelephonyIcons.THREE_G;
        if (this.mConfig.hspaDataDistinguishable) {
            mobileIconGroup = TelephonyIcons.H;
        }
        this.mNetworkToIconLookup.put(8, mobileIconGroup);
        this.mNetworkToIconLookup.put(9, mobileIconGroup);
        this.mNetworkToIconLookup.put(10, mobileIconGroup);
        this.mNetworkToIconLookup.put(15, mobileIconGroup);
        if (this.mConfig.show4gForLte) {
            this.mNetworkToIconLookup.put(13, TelephonyIcons.FOUR_G);
            if (this.mConfig.hideLtePlus) {
                this.mNetworkToIconLookup.put(19, TelephonyIcons.FOUR_G);
            } else {
                this.mNetworkToIconLookup.put(19, TelephonyIcons.FOUR_G_PLUS);
            }
        } else {
            this.mNetworkToIconLookup.put(13, TelephonyIcons.LTE);
            if (this.mConfig.hideLtePlus) {
                this.mNetworkToIconLookup.put(19, TelephonyIcons.LTE);
            } else {
                this.mNetworkToIconLookup.put(19, TelephonyIcons.LTE_PLUS);
            }
        }
        this.mNetworkToIconLookup.put(18, TelephonyIcons.WFC);
    }

    private int getNumLevels() {
        return Build.VERSION.SDK_INT > 28 ? 6 : 5;
    }

    public int getCurrentIconId() {
        if (!hasService(this.mServiceState)) {
            return TelephonyIcons.getSignalNullIcon(this.mSlotId);
        }
        int i = ((MobileState) this.mCurrentState).level;
        int[] iArr = TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH[0];
        if (i > 5) {
            i = 5;
        }
        return iArr[i];
    }

    public int getQsCurrentIconId() {
        T t = this.mCurrentState;
        if (((MobileState) t).airplaneMode) {
            return SignalDrawable.getAirplaneModeState(getNumLevels());
        }
        if (((MobileState) t).iconGroup == TelephonyIcons.CARRIER_NETWORK_CHANGE) {
            return SignalDrawable.getCarrierChangeState(getNumLevels());
        }
        boolean z = false;
        if (((MobileState) t).connected) {
            int i = ((MobileState) t).level;
            int numLevels = getNumLevels();
            if (((MobileState) this.mCurrentState).inetCondition == 0) {
                z = true;
            }
            return SignalDrawable.getState(i, numLevels, z);
        } else if (((MobileState) t).enabled) {
            return SignalDrawable.getEmptyState(getNumLevels());
        } else {
            return 0;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:84:0x0191  */
    /* JADX WARNING: Removed duplicated region for block: B:90:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyListeners(com.android.systemui.statusbar.policy.NetworkController.SignalCallback r24) {
        /*
            r23 = this;
            r0 = r23
            r15 = r24
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r1 = r0.mConfig
            boolean r1 = r1.readIconsFromXml
            if (r1 == 0) goto L_0x000d
            r23.generateIconGroup()
        L_0x000d:
            com.android.systemui.statusbar.policy.SignalController$IconGroup r1 = r23.getIcons()
            r14 = r1
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r14 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileIconGroup) r14
            int r1 = r23.getContentDescription()
            java.lang.String r1 = r0.getStringIfExists(r1)
            int r2 = r14.mDataContentDescription
            java.lang.String r11 = r0.getStringIfExists(r2)
            T r2 = r0.mCurrentState
            r3 = r2
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r3 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r3
            com.android.systemui.statusbar.policy.SignalController$IconGroup r3 = r3.iconGroup
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r4 = com.android.systemui.statusbar.policy.TelephonyIcons.DATA_DISABLED
            r13 = 1
            if (r3 != r4) goto L_0x0036
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r2 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r2
            boolean r2 = r2.userSetup
            if (r2 == 0) goto L_0x0036
            r2 = r13
            goto L_0x0037
        L_0x0036:
            r2 = 0
        L_0x0037:
            T r3 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r3 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r3
            boolean r3 = r3.dataConnected
            if (r3 != 0) goto L_0x0044
            if (r2 == 0) goto L_0x0042
            goto L_0x0044
        L_0x0042:
            r3 = 0
            goto L_0x0045
        L_0x0044:
            r3 = r13
        L_0x0045:
            com.android.systemui.statusbar.policy.NetworkController$IconState r4 = new com.android.systemui.statusbar.policy.NetworkController$IconState
            T r5 = r0.mCurrentState
            r6 = r5
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.enabled
            if (r6 == 0) goto L_0x0058
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r5 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r5
            boolean r5 = r5.airplaneMode
            if (r5 != 0) goto L_0x0058
            r5 = r13
            goto L_0x0059
        L_0x0058:
            r5 = 0
        L_0x0059:
            int r6 = r23.getCurrentIconId()
            r4.<init>(r5, r6, r1)
            T r5 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r5 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r5
            boolean r5 = r5.dataSim
            r6 = 0
            if (r5 == 0) goto L_0x00a0
            if (r3 == 0) goto L_0x006e
            int r3 = r14.mQsDataType
            goto L_0x006f
        L_0x006e:
            r3 = 0
        L_0x006f:
            com.android.systemui.statusbar.policy.NetworkController$IconState r5 = new com.android.systemui.statusbar.policy.NetworkController$IconState
            T r7 = r0.mCurrentState
            r8 = r7
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r8 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r8
            boolean r8 = r8.enabled
            if (r8 == 0) goto L_0x0082
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r7 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r7
            boolean r7 = r7.isEmergency
            if (r7 != 0) goto L_0x0082
            r7 = r13
            goto L_0x0083
        L_0x0082:
            r7 = 0
        L_0x0083:
            int r8 = r23.getQsCurrentIconId()
            r5.<init>(r7, r8, r1)
            T r1 = r0.mCurrentState
            r7 = r1
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r7 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r7
            boolean r7 = r7.isEmergency
            if (r7 == 0) goto L_0x0094
            goto L_0x0098
        L_0x0094:
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            java.lang.String r6 = r1.networkName
        L_0x0098:
            r16 = r6
            r22 = r5
            r5 = r3
            r3 = r22
            goto L_0x00a4
        L_0x00a0:
            r3 = r6
            r16 = r3
            r5 = 0
        L_0x00a4:
            T r1 = r0.mCurrentState
            r6 = r1
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.dataConnected
            if (r6 == 0) goto L_0x00bc
            r6 = r1
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            boolean r6 = r6.carrierNetworkChangeMode
            if (r6 != 0) goto L_0x00bc
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            boolean r1 = r1.activityIn
            if (r1 == 0) goto L_0x00bc
            r6 = r13
            goto L_0x00bd
        L_0x00bc:
            r6 = 0
        L_0x00bd:
            T r1 = r0.mCurrentState
            r7 = r1
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r7 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r7
            boolean r7 = r7.dataConnected
            if (r7 == 0) goto L_0x00d5
            r7 = r1
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r7 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r7
            boolean r7 = r7.carrierNetworkChangeMode
            if (r7 != 0) goto L_0x00d5
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            boolean r1 = r1.activityOut
            if (r1 == 0) goto L_0x00d5
            r7 = r13
            goto L_0x00d6
        L_0x00d5:
            r7 = 0
        L_0x00d6:
            T r1 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            boolean r1 = r1.isDefault
            int r1 = r0.mStyle
            T r1 = r0.mCurrentState
            r2 = r1
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r2 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r2
            boolean r8 = r2.dataConnected
            int r2 = r0.mSlotId
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            java.lang.String r1 = r1.networkNameVoice
            r15.setNetworkNameVoice(r2, r1)
            int r1 = r0.mSlotId
            T r2 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r2 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r2
            boolean r2 = r2.dataSim
            r15.setIsDefaultDataSim(r1, r2)
            int r9 = r14.mDataType
            int r10 = r14.mStackedDataIcon
            int r2 = r14.mStackedVoiceIcon
            boolean r1 = r14.mIsWide
            r17 = r14
            int r14 = r0.mSlotId
            T r12 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r12 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r12
            boolean r12 = r12.roaming
            r19 = r1
            r1 = r24
            r20 = r2
            r2 = r4
            r4 = r9
            r9 = r10
            r10 = r20
            r18 = r12
            r12 = r16
            r13 = r19
            r21 = r17
            r15 = r18
            r1.setMobileDataIndicators(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15)
            android.telephony.ServiceState r1 = r0.mServiceState
            if (r1 == 0) goto L_0x0139
            int r1 = r1.getRilVoiceRadioTechnology()
            r2 = 6
            if (r1 == r2) goto L_0x0136
            r2 = 4
            if (r1 == r2) goto L_0x0136
            r2 = 5
            if (r1 == r2) goto L_0x0136
            r13 = 1
            goto L_0x0137
        L_0x0136:
            r13 = 0
        L_0x0137:
            r12 = r13
            goto L_0x013a
        L_0x0139:
            r12 = 0
        L_0x013a:
            if (r12 == 0) goto L_0x015b
            boolean r1 = r0.mEnableVolteForSlot
            if (r1 == 0) goto L_0x015b
            T r1 = r0.mCurrentState
            r2 = r1
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r2 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r2
            boolean r2 = r2.imsRegister
            if (r2 != 0) goto L_0x015b
            boolean r2 = r0.mIsCtSim
            if (r2 == 0) goto L_0x015b
            r2 = r1
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r2 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r2
            boolean r2 = r2.airplaneMode
            if (r2 != 0) goto L_0x015b
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            r2 = 1
            r1.volteNoService = r2
            r2 = 0
            goto L_0x0162
        L_0x015b:
            T r1 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            r2 = 0
            r1.volteNoService = r2
        L_0x0162:
            T r1 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            boolean r1 = r1.volteNoService
            if (r1 == 0) goto L_0x0180
            boolean r1 = r0.mSupportDualVolte
            if (r1 != 0) goto L_0x0180
            int r1 = r0.mSlotId
            miui.telephony.SubscriptionManager r3 = miui.telephony.SubscriptionManager.getDefault()
            int r3 = r3.getDefaultDataSlotId()
            if (r1 == r3) goto L_0x0180
            T r1 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            r1.volteNoService = r2
        L_0x0180:
            int r1 = r0.mSlotId
            T r2 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r2 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r2
            boolean r2 = r2.volteNoService
            r3 = r24
            r3.setVolteNoService(r1, r2)
            com.android.systemui.statusbar.policy.NetworkController$MobileTypeListener r1 = r0.mMobileTypeListener
            if (r1 == 0) goto L_0x01ad
            boolean r1 = r0.mIsShowVoiceType
            if (r1 == 0) goto L_0x019c
            T r1 = r0.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            java.lang.String r1 = r1.networkNameVoice
            goto L_0x01a6
        L_0x019c:
            r1 = r21
            int r1 = r1.mDataType
            int r2 = r0.mSlotId
            java.lang.String r1 = com.android.systemui.statusbar.policy.TelephonyIcons.getNetworkTypeName(r1, r2)
        L_0x01a6:
            com.android.systemui.statusbar.policy.NetworkController$MobileTypeListener r2 = r0.mMobileTypeListener
            int r0 = r0.mSlotId
            r2.updateMobileTypeName(r0, r1)
        L_0x01ad:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.MobileSignalController.notifyListeners(com.android.systemui.statusbar.policy.NetworkController$SignalCallback):void");
    }

    /* access modifiers changed from: protected */
    public MobileState cleanState() {
        return new MobileState();
    }

    public void onSignalStrengthChanged(int i, SignalController.IconGroup iconGroup) {
        update5GConnectState();
        boolean isFiveGBearerAllocated = this.mFiveGController.isFiveGBearerAllocated(getSimSlotIndex());
        if (!isFiveGBearerAllocated) {
            ((MobileState) this.mCurrentState).mIsShow5GSignalStrength = false;
        } else if (i != 0) {
            T t = this.mCurrentState;
            ((MobileState) t).mIsShow5GSignalStrength = true;
            ((MobileState) t).level = i;
        } else if (!this.mFiveGController.isConnectedOnSaMode(getSimSlotIndex()) || !isCalling(getOtherSlotId(this.mSlotId))) {
            T t2 = this.mCurrentState;
            ((MobileState) t2).mIsShow5GSignalStrength = false;
            ((MobileState) t2).level = i;
        } else {
            ((MobileState) this.mCurrentState).mIsShow5GSignalStrength = true;
        }
        if (miui.telephony.TelephonyManager.isCustForKrOps()) {
            ((MobileState) this.mCurrentState).KrIconGroup = iconGroup;
        }
        String str = this.mTag;
        Log.d(str, "is5GConnected = " + isFiveGBearerAllocated + ", slotId = " + getSimSlotIndex() + ", level = " + i + ", current level = " + ((MobileState) this.mCurrentState).level + ", mIsShow5GSignalStrength: " + ((MobileState) this.mCurrentState).mIsShow5GSignalStrength);
        updateSignalStrength();
        notifyListenersIfNecessary();
    }

    public static boolean isCalling(int i) {
        return ((CallStateController) Dependency.get(CallStateController.class)).getCallState(i) != 0;
    }

    public static int getOtherSlotId(int i) {
        for (int i2 = 0; i2 < TelephonyManagerEx.getDefault().getPhoneCount(); i2++) {
            if (i2 != i) {
                return i2;
            }
        }
        return -1;
    }

    public int getSlot() {
        return this.mSlotId;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0004, code lost:
        r3 = getCombinedServiceState(r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean hasService(android.telephony.ServiceState r3) {
        /*
            r0 = 0
            if (r3 != 0) goto L_0x0004
            return r0
        L_0x0004:
            int r3 = getCombinedServiceState(r3)
            r1 = 3
            if (r3 == r1) goto L_0x0013
            r1 = 1
            if (r3 == r1) goto L_0x0013
            r2 = 2
            if (r3 != r2) goto L_0x0012
            goto L_0x0013
        L_0x0012:
            return r1
        L_0x0013:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.MobileSignalController.hasService(android.telephony.ServiceState):boolean");
    }

    public static int getCombinedServiceState(ServiceState serviceState) {
        if (serviceState == null) {
            return 1;
        }
        int state = serviceState.getState();
        int dataRegState = serviceState.getDataRegState();
        if ((state == 1 || state == 2) && dataRegState == 0 && serviceState.getDataNetworkType() != 18) {
            return 0;
        }
        return state;
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
        ServiceState serviceState;
        if (isCarrierNetworkChangeActive()) {
            return false;
        }
        if (!isCdma() || (serviceState = this.mServiceState) == null) {
            return TelephonyManagerEx.getDefault().isNetworkRoamingForSlot(this.mSlotId);
        }
        int cdmaEriIconMode = serviceState.getCdmaEriIconMode();
        int cdmaEriIconIndex = this.mServiceState.getCdmaEriIconIndex();
        if (cdmaEriIconIndex < 0 || cdmaEriIconIndex == 1) {
            return false;
        }
        if (cdmaEriIconMode == 0 || cdmaEriIconMode == 1) {
            return true;
        }
        return false;
    }

    private boolean isCarrierNetworkChangeActive() {
        return ((MobileState) this.mCurrentState).carrierNetworkChangeMode;
    }

    private boolean isChinaTelecomSim(int i) {
        String simOperatorForSlot = miui.telephony.TelephonyManager.getDefault().getSimOperatorForSlot(i);
        if (TextUtils.isEmpty(simOperatorForSlot)) {
            return false;
        }
        if (simOperatorForSlot.equals("46003") || simOperatorForSlot.equals("46011") || simOperatorForSlot.equals("46005") || simOperatorForSlot.equals("45502") || simOperatorForSlot.equals("45507")) {
            return true;
        }
        return false;
    }

    private void updateCtSim(Intent intent, int i) {
        String stringExtra = intent.getStringExtra("ss");
        if ("LOADED".equals(stringExtra)) {
            this.mIsCtSim = isChinaTelecomSim(i);
        } else if ("ABSENT".equals(stringExtra)) {
            this.mIsCtSim = false;
        }
    }

    public void handleBroadcast(Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
            updateDataSim();
            notifyListenersIfNecessary();
            return;
        }
        boolean z = false;
        if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
            updateCtSim(intent, this.mSlotId);
            if (this.mIsFirstSimStateChange) {
                this.mIsFirstSimStateChange = false;
                ((MobileState) this.mCurrentState).phoneType = miui.telephony.TelephonyManager.getDefault().getPhoneTypeForSlot(this.mSlotId);
            }
        } else if (action.equals("miui.intent.action.ACTION_ENHANCED_4G_LTE_MODE_CHANGE_FOR_SLOT1") || action.equals("miui.intent.action.ACTION_ENHANCED_4G_LTE_MODE_CHANGE_FOR_SLOT2")) {
            this.mEnableVolteForSlot = intent.getBooleanExtra("extra_is_enhanced_4g_lte_on", false);
            notifyListenersIfNecessary();
        } else if (action.equals("android.intent.action.RADIO_TECHNOLOGY")) {
            ((MobileState) this.mCurrentState).phoneType = miui.telephony.TelephonyManager.getDefault().getPhoneTypeForSlot(this.mSlotId);
            notifyListenersIfNecessary();
        } else if (action.equals("android.intent.action.ANY_DATA_STATE")) {
            String stringExtra = intent.getStringExtra("apnType");
            String stringExtra2 = intent.getStringExtra("state");
            if ("mms".equals(stringExtra)) {
                ((MobileState) this.mCurrentState).mmsDataState = PhoneConstants.DataState.valueOf(stringExtra2) == PhoneConstants.DataState.CONNECTED;
                T t = this.mCurrentState;
                MobileState mobileState = (MobileState) t;
                if (((MobileState) t).connected && (((MobileState) t).dataState == 2 || (((MobileState) t).mmsDataState && this.mNetworkController.dataConnedInMMsForOperation(this.mSlotId)))) {
                    z = true;
                }
                mobileState.dataConnected = z;
                notifyListenersIfNecessary();
            }
        }
    }

    private void updateDataSim() {
        int defaultDataSubId = this.mDefaults.getDefaultDataSubId();
        boolean z = true;
        if (SubscriptionManager.isValidSubscriptionId(defaultDataSubId)) {
            MobileState mobileState = (MobileState) this.mCurrentState;
            if (defaultDataSubId != this.mSubId) {
                z = false;
            }
            mobileState.dataSim = z;
            return;
        }
        ((MobileState) this.mCurrentState).dataSim = true;
    }

    /* access modifiers changed from: private */
    public void update5GConnectState() {
        if (FiveGStatus.isNr5G(this.mServiceState) || getDataNetworkType() == 20 || this.mFiveGController.isFiveGConnect(getSimSlotIndex(), getDataNetworkType())) {
            ((MobileState) this.mCurrentState).mIs5GConnected = true;
        } else {
            ((MobileState) this.mCurrentState).mIs5GConnected = false;
        }
    }

    private void updateSignalStrength() {
        int dataNetworkType;
        T t = this.mCurrentState;
        if (!((MobileState) t).mIsShow5GSignalStrength) {
            SignalStrength signalStrength = this.mSignalStrength;
            if (!(signalStrength == null || this.mServiceState == null)) {
                ((MobileState) t).level = getMiuiLevel(signalStrength);
                if (this.mConfig.showRsrpSignalLevelforLTE && ((dataNetworkType = this.mServiceState.getDataNetworkType()) == 13 || dataNetworkType == 19)) {
                    ((MobileState) this.mCurrentState).level = getAlternateLteLevel(this.mSignalStrength);
                }
            }
            String str = this.mTag;
            Log.d(str, "4G level = " + ((MobileState) this.mCurrentState).level);
        }
    }

    private int getMiuiLevel(SignalStrength signalStrength) {
        if (Build.VERSION.SDK_INT > 28) {
            return miui.telephony.TelephonyManager.getDefault().getMiuiLevel(signalStrength);
        }
        return signalStrength.getLevel();
    }

    /* access modifiers changed from: private */
    public final void updateTelephony() {
        ServiceState serviceState;
        if (SignalController.DEBUG) {
            Log.d(this.mTag, "updateTelephony: hasService=" + hasService(this.mServiceState) + " ss=" + this.mSignalStrength);
        }
        boolean z = true;
        ((MobileState) this.mCurrentState).connected = hasService(this.mServiceState) && this.mSignalStrength != null;
        updateSignalStrength();
        if (this.mNetworkToIconLookup.indexOfKey(((MobileState) this.mCurrentState).dataNetType) >= 0) {
            T t = this.mCurrentState;
            ((MobileState) t).iconGroup = this.mNetworkToIconLookup.get(((MobileState) t).dataNetType);
        } else {
            ((MobileState) this.mCurrentState).iconGroup = this.mDefaultIcons;
        }
        T t2 = this.mCurrentState;
        MobileState mobileState = (MobileState) t2;
        if (!((MobileState) t2).connected || (((MobileState) t2).dataState != 2 && (!((MobileState) t2).mmsDataState || !this.mNetworkController.dataConnedInMMsForOperation(this.mSlotId)))) {
            z = false;
        }
        mobileState.dataConnected = z;
        updateVoiceType(this.mSlotId, getVoiceNetworkType());
        ((MobileState) this.mCurrentState).roaming = isRoaming();
        if (isCarrierNetworkChangeActive()) {
            ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.CARRIER_NETWORK_CHANGE;
        } else if (isDataDisabled()) {
            ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.DATA_DISABLED;
        }
        boolean isEmergencyOnly = isEmergencyOnly();
        T t3 = this.mCurrentState;
        if (isEmergencyOnly != ((MobileState) t3).isEmergency) {
            ((MobileState) t3).isEmergency = isEmergencyOnly();
            this.mNetworkController.recalculateEmergency();
        }
        if (((MobileState) this.mCurrentState).networkName == this.mNetworkNameDefault && (serviceState = this.mServiceState) != null && !TextUtils.isEmpty(serviceState.getOperatorAlphaShort())) {
            ((MobileState) this.mCurrentState).networkName = this.mServiceState.getOperatorAlphaShort();
        }
        ServiceState serviceState2 = this.mServiceState;
        if (serviceState2 != null) {
            ((MobileState) this.mCurrentState).networkNameData = TelephonyManager.getNetworkTypeName(serviceState2.getDataNetworkType());
        }
        if (this.mConfig.readIconsFromXml) {
            ((MobileState) this.mCurrentState).voiceLevel = getVoiceSignalLevel();
        }
        notifyListenersIfNecessary();
    }

    private boolean isDataDisabled() {
        return !this.mPhone.getDataEnabled(this.mSubId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:56:0x01a4  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x01b5  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x020f  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0214  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x024b  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0250  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void generateIconGroup() {
        /*
            r32 = this;
            r7 = r32
            T r0 = r7.mCurrentState
            r1 = r0
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            int r1 = r1.level
            r2 = 5
            if (r1 < r2) goto L_0x000e
            r8 = r2
            goto L_0x0013
        L_0x000e:
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r0 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r0
            int r0 = r0.level
            r8 = r0
        L_0x0013:
            T r0 = r7.mCurrentState
            r1 = r0
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            int r1 = r1.voiceLevel
            if (r1 < r2) goto L_0x001e
            r9 = r2
            goto L_0x0023
        L_0x001e:
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r0 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r0
            int r0 = r0.voiceLevel
            r9 = r0
        L_0x0023:
            T r0 = r7.mCurrentState
            r1 = r0
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            int r1 = r1.inetCondition
            if (r1 < r2) goto L_0x002d
            goto L_0x0031
        L_0x002d:
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r0 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r0
            int r2 = r0.inetCondition
        L_0x0031:
            r10 = r2
            T r0 = r7.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r0 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r0
            boolean r0 = r0.dataConnected
            boolean r11 = r32.isRoaming()
            int r1 = r32.getVoiceNetworkType()
            int r2 = r32.getDataNetworkType()
            int[] r3 = com.android.systemui.statusbar.policy.AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH
            r12 = 0
            r22 = r3[r12]
            int r3 = r7.mSlotId
            if (r3 < 0) goto L_0x027a
            android.telephony.TelephonyManager r4 = r7.mPhone
            int r4 = r4.getPhoneCount()
            if (r3 <= r4) goto L_0x0057
            goto L_0x027a
        L_0x0057:
            boolean r3 = com.android.systemui.statusbar.policy.SignalController.DEBUG
            if (r3 == 0) goto L_0x00e7
            java.lang.String r3 = r7.mTag
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "generateIconGroup slot:"
            r4.append(r5)
            int r5 = r7.mSlotId
            r4.append(r5)
            java.lang.String r5 = " style:"
            r4.append(r5)
            int r5 = r7.mStyle
            r4.append(r5)
            java.lang.String r5 = " connected:"
            r4.append(r5)
            T r5 = r7.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r5 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r5
            boolean r5 = r5.connected
            r4.append(r5)
            java.lang.String r5 = " inetCondition:"
            r4.append(r5)
            r4.append(r10)
            java.lang.String r5 = " roaming:"
            r4.append(r5)
            r4.append(r11)
            java.lang.String r5 = " level:"
            r4.append(r5)
            r4.append(r8)
            java.lang.String r5 = " voiceLevel:"
            r4.append(r5)
            r4.append(r9)
            java.lang.String r5 = " dataConnected:"
            r4.append(r5)
            r4.append(r0)
            java.lang.String r0 = " dataActivity:"
            r4.append(r0)
            T r0 = r7.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r0 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r0
            int r0 = r0.dataActivity
            r4.append(r0)
            java.lang.String r0 = " CS:"
            r4.append(r0)
            r4.append(r1)
            java.lang.String r0 = "/"
            r4.append(r0)
            java.lang.String r5 = android.telephony.TelephonyManager.getNetworkTypeName(r1)
            r4.append(r5)
            java.lang.String r5 = ", PS:"
            r4.append(r5)
            r4.append(r2)
            r4.append(r0)
            java.lang.String r0 = android.telephony.TelephonyManager.getNetworkTypeName(r2)
            r4.append(r0)
            java.lang.String r0 = r4.toString()
            android.util.Log.d(r3, r0)
        L_0x00e7:
            android.telephony.ServiceState r0 = r7.mServiceState
            int r14 = com.android.systemui.statusbar.NetworkTypeUtils.getDataNetTypeFromServiceState(r2, r0)
            r13 = 1
            if (r14 == 0) goto L_0x0101
            boolean r0 = r32.isCdma()
            if (r0 == 0) goto L_0x00ff
            T r0 = r7.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r0 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r0
            int r0 = r0.callState
            if (r0 == 0) goto L_0x00ff
            goto L_0x0101
        L_0x00ff:
            r0 = r12
            goto L_0x0102
        L_0x0101:
            r0 = r13
        L_0x0102:
            r7.mIsShowVoiceType = r0
            if (r0 == 0) goto L_0x0108
            r2 = r1
            goto L_0x0109
        L_0x0108:
            r2 = r14
        L_0x0109:
            int r0 = r7.mSlotId
            r7.updateVoiceType(r0, r1)
            int r1 = r7.mSlotId
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r0 = r7.mConfig
            boolean r3 = r0.showAtLeast3G
            boolean r4 = r0.show4gForLte
            boolean r5 = r0.hspaDataDistinguishable
            r0 = r32
            r6 = r10
            r0.updateDataType(r1, r2, r3, r4, r5, r6)
            int r0 = r7.mSlotId
            android.telephony.SignalStrength r1 = r7.mSignalStrength
            int r0 = com.android.systemui.statusbar.policy.TelephonyIcons.getSignalStrengthIcon(r0, r10, r8, r11, r1)
            boolean r1 = com.android.systemui.statusbar.policy.SignalController.DEBUG
            if (r1 == 0) goto L_0x0145
            java.lang.String r1 = r7.mTag
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "singleSignalIcon:"
            r2.append(r3)
            java.lang.String r3 = r7.getResourceName(r0)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r1, r2)
        L_0x0145:
            T r1 = r7.mCurrentState
            r2 = r1
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r2 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r2
            boolean r2 = r2.dataConnected
            if (r2 == 0) goto L_0x015b
            int r2 = r7.mSlotId
            if (r2 < 0) goto L_0x015b
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r1 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r1
            int r1 = r1.dataActivity
            int r1 = com.android.systemui.statusbar.policy.TelephonyIcons.getDataActivity(r2, r1)
            goto L_0x015c
        L_0x015b:
            r1 = r12
        L_0x015c:
            int r2 = com.android.systemui.statusbar.policy.TelephonyIcons.convertMobileStrengthIcon(r0)
            boolean r3 = com.android.systemui.statusbar.policy.SignalController.DEBUG
            if (r3 == 0) goto L_0x017f
            java.lang.String r3 = r7.mTag
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "unstackedSignalIcon:"
            r4.append(r5)
            java.lang.String r5 = r7.getResourceName(r2)
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.d(r3, r4)
        L_0x017f:
            if (r0 == r2) goto L_0x0187
            r31 = r2
            r2 = r0
            r0 = r31
            goto L_0x0188
        L_0x0187:
            r2 = r12
        L_0x0188:
            int r3 = r7.mStyle
            if (r3 != r13) goto L_0x01a1
            if (r11 != 0) goto L_0x0199
            boolean r3 = r32.showDataAndVoice()
            if (r3 == 0) goto L_0x0199
            int r3 = com.android.systemui.statusbar.policy.TelephonyIcons.getStackedVoiceIcon(r9)
            goto L_0x01a2
        L_0x0199:
            if (r11 == 0) goto L_0x01a1
            if (r1 == 0) goto L_0x01a1
            int r0 = com.android.systemui.statusbar.policy.TelephonyIcons.getRoamingSignalIconId(r8, r10)
        L_0x01a1:
            r3 = r12
        L_0x01a2:
            if (r3 != 0) goto L_0x01a5
            r2 = r12
        L_0x01a5:
            int r4 = r7.mSlotId
            int[] r17 = com.android.systemui.statusbar.policy.TelephonyIcons.getSignalStrengthDes(r4)
            int r4 = r7.mSlotId
            int r20 = com.android.systemui.statusbar.policy.TelephonyIcons.getSignalNullIcon(r4)
            boolean r4 = com.android.systemui.statusbar.policy.SignalController.DEBUG
            if (r4 == 0) goto L_0x01f4
            java.lang.String r4 = r7.mTag
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "singleSignalIcon="
            r5.append(r6)
            java.lang.String r6 = r7.getResourceName(r0)
            r5.append(r6)
            java.lang.String r6 = " dataActivityId="
            r5.append(r6)
            java.lang.String r6 = r7.getResourceName(r1)
            r5.append(r6)
            java.lang.String r6 = " stackedDataIcon="
            r5.append(r6)
            java.lang.String r6 = r7.getResourceName(r2)
            r5.append(r6)
            java.lang.String r6 = " stackedVoiceIcon="
            r5.append(r6)
            java.lang.String r6 = r7.getResourceName(r3)
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r4, r5)
        L_0x01f4:
            int r4 = r7.mSlotId
            int r4 = com.android.systemui.statusbar.policy.TelephonyIcons.getDataTypeIcon(r4)
            int r5 = r7.mSlotId
            int r5 = com.android.systemui.statusbar.policy.TelephonyIcons.getQSDataTypeIcon(r5)
            int[] r6 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            int r8 = r7.mSlotId
            r6 = r6[r8]
            int r8 = com.android.systemui.statusbar.policy.TelephonyIcons.getDataTypeDesc(r8)
            boolean r9 = r7.mIsShowVoiceType
            if (r9 == 0) goto L_0x020f
            goto L_0x0210
        L_0x020f:
            r12 = r6
        L_0x0210:
            boolean r6 = com.android.systemui.statusbar.policy.SignalController.DEBUG
            if (r6 == 0) goto L_0x0243
            java.lang.String r6 = r7.mTag
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "updateDataNetType, dataTypeIcon="
            r9.append(r10)
            java.lang.String r4 = r7.getResourceName(r4)
            r9.append(r4)
            java.lang.String r4 = " qsDataTypeIcon="
            r9.append(r4)
            java.lang.String r4 = r7.getResourceName(r5)
            r9.append(r4)
            java.lang.String r4 = " dataContentDesc="
            r9.append(r4)
            r9.append(r8)
            java.lang.String r4 = r9.toString()
            android.util.Log.d(r6, r4)
        L_0x0243:
            T r4 = r7.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r4 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r4
            boolean r4 = r4.mIs5GConnected
            if (r4 == 0) goto L_0x0250
            r4 = 10
            r24 = r4
            goto L_0x0252
        L_0x0250:
            r24 = r12
        L_0x0252:
            T r4 = r7.mCurrentState
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r4 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r4
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = new com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup
            r13 = r6
            java.lang.String r14 = android.telephony.TelephonyManager.getNetworkTypeName(r14)
            r15 = 0
            r16 = 0
            r18 = 0
            r19 = 0
            r21 = 0
            r25 = 0
            r23 = r8
            r26 = r5
            r27 = r0
            r28 = r2
            r29 = r3
            r30 = r1
            r13.<init>(r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30)
            r4.iconGroup = r6
            return
        L_0x027a:
            java.lang.String r0 = r7.mTag
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "generateIconGroup invalid slotId:"
            r1.append(r2)
            int r2 = r7.mSlotId
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.e(r0, r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.MobileSignalController.generateIconGroup():void");
    }

    private int getSimSlotIndex() {
        SubscriptionInfo subscriptionInfo = this.mSubscriptionInfo;
        int slotId = subscriptionInfo != null ? subscriptionInfo.getSlotId() : -1;
        if (SignalController.DEBUG) {
            String str = this.mTag;
            Log.d(str, "getSimSlotIndex, slotId: " + slotId);
        }
        return slotId;
    }

    private int getVoiceNetworkType() {
        ServiceState serviceState = this.mServiceState;
        if (serviceState == null) {
            return 0;
        }
        return serviceState.getVoiceNetworkType();
    }

    private int getDataNetworkType() {
        ServiceState serviceState = this.mServiceState;
        if (serviceState == null) {
            return 0;
        }
        return serviceState.getDataNetworkType();
    }

    public void setImsRegister(CallbackHandler callbackHandler, boolean z) {
        ((MobileState) this.mCurrentState).imsRegister = z;
        callbackHandler.setIsImsRegisted(this.mSlotId, z);
    }

    public void setSpeechHd(CallbackHandler callbackHandler, boolean z) {
        ((MobileState) this.mCurrentState).speedHd = z;
        callbackHandler.setSpeechHd(this.mSlotId, z);
        notifyListenersIfNecessary();
    }

    public void setVowifi(CallbackHandler callbackHandler, boolean z) {
        ((MobileState) this.mCurrentState).vowifi = z;
        callbackHandler.setVowifi(this.mSlotId, z);
        notifyListenersIfNecessary();
    }

    private int getVoiceSignalLevel() {
        if (this.mSignalStrength == null) {
            return 0;
        }
        boolean isCdma = isCdma();
        SignalStrength signalStrength = this.mSignalStrength;
        return isCdma ? signalStrength.getCdmaLevel() : signalStrength.getGsmLevel();
    }

    private boolean showDataAndVoice() {
        if (this.mStyle != 1) {
            return false;
        }
        int dataNetworkType = getDataNetworkType();
        int voiceNetworkType = getVoiceNetworkType();
        return (dataNetworkType == 5 || dataNetworkType == 5 || dataNetworkType == 6 || dataNetworkType == 12 || dataNetworkType == 14 || dataNetworkType == 13 || dataNetworkType == 19) && (voiceNetworkType == 16 || voiceNetworkType == 7 || voiceNetworkType == 4);
    }

    private int getAlternateLteLevel(SignalStrength signalStrength) {
        int lteDbm = signalStrength.getLteDbm();
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
        if (SignalController.DEBUG) {
            String str = this.mTag;
            Log.d(str, "getAlternateLteLevel lteRsrp:" + lteDbm + " rsrpLevel = " + i);
        }
        return i;
    }

    private String getResourceName(int i) {
        if (i == 0) {
            return "(null)";
        }
        try {
            return this.mContext.getResources().getResourceName(i);
        } catch (Resources.NotFoundException unused) {
            return "(unknown)";
        }
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
        if (this.mConfig.readIconsFromXml) {
            ((MobileState) this.mCurrentState).dataActivity = i;
        }
        notifyListenersIfNecessary();
    }

    public void dump(PrintWriter printWriter) {
        super.dump(printWriter);
        printWriter.println("  mSubscription=" + this.mSubscriptionInfo + ",");
        printWriter.println("  mServiceState=" + this.mServiceState + ",");
        printWriter.println("  mSignalStrength=" + this.mSignalStrength + ",");
        this.mFiveGController.dump(printWriter);
    }

    class MobilePhoneStateListener extends PhoneStateListener {
        public MobilePhoneStateListener(int i, Looper looper) {
            super(looper);
            setSubId(i);
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            Log.d("MobileSignalController", "onSignalStrengthsChanged: " + signalStrength);
            SignalStrength unused = MobileSignalController.this.mSignalStrength = signalStrength;
            MobileSignalController.this.updateTelephony();
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            ServiceState unused = MobileSignalController.this.mServiceState = serviceState;
            ((MobileState) MobileSignalController.this.mCurrentState).dataNetType = NetworkTypeUtils.getDataNetTypeFromServiceState(serviceState.getDataNetworkType(), serviceState);
            Log.d("MobileSignalController", "onServiceStateChanged: " + serviceState + ", data type = " + ((MobileState) MobileSignalController.this.mCurrentState).dataNetType);
            MobileSignalController.this.update5GConnectState();
            MobileSignalController.this.updateTelephony();
        }

        public void onCallStateChanged(int i, String str) {
            ((MobileState) MobileSignalController.this.mCurrentState).callState = i;
            ((CallStateController) Dependency.get(CallStateController.class)).setCallState(MobileSignalController.this.mSlotId, i);
            MobileSignalController.this.updateTelephony();
        }

        public void onDataConnectionStateChanged(int i, int i2) {
            Log.d("MobileSignalController", "onDataConnectionStateChanged: " + i);
            MobileSignalController mobileSignalController = MobileSignalController.this;
            ((MobileState) mobileSignalController.mCurrentState).dataState = i;
            mobileSignalController.updateTelephony();
        }

        public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState preciseDataConnectionState) {
            if (Constants.IS_MEDIATEK && Build.VERSION.SDK_INT >= 28) {
                if (preciseDataConnectionState == null) {
                    Log.w(MobileSignalController.this.mTag, "onPreciseDataConnectionStateChanged: dataConnectionState is null");
                    return;
                }
                String str = null;
                try {
                    str = (String) preciseDataConnectionState.getClass().getMethod("getDataConnectionAPNType", new Class[0]).invoke(preciseDataConnectionState, new Object[0]);
                } catch (Exception e) {
                    Log.e("MobileSignalController", "onPreciseDataConnectionStateChanged error", e);
                }
                if (str != null && str.equals("preempt") && preciseDataConnectionState.getDataConnectionState() != -1) {
                    String str2 = MobileSignalController.this.mTag;
                    Log.d(str2, "onPreciseDataConnectionStateChanged: dataConnectionState=" + preciseDataConnectionState);
                    ((MobileState) MobileSignalController.this.mCurrentState).dataState = preciseDataConnectionState.getDataConnectionState();
                    ((MobileState) MobileSignalController.this.mCurrentState).dataNetType = preciseDataConnectionState.getDataConnectionNetworkType();
                    MobileSignalController mobileSignalController = MobileSignalController.this;
                    if (((MobileState) mobileSignalController.mCurrentState).dataNetType == 13 && mobileSignalController.mServiceState != null && ServiceStateCompat.isUsingCarrierAggregation(MobileSignalController.this.mServiceState)) {
                        ((MobileState) MobileSignalController.this.mCurrentState).dataNetType = 19;
                    }
                    MobileSignalController.this.updateTelephony();
                } else if (str != null && str.equals("default") && ((MobileState) MobileSignalController.this.mCurrentState).dataState != preciseDataConnectionState.getDataConnectionState()) {
                    String str3 = MobileSignalController.this.mTag;
                    Log.d(str3, "onPreciseDataConnectionStateChanged: APN_TYPE_DEFAULT, dataConnectionState=" + preciseDataConnectionState);
                    ((MobileState) MobileSignalController.this.mCurrentState).dataState = preciseDataConnectionState.getDataConnectionState();
                    MobileSignalController.this.updateTelephony();
                }
            }
        }

        public void onDataActivity(int i) {
            if (SignalController.DEBUG) {
                String str = MobileSignalController.this.mTag;
                Log.d(str, "onDataActivity: direction=" + i);
            }
            MobileSignalController.this.setActivity(i);
        }

        public void onCarrierNetworkChange(boolean z) {
            if (SignalController.DEBUG) {
                String str = MobileSignalController.this.mTag;
                Log.d(str, "onCarrierNetworkChange: active=" + z);
            }
            MobileSignalController mobileSignalController = MobileSignalController.this;
            ((MobileState) mobileSignalController.mCurrentState).carrierNetworkChangeMode = z;
            mobileSignalController.updateTelephony();
        }
    }

    static class MobileIconGroup extends SignalController.IconGroup {
        final int mDataContentDescription;
        final int mDataType;
        final boolean mIsWide;
        final int mQsDataType;
        final int mStackedDataIcon;
        final int mStackedVoiceIcon;

        public MobileIconGroup(String str, int[][] iArr, int[][] iArr2, int[] iArr3, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8) {
            this(str, iArr, iArr2, iArr3, i, i2, i3, i4, i5, i6, i7, z, i8, 0, 0, 0, 0);
        }

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public MobileIconGroup(String str, int[][] iArr, int[][] iArr2, int[] iArr3, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12) {
            super(str, iArr, iArr2, iArr3, i, i2, i3, i4, i5);
            this.mDataContentDescription = i6;
            this.mDataType = i7;
            this.mIsWide = z;
            this.mQsDataType = i8;
            this.mStackedDataIcon = i10;
            this.mStackedVoiceIcon = i11;
        }
    }

    private void updateVoiceType(int i, int i2) {
        boolean z;
        String str = "";
        if (hasService(this.mServiceState)) {
            long networkClass = (long) ((int) TelephonyManagerEx.getDefault().getNetworkClass(i2));
            if (networkClass == 397312) {
                str = (!miui.os.Build.IS_CT_CUSTOMIZATION_TEST || !SUPPORT_CA || !isCdma()) ? TelephonyIcons.getNetworkTypeName(6, this.mSlotId) : TelephonyIcons.getNetworkTypeName(7, this.mSlotId);
            } else if (networkClass == 93108) {
                str = TelephonyIcons.getNetworkTypeName(3, this.mSlotId);
            } else if (miui.os.Build.IS_CM_CUSTOMIZATION_TEST || (z = miui.os.Build.IS_CU_CUSTOMIZATION_TEST) || z) {
                str = TelephonyIcons.getNetworkTypeName(1, this.mSlotId);
            }
        }
        ((MobileState) this.mCurrentState).networkNameVoice = str;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateDataType(int r21, int r22, boolean r23, boolean r24, boolean r25, int r26) {
        /*
            r20 = this;
            r0 = r20
            r1 = r22
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeArray
            r2 = r2[r21]
            android.content.res.Resources r3 = r0.mRes
            r4 = 0
            java.lang.String r5 = "com.android.systemui"
            int r2 = r3.getIdentifier(r2, r4, r5)
            android.content.res.Resources r3 = r0.mRes
            java.lang.String[] r2 = r3.getStringArray(r2)
            android.telephony.ServiceState r3 = r0.mServiceState
            if (r3 == 0) goto L_0x0020
            java.lang.String r3 = r3.getOperatorNumeric()
            goto L_0x0021
        L_0x0020:
            r3 = r4
        L_0x0021:
            r6 = 2131233595(0x7f080b3b, float:1.8083332E38)
            r7 = 7
            java.lang.String r9 = "MobileSignalController"
            r10 = 9
            r11 = 6
            r12 = 1
            r13 = 8
            r14 = 2131233594(0x7f080b3a, float:1.808333E38)
            r15 = 2
            r8 = 3
            r16 = 0
            switch(r1) {
                case 0: goto L_0x02bc;
                case 1: goto L_0x0271;
                case 2: goto L_0x02df;
                case 3: goto L_0x0306;
                case 4: goto L_0x0201;
                case 5: goto L_0x024f;
                case 6: goto L_0x024f;
                case 7: goto L_0x0228;
                case 8: goto L_0x00b0;
                case 9: goto L_0x00b0;
                case 10: goto L_0x00b0;
                case 11: goto L_0x0037;
                case 12: goto L_0x024f;
                case 13: goto L_0x004f;
                case 14: goto L_0x024f;
                case 15: goto L_0x00b0;
                case 16: goto L_0x0271;
                case 17: goto L_0x0306;
                case 18: goto L_0x0037;
                case 19: goto L_0x004f;
                default: goto L_0x0037;
            }
        L_0x0037:
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r0[r21] = r16
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            r0[r21] = r16
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r0[r21] = r16
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String r1 = ""
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r16
            goto L_0x0326
        L_0x004f:
            if (r24 == 0) goto L_0x008b
            int[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r2[r21] = r11
            int[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r3 = r0.mRes
            java.lang.String[] r10 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeGenerationArray
            r10 = r10[r12]
            int r3 = r3.getIdentifier(r10, r4, r5)
            r2[r21] = r3
            r2 = 19
            if (r1 != r2) goto L_0x0079
            int[] r1 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r1[r21] = r7
            int[] r1 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeGenerationArray
            r2 = r2[r15]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r1[r21] = r0
        L_0x0079:
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r0[r21] = r6
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r1 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeGenerationDescArray
            r1 = r1[r12]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r8
            goto L_0x0326
        L_0x008b:
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r3[r21] = r10
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            r2 = r2[r1]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r3[r21] = r0
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r2 = 2131233601(0x7f080b41, float:1.8083344E38)
            r0[r21] = r2
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeDescriptionArray
            r1 = r2[r1]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r8
            goto L_0x0326
        L_0x00b0:
            java.util.List<java.lang.String> r7 = r0.mMccNncList
            boolean r7 = r7.contains(r3)
            r12 = 10
            r17 = 5
            r18 = 2131233599(0x7f080b3f, float:1.808334E38)
            r19 = 4
            if (r7 == 0) goto L_0x016a
            android.telephony.ServiceState r7 = r0.mServiceState
            if (r7 == 0) goto L_0x00ee
            int r7 = r7.getRilDataRadioTechnology()
            r14 = 20
            if (r7 != r14) goto L_0x00ee
            int[] r7 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r7[r21] = r11
            int[] r7 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r10 = r0.mRes
            r2 = r2[r1]
            int r2 = r10.getIdentifier(r2, r4, r5)
            r7[r21] = r2
            int[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r2[r21] = r6
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r4 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeDescriptionArray
            r4 = r4[r1]
            r2[r21] = r4
            int[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r2[r21] = r8
            goto L_0x013a
        L_0x00ee:
            if (r1 == r12) goto L_0x011a
            if (r1 != r10) goto L_0x00f3
            goto L_0x011a
        L_0x00f3:
            if (r1 == r13) goto L_0x00f9
            r6 = 15
            if (r1 != r6) goto L_0x013a
        L_0x00f9:
            int[] r6 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r6[r21] = r17
            int[] r6 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r7 = r0.mRes
            r2 = r2[r1]
            int r2 = r7.getIdentifier(r2, r4, r5)
            r6[r21] = r2
            int[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r2[r21] = r18
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r4 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeDescriptionArray
            r4 = r4[r1]
            r2[r21] = r4
            int[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r2[r21] = r17
            goto L_0x013a
        L_0x011a:
            int[] r6 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r6[r21] = r19
            int[] r6 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r7 = r0.mRes
            r2 = r2[r1]
            int r2 = r7.getIdentifier(r2, r4, r5)
            r6[r21] = r2
            int[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r2[r21] = r18
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r4 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeDescriptionArray
            r4 = r4[r1]
            r2[r21] = r4
            int[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r2[r21] = r19
        L_0x013a:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "datatype = "
            r2.append(r4)
            java.lang.String r4 = android.telephony.TelephonyManager.getNetworkTypeName(r22)
            r2.append(r4)
            java.lang.String r4 = "; show datatype="
            r2.append(r4)
            int r0 = r0.mSlotId
            java.lang.String r0 = com.android.systemui.statusbar.policy.TelephonyIcons.getNetworkTypeName(r1, r0)
            r2.append(r0)
            java.lang.String r0 = "; networkOperator="
            r2.append(r0)
            r2.append(r3)
            java.lang.String r0 = r2.toString()
            android.util.Log.d(r9, r0)
            goto L_0x0326
        L_0x016a:
            if (r1 == r13) goto L_0x01b9
            if (r1 == r10) goto L_0x01b9
            if (r1 != r12) goto L_0x0171
            goto L_0x01b9
        L_0x0171:
            if (r25 == 0) goto L_0x0195
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r3[r21] = r17
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            r2 = r2[r1]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r3[r21] = r0
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r0[r21] = r18
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeDescriptionArray
            r1 = r2[r1]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r17
            goto L_0x0326
        L_0x0195:
            int[] r1 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r1[r21] = r8
            int[] r1 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeGenerationArray
            r2 = r2[r16]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r1[r21] = r0
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r0[r21] = r14
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r1 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeGenerationDescArray
            r1 = r1[r16]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r15
            goto L_0x0326
        L_0x01b9:
            if (r25 == 0) goto L_0x01dd
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r3[r21] = r19
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            r2 = r2[r1]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r3[r21] = r0
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r0[r21] = r18
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeDescriptionArray
            r1 = r2[r1]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r19
            goto L_0x0326
        L_0x01dd:
            int[] r1 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r1[r21] = r8
            int[] r1 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeGenerationArray
            r2 = r2[r16]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r1[r21] = r0
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r0[r21] = r14
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r1 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeGenerationDescArray
            r1 = r1[r16]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r15
            goto L_0x0326
        L_0x0201:
            if (r23 != 0) goto L_0x0228
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r3[r21] = r13
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            r2 = r2[r1]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r3[r21] = r0
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r2 = 2131233593(0x7f080b39, float:1.8083328E38)
            r0[r21] = r2
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeDescriptionArray
            r1 = r2[r1]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r7
            goto L_0x0326
        L_0x0228:
            if (r23 != 0) goto L_0x024f
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r3[r21] = r13
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            r2 = r2[r1]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r3[r21] = r0
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r2 = 2131233593(0x7f080b39, float:1.8083328E38)
            r0[r21] = r2
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeDescriptionArray
            r1 = r2[r1]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r11
            goto L_0x0326
        L_0x024f:
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r3[r21] = r8
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            r2 = r2[r1]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r3[r21] = r0
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r0[r21] = r14
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeDescriptionArray
            r1 = r2[r1]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r15
            goto L_0x0326
        L_0x0271:
            if (r23 != 0) goto L_0x0299
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r6 = 1
            r3[r21] = r6
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            r2 = r2[r1]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r3[r21] = r0
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r2 = 2131233598(0x7f080b3e, float:1.8083338E38)
            r0[r21] = r2
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeDescriptionArray
            r1 = r2[r1]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r16
            goto L_0x0326
        L_0x0299:
            int[] r1 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r1[r21] = r8
            int[] r1 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeGenerationArray
            r2 = r2[r16]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r1[r21] = r0
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r0[r21] = r14
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r1 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeGenerationDescArray
            r1 = r1[r16]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r15
            goto L_0x0326
        L_0x02bc:
            if (r23 != 0) goto L_0x02df
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            r2 = r2[r1]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r3[r21] = r0
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r0[r21] = r16
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeDescriptionArray
            r1 = r2[r1]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r0[r21] = r16
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r16
            goto L_0x0326
        L_0x02df:
            if (r23 != 0) goto L_0x0306
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            r2 = r2[r1]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r3[r21] = r0
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r2 = 2131233597(0x7f080b3d, float:1.8083336E38)
            r0[r21] = r2
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeDescriptionArray
            r1 = r2[r1]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r0[r21] = r15
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r1 = 1
            r0[r21] = r1
            goto L_0x0326
        L_0x0306:
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r3[r21] = r8
            int[] r3 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            android.content.res.Resources r0 = r0.mRes
            r2 = r2[r1]
            int r0 = r0.getIdentifier(r2, r4, r5)
            r3[r21] = r0
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedQSDataTypeIcon
            r0[r21] = r14
            java.lang.String[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeDesc
            java.lang.String[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mDataTypeDescriptionArray
            r1 = r2[r1]
            r0[r21] = r1
            int[] r0 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedSignalStreagthIndex
            r0[r21] = r13
        L_0x0326:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "updateDataType "
            r0.append(r1)
            java.lang.Object[] r1 = new java.lang.Object[r8]
            java.lang.Integer r2 = java.lang.Integer.valueOf(r21)
            r1[r16] = r2
            int[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataTypeIcon
            r2 = r2[r21]
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r3 = 1
            r1[r3] = r2
            int[] r2 = com.android.systemui.statusbar.policy.TelephonyIcons.mSelectedDataActivityIndex
            r2 = r2[r21]
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r1[r15] = r2
            java.lang.String r2 = "mSelectedDataTypeIcon[%d]=%d, mSelectedDataActivityIndex=%d"
            java.lang.String r1 = java.lang.String.format(r2, r1)
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            android.util.Log.d(r9, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.MobileSignalController.updateDataType(int, int, boolean, boolean, boolean, int):void");
    }

    public void setMobileTypeListener(NetworkController.MobileTypeListener mobileTypeListener) {
        this.mMobileTypeListener = mobileTypeListener;
    }

    static class MobileState extends SignalController.State {
        SignalController.IconGroup KrIconGroup;
        boolean airplaneMode;
        int callState;
        boolean carrierNetworkChangeMode;
        int dataActivity;
        boolean dataConnected;
        int dataNetType;
        boolean dataSim;
        int dataState;
        boolean imsRegister;
        boolean isDefault;
        boolean isEmergency;
        boolean mIs5GConnected;
        boolean mIsShow5GSignalStrength;
        boolean mmsDataState;
        String networkName;
        String networkNameData;
        String networkNameVoice;
        int phoneType;
        boolean roaming;
        boolean speedHd;
        boolean userSetup;
        int voiceLevel;
        boolean volteNoService;
        boolean vowifi;

        MobileState() {
        }

        public void copyFrom(SignalController.State state) {
            super.copyFrom(state);
            MobileState mobileState = (MobileState) state;
            this.dataSim = mobileState.dataSim;
            this.mIs5GConnected = mobileState.mIs5GConnected;
            this.mIsShow5GSignalStrength = mobileState.mIsShow5GSignalStrength;
            this.networkName = mobileState.networkName;
            this.networkNameData = mobileState.networkNameData;
            this.networkNameVoice = mobileState.networkNameVoice;
            this.dataNetType = mobileState.dataNetType;
            this.dataState = mobileState.dataState;
            this.dataConnected = mobileState.dataConnected;
            this.isDefault = mobileState.isDefault;
            this.isEmergency = mobileState.isEmergency;
            this.airplaneMode = mobileState.airplaneMode;
            this.carrierNetworkChangeMode = mobileState.carrierNetworkChangeMode;
            this.userSetup = mobileState.userSetup;
            this.roaming = mobileState.roaming;
            this.imsRegister = mobileState.imsRegister;
            this.speedHd = mobileState.speedHd;
            this.vowifi = mobileState.vowifi;
            this.volteNoService = mobileState.volteNoService;
            this.dataActivity = mobileState.dataActivity;
            this.voiceLevel = mobileState.voiceLevel;
            this.phoneType = mobileState.phoneType;
            this.mmsDataState = mobileState.mmsDataState;
            this.callState = mobileState.callState;
            this.KrIconGroup = mobileState.KrIconGroup;
        }

        /* access modifiers changed from: protected */
        public void toString(StringBuilder sb) {
            super.toString(sb);
            sb.append(',');
            sb.append("dataSim=");
            sb.append(this.dataSim);
            sb.append(',');
            sb.append("mIs5GConnected=");
            sb.append(this.mIs5GConnected);
            sb.append(',');
            sb.append("mIsShow5GSignalStrength=");
            sb.append(this.mIsShow5GSignalStrength);
            sb.append(',');
            sb.append("networkName=");
            sb.append(this.networkName);
            sb.append(',');
            sb.append("networkNameData=");
            sb.append(this.networkNameData);
            sb.append(',');
            sb.append("networkNameVoice=");
            sb.append(this.networkNameVoice);
            sb.append(',');
            sb.append("dataNetType=");
            sb.append(this.dataNetType);
            sb.append(',');
            sb.append("dataState=");
            sb.append(this.dataState);
            sb.append(',');
            sb.append("dataConnected=");
            sb.append(this.dataConnected);
            sb.append(',');
            sb.append("roaming=");
            sb.append(this.roaming);
            sb.append(',');
            sb.append("imsRegister=");
            sb.append(this.imsRegister);
            sb.append(',');
            sb.append("speedHd=");
            sb.append(this.speedHd);
            sb.append(',');
            sb.append("vowifi=");
            sb.append(this.vowifi);
            sb.append(',');
            sb.append("volteNoService=");
            sb.append(this.volteNoService);
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
            sb.append("voiceLevel=");
            sb.append(this.voiceLevel);
            sb.append(',');
            sb.append("phoneType=");
            sb.append(this.phoneType);
            sb.append(',');
            sb.append("mmsDataState=");
            sb.append(this.mmsDataState);
            sb.append(',');
            sb.append("callState=");
            sb.append(this.callState);
            sb.append(',');
            sb.append("KrIconGroup=");
            sb.append(this.KrIconGroup);
            sb.append(',');
            sb.append("dataActivity=");
            sb.append(this.dataActivity);
        }

        public boolean equals(Object obj) {
            if (super.equals(obj)) {
                MobileState mobileState = (MobileState) obj;
                return Objects.equals(mobileState.networkName, this.networkName) && Objects.equals(mobileState.networkNameData, this.networkNameData) && Objects.equals(mobileState.networkNameVoice, this.networkNameVoice) && mobileState.dataNetType == this.dataNetType && mobileState.dataState == this.dataState && mobileState.dataSim == this.dataSim && mobileState.mIs5GConnected == this.mIs5GConnected && mobileState.mIsShow5GSignalStrength == this.mIsShow5GSignalStrength && mobileState.dataConnected == this.dataConnected && mobileState.isEmergency == this.isEmergency && mobileState.airplaneMode == this.airplaneMode && mobileState.carrierNetworkChangeMode == this.carrierNetworkChangeMode && mobileState.userSetup == this.userSetup && mobileState.isDefault == this.isDefault && mobileState.roaming == this.roaming && mobileState.imsRegister == this.imsRegister && mobileState.speedHd == this.speedHd && mobileState.vowifi == this.vowifi && mobileState.volteNoService == this.volteNoService && mobileState.voiceLevel == this.voiceLevel && mobileState.dataActivity == this.dataActivity && mobileState.phoneType == this.phoneType && mobileState.mmsDataState == this.mmsDataState && mobileState.callState == this.callState && mobileState.KrIconGroup == this.KrIconGroup;
            }
        }
    }
}
