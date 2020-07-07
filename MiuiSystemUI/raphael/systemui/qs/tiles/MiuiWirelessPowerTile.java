package com.android.systemui.qs.tiles;

import android.content.Intent;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

public class MiuiWirelessPowerTile extends QSTileImpl<QSTile.BooleanState> {
    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        return null;
    }

    public Intent getLongClickIntent() {
        return null;
    }

    public int getMetricsCategory() {
        return -1;
    }

    public CharSequence getTileLabel() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
    }

    public void handleSetListening(boolean z) {
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
    }

    public boolean isAvailable() {
        return false;
    }

    public MiuiWirelessPowerTile(QSHost qSHost) {
        super(qSHost);
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }
}
