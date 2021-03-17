package com.android.systemui.qs;

import android.content.Context;
import com.android.internal.logging.InstanceId;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.qs.external.TileServices;
import com.android.systemui.qs.logging.QSLogger;

public interface QSHost {

    public interface Callback {
        void onTilesChanged();
    }

    void collapsePanels();

    int getBarState();

    Context getContext();

    InstanceId getNewInstanceId();

    QSLogger getQSLogger();

    TileServices getTileServices();

    UiEventLogger getUiEventLogger();

    Context getUserContext();

    int indexOf(String str);

    boolean isQSFullyCollapsed();

    void removeTile(String str);

    void unmarkTileAsAutoAdded(String str);

    void warn(String str, Throwable th);
}
