package com.android.systemui.plugins;

import android.content.Context;
import com.android.systemui.plugins.Plugin;

public interface PluginListener<T extends Plugin> {
    void onPluginConnected(T t, Context context);

    void onPluginDisconnected(T t) {
    }
}
