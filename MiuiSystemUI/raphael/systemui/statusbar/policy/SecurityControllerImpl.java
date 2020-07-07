package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.admin.DevicePolicyManagerCompat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserHandleCompat;
import android.os.UserManager;
import android.os.UserManagerCompat;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.SecurityController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class SecurityControllerImpl extends CurrentUserTracker implements SecurityController {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("SecurityController", 3);
    private static final NetworkRequest REQUEST = new NetworkRequest.Builder().removeCapability(15).removeCapability(13).removeCapability(14).build();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.security.action.TRUST_STORE_CHANGED".equals(intent.getAction())) {
                SecurityControllerImpl.this.refreshCACerts();
            }
        }
    };
    @GuardedBy({"mCallbacks"})
    private final ArrayList<SecurityController.SecurityControllerCallback> mCallbacks = new ArrayList<>();
    private final ConnectivityManager mConnectivityManager;
    private final IConnectivityManager mConnectivityManagerService;
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mCurrentUserId;
    private SparseArray<VpnConfig> mCurrentVpns = new SparseArray<>();
    private final DevicePolicyManager mDevicePolicyManager;
    /* access modifiers changed from: private */
    public ArrayMap<Integer, Boolean> mHasCACerts = new ArrayMap<>();
    private final ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            if (SecurityControllerImpl.DEBUG) {
                Log.d("SecurityController", "onAvailable " + network.netId);
            }
            SecurityControllerImpl.this.updateState();
            SecurityControllerImpl.this.fireCallbacks();
        }

        public void onLost(Network network) {
            if (SecurityControllerImpl.DEBUG) {
                Log.d("SecurityController", "onLost " + network.netId);
            }
            SecurityControllerImpl.this.updateState();
            SecurityControllerImpl.this.fireCallbacks();
        }
    };
    private final PackageManager mPackageManager;
    private final UserManager mUserManager;
    private int mVpnUserId;

    public SecurityControllerImpl(Context context) {
        super(context);
        this.mContext = context;
        this.mDevicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mConnectivityManagerService = IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity"));
        this.mPackageManager = context.getPackageManager();
        this.mUserManager = (UserManager) context.getSystemService("user");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.security.action.TRUST_STORE_CHANGED");
        context.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, intentFilter, (String) null, new Handler((Looper) Dependency.get(Dependency.BG_LOOPER)));
        this.mConnectivityManager.registerNetworkCallback(REQUEST, this.mNetworkCallback);
        onUserSwitched(ActivityManager.getCurrentUser());
        startTracking();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("SecurityController state:");
        printWriter.print("  mCurrentVpns={");
        for (int i = 0; i < this.mCurrentVpns.size(); i++) {
            if (i > 0) {
                printWriter.print(", ");
            }
            printWriter.print(this.mCurrentVpns.keyAt(i));
            printWriter.print('=');
            printWriter.print(this.mCurrentVpns.valueAt(i).user);
        }
        printWriter.println("}");
    }

    public boolean isDeviceManaged() {
        return DevicePolicyManagerCompat.isDeviceManaged(this.mDevicePolicyManager);
    }

    public CharSequence getDeviceOwnerOrganizationName() {
        return DevicePolicyManagerCompat.getDeviceOwnerOrganizationName(this.mDevicePolicyManager);
    }

    public CharSequence getWorkProfileOrganizationName() {
        int workProfileUserId = getWorkProfileUserId(this.mCurrentUserId);
        if (workProfileUserId == -10000) {
            return null;
        }
        return DevicePolicyManagerCompat.getOrganizationNameForUser(this.mDevicePolicyManager, workProfileUserId);
    }

    public String getPrimaryVpnName() {
        VpnConfig vpnConfig = this.mCurrentVpns.get(this.mVpnUserId);
        if (vpnConfig != null) {
            return getNameForVpnConfig(vpnConfig, new UserHandle(this.mVpnUserId));
        }
        return null;
    }

    private int getWorkProfileUserId(int i) {
        for (UserInfo userInfo : this.mUserManager.getProfiles(i)) {
            if (userInfo.isManagedProfile()) {
                return userInfo.id;
            }
        }
        return -10000;
    }

    public boolean hasWorkProfile() {
        return getWorkProfileUserId(this.mCurrentUserId) != -10000;
    }

    public String getWorkProfileVpnName() {
        VpnConfig vpnConfig;
        int workProfileUserId = getWorkProfileUserId(this.mVpnUserId);
        if (workProfileUserId == -10000 || (vpnConfig = this.mCurrentVpns.get(workProfileUserId)) == null) {
            return null;
        }
        return getNameForVpnConfig(vpnConfig, UserHandleCompat.of(workProfileUserId));
    }

    public boolean isNetworkLoggingEnabled() {
        return DevicePolicyManagerCompat.isNetworkLoggingEnabled(this.mDevicePolicyManager);
    }

    public boolean isVpnEnabled() {
        for (int i : UserManagerCompat.getProfileIdsWithDisabled(this.mUserManager, this.mVpnUserId)) {
            if (this.mCurrentVpns.get(i) != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isSilentVpnPackage() {
        return "com.miui.vpnsdkmanager".equals(getVpnPackageName());
    }

    public boolean hasCACertInCurrentUser() {
        Boolean bool = this.mHasCACerts.get(Integer.valueOf(this.mCurrentUserId));
        return bool != null && bool.booleanValue();
    }

    public boolean hasCACertInWorkProfile() {
        Boolean bool;
        int workProfileUserId = getWorkProfileUserId(this.mCurrentUserId);
        if (workProfileUserId == -10000 || (bool = this.mHasCACerts.get(Integer.valueOf(workProfileUserId))) == null || !bool.booleanValue()) {
            return false;
        }
        return true;
    }

    public void removeCallback(SecurityController.SecurityControllerCallback securityControllerCallback) {
        synchronized (this.mCallbacks) {
            if (securityControllerCallback != null) {
                if (DEBUG) {
                    Log.d("SecurityController", "removeCallback " + securityControllerCallback);
                }
                this.mCallbacks.remove(securityControllerCallback);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0030, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addCallback(com.android.systemui.statusbar.policy.SecurityController.SecurityControllerCallback r5) {
        /*
            r4 = this;
            java.util.ArrayList<com.android.systemui.statusbar.policy.SecurityController$SecurityControllerCallback> r0 = r4.mCallbacks
            monitor-enter(r0)
            if (r5 == 0) goto L_0x002f
            java.util.ArrayList<com.android.systemui.statusbar.policy.SecurityController$SecurityControllerCallback> r1 = r4.mCallbacks     // Catch:{ all -> 0x0031 }
            boolean r1 = r1.contains(r5)     // Catch:{ all -> 0x0031 }
            if (r1 == 0) goto L_0x000e
            goto L_0x002f
        L_0x000e:
            boolean r1 = DEBUG     // Catch:{ all -> 0x0031 }
            if (r1 == 0) goto L_0x0028
            java.lang.String r1 = "SecurityController"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0031 }
            r2.<init>()     // Catch:{ all -> 0x0031 }
            java.lang.String r3 = "addCallback "
            r2.append(r3)     // Catch:{ all -> 0x0031 }
            r2.append(r5)     // Catch:{ all -> 0x0031 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0031 }
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x0031 }
        L_0x0028:
            java.util.ArrayList<com.android.systemui.statusbar.policy.SecurityController$SecurityControllerCallback> r4 = r4.mCallbacks     // Catch:{ all -> 0x0031 }
            r4.add(r5)     // Catch:{ all -> 0x0031 }
            monitor-exit(r0)     // Catch:{ all -> 0x0031 }
            return
        L_0x002f:
            monitor-exit(r0)     // Catch:{ all -> 0x0031 }
            return
        L_0x0031:
            r4 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0031 }
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.SecurityControllerImpl.addCallback(com.android.systemui.statusbar.policy.SecurityController$SecurityControllerCallback):void");
    }

    public void onUserSwitched(int i) {
        this.mCurrentUserId = i;
        if (this.mUserManager.getUserInfo(i).isRestricted()) {
            this.mVpnUserId = -10000;
        } else {
            this.mVpnUserId = this.mCurrentUserId;
        }
        refreshCACerts();
        fireCallbacks();
    }

    public String getVpnPackageName() {
        VpnConfig vpnConfig = this.mCurrentVpns.get(this.mVpnUserId);
        if (vpnConfig != null) {
            return vpnConfig.user;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void refreshCACerts() {
        if (Build.VERSION.SDK_INT >= 26) {
            new CACertLoader().execute(new Integer[]{Integer.valueOf(this.mCurrentUserId)});
            int workProfileUserId = getWorkProfileUserId(this.mCurrentUserId);
            if (workProfileUserId != -10000) {
                new CACertLoader().execute(new Integer[]{Integer.valueOf(workProfileUserId)});
            }
        }
    }

    private String getNameForVpnConfig(VpnConfig vpnConfig, UserHandle userHandle) {
        if (vpnConfig.legacy) {
            return this.mContext.getString(R.string.legacy_vpn_name);
        }
        String str = vpnConfig.user;
        try {
            return VpnConfig.getVpnLabel(this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, userHandle), str).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SecurityController", "Package " + str + " is not present", e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void fireCallbacks() {
        synchronized (this.mCallbacks) {
            Iterator<SecurityController.SecurityControllerCallback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onStateChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateState() {
        SparseArray<VpnConfig> sparseArray = new SparseArray<>();
        try {
            for (UserInfo userInfo : this.mUserManager.getUsers()) {
                VpnConfig vpnConfig = this.mConnectivityManagerService.getVpnConfig(userInfo.id);
                if (vpnConfig != null) {
                    if (vpnConfig.legacy) {
                        LegacyVpnInfo legacyVpnInfo = this.mConnectivityManagerService.getLegacyVpnInfo(userInfo.id);
                        if (legacyVpnInfo != null) {
                            if (legacyVpnInfo.state != 3) {
                            }
                        }
                    }
                    sparseArray.put(userInfo.id, vpnConfig);
                }
            }
            this.mCurrentVpns = sparseArray;
        } catch (RemoteException e) {
            Log.e("SecurityController", "Unable to list active VPNs", e);
        }
    }

    protected class CACertLoader extends AsyncTask<Integer, Void, Pair<Integer, Boolean>> {
        protected CACertLoader() {
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x003d, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x003e, code lost:
            if (r1 != null) goto L_0x0040;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            r1.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0048, code lost:
            throw r3;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public android.util.Pair<java.lang.Integer, java.lang.Boolean> doInBackground(final java.lang.Integer... r6) {
            /*
                r5 = this;
                r0 = 0
                com.android.systemui.statusbar.policy.SecurityControllerImpl r1 = com.android.systemui.statusbar.policy.SecurityControllerImpl.this     // Catch:{ RemoteException | AssertionError | InterruptedException -> 0x0049 }
                android.content.Context r1 = r1.mContext     // Catch:{ RemoteException | AssertionError | InterruptedException -> 0x0049 }
                r2 = r6[r0]     // Catch:{ RemoteException | AssertionError | InterruptedException -> 0x0049 }
                int r2 = r2.intValue()     // Catch:{ RemoteException | AssertionError | InterruptedException -> 0x0049 }
                android.os.UserHandle r2 = android.os.UserHandleCompat.of(r2)     // Catch:{ RemoteException | AssertionError | InterruptedException -> 0x0049 }
                android.security.KeyChain$KeyChainConnection r1 = android.security.KeyChain.bindAsUser(r1, r2)     // Catch:{ RemoteException | AssertionError | InterruptedException -> 0x0049 }
                android.security.IKeyChainService r2 = r1.getService()     // Catch:{ all -> 0x003b }
                android.content.pm.StringParceledListSlice r2 = r2.getUserCaAliases()     // Catch:{ all -> 0x003b }
                java.util.List r2 = r2.getList()     // Catch:{ all -> 0x003b }
                boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x003b }
                if (r2 != 0) goto L_0x0029
                r2 = 1
                goto L_0x002a
            L_0x0029:
                r2 = r0
            L_0x002a:
                android.util.Pair r3 = new android.util.Pair     // Catch:{ all -> 0x003b }
                r4 = r6[r0]     // Catch:{ all -> 0x003b }
                java.lang.Boolean r2 = java.lang.Boolean.valueOf(r2)     // Catch:{ all -> 0x003b }
                r3.<init>(r4, r2)     // Catch:{ all -> 0x003b }
                if (r1 == 0) goto L_0x003a
                r1.close()     // Catch:{ RemoteException | AssertionError | InterruptedException -> 0x0049 }
            L_0x003a:
                return r3
            L_0x003b:
                r2 = move-exception
                throw r2     // Catch:{ all -> 0x003d }
            L_0x003d:
                r3 = move-exception
                if (r1 == 0) goto L_0x0048
                r1.close()     // Catch:{ all -> 0x0044 }
                goto L_0x0048
            L_0x0044:
                r1 = move-exception
                r2.addSuppressed(r1)     // Catch:{ RemoteException | AssertionError | InterruptedException -> 0x0049 }
            L_0x0048:
                throw r3     // Catch:{ RemoteException | AssertionError | InterruptedException -> 0x0049 }
            L_0x0049:
                r1 = move-exception
                java.lang.String r2 = r1.getMessage()
                if (r2 == 0) goto L_0x0059
                java.lang.String r1 = r1.getMessage()
                java.lang.String r2 = "SecurityController"
                android.util.Log.i(r2, r1)
            L_0x0059:
                android.os.Handler r1 = new android.os.Handler
                com.android.systemui.Dependency$DependencyKey<android.os.Looper> r2 = com.android.systemui.Dependency.BG_LOOPER
                java.lang.Object r2 = com.android.systemui.Dependency.get(r2)
                android.os.Looper r2 = (android.os.Looper) r2
                r1.<init>(r2)
                com.android.systemui.statusbar.policy.SecurityControllerImpl$CACertLoader$1 r2 = new com.android.systemui.statusbar.policy.SecurityControllerImpl$CACertLoader$1
                r2.<init>(r6)
                r3 = 30000(0x7530, double:1.4822E-319)
                r1.postDelayed(r2, r3)
                android.util.Pair r5 = new android.util.Pair
                r6 = r6[r0]
                r0 = 0
                r5.<init>(r6, r0)
                return r5
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.SecurityControllerImpl.CACertLoader.doInBackground(java.lang.Integer[]):android.util.Pair");
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Pair<Integer, Boolean> pair) {
            if (SecurityControllerImpl.DEBUG) {
                Log.d("SecurityController", "onPostExecute " + pair);
            }
            if (pair.second != null) {
                SecurityControllerImpl.this.mHasCACerts.put((Integer) pair.first, (Boolean) pair.second);
                SecurityControllerImpl.this.fireCallbacks();
            }
        }
    }
}
