package com.android.keyguard.fod;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.systemui.plugins.R;
import miui.util.FeatureParser;

class MiuiGxzwOverlayTypeManager {
    private final Context mContext;
    private final int mOverlayType = caculateType();
    private final boolean mSupportLowBrightnessFod = caculateSupportLowBrightnessFod();

    private boolean isTypeValid(int i) {
        return i >= 0 && i <= 3;
    }

    MiuiGxzwOverlayTypeManager(Context context) {
        this.mContext = context;
        Slog.i("MiuiGxzwOverlayTypeManager", "mOverlayType = " + this.mOverlayType + ", mSupportLowBrightnessFod = " + this.mSupportLowBrightnessFod);
    }

    public boolean isOverlayTypeUrsa() {
        return this.mOverlayType == 0;
    }

    public boolean isOverlayTypeAlwaysOn() {
        return this.mOverlayType == 3;
    }

    private int caculateType() {
        int integer = FeatureParser.getInteger("fod_solution", -1);
        Log.i("MiuiGxzwOverlayTypeManager", "feature: fod_solution = " + integer);
        if (isTypeValid(integer)) {
            return integer;
        }
        if (getDimLayerProperty()) {
            return 2;
        }
        int integer2 = this.mContext.getResources().getInteger(R.integer.config_fodSolution);
        if (isTypeValid(integer2)) {
            return integer2;
        }
        return 3;
    }

    private boolean getDimLayerProperty() {
        if (SystemProperties.getBoolean("ro.vendor.fod.dimlayer.enable", false) || SystemProperties.getBoolean("ro.fod.dimlayer.enable", false)) {
            return true;
        }
        return false;
    }

    private boolean caculateSupportLowBrightnessFod() {
        if (FeatureParser.hasFeature("support_low_brightness_fod", 1)) {
            boolean z = FeatureParser.getBoolean("support_low_brightness_fod", false);
            Log.i("MiuiGxzwOverlayTypeManager", "feature: support_low_brightness_fod = " + z);
            return z;
        }
        Log.i("MiuiGxzwOverlayTypeManager", "not set feature: support_low_brightness_fod");
        if (this.mOverlayType == 1) {
            return true;
        }
        return false;
    }
}
