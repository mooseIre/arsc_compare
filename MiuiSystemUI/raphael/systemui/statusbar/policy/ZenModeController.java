package com.android.systemui.statusbar.policy;

import android.service.notification.Condition;
import android.service.notification.ZenModeConfig;

public interface ZenModeController extends CallbackController<Callback> {

    public interface Callback {
        void onConditionsChanged(Condition[] conditionArr);

        void onConfigChanged(ZenModeConfig zenModeConfig);

        void onEffectsSupressorChanged();

        void onManualRuleChanged(ZenModeConfig.ZenRule zenRule);

        void onNextAlarmChanged();

        void onZenAvailableChanged(boolean z);

        void onZenChanged(int i);
    }

    ZenModeConfig getConfig();

    int getCurrentUser();

    int getZen();
}
