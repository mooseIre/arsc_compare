package com.android.systemui.statusbar.policy;

import android.os.Bundle;
import com.android.systemui.DemoMode;
import com.android.systemui.Dumpable;

public interface BatteryController extends DemoMode, Dumpable, CallbackController<BatteryStateChangeCallback> {

    public interface BatteryStateChangeCallback {
        default void dispatchDemoCommand(String str, Bundle bundle) {
        }

        default void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        }

        default void onBatteryStyleChanged(int i) {
        }

        default void onExtremePowerSaveChanged(boolean z) {
        }

        default void onPowerSaveChanged(boolean z) {
        }
    }

    public interface EstimateFetchCompletion {
        void onBatteryRemainingEstimateRetrieved(String str);
    }

    default void getEstimatedTimeRemainingString(EstimateFetchCompletion estimateFetchCompletion) {
    }

    default void init() {
    }

    boolean isAodPowerSave();

    boolean isPowerSave();

    void setPowerSaveMode(boolean z);
}
