package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityLte;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.ServiceState;
import android.util.LocalLog;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.statusbar.policy.MobileSignalController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import miui.os.Build;
import miui.os.SystemProperties;
import miui.telephony.SubscriptionManager;
import miui.telephony.TelephonyManager;
import org.codeaurora.internal.BearerAllocationStatus;
import org.codeaurora.internal.Client;
import org.codeaurora.internal.DcParam;
import org.codeaurora.internal.IExtTelephony;
import org.codeaurora.internal.INetworkCallback;
import org.codeaurora.internal.NetworkCallbackBase;
import org.codeaurora.internal.NrConfigType;
import org.codeaurora.internal.NrIconType;
import org.codeaurora.internal.ServiceUtil;
import org.codeaurora.internal.SignalStrength;
import org.codeaurora.internal.Status;
import org.codeaurora.internal.Token;
import org.codeaurora.internal.UpperLayerIndInfo;

public class MiuiFiveGServiceClient {
    private static final boolean DEBUG = true;
    public static final int[] RSRP_THRESH_LENIENT = {-140, -125, -115, -110, -102};
    private static final HashMap<String, LocalLog> sLocalLogs = new HashMap<>();
    ContentObserver m5gEnabledObserver = new ContentObserver(this.mHandler) {
        /* class com.android.systemui.statusbar.policy.MiuiFiveGServiceClient.AnonymousClass6 */

        public void onChange(boolean z) {
            MiuiFiveGServiceClient.this.update5GIcon();
        }
    };
    private boolean[] m5gIconCarrierOptimization = new boolean[TelephonyManager.getDefault().getPhoneCount()];
    private int mBindRetryTimes = 0;
    @VisibleForTesting
    INetworkCallback mCallback = new NetworkCallbackBase() {
        /* class com.android.systemui.statusbar.policy.MiuiFiveGServiceClient.AnonymousClass4 */

        public void on5gStatus(int i, Token token, Status status, boolean z) throws RemoteException {
            MiuiFiveGServiceClient.localLog("on5gStatus", "on5gStatus: slotId= " + i + " token=" + token + " status=" + status + " enableStatus=" + z);
        }

        public void onNrDcParam(int i, Token token, Status status, DcParam dcParam) throws RemoteException {
            MiuiFiveGServiceClient.localLog("onNrDcParam", "onNrDcParam: slotId=" + i + " token=" + token + " status=" + status + " dcParam=" + dcParam);
            if (status.get() == 1) {
                FiveGServiceState currentServiceState = MiuiFiveGServiceClient.this.getCurrentServiceState(i);
                currentServiceState.mDcnr = dcParam.getDcnr();
                MiuiFiveGServiceClient.this.update5GIcon(currentServiceState, i);
                MiuiFiveGServiceClient.this.notifyListenersIfNecessary(i);
            }
        }

        public void onSignalStrength(int i, Token token, Status status, SignalStrength signalStrength) throws RemoteException {
            MiuiFiveGServiceClient.localLog("onSignalStrength", "onSignalStrength: slotId=" + i + " token=" + token + " status=" + status + " signalStrength=" + signalStrength + " mIsCustForJpKd=" + MiuiFiveGServiceClient.this.mIsCustForJpKd);
            if (status.get() == 1 && signalStrength != null && !MiuiFiveGServiceClient.this.mIsCustForJpKd) {
                MiuiFiveGServiceClient.this.getCurrentServiceState(i).mLevel = MiuiFiveGServiceClient.this.getRsrpLevel(signalStrength.getRsrp());
                MiuiFiveGServiceClient.this.notifyListenersIfNecessary(i);
            }
        }

        public void onAnyNrBearerAllocation(int i, Token token, Status status, BearerAllocationStatus bearerAllocationStatus) throws RemoteException {
            MiuiFiveGServiceClient.localLog("onAnyNrBearerAllocation", "onAnyNrBearerAllocation: slotId=" + i + " token=" + token + " status=" + status + " bearerStatus=" + bearerAllocationStatus.get());
            if (status.get() == 1) {
                FiveGServiceState currentServiceState = MiuiFiveGServiceClient.this.getCurrentServiceState(i);
                currentServiceState.mBearerAllocationStatus = bearerAllocationStatus.get();
                MiuiFiveGServiceClient.this.update5GIcon(currentServiceState, i);
                MiuiFiveGServiceClient.this.notifyListenersIfNecessary(i);
            }
        }

        public void onUpperLayerIndInfo(int i, Token token, Status status, UpperLayerIndInfo upperLayerIndInfo) throws RemoteException {
            MiuiFiveGServiceClient.localLog("onUpperLayerIndInfo", "onUpperLayerIndInfo: slotId=" + i + " token=" + token + " status=" + status + " plmn=" + upperLayerIndInfo.getPlmnInfoListAvailable() + " upperLayerIndInfo=" + upperLayerIndInfo.getUpperLayerIndInfoAvailable());
            if (status.get() == 1) {
                FiveGServiceState currentServiceState = MiuiFiveGServiceClient.this.getCurrentServiceState(i);
                currentServiceState.mPlmn = upperLayerIndInfo.getPlmnInfoListAvailable();
                currentServiceState.mUpperLayerInd = upperLayerIndInfo.getUpperLayerIndInfoAvailable();
                MiuiFiveGServiceClient.this.update5GIcon(currentServiceState, i);
                MiuiFiveGServiceClient.this.notifyListenersIfNecessary(i);
            }
        }

        public void on5gConfigInfo(int i, Token token, Status status, NrConfigType nrConfigType) throws RemoteException {
            MiuiFiveGServiceClient.localLog("on5gConfigInfo", "on5gConfigInfo: slotId = " + i + " token = " + token + " status" + status + " NrConfigType = " + nrConfigType);
            if (status.get() == 1) {
                FiveGServiceState currentServiceState = MiuiFiveGServiceClient.this.getCurrentServiceState(i);
                currentServiceState.mNrConfigType = nrConfigType.get();
                MiuiFiveGServiceClient.this.update5GIcon(currentServiceState, i);
                MiuiFiveGServiceClient.this.notifyListenersIfNecessary(i);
            }
        }

        public void onNrIconType(int i, Token token, Status status, NrIconType nrIconType) throws RemoteException {
            MiuiFiveGServiceClient.localLog("onNrIconType", "onNrIconType: slotId = " + i + " token = " + token + " status" + status + " NrIconType = " + nrIconType);
            if (status.get() == 1) {
                FiveGServiceState currentServiceState = MiuiFiveGServiceClient.this.getCurrentServiceState(i);
                currentServiceState.mNrIconType = nrIconType.get();
                MiuiFiveGServiceClient.this.update5GIcon(currentServiceState, i);
                MiuiFiveGServiceClient.this.notifyListenersIfNecessary(i);
            }
        }
    };
    private Client mClient;
    private Context mContext;
    private final SparseArray<FiveGServiceState> mCurrentServiceStates = new SparseArray<>();
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.android.systemui.statusbar.policy.MiuiFiveGServiceClient.AnonymousClass3 */

