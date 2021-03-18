package com.android.systemui.statusbar.policy;

import android.content.res.Configuration;

public interface ConfigurationController extends CallbackController<ConfigurationListener> {

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
    }

    void notifyThemeChanged();

    void onConfigurationChanged(Configuration configuration);
}
