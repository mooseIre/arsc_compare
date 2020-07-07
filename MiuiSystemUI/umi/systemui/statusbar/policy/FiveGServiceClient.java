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
import android.os.RemoteException;
import android.provider.Settings;
import android.util.LocalLog;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.MobileSignalController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import miui.os.Build;
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

public class FiveGServiceClient {
    private static final boolean DEBUG = true;
    private static final HashMap<String, LocalLog> sLocalLogs = new HashMap<>();
    ContentObserver m5gEnabledObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            FiveGServiceClient.this.update5GIcon();
        }
    };
    /* access modifiers changed from: private */
    public int mBindRetryTimes = 0;
    @VisibleForTesting
    INetworkCallback mCallback = new NetworkCallbackBase() {
        public void on5gStatus(int i, Token token, Status status, boolean z) throws RemoteException {
            FiveGServiceClient.localLog("on5gStatus", "on5gStatus: slotId= " + i + " token=" + token + " status=" + status + " enableStatus=" + z);
        }

        public void onNrDcParam(int i, Token token, Status status, DcParam dcParam) throws RemoteException {
            FiveGServiceClient.localLog("onNrDcParam", "onNrDcParam: slotId=" + i + " token=" + token + " status=" + status + " dcParam=" + dcParam);
            if (status.get() == 1) {
                FiveGServiceState currentServiceState = FiveGServiceClient.this.getCurrentServiceState(i);
                int unused = currentServiceState.mDcnr = dcParam.getDcnr();
                FiveGServiceClient.this.update5GIcon(currentServiceState, i);
                FiveGServiceClient.this.notifyListenersIfNecessary(i);
            }
        }

        public void onSignalStrength(int i, Token token, Status status, SignalStrength signalStrength) throws RemoteException {
            FiveGServiceClient.localLog("onSignalStrength", "onSignalStrength: slotId=" + i + " token=" + token + " status=" + status + " signalStrength=" + signalStrength);
            if (status.get() == 1 && signalStrength != null) {
                int unused = FiveGServiceClient.this.getCurrentServiceState(i).mLevel = FiveGServiceClient.this.getRsrpLevel(signalStrength.getRsrp());
                FiveGServiceClient.this.notifyListenersIfNecessary(i);
            }
        }

        public void onAnyNrBearerAllocation(int i, Token token, Status status, BearerAllocationStatus bearerAllocationStatus) throws RemoteException {
            FiveGServiceClient.localLog("onAnyNrBearerAllocation", "onAnyNrBearerAllocation: slotId=" + i + " token=" + token + " status=" + status + " bearerStatus=" + bearerAllocationStatus.get());
            if (status.get() == 1) {
                FiveGServiceState currentServiceState = FiveGServiceClient.this.getCurrentServiceState(i);
                int unused = currentServiceState.mBearerAllocationStatus = bearerAllocationStatus.get();
                FiveGServiceClient.this.update5GIcon(currentServiceState, i);
                FiveGServiceClient.this.notifyListenersIfNecessary(i);
            }
        }

        public void onUpperLayerIndInfo(int i, Token token, Status status, UpperLayerIndInfo upperLayerIndInfo) throws RemoteException {
            FiveGServiceClient.localLog("onUpperLayerIndInfo", "onUpperLayerIndInfo: slotId=" + i + " token=" + token + " status=" + status + " plmn=" + upperLayerIndInfo.getPlmnInfoListAvailable() + " upperLayerIndInfo=" + upperLayerIndInfo.getUpperLayerIndInfoAvailable());
            if (status.get() == 1) {
                FiveGServiceState currentServiceState = FiveGServiceClient.this.getCurrentServiceState(i);
                int unused = currentServiceState.mPlmn = upperLayerIndInfo.getPlmnInfoListAvailable();
                int unused2 = currentServiceState.mUpperLayerInd = upperLayerIndInfo.getUpperLayerIndInfoAvailable();
                FiveGServiceClient.this.update5GIcon(currentServiceState, i);
                FiveGServiceClient.this.notifyListenersIfNecessary(i);
            }
        }

        public void on5gConfigInfo(int i, Token token, Status status, NrConfigType nrConfigType) throws RemoteException {
            FiveGServiceClient.localLog("on5gConfigInfo", "on5gConfigInfo: slotId = " + i + " token = " + token + " status" + status + " NrConfigType = " + nrConfigType);
            if (status.get() == 1) {
                FiveGServiceState currentServiceState = FiveGServiceClient.this.getCurrentServiceState(i);
                int unused = currentServiceState.mNrConfigType = nrConfigType.get();
                FiveGServiceClient.this.update5GIcon(currentServiceState, i);
                FiveGServiceClient.this.notifyListenersIfNecessary(i);
            }
        }

        public void onNrIconType(int i, Token token, Status status, NrIconType nrIconType) throws RemoteException {
            FiveGServiceClient.localLog("onNrIconType", "onNrIconType: slotId = " + i + " token = " + token + " status" + status + " NrIconType = " + nrIconType);
            if (status.get() == 1) {
                FiveGServiceState currentServiceState = FiveGServiceClient.this.getCurrentServiceState(i);
                int unused = currentServiceState.mNrIconType = nrIconType.get();
                FiveGServiceClient.this.update5GIcon(currentServiceState, i);
                FiveGServiceClient.this.notifyListenersIfNecessary(i);
            }
        }
    };
    /* access modifiers changed from: private */
    public Client mClient;
    private Context mContext;
    private final SparseArray<FiveGServiceState> mCurrentServiceStates = new SparseArray<>();
    private int mDefaultDataSlotId;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1024:
                    FiveGServiceClient.this.binderService();
                    return;
                case 1025:
                    FiveGServiceClient.this.initFiveGServiceState();
                    return;
                case 1026:
                    FiveGServiceClient.this.mIsDelayUpdate5GIcon[((Integer) message.obj).intValue()] = false;
                    FiveGServiceClient.this.update5GIcon();
                    return;
                default:
                    return;
            }
        }
    };
    private int mInitRetryTimes = 0;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("miui.intent.action.ACTION_DEFAULT_DATA_SLOT_CHANGED")) {
                FiveGServiceClient.this.update5GIcon();
            }
        }
    };
    private boolean mIsCustForKrOps;
    /* access modifiers changed from: private */
    public boolean[] mIsDelayUpdate5GIcon = null;
    private boolean mIsUserFiveGEnabled = true;
    private int[] mLastBearerAllocationStatus = null;
    private final SparseArray<FiveGServiceState> mLastServiceStates = new SparseArray<>();
    ContentObserver mNetworkDisplayObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            FiveGServiceClient.this.update5GIcon();
        }
    };
    /* access modifiers changed from: private */
    public IExtTelephony mNetworkService;
    /* access modifiers changed from: private */
    public String mPackageName;
    private final int[] mRsrpThresholds;
    /* access modifiers changed from: private */
    public boolean mServiceConnected;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            FiveGServiceClient.localLog("onServiceConnected", "onServiceConnected name=" + componentName + ", service=" + iBinder);
            try {
                IExtTelephony unused = FiveGServiceClient.this.mNetworkService = IExtTelephony.Stub.asInterface(iBinder);
                Client unused2 = FiveGServiceClient.this.mClient = FiveGServiceClient.this.mNetworkService.registerCallback(FiveGServiceClient.this.mPackageName, FiveGServiceClient.this.mCallback);
                boolean unused3 = FiveGServiceClient.this.mServiceConnected = true;
                FiveGServiceClient.this.initFiveGServiceState();
                Log.d("FiveGServiceClient", "Client = " + FiveGServiceClient.this.mClient);
            } catch (Exception e) {
                Log.d("FiveGServiceClient", "onServiceConnected: Exception = " + e);
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            FiveGServiceClient.localLog("onServiceDisconnected", "onServiceDisconnected name=" + componentName + " bindRetryTimes=" + FiveGServiceClient.this.mBindRetryTimes);
            cleanup();
        }

        public void onBindingDied(ComponentName componentName) {
            FiveGServiceClient.localLog("onBindingDied", "onBindingDied name=" + componentName + ", bindRetryTimes=" + FiveGServiceClient.this.mBindRetryTimes);
            cleanup();
            if (FiveGServiceClient.this.mBindRetryTimes < 4) {
                Log.d("FiveGServiceClient", "try to re-bind");
                FiveGServiceClient.this.mHandler.sendEmptyMessageDelayed(1024, (long) ((FiveGServiceClient.this.mBindRetryTimes * 2000) + 3000));
            }
        }

        private void cleanup() {
            Log.d("FiveGServiceClient", "cleanup");
            boolean unused = FiveGServiceClient.this.mServiceConnected = false;
            IExtTelephony unused2 = FiveGServiceClient.this.mNetworkService = null;
            Client unused3 = FiveGServiceClient.this.mClient = null;
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
        /* access modifiers changed from: private */
        public int mBearerAllocationStatus = 0;
        /* access modifiers changed from: private */
        public int mDcnr = 0;
        /* access modifiers changed from: private */
        public MobileSignalController.MobileIconGroup mIconGroup = TelephonyIcons.UNKNOWN;
        /* access modifiers changed from: private */
        public int mLevel = 0;
        /* access modifiers changed from: private */
        public int mNrConfigType = 0;
        /* access modifiers changed from: private */
        public int mNrIconType = -1;
        /* access modifiers changed from: private */
        public int mPlmn = 0;
        /* access modifiers changed from: private */
        public int mUpperLayerInd = 0;

        public boolean isConnectedOnSaMode() {
            return this.mNrConfigType == 1 && this.mIconGroup != TelephonyIcons.UNKNOWN;
        }

        public boolean isConnectedOnNsaMode() {
            return this.mNrConfigType == 0 && this.mIconGroup != TelephonyIcons.UNKNOWN;
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

    public FiveGServiceClient(Context context) {
        this.mContext = context;
        this.mPackageName = context.getPackageName();
        this.mRsrpThresholds = this.mContext.getResources().getIntArray(R.array.config_5g_signal_rsrp_thresholds);
        this.mContext.getResources().getIntArray(R.array.config_5g_signal_snr_thresholds);
        try {
            this.mIsCustForKrOps = ((Boolean) TelephonyManager.class.getMethod("isCustForKrOps", (Class[]) null).invoke((Object) null, new Object[0])).booleanValue();
        } catch (Exception e) {
            Log.e("FiveGServiceClient", "isCustForKrOps Exception" + e);
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

    /* access modifiers changed from: private */
    public void binderService() {
        boolean bindService = ServiceUtil.bindService(this.mContext, this.mServiceConnection);
        localLog("binderService", "binderService success=" + bindService + " bindRetryTimes=" + this.mBindRetryTimes + " maxRetryTimes=" + 4);
        if (!bindService && this.mBindRetryTimes < 4 && !this.mHandler.hasMessages(1024)) {
            this.mHandler.sendEmptyMessageDelayed(1024, (long) ((this.mBindRetryTimes * 2000) + 3000));
            this.mBindRetryTimes++;
        }
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
    public int getRsrpLevel(int i) {
        return getLevel(i, this.mRsrpThresholds);
    }

    private static int getLevel(int i, int[] iArr) {
        int i2 = 1;
        int i3 = 0;
        if (iArr[iArr.length - 1] < i || i < iArr[0]) {
            i2 = 0;
        } else {
            while (true) {
                if (i3 >= iArr.length - 1) {
                    break;
                }
                if (iArr[i3] < i) {
                    int i4 = i3 + 1;
                    if (i <= iArr[i4]) {
                        i2 = i4;
                        break;
                    }
                }
                i3++;
            }
        }
        if (DEBUG) {
            Log.d("FiveGServiceClient", "value=" + i + " level=" + i2);
        }
        return i2;
    }

    /* access modifiers changed from: private */
    public void notifyListenersIfNecessary(int i) {
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
    public void initFiveGServiceState() {
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
        if (!this.mIsUserFiveGEnabled || i != this.mDefaultDataSlotId) {
            MobileSignalController.MobileIconGroup unused = fiveGServiceState.mIconGroup = TelephonyIcons.UNKNOWN;
        } else if (fiveGServiceState.mNrConfigType == 1) {
            MobileSignalController.MobileIconGroup unused2 = fiveGServiceState.mIconGroup = getSaIcon(fiveGServiceState);
        } else if (fiveGServiceState.mNrConfigType == 0) {
            MobileSignalController.MobileIconGroup unused3 = fiveGServiceState.mIconGroup = getNrIconGroup(fiveGServiceState, i);
        } else {
            MobileSignalController.MobileIconGroup unused4 = fiveGServiceState.mIconGroup = TelephonyIcons.UNKNOWN;
        }
        localLog("update5GIcon slotId=" + i, "update5GIcon FiveGServiceState: " + fiveGServiceState + ", cmccSim=" + isCmccSimCard(i));
    }

    private MobileSignalController.MobileIconGroup getSaIcon(FiveGServiceState fiveGServiceState) {
        if (fiveGServiceState.mBearerAllocationStatus > 0) {
            return TelephonyIcons.FIVE_G_SA;
        }
        return TelephonyIcons.UNKNOWN;
    }

    private MobileSignalController.MobileIconGroup getNrIconGroup(FiveGServiceState fiveGServiceState, int i) {
        MobileSignalController.MobileIconGroup mobileIconGroup = TelephonyIcons.UNKNOWN;
        if (this.mIsCustForKrOps) {
            return getCustKrNrIcon(fiveGServiceState, i);
        }
        if ("andromeda".equals(Build.DEVICE) || "crux".equals(Build.DEVICE)) {
            return getConfigDIconGroup(fiveGServiceState);
        }
        if (Build.IS_INTERNATIONAL_BUILD) {
            return getNrIconTypeIconGroup(fiveGServiceState);
        }
        if (isCmccSimCard(i)) {
            return getNrIconTypeIconGroup(fiveGServiceState);
        }
        return getConfigDIconGroup(fiveGServiceState);
    }

    private MobileSignalController.MobileIconGroup getNrIconTypeIconGroup(FiveGServiceState fiveGServiceState) {
        MobileSignalController.MobileIconGroup mobileIconGroup = TelephonyIcons.UNKNOWN;
        int access$300 = fiveGServiceState.mNrIconType;
        if (access$300 == 1) {
            return TelephonyIcons.FIVE_G_BASIC;
        }
        if (access$300 != 2) {
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
        Log.d("FiveGServiceClient", "getCustKrNrIcon krNrIcon = " + mobileIconGroup + "; phoneId=" + i);
        return mobileIconGroup;
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
            mobileIconGroup = TelephonyIcons.FIVE_G_BASIC;
        } else {
            if (this.mLastBearerAllocationStatus[i] > 0 && !this.mHandler.hasMessages(1026, Integer.valueOf(i))) {
                this.mIsDelayUpdate5GIcon[i] = true;
                Message obtainMessage = this.mHandler.obtainMessage(1026, Integer.valueOf(i));
                Log.d("FiveGServiceClient", "send DELAY_UPDATE_5GICON ");
                this.mHandler.sendMessageDelayed(obtainMessage, 11000);
            }
            if (this.mIsDelayUpdate5GIcon[i]) {
                Log.d("FiveGServiceClient", "isDelayUpdate5GIcon show 5G reverse icon");
                mobileIconGroup = TelephonyIcons.FIVE_G_BASIC;
            } else if (i2 == 1 && fiveGServiceState.mUpperLayerInd == 1 && fiveGServiceState.mPlmn == 1) {
                mobileIconGroup = TelephonyIcons.FIVE_G_BASIC;
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
            for (Map.Entry next : sLocalLogs.entrySet()) {
                printWriter.println((String) next.getKey());
                ((LocalLog) next.getValue()).dump((FileDescriptor) null, printWriter, (String[]) null);
            }
            printWriter.println("FiveGServiceClient dump end.");
            printWriter.flush();
        }
    }

    private void registerFivegEvents() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("fiveg_user_enable"), false, this.m5gEnabledObserver);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("miui.intent.action.ACTION_DEFAULT_DATA_SLOT_CHANGED");
        this.mContext.registerReceiver(this.mIntentReceiver, intentFilter);
        update5GIcon();
    }

    private void registerNetworkDisplayEvents() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("setting_network_state_display"), false, this.mNetworkDisplayObserver);
    }

    /* access modifiers changed from: private */
    public void update5GIcon() {
        boolean z = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "fiveg_user_enable", 1) != 1) {
            z = false;
        }
        this.mIsUserFiveGEnabled = z;
        this.mDefaultDataSlotId = SubscriptionManager.getDefault().getDefaultDataSlotId();
        localLog("5GEnabledChanged", "5G enable state has changed to " + this.mIsUserFiveGEnabled + ", dds is " + this.mDefaultDataSlotId);
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            update5GIcon(getCurrentServiceState(i), i);
            notifyListenersIfNecessary(i);
        }
    }
}
