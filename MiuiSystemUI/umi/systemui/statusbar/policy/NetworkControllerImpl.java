package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
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
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.MiuiCarrierTextController;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import miui.os.Build;

public class NetworkControllerImpl extends BroadcastReceiver implements NetworkController, DemoMode, DataUsageController.NetworkNameProvider, Dumpable {
    static final boolean CHATTY = Log.isLoggable("NetworkControllerChat", 3);
    static final boolean DEBUG = Log.isLoggable("NetworkController", 3);
    private final AccessPointControllerImpl mAccessPoints;
    private int mActiveMobileDataSubscription;
    private boolean mAirplaneMode;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final CallbackHandler mCallbackHandler;
    private final Runnable mClearForceValidated;
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
    private boolean mForceCellularValidated;
    private final boolean mHasMobileDataFeature;
    private boolean mHasNoSubs;
    private boolean mInetCondition;
    private boolean mIsEmergency;
    @VisibleForTesting
    ServiceState[] mLastServiceState;
    @VisibleForTesting
    boolean mListening;
    private Locale mLocale;
    private final Object mLock;
    @VisibleForTesting
    final SparseArray<MobileSignalController> mMobileSignalControllers;
    private final TelephonyManager mPhone;
    private PhoneStateListener mPhoneStateListener;
    private final Handler mReceiverHandler;
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
    private final WifiManager mWifiManager;
    @VisibleForTesting
    final MiuiWifiSignalController mWifiSignalController;

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.policy.NetworkControllerImpl$1  reason: invalid class name */
    public class AnonymousClass1 implements ConfigurationController.ConfigurationListener {
    }

    public NetworkControllerImpl(Context context, Looper looper, DeviceProvisionedController deviceProvisionedController, BroadcastDispatcher broadcastDispatcher, ConnectivityManager connectivityManager, TelephonyManager telephonyManager, WifiManager wifiManager, NetworkScoreManager networkScoreManager) {
        this(context, connectivityManager, telephonyManager, wifiManager, networkScoreManager, SubscriptionManager.from(context), Config.readConfig(context), looper, new CallbackHandler(), new AccessPointControllerImpl(context), new DataUsageController(context), new SubscriptionDefaults(), deviceProvisionedController, broadcastDispatcher);
        this.mReceiverHandler.post(this.mRegisterListeners);
    }

