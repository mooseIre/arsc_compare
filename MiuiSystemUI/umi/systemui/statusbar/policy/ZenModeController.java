package com.android.systemui.statusbar.policy;

import android.app.NotificationManager;
import android.net.Uri;
import android.service.notification.ZenModeConfig;

public interface ZenModeController extends CallbackController<Callback> {

    public interface Callback {
        default void onConfigChanged(ZenModeConfig zenModeConfig) {
        }

        default void onConsolidatedPolicyChanged(NotificationManager.Policy policy) {
        }

        default void onEffectsSupressorChanged() {
        }

        default void onManualRuleChanged(ZenModeConfig.ZenRule zenRule) {
        }

        default void onNextAlarmChanged() {
        }

        default void onVibrateChanged(boolean z) {
        }

        default void onZenAvailableChanged(boolean z) {
        }

        default void onZenOrRingerChanged(boolean z, boolean z2) {
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
