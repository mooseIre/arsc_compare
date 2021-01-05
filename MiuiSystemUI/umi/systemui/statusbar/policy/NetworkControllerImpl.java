package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UiccAccessRule;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0021R$string;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.MiuiWifiSignalController;
import com.android.systemui.statusbar.policy.MobileSignalController;
import com.android.systemui.statusbar.policy.NetworkController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import miui.os.Build;

public class NetworkControllerImpl extends BroadcastReceiver implements NetworkController, DemoMode, DataUsageController.NetworkNameProvider, Dumpable {
    static final boolean CHATTY = Log.isLoggable("NetworkControllerChat", 3);
    static final boolean DEBUG = Log.isLoggable("NetworkController", 3);
    private final AccessPointControllerImpl mAccessPoints;
    /* access modifiers changed from: private */
    public int mActiveMobileDataSubscription;
    private boolean mAirplaneMode;
    private final BroadcastDispatcher mBroadcastDispatcher;
    /* access modifiers changed from: private */
    public final CallbackHandler mCallbackHandler;
    /* access modifiers changed from: private */
    public final Runnable mClearForceValidated;
    private Config mConfig;
    private final BitSet mConnectedTransports;
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private List<SubscriptionInfo> mCurrentSubscriptions;
    private int mCurrentUserId;
    private final DataSaverController mDataSaverController;
    private final DataUsageController mDataUsageController;
    private MobileSignalController mDefaultSignalController;
    private boolean mDemoInetCondition;
    private boolean mDemoMode;
    private MiuiWifiSignalController.WifiState mDemoWifiState;
    private int mEmergencySource;
    @VisibleForTesting
    final EthernetSignalController mEthernetSignalController;
    /* access modifiers changed from: private */
    public boolean mForceCellularValidated;
    private final boolean mHasMobileDataFeature;
    private boolean mHasNoSubs;
    private boolean mInetCondition;
    private boolean mIsEmergency;
    @VisibleForTesting
    ServiceState mLastServiceState;
    @VisibleForTesting
    boolean mListening;
    private Locale mLocale;
    private final Object mLock;
    @VisibleForTesting
    final SparseArray<MobileSignalController> mMobileSignalControllers;
    private final TelephonyManager mPhone;
    private PhoneStateListener mPhoneStateListener;
    /* access modifiers changed from: private */
    public final Handler mReceiverHandler;
    private final Runnable mRegisterListeners;
    private boolean mSimDetected;
    protected boolean[] mSpeechHd;
    private final SubscriptionDefaults mSubDefaults;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener;
    private final SubscriptionManager mSubscriptionManager;
    private boolean mUserSetup;
    private final CurrentUserTracker mUserTracker;
    private final BitSet mValidatedTransports;
    protected boolean[] mVolte;
    protected boolean[] mVowifi;
    /* access modifiers changed from: private */
    public final WifiManager mWifiManager;
    @VisibleForTesting
    final MiuiWifiSignalController mWifiSignalController;

    /* renamed from: com.android.systemui.statusbar.policy.NetworkControllerImpl$1  reason: invalid class name */
    class AnonymousClass1 implements ConfigurationController.ConfigurationListener {
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public NetworkControllerImpl(android.content.Context r17, android.os.Looper r18, com.android.systemui.statusbar.policy.DeviceProvisionedController r19, com.android.systemui.broadcast.BroadcastDispatcher r20, android.net.ConnectivityManager r21, android.telephony.TelephonyManager r22, android.net.wifi.WifiManager r23, android.net.NetworkScoreManager r24) {
        /*
            r16 = this;
            r15 = r16
            r1 = r17
            android.telephony.SubscriptionManager r6 = android.telephony.SubscriptionManager.from(r17)
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r7 = com.android.systemui.statusbar.policy.NetworkControllerImpl.Config.readConfig(r17)
            com.android.systemui.statusbar.policy.CallbackHandler r9 = new com.android.systemui.statusbar.policy.CallbackHandler
            r9.<init>()
            com.android.systemui.statusbar.policy.AccessPointControllerImpl r10 = new com.android.systemui.statusbar.policy.AccessPointControllerImpl
            r10.<init>(r1)
            com.android.settingslib.net.DataUsageController r11 = new com.android.settingslib.net.DataUsageController
            r11.<init>(r1)
            com.android.systemui.statusbar.policy.NetworkControllerImpl$SubscriptionDefaults r12 = new com.android.systemui.statusbar.policy.NetworkControllerImpl$SubscriptionDefaults
            r12.<init>()
            r0 = r16
            r2 = r21
            r3 = r22
            r4 = r23
            r5 = r24
            r8 = r18
            r13 = r19
            r14 = r20
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14)
            android.os.Handler r0 = r15.mReceiverHandler
            java.lang.Runnable r1 = r15.mRegisterListeners
            r0.post(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.NetworkControllerImpl.<init>(android.content.Context, android.os.Looper, com.android.systemui.statusbar.policy.DeviceProvisionedController, com.android.systemui.broadcast.BroadcastDispatcher, android.net.ConnectivityManager, android.telephony.TelephonyManager, android.net.wifi.WifiManager, android.net.NetworkScoreManager):void");
    }

    @VisibleForTesting
    NetworkControllerImpl(Context context, ConnectivityManager connectivityManager, TelephonyManager telephonyManager, WifiManager wifiManager, NetworkScoreManager networkScoreManager, SubscriptionManager subscriptionManager, Config config, Looper looper, CallbackHandler callbackHandler, AccessPointControllerImpl accessPointControllerImpl, DataUsageController dataUsageController, SubscriptionDefaults subscriptionDefaults, DeviceProvisionedController deviceProvisionedController, BroadcastDispatcher broadcastDispatcher) {
        DataUsageController dataUsageController2 = dataUsageController;
        final DeviceProvisionedController deviceProvisionedController2 = deviceProvisionedController;
        BroadcastDispatcher broadcastDispatcher2 = broadcastDispatcher;
        this.mLock = new Object();
        this.mActiveMobileDataSubscription = -1;
        this.mMobileSignalControllers = new SparseArray<>();
        this.mConnectedTransports = new BitSet();
        this.mValidatedTransports = new BitSet();
        this.mAirplaneMode = false;
        this.mLocale = null;
        this.mCurrentSubscriptions = new ArrayList();
        this.mClearForceValidated = new Runnable() {
            public final void run() {
                NetworkControllerImpl.this.lambda$new$0$NetworkControllerImpl();
            }
        };
        this.mRegisterListeners = new Runnable() {
            public void run() {
                NetworkControllerImpl.this.registerListeners();
            }
        };
        this.mContext = context;
        this.mConfig = config;
        this.mReceiverHandler = new Handler(looper);
        this.mCallbackHandler = callbackHandler;
        this.mDataSaverController = new DataSaverControllerImpl(context);
        this.mBroadcastDispatcher = broadcastDispatcher2;
        this.mSubscriptionManager = subscriptionManager;
        this.mSubDefaults = subscriptionDefaults;
        this.mConnectivityManager = connectivityManager;
        this.mHasMobileDataFeature = connectivityManager.isNetworkSupported(0);
        this.mPhone = telephonyManager;
        int activeModemCount = telephonyManager.getActiveModemCount();
        this.mVolte = new boolean[activeModemCount];
        this.mVowifi = new boolean[activeModemCount];
        this.mSpeechHd = new boolean[activeModemCount];
        this.mWifiManager = wifiManager;
        this.mLocale = this.mContext.getResources().getConfiguration().locale;
        this.mAccessPoints = accessPointControllerImpl;
        this.mDataUsageController = dataUsageController2;
        dataUsageController2.setNetworkController(this);
        this.mDataUsageController.setCallback(new DataUsageController.Callback() {
            public void onMobileDataEnabled(boolean z) {
                NetworkControllerImpl.this.mCallbackHandler.setMobileDataEnabled(z);
                NetworkControllerImpl.this.notifyControllersMobileDataChanged();
            }
        });
        this.mWifiSignalController = new MiuiWifiSignalController(this.mContext, this.mHasMobileDataFeature, this.mCallbackHandler, this, this.mWifiManager, this.mConnectivityManager, networkScoreManager);
        this.mEthernetSignalController = new EthernetSignalController(this.mContext, this.mCallbackHandler, this);
        updateAirplaneMode(true);
        AnonymousClass3 r0 = new CurrentUserTracker(broadcastDispatcher2) {
            public void onUserSwitched(int i) {
                NetworkControllerImpl.this.onUserSwitched(i);
            }
        };
        this.mUserTracker = r0;
        r0.startTracking();
        deviceProvisionedController2.addCallback(new DeviceProvisionedController.DeviceProvisionedListener() {
            public void onUserSetupChanged() {
                NetworkControllerImpl networkControllerImpl = NetworkControllerImpl.this;
                DeviceProvisionedController deviceProvisionedController = deviceProvisionedController2;
                networkControllerImpl.setUserSetupComplete(deviceProvisionedController.isUserSetup(deviceProvisionedController.getCurrentUser()));
            }
        });
        this.mConnectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            private Network mLastNetwork;
            private NetworkCapabilities mLastNetworkCapabilities;

            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                NetworkCapabilities networkCapabilities2 = this.mLastNetworkCapabilities;
                boolean z = networkCapabilities2 != null && networkCapabilities2.hasCapability(16);
                boolean hasCapability = networkCapabilities.hasCapability(16);
                if (!network.equals(this.mLastNetwork) || !networkCapabilities.equalsTransportTypes(this.mLastNetworkCapabilities) || hasCapability != z) {
                    this.mLastNetwork = network;
                    this.mLastNetworkCapabilities = networkCapabilities;
                    NetworkControllerImpl.this.updateConnectivity();
                }
            }
        }, this.mReceiverHandler);
        Handler handler = this.mReceiverHandler;
        Objects.requireNonNull(handler);
        this.mPhoneStateListener = new PhoneStateListener(new Executor(handler) {
            public final /* synthetic */ Handler f$0;

            {
                this.f$0 = r1;
            }

            public final void execute(Runnable runnable) {
                this.f$0.post(runnable);
            }
        }) {
            public void onActiveDataSubscriptionIdChanged(int i) {
                NetworkControllerImpl networkControllerImpl = NetworkControllerImpl.this;
                if (networkControllerImpl.keepCellularValidationBitInSwitch(networkControllerImpl.mActiveMobileDataSubscription, i)) {
                    if (NetworkControllerImpl.DEBUG) {
                        Log.d("NetworkController", ": mForceCellularValidated to true.");
                    }
                    boolean unused = NetworkControllerImpl.this.mForceCellularValidated = true;
                    NetworkControllerImpl.this.mReceiverHandler.removeCallbacks(NetworkControllerImpl.this.mClearForceValidated);
                    NetworkControllerImpl.this.mReceiverHandler.postDelayed(NetworkControllerImpl.this.mClearForceValidated, 2000);
                }
                int unused2 = NetworkControllerImpl.this.mActiveMobileDataSubscription = i;
                NetworkControllerImpl.this.doUpdateMobileControllers();
            }
        };
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$NetworkControllerImpl() {
        if (DEBUG) {
            Log.d("NetworkController", ": mClearForceValidated");
        }
        this.mForceCellularValidated = false;
        updateConnectivity();
    }

