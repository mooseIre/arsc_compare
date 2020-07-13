package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManagerCompat;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import androidx.preference.PreferenceManager;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.SlaveWifiUtils;
import com.android.settingslib.wifi.WifiTracker;
import com.android.settingslib.wifi.WifiUtilsHelper;
import com.android.systemui.SettingsLibCompat;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.NetworkController;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import miui.app.AlertDialog;
import miui.view.MiuiHapticFeedbackConstants;

public class AccessPointControllerImpl implements NetworkController.AccessPointController, WifiTracker.WifiListener {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("AccessPointController", 3);
    private static final int[] LEGACY_ICONS = {R.drawable.ic_qs_wifi_full_0, R.drawable.ic_qs_wifi_full_1, R.drawable.ic_qs_wifi_full_2, R.drawable.ic_qs_wifi_full_3, R.drawable.ic_qs_wifi_full_4};
    private static final int[] WIFI_SIX_GENERATION_ICONS = {R.drawable.ic_qs_wifi_6_signal_0, R.drawable.ic_qs_wifi_6_signal_1, R.drawable.ic_qs_wifi_6_signal_2, R.drawable.ic_qs_wifi_6_signal_3, R.drawable.ic_qs_wifi_6_signal_4};
    private final ArrayList<NetworkController.AccessPointController.AccessPointCallback> mCallbacks = new ArrayList<>();
    private final WifiManager.ActionListener mConnectListener = new WifiManager.ActionListener() {
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
    private final ConnectivityManager mConnectivityManager;
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mCurrentUser;
    /* access modifiers changed from: private */
    public SlaveWifiUtils mSlaveWifiUtils;
    private final UserManager mUserManager;
    private final WifiTracker mWifiTracker;

    public void onWifiStateChanged(int i) {
    }

    public AccessPointControllerImpl(Context context, Looper looper) {
        this.mContext = context;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mWifiTracker = new WifiTracker(context, this, false, true);
        this.mCurrentUser = ActivityManager.getCurrentUser();
        this.mSlaveWifiUtils = new SlaveWifiUtils(context);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        this.mWifiTracker.onDestroy();
    }

    public boolean canConfigWifi() {
        return !this.mUserManager.hasUserRestriction("no_config_wifi", new UserHandle(this.mCurrentUser));
    }

    public void onUserSwitched(int i) {
        this.mCurrentUser = i;
    }

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

    public void scanForAccessPoints() {
        fireAcccessPointsCallback(this.mWifiTracker.getAccessPoints());
    }

    public int getIcon(AccessPoint accessPoint) {
        int level = accessPoint.getLevel();
        if (SettingsLibCompat.getWifiStandard(accessPoint) == 6) {
            int[] iArr = WIFI_SIX_GENERATION_ICONS;
            if (level < 0 || level >= LEGACY_ICONS.length) {
                level = 0;
            }
            return iArr[level];
        }
        int[] iArr2 = LEGACY_ICONS;
        if (level < 0 || level >= iArr2.length) {
            level = 0;
        }
        return iArr2[level];
    }

    public boolean connect(AccessPoint accessPoint) {
        if (accessPoint == null || accessPoint.isActive() || accessPoint.isSlaveActive()) {
            return false;
        }
        if (DEBUG) {
            Log.d("AccessPointController", "connect networkId=" + accessPoint.getConfig().networkId);
        }
        if (!this.mSlaveWifiUtils.isSlaveWifiEnabled() || !sameBandToCurrentSlaveWifi(accessPoint) || isWifiSwitchPromptNotRemind() || (!accessPoint.isSaved() && accessPoint.getSecurity() != 0)) {
            return mainWifiConnect(accessPoint);
        }
        showAlertDialog(accessPoint);
        return true;
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
            it.next().onAccessPointsChanged(list);
        }
    }

    public void dump(PrintWriter printWriter) {
        this.mWifiTracker.dump(printWriter);
    }

