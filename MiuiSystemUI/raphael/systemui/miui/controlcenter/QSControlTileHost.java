package com.android.systemui.miui.controlcenter;

import android.content.Context;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;

public class QSControlTileHost extends QSTileHost {
    public QSControlTileHost(Context context, StatusBar statusBar, StatusBarIconController statusBarIconController) {
        super(context, statusBar, statusBarIconController, true);
    }
}
