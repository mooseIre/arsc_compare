package com.android.systemui.qs;

import android.content.Context;
import com.android.systemui.qs.external.TileServices;

public interface QSHost {

    public interface Callback {
        void onTilesChanged();
    }

    boolean collapseAfterClick();

    void collapsePanels();

    int getBarState();

    Context getContext();

    TileServices getTileServices();

    int indexOf(String str);

    boolean isDriveModeInstalled();

    boolean isQSFullyCollapsed();

    void removeTile(String str);

    void warn(String str, Throwable th);
}