    @VisibleForTesting
    NetworkControllerImpl(Context context, ConnectivityManager connectivityManager, TelephonyManager telephonyManager, WifiManager wifiManager, NetworkScoreManager networkScoreManager, SubscriptionManager subscriptionManager, Config config, Looper looper, CallbackHandler callbackHandler, AccessPointControllerImpl accessPointControllerImpl, DataUsageController dataUsageController, SubscriptionDefaults subscriptionDefaults, final DeviceProvisionedController deviceProvisionedController, BroadcastDispatcher broadcastDispatcher) {
        this.mLock = new Object();
        this.mActiveMobileDataSubscription = -1;
        this.mMobileSignalControllers = new SparseArray<>();
        this.mConnectedTransports = new BitSet();
        this.mValidatedTransports = new BitSet();
        this.mAirplaneMode = false;
        this.mLocale = null;
        this.mCurrentSubscriptions = new ArrayList();
        this.mClearForceValidated = new Runnable() {
            /* class com.android.systemui.statusbar.policy.$$Lambda$NetworkControllerImpl$oNWIIIg3gBRqx9jT8qywGtEkW2E */

            public final void run() {
                NetworkControllerImpl.this.lambda$new$0$NetworkControllerImpl();
            }
        };
        this.mRegisterListeners = new Runnable() {
            /* class com.android.systemui.statusbar.policy.NetworkControllerImpl.AnonymousClass10 */

            public void run() {
                NetworkControllerImpl.this.registerListeners();
            }
        };
        this.mContext = context;
        this.mConfig = config;
        this.mReceiverHandler = new Handler(looper);
        this.mCallbackHandler = callbackHandler;
        this.mDataSaverController = new DataSaverControllerImpl(context);
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mSubscriptionManager = subscriptionManager;
        this.mSubDefaults = subscriptionDefaults;
        this.mConnectivityManager = connectivityManager;
        this.mHasMobileDataFeature = connectivityManager.isNetworkSupported(0);
        this.mPhone = telephonyManager;
        int activeModemCount = telephonyManager.getActiveModemCount();
        this.mVolte = new boolean[activeModemCount];
        this.mVowifi = new boolean[activeModemCount];
        this.mSpeechHd = new boolean[activeModemCount];
        this.mLastServiceState = new ServiceState[activeModemCount];
        this.mWifiManager = wifiManager;
        this.mLocale = this.mContext.getResources().getConfiguration().locale;
        this.mAccessPoints = accessPointControllerImpl;
        this.mDataUsageController = dataUsageController;
        dataUsageController.setNetworkController(this);
        this.mDataUsageController.setCallback(new DataUsageController.Callback() {
            /* class com.android.systemui.statusbar.policy.NetworkControllerImpl.AnonymousClass2 */

            @Override // com.android.settingslib.net.DataUsageController.Callback
            public void onMobileDataEnabled(boolean z) {
                NetworkControllerImpl.this.mCallbackHandler.setMobileDataEnabled(z);
                NetworkControllerImpl.this.notifyControllersMobileDataChanged();
            }
        });
        this.mWifiSignalController = new MiuiWifiSignalController(this.mContext, this.mHasMobileDataFeature, this.mCallbackHandler, this, this.mWifiManager, this.mConnectivityManager, networkScoreManager);
        this.mEthernetSignalController = new EthernetSignalController(this.mContext, this.mCallbackHandler, this);
        updateAirplaneMode(true);
        AnonymousClass3 r0 = new CurrentUserTracker(broadcastDispatcher) {
            /* class com.android.systemui.statusbar.policy.NetworkControllerImpl.AnonymousClass3 */

            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                NetworkControllerImpl.this.onUserSwitched(i);
            }
        };
        this.mUserTracker = r0;
        r0.startTracking();
        deviceProvisionedController.addCallback(new DeviceProvisionedController.DeviceProvisionedListener() {
            /* class com.android.systemui.statusbar.policy.NetworkControllerImpl.AnonymousClass4 */

            @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
            public void onUserSetupChanged() {
                NetworkControllerImpl networkControllerImpl = NetworkControllerImpl.this;
                DeviceProvisionedController deviceProvisionedController = deviceProvisionedController;
                networkControllerImpl.setUserSetupComplete(deviceProvisionedController.isUserSetup(deviceProvisionedController.getCurrentUser()));
            }
        });
        this.mConnectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            /* class com.android.systemui.statusbar.policy.NetworkControllerImpl.AnonymousClass5 */
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
            /* class com.android.systemui.statusbar.policy.$$Lambda$LfzJt661qZfn2w6SYHFbD3aMy0 */
            public final /* synthetic */ Handler f$0;

            {
                this.f$0 = r1;
            }

