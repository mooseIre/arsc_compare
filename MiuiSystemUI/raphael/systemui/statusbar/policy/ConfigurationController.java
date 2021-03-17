package com.android.systemui.statusbar.policy;

import android.content.res.Configuration;

public interface ConfigurationController extends CallbackController<ConfigurationListener> {

    public interface ConfigurationListener {
        void onConfigChanged(Configuration configuration) {
        }

        void onDensityOrFontScaleChanged() {
        }

        void onLocaleListChanged() {
        }

        void onMiuiThemeChanged(boolean z) {
        }

        void onOverlayChanged() {
        }

        void onThemeChanged() {
        }

        void onUiModeChanged() {
        }
    }

    void notifyThemeChanged();

    void onConfigurationChanged(Configuration configuration);
}
