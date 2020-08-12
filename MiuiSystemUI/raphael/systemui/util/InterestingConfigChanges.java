package com.android.systemui.util;

import android.content.res.Configuration;
import android.content.res.Resources;

public class InterestingConfigChanges {
    private final int mFlags;
    private final Configuration mLastConfiguration = new Configuration();
    private int mLastDensity;

    public InterestingConfigChanges(int i) {
        this.mFlags = i;
    }

    public boolean applyNewConfig(Resources resources) {
        int updateFrom = this.mLastConfiguration.updateFrom(resources.getConfiguration());
        if (!(this.mLastDensity != resources.getDisplayMetrics().densityDpi) && (updateFrom & this.mFlags) == 0) {
            return false;
        }
        this.mLastDensity = resources.getDisplayMetrics().densityDpi;
        return true;
    }
}
