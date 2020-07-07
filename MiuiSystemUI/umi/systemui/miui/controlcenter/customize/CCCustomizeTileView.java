package com.android.systemui.miui.controlcenter.customize;

import android.content.Context;
import com.android.systemui.miui.controlcenter.tileImpl.CCQSTileView;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;

public class CCCustomizeTileView extends CCQSTileView {
    public CCCustomizeTileView(Context context, QSIconView qSIconView) {
        super(context, qSIconView);
        setGravity(49);
    }

    /* access modifiers changed from: protected */
    public void handleStateChanged(QSTile.State state) {
        super.handleStateChanged(state);
    }
}
