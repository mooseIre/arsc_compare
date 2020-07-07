package com.android.systemui.qs.tiles;

import com.android.settingslib.wifi.AccessPoint;

public class WifiTileHelper {
    public static AccessPoint[] filterUnreachableAPs(AccessPoint[] accessPointArr) {
        int i = 0;
        for (AccessPoint isReachable : accessPointArr) {
            if (isReachable.isReachable()) {
                i++;
            }
        }
        if (i == accessPointArr.length) {
            return accessPointArr;
        }
        AccessPoint[] accessPointArr2 = new AccessPoint[i];
        int i2 = 0;
        for (AccessPoint accessPoint : accessPointArr) {
            if (accessPoint.isReachable()) {
                accessPointArr2[i2] = accessPoint;
                i2++;
            }
        }
        return accessPointArr2;
    }
}
