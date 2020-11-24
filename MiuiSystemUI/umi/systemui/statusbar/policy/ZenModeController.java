package com.android.systemui.statusbar.policy;

import android.app.NotificationManager;
import android.net.Uri;
import android.service.notification.ZenModeConfig;

public interface ZenModeController extends CallbackController<Callback> {

    public interface Callback {
        void onConfigChanged(ZenModeConfig zenModeConfig) {
        }

        void onConsolidatedPolicyChanged(NotificationManager.Policy policy) {
        }

        void onEffectsSupressorChanged() {
        }

        void onManualRuleChanged(ZenModeConfig.ZenRule zenRule) {
        }

        void onNextAlarmChanged() {
        }

        void onVibrateChanged(boolean z) {
        }

        void onZenAvailableChanged(boolean z) {
        }

        void onZenOrRingerChanged(boolean z, boolean z2) {
        }
    }

    boolean areNotificationsHiddenInShade();

    ZenModeConfig getConfig();

    NotificationManager.Policy getConsolidatedPolicy();

    ZenModeConfig.ZenRule getManualRule();

    long getNextAlarm();

    int getZen();

    boolean isRingerModeOn();

    boolean isVibrateOn();

    boolean isVibratorAvailable();

    boolean isVolumeRestricted();

    boolean isZenAvailable();

    boolean isZenModeOn();

    void setZen(int i, Uri uri, String str);

    void toggleSilent();

    void toggleVibrate();
}
