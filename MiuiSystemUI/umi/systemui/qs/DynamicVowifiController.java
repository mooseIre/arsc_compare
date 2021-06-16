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
import com.android.systemui.C0021R$string;
import java.util.ArrayList;
import java.util.Arrays;

public class DynamicVowifiController {
    private static final String[] VOWIFI_TILES = {"vowifi1", "vowifi2"};
    private final Context mContext;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.qs.DynamicVowifiController.AnonymousClass1 */

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

    private String getAllTiles() {
        String stringForUser = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "sysui_qs_tiles", ActivityManager.getCurrentUser());
        return stringForUser == null ? this.mContext.getString(C0021R$string.quick_settings_tiles_default) : stringForUser;
    }

    private void updateTiles(String str) {
        Log.d("DynamicVowifiController", "updateTiles: " + str);
        Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "sysui_qs_tiles", str, ActivityManager.getCurrentUser());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCarrierConfigChanged(int i, int i2) {
        String str;
        String[] strArr = VOWIFI_TILES;
        String allTiles = getAllTiles();
        if (!SubscriptionManager.isValidSubscriptionId(i2) || !ImsManager.getInstance(this.mContext, i).isWfcEnabledByPlatform()) {
            str = removeVoWifiTile(allTiles, strArr[i]);
        } else {
            str = addVoWifiTile(allTiles, strArr[i]);
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
        for (String str3 : str.split(",")) {
            String trim = str3.trim();
            if (!trim.isEmpty() && !ArrayUtils.contains(str2.split(","), trim)) {
                arrayList.add(trim);
            }
        }
        return TextUtils.join(",", arrayList);
    }

    private String addVoWifiTile(String str, String str2) {
        CharSequence[] charSequenceArr = VOWIFI_TILES;
        Log.d("DynamicVowifiController", "addVoWifiTile: " + str + " addedTile: " + str2);
        ArrayList arrayList = new ArrayList();
        String[] split = str.split(",");
        if (!str.contains(str2)) {
            if (charSequenceArr[0].equals(str2)) {
                arrayList.add(0, str2);
            } else if (charSequenceArr[1].equals(str2)) {
                if (str.contains(charSequenceArr[0])) {
                    arrayList.add(0, charSequenceArr[0]);
                    arrayList.add(1, str2);
                    split = ArrayUtils.removeString(split, charSequenceArr[0]);
                } else {
                    arrayList.add(0, str2);
                }
            }
        }
        arrayList.addAll(Arrays.asList(split));
        return TextUtils.join(",", arrayList);
    }
}
