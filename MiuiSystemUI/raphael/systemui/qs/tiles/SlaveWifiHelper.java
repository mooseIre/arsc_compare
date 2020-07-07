package com.android.systemui.qs.tiles;

import android.content.Context;
import com.android.settingslib.wifi.AccessPoint;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.plugins.R;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.statusbar.policy.NetworkController;
import java.util.ArrayList;

public class SlaveWifiHelper {
    public static void updateItem(Context context, NetworkController.AccessPointController accessPointController, QSDetailItems qSDetailItems, ArrayList<QSDetailItems.Item> arrayList, AccessPoint accessPoint) {
        QSDetailItems.Item acquireItem = qSDetailItems.acquireItem();
        acquireItem.tag = accessPoint;
        acquireItem.icon = accessPointController.getIcon(accessPoint);
        acquireItem.line1 = accessPoint.getSsid();
        if (accessPoint.isActive()) {
            acquireItem.selected = true;
            acquireItem.line2 = accessPoint.getSummary();
            acquireItem.icon2 = R.drawable.ic_qs_detail_item_selected;
            arrayList.add(acquireItem);
        } else if (accessPoint.isSlaveActive()) {
            acquireItem.selected = true;
            acquireItem.line2 = context.getString(R.string.quick_settings_wifi_detail_dual_wifi_accelerated);
            acquireItem.icon2 = R.drawable.ic_qs_detail_item_selected;
            arrayList.add(acquireItem);
            if (!((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter()) {
                QSDetailItems.Item acquireItem2 = qSDetailItems.acquireItem();
                acquireItem2.type = 2;
                arrayList.add(acquireItem2);
            }
        } else {
            acquireItem.selected = false;
            acquireItem.line2 = null;
            acquireItem.icon2 = accessPoint.getSecurity() != 0 ? R.drawable.ic_qs_wifi_lock : -1;
            arrayList.add(acquireItem);
        }
    }
}
