package com.android.systemui.plugins.miui.qs;

import android.content.Intent;
import com.android.systemui.plugins.qs.QSTile;

public interface MiuiQSTile {
    void addCallback(QSTile.Callback callback);

    String composeChangeAnnouncement();

    Intent getLongClickIntent();

    int getMetricsCategory();

    QSTile.State getState();

    String getTileSpec();

    void handleClick();

    boolean isAvailable();

    QSTile.State newTileState();

    void refreshState(Object obj);

    void removeCallback(QSTile.Callback callback);

    void setListening(boolean z);
}
