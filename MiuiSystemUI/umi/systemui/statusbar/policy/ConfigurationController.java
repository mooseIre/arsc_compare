package com.android.systemui.statusbar.policy;

import android.content.res.Configuration;

public interface ConfigurationController extends CallbackController<ConfigurationListener> {
    void notifyThemeChanged();

    void onConfigurationChanged(Configuration configuration);

    public interface ConfigurationListener {
        default void onConfigChanged(Configuration configuration) {
        }

        default void onDensityOrFontScaleChanged() {
        }

        default void onLocaleListChanged() {
        }

        default void onMiuiThemeChanged(boolean z) {
        }

        default void onOverlayChanged() {
        }

        default void onThemeChanged() {
        }

        default void onUiModeChanged() {
        }

        default void onMiuiThemeChanged(boolean z, boolean z2) {
            onMiuiThemeChanged(z);
        }
    }
}
