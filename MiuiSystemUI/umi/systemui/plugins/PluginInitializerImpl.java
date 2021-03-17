package com.android.systemui.plugins;

import android.content.Context;
import android.os.Looper;
import com.android.systemui.C0008R$array;
import com.android.systemui.Dependency;
import com.android.systemui.shared.plugins.PluginEnabler;
import com.android.systemui.shared.plugins.PluginInitializer;

public class PluginInitializerImpl implements PluginInitializer {
    private static final boolean WTFS_SHOULD_CRASH = false;
    private boolean mWtfsSet;

    public void handleWtfs() {
    }

    public Looper getBgLooper() {
        return (Looper) Dependency.get(Dependency.BG_LOOPER);
    }

    public void onPluginManagerInit() {
        ((PluginDependencyProvider) Dependency.get(PluginDependencyProvider.class)).allowPluginDependency(ActivityStarter.class);
    }

    public String[] getWhitelistedPlugins(Context context) {
        return context.getResources().getStringArray(C0008R$array.config_pluginWhitelist);
    }

    public PluginEnabler getPluginEnabler(Context context) {
        return new PluginEnablerImpl(context);
    }
}