        public void binderDied() {
            if (MiuiFiveGServiceClient.this.mNetworkService != null) {
                MiuiFiveGServiceClient.this.mNetworkService.asBinder().unlinkToDeath(MiuiFiveGServiceClient.this.mDeathRecipient, 0);
            }
            MiuiFiveGServiceClient.this.mServiceConnected = false;
            MiuiFiveGServiceClient.this.mNetworkService = null;
            MiuiFiveGServiceClient.this.mClient = null;
            MiuiFiveGServiceClient.this.mHandler.removeMessages(1024);
            MiuiFiveGServiceClient.this.mBindRetryTimes = 0;
            MiuiFiveGServiceClient.localLog("binderDied", "unlinkToDeath  has been completed, binderService is going");
            MiuiFiveGServiceClient.this.binderService();
        }
    };
    private int mDefaultDataSlotId;
    private Method mGetCustomedRsrpThresholdsMethod;
    private Handler mHandler = new Handler() {
        /* class com.android.systemui.statusbar.policy.MiuiFiveGServiceClient.AnonymousClass1 */

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1024:
                    MiuiFiveGServiceClient.this.binderService();
                    return;
                case 1025:
                    MiuiFiveGServiceClient.this.initFiveGServiceState();
                    return;
                case 1026:
                    MiuiFiveGServiceClient.this.mIsDelayUpdate5GIcon[((Integer) message.obj).intValue()] = false;
                    MiuiFiveGServiceClient.this.update5GIcon();
                    return;
                default:
                    return;
            }
        }
    };
    private int mInitRetryTimes = 0;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.policy.MiuiFiveGServiceClient.AnonymousClass8 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("miui.intent.action.ACTION_DEFAULT_DATA_SLOT_CHANGED")) {
                    MiuiFiveGServiceClient.this.update5GIcon();
                } else if (action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    int i = intent.getExtras().getInt("android.telephony.extra.SLOT_INDEX");
                    MiuiFiveGServiceClient.this.update5gIconCarrierOptimization(i);
                    if (i >= 0 && i < MiuiFiveGServiceClient.this.m5gIconCarrierOptimization.length && MiuiFiveGServiceClient.this.m5gIconCarrierOptimization[i]) {
                        MiuiFiveGServiceClient.this.update5GIcon(MiuiFiveGServiceClient.this.getCurrentServiceState(i), i);
                        MiuiFiveGServiceClient.this.notifyListenersIfNecessary(i);
                    }
                }
            }
        }
    };
    private boolean mIsCustForJpKd;
    private boolean mIsCustForKrOps;
    private boolean[] mIsDelayUpdate5GIcon = null;
    private boolean mIsDualNrEnabled = false;
    private boolean mIsUserFiveGEnabled = true;
    private int[] mLastBearerAllocationStatus = null;
    private final SparseArray<FiveGServiceState> mLastServiceStates = new SparseArray<>();
    private final SparseArray<MobileSignalController> mMobileSignalControllers = new SparseArray<>();
    ContentObserver mNetworkDisplayObserver = new ContentObserver(this.mHandler) {
        /* class com.android.systemui.statusbar.policy.MiuiFiveGServiceClient.AnonymousClass7 */

        public void onChange(boolean z) {
            MiuiFiveGServiceClient.this.update5GIcon();
        }
    };
    private IExtTelephony mNetworkService;
    private String mPackageName;
    private boolean mServiceConnected;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.android.systemui.statusbar.policy.MiuiFiveGServiceClient.AnonymousClass2 */

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MiuiFiveGServiceClient.localLog("onServiceConnected", "onServiceConnected name=" + componentName + ", service=" + iBinder);
            try {
                MiuiFiveGServiceClient.this.mNetworkService = IExtTelephony.Stub.asInterface(iBinder);
                MiuiFiveGServiceClient.this.mClient = MiuiFiveGServiceClient.this.mNetworkService.registerCallback(MiuiFiveGServiceClient.this.mPackageName, MiuiFiveGServiceClient.this.mCallback);
                MiuiFiveGServiceClient.this.mServiceConnected = true;
                MiuiFiveGServiceClient.this.initFiveGServiceState();
                iBinder.linkToDeath(MiuiFiveGServiceClient.this.mDeathRecipient, 0);
                Log.d("FiveGServiceClient", "Client = " + MiuiFiveGServiceClient.this.mClient);
            } catch (Exception e) {
                Log.d("FiveGServiceClient", "onServiceConnected: Exception = " + e);
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            MiuiFiveGServiceClient.localLog("onServiceDisconnected", "onServiceDisconnected name=" + componentName + " bindRetryTimes=" + MiuiFiveGServiceClient.this.mBindRetryTimes);
            cleanup();
        }

        public void onBindingDied(ComponentName componentName) {
            MiuiFiveGServiceClient.localLog("onBindingDied", "onBindingDied name=" + componentName + ", bindRetryTimes=" + MiuiFiveGServiceClient.this.mBindRetryTimes);
            cleanup();
            if (MiuiFiveGServiceClient.this.mBindRetryTimes < 4) {
                Log.d("FiveGServiceClient", "try to re-bind");
                MiuiFiveGServiceClient.this.mHandler.sendEmptyMessageDelayed(1024, (long) ((MiuiFiveGServiceClient.this.mBindRetryTimes * 2000) + 3000));
            }
        }

        private void cleanup() {
            Log.d("FiveGServiceClient", "cleanup");
            MiuiFiveGServiceClient.this.mServiceConnected = false;
            MiuiFiveGServiceClient.this.mNetworkService = null;
            MiuiFiveGServiceClient.this.mClient = null;
        }
    };
    @VisibleForTesting
    final SparseArray<IFiveGStateListener> mStatesListeners = new SparseArray<>();

    public interface IFiveGStateListener {
        void onStateChanged(FiveGServiceState fiveGServiceState);
    }

    static {
        Log.isLoggable("FiveGServiceClient", 3);
    }

    public static class FiveGServiceState {
        private int mBearerAllocationStatus = 0;
        private int mDcnr = 0;
        private MobileSignalController.MobileIconGroup mIconGroup = TelephonyIcons.UNKNOWN;
        private int mLevel = 0;
        private int mNrConfigType = 0;
        private int mNrIconType = -1;
        private int mPlmn = 0;
        private int mUpperLayerInd = 0;

        public boolean isConnectedOnSaMode() {
            return this.mNrConfigType == 1 && this.mIconGroup != TelephonyIcons.UNKNOWN;
        }

        public boolean isConnectedOnNsaMode() {
            return this.mNrConfigType == 0 && this.mIconGroup != TelephonyIcons.UNKNOWN;
        }

        public MobileSignalController.MobileIconGroup getIconGroup() {
            return this.mIconGroup;
        }

        @VisibleForTesting
        public int getSignalLevel() {
            return this.mLevel;
        }

        @VisibleForTesting
        public int getAllocated() {
            return this.mBearerAllocationStatus;
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public int getNrConfigType() {
            return this.mNrConfigType;
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public int getDcnr() {
            return this.mDcnr;
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public int getPlmn() {
            return this.mPlmn;
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public int getUpperLayerInd() {
            return this.mUpperLayerInd;
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public int getNrIconType() {
            return this.mNrIconType;
        }

        public void copyFrom(FiveGServiceState fiveGServiceState) {
            this.mBearerAllocationStatus = fiveGServiceState.mBearerAllocationStatus;
            this.mPlmn = fiveGServiceState.mPlmn;
            this.mUpperLayerInd = fiveGServiceState.mUpperLayerInd;
            this.mDcnr = fiveGServiceState.mDcnr;
            this.mLevel = fiveGServiceState.mLevel;
            this.mNrConfigType = fiveGServiceState.mNrConfigType;
            this.mIconGroup = fiveGServiceState.mIconGroup;
            this.mNrIconType = fiveGServiceState.mNrIconType;
        }

        public boolean equals(FiveGServiceState fiveGServiceState) {
            return this.mBearerAllocationStatus == fiveGServiceState.mBearerAllocationStatus && this.mPlmn == fiveGServiceState.mPlmn && this.mUpperLayerInd == fiveGServiceState.mUpperLayerInd && this.mDcnr == fiveGServiceState.mDcnr && this.mLevel == fiveGServiceState.mLevel && this.mNrConfigType == fiveGServiceState.mNrConfigType && this.mIconGroup == fiveGServiceState.mIconGroup && this.mNrIconType == fiveGServiceState.mNrIconType;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("mBearerAllocationStatus=");
            sb.append(this.mBearerAllocationStatus);
            sb.append(", ");
            sb.append("mPlmn=");
            sb.append(this.mPlmn);
            sb.append(", ");
            sb.append("mUpperLayerInd=");
            sb.append(this.mUpperLayerInd);
            sb.append(", ");
            sb.append("mDcnr=" + this.mDcnr);
            sb.append(", ");
            sb.append("mLevel=");
            sb.append(this.mLevel);
            sb.append(", ");
            sb.append("mNrConfigType=");
            sb.append(this.mNrConfigType);
            sb.append(", ");
            sb.append("mIconGroup=");
            sb.append(this.mIconGroup);
            sb.append(", ");
            sb.append("mNrIconType=");
            sb.append(this.mNrIconType);
            return sb.toString();
        }
    }

    public MiuiFiveGServiceClient(Context context) {
        this.mContext = context;
        this.mPackageName = context.getPackageName();
        try {
            this.mIsCustForKrOps = ((Boolean) TelephonyManager.class.getMethod("isCustForKrOps", null).invoke(null, new Object[0])).booleanValue();
            this.mIsCustForJpKd = ((Boolean) TelephonyManager.class.getMethod("isCustForJpKd", null).invoke(null, new Object[0])).booleanValue();
        } catch (Exception e) {
            Log.e("FiveGServiceClient", "isCustForKrOps or mIsCustForJpKd Exception" + e);
        }
        if (this.mIsCustForKrOps) {
            registerNetworkDisplayEvents();
            int phoneCount = TelephonyManager.getDefault().getPhoneCount();
            this.mIsDelayUpdate5GIcon = new boolean[phoneCount];
            this.mLastBearerAllocationStatus = new int[phoneCount];
            for (int i = 0; i < phoneCount; i++) {
                this.mIsDelayUpdate5GIcon[i] = false;
                this.mLastBearerAllocationStatus[i] = 0;
            }
        }
        try {
            Method declaredMethod = Class.forName("android.telephony.MiuiCellSignalStrength").getDeclaredMethod("getCustomedRsrpThresholds", new Class[0]);
            this.mGetCustomedRsrpThresholdsMethod = declaredMethod;
            declaredMethod.setAccessible(true);
        } catch (Exception e2) {
            this.mGetCustomedRsrpThresholdsMethod = null;
            Log.e("FiveGServiceClient", "init can't find getCustomedRsrpThresholds.\n", e2);
        }
        registerFivegEvents();
    }

    public void registerListener(int i, IFiveGStateListener iFiveGStateListener) {
        Log.d("FiveGServiceClient", "registerListener phoneId=" + i);
        this.mStatesListeners.put(i, iFiveGStateListener);
        if (!isServiceConnected()) {
            binderService();
        } else {
            initFiveGServiceState(i);
        }
    }

    public void addMobileSignalController(MobileSignalController mobileSignalController) {
        Log.d("FiveGServiceClient", "addMobileSignalController phoneId=" + mobileSignalController.getSlot());
        if (SubscriptionManager.isValidSlotId(mobileSignalController.getSlot())) {
            this.mMobileSignalControllers.put(mobileSignalController.getSlot(), mobileSignalController);
        }
    }

    public void removeMobileSignalController(MobileSignalController mobileSignalController) {
        Log.d("FiveGServiceClient", "removeMobileSignalController phoneId=" + mobileSignalController.getSlot());
        if (SubscriptionManager.isValidSlotId(mobileSignalController.getSlot())) {
            this.mMobileSignalControllers.remove(mobileSignalController.getSlot());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void binderService() {
        boolean bindService = ServiceUtil.bindService(this.mContext, this.mServiceConnection);
        localLog("binderService", "binderService success=" + bindService + " bindRetryTimes=" + this.mBindRetryTimes + " maxRetryTimes=" + 4);
        if (!bindService && this.mBindRetryTimes < 4 && !this.mHandler.hasMessages(1024)) {
            this.mHandler.sendEmptyMessageDelayed(1024, (long) ((this.mBindRetryTimes * 2000) + 3000));
            this.mBindRetryTimes++;
        }
        update5GIcon();
    }

    public boolean isServiceConnected() {
        return this.mServiceConnected;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public FiveGServiceState getCurrentServiceState(int i) {
        return getServiceState(i, this.mCurrentServiceStates);
    }

    private FiveGServiceState getLastServiceState(int i) {
        return getServiceState(i, this.mLastServiceStates);
    }

    private static FiveGServiceState getServiceState(int i, SparseArray<FiveGServiceState> sparseArray) {
        FiveGServiceState fiveGServiceState = sparseArray.get(i);
        if (fiveGServiceState != null) {
            return fiveGServiceState;
        }
        FiveGServiceState fiveGServiceState2 = new FiveGServiceState();
        sparseArray.put(i, fiveGServiceState2);
        return fiveGServiceState2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getRsrpLevel(int i) {
        return getLevel(i, getCustomedRsrpThresholds());
    }

    private static int getLevel(int i, int[] iArr) {
        int i2;
        if (i < -140 || i > -44) {
            i2 = 0;
        } else {
            i2 = iArr.length;
            while (i2 > 0 && i < iArr[i2 - 1]) {
                i2--;
            }
        }
        if (DEBUG) {
            Log.d("FiveGServiceClient", "getLevel: value = " + i + ", level = " + i2);
        }
        return i2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyListenersIfNecessary(int i) {
        FiveGServiceState currentServiceState = getCurrentServiceState(i);
        FiveGServiceState lastServiceState = getLastServiceState(i);
        if (!currentServiceState.equals(lastServiceState)) {
            if (DEBUG) {
                Log.d("FiveGServiceClient", "phoneId(" + i + ") Change in state from " + lastServiceState + " \n\tto " + currentServiceState);
            }
            lastServiceState.copyFrom(currentServiceState);
            IFiveGStateListener iFiveGStateListener = this.mStatesListeners.get(i);
            if (iFiveGStateListener != null) {
                iFiveGStateListener.onStateChanged(currentServiceState);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initFiveGServiceState() {
        Log.d("FiveGServiceClient", "initFiveGServiceState size=" + this.mStatesListeners.size());
        for (int i = 0; i < this.mStatesListeners.size(); i++) {
            initFiveGServiceState(this.mStatesListeners.keyAt(i));
        }
    }

    private void initFiveGServiceState(int i) {
        localLog("initFiveGServiceState", "initFiveGServiceState initRetryTimes=" + this.mInitRetryTimes + " maxRetryTimes=" + 4 + " mNetworkService=" + this.mNetworkService + " mClient=" + this.mClient);
        if (this.mNetworkService != null && this.mClient != null) {
            Log.d("FiveGServiceClient", "query 5G service state for phoneId " + i);
            try {
                Log.d("FiveGServiceClient", "queryNrDcParam result:" + this.mNetworkService.queryNrDcParam(i, this.mClient));
                Log.d("FiveGServiceClient", "queryNrBearerAllocation result:" + this.mNetworkService.queryNrBearerAllocation(i, this.mClient));
                Log.d("FiveGServiceClient", "queryNrSignalStrength result:" + this.mNetworkService.queryNrSignalStrength(i, this.mClient));
                Log.d("FiveGServiceClient", "queryUpperLayerIndInfo result:" + this.mNetworkService.queryUpperLayerIndInfo(i, this.mClient));
                Log.d("FiveGServiceClient", "query5gConfigInfo result:" + this.mNetworkService.query5gConfigInfo(i, this.mClient));
                Log.d("FiveGServiceClient", "queryNrIconType result:" + this.mNetworkService.queryNrIconType(i, this.mClient));
            } catch (Exception e) {
                Log.d("FiveGServiceClient", "initFiveGServiceState: Exception = " + e);
                if (this.mInitRetryTimes < 4 && !this.mHandler.hasMessages(1025)) {
                    this.mHandler.sendEmptyMessageDelayed(1025, (long) ((this.mInitRetryTimes * 2000) + 3000));
                    this.mInitRetryTimes++;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void update5GIcon(FiveGServiceState fiveGServiceState, int i) {
        if (!this.mIsUserFiveGEnabled || (i != this.mDefaultDataSlotId && !this.mIsDualNrEnabled)) {
            fiveGServiceState.mIconGroup = TelephonyIcons.UNKNOWN;
        } else if (fiveGServiceState.mNrConfigType == 1) {
            fiveGServiceState.mIconGroup = getSaIcon(fiveGServiceState);
        } else if (fiveGServiceState.mNrConfigType == 0) {
            fiveGServiceState.mIconGroup = getNrIconGroup(fiveGServiceState, i);
            dualNrIconGroupOptimization();
        } else {
            fiveGServiceState.mIconGroup = TelephonyIcons.UNKNOWN;
        }
        setFiveGIndicatorProperties(fiveGServiceState.mIconGroup, i);
        localLog("update5GIcon slotId=" + i, "update5GIcon FiveGServiceState: " + fiveGServiceState + ", mIsUserFiveGEnabled=" + this.mIsUserFiveGEnabled + ", mIsDualNrEnabled=" + this.mIsDualNrEnabled + ", cmccSim=" + isCmccSimCard(i));
    }

    private MobileSignalController.MobileIconGroup getSaIcon(FiveGServiceState fiveGServiceState) {
        if (fiveGServiceState.mBearerAllocationStatus > 0 || fiveGServiceState.mNrIconType == 1) {
            return TelephonyIcons.FIVE_G_SA;
        }
        return TelephonyIcons.UNKNOWN;
    }

    private MobileSignalController.MobileIconGroup getNrIconGroup(FiveGServiceState fiveGServiceState, int i) {
        MobileSignalController.MobileIconGroup mobileIconGroup = TelephonyIcons.UNKNOWN;
        if (this.mIsCustForKrOps) {
            return getCustKrNrIcon(fiveGServiceState, i);
        }
        if (("andromeda".equals(Build.DEVICE) && !isCustForSfrOps(i)) || "crux".equals(Build.DEVICE)) {
            return getConfigDIconGroup(fiveGServiceState);
        }
        if (!Build.IS_INTERNATIONAL_BUILD) {
            if ((!isCmccSimCard(i) || !Build.IS_CM_CUSTOMIZATION) && !Build.IS_CT_CUSTOMIZATION_TEST) {
                return getConfigDIconGroup(fiveGServiceState);
            }
            return getNrIconTypeIconGroup(fiveGServiceState);
        } else if (this.m5gIconCarrierOptimization[i]) {
            return getConfigDIconGroup(fiveGServiceState);
        } else {
            return getNrIconTypeIconGroup(fiveGServiceState);
        }
    }

    private void setFiveGIndicatorProperties(MobileSignalController.MobileIconGroup mobileIconGroup, int i) {
        if (mobileIconGroup == TelephonyIcons.FIVE_G_BASIC || mobileIconGroup == TelephonyIcons.FIVE_G_UWB) {
            SystemProperties.set("persist.sys.5g.indicator" + i, 20);
            return;
        }
        SystemProperties.set("persist.sys.5g.indicator" + i, 13);
    }

    private void dualNrIconGroupOptimization() {
        int otherSlotId = getOtherSlotId(this.mDefaultDataSlotId);
        FiveGServiceState currentServiceState = getCurrentServiceState(this.mDefaultDataSlotId);
        FiveGServiceState currentServiceState2 = getCurrentServiceState(otherSlotId);
        if (this.mIsDualNrEnabled && currentServiceState.mIconGroup == TelephonyIcons.FIVE_G_BASIC && currentServiceState2.mIconGroup != TelephonyIcons.FIVE_G_BASIC) {
            if (currentServiceState2.mUpperLayerInd != 1 || currentServiceState2.mPlmn != 1) {
                boolean isSameOperatorCard = isSameOperatorCard(this.mDefaultDataSlotId, otherSlotId);
                boolean isSameCell = isSameCell(this.mDefaultDataSlotId, otherSlotId);
                if (isSameOperatorCard && isSameCell) {
                    currentServiceState2.mIconGroup = TelephonyIcons.FIVE_G_BASIC;
                    notifyListenersIfNecessary(otherSlotId);
                }
                localLog("dualNrIconGroupOptimization", "isSameOperatorCard = " + isSameOperatorCard + ", isSameCell = " + isSameCell + ", dataSlotIdState = " + currentServiceState + ", viceSlotIdState = " + currentServiceState2 + ", mIsDualNrEnabled = " + this.mIsDualNrEnabled + ", mDefaultDataSlotId = " + this.mDefaultDataSlotId);
            }
        }
    }

    private boolean isSameCell(int i, int i2) {
        CellIdentityLte cellIdentityLte = getCellIdentityLte(i);
        CellIdentityLte cellIdentityLte2 = getCellIdentityLte(i2);
        if (cellIdentityLte == null || cellIdentityLte2 == null) {
            return false;
        }
        return cellIdentityLte.equals(cellIdentityLte2);
    }

    private CellIdentityLte getCellIdentityLte(int i) {
        ServiceState serviceState;
        CellIdentityLte cellIdentityLte = null;
        if (this.mMobileSignalControllers.get(i) == null || (serviceState = this.mMobileSignalControllers.get(i).getServiceState()) == null) {
            return null;
        }
        List networkRegistrationInfoListForTransportType = serviceState.getNetworkRegistrationInfoListForTransportType(1);
        if (!(networkRegistrationInfoListForTransportType == null || networkRegistrationInfoListForTransportType.get(0) == null)) {
            CellIdentity cellIdentity = ((NetworkRegistrationInfo) networkRegistrationInfoListForTransportType.get(0)).getCellIdentity();
            if (cellIdentity instanceof CellIdentityLte) {
                cellIdentityLte = (CellIdentityLte) cellIdentity;
            }
            localLog("getCellIdentityLte", "cellIdentity = " + cellIdentity + "cellIdentityLte = " + cellIdentityLte + ", slotId = " + i);
        }
        return cellIdentityLte;
    }

    private boolean isSameOperatorCard(int i, int i2) {
        return TelephonyManager.getDefault().isSameOperator(TelephonyManager.getDefault().getSimOperatorForSlot(i), TelephonyManager.getDefault().getSimOperatorForSlot(i2));
    }

    public int getOtherSlotId(int i) {
        for (int i2 = 0; i2 < TelephonyManager.getDefault().getPhoneCount(); i2++) {
            if (i2 != i) {
                return i2;
            }
        }
        return -1;
    }

    private MobileSignalController.MobileIconGroup getNrIconTypeIconGroup(FiveGServiceState fiveGServiceState) {
        MobileSignalController.MobileIconGroup mobileIconGroup = TelephonyIcons.UNKNOWN;
        int i = fiveGServiceState.mNrIconType;
        if (i == 1) {
            return TelephonyIcons.FIVE_G_BASIC;
        }
        if (i != 2) {
            return mobileIconGroup;
        }
        return TelephonyIcons.FIVE_G_UWB;
    }

    private MobileSignalController.MobileIconGroup getConfigDIconGroup(FiveGServiceState fiveGServiceState) {
        MobileSignalController.MobileIconGroup nrIconTypeIconGroup = getNrIconTypeIconGroup(fiveGServiceState);
        if (nrIconTypeIconGroup == TelephonyIcons.UNKNOWN) {
            return (fiveGServiceState.mBearerAllocationStatus > 0 || (fiveGServiceState.mUpperLayerInd == 1 && fiveGServiceState.mPlmn == 1)) ? TelephonyIcons.FIVE_G_BASIC : nrIconTypeIconGroup;
        }
        return nrIconTypeIconGroup;
    }

    private boolean isCmccSimCard(int i) {
        return TelephonyManager.getDefault().isSameOperator("46000", TelephonyManager.getDefault().getSimOperatorForSlot(i));
    }

    private MobileSignalController.MobileIconGroup getCustKrNrIcon(FiveGServiceState fiveGServiceState, int i) {
        MobileSignalController.MobileIconGroup mobileIconGroup = TelephonyIcons.UNKNOWN;
        if (fiveGServiceState.mNrIconType == 1 || fiveGServiceState.mNrIconType == 2) {
            mobileIconGroup = getKrFiveGIcon(fiveGServiceState, i);
        } else if (this.mHandler.hasMessages(1026, Integer.valueOf(i))) {
            this.mIsDelayUpdate5GIcon[i] = false;
            Log.d("FiveGServiceClient", "LTE connected removed DELAY_UPDATE_5GICON ");
            this.mHandler.removeMessages(1026, Integer.valueOf(i));
        }
        this.mLastBearerAllocationStatus[i] = fiveGServiceState.mBearerAllocationStatus;
        setLguIndicatorProperties(mobileIconGroup);
        Log.d("FiveGServiceClient", "getCustKrNrIcon krNrIcon = " + mobileIconGroup + "; phoneId=" + i);
        return mobileIconGroup;
    }

    private void setLguIndicatorProperties(MobileSignalController.MobileIconGroup mobileIconGroup) {
        if (mobileIconGroup == TelephonyIcons.FIVE_G_KR_ON) {
            SystemProperties.set("persist.sys.lgu.5g.indicator", 0);
        } else if (mobileIconGroup == TelephonyIcons.FIVE_G_KR_OFF) {
            SystemProperties.set("persist.sys.lgu.5g.indicator", 1);
        } else {
            SystemProperties.set("persist.sys.lgu.5g.indicator", 2);
        }
    }

    private boolean isCustForSfrOps(int i) {
        String simOperatorForSlot = TelephonyManager.getDefault().getSimOperatorForSlot(i);
        return "20810".equals(simOperatorForSlot) || "26806".equals(simOperatorForSlot);
    }

    private MobileSignalController.MobileIconGroup getKrFiveGIcon(FiveGServiceState fiveGServiceState, int i) {
        MobileSignalController.MobileIconGroup mobileIconGroup = TelephonyIcons.UNKNOWN;
        int i2 = Settings.Global.getInt(this.mContext.getContentResolver(), "setting_network_state_display", 1);
        if (fiveGServiceState.mBearerAllocationStatus > 0) {
            if (this.mHandler.hasMessages(1026, Integer.valueOf(i))) {
                this.mIsDelayUpdate5GIcon[i] = false;
                Log.d("FiveGServiceClient", "5G connected removed DELAY_UPDATE_5GICON ");
                this.mHandler.removeMessages(1026, Integer.valueOf(i));
            }
            mobileIconGroup = TelephonyIcons.FIVE_G_KR_ON;
        } else {
            if (this.mLastBearerAllocationStatus[i] > 0 && !this.mHandler.hasMessages(1026, Integer.valueOf(i))) {
                this.mIsDelayUpdate5GIcon[i] = true;
                Message obtainMessage = this.mHandler.obtainMessage(1026, Integer.valueOf(i));
                Log.d("FiveGServiceClient", "send DELAY_UPDATE_5GICON ");
                this.mHandler.sendMessageDelayed(obtainMessage, 11000);
            }
            if (this.mIsDelayUpdate5GIcon[i]) {
                Log.d("FiveGServiceClient", "isDelayUpdate5GIcon show 5G reverse icon");
                mobileIconGroup = TelephonyIcons.FIVE_G_KR_ON;
            } else if (i2 == 1 && fiveGServiceState.mUpperLayerInd == 1 && fiveGServiceState.mPlmn == 1) {
                mobileIconGroup = TelephonyIcons.FIVE_G_KR_OFF;
            }
        }
        Log.d("FiveGServiceClient", "getKrFiveGIcon isAvailNetworkDisplay = " + i2);
        return mobileIconGroup;
    }

    /* access modifiers changed from: private */
    public static void localLog(String str, String str2) {
        String str3 = "FiveGServiceClient." + str;
        synchronized (sLocalLogs) {
            if (!sLocalLogs.containsKey(str3)) {
                sLocalLogs.put(str3, new LocalLog(30));
            }
            sLocalLogs.get(str3).log(str2);
        }
        if (DEBUG) {
            Log.d("FiveGServiceClient", str2);
        }
    }

    public void dump(PrintWriter printWriter) {
        synchronized (sLocalLogs) {
            printWriter.println("FiveGServiceClient dump start:");
            printWriter.print("  mServiceConnected=");
            printWriter.println(this.mServiceConnected);
            printWriter.print("  mBindRetryTimes=");
            printWriter.println(this.mBindRetryTimes);
            printWriter.print("  mInitRetryTimes=");
            printWriter.println(this.mInitRetryTimes);
            printWriter.print("  mNetworkService=");
            printWriter.println(this.mNetworkService);
            printWriter.print("  mClient=");
            printWriter.println(this.mClient);
            for (Map.Entry<String, LocalLog> entry : sLocalLogs.entrySet()) {
                printWriter.println(entry.getKey());
                entry.getValue().dump((FileDescriptor) null, printWriter, (String[]) null);
            }
            printWriter.println("FiveGServiceClient dump end.");
            printWriter.flush();
        }
    }

    private void registerFivegEvents() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("fiveg_user_enable"), false, this.m5gEnabledObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("dual_nr_enabled"), false, new ContentObserver(this.mHandler) {
            /* class com.android.systemui.statusbar.policy.MiuiFiveGServiceClient.AnonymousClass5 */

            public void onChange(boolean z) {
                MiuiFiveGServiceClient.this.update5GIcon();
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("miui.intent.action.ACTION_DEFAULT_DATA_SLOT_CHANGED");
        intentFilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        this.mContext.registerReceiver(this.mIntentReceiver, intentFilter);
        update5GIcon();
    }

    private void registerNetworkDisplayEvents() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("setting_network_state_display"), false, this.mNetworkDisplayObserver);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void update5GIcon() {
        boolean z = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "fiveg_user_enable", 1) != 1) {
            z = false;
        }
        this.mIsUserFiveGEnabled = z;
        this.mIsDualNrEnabled = TelephonyManager.getDefault().isDualNrEnabled();
        this.mDefaultDataSlotId = SubscriptionManager.getDefault().getDefaultDataSlotId();
        localLog("5GEnabledChanged", "5G enable state has changed to " + this.mIsUserFiveGEnabled + ", mIsDualNrEnabled = " + this.mIsDualNrEnabled + ", dds is " + this.mDefaultDataSlotId);
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            update5GIcon(getCurrentServiceState(i), i);
            notifyListenersIfNecessary(i);
        }
    }

    public int[] getCustomedRsrpThresholds() {
        Method method = this.mGetCustomedRsrpThresholdsMethod;
        if (method == null) {
            return RSRP_THRESH_LENIENT;
        }
        try {
            return (int[]) method.invoke(null, new Object[0]);
        } catch (Exception e) {
            Log.e("FiveGServiceClient", "invoke getCustomedRsrpThresholds fail.\n", e);
            return RSRP_THRESH_LENIENT;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void update5gIconCarrierOptimization(int i) {
        int subscriptionIdForSlot = SubscriptionManager.getDefault().getSubscriptionIdForSlot(i);
        if (i >= 0) {
            boolean[] zArr = this.m5gIconCarrierOptimization;
            if (i < zArr.length) {
                zArr[i] = getCarrierConfigIconForSubId(subscriptionIdForSlot, "config_5g_icon_optimization", false);
                localLog("update5gIconCarrierOptimization", "slotId = " + i + ",subId = " + subscriptionIdForSlot + "," + this.m5gIconCarrierOptimization[i]);
            }
        }
    }

    private boolean getCarrierConfigIconForSubId(int i, String str, boolean z) {
        PersistableBundle configForSubId;
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        return (carrierConfigManager == null || (configForSubId = carrierConfigManager.getConfigForSubId(i)) == null) ? z : configForSubId.getBoolean(str, z);
    }
}
