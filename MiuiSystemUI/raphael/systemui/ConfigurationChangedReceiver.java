package com.android.systemui;

import android.content.res.Configuration;

public interface ConfigurationChangedReceiver {
    void onConfigurationChanged(Configuration configuration);
}
