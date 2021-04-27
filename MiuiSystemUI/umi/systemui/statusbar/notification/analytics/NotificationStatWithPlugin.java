package com.android.systemui.statusbar.notification.analytics;

import android.content.Context;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.miui.notification.MiuiNotificationStatPlugin;
import com.android.systemui.shared.plugins.PluginManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/* access modifiers changed from: package-private */
public class NotificationStatWithPlugin implements PluginListener<MiuiNotificationStatPlugin>, MiuiNotificationStatPlugin {
    protected List<MiuiNotificationStatPlugin> mPlugins = new ArrayList();

    public NotificationStatWithPlugin() {
        ((PluginManager) Dependency.get(PluginManager.class)).addPluginListener(MiuiNotificationStatPlugin.ACTION, this, MiuiNotificationStatPlugin.class, true);
    }

    public void onPluginConnected(MiuiNotificationStatPlugin miuiNotificationStatPlugin, Context context) {
        this.mPlugins.add(miuiNotificationStatPlugin);
    }

    public void onPluginDisconnected(MiuiNotificationStatPlugin miuiNotificationStatPlugin) {
        this.mPlugins.remove(miuiNotificationStatPlugin);
    }

    @Override // com.android.systemui.plugins.miui.notification.MiuiNotificationStatPlugin
    public void onPluginEvent(Context context, String str, HashMap<String, Object> hashMap) {
        this.mPlugins.forEach(new Consumer(context, str, hashMap) {
            /* class com.android.systemui.statusbar.notification.analytics.$$Lambda$NotificationStatWithPlugin$GXNAQzXvveSJDK6kUF5FoDF4og */
            public final /* synthetic */ Context f$0;
            public final /* synthetic */ String f$1;
            public final /* synthetic */ HashMap f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                NotificationStatWithPlugin.lambda$onPluginEvent$0(this.f$0, this.f$1, this.f$2, (MiuiNotificationStatPlugin) obj);
            }
        });
    }
}
