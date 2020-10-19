package com.android.systemui.qs;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ImsManager;
import com.android.internal.util.ArrayUtils;
import com.android.systemui.plugins.R;
import java.util.ArrayList;
import java.util.Arrays;

public class DynamicVowifiController {
    private static final String[] VOWIFI_TILES = {"vowifi1", "vowifi2"};
    private final Context mContext;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction())) {
                int i = intent.getExtras().getInt("android.telephony.extra.SLOT_INDEX");
                int intExtra = intent.getIntExtra("android.telephony.extra.SUBSCRIPTION_INDEX", -1);
                Log.d("DynamicVowifiController", "onReceive: phoneId " + i + " subId " + intExtra);
                DynamicVowifiController.this.handleCarrierConfigChanged(i, intExtra);
            }
        }
    };

    public DynamicVowifiController(Context context) {
        this.mContext = context;
        updateTiles(removeVoWifiTile(getAllTiles(), TextUtils.join(",", VOWIFI_TILES)));
    }

    public void registerReceiver() {
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
    }

    public void unregisterReceiver() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    private String getAllTiles() {
        String stringForUser = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "sysui_qs_tiles", ActivityManager.getCurrentUser());
        return stringForUser == null ? this.mContext.getString(R.string.quick_settings_tiles_default) : stringForUser;
    }

    private void updateTiles(String str) {
        Log.d("DynamicVowifiController", "updateTiles: " + str);
        Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "sysui_qs_tiles", str, ActivityManager.getCurrentUser());
    }

    /* access modifiers changed from: private */
    public void handleCarrierConfigChanged(int i, int i2) {
        String str;
        String allTiles = getAllTiles();
        if (!SubscriptionManager.isValidSubscriptionId(i2) || !ImsManager.getInstance(this.mContext, i).isWfcEnabledByPlatform()) {
            str = removeVoWifiTile(allTiles, VOWIFI_TILES[i]);
        } else {
            str = addVoWifiTile(allTiles, VOWIFI_TILES[i]);
        }
        if (allTiles.equals(str)) {
            Log.d("DynamicVowifiController", "handleCarrierConfigChanged: no change");
        } else {
            updateTiles(str);
        }
    }

    private String removeVoWifiTile(String str, String str2) {
        Log.d("DynamicVowifiController", "removeVoWifiTile: " + str + " removedTile: " + str2);
        ArrayList arrayList = new ArrayList();
        for (String trim : str.split(",")) {
            String trim2 = trim.trim();
            if (!trim2.isEmpty() && !ArrayUtils.contains(str2.split(","), trim2)) {
                arrayList.add(trim2);
            }
        }
        return TextUtils.join(",", arrayList);
    }

    private String addVoWifiTile(String str, String str2) {
        Log.d("DynamicVowifiController", "addVoWifiTile: " + str + " addedTile: " + str2);
        ArrayList arrayList = new ArrayList();
        String[] split = str.split(",");
        if (!str.contains(str2)) {
            if (VOWIFI_TILES[0].equals(str2)) {
                arrayList.add(0, str2);
            } else if (VOWIFI_TILES[1].equals(str2)) {
                if (str.contains(VOWIFI_TILES[0])) {
                    arrayList.add(0, VOWIFI_TILES[0]);
                    arrayList.add(1, str2);
                    split = ArrayUtils.removeString(split, VOWIFI_TILES[0]);
                } else {
                    arrayList.add(0, str2);
                }
            }
        }
        arrayList.addAll(Arrays.asList(split));
        return TextUtils.join(",", arrayList);
    }
}
