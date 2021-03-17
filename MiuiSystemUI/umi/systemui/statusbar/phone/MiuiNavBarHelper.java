package com.android.systemui.statusbar.phone;

public class MiuiNavBarHelper {
    public static int transformAppearance(int i, boolean z) {
        return z ? i | 4096 : i & -4097;
    }

    public static int transformMiuiLightBarFlag(int i) {
        return i | 4096;
    }
}
