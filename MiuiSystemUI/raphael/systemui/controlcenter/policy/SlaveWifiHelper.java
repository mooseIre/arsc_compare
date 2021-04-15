package com.android.systemui.controlcenter.policy;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import androidx.preference.PreferenceManager;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.SlaveWifiUtils;
import com.android.settingslib.wifi.WifiUtils;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.qs.MiuiQSDetailItems;
import com.android.systemui.statusbar.policy.NetworkController;
import java.util.ArrayList;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import miui.app.AlertDialog;
import org.jetbrains.annotations.NotNull;

/* compiled from: SlaveWifiHelper.kt */
public final class SlaveWifiHelper {
    private final ConnectivityManager connectivityManager;
    @NotNull
    private final Context context;
    private final SlaveWifiUtils slaveWifiUtils;

    public SlaveWifiHelper(@NotNull Context context2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        this.context = context2;
        this.slaveWifiUtils = new SlaveWifiUtils(context2);
        Object systemService = this.context.getSystemService("connectivity");
        if (systemService != null) {
            this.connectivityManager = (ConnectivityManager) systemService;
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.net.ConnectivityManager");
    }

    public final void updateItem(@NotNull Context context2, @NotNull NetworkController.AccessPointController accessPointController, @NotNull MiuiQSDetailItems miuiQSDetailItems, @NotNull ArrayList<MiuiQSDetailItems.Item> arrayList, @NotNull AccessPoint accessPoint) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(accessPointController, "accessPointController");
        Intrinsics.checkParameterIsNotNull(miuiQSDetailItems, "qsDetailItems");
        Intrinsics.checkParameterIsNotNull(arrayList, "items");
        Intrinsics.checkParameterIsNotNull(accessPoint, "ap");
        MiuiQSDetailItems.Item acquireItem = miuiQSDetailItems.acquireItem();
        acquireItem.tag = accessPoint;
        acquireItem.icon = accessPointController.getIcon(accessPoint);
        acquireItem.line1 = accessPoint.getSsid();
        if (accessPoint.isActive()) {
            acquireItem.selected = true;
            acquireItem.line2 = accessPoint.getSummary();
            acquireItem.icon2 = C0013R$drawable.ic_qs_detail_item_selected;
            arrayList.add(acquireItem);
        } else if (isSlaveActive(accessPoint)) {
            acquireItem.selected = true;
            acquireItem.line2 = context2.getString(C0021R$string.quick_settings_wifi_detail_dual_wifi_accelerated);
            acquireItem.icon2 = C0013R$drawable.ic_qs_detail_item_selected;
            arrayList.add(acquireItem);
            Object obj = Dependency.get(ControlPanelController.class);
            Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(ControlPanelController::class.java)");
            if (!((ControlPanelController) obj).isUseControlCenter()) {
                MiuiQSDetailItems.Item acquireItem2 = miuiQSDetailItems.acquireItem();
                acquireItem2.type = 2;
                arrayList.add(acquireItem2);
            }
        } else {
            acquireItem.selected = false;
            acquireItem.line2 = null;
            acquireItem.icon2 = accessPoint.getSecurity() != 0 ? C0013R$drawable.ic_qs_wifi_lock : -1;
            arrayList.add(acquireItem);
        }
    }

