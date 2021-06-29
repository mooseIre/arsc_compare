package com.android.systemui.statusbar.notification.unimportant;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.miui.notification.UnimportantSdkPlugin;
import com.android.systemui.shared.plugins.PluginManager;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class UnimportantSdkWithPlugin implements PluginListener<UnimportantSdkPlugin>, UnimportantSdkPlugin {
    protected UnimportantSdkPlugin mPlugins = null;

    public UnimportantSdkWithPlugin() {
        ((PluginManager) Dependency.get(PluginManager.class)).addPluginListener(UnimportantSdkPlugin.ACTION, this, UnimportantSdkPlugin.class, true);
    }

    public void onPluginConnected(UnimportantSdkPlugin unimportantSdkPlugin, Context context) {
        this.mPlugins = unimportantSdkPlugin;
    }

    public void onPluginDisconnected(UnimportantSdkPlugin unimportantSdkPlugin) {
        this.mPlugins = null;
    }

    @Override // com.android.systemui.plugins.miui.notification.UnimportantSdkPlugin
    public void init() {
        UnimportantSdkPlugin unimportantSdkPlugin = this.mPlugins;
        if (unimportantSdkPlugin != null) {
            unimportantSdkPlugin.init();
        }
    }

    @Override // com.android.systemui.plugins.miui.notification.UnimportantSdkPlugin
    public int foldReason(StatusBarNotification statusBarNotification, String str, Map<String, Integer> map) {
        UnimportantSdkPlugin unimportantSdkPlugin = this.mPlugins;
        if (unimportantSdkPlugin == null) {
            return 0;
        }
        return unimportantSdkPlugin.foldReason(statusBarNotification, str, map);
    }

    @Override // com.android.systemui.plugins.miui.notification.UnimportantSdkPlugin
    public void updatePushFilter(List<JSONObject> list) {
        UnimportantSdkPlugin unimportantSdkPlugin = this.mPlugins;
        if (unimportantSdkPlugin != null) {
            unimportantSdkPlugin.updatePushFilter(list);
        }
    }
}