            public final void execute(Runnable runnable) {
                this.f$0.post(runnable);
            }
        }) {
            /* class com.android.systemui.statusbar.policy.NetworkControllerImpl.AnonymousClass6 */

            public void onActiveDataSubscriptionIdChanged(int i) {
                NetworkControllerImpl networkControllerImpl = NetworkControllerImpl.this;
                if (networkControllerImpl.keepCellularValidationBitInSwitch(networkControllerImpl.mActiveMobileDataSubscription, i)) {
                    if (NetworkControllerImpl.DEBUG) {
                        Log.d("NetworkController", ": mForceCellularValidated to true.");
                    }
                    NetworkControllerImpl.this.mForceCellularValidated = true;
                    NetworkControllerImpl.this.mReceiverHandler.removeCallbacks(NetworkControllerImpl.this.mClearForceValidated);
                    NetworkControllerImpl.this.mReceiverHandler.postDelayed(NetworkControllerImpl.this.mClearForceValidated, 2000);
                }
                NetworkControllerImpl.this.mActiveMobileDataSubscription = i;
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

    @Override // com.android.systemui.statusbar.policy.NetworkController
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
            this.mSubscriptionListener = new SubListener(this, null);
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
            /* class com.android.systemui.statusbar.policy.$$Lambda$NetworkControllerImpl$kkWH_IrhHJnwynPIgTUQ4s52BA4 */

            public final void run() {
                NetworkControllerImpl.this.updateConnectivity();
            }
        });
        Handler handler = this.mReceiverHandler;
        MiuiWifiSignalController miuiWifiSignalController = this.mWifiSignalController;
        Objects.requireNonNull(miuiWifiSignalController);
        handler.post(new Runnable() {
            /* class com.android.systemui.statusbar.policy.$$Lambda$xULI_xemYZCjWy9LoCf2vKPiuIc */

            public final void run() {
                MiuiWifiSignalController.this.fetchInitialState();
            }
        });
        updateMobileControllers();
        this.mReceiverHandler.post(new Runnable() {
            /* class com.android.systemui.statusbar.policy.$$Lambda$w0FuAr6MDKkUIV1rcwwlRClXFUs */

            public final void run() {
                NetworkControllerImpl.this.recalculateEmergency();
            }
        });
    }

    private void unregisterListeners() {
        this.mListening = false;
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).unregisterListener();
        }
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mSubscriptionListener);
        this.mBroadcastDispatcher.unregisterReceiver(this);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public NetworkController.AccessPointController getAccessPointController() {
        return this.mAccessPoints;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public DataUsageController getMobileDataController() {
        return this.mDataUsageController;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void addEmergencyListener(NetworkController.EmergencyListener emergencyListener) {
        this.mCallbackHandler.setListening(emergencyListener, true);
        this.mCallbackHandler.setEmergencyCallsOnly(isEmergencyOnly());
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
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

    @Override // com.android.systemui.statusbar.policy.NetworkController, com.android.settingslib.net.DataUsageController.NetworkNameProvider
    public String getMobileDataNetworkName() {
        MobileSignalController dataController = getDataController();
        return dataController != null ? ((MobileSignalController.MobileState) dataController.getState()).networkNameData : "";
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
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
    /* access modifiers changed from: public */
    private void notifyControllersMobileDataChanged() {
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).onMobileDataChanged();
        }
    }

    public boolean isEmergencyOnly() {
        if (this.mMobileSignalControllers.size() == 0) {
            this.mEmergencySource = 0;
            if (this.mLastServiceState != null) {
                int i = 0;
                while (true) {
                    ServiceState[] serviceStateArr = this.mLastServiceState;
                    if (i >= serviceStateArr.length) {
                        break;
                    } else if (serviceStateArr[i] != null && serviceStateArr[i].isEmergencyOnly()) {
                        return true;
                    } else {
                        i++;
                    }
                }
            }
            return false;
        }
        int defaultVoiceSubId = this.mSubDefaults.getDefaultVoiceSubId();
        if (!SubscriptionManager.isValidSubscriptionId(defaultVoiceSubId)) {
            for (int i2 = 0; i2 < this.mMobileSignalControllers.size(); i2++) {
                MobileSignalController valueAt = this.mMobileSignalControllers.valueAt(i2);
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

    @Override // com.android.systemui.statusbar.policy.NetworkController
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

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void removeCallback(NetworkController.SignalCallback signalCallback) {
        this.mCallbackHandler.setListening(signalCallback, false);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void setWifiEnabled(final boolean z) {
        new AsyncTask<Void, Void, Void>() {
            /* class com.android.systemui.statusbar.policy.NetworkControllerImpl.AnonymousClass7 */

            /* access modifiers changed from: protected */
            public Void doInBackground(Void... voidArr) {
                NetworkControllerImpl.this.mWifiManager.setWifiEnabled(z);
                return null;
            }
        }.execute(new Void[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserSwitched(int i) {
        this.mCurrentUserId = i;
        this.mAccessPoints.onUserSwitched(i);
        updateConnectivity();
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void onReceive(Context context, Intent intent) {
        char c;
        Log.d("NetworkController", "onReceive: intent=" + intent);
        String action = intent.getAction();
        switch (action.hashCode()) {
            case -2104353374:
                if (action.equals("android.intent.action.SERVICE_STATE")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -2064771159:
                if (action.equals("android.intent.action.ACTION_IMS_REGISTED")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -2047452593:
                if (action.equals("android.intent.action.ACTION_SPEECH_CODEC_IS_HD")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -1859256000:
                if (action.equals("miui.intent.action.ACTION_ENHANCED_4G_LTE_MODE_CHANGE_FOR_SLOT1")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -1859255999:
                if (action.equals("miui.intent.action.ACTION_ENHANCED_4G_LTE_MODE_CHANGE_FOR_SLOT2")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -1465084191:
                if (action.equals("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1172645946:
                if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1138588223:
                if (action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1082809675:
                if (action.equals("android.intent.action.RADIO_TECHNOLOGY")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -1076576821:
                if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -229777127:
                if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -25388475:
                if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 623179603:
                if (action.equals("android.net.conn.INET_CONDITION_ACTION")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
                updateConnectivity();
                return;
            case 2:
                refreshLocale();
                updateAirplaneMode(false);
                return;
            case 3:
                recalculateEmergency();
                return;
            case 4:
                break;
            case 5:
                if (!intent.getBooleanExtra("rebroadcastOnUnlock", false)) {
                    updateMobileControllers();
                    return;
                }
                return;
            case 6:
                int intExtra = intent.getIntExtra("android.telephony.extra.SLOT_INDEX", -1);
                if (intExtra != -1) {
                    this.mLastServiceState[intExtra] = ServiceState.newFromBundle(intent.getExtras());
                    if (this.mMobileSignalControllers.size() == 0) {
                        recalculateEmergency();
                        return;
                    }
                    return;
                }
                return;
            case 7:
                this.mConfig = Config.readConfig(this.mContext);
                this.mReceiverHandler.post(new Runnable() {
                    /* class com.android.systemui.statusbar.policy.$$Lambda$ybM43k5QVX_SxWbQACu1XwL3Knk */

                    public final void run() {
                        NetworkControllerImpl.this.handleConfigurationChanged();
                    }
                });
                return;
            case '\b':
                final int intExtra2 = intent.getIntExtra("phone", -1);
                int intExtra3 = intent.getIntExtra("android.telephony.extra.SUBSCRIPTION_INDEX", -1);
                boolean booleanExtra = intent.getBooleanExtra("state", false);
                final boolean booleanExtra2 = intent.getBooleanExtra("wfc_state", false);
                if (intExtra2 >= 0) {
                    boolean[] zArr = this.mVolte;
                    if (intExtra2 < zArr.length) {
                        zArr[intExtra2] = booleanExtra;
                        this.mVowifi[intExtra2] = booleanExtra2;
                    }
                }
                if (!SubscriptionManager.isValidSubscriptionId(intExtra3)) {
                    return;
                }
                if (this.mMobileSignalControllers.indexOfKey(intExtra3) >= 0) {
                    this.mMobileSignalControllers.get(intExtra3).setVolte(booleanExtra);
                    this.mMobileSignalControllers.get(intExtra3).setVowifi(booleanExtra2);
                    this.mCallbackHandler.post(new Runnable(this) {
                        /* class com.android.systemui.statusbar.policy.NetworkControllerImpl.AnonymousClass8 */

                        public void run() {
                            ((MiuiCarrierTextController) Dependency.get(MiuiCarrierTextController.class)).setVowifi(intExtra2, booleanExtra2);
                        }
                    });
                    return;
                }
                updateMobileControllers();
                return;
            case '\t':
                int intExtra4 = intent.getIntExtra("phone", -1);
                int intExtra5 = intent.getIntExtra("android.telephony.extra.SUBSCRIPTION_INDEX", -1);
                boolean booleanExtra3 = intent.getBooleanExtra("is_hd", false);
                if (intExtra4 >= 0 && intExtra4 < this.mVolte.length) {
                    this.mSpeechHd[intExtra4] = booleanExtra3;
                }
                if (!SubscriptionManager.isValidSubscriptionId(intExtra5)) {
                    return;
                }
                if (this.mMobileSignalControllers.indexOfKey(intExtra5) >= 0) {
                    this.mMobileSignalControllers.get(intExtra5).setSpeechHd(booleanExtra3);
                    return;
                } else {
                    updateMobileControllers();
                    return;
                }
            default:
                int intExtra6 = intent.getIntExtra("android.telephony.extra.SUBSCRIPTION_INDEX", -1);
                if (!SubscriptionManager.isValidSubscriptionId(intExtra6)) {
                    this.mWifiSignalController.handleBroadcast(intent);
                    return;
                } else if (this.mMobileSignalControllers.indexOfKey(intExtra6) >= 0) {
                    this.mMobileSignalControllers.get(intExtra6).handleBroadcast(intent);
                    return;
                } else {
                    updateMobileControllers();
                    return;
                }
        }
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).handleBroadcast(intent);
        }
        this.mConfig = Config.readConfig(this.mContext);
        this.mReceiverHandler.post(new Runnable() {
            /* class com.android.systemui.statusbar.policy.$$Lambda$ybM43k5QVX_SxWbQACu1XwL3Knk */

            public final void run() {
                NetworkControllerImpl.this.handleConfigurationChanged();
            }
        });
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
    /* access modifiers changed from: public */
    private void updateMobileControllers() {
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
        ArrayList arrayList = new ArrayList();
        for (SubscriptionInfo subscriptionInfo : completeActiveSubscriptionInfoList) {
            if (subscriptionInfo.areUiccApplicationsEnabled()) {
                arrayList.add(subscriptionInfo);
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
            if (!(simState == 1 || simState == 0)) {
                return true;
            }
        }
        return false;
    }

    @GuardedBy({"mLock"})
    @VisibleForTesting
    public void setCurrentSubscriptionsLocked(List<SubscriptionInfo> list) {
        int i;
        Collections.sort(list, new Comparator<SubscriptionInfo>(this) {
            /* class com.android.systemui.statusbar.policy.NetworkControllerImpl.AnonymousClass9 */

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
        this.mCurrentSubscriptions = list;
        SparseArray sparseArray = new SparseArray();
        for (int i2 = 0; i2 < this.mMobileSignalControllers.size(); i2++) {
            sparseArray.put(this.mMobileSignalControllers.keyAt(i2), this.mMobileSignalControllers.valueAt(i2));
        }
        this.mMobileSignalControllers.clear();
        int size = list.size();
        int i3 = 0;
        while (i3 < size) {
            int subscriptionId = list.get(i3).getSubscriptionId();
            if (sparseArray.indexOfKey(subscriptionId) < 0 || !NetworkControllerHelper.equalMccMnc(((MobileSignalController) sparseArray.get(subscriptionId)).getSubscriptionInfo(), list.get(i3))) {
                i = size;
                MobileSignalController mobileSignalController = new MobileSignalController(this.mContext, this.mConfig, this.mHasMobileDataFeature, this.mPhone.createForSubscriptionId(subscriptionId), this.mCallbackHandler, this, list.get(i3), this.mSubDefaults, this.mReceiverHandler.getLooper());
                mobileSignalController.setUserSetupComplete(this.mUserSetup);
                this.mMobileSignalControllers.put(subscriptionId, mobileSignalController);
                if (list.get(i3).getSimSlotIndex() == 0) {
                    this.mDefaultSignalController = mobileSignalController;
                }
                if (this.mListening) {
                    mobileSignalController.registerListener();
                }
            } else {
                this.mMobileSignalControllers.put(subscriptionId, (MobileSignalController) sparseArray.get(subscriptionId));
                sparseArray.remove(subscriptionId);
                i = size;
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
        this.mCallbackHandler.setSubs(list);
        notifyAllListeners();
        pushConnectivityToSignals();
        updateAirplaneMode(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setUserSetupComplete(boolean z) {
        this.mReceiverHandler.post(new Runnable(z) {
            /* class com.android.systemui.statusbar.policy.$$Lambda$NetworkControllerImpl$8S0AEfzpddaLuo_a_D4xFAJ8V58 */
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                NetworkControllerImpl.this.lambda$setUserSetupComplete$1$NetworkControllerImpl(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: handleSetUserSetupComplete */
    public void lambda$setUserSetupComplete$1(boolean z) {
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
        for (SubscriptionInfo subscriptionInfo : list) {
            if (this.mMobileSignalControllers.indexOfKey(subscriptionInfo.getSubscriptionId()) < 0) {
                return false;
            }
            MobileSignalController mobileSignalController = this.mMobileSignalControllers.get(subscriptionInfo.getSubscriptionId());
            if (!(mobileSignalController == null || NetworkControllerHelper.equalMccMnc(subscriptionInfo, mobileSignalController.getSubscriptionInfo()))) {
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
    /* access modifiers changed from: public */
    public void updateConnectivity() {
        this.mWifiSignalController.updateWifiNoNetwork();
        this.mConnectedTransports.clear();
        this.mValidatedTransports.clear();
        NetworkCapabilities[] defaultNetworkCapabilitiesForUser = this.mConnectivityManager.getDefaultNetworkCapabilitiesForUser(this.mCurrentUserId);
        for (NetworkCapabilities networkCapabilities : defaultNetworkCapabilitiesForUser) {
            int[] transportTypes = networkCapabilities.getTransportTypes();
            for (int i : transportTypes) {
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

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
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
        int i = 0;
        for (int i2 = 0; i2 < this.mMobileSignalControllers.size(); i2++) {
            this.mMobileSignalControllers.valueAt(i2).dump(printWriter);
        }
        this.mWifiSignalController.dump(printWriter);
        this.mEthernetSignalController.dump(printWriter);
        this.mAccessPoints.dump(printWriter);
        if (Dependency.get(FiveGControllerImpl.class) != null) {
            ((FiveGControllerImpl) Dependency.get(FiveGControllerImpl.class)).dump(printWriter);
        }
        if (this.mLastServiceState != null) {
            while (true) {
                ServiceState[] serviceStateArr = this.mLastServiceState;
                if (i < serviceStateArr.length) {
                    if (serviceStateArr[i] != null) {
                        printWriter.println("  mLastServiceState[" + i + "]: " + this.mLastServiceState[i].toString());
                    }
                    i++;
                } else {
                    return;
                }
            }
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
    @Override // com.android.systemui.DemoMode
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dispatchDemoCommand(java.lang.String r19, android.os.Bundle r20) {
        /*
        // Method dump skipped, instructions count: 1089
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.NetworkControllerImpl.dispatchDemoCommand(java.lang.String, android.os.Bundle):void");
    }

    private SubscriptionInfo addSignalController(int i, int i2) {
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo(i, "", i2, "", "", 0, 0, "", 0, null, null, null, "", false, null, null);
        MobileSignalController mobileSignalController = new MobileSignalController(this.mContext, this.mConfig, this.mHasMobileDataFeature, this.mPhone.createForSubscriptionId(subscriptionInfo.getSubscriptionId()), this.mCallbackHandler, this, subscriptionInfo, this.mSubDefaults, this.mReceiverHandler.getLooper());
        this.mMobileSignalControllers.put(i, mobileSignalController);
        ((MobileSignalController.MobileState) mobileSignalController.getState()).userSetup = true;
        return subscriptionInfo;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public boolean hasEmergencyCryptKeeperText() {
        return EncryptionHelper.IS_DATA_ENCRYPTED;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
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

    /* access modifiers changed from: private */
    public class SubListener extends SubscriptionManager.OnSubscriptionsChangedListener {
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

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class Config {
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
