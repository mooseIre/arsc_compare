package com.android.systemui.plugins.qs;

import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(action = "com.android.systemui.action.PLUGIN_QS_FACTORY", version = 1)
public interface QSFactory extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_QS_FACTORY";
    public static final int VERSION = 1;

    QSTile createTile(String str);

    QSTile createTile(String str, boolean z);

    QSTileView createTileView(QSTile qSTile, boolean z);
}
