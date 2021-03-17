package com.android.systemui.plugins.miui.qs;

import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import java.util.Map;

@ProvidesInterface(action = MiuiQSTilePlugin.ACTION, version = 1)
public interface MiuiQSTilePlugin extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_MIUI_QS_TILE";
    public static final int VERSION = 1;

    Map<String, MiuiQSTile> getAllPluginTiles();

    String getDefaultTileWithOrder();

    String getStockTileWithOrder();
}
