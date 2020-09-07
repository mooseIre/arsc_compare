package com.android.systemui.qs.external;

import com.android.systemui.plugins.qs.QSTile;

public class CustomTileCompat {
    public static void judgeResetState(TileServiceManager tileServiceManager, CustomTile customTile) {
        if (tileServiceManager.isToggleableTile()) {
            customTile.resetStates();
        }
    }

    public static void judgeForceChangeValue(QSTile.State state) {
        if (state instanceof QSTile.BooleanState) {
            ((QSTile.BooleanState) state).value = state.state == 2;
        }
    }
}