    public final boolean connect(@NotNull Context context2, @NotNull AccessPoint accessPoint, @NotNull NetworkController.AccessPointController accessPointController) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(accessPoint, "ap");
        Intrinsics.checkParameterIsNotNull(accessPointController, "accessPointController");
        if (accessPoint.isActive() || isSlaveActive(accessPoint)) {
            return false;
        }
        if (!this.slaveWifiUtils.isSlaveWifiEnabled() || !sameBandToCurrentSlaveWifi(accessPoint) || isWifiSwitchPromptNotRemind() || (!accessPoint.isSaved() && accessPoint.getSecurity() != 0)) {
            return accessPointController.connect(accessPoint);
        }
        showAlertDialog(context2, accessPoint, accessPointController);
        return true;
    }

    private final void showAlertDialog(Context context2, AccessPoint accessPoint, NetworkController.AccessPointController accessPointController) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context2, C0022R$style.Theme_Dialog_Alert);
        builder.setCancelable(false);
        Resources resources = context2.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.getResources()");
        builder.setTitle(resources.getString(C0021R$string.quick_settings_wifi_detail_dual_wifi_switching_prompt));
        builder.setMessage(resources.getString(C0021R$string.quick_settings_wifi_detail_dual_wifi_switching_summary));
        builder.setCheckBox(false, resources.getString(C0021R$string.quick_settings_wifi_detail_dual_wifi_switching_not_remind));
        builder.setNegativeButton(resources.getString(C0021R$string.quick_settings_wifi_detail_dual_wifi_switching_cancel), SlaveWifiHelper$showAlertDialog$1.INSTANCE);
        builder.setPositiveButton(resources.getString(C0021R$string.quick_settings_wifi_detail_dual_wifi_switching_confirm), new SlaveWifiHelper$showAlertDialog$2(this, context2, accessPointController, accessPoint));
        AlertDialog create = builder.create();
        Intrinsics.checkExpressionValueIsNotNull(create, "dialog");
        create.getWindow().setType(2010);
        create.getWindow().addPrivateFlags(16);
        create.show();
    }

    private final boolean sameBandToCurrentSlaveWifi(AccessPoint accessPoint) {
        WifiInfo wifiSlaveConnectionInfo = this.slaveWifiUtils.getWifiSlaveConnectionInfo();
        NetworkInfo networkInfo = this.connectivityManager.getNetworkInfo(this.slaveWifiUtils.getSlaveWifiCurrentNetwork());
        if (wifiSlaveConnectionInfo == null || networkInfo == null || !networkInfo.isConnected()) {
            return false;
        }
        if (is24Ghz(wifiSlaveConnectionInfo)) {
            if (isOnly5Ghz(accessPoint)) {
                return false;
            }
        } else if (isOnly24Ghz(accessPoint)) {
            return false;
        }
        return true;
    }

    private final boolean isWifiSwitchPromptNotRemind() {
        return PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("dual_wifi_switching_not_remind", false);
    }

    private final boolean isSlaveActive(AccessPoint accessPoint) {
        try {
            Object invoke = AccessPoint.class.getDeclaredMethod("isSlaveActive", new Class[0]).invoke(accessPoint, new Object[0]);
            if (invoke != null) {
                return ((Boolean) invoke).booleanValue();
            }
            throw new TypeCastException("null cannot be cast to non-null type kotlin.Boolean");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private final boolean isOnly5Ghz(AccessPoint accessPoint) {
        try {
            Object invoke = AccessPoint.class.getDeclaredMethod("isOnly5Ghz", new Class[0]).invoke(accessPoint, new Object[0]);
            if (invoke != null) {
                return ((Boolean) invoke).booleanValue();
            }
            throw new TypeCastException("null cannot be cast to non-null type kotlin.Boolean");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private final boolean isOnly24Ghz(AccessPoint accessPoint) {
        try {
            Object invoke = AccessPoint.class.getDeclaredMethod("isOnly24Ghz", new Class[0]).invoke(accessPoint, new Object[0]);
            if (invoke != null) {
                return ((Boolean) invoke).booleanValue();
            }
            throw new TypeCastException("null cannot be cast to non-null type kotlin.Boolean");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private final boolean is24Ghz(WifiInfo wifiInfo) {
        try {
            Object invoke = WifiUtils.class.getDeclaredMethod("is24Ghz", WifiInfo.class).invoke(null, wifiInfo);
            if (invoke != null) {
                return ((Boolean) invoke).booleanValue();
            }
            throw new TypeCastException("null cannot be cast to non-null type kotlin.Boolean");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
