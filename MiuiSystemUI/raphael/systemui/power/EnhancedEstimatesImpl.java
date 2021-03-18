package com.android.systemui.power;

import com.android.settingslib.fuelgauge.Estimate;

public class EnhancedEstimatesImpl implements EnhancedEstimates {
    @Override // com.android.systemui.power.EnhancedEstimates
    public boolean getLowWarningEnabled() {
        return true;
    }

    @Override // com.android.systemui.power.EnhancedEstimates
    public long getLowWarningThreshold() {
        return 0;
    }

    @Override // com.android.systemui.power.EnhancedEstimates
    public long getSevereWarningThreshold() {
        return 0;
    }

    @Override // com.android.systemui.power.EnhancedEstimates
    public boolean isHybridNotificationEnabled() {
        return false;
    }

    @Override // com.android.systemui.power.EnhancedEstimates
    public Estimate getEstimate() {
        return new Estimate(-1, false, -1);
    }
}