    private void showAlertDialog(final AccessPoint accessPoint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setCancelable(false);
        Resources resources = this.mContext.getResources();
        builder.setTitle((CharSequence) resources.getString(R.string.quick_settings_wifi_detail_dual_wifi_switching_prompt));
        builder.setMessage((CharSequence) resources.getString(R.string.quick_settings_wifi_detail_dual_wifi_switching_summary));
        builder.setCheckBox(false, resources.getString(R.string.quick_settings_wifi_detail_dual_wifi_switching_not_remind));
        builder.setNegativeButton((CharSequence) resources.getString(R.string.quick_settings_wifi_detail_dual_wifi_switching_cancel), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton((CharSequence) resources.getString(R.string.quick_settings_wifi_detail_dual_wifi_switching_confirm), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (((AlertDialog) dialogInterface).isChecked()) {
                    PreferenceManager.getDefaultSharedPreferences(AccessPointControllerImpl.this.mContext).edit().putBoolean("dual_wifi_switching_not_remind", true).commit();
                }
                AccessPointControllerImpl.this.mSlaveWifiUtils.disconnectSlaveWifi();
                boolean unused = AccessPointControllerImpl.this.mainWifiConnect(accessPoint);
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.getWindow().setType(2010);
        create.getWindow().addPrivateFlags(16);
        create.show();
    }

    /* access modifiers changed from: private */
    public boolean mainWifiConnect(AccessPoint accessPoint) {
        if (accessPoint.isSaved()) {
            this.mWifiTracker.getManager().connect(accessPoint.getConfig().networkId, this.mConnectListener);
            fireConnectionStart(accessPoint);
            return false;
        } else if (accessPoint.getSecurity() != 0) {
            Intent intent = new Intent("android.settings.WIFI_SETTINGS");
            intent.putExtra("wifi_start_connect_ssid", accessPoint.getSsidStr());
            intent.putExtra("ssid", accessPoint.getSsidStr());
            intent.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
            fireSettingsIntentCallback(intent);
            return true;
        } else {
            accessPoint.generateOpenNetworkConfig();
            this.mWifiTracker.getManager().connect(accessPoint.getConfig(), this.mConnectListener);
            fireConnectionStart(accessPoint);
            return false;
        }
    }

    private void fireConnectionStart(AccessPoint accessPoint) {
        Iterator<NetworkController.AccessPointController.AccessPointCallback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onConnectionStart(accessPoint);
        }
    }

    private boolean sameBandToCurrentSlaveWifi(AccessPoint accessPoint) {
        WifiInfo wifiSlaveConnectionInfo = this.mSlaveWifiUtils.getWifiSlaveConnectionInfo();
        NetworkInfo networkInfo = this.mConnectivityManager.getNetworkInfo(this.mSlaveWifiUtils.getSlaveWifiCurrentNetwork());
        if (wifiSlaveConnectionInfo == null || networkInfo == null || !networkInfo.isConnected()) {
            return false;
        }
        if (WifiUtilsHelper.is24GHz(wifiSlaveConnectionInfo)) {
            if (!accessPoint.isOnly5Ghz()) {
                return true;
            }
        } else if (!accessPoint.isOnly24Ghz()) {
            return true;
        }
        return false;
    }

    private boolean isWifiSwitchPromptNotRemind() {
        return PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean("dual_wifi_switching_not_remind", false);
    }

    public void onConnectedChanged() {
        fireAcccessPointsCallback(this.mWifiTracker.getAccessPoints());
    }

    public void onAccessPointsChanged() {
        fireAcccessPointsCallback(this.mWifiTracker.getAccessPoints());
    }

    public void updateVerboseLoggingLevel() {
        WifiManager manager = this.mWifiTracker.getManager();
        if (manager != null) {
            WifiTracker.sVerboseLogging = WifiManagerCompat.isVerboseLoggingEnabled(manager);
        }
    }
}
