package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.WifiTracker;
import com.android.systemui.statusbar.policy.NetworkController;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AccessPointControllerImpl implements NetworkController.AccessPointController, WifiTracker.WifiListener {
    private static final boolean DEBUG = Log.isLoggable("AccessPointController", 3);
    private final ArrayList<NetworkController.AccessPointController.AccessPointCallback> mCallbacks = new ArrayList<>();
    private final WifiManager.ActionListener mConnectListener = new WifiManager.ActionListener() {
        /* class com.android.systemui.statusbar.policy.AccessPointControllerImpl.AnonymousClass1 */

        public void onSuccess() {
            if (AccessPointControllerImpl.DEBUG) {
                Log.d("AccessPointController", "connect success");
            }
        }

        public void onFailure(int i) {
            if (AccessPointControllerImpl.DEBUG) {
                Log.d("AccessPointController", "connect failure reason=" + i);
            }
        }
    };
    private final Context mContext;
    private int mCurrentUser;
    private final UserManager mUserManager;
    private final WifiTracker mWifiTracker;

    @Override // com.android.settingslib.wifi.WifiTracker.WifiListener
    public void onWifiStateChanged(int i) {
    }

    static {
        int[] iArr = MiuiWifiIcons.WIFI_FULL_ICONS;
    }

    public AccessPointControllerImpl(Context context) {
        this.mContext = context;
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mWifiTracker = new WifiTracker(context, this, false, true);
        this.mCurrentUser = ActivityManager.getCurrentUser();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        this.mWifiTracker.onDestroy();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController
    public boolean canConfigWifi() {
        return !this.mUserManager.hasUserRestriction("no_config_wifi", new UserHandle(this.mCurrentUser));
    }

    public void onUserSwitched(int i) {
        this.mCurrentUser = i;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController
    public void addAccessPointCallback(NetworkController.AccessPointController.AccessPointCallback accessPointCallback) {
        if (accessPointCallback != null && !this.mCallbacks.contains(accessPointCallback)) {
            if (DEBUG) {
                Log.d("AccessPointController", "addCallback " + accessPointCallback);
            }
            this.mCallbacks.add(accessPointCallback);
            if (this.mCallbacks.size() == 1) {
                this.mWifiTracker.onStart();
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController
    public void removeAccessPointCallback(NetworkController.AccessPointController.AccessPointCallback accessPointCallback) {
        if (accessPointCallback != null) {
            if (DEBUG) {
                Log.d("AccessPointController", "removeCallback " + accessPointCallback);
            }
            this.mCallbacks.remove(accessPointCallback);
            if (this.mCallbacks.isEmpty()) {
                this.mWifiTracker.onStop();
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController
    public void scanForAccessPoints() {
        fireAcccessPointsCallback(this.mWifiTracker.getAccessPoints());
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController
    public int getIcon(AccessPoint accessPoint) {
        int level = accessPoint.getLevel();
        if (accessPoint.getWifiStandard() == 6) {
            int[] iArr = MiuiWifiIcons.WIFI_6_FULL_ICONS;
            if (level < 0) {
                level = 0;
            }
            return iArr[level];
        }
        int[] iArr2 = MiuiWifiIcons.WIFI_FULL_ICONS;
        if (level < 0) {
            level = 0;
        }
        return iArr2[level];
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController
    public boolean connect(AccessPoint accessPoint) {
        if (accessPoint == null) {
            return false;
        }
        if (DEBUG) {
            Log.d("AccessPointController", "connect networkId=" + accessPoint.getConfig().networkId);
        }
        if (accessPoint.isSaved()) {
            int i = accessPoint.getConfig().networkId;
            if (i >= 0) {
                this.mWifiTracker.getManager().connect(i, this.mConnectListener);
            } else {
                Log.e("AccessPointController", "error: networkId < 0 :" + i);
            }
        } else if (accessPoint.getSecurity() != 0) {
            Intent intent = new Intent("android.settings.WIFI_SETTINGS");
            intent.putExtra("ssid", accessPoint.getSsidStr());
            intent.addFlags(268435456);
            fireSettingsIntentCallback(intent);
            return true;
        } else {
            accessPoint.generateOpenNetworkConfig();
            this.mWifiTracker.getManager().connect(accessPoint.getConfig(), this.mConnectListener);
        }
        return false;
    }

    private void fireSettingsIntentCallback(Intent intent) {
        Iterator<NetworkController.AccessPointController.AccessPointCallback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onSettingsActivityTriggered(intent);
        }
    }

    private void fireAcccessPointsCallback(List<AccessPoint> list) {
        Iterator<NetworkController.AccessPointController.AccessPointCallback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            NetworkController.AccessPointController.AccessPointCallback next = it.next();
            if (next != null) {
                next.onAccessPointsChanged(list);
            } else {
                Log.e("AccessPointController", "error: callback is null!");
            }
        }
    }

    public void dump(PrintWriter printWriter) {
        this.mWifiTracker.dump(printWriter);
    }

    @Override // com.android.settingslib.wifi.WifiTracker.WifiListener
    public void onConnectedChanged() {
        fireAcccessPointsCallback(this.mWifiTracker.getAccessPoints());
    }

    @Override // com.android.settingslib.wifi.WifiTracker.WifiListener
    public void onAccessPointsChanged() {
        fireAcccessPointsCallback(this.mWifiTracker.getAccessPoints());
    }
}
