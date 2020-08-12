package com.android.systemui.statusbar.policy;

import android.os.Bundle;
import com.android.systemui.DemoMode;
import com.android.systemui.Dumpable;

public interface BatteryController extends DemoMode, Dumpable, CallbackController<BatteryStateChangeCallback> {

    public interface BatteryStateChangeCallback {
        void dispatchDemoCommand(String str, Bundle bundle) {
        }

        void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        }

        void onBatteryStyleChanged(int i) {
        }

        void onExtremePowerSaveChanged(boolean z) {
        }

        void onPowerSaveChanged(boolean z) {
        }
    }

    boolean isExtremePowerSave();

    boolean isPowerSave();
}
