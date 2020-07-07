package com.android.systemui.statusbar.policy;

import android.content.res.Configuration;

public interface ConfigurationController extends CallbackController<ConfigurationListener> {

    public interface ConfigurationListener {
        void onConfigChanged(Configuration configuration) {
        }

        void onDensityOrFontScaleChanged() {
        }
    }

    boolean isNightMode();
}
