package com.android.systemui.statusbar.phone;

import android.text.TextUtils;
import android.util.ArraySet;

public abstract class StatusBarIconControllerHelper implements StatusBarIconController {
    public static ArraySet<String> getIconBlacklist(String str) {
        ArraySet<String> arraySet = new ArraySet<>();
        if (str == null) {
            str = "rotate,ime";
        }
        for (String str2 : str.split(",")) {
            if (!TextUtils.isEmpty(str2)) {
                arraySet.add(str2);
            }
        }
        return arraySet;
    }
}