    /* access modifiers changed from: package-private */
    public boolean isInGroupDataSwitch(int i, int i2) {
        SubscriptionInfo activeSubscriptionInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(i);
        SubscriptionInfo activeSubscriptionInfo2 = this.mSubscriptionManager.getActiveSubscriptionInfo(i2);
        return (activeSubscriptionInfo == null || activeSubscriptionInfo2 == null || activeSubscriptionInfo.getGroupUuid() == null || !activeSubscriptionInfo.getGroupUuid().equals(activeSubscriptionInfo2.getGroupUuid())) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public boolean keepCellularValidationBitInSwitch(int i, int i2) {
        if (!this.mValidatedTransports.get(0) || !isInGroupDataSwitch(i, i2)) {
            return false;
        }
        return true;
    }

    public DataSaverController getDataSaverController() {
        return this.mDataSaverController;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void registerListeners() {
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).registerListener();
        }
        if (this.mSubscriptionListener == null) {
            this.mSubscriptionListener = new SubListener(this, (AnonymousClass1) null);
        }
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
        this.mPhone.listen(this.mPhoneStateListener, 4194304);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.intent.action.SERVICE_STATE");
        intentFilter.addAction("android.telephony.action.SERVICE_PROVIDERS_UPDATED");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.conn.INET_CONDITION_ACTION");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        intentFilter.addAction("android.intent.action.ANY_DATA_STATE");
        intentFilter.addAction("android.intent.action.ACTION_IMS_REGISTED");
        intentFilter.addAction("android.intent.action.ACTION_SPEECH_CODEC_IS_HD");
        intentFilter.addAction("android.intent.action.RADIO_TECHNOLOGY");
        if (miui.telephony.TelephonyManager.getDefault().getCtVolteSupportedMode() > 0) {
            intentFilter.addAction("miui.intent.action.ACTION_ENHANCED_4G_LTE_MODE_CHANGE_FOR_SLOT1");
            intentFilter.addAction("miui.intent.action.ACTION_ENHANCED_4G_LTE_MODE_CHANGE_FOR_SLOT2");
        }
        this.mBroadcastDispatcher.registerReceiverWithHandler(this, intentFilter, this.mReceiverHandler);
        this.mListening = true;
        this.mReceiverHandler.post(new Runnable() {
            public final void run() {
                NetworkControllerImpl.this.updateConnectivity();
            }
        });
        Handler handler = this.mReceiverHandler;
        MiuiWifiSignalController miuiWifiSignalController = this.mWifiSignalController;
        Objects.requireNonNull(miuiWifiSignalController);
        handler.post(new Runnable() {
            public final void run() {
                MiuiWifiSignalController.this.fetchInitialState();
            }
        });
        this.mReceiverHandler.post(new Runnable() {
            public final void run() {
                NetworkControllerImpl.this.lambda$registerListeners$1$NetworkControllerImpl();
            }
        });
        updateMobileControllers();
        this.mReceiverHandler.post(new Runnable() {
            public final void run() {
                NetworkControllerImpl.this.recalculateEmergency();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$registerListeners$1 */
    public /* synthetic */ void lambda$registerListeners$1$NetworkControllerImpl() {
        if (this.mLastServiceState == null) {
            this.mLastServiceState = this.mPhone.getServiceState();
            if (this.mMobileSignalControllers.size() == 0) {
                recalculateEmergency();
            }
        }
    }

    private void unregisterListeners() {
        this.mListening = false;
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).unregisterListener();
        }
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mSubscriptionListener);
        this.mBroadcastDispatcher.unregisterReceiver(this);
    }

    public NetworkController.AccessPointController getAccessPointController() {
        return this.mAccessPoints;
    }

    public DataUsageController getMobileDataController() {
        return this.mDataUsageController;
    }

    public void addEmergencyListener(NetworkController.EmergencyListener emergencyListener) {
        this.mCallbackHandler.setListening(emergencyListener, true);
        this.mCallbackHandler.setEmergencyCallsOnly(isEmergencyOnly());
    }

    public boolean hasMobileDataFeature() {
        return this.mHasMobileDataFeature;
    }

    public boolean hasVoiceCallingFeature() {
        return this.mPhone.getPhoneType() != 0;
    }

    private MobileSignalController getDataController() {
        int activeDataSubId = this.mSubDefaults.getActiveDataSubId();
        if (!SubscriptionManager.isValidSubscriptionId(activeDataSubId)) {
            if (DEBUG) {
                Log.e("NetworkController", "No data sim selected");
            }
            return this.mDefaultSignalController;
        } else if (this.mMobileSignalControllers.indexOfKey(activeDataSubId) >= 0) {
            return this.mMobileSignalControllers.get(activeDataSubId);
        } else {
            if (DEBUG) {
                Log.e("NetworkController", "Cannot find controller for data sub: " + activeDataSubId);
            }
            return this.mDefaultSignalController;
        }
    }

    public String getMobileDataNetworkName() {
        MobileSignalController dataController = getDataController();
        return dataController != null ? ((MobileSignalController.MobileState) dataController.getState()).networkNameData : "";
    }

    public int getNumberSubscriptions() {
        return this.mMobileSignalControllers.size();
    }

    /* access modifiers changed from: package-private */
    public boolean isDataControllerDisabled() {
        MobileSignalController dataController = getDataController();
        if (dataController == null) {
            return false;
        }
        return dataController.isDataDisabled();
    }

    /* access modifiers changed from: private */
    public void notifyControllersMobileDataChanged() {
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).onMobileDataChanged();
        }
    }

    public boolean isEmergencyOnly() {
        if (this.mMobileSignalControllers.size() == 0) {
            this.mEmergencySource = 0;
            ServiceState serviceState = this.mLastServiceState;
            if (serviceState == null || !serviceState.isEmergencyOnly()) {
                return false;
            }
            return true;
        }
        int defaultVoiceSubId = this.mSubDefaults.getDefaultVoiceSubId();
        if (!SubscriptionManager.isValidSubscriptionId(defaultVoiceSubId)) {
            for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
                MobileSignalController valueAt = this.mMobileSignalControllers.valueAt(i);
                if (!((MobileSignalController.MobileState) valueAt.getState()).isEmergency) {
                    this.mEmergencySource = valueAt.mSubscriptionInfo.getSubscriptionId() + 100;
                    if (DEBUG) {
                        Log.d("NetworkController", "Found emergency " + valueAt.mTag);
                    }
                    return false;
                }
            }
        }
        if (this.mMobileSignalControllers.indexOfKey(defaultVoiceSubId) >= 0) {
            this.mEmergencySource = defaultVoiceSubId + 200;
            if (DEBUG) {
                Log.d("NetworkController", "Getting emergency from " + defaultVoiceSubId);
            }
            return ((MobileSignalController.MobileState) this.mMobileSignalControllers.get(defaultVoiceSubId).getState()).isEmergency;
        } else if (this.mMobileSignalControllers.size() == 1) {
            this.mEmergencySource = this.mMobileSignalControllers.keyAt(0) + 400;
            if (DEBUG) {
                Log.d("NetworkController", "Getting assumed emergency from " + this.mMobileSignalControllers.keyAt(0));
            }
            return ((MobileSignalController.MobileState) this.mMobileSignalControllers.valueAt(0).getState()).isEmergency;
        } else {
            if (DEBUG) {
                Log.e("NetworkController", "Cannot find controller for voice sub: " + defaultVoiceSubId);
            }
            this.mEmergencySource = defaultVoiceSubId + 300;
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void recalculateEmergency() {
        boolean isEmergencyOnly = isEmergencyOnly();
        this.mIsEmergency = isEmergencyOnly;
        this.mCallbackHandler.setEmergencyCallsOnly(isEmergencyOnly);
    }

    public void addCallback(NetworkController.SignalCallback signalCallback) {
        signalCallback.setSubs(this.mCurrentSubscriptions);
        signalCallback.setIsAirplaneMode(new NetworkController.IconState(this.mAirplaneMode, TelephonyIcons.FLIGHT_MODE_ICON, C0021R$string.accessibility_airplane_mode, this.mContext));
        signalCallback.setNoSims(this.mHasNoSubs, this.mSimDetected);
        this.mWifiSignalController.notifyListeners(signalCallback);
        this.mEthernetSignalController.notifyListeners(signalCallback);
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).notifyListeners(signalCallback);
        }
        this.mCallbackHandler.setListening(signalCallback, true);
    }

    public void removeCallback(NetworkController.SignalCallback signalCallback) {
        this.mCallbackHandler.setListening(signalCallback, false);
    }

    public void setWifiEnabled(final boolean z) {
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            public Void doInBackground(Void... voidArr) {
                NetworkControllerImpl.this.mWifiManager.setWifiEnabled(z);
                return null;
            }
        }.execute(new Void[0]);
    }

    /* access modifiers changed from: private */
    public void onUserSwitched(int i) {
        this.mCurrentUserId = i;
        this.mAccessPoints.onUserSwitched(i);
        updateConnectivity();
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReceive(android.content.Context r5, android.content.Intent r6) {
        /*
            r4 = this;
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r0 = "onReceive: intent="
            r5.append(r0)
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            java.lang.String r0 = "NetworkController"
            android.util.Log.d(r0, r5)
            java.lang.String r5 = r6.getAction()
            int r0 = r5.hashCode()
            r1 = -1
            r2 = 0
            switch(r0) {
                case -2104353374: goto L_0x00a6;
                case -2064771159: goto L_0x009b;
                case -2047452593: goto L_0x0090;
                case -1859256000: goto L_0x0085;
                case -1859255999: goto L_0x007a;
                case -1465084191: goto L_0x0070;
                case -1172645946: goto L_0x0066;
                case -1138588223: goto L_0x005c;
                case -1082809675: goto L_0x0051;
                case -1076576821: goto L_0x0046;
                case -229777127: goto L_0x003b;
                case -25388475: goto L_0x0030;
                case 623179603: goto L_0x0025;
                default: goto L_0x0023;
            }
        L_0x0023:
            goto L_0x00b0
        L_0x0025:
            java.lang.String r0 = "android.net.conn.INET_CONDITION_ACTION"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x00b0
            r5 = 1
            goto L_0x00b1
        L_0x0030:
            java.lang.String r0 = "android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x00b0
            r5 = 4
            goto L_0x00b1
        L_0x003b:
            java.lang.String r0 = "android.intent.action.SIM_STATE_CHANGED"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x00b0
            r5 = 5
            goto L_0x00b1
        L_0x0046:
            java.lang.String r0 = "android.intent.action.AIRPLANE_MODE"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x00b0
            r5 = 2
            goto L_0x00b1
        L_0x0051:
            java.lang.String r0 = "android.intent.action.RADIO_TECHNOLOGY"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x00b0
            r5 = 10
            goto L_0x00b1
        L_0x005c:
            java.lang.String r0 = "android.telephony.action.CARRIER_CONFIG_CHANGED"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x00b0
            r5 = 7
            goto L_0x00b1
        L_0x0066:
            java.lang.String r0 = "android.net.conn.CONNECTIVITY_CHANGE"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x00b0
            r5 = r2
            goto L_0x00b1
        L_0x0070:
            java.lang.String r0 = "android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x00b0
            r5 = 3
            goto L_0x00b1
        L_0x007a:
            java.lang.String r0 = "miui.intent.action.ACTION_ENHANCED_4G_LTE_MODE_CHANGE_FOR_SLOT2"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x00b0
            r5 = 12
            goto L_0x00b1
        L_0x0085:
            java.lang.String r0 = "miui.intent.action.ACTION_ENHANCED_4G_LTE_MODE_CHANGE_FOR_SLOT1"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x00b0
            r5 = 11
            goto L_0x00b1
        L_0x0090:
            java.lang.String r0 = "android.intent.action.ACTION_SPEECH_CODEC_IS_HD"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x00b0
            r5 = 9
            goto L_0x00b1
        L_0x009b:
            java.lang.String r0 = "android.intent.action.ACTION_IMS_REGISTED"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x00b0
            r5 = 8
            goto L_0x00b1
        L_0x00a6:
            java.lang.String r0 = "android.intent.action.SERVICE_STATE"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x00b0
            r5 = 6
            goto L_0x00b1
        L_0x00b0:
            r5 = r1
        L_0x00b1:
            java.lang.String r0 = "phone"
            java.lang.String r3 = "android.telephony.extra.SUBSCRIPTION_INDEX"
            switch(r5) {
                case 0: goto L_0x01c6;
                case 1: goto L_0x01c6;
                case 2: goto L_0x01bf;
                case 3: goto L_0x01bb;
                case 4: goto L_0x0192;
                case 5: goto L_0x0185;
                case 6: goto L_0x016f;
                case 7: goto L_0x015c;
                case 8: goto L_0x0110;
                case 9: goto L_0x00d7;
                default: goto L_0x00b8;
            }
        L_0x00b8:
            int r5 = r6.getIntExtra(r3, r1)
            boolean r0 = android.telephony.SubscriptionManager.isValidSubscriptionId(r5)
            if (r0 == 0) goto L_0x01ce
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r0 = r4.mMobileSignalControllers
            int r0 = r0.indexOfKey(r5)
            if (r0 < 0) goto L_0x01ca
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r4 = r4.mMobileSignalControllers
            java.lang.Object r4 = r4.get(r5)
            com.android.systemui.statusbar.policy.MobileSignalController r4 = (com.android.systemui.statusbar.policy.MobileSignalController) r4
            r4.handleBroadcast(r6)
            goto L_0x01d3
        L_0x00d7:
            int r5 = r6.getIntExtra(r0, r1)
            int r0 = r6.getIntExtra(r3, r1)
            java.lang.String r1 = "is_hd"
            boolean r6 = r6.getBooleanExtra(r1, r2)
            if (r5 < 0) goto L_0x00f0
            boolean[] r1 = r4.mVolte
            int r1 = r1.length
            if (r5 >= r1) goto L_0x00f0
            boolean[] r1 = r4.mSpeechHd
            r1[r5] = r6
        L_0x00f0:
            boolean r5 = android.telephony.SubscriptionManager.isValidSubscriptionId(r0)
            if (r5 == 0) goto L_0x01d3
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r5 = r4.mMobileSignalControllers
            int r5 = r5.indexOfKey(r0)
            if (r5 < 0) goto L_0x010b
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r4 = r4.mMobileSignalControllers
            java.lang.Object r4 = r4.get(r0)
            com.android.systemui.statusbar.policy.MobileSignalController r4 = (com.android.systemui.statusbar.policy.MobileSignalController) r4
            r4.setSpeechHd(r6)
            goto L_0x01d3
        L_0x010b:
            r4.updateMobileControllers()
            goto L_0x01d3
        L_0x0110:
            int r5 = r6.getIntExtra(r0, r1)
            int r0 = r6.getIntExtra(r3, r1)
            java.lang.String r1 = "state"
            boolean r1 = r6.getBooleanExtra(r1, r2)
            java.lang.String r3 = "wfc_state"
            boolean r6 = r6.getBooleanExtra(r3, r2)
            if (r5 < 0) goto L_0x0131
            boolean[] r2 = r4.mVolte
            int r3 = r2.length
            if (r5 >= r3) goto L_0x0131
            r2[r5] = r1
            boolean[] r2 = r4.mVowifi
            r2[r5] = r6
        L_0x0131:
            boolean r5 = android.telephony.SubscriptionManager.isValidSubscriptionId(r0)
            if (r5 == 0) goto L_0x01d3
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r5 = r4.mMobileSignalControllers
            int r5 = r5.indexOfKey(r0)
            if (r5 < 0) goto L_0x0157
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r5 = r4.mMobileSignalControllers
            java.lang.Object r5 = r5.get(r0)
            com.android.systemui.statusbar.policy.MobileSignalController r5 = (com.android.systemui.statusbar.policy.MobileSignalController) r5
            r5.setVolte(r1)
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r4 = r4.mMobileSignalControllers
            java.lang.Object r4 = r4.get(r0)
            com.android.systemui.statusbar.policy.MobileSignalController r4 = (com.android.systemui.statusbar.policy.MobileSignalController) r4
            r4.setVowifi(r6)
            goto L_0x01d3
        L_0x0157:
            r4.updateMobileControllers()
            goto L_0x01d3
        L_0x015c:
            android.content.Context r5 = r4.mContext
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r5 = com.android.systemui.statusbar.policy.NetworkControllerImpl.Config.readConfig(r5)
            r4.mConfig = r5
            android.os.Handler r5 = r4.mReceiverHandler
            com.android.systemui.statusbar.policy.-$$Lambda$ybM43k5QVX_SxWbQACu1XwL3Knk r6 = new com.android.systemui.statusbar.policy.-$$Lambda$ybM43k5QVX_SxWbQACu1XwL3Knk
            r6.<init>()
            r5.post(r6)
            goto L_0x01d3
        L_0x016f:
            android.os.Bundle r5 = r6.getExtras()
            android.telephony.ServiceState r5 = android.telephony.ServiceState.newFromBundle(r5)
            r4.mLastServiceState = r5
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r5 = r4.mMobileSignalControllers
            int r5 = r5.size()
            if (r5 != 0) goto L_0x01d3
            r4.recalculateEmergency()
            goto L_0x01d3
        L_0x0185:
            java.lang.String r5 = "rebroadcastOnUnlock"
            boolean r5 = r6.getBooleanExtra(r5, r2)
            if (r5 == 0) goto L_0x018e
            goto L_0x01d3
        L_0x018e:
            r4.updateMobileControllers()
            goto L_0x01d3
        L_0x0192:
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r5 = r4.mMobileSignalControllers
            int r5 = r5.size()
            if (r2 >= r5) goto L_0x01a8
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r5 = r4.mMobileSignalControllers
            java.lang.Object r5 = r5.valueAt(r2)
            com.android.systemui.statusbar.policy.MobileSignalController r5 = (com.android.systemui.statusbar.policy.MobileSignalController) r5
            r5.handleBroadcast(r6)
            int r2 = r2 + 1
            goto L_0x0192
        L_0x01a8:
            android.content.Context r5 = r4.mContext
            com.android.systemui.statusbar.policy.NetworkControllerImpl$Config r5 = com.android.systemui.statusbar.policy.NetworkControllerImpl.Config.readConfig(r5)
            r4.mConfig = r5
            android.os.Handler r5 = r4.mReceiverHandler
            com.android.systemui.statusbar.policy.-$$Lambda$ybM43k5QVX_SxWbQACu1XwL3Knk r6 = new com.android.systemui.statusbar.policy.-$$Lambda$ybM43k5QVX_SxWbQACu1XwL3Knk
            r6.<init>()
            r5.post(r6)
            goto L_0x01d3
        L_0x01bb:
            r4.recalculateEmergency()
            goto L_0x01d3
        L_0x01bf:
            r4.refreshLocale()
            r4.updateAirplaneMode(r2)
            goto L_0x01d3
        L_0x01c6:
            r4.updateConnectivity()
            goto L_0x01d3
        L_0x01ca:
            r4.updateMobileControllers()
            goto L_0x01d3
        L_0x01ce:
            com.android.systemui.statusbar.policy.MiuiWifiSignalController r4 = r4.mWifiSignalController
            r4.handleBroadcast(r6)
        L_0x01d3:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.NetworkControllerImpl.onReceive(android.content.Context, android.content.Intent):void");
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void handleConfigurationChanged() {
        updateMobileControllers();
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).setConfiguration(this.mConfig);
        }
        refreshLocale();
    }

    /* access modifiers changed from: private */
    public void updateMobileControllers() {
        if (this.mListening) {
            doUpdateMobileControllers();
        }
    }

    private void filterMobileSubscriptionInSameGroup(List<SubscriptionInfo> list) {
        if (list.size() == 2) {
            SubscriptionInfo subscriptionInfo = list.get(0);
            SubscriptionInfo subscriptionInfo2 = list.get(1);
            if (subscriptionInfo.getGroupUuid() != null && subscriptionInfo.getGroupUuid().equals(subscriptionInfo2.getGroupUuid())) {
                if (!subscriptionInfo.isOpportunistic() && !subscriptionInfo2.isOpportunistic()) {
                    return;
                }
                if (CarrierConfigManager.getDefaultConfig().getBoolean("always_show_primary_signal_bar_in_opportunistic_network_boolean")) {
                    if (!subscriptionInfo.isOpportunistic()) {
                        subscriptionInfo = subscriptionInfo2;
                    }
                    list.remove(subscriptionInfo);
                    return;
                }
                if (subscriptionInfo.getSubscriptionId() == this.mActiveMobileDataSubscription) {
                    subscriptionInfo = subscriptionInfo2;
                }
                list.remove(subscriptionInfo);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void doUpdateMobileControllers() {
        List<SubscriptionInfo> completeActiveSubscriptionInfoList = this.mSubscriptionManager.getCompleteActiveSubscriptionInfoList();
        if (completeActiveSubscriptionInfoList == null) {
            completeActiveSubscriptionInfoList = Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        List activeSubscriptionInfoList = miui.telephony.SubscriptionManager.getDefault().getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList != null) {
            for (SubscriptionInfo subscriptionInfo : completeActiveSubscriptionInfoList) {
                Iterator it = activeSubscriptionInfoList.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (((miui.telephony.SubscriptionInfo) it.next()).getSlotId() == subscriptionInfo.getSimSlotIndex()) {
                            arrayList.add(subscriptionInfo);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        filterMobileSubscriptionInSameGroup(arrayList);
        if (hasCorrectMobileControllers(arrayList)) {
            updateNoSims();
            return;
        }
        synchronized (this.mLock) {
            setCurrentSubscriptionsLocked(arrayList);
        }
        updateNoSims();
        recalculateEmergency();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void updateNoSims() {
        boolean z = this.mHasMobileDataFeature && this.mMobileSignalControllers.size() == 0;
        boolean hasAnySim = hasAnySim();
        if (z != this.mHasNoSubs || hasAnySim != this.mSimDetected) {
            this.mHasNoSubs = z;
            this.mSimDetected = hasAnySim;
            this.mCallbackHandler.setNoSims(z, hasAnySim);
        }
    }

    private boolean hasAnySim() {
        int activeModemCount = this.mPhone.getActiveModemCount();
        for (int i = 0; i < activeModemCount; i++) {
            int simState = this.mPhone.getSimState(i);
            if (simState != 1 && simState != 0) {
                return true;
            }
        }
        return false;
    }

    @GuardedBy({"mLock"})
    @VisibleForTesting
    public void setCurrentSubscriptionsLocked(List<SubscriptionInfo> list) {
        int i;
        List<SubscriptionInfo> list2 = list;
        Collections.sort(list2, new Comparator<SubscriptionInfo>(this) {
            public int compare(SubscriptionInfo subscriptionInfo, SubscriptionInfo subscriptionInfo2) {
                int i;
                int i2;
                if (subscriptionInfo.getSimSlotIndex() == subscriptionInfo2.getSimSlotIndex()) {
                    i2 = subscriptionInfo.getSubscriptionId();
                    i = subscriptionInfo2.getSubscriptionId();
                } else {
                    i2 = subscriptionInfo.getSimSlotIndex();
                    i = subscriptionInfo2.getSimSlotIndex();
                }
                return i2 - i;
            }
        });
        this.mCurrentSubscriptions = list2;
        SparseArray sparseArray = new SparseArray();
        for (int i2 = 0; i2 < this.mMobileSignalControllers.size(); i2++) {
            sparseArray.put(this.mMobileSignalControllers.keyAt(i2), this.mMobileSignalControllers.valueAt(i2));
        }
        this.mMobileSignalControllers.clear();
        int size = list.size();
        int i3 = 0;
        while (i3 < size) {
            int subscriptionId = list2.get(i3).getSubscriptionId();
            if (sparseArray.indexOfKey(subscriptionId) >= 0) {
                this.mMobileSignalControllers.put(subscriptionId, (MobileSignalController) sparseArray.get(subscriptionId));
                sparseArray.remove(subscriptionId);
                i = size;
            } else {
                SubscriptionDefaults subscriptionDefaults = this.mSubDefaults;
                MobileSignalController mobileSignalController = r0;
                SubscriptionDefaults subscriptionDefaults2 = subscriptionDefaults;
                i = size;
                MobileSignalController mobileSignalController2 = new MobileSignalController(this.mContext, this.mConfig, this.mHasMobileDataFeature, this.mPhone.createForSubscriptionId(subscriptionId), this.mCallbackHandler, this, list2.get(i3), subscriptionDefaults2, this.mReceiverHandler.getLooper());
                mobileSignalController.setUserSetupComplete(this.mUserSetup);
                this.mMobileSignalControllers.put(subscriptionId, mobileSignalController);
                if (list2.get(i3).getSimSlotIndex() == 0) {
                    this.mDefaultSignalController = mobileSignalController;
                }
                if (this.mListening) {
                    mobileSignalController.registerListener();
                }
            }
            i3++;
            size = i;
        }
        if (this.mListening) {
            for (int i4 = 0; i4 < sparseArray.size(); i4++) {
                int keyAt = sparseArray.keyAt(i4);
                if (sparseArray.get(keyAt) == this.mDefaultSignalController) {
                    this.mDefaultSignalController = null;
                }
                ((MobileSignalController) sparseArray.get(keyAt)).unregisterListener();
            }
        }
        this.mCallbackHandler.setSubs(list2);
        notifyAllListeners();
        pushConnectivityToSignals();
        updateAirplaneMode(true);
    }

    /* access modifiers changed from: private */
    public void setUserSetupComplete(boolean z) {
        this.mReceiverHandler.post(new Runnable(z) {
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                NetworkControllerImpl.this.lambda$setUserSetupComplete$2$NetworkControllerImpl(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: handleSetUserSetupComplete */
    public void lambda$setUserSetupComplete$2(boolean z) {
        this.mUserSetup = z;
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).setUserSetupComplete(this.mUserSetup);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean hasCorrectMobileControllers(List<SubscriptionInfo> list) {
        if (list.size() != this.mMobileSignalControllers.size()) {
            return false;
        }
        for (SubscriptionInfo subscriptionId : list) {
            if (this.mMobileSignalControllers.indexOfKey(subscriptionId.getSubscriptionId()) < 0) {
                return false;
            }
        }
        return true;
    }

    private void updateAirplaneMode(boolean z) {
        boolean z2 = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
            z2 = false;
        }
        if (z2 != this.mAirplaneMode || z) {
            this.mAirplaneMode = z2;
            for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
                this.mMobileSignalControllers.valueAt(i).setAirplaneMode(this.mAirplaneMode);
            }
            notifyListeners();
        }
    }

    private void refreshLocale() {
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        if (!locale.equals(this.mLocale)) {
            this.mLocale = locale;
            this.mWifiSignalController.refreshLocale();
            notifyAllListeners();
        }
    }

    private void notifyAllListeners() {
        notifyListeners();
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).notifyListeners();
        }
        this.mWifiSignalController.notifyListeners();
        this.mEthernetSignalController.notifyListeners();
    }

    private void notifyListeners() {
        this.mCallbackHandler.setIsAirplaneMode(new NetworkController.IconState(this.mAirplaneMode, TelephonyIcons.FLIGHT_MODE_ICON, C0021R$string.accessibility_airplane_mode, this.mContext));
        this.mCallbackHandler.setNoSims(this.mHasNoSubs, this.mSimDetected);
    }

    /* access modifiers changed from: private */
    public void updateConnectivity() {
        this.mWifiSignalController.updateWifiNoNetwork();
        this.mConnectedTransports.clear();
        this.mValidatedTransports.clear();
        for (NetworkCapabilities networkCapabilities : this.mConnectivityManager.getDefaultNetworkCapabilitiesForUser(this.mCurrentUserId)) {
            for (int i : networkCapabilities.getTransportTypes()) {
                this.mConnectedTransports.set(i);
                if (networkCapabilities.hasCapability(16)) {
                    this.mValidatedTransports.set(i);
                }
            }
        }
        if (this.mForceCellularValidated) {
            this.mValidatedTransports.set(0);
        }
        if (CHATTY) {
            Log.d("NetworkController", "updateConnectivity: mConnectedTransports=" + this.mConnectedTransports);
            Log.d("NetworkController", "updateConnectivity: mValidatedTransports=" + this.mValidatedTransports);
        }
        this.mInetCondition = !this.mValidatedTransports.isEmpty();
        pushConnectivityToSignals();
    }

    private void pushConnectivityToSignals() {
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        }
        this.mWifiSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        this.mEthernetSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        Class cls = FiveGControllerImpl.class;
        printWriter.println("NetworkController state:");
        printWriter.println("  - telephony ------");
        printWriter.print("  hasVoiceCallingFeature()=");
        printWriter.println(hasVoiceCallingFeature());
        printWriter.println("  mListening=" + this.mListening);
        printWriter.println("  - connectivity ------");
        printWriter.print("  mConnectedTransports=");
        printWriter.println(this.mConnectedTransports);
        printWriter.print("  mValidatedTransports=");
        printWriter.println(this.mValidatedTransports);
        printWriter.print("  mInetCondition=");
        printWriter.println(this.mInetCondition);
        printWriter.print("  mAirplaneMode=");
        printWriter.println(this.mAirplaneMode);
        printWriter.print("  mLocale=");
        printWriter.println(this.mLocale);
        printWriter.print("  mLastServiceState=");
        printWriter.println(this.mLastServiceState);
        printWriter.print("  mIsEmergency=");
        printWriter.println(this.mIsEmergency);
        printWriter.print("  mEmergencySource=");
        printWriter.println(emergencyToString(this.mEmergencySource));
        printWriter.println("  - config ------");
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).dump(printWriter);
        }
        this.mWifiSignalController.dump(printWriter);
        this.mEthernetSignalController.dump(printWriter);
        this.mAccessPoints.dump(printWriter);
        if (Dependency.get(cls) != null) {
            ((FiveGControllerImpl) Dependency.get(cls)).dump(printWriter);
        }
    }

    private static final String emergencyToString(int i) {
        if (i > 300) {
            return "ASSUMED_VOICE_CONTROLLER(" + (i - 200) + ")";
        } else if (i > 300) {
            return "NO_SUB(" + (i - 300) + ")";
        } else if (i > 200) {
            return "VOICE_CONTROLLER(" + (i - 200) + ")";
        } else if (i <= 100) {
            return i == 0 ? "NO_CONTROLLERS" : "UNKNOWN_SOURCE";
        } else {
            return "FIRST_CONTROLLER(" + (i - 100) + ")";
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:210:0x03f3  */
    /* JADX WARNING: Removed duplicated region for block: B:216:0x0407  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x015f  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0177  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dispatchDemoCommand(java.lang.String r19, android.os.Bundle r20) {
        /*
            r18 = this;
            r0 = r18
            r1 = r19
            r2 = r20
            boolean r3 = r0.mDemoMode
            java.lang.String r4 = "NetworkController"
            r5 = 1
            if (r3 != 0) goto L_0x0037
            java.lang.String r3 = "enter"
            boolean r3 = r1.equals(r3)
            if (r3 == 0) goto L_0x0037
            boolean r1 = DEBUG
            if (r1 == 0) goto L_0x001e
            java.lang.String r1 = "Entering demo mode"
            android.util.Log.d(r4, r1)
        L_0x001e:
            r18.unregisterListeners()
            r0.mDemoMode = r5
            boolean r1 = r0.mInetCondition
            r0.mDemoInetCondition = r1
            com.android.systemui.statusbar.policy.MiuiWifiSignalController r1 = r0.mWifiSignalController
            com.android.systemui.statusbar.policy.SignalController$State r1 = r1.getState()
            com.android.systemui.statusbar.policy.MiuiWifiSignalController$WifiState r1 = (com.android.systemui.statusbar.policy.MiuiWifiSignalController.WifiState) r1
            r0.mDemoWifiState = r1
            java.lang.String r0 = "DemoMode"
            r1.ssid = r0
            goto L_0x0440
        L_0x0037:
            boolean r3 = r0.mDemoMode
            r6 = 0
            if (r3 == 0) goto L_0x0079
            java.lang.String r3 = "exit"
            boolean r3 = r1.equals(r3)
            if (r3 == 0) goto L_0x0079
            boolean r1 = DEBUG
            if (r1 == 0) goto L_0x004d
            java.lang.String r1 = "Exiting demo mode"
            android.util.Log.d(r4, r1)
        L_0x004d:
            r0.mDemoMode = r6
            r18.updateMobileControllers()
        L_0x0052:
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r1 = r0.mMobileSignalControllers
            int r1 = r1.size()
            if (r6 >= r1) goto L_0x0068
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r1 = r0.mMobileSignalControllers
            java.lang.Object r1 = r1.valueAt(r6)
            com.android.systemui.statusbar.policy.MobileSignalController r1 = (com.android.systemui.statusbar.policy.MobileSignalController) r1
            r1.resetLastState()
            int r6 = r6 + 1
            goto L_0x0052
        L_0x0068:
            com.android.systemui.statusbar.policy.MiuiWifiSignalController r1 = r0.mWifiSignalController
            r1.resetLastState()
            android.os.Handler r1 = r0.mReceiverHandler
            java.lang.Runnable r2 = r0.mRegisterListeners
            r1.post(r2)
            r18.notifyAllListeners()
            goto L_0x0440
        L_0x0079:
            boolean r3 = r0.mDemoMode
            if (r3 == 0) goto L_0x0440
            java.lang.String r3 = "network"
            boolean r1 = r1.equals(r3)
            if (r1 == 0) goto L_0x0440
            java.lang.String r1 = "airplane"
            java.lang.String r1 = r2.getString(r1)
            java.lang.String r3 = "show"
            if (r1 == 0) goto L_0x00a3
            boolean r1 = r1.equals(r3)
            com.android.systemui.statusbar.policy.CallbackHandler r4 = r0.mCallbackHandler
            com.android.systemui.statusbar.policy.NetworkController$IconState r7 = new com.android.systemui.statusbar.policy.NetworkController$IconState
            int r8 = com.android.systemui.statusbar.policy.TelephonyIcons.FLIGHT_MODE_ICON
            int r9 = com.android.systemui.C0021R$string.accessibility_airplane_mode
            android.content.Context r10 = r0.mContext
            r7.<init>(r1, r8, r9, r10)
            r4.setIsAirplaneMode(r7)
        L_0x00a3:
            java.lang.String r1 = "fully"
            java.lang.String r1 = r2.getString(r1)
            if (r1 == 0) goto L_0x00e6
            boolean r1 = java.lang.Boolean.parseBoolean(r1)
            r0.mDemoInetCondition = r1
            java.util.BitSet r1 = new java.util.BitSet
            r1.<init>()
            boolean r4 = r0.mDemoInetCondition
            if (r4 == 0) goto L_0x00c1
            com.android.systemui.statusbar.policy.MiuiWifiSignalController r4 = r0.mWifiSignalController
            int r4 = r4.mTransportType
            r1.set(r4)
        L_0x00c1:
            com.android.systemui.statusbar.policy.MiuiWifiSignalController r4 = r0.mWifiSignalController
            r4.updateConnectivity(r1, r1)
            r4 = r6
        L_0x00c7:
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r7 = r0.mMobileSignalControllers
            int r7 = r7.size()
            if (r4 >= r7) goto L_0x00e6
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r7 = r0.mMobileSignalControllers
            java.lang.Object r7 = r7.valueAt(r4)
            com.android.systemui.statusbar.policy.MobileSignalController r7 = (com.android.systemui.statusbar.policy.MobileSignalController) r7
            boolean r8 = r0.mDemoInetCondition
            if (r8 == 0) goto L_0x00e0
            int r8 = r7.mTransportType
            r1.set(r8)
        L_0x00e0:
            r7.updateConnectivity(r1, r1)
            int r4 = r4 + 1
            goto L_0x00c7
        L_0x00e6:
            java.lang.String r1 = "wifi"
            java.lang.String r1 = r2.getString(r1)
            java.lang.String r7 = "inout"
            java.lang.String r8 = "out"
            java.lang.String r9 = "in"
            r11 = 110414(0x1af4e, float:1.54723E-40)
            r12 = 3365(0xd25, float:4.715E-42)
            java.lang.String r13 = "null"
            java.lang.String r14 = "activity"
            java.lang.String r15 = "level"
            r16 = -1
            if (r1 == 0) goto L_0x0199
            boolean r1 = r1.equals(r3)
            java.lang.String r6 = r2.getString(r15)
            if (r6 == 0) goto L_0x012f
            com.android.systemui.statusbar.policy.MiuiWifiSignalController$WifiState r4 = r0.mDemoWifiState
            boolean r17 = r6.equals(r13)
            if (r17 == 0) goto L_0x0116
            r6 = r16
            goto L_0x0122
        L_0x0116:
            int r6 = java.lang.Integer.parseInt(r6)
            int r17 = com.android.systemui.statusbar.policy.MiuiWifiIcons.WIFI_LEVEL_COUNT
            int r10 = r17 + -1
            int r6 = java.lang.Math.min(r6, r10)
        L_0x0122:
            r4.level = r6
            com.android.systemui.statusbar.policy.MiuiWifiSignalController$WifiState r4 = r0.mDemoWifiState
            int r6 = r4.level
            if (r6 < 0) goto L_0x012c
            r6 = r5
            goto L_0x012d
        L_0x012c:
            r6 = 0
        L_0x012d:
            r4.connected = r6
        L_0x012f:
            java.lang.String r4 = r2.getString(r14)
            if (r4 == 0) goto L_0x017e
            int r6 = r4.hashCode()
            if (r6 == r12) goto L_0x0153
            if (r6 == r11) goto L_0x014b
            r10 = 100357129(0x5fb5409, float:2.3634796E-35)
            if (r6 == r10) goto L_0x0143
            goto L_0x015b
        L_0x0143:
            boolean r4 = r4.equals(r7)
            if (r4 == 0) goto L_0x015b
            r4 = 0
            goto L_0x015d
        L_0x014b:
            boolean r4 = r4.equals(r8)
            if (r4 == 0) goto L_0x015b
            r4 = 2
            goto L_0x015d
        L_0x0153:
            boolean r4 = r4.equals(r9)
            if (r4 == 0) goto L_0x015b
            r4 = r5
            goto L_0x015d
        L_0x015b:
            r4 = r16
        L_0x015d:
            if (r4 == 0) goto L_0x0177
            if (r4 == r5) goto L_0x0171
            r6 = 2
            if (r4 == r6) goto L_0x016b
            com.android.systemui.statusbar.policy.MiuiWifiSignalController r4 = r0.mWifiSignalController
            r10 = 0
            r4.setActivity(r10)
            goto L_0x0184
        L_0x016b:
            com.android.systemui.statusbar.policy.MiuiWifiSignalController r4 = r0.mWifiSignalController
            r4.setActivity(r6)
            goto L_0x0184
        L_0x0171:
            com.android.systemui.statusbar.policy.MiuiWifiSignalController r4 = r0.mWifiSignalController
            r4.setActivity(r5)
            goto L_0x0184
        L_0x0177:
            com.android.systemui.statusbar.policy.MiuiWifiSignalController r4 = r0.mWifiSignalController
            r6 = 3
            r4.setActivity(r6)
            goto L_0x0184
        L_0x017e:
            com.android.systemui.statusbar.policy.MiuiWifiSignalController r4 = r0.mWifiSignalController
            r6 = 0
            r4.setActivity(r6)
        L_0x0184:
            java.lang.String r4 = "ssid"
            java.lang.String r4 = r2.getString(r4)
            if (r4 == 0) goto L_0x0190
            com.android.systemui.statusbar.policy.MiuiWifiSignalController$WifiState r6 = r0.mDemoWifiState
            r6.ssid = r4
        L_0x0190:
            com.android.systemui.statusbar.policy.MiuiWifiSignalController$WifiState r4 = r0.mDemoWifiState
            r4.enabled = r1
            com.android.systemui.statusbar.policy.MiuiWifiSignalController r1 = r0.mWifiSignalController
            r1.notifyListeners()
        L_0x0199:
            java.lang.String r1 = "sims"
            java.lang.String r1 = r2.getString(r1)
            r4 = 8
            if (r1 == 0) goto L_0x01f4
            int r1 = java.lang.Integer.parseInt(r1)
            int r1 = android.util.MathUtils.constrain(r1, r5, r4)
            java.util.ArrayList r6 = new java.util.ArrayList
            r6.<init>()
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r10 = r0.mMobileSignalControllers
            int r10 = r10.size()
            if (r1 == r10) goto L_0x01f4
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r10 = r0.mMobileSignalControllers
            r10.clear()
            android.telephony.SubscriptionManager r10 = r0.mSubscriptionManager
            int r10 = r10.getActiveSubscriptionInfoCountMax()
            r11 = r10
        L_0x01c4:
            int r12 = r10 + r1
            if (r11 >= r12) goto L_0x01d2
            android.telephony.SubscriptionInfo r12 = r0.addSignalController(r11, r11)
            r6.add(r12)
            int r11 = r11 + 1
            goto L_0x01c4
        L_0x01d2:
            com.android.systemui.statusbar.policy.CallbackHandler r1 = r0.mCallbackHandler
            r1.setSubs(r6)
            r1 = 0
        L_0x01d8:
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r6 = r0.mMobileSignalControllers
            int r6 = r6.size()
            if (r1 >= r6) goto L_0x01f4
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r6 = r0.mMobileSignalControllers
            int r6 = r6.keyAt(r1)
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r10 = r0.mMobileSignalControllers
            java.lang.Object r6 = r10.get(r6)
            com.android.systemui.statusbar.policy.MobileSignalController r6 = (com.android.systemui.statusbar.policy.MobileSignalController) r6
            r6.notifyListeners()
            int r1 = r1 + 1
            goto L_0x01d8
        L_0x01f4:
            java.lang.String r1 = "nosim"
            java.lang.String r1 = r2.getString(r1)
            if (r1 == 0) goto L_0x0209
            boolean r1 = r1.equals(r3)
            r0.mHasNoSubs = r1
            com.android.systemui.statusbar.policy.CallbackHandler r6 = r0.mCallbackHandler
            boolean r10 = r0.mSimDetected
            r6.setNoSims(r1, r10)
        L_0x0209:
            java.lang.String r1 = "mobile"
            java.lang.String r1 = r2.getString(r1)
            if (r1 == 0) goto L_0x041d
            boolean r1 = r1.equals(r3)
            java.lang.String r6 = "datatype"
            java.lang.String r6 = r2.getString(r6)
            java.lang.String r10 = "slot"
            java.lang.String r10 = r2.getString(r10)
            boolean r11 = android.text.TextUtils.isEmpty(r10)
            if (r11 == 0) goto L_0x0229
            r10 = 0
            goto L_0x022d
        L_0x0229:
            int r10 = java.lang.Integer.parseInt(r10)
        L_0x022d:
            r11 = 0
            int r4 = android.util.MathUtils.constrain(r10, r11, r4)
            java.util.ArrayList r10 = new java.util.ArrayList
            r10.<init>()
        L_0x0237:
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r11 = r0.mMobileSignalControllers
            int r11 = r11.size()
            if (r11 > r4) goto L_0x024d
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r11 = r0.mMobileSignalControllers
            int r11 = r11.size()
            android.telephony.SubscriptionInfo r11 = r0.addSignalController(r11, r11)
            r10.add(r11)
            goto L_0x0237
        L_0x024d:
            boolean r11 = r10.isEmpty()
            if (r11 != 0) goto L_0x0258
            com.android.systemui.statusbar.policy.CallbackHandler r11 = r0.mCallbackHandler
            r11.setSubs(r10)
        L_0x0258:
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r10 = r0.mMobileSignalControllers
            java.lang.Object r4 = r10.valueAt(r4)
            com.android.systemui.statusbar.policy.MobileSignalController r4 = (com.android.systemui.statusbar.policy.MobileSignalController) r4
            com.android.systemui.statusbar.policy.SignalController$State r10 = r4.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r10 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r10
            if (r6 == 0) goto L_0x026a
            r11 = r5
            goto L_0x026b
        L_0x026a:
            r11 = 0
        L_0x026b:
            r10.dataSim = r11
            com.android.systemui.statusbar.policy.SignalController$State r10 = r4.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r10 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r10
            if (r6 == 0) goto L_0x0277
            r11 = r5
            goto L_0x0278
        L_0x0277:
            r11 = 0
        L_0x0278:
            r10.isDefault = r11
            com.android.systemui.statusbar.policy.SignalController$State r10 = r4.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r10 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r10
            if (r6 == 0) goto L_0x0284
            r11 = r5
            goto L_0x0285
        L_0x0284:
            r11 = 0
        L_0x0285:
            r10.dataConnected = r11
            if (r6 == 0) goto L_0x033e
            com.android.systemui.statusbar.policy.SignalController$State r10 = r4.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r10 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r10
            java.lang.String r11 = "1x"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x029b
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.ONE_X
            goto L_0x033c
        L_0x029b:
            java.lang.String r11 = "3g"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x02a7
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.THREE_G
            goto L_0x033c
        L_0x02a7:
            java.lang.String r11 = "4g"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x02b3
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.FOUR_G
            goto L_0x033c
        L_0x02b3:
            java.lang.String r11 = "4g+"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x02bf
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.FOUR_G_PLUS
            goto L_0x033c
        L_0x02bf:
            java.lang.String r11 = "5g"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x02cb
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.NR_5G
            goto L_0x033c
        L_0x02cb:
            java.lang.String r11 = "5ge"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x02d7
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.LTE_CA_5G_E
            goto L_0x033c
        L_0x02d7:
            java.lang.String r11 = "5g+"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x02e2
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.NR_5G_PLUS
            goto L_0x033c
        L_0x02e2:
            java.lang.String r11 = "e"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x02ed
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.E
            goto L_0x033c
        L_0x02ed:
            java.lang.String r11 = "g"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x02f8
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.G
            goto L_0x033c
        L_0x02f8:
            java.lang.String r11 = "h"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x0303
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.H
            goto L_0x033c
        L_0x0303:
            java.lang.String r11 = "h+"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x030e
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.H_PLUS
            goto L_0x033c
        L_0x030e:
            java.lang.String r11 = "lte"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x0319
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.LTE
            goto L_0x033c
        L_0x0319:
            java.lang.String r11 = "lte+"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x0324
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.LTE_PLUS
            goto L_0x033c
        L_0x0324:
            java.lang.String r11 = "dis"
            boolean r11 = r6.equals(r11)
            if (r11 == 0) goto L_0x032f
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.DATA_DISABLED
            goto L_0x033c
        L_0x032f:
            java.lang.String r11 = "not"
            boolean r6 = r6.equals(r11)
            if (r6 == 0) goto L_0x033a
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.NOT_DEFAULT_DATA
            goto L_0x033c
        L_0x033a:
            com.android.systemui.statusbar.policy.MobileSignalController$MobileIconGroup r6 = com.android.systemui.statusbar.policy.TelephonyIcons.UNKNOWN
        L_0x033c:
            r10.iconGroup = r6
        L_0x033e:
            java.lang.String r6 = "roam"
            boolean r10 = r2.containsKey(r6)
            if (r10 == 0) goto L_0x0356
            com.android.systemui.statusbar.policy.SignalController$State r10 = r4.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r10 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r10
            java.lang.String r6 = r2.getString(r6)
            boolean r6 = r3.equals(r6)
            r10.roaming = r6
        L_0x0356:
            java.lang.String r6 = r2.getString(r15)
            if (r6 == 0) goto L_0x038e
            com.android.systemui.statusbar.policy.SignalController$State r10 = r4.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r10 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r10
            boolean r11 = r6.equals(r13)
            if (r11 == 0) goto L_0x036b
            r6 = r16
            goto L_0x0377
        L_0x036b:
            int r6 = java.lang.Integer.parseInt(r6)
            int r11 = android.telephony.CellSignalStrength.getNumSignalStrengthLevels()
            int r6 = java.lang.Math.min(r6, r11)
        L_0x0377:
            r10.level = r6
            com.android.systemui.statusbar.policy.SignalController$State r6 = r4.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r6 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r6
            com.android.systemui.statusbar.policy.SignalController$State r10 = r4.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r10 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r10
            int r10 = r10.level
            if (r10 < 0) goto L_0x038b
            r10 = r5
            goto L_0x038c
        L_0x038b:
            r10 = 0
        L_0x038c:
            r6.connected = r10
        L_0x038e:
            java.lang.String r6 = "inflate"
            boolean r10 = r2.containsKey(r6)
            if (r10 == 0) goto L_0x03b6
            r10 = 0
        L_0x0397:
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r11 = r0.mMobileSignalControllers
            int r11 = r11.size()
            if (r10 >= r11) goto L_0x03b6
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r11 = r0.mMobileSignalControllers
            java.lang.Object r11 = r11.valueAt(r10)
            com.android.systemui.statusbar.policy.MobileSignalController r11 = (com.android.systemui.statusbar.policy.MobileSignalController) r11
            java.lang.String r12 = r2.getString(r6)
            java.lang.String r13 = "true"
            boolean r12 = r13.equals(r12)
            r11.mInflateSignalStrengths = r12
            int r10 = r10 + 1
            goto L_0x0397
        L_0x03b6:
            java.lang.String r6 = r2.getString(r14)
            if (r6 == 0) goto L_0x040d
            com.android.systemui.statusbar.policy.SignalController$State r10 = r4.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r10 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r10
            r10.dataConnected = r5
            int r10 = r6.hashCode()
            r11 = 3365(0xd25, float:4.715E-42)
            if (r10 == r11) goto L_0x03e7
            r11 = 110414(0x1af4e, float:1.54723E-40)
            if (r10 == r11) goto L_0x03df
            r9 = 100357129(0x5fb5409, float:2.3634796E-35)
            if (r10 == r9) goto L_0x03d7
            goto L_0x03ef
        L_0x03d7:
            boolean r6 = r6.equals(r7)
            if (r6 == 0) goto L_0x03ef
            r6 = 0
            goto L_0x03f1
        L_0x03df:
            boolean r6 = r6.equals(r8)
            if (r6 == 0) goto L_0x03ef
            r6 = 2
            goto L_0x03f1
        L_0x03e7:
            boolean r6 = r6.equals(r9)
            if (r6 == 0) goto L_0x03ef
            r6 = r5
            goto L_0x03f1
        L_0x03ef:
            r6 = r16
        L_0x03f1:
            if (r6 == 0) goto L_0x0407
            if (r6 == r5) goto L_0x0402
            r7 = 2
            if (r6 == r7) goto L_0x03fd
            r6 = 0
            r4.setActivity(r6)
            goto L_0x0411
        L_0x03fd:
            r6 = 0
            r4.setActivity(r7)
            goto L_0x0411
        L_0x0402:
            r6 = 0
            r4.setActivity(r5)
            goto L_0x0411
        L_0x0407:
            r5 = 3
            r6 = 0
            r4.setActivity(r5)
            goto L_0x0411
        L_0x040d:
            r6 = 0
            r4.setActivity(r6)
        L_0x0411:
            com.android.systemui.statusbar.policy.SignalController$State r5 = r4.getState()
            com.android.systemui.statusbar.policy.MobileSignalController$MobileState r5 = (com.android.systemui.statusbar.policy.MobileSignalController.MobileState) r5
            r5.enabled = r1
            r4.notifyListeners()
            goto L_0x041e
        L_0x041d:
            r6 = 0
        L_0x041e:
            java.lang.String r1 = "carriernetworkchange"
            java.lang.String r1 = r2.getString(r1)
            if (r1 == 0) goto L_0x0440
            boolean r1 = r1.equals(r3)
        L_0x042a:
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r2 = r0.mMobileSignalControllers
            int r2 = r2.size()
            if (r6 >= r2) goto L_0x0440
            android.util.SparseArray<com.android.systemui.statusbar.policy.MobileSignalController> r2 = r0.mMobileSignalControllers
            java.lang.Object r2 = r2.valueAt(r6)
            com.android.systemui.statusbar.policy.MobileSignalController r2 = (com.android.systemui.statusbar.policy.MobileSignalController) r2
            r2.setCarrierNetworkChangeMode(r1)
            int r6 = r6 + 1
            goto L_0x042a
        L_0x0440:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.NetworkControllerImpl.dispatchDemoCommand(java.lang.String, android.os.Bundle):void");
    }

    private SubscriptionInfo addSignalController(int i, int i2) {
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo(i, "", i2, "", "", 0, 0, "", 0, (Bitmap) null, (String) null, (String) null, "", false, (UiccAccessRule[]) null, (String) null);
        MobileSignalController mobileSignalController = new MobileSignalController(this.mContext, this.mConfig, this.mHasMobileDataFeature, this.mPhone.createForSubscriptionId(subscriptionInfo.getSubscriptionId()), this.mCallbackHandler, this, subscriptionInfo, this.mSubDefaults, this.mReceiverHandler.getLooper());
        this.mMobileSignalControllers.put(i, mobileSignalController);
        ((MobileSignalController.MobileState) mobileSignalController.getState()).userSetup = true;
        return subscriptionInfo;
    }

    public boolean hasEmergencyCryptKeeperText() {
        return EncryptionHelper.IS_DATA_ENCRYPTED;
    }

    public boolean isRadioOn() {
        return !this.mAirplaneMode;
    }

    public boolean isVolteOn(int i) {
        if (i >= 0) {
            boolean[] zArr = this.mVolte;
            return i < zArr.length && zArr[i];
        }
    }

    public boolean isVowifiOn(int i) {
        if (i >= 0) {
            boolean[] zArr = this.mVowifi;
            return i < zArr.length && zArr[i];
        }
    }

    public boolean isSpeechHdOn(int i) {
        if (i >= 0) {
            boolean[] zArr = this.mSpeechHd;
            return i < zArr.length && zArr[i];
        }
    }

    private class SubListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        private SubListener() {
        }

        /* synthetic */ SubListener(NetworkControllerImpl networkControllerImpl, AnonymousClass1 r2) {
            this();
        }

        public void onSubscriptionsChanged() {
            NetworkControllerImpl.this.updateMobileControllers();
        }
    }

    public static class SubscriptionDefaults {
        public int getDefaultVoiceSubId() {
            return SubscriptionManager.getDefaultVoiceSubscriptionId();
        }

        public int getActiveDataSubId() {
            return SubscriptionManager.getActiveDataSubscriptionId();
        }
    }

    @VisibleForTesting
    static class Config {
        boolean alwaysShowDataRatIcon = false;
        boolean alwaysShowNetworkTypeIcon = false;
        boolean hspaDataDistinguishable;
        boolean show4gFor3g = false;
        boolean show4gForLte = false;
        boolean showAtLeast3G = false;
        boolean showRsrpSignalLevelforLTE = false;
        boolean showVolteIcon = false;
        boolean showVowifiIcon = false;

        Config() {
        }

        static Config readConfig(Context context) {
            Config config = new Config();
            Resources resources = context.getResources();
            config.showAtLeast3G = resources.getBoolean(C0010R$bool.config_showMin3G);
            resources.getBoolean(17891360);
            config.hspaDataDistinguishable = resources.getBoolean(C0010R$bool.config_hspa_data_distinguishable) && !Build.IS_CM_CUSTOMIZATION;
            resources.getBoolean(17891475);
            config.alwaysShowNetworkTypeIcon = context.getResources().getBoolean(C0010R$bool.config_alwaysShowTypeIcon);
            config.showRsrpSignalLevelforLTE = resources.getBoolean(C0010R$bool.config_showRsrpSignalLevelforLTE);
            resources.getBoolean(C0010R$bool.config_hideNoInternetState);
            config.showVolteIcon = resources.getBoolean(C0010R$bool.config_display_volte);
            SubscriptionManager.from(context);
            PersistableBundle configForSubId = ((CarrierConfigManager) context.getSystemService("carrier_config")).getConfigForSubId(SubscriptionManager.getDefaultDataSubscriptionId());
            if (configForSubId != null) {
                config.alwaysShowDataRatIcon = configForSubId.getBoolean("always_show_data_rat_icon_bool");
                config.show4gForLte = configForSubId.getBoolean("show_4g_for_lte_data_icon_bool");
                config.show4gFor3g = configForSubId.getBoolean("show_4g_for_3g_data_icon_bool");
                configForSubId.getBoolean("hide_lte_plus_data_icon_bool");
            }
            SystemProperties.getBoolean("persist.sysui.rat_icon_enhancement", false);
            config.showVowifiIcon = resources.getBoolean(C0010R$bool.config_display_vowifi);
            return config;
        }
    }
}
