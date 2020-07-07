package com.android.systemui.plugins;

import android.content.Context;
import android.os.Looper;
import com.android.systemui.Dependency;

public class PluginInitializerImpl implements PluginInitializer {
    public Looper getBgLooper() {
        return (Looper) Dependency.get(Dependency.BG_LOOPER);
    }

    public void onPluginManagerInit() {
        ((PluginDependencyProvider) Dependency.get(PluginDependencyProvider.class)).allowPluginDependency(ActivityStarter.class);
    }

    public String[] getWhitelistedPlugins(Context context) {
        return context.getResources().getStringArray(R.array.config_pluginWhitelist);
    }

    public PluginEnabler getPluginEnabler(Context context) {
        return new PluginEnablerImpl(context);
    }
}
