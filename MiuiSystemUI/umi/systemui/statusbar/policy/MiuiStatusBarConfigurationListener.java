package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.res.Configuration;
import com.android.systemui.statusbar.phone.BatteryIcon;
import com.android.systemui.statusbar.policy.ConfigurationController;

public class MiuiStatusBarConfigurationListener implements ConfigurationController.ConfigurationListener {
    protected ConfigurationController mConfigurationController;
    protected Context mContext;

    public MiuiStatusBarConfigurationListener(ConfigurationController configurationController, Context context) {
        this.mConfigurationController = configurationController;
        this.mContext = context;
    }

    public void start() {
        this.mConfigurationController.addCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        BatteryIcon.getInstance(this.mContext).clear();
    }
}
